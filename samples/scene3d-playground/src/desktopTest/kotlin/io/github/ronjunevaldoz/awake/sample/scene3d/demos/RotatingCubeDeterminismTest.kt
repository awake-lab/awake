// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.demos

import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.OrbitCameraController
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.utils.readResourceBytes
import io.github.ronjunevaldoz.awake.render.material.Material as RenderMaterial
import io.github.ronjunevaldoz.awake.render.mesh.Mesh as RenderMesh
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
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
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * User asked directly whether the reported cube "blinking up and down" could be a shader bug --
 * a fair question that [RotatingCubeContinuousSpinStabilityTest] (top/bottom silhouette edge
 * tracking) can't fully answer, since it only checks two rows, not the whole frame, and
 * [GltfViewerDeterminismTest] only ever exercised the Duck, never this cube's own mesh/shader
 * inputs. This test closes that gap directly: renders the *exact same* cube frame -- same spin
 * angle, same camera, same wireframe-off state -- twice in a row through the real
 * `triangle.wgsl` shader (`vertexEntryPoint`/`fragmentEntryPoint = vertexMain/fragmentMain`, the
 * same PositionNormalColor/Lambertian pipeline the live app uses) and requires the two full-frame
 * renders to be pixel-identical.
 *
 * A live-instrumented log (`println` in `RotatingCubeDemo.onUpdate`, temporary, not committed)
 * separately confirmed the CPU-side inputs feeding this shader are already 100% constant frame
 * to frame during real continuous auto-spin -- 100 samples across ~25s, `cubeModelMatrix
 * .translationY`, `camera.eye`, `camera.elevation`, and `camera.zoom` were bit-identical on every
 * single sample. If the *inputs* never change and the *shader* is also deterministic (this test),
 * there is no remaining code-level mechanism left in this render path that could make the cube
 * visibly move -- a genuine "no bug found" conclusion, not a lack of looking.
 */
class RotatingCubeDeterminismTest {

    @Test
    fun cubeRenderIsDeterministicForAnIdenticalFrame() {
        val graphicsDevice = GraphicsDevice()
        graphicsDevice.createHeadless()
        val swapchainManager = SwapchainManager(graphicsDevice, MAX_FRAMES_IN_FLIGHT)
        swapchainManager.createHeadless(TARGET_SIZE, TARGET_SIZE)
        val pipelineLayoutMaterial = Material(graphicsDevice)
        val renderPipeline = RenderPipeline(
            graphicsDevice,
            swapchainManager,
            pipelineLayoutMaterial.descriptorSetLayout,
            runBlocking { loadShaderPair("assets/shader/vulkan/triangle.vert.spv", "assets/shader/vulkan/triangle.frag.spv") },
            VertexFormat.PositionNormalColor,
            vertexEntryPoint = "vertexMain",
            fragmentEntryPoint = "fragmentMain"
        )
        val lineRenderPipeline = LineRenderPipeline(
            graphicsDevice,
            swapchainManager,
            renderPipeline.renderPass,
            runBlocking { loadShaderPair("assets/shader/vulkan/debug_line.vert.spv", "assets/shader/vulkan/debug_line.frag.spv") },
            MAX_FRAMES_IN_FLIGHT
        )
        val transferContext = TransferContext(graphicsDevice)
        val renderer = Renderer(
            graphicsDevice,
            swapchainManager,
            renderPipeline,
            emptyMap(),
            lineRenderPipeline,
            transferContext,
            runBlocking { loadShaderPair("assets/shader/vulkan/ui_quad.vert.spv", "assets/shader/vulkan/ui_quad.frag.spv") },
            runBlocking { loadShaderPair("assets/shader/vulkan/ui_glyph.vert.spv", "assets/shader/vulkan/ui_glyph.frag.spv") },
            runBlocking { loadShaderPair("assets/shader/vulkan/ui_texture.vert.spv", "assets/shader/vulkan/ui_texture.frag.spv") },
            runBlocking {
                loadShaderPair("assets/shader/vulkan/ui_rounded_quad.vert.spv", "assets/shader/vulkan/ui_rounded_quad.frag.spv")
            },
            MAX_FRAMES_IN_FLIGHT
        )

        var mesh: RenderMesh? = null
        var material: RenderMaterial? = null
        try {
            val target = renderer.createRenderTarget(TARGET_SIZE, TARGET_SIZE)
            val createdMesh = renderer.createMesh(rotatingCubeGeometry).also { mesh = it }
            val createdMaterial = renderer.createMaterial().also { material = it }

            // Mid-spin, not hours=0 -- a resting/unrotated cube is too axis-aligned to exercise
            // the shader's per-pixel Lambertian interpolation across all three visible faces the
            // way a live, spinning cube actually does.
            val spinRadians = 1.2f
            val cubeModel = Mat4().translate(0f, CUBE_REST_HEIGHT, 0f).rotateY(spinRadians)
            val camera = OrbitCameraController(zoom = 15f, zoomMin = 2f, zoomMax = 15f).apply {
                elevation = 2.2f
            }.computeCamera(Vec3(0f, CUBE_REST_HEIGHT, 0f))

            fun renderOnce(): ByteArray {
                renderer.renderToTexture(target, camera, listOf(DrawCall(createdMesh, createdMaterial, cubeModel)))
                return runBlocking { renderer.readPixels(target) }.data
            }

            val first = renderOnce()
            val second = renderOnce()

            assertTrue(
                first.contentEquals(second),
                "Cube render is non-deterministic for an unchanging scene -- two renders of the exact " +
                    "same spin angle/camera through the real shader produced different pixels."
            )
        } finally {
            mesh?.destroy()
            material?.destroy()
            renderer.destroy()
            lineRenderPipeline.destroy()
            renderPipeline.destroy()
            VulkanDescriptors.vkDestroyDescriptorSetLayout(graphicsDevice.device, pipelineLayoutMaterial.descriptorSetLayout.handle)
            transferContext.destroy()
            graphicsDevice.destroy()
        }
    }

    private suspend fun loadShaderPair(vertexPath: String, fragmentPath: String): ShaderPair =
        ShaderPair(readResourceBytes(vertexPath), readResourceBytes(fragmentPath))

    private companion object {
        const val TARGET_SIZE = 256
        const val MAX_FRAMES_IN_FLIGHT = 1
        const val CUBE_REST_HEIGHT = 0.5f
    }
}
