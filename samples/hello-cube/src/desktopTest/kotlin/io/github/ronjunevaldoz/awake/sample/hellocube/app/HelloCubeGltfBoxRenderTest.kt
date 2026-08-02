// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.hellocube.app

import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.mesh.gltf.GltfParser
import io.github.ronjunevaldoz.awake.core.utils.readResourceBytes
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.sample.hellocube.debug.saveDebugPng
import io.github.ronjunevaldoz.awake.sample.hellocube.scene.gltfBoxGlb
import io.github.ronjunevaldoz.awake.sample.hellocube.scene.sampleVertexStride
import io.github.ronjunevaldoz.awake.vulkan.commands.TransferContext
import io.github.ronjunevaldoz.awake.vulkan.debug.LineRenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanDescriptors
import io.github.ronjunevaldoz.awake.vulkan.material.Material
import io.github.ronjunevaldoz.awake.vulkan.pipeline.RenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.pipeline.ShaderPair
import io.github.ronjunevaldoz.awake.vulkan.renderer.Renderer as VulkanRenderer
import io.github.ronjunevaldoz.awake.vulkan.swapchain.SwapchainManager
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

/**
 * Proves the glTF model-loading track's actual deliverable -- `GltfParser.parseScene` ->
 * `LoadedScene` -> `renderer.createMesh()` -> a real rendered pixel -- end to end, on the
 * critical-path Vulkan backend, purely inside a JVM test process. Same headless
 * `GraphicsDevice.createHeadless()`/`SwapchainManager.createHeadless()`/`RenderPipeline`/
 * `Renderer` construction `awake:backend:vulkan`'s own `RendererHeadlessPixelBaselineTest`
 * uses (no GLFW window, no swapchain, ever created) -- driven directly against `Renderer`
 * (not the full `SceneGameRuntime`/`RenderSystem` game lifecycle: `SceneGameRuntime.ready()`
 * eagerly runs an infrastructure-system draw pass that assumes a real presentable swapchain
 * image is available, which a headless-created `SwapchainManager` doesn't provide -- driving
 * `Renderer.renderToTexture()` directly, the same offscreen path `readback()`/
 * `RendererHeadlessPixelBaselineTest` already use, sidesteps that entirely).
 *
 * Renders the real Khronos `Box.glb` fixture's parsed primitive (see `GltfBoxAsset.kt`) with
 * a camera pointed straight at it, and asserts the readback's center pixel is substantially
 * brighter than the renderer's opaque-black clear color -- a `LoadedScene` that silently
 * produced zero triangles, or bad vertex/index data, would leave that pixel black, so this is
 * a real correctness check, not just a "did it throw" smoke test.
 */
