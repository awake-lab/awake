// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan

import io.github.ronjunevaldoz.awake.core.math.Camera
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
import io.github.ronjunevaldoz.awake.vulkan.pipeline.PipelineTable
import io.github.ronjunevaldoz.awake.vulkan.pipeline.RenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.pipeline.UiShaderPairs
import io.github.ronjunevaldoz.awake.vulkan.pipeline.createSceneRenderPass
import io.github.ronjunevaldoz.awake.vulkan.pipeline.ShaderPair
import io.github.ronjunevaldoz.awake.vulkan.renderer.Renderer
import io.github.ronjunevaldoz.awake.vulkan.swapchain.SwapchainManager
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import io.github.ronjunevaldoz.awake.render.material.Material as RenderMaterial
import io.github.ronjunevaldoz.awake.render.mesh.Mesh as RenderMesh

/**
 * Real pixels for [io.github.ronjunevaldoz.awake.render.renderer.Renderer.sceneViewport].
 *
 * An editor sets that rect so the scene stays inside its viewport panel instead of filling the
 * window behind the panels. Everything about it -- the clamp, the aspect, the recorded
 * viewport/scissor -- was previously only compile-verified, and a scissor that silently did
 * nothing would look exactly like the bug it was added to fix. This renders geometry through the
 * real device and reads the framebuffer back.
 */
class RendererSceneViewportTest {

    // One test, not several: each device session creates a Vulkan instance, and a second
    // vkCreateInstance in the same JVM fails. Every headless class in this module is shaped the
    // same way for that reason.
    @Test
    fun theSceneIsConfinedToItsViewportRectAndAnOversizedRectIsClamped() {
        withHeadlessRenderer { renderer ->
            val target = renderer.createRenderTarget(TARGET_SIZE, TARGET_SIZE)
            var mesh: RenderMesh? = null
            var material: RenderMaterial? = null
            try {
                val createdMesh = renderer.createMesh(MeshGeometry(cubeVertices, cubeIndices)).also { mesh = it }
                val createdMaterial = renderer.createMaterial().also { material = it }
                val camera = Camera(
                    eye = Vec3(2.5f, 2f, 4f),
                    center = Vec3(0f, 0f, 0f),
                    fovYRadians = 1f,
                    near = 0.1f,
                    far = 10f,
                )
                val draw = listOf(DrawCall(createdMesh, createdMaterial))

                // Full surface first: the cube has to be visible on both halves, or "nothing on
                // the left" below would pass for the wrong reason.
                renderer.sceneViewport = null
                renderer.renderToTexture(target, camera, draw)
                val whole = runBlocking { renderer.readPixels(target) }.data
                assertTrue(
                    paintedPixels(whole, LEFT_HALF) > MIN_PAINTED,
                    "the unconfined cube must cover the left half, else this test proves nothing",
                )

                // Right half only.
                renderer.sceneViewport = RenderViewport(
                    x = HALF.toFloat(),
                    y = 0f,
                    width = HALF.toFloat(),
                    height = TARGET_SIZE.toFloat(),
                )
                renderer.renderToTexture(target, camera, draw)
                val confined = runBlocking { renderer.readPixels(target) }.data

                assertEquals(
                    0,
                    paintedPixels(confined, LEFT_HALF),
                    "no geometry may reach outside the scene viewport",
                )
                assertTrue(
                    paintedPixels(confined, RIGHT_HALF) > MIN_PAINTED,
                    "the cube must still render inside the scene viewport",
                )

                // A rect the surface cannot contain is clamped, not passed to the driver: Vulkan
                // rejects an out-of-bounds scissor, and a caller's panel bounds can outlive the
                // surface they were measured against (a resize lands between the two).
                renderer.sceneViewport = RenderViewport(
                    x = -100f,
                    y = -100f,
                    width = TARGET_SIZE * 4f,
                    height = TARGET_SIZE * 4f,
                )
                renderer.renderToTexture(target, camera, draw)
                val clamped = runBlocking { renderer.readPixels(target) }.data
                assertTrue(
                    paintedPixels(clamped, 0 until TARGET_SIZE) > MIN_PAINTED,
                    "a clamped rect must still render the scene",
                )
            } finally {
                renderer.sceneViewport = null
                mesh?.destroy()
                material?.destroy()
            }
        }
    }

