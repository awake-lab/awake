// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.demos

import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.utils.readResourceBytes
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanDescriptors
import io.github.ronjunevaldoz.awake.vulkan.material.Material
import io.github.ronjunevaldoz.awake.vulkan.mesh.SkinnedInstanceBuffer
import io.github.ronjunevaldoz.awake.vulkan.pipeline.RenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.pipeline.ShaderPair
import io.github.ronjunevaldoz.awake.vulkan.swapchain.SwapchainManager
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Builds the animated-instancing GPU resources against a real (headless) device: the
 * skinned-instanced pipeline -- whose layout must declare BOTH the material set and
 * `skinned_instanced.wgsl`'s `@group(1)` joint-palette storage set -- and a
 * [SkinnedInstanceBuffer] (storage buffer + its own descriptor pool/set), written with two
 * differently-posed instances and torn down again.
 *
 * A creation/lifecycle check, not a pixel one: the offscreen `renderToTexture` path binds only
 * the primary pipeline before recording (see `Renderer.renderToTexture`), so it cannot draw an
 * instanced call at all -- a rendered baseline for this feature needs the on-screen frame path,
 * which has no headless readback. What this does catch is the failure mode that actually bites
 * here: a pipeline layout that doesn't match the descriptor sets the shader declares.
 *
 * Shares the same headless-device wiring/one-device-per-class constraint as
 * [RotatingCubePixelBaselineTest] -- see its doc comment (`forkEvery = 1` in this module's
 * build script is what keeps both classes from colliding on Vulkan's global loader state).
 */
class SkinnedInstancedPipelineSmokeTest {

    @Test
    fun buildsSkinnedInstancedPipelineAndPaletteBuffer() {
        val graphicsDevice = GraphicsDevice()
        graphicsDevice.createHeadless()
        val swapchainManager = SwapchainManager(graphicsDevice, MAX_FRAMES_IN_FLIGHT)
        swapchainManager.createHeadless(TARGET_SIZE, TARGET_SIZE)
        val materialLayout = Material.createDescriptorSetLayout(graphicsDevice)
        val paletteLayout = SkinnedInstanceBuffer.createDescriptorSetLayout(graphicsDevice)
        var pipeline: RenderPipeline? = null
        var paletteBuffer: SkinnedInstanceBuffer? = null
        try {
            pipeline = RenderPipeline(
                graphicsDevice,
                swapchainManager,
                materialLayout,
                runBlocking {
                    ShaderPair(
                        readResourceBytes("assets/shader/vulkan/skinned_instanced.vert.spv"),
                        readResourceBytes("assets/shader/vulkan/skinned_instanced.frag.spv"),
                    )
                },
                VertexFormat.PositionNormalColorSkin,
                // naga preserves the WGSL entry-point names -- see RotatingCubePixelBaselineTest.
                vertexEntryPoint = "vertexMain",
                fragmentEntryPoint = "fragmentMain",
                instanced = true,
                extraDescriptorSetLayouts = listOf(paletteLayout),
            )
            assertTrue(pipeline.graphicsPipeline.first() != 0L, "skinned-instanced pipeline must be created")

            paletteBuffer = SkinnedInstanceBuffer(graphicsDevice, framesInFlight = MAX_FRAMES_IN_FLIGHT)
            // Two instances, deliberately different poses: the first identity, the second with
            // its root joint translated -- the exact "same mesh, same draw, different pose"
            // shape the demo produces.
            val rest = Mat4().data
            val moved = Mat4().translate(1f, 0f, 0f).data
            paletteBuffer.update(0, listOf(rest, moved))
        } finally {
            paletteBuffer?.destroy()
            pipeline?.destroy()
            VulkanDescriptors.vkDestroyDescriptorSetLayout(graphicsDevice.device, paletteLayout.handle)
            VulkanDescriptors.vkDestroyDescriptorSetLayout(graphicsDevice.device, materialLayout.handle)
            // No swapchainManager.destroy(): a headless SwapchainManager never created a
            // swapchain, and vkDestroySwapchainKHR is an unresolved (null) entry point without
            // the surface extension -- calling it segfaults. Same omission
            // RotatingCubePixelBaselineTest's own teardown makes.
            graphicsDevice.destroy()
        }
    }

    private companion object {
        const val TARGET_SIZE = 64
        const val MAX_FRAMES_IN_FLIGHT = 1
    }
}