class HelloCubeGltfBoxRenderTest {
    @Test
    fun gltfBoxRendersNonBackgroundPixels() {
        val graphicsDevice = GraphicsDevice()
        graphicsDevice.createHeadless()
        val swapchainManager = SwapchainManager(graphicsDevice, 1)
        swapchainManager.createHeadless(TARGET_SIZE, TARGET_SIZE)
        val pipelineLayoutMaterial = Material(graphicsDevice)
        val renderPipeline = RenderPipeline(
            graphicsDevice,
            swapchainManager,
            pipelineLayoutMaterial.descriptorSetLayout,
            runBlocking {
                ShaderPair(
                    readResourceBytes("assets/shader/vulkan/triangle.vert.spv"),
                    readResourceBytes("assets/shader/vulkan/triangle.frag.spv")
                )
            },
            sampleVertexStride,
            // hello-cube's own "triangle" shader set (see HelloCubeVulkanBootstrap.kt's
            // gameShaderSet("triangle")) uses "vertexMain"/"fragmentMain" entry points, not
            // RenderPipeline's "main" default.
            vertexEntryPoint = "vertexMain",
            fragmentEntryPoint = "fragmentMain"
        )
        val lineRenderPipeline = LineRenderPipeline(
            graphicsDevice,
            swapchainManager,
            renderPipeline.renderPass,
            runBlocking {
                ShaderPair(
                    readResourceBytes("assets/shader/vulkan/debug_line.vert.spv"),
                    readResourceBytes("assets/shader/vulkan/debug_line.frag.spv")
                )
            }
        )
        val transferContext = TransferContext(graphicsDevice)
        val renderer = VulkanRenderer(
            graphicsDevice,
            swapchainManager,
            renderPipeline,
            lineRenderPipeline,
            transferContext,
            runBlocking {
                ShaderPair(
                    readResourceBytes("assets/shader/vulkan/ui_quad.vert.spv"),
                    readResourceBytes("assets/shader/vulkan/ui_quad.frag.spv")
                )
            },
            runBlocking {
                ShaderPair(
                    readResourceBytes("assets/shader/vulkan/ui_glyph.vert.spv"),
                    readResourceBytes("assets/shader/vulkan/ui_glyph.frag.spv")
                )
            },
            runBlocking {
                ShaderPair(
                    readResourceBytes("assets/shader/vulkan/ui_texture.vert.spv"),
                    readResourceBytes("assets/shader/vulkan/ui_texture.frag.spv")
                )
            },
            runBlocking {
                ShaderPair(
                    readResourceBytes("assets/shader/vulkan/ui_rounded_quad.vert.spv"),
                    readResourceBytes("assets/shader/vulkan/ui_rounded_quad.frag.spv")
                )
            },
            1
        )

        var mesh: io.github.ronjunevaldoz.awake.render.mesh.Mesh? = null
        var material: io.github.ronjunevaldoz.awake.render.material.Material? = null
        try {
            // The actual thing under test: GltfParser.parseScene() -> LoadedScene ->
            // renderer.createMesh(), no hand-rolled MeshGeometry.
            val primitive = GltfParser.parseScene(gltfBoxGlb()).meshes.single().primitives.single()
            val createdMesh = renderer.createMesh(MeshGeometry(primitive.vertices, primitive.indices))
                .also { mesh = it }
            val createdMaterial = renderer.createMaterial().also { material = it }

            val target = renderer.createRenderTarget(TARGET_SIZE, TARGET_SIZE)
            val camera = Camera(
                eye = Vec3(2.5f, 2f, 4f),
                center = Vec3(0f, 0f, 0f),
                fovYRadians = 1f,
                near = 0.1f,
                far = 10f,
                flipYForClipSpace = renderer.flipYForClipSpace
            )
            try {
                renderer.renderToTexture(target, camera, listOf(DrawCall(createdMesh, createdMaterial)))
                val pixels = runBlocking { renderer.readPixels(target) }
                val outDir = File("build/gltf-box-render-test").apply { mkdirs() }
                saveDebugPng(pixels.data, pixels.width, pixels.height, File(outDir, "gltf-box.png").path)

                val center = (TARGET_SIZE / 2) * TARGET_SIZE * 4 + (TARGET_SIZE / 2) * 4
                val r = pixels.data[center].toInt() and 0xFF
                val g = pixels.data[center + 1].toInt() and 0xFF
                val b = pixels.data[center + 2].toInt() and 0xFF
                assertTrue(
                    r > BACKGROUND_BRIGHTNESS_THRESHOLD ||
                        g > BACKGROUND_BRIGHTNESS_THRESHOLD ||
                        b > BACKGROUND_BRIGHTNESS_THRESHOLD,
                    "Expected the glTF-loaded Box.glb primitive to render a non-background " +
                        "pixel at the readback's center (camera is pointed straight at it), " +
                        "but got rgb=($r, $g, $b) -- the renderer's clear color is opaque " +
                        "black. Loaded geometry likely isn't actually reaching the GPU."
                )
            } finally {
                target.destroy()
            }
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

    private companion object {
        const val TARGET_SIZE = 256
        const val BACKGROUND_BRIGHTNESS_THRESHOLD = 40
    }
}