    private fun withHeadlessRenderer(block: (Renderer) -> Unit) {
        val graphicsDevice = GraphicsDevice()
        graphicsDevice.createHeadless()
        val swapchainManager = SwapchainManager(graphicsDevice, MAX_FRAMES_IN_FLIGHT)
        swapchainManager.createHeadless(TARGET_SIZE, TARGET_SIZE)
        val pipelineLayoutMaterial = Material(graphicsDevice)
        val sceneRenderPass = createSceneRenderPass(graphicsDevice, swapchainManager)
        val renderPipeline = RenderPipeline(
            graphicsDevice,
            swapchainManager,
            sceneRenderPass,
            pipelineLayoutMaterial.descriptorSetLayout,
            runBlocking { shaderPair("assets/shader/vulkan/triangle.vert.spv", "assets/shader/vulkan/triangle.frag.spv") },
            VertexFormat.PositionColorUv,
            vertexEntryPoint = "vertexMain",
            fragmentEntryPoint = "fragmentMain",
        )
        val lineRenderPipeline = LineRenderPipeline(
            graphicsDevice,
            swapchainManager,
            sceneRenderPass,
            runBlocking { shaderPair("assets/shader/vulkan/debug_line.vert.spv", "assets/shader/vulkan/debug_line.frag.spv") },
            MAX_FRAMES_IN_FLIGHT,
        )
        val transferContext = TransferContext(graphicsDevice)
        val renderer = Renderer(
            graphicsDevice = graphicsDevice,
            swapchainManager = swapchainManager,
            pipelines = PipelineTable(primary = renderPipeline),
            lineRenderPipeline = lineRenderPipeline,
            transferContext = transferContext,
            uiShaderPairs = UiShaderPairs(
                quad = runBlocking { shaderPair("assets/shader/vulkan/ui_quad.vert.spv", "assets/shader/vulkan/ui_quad.frag.spv") },
                glyph = runBlocking { shaderPair("assets/shader/vulkan/ui_glyph.vert.spv", "assets/shader/vulkan/ui_glyph.frag.spv") },
                texture = runBlocking {
                    shaderPair("assets/shader/vulkan/ui_texture.vert.spv", "assets/shader/vulkan/ui_texture.frag.spv")
                },
                roundedQuad = runBlocking {
                    shaderPair("assets/shader/vulkan/ui_rounded_quad.vert.spv", "assets/shader/vulkan/ui_rounded_quad.frag.spv")
                },
            ),
            maxFramesInFlight = MAX_FRAMES_IN_FLIGHT,
        )
        try {
            block(renderer)
        } finally {
            renderer.destroy()
            lineRenderPipeline.destroy()
            renderPipeline.destroy()
            // Only the layout was ever used (to build renderPipeline's pipeline layout), so only
            // the layout is destroyed -- same teardown RendererHeadlessPixelBaselineTest documents.
            VulkanDescriptors.vkDestroyDescriptorSetLayout(
                graphicsDevice.device,
                pipelineLayoutMaterial.descriptorSetLayout.handle,
            )
            transferContext.destroy()
            Vulkan.vkDestroyRenderPass(graphicsDevice.device, sceneRenderPass)
            graphicsDevice.destroy()
        }
    }

    private suspend fun shaderPair(vertexPath: String, fragmentPath: String): ShaderPair =
        ShaderPair(readResourceBytes(vertexPath), readResourceBytes(fragmentPath))

    /** Anything that is not the clear colour. The clear is opaque black, and the cube's own
     * vertex colours are not, so "painted" is simply a non-zero colour channel. */
    private fun paintedPixels(pixels: ByteArray, xRange: IntRange): Int {
        var count = 0
        for (y in 0 until TARGET_SIZE) {
            for (x in xRange) {
                val offset = (y * TARGET_SIZE + x) * 4
                val r = pixels[offset].toInt() and 0xFF
                val g = pixels[offset + 1].toInt() and 0xFF
                val b = pixels[offset + 2].toInt() and 0xFF
                if (r + g + b > 0) count += 1
            }
        }
        return count
    }

    private companion object {
        // Copied from RendererHeadlessPixelBaselineTest for the same reason it copies from
        // samples/hello-cube: this module is a dependency OF the samples, not the other way.
        val cubeVertices = floatArrayOf(
            -0.5f, -0.5f, -0.5f, 0f, 0f, 0f, 0f, 0f,
            0.5f, -0.5f, -0.5f, 1f, 0f, 0f, 1f, 0f,
            0.5f, 0.5f, -0.5f, 1f, 1f, 0f, 1f, 1f,
            -0.5f, 0.5f, -0.5f, 0f, 1f, 0f, 0f, 1f,
            -0.5f, -0.5f, 0.5f, 0f, 0f, 1f, 0f, 0f,
            0.5f, -0.5f, 0.5f, 1f, 0f, 1f, 1f, 0f,
            0.5f, 0.5f, 0.5f, 1f, 1f, 1f, 1f, 1f,
            -0.5f, 0.5f, 0.5f, 0f, 1f, 1f, 0f, 1f,
        )
        val cubeIndices = intArrayOf(
            0, 1, 2, 2, 3, 0,
            4, 5, 6, 6, 7, 4,
            0, 3, 7, 7, 4, 0,
            1, 5, 6, 6, 2, 1,
            0, 4, 5, 5, 1, 0,
            3, 2, 6, 6, 7, 3,
        )

        const val TARGET_SIZE = 128
        const val HALF = TARGET_SIZE / 2
        const val MAX_FRAMES_IN_FLIGHT = 2
        const val MIN_PAINTED = 100
        val LEFT_HALF = 0 until HALF
        val RIGHT_HALF = HALF until TARGET_SIZE
    }
}
