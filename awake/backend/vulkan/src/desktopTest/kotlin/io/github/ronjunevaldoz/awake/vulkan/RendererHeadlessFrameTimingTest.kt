// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan

import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.utils.readResourceBytes
import io.github.ronjunevaldoz.awake.engine.app.core.FrameStats
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.testing.FrameSpans
import io.github.ronjunevaldoz.awake.testing.formatTimingBaseline
import io.github.ronjunevaldoz.awake.vulkan.commands.TransferContext
import io.github.ronjunevaldoz.awake.vulkan.debug.LineRenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanDescriptors
import io.github.ronjunevaldoz.awake.vulkan.material.Material
import io.github.ronjunevaldoz.awake.vulkan.pipeline.OpaqueRenderFeature
import io.github.ronjunevaldoz.awake.vulkan.pipeline.PipelineTable
import io.github.ronjunevaldoz.awake.vulkan.pipeline.RenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.pipeline.ShaderPair
import io.github.ronjunevaldoz.awake.vulkan.pipeline.UiRenderFeature
import io.github.ronjunevaldoz.awake.vulkan.pipeline.UiShaderPairs
import io.github.ronjunevaldoz.awake.vulkan.pipeline.createSceneRenderPass
import io.github.ronjunevaldoz.awake.vulkan.renderer.Renderer
import io.github.ronjunevaldoz.awake.vulkan.swapchain.SwapchainManager
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.time.TimeSource

/**
 * Renders the same headless cube scene [RendererHeadlessPixelBaselineTest] verifies for pixel
 * correctness, but N times in a row, and reports total/mean/p50/p95/p99 frame time -- the 3D
 * backend's equivalent of `UiShowcaseLayoutCostTest`'s frame-cost reporting style.
 *
 * **Diagnostic only, not a regression gate.** Unlike `UiShowcaseLayoutCostTest` (JVM wall-clock
 * timing, trustworthy), this renders through lavapipe/Mesa's software Vulkan implementation in
 * CI (no real GPU) -- timing there is noisy and has no fixed relationship to real-GPU
 * performance, so this test only `println`s numbers and asserts nothing beyond "it ran and
 * produced non-zero timings". Do not add a hard threshold assertion here without first
 * confirming this test runs on real hardware in CI.
 */
class RendererHeadlessFrameTimingTest {

    @Test
    fun headlessCubeRenderFrameTiming() {
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
            runBlocking {
                loadShaderPair(
                    "assets/shader/vulkan/triangle.vert.spv",
                    "assets/shader/vulkan/triangle.frag.spv",
                )
            },
            VertexFormat.PositionColorUv,
            vertexEntryPoint = "vertexMain",
            fragmentEntryPoint = "fragmentMain",
        )
        val lineRenderPipeline = LineRenderPipeline(
            graphicsDevice,
            swapchainManager,
            renderPipeline.renderPass,
            runBlocking {
                loadShaderPair(
                    "assets/shader/vulkan/debug_line.vert.spv",
                    "assets/shader/vulkan/debug_line.frag.spv",
                )
            },
            MAX_FRAMES_IN_FLIGHT,
        )
        val transferContext = TransferContext(graphicsDevice)
        val renderer = Renderer(
            graphicsDevice = graphicsDevice,
            swapchainManager = swapchainManager,
            pipelines = PipelineTable(primary = renderPipeline),
            renderFeatures = listOf(OpaqueRenderFeature(lineRenderPipeline), UiRenderFeature()),
            transferContext = transferContext,
            uiShaderPairs = UiShaderPairs(
                quad = runBlocking {
                    loadShaderPair(
                        "assets/shader/vulkan/ui_quad.vert.spv",
                        "assets/shader/vulkan/ui_quad.frag.spv",
                    )
                },
                glyph = runBlocking {
                    loadShaderPair(
                        "assets/shader/vulkan/ui_glyph.vert.spv",
                        "assets/shader/vulkan/ui_glyph.frag.spv",
                    )
                },
                texture = runBlocking {
                    loadShaderPair(
                        "assets/shader/vulkan/ui_texture.vert.spv",
                        "assets/shader/vulkan/ui_texture.frag.spv",
                    )
                },
                roundedQuad = runBlocking {
                    loadShaderPair(
                        "assets/shader/vulkan/ui_rounded_quad.vert.spv",
                        "assets/shader/vulkan/ui_rounded_quad.frag.spv",
                    )
                },
            ),
            maxFramesInFlight = MAX_FRAMES_IN_FLIGHT,
        )

