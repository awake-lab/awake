// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio

import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.utils.readResourceBytes
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.RenderViewport
import io.github.ronjunevaldoz.awake.vulkan.commands.TransferContext
import io.github.ronjunevaldoz.awake.vulkan.debug.LineRenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanDescriptors
import io.github.ronjunevaldoz.awake.vulkan.material.Material
import io.github.ronjunevaldoz.awake.vulkan.pipeline.RenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.pipeline.ShaderPair
import io.github.ronjunevaldoz.awake.vulkan.renderer.Renderer
import io.github.ronjunevaldoz.awake.vulkan.swapchain.SwapchainManager
import kotlinx.coroutines.runBlocking
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertTrue

private const val SIZE = 256
private const val MAX_FRAMES_IN_FLIGHT = 2

/**
 * Renders studio's own scene through a real Vulkan device and asserts which way up it came out.
 *
 * Everything else about the 3D path is checked without drawing anything: unit tests for the math,
 * semantic tests for the shell. That leaves the class of bug this exists for -- a scene rendered
 * upside down, mirrored, or into the wrong part of the surface -- invisible to the whole suite,
 * which is exactly what happened when the viewport rect landed.
 *
 * The PNG is written either way, so a failure can be looked at rather than only read.
 */
class StudioSceneFrameCaptureTest {

    @Test
    fun theSceneRendersUprightInsideItsViewportRect() {
        withHeadlessRenderer { renderer -> captureAndAssert(renderer) }
    }

    /** The two frames this exists to look at: full surface, then a top-half viewport. */
    private fun captureAndAssert(renderer: Renderer) {
        val target = renderer.createRenderTarget(SIZE, SIZE)
        val ground = renderer.createMesh(MeshGeometry(groundVertices, groundIndices, VertexFormat.PositionColorUv))
        val cube = renderer.createMesh(MeshGeometry(cubeVertices, cubeIndices, VertexFormat.PositionColorUv))
        val material = renderer.createMaterial()
        val camera = Camera(
            eye = Vec3(0f, 5f, 10f),
            center = Vec3(0f, 0.5f, 0f),
            fovYRadians = 0.785f,
            near = 0.1f,
            far = 100f,
        )
        val draws = listOf(
            DrawCall(ground, material),
            DrawCall(cube, material, Mat4().translate(0f, 0.5f, 0f)),
        )
        try {
            renderer.sceneViewport = RenderViewport(0f, 0f, SIZE.toFloat(), SIZE.toFloat())
            renderer.renderToTexture(target, camera, draws)
            val pixels = runBlocking { renderer.readPixels(target) }.data
            writePng(pixels, File("/tmp/studio-scene-frame.png"))
            // The camera looks down at the ground, so the ground fills the LOWER part of the image
            // and the clear colour the upper. A flipped scene fails here.
            val upper = paintedRows(pixels, 0 until SIZE / 3)
            val lower = paintedRows(pixels, SIZE * 2 / 3 until SIZE)
            assertTrue(lower > upper, "ground must be below: upper=$upper lower=$lower (/tmp/studio-scene-frame.png)")

            // Same scene into the TOP half only. Vulkan's NDC is +Y down and this engine flips Y
            // inside the projection, so a viewport with a Y offset is exactly where the flip and
            // the rect could disagree -- content would land in the opposite half from the rect.
            renderer.sceneViewport = RenderViewport(0f, 0f, SIZE.toFloat(), SIZE / 2f)
            renderer.renderToTexture(target, camera, draws)
            val halfPixels = runBlocking { renderer.readPixels(target) }.data
            writePng(halfPixels, File("/tmp/studio-scene-half.png"))
            val inTop = paintedRows(halfPixels, 0 until SIZE / 2)
            val inBottom = paintedRows(halfPixels, SIZE / 2 until SIZE)
            assertTrue(inTop > inBottom, "top-half viewport must render on top: $inTop vs $inBottom")
        } finally {
            ground.destroy()
            cube.destroy()
            material.destroy()
            renderer.sceneViewport = null
        }
    }