        var mesh: io.github.ronjunevaldoz.awake.render.mesh.Mesh? = null
        var material: io.github.ronjunevaldoz.awake.render.material.Material? = null
        try {
            val target = renderer.createRenderTarget(TARGET_SIZE, TARGET_SIZE)
            val camera = Camera(
                eye = Vec3(2.5f, 2f, 4f),
                center = Vec3(0f, 0f, 0f),
                fovYRadians = 1f,
                near = 0.1f,
                far = 10f,
            )
            val createdMesh =
                renderer.createMesh(MeshGeometry(cubeVertices, cubeIndices)).also { mesh = it }
            val createdMaterial = renderer.createMaterial().also { material = it }
            val drawCalls = listOf(DrawCall(createdMesh, createdMaterial))

            // Warm up (JIT + first-frame allocation costs) before the measured frames, same
            // rationale as UiShowcaseLayoutCostTest.measureOneFrame's warmupFrames.
            repeat(WARMUP_FRAMES) {
                renderer.renderToTexture(target, camera, drawCalls)
                runBlocking { renderer.readPixels(target) }
            }

            val stats = FrameStats(
                sampleWindowSeconds = Float.MAX_VALUE,
                percentileWindowSize = FRAME_COUNT,
            )
            var totalNanos = 0L
            repeat(FRAME_COUNT) {
                val start = TimeSource.Monotonic.markNow()
                renderer.renderToTexture(target, camera, drawCalls)
                runBlocking { renderer.readPixels(target) }
                val elapsedNanos = start.elapsedNow().inWholeNanoseconds
                totalNanos += elapsedNanos
                stats.update(elapsedNanos / 1_000_000_000f)
            }

            val totalMs = totalNanos / 1_000_000.0
            val meanMs = totalMs / FRAME_COUNT
            println(
                "Headless cube render timing over $FRAME_COUNT frames (lavapipe/Mesa software " +
                        "Vulkan in CI -- diagnostic only, NOT representative of real-GPU timing, " +
                        "NOT a regression gate):\n" +
                        "  total=%.3fms mean=%.3fms p50=%.3fms p95=%.3fms p99=%.3fms".format(
                            totalMs,
                            meanMs,
                            stats.p50FrameTimeMs,
                            stats.p95FrameTimeMs,
                            stats.p99FrameTimeMs,
                        ),
            )

            check(totalNanos > 0) { "headless frame timing measured zero time -- instrumentation broken" }

            // Second measurement, same scene geometry but [BATCH_DRAW_CALLS] draw calls per
            // frame instead of one. The one-cube number above is dominated by submit/fence/
            // readback and can't resolve a change in per-draw-call recording cost at all; this
            // one multiplies exactly that path by BATCH_DRAW_CALLS while leaving everything
            // else identical, which is what makes a before/after of the draw path readable.
            // readPixels is deliberately outside the span: it is the same cost either way.
            val batchDrawCalls = List(BATCH_DRAW_CALLS) { DrawCall(createdMesh, createdMaterial) }
            repeat(WARMUP_FRAMES) { renderer.renderToTexture(target, camera, batchDrawCalls) }
            val spans = FrameSpans()
            repeat(FRAME_COUNT) {
                spans.span(BATCH_SPAN) { renderer.renderToTexture(target, camera, batchDrawCalls) }
            }
            spans.requireAllSpansClosed()
            println(
                "Headless $BATCH_DRAW_CALLS-draw-call frame timing over $FRAME_COUNT frames:\n" +
                        formatTimingBaseline(
                            spans.meansMs(),
                            note = "mean ms per frame, prepare+record+submit+fence, no readback",
                        ),
            )
        } finally {
            // Same teardown order/reasoning as RendererHeadlessPixelBaselineTest.
            mesh?.destroy()
            material?.destroy()
            renderer.destroy()
            renderPipeline.destroy()
            VulkanDescriptors.vkDestroyDescriptorSetLayout(
                graphicsDevice.device,
                pipelineLayoutMaterial.descriptorSetLayout.handle,
            )
            transferContext.destroy()
            Vulkan.vkDestroyRenderPass(graphicsDevice.device, sceneRenderPass)
            graphicsDevice.destroy()
        }
    }

    private companion object {
        const val TARGET_SIZE = 128
        const val MAX_FRAMES_IN_FLIGHT = 1
        const val WARMUP_FRAMES = 5
        const val FRAME_COUNT = 100

        /** Enough draw calls that per-draw-call recording cost is above the noise floor of a
         * single submit+fence round trip, without making the measurement GPU-bound. */
        const val BATCH_DRAW_CALLS = 64
        const val BATCH_SPAN = "opaque-batch-frame"

        val cubeVertices = floatArrayOf(
            -0.5f, -0.5f, -0.5f, 0f, 0f, 0f, 0f, 0f, // v0
            0.5f, -0.5f, -0.5f, 1f, 0f, 0f, 1f, 0f, // v1
            0.5f, 0.5f, -0.5f, 1f, 1f, 0f, 1f, 1f, // v2
            -0.5f, 0.5f, -0.5f, 0f, 1f, 0f, 0f, 1f, // v3
            -0.5f, -0.5f, 0.5f, 0f, 0f, 1f, 0f, 0f, // v4
            0.5f, -0.5f, 0.5f, 1f, 0f, 1f, 1f, 0f, // v5
            0.5f, 0.5f, 0.5f, 1f, 1f, 1f, 1f, 1f, // v6
            -0.5f, 0.5f, 0.5f, 0f, 1f, 1f, 0f, 1f, // v7
        )
        val cubeIndices = intArrayOf(
            0, 1, 2, 2, 3, 0, // back
            4, 5, 6, 6, 7, 4, // front
            0, 3, 7, 7, 4, 0, // left
            1, 5, 6, 6, 2, 1, // right
            0, 4, 5, 5, 1, 0, // bottom
            3, 2, 6, 6, 7, 3, // top
        )
    }
}

/** Reads [vertexPath]/[fragmentPath] into one [ShaderPair] -- copied from
 * RendererHeadlessPixelBaselineTest.kt (Kotlin top-level `private fun`s are file-scoped, so this
 * can't be reused directly across the two test files). */
private suspend fun loadShaderPair(vertexPath: String, fragmentPath: String): ShaderPair =
    ShaderPair(readResourceBytes(vertexPath), readResourceBytes(fragmentPath))