    private fun withHeadlessRenderer(block: (Renderer) -> Unit) {
        val graphicsDevice = GraphicsDevice()
        graphicsDevice.createHeadless()
        val swapchainManager = SwapchainManager(graphicsDevice, MAX_FRAMES_IN_FLIGHT)
        swapchainManager.createHeadless(SIZE, SIZE)
        val pipelineLayoutMaterial = Material(graphicsDevice)
        val renderPipeline = RenderPipeline(
            graphicsDevice,
            swapchainManager,
            pipelineLayoutMaterial.descriptorSetLayout,
            // The triangle pipeline, not lit_shadow: that one declares a shadow-map descriptor the
            // plain Material layout does not, and this test is about which way up the scene comes
            // out -- a projection and viewport property, not a shading one.
            runBlocking { shaders("triangle") },
            VertexFormat.PositionColorUv,
            vertexEntryPoint = "vertexMain",
            fragmentEntryPoint = "fragmentMain",
        )
        val lineRenderPipeline = LineRenderPipeline(
            graphicsDevice,
            swapchainManager,
            renderPipeline.renderPass,
            runBlocking { shaders("debug_line") },
            MAX_FRAMES_IN_FLIGHT,
        )
        val transferContext = TransferContext(graphicsDevice)
        val renderer = Renderer(
            graphicsDevice,
            swapchainManager,
            renderPipeline,
            emptyMap(),
            lineRenderPipeline,
            transferContext,
            runBlocking { shaders("ui_quad") },
            runBlocking { shaders("ui_glyph") },
            runBlocking { shaders("ui_texture") },
            runBlocking { shaders("ui_rounded_quad") },
            MAX_FRAMES_IN_FLIGHT,
        )
        try {
            block(renderer)
        } finally {
            renderer.destroy()
            lineRenderPipeline.destroy()
            renderPipeline.destroy()
            VulkanDescriptors.vkDestroyDescriptorSetLayout(
                graphicsDevice.device,
                pipelineLayoutMaterial.descriptorSetLayout.handle,
            )
            transferContext.destroy()
            graphicsDevice.destroy()
        }
    }

    private companion object {
        // Position vec3, colour vec3, uv vec2 -- the layout triangle.wgsl expects. A ground quad
        // at y=0 and studio's unit cube, which is all the scene needs to answer "which way up".
        val groundVertices = floatArrayOf(
            -5f, 0f, -5f, 0.4f, 0.4f, 0.45f, 0f, 0f,
            5f, 0f, -5f, 0.4f, 0.4f, 0.45f, 1f, 0f,
            5f, 0f, 5f, 0.4f, 0.4f, 0.45f, 1f, 1f,
            -5f, 0f, 5f, 0.4f, 0.4f, 0.45f, 0f, 1f,
        )
        val groundIndices = intArrayOf(0, 1, 2, 2, 3, 0)
        val cubeVertices = floatArrayOf(
            -0.5f, -0.5f, -0.5f, 1f, 0.3f, 0.3f, 0f, 0f,
            0.5f, -0.5f, -0.5f, 1f, 0.3f, 0.3f, 1f, 0f,
            0.5f, 0.5f, -0.5f, 1f, 0.3f, 0.3f, 1f, 1f,
            -0.5f, 0.5f, -0.5f, 1f, 0.3f, 0.3f, 0f, 1f,
            -0.5f, -0.5f, 0.5f, 1f, 0.6f, 0.3f, 0f, 0f,
            0.5f, -0.5f, 0.5f, 1f, 0.6f, 0.3f, 1f, 0f,
            0.5f, 0.5f, 0.5f, 1f, 0.6f, 0.3f, 1f, 1f,
            -0.5f, 0.5f, 0.5f, 1f, 0.6f, 0.3f, 0f, 1f,
        )
        val cubeIndices = intArrayOf(
            0, 1, 2, 2, 3, 0,
            4, 5, 6, 6, 7, 4,
            0, 3, 7, 7, 4, 0,
            1, 5, 6, 6, 2, 1,
            0, 4, 5, 5, 1, 0,
            3, 2, 6, 6, 7, 3,
        )
    }

    private suspend fun shaders(name: String): ShaderPair = ShaderPair(
        readResourceBytes("assets/shader/vulkan/$name.vert.spv"),
        readResourceBytes("assets/shader/vulkan/$name.frag.spv"),
    )

    /** Pixels that are neither the clear colour nor black -- i.e. lit geometry. */
    private fun paintedRows(pixels: ByteArray, rows: IntRange): Int {
        var count = 0
        for (y in rows) {
            for (x in 0 until SIZE) {
                val offset = (y * SIZE + x) * 4
                val r = pixels[offset].toInt() and 0xFF
                val g = pixels[offset + 1].toInt() and 0xFF
                val b = pixels[offset + 2].toInt() and 0xFF
                if (r + g + b > 24) count += 1
            }
        }
        return count
    }

    private fun writePng(pixels: ByteArray, file: File) {
        val image = BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB)
        var offset = 0
        for (y in 0 until SIZE) {
            for (x in 0 until SIZE) {
                val r = pixels[offset].toInt() and 0xFF
                val g = pixels[offset + 1].toInt() and 0xFF
                val b = pixels[offset + 2].toInt() and 0xFF
                val a = pixels[offset + 3].toInt() and 0xFF
                image.setRGB(x, y, (a shl 24) or (r shl 16) or (g shl 8) or b)
                offset += 4
            }
        }
        ImageIO.write(image, "png", file)
    }
}
