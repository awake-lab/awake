// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.core.utils.readResourceBytes
import io.github.ronjunevaldoz.awake.testing.comparePixels
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.vulkan.commands.TransferContext
import io.github.ronjunevaldoz.awake.vulkan.debug.LineRenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanDescriptors
import io.github.ronjunevaldoz.awake.vulkan.material.Material
import io.github.ronjunevaldoz.awake.vulkan.pipeline.RenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.renderer.Renderer
import io.github.ronjunevaldoz.awake.vulkan.swapchain.SwapchainManager
import kotlinx.coroutines.runBlocking
import org.junit.AfterClass
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertTrue

private fun writeUiBaselinePng(pixels: ByteArray, width: Int, height: Int, file: File) {
    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    var offset = 0
    for (y in 0 until height) {
        for (x in 0 until width) {
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

class RendererHeadlessUiGlyphBaselineTest {

    @Test
    fun headlessUiGlyphRenderMatchesBaseline() {
        withHeadlessUiRenderer { renderer ->
            val font = BitmapFont()
            val target = renderer.createRenderTarget(TARGET_SIZE, TARGET_SIZE)
            val glyphs = buildList {
                addAll(glyphRun("AWAKE", font, x = 14f, y = 18f, scale = 2f, color = Color(0.96f, 0.97f, 1f, 1f)))
                addAll(glyphRun("UI_01", font, x = 16f, y = 54f, scale = 2f, color = Color(0.6f, 0.78f, 1f, 1f)))
            }

            renderer.renderUiGlyphsToTexture(target, glyphs, font)
            val pixels = runBlocking { renderer.readPixels(target) }

            val baselineFile = baselineOutputFile()
            if (!baselineFile.exists()) {
                baselineFile.parentFile.mkdirs()
                baselineFile.writeBytes(pixels.data)
                assertTrue(
                    false,
                    "Recorded missing UI glyph baseline to ${baselineFile.absolutePath}. Review and rerun the test."
                )
            }

            val baseline = baselineFile.readBytes()
            val result = comparePixels(pixels.data, baseline)
            if (!result.matches) {
                val failureDir = File("build/test-failures/RendererHeadlessUiGlyphBaselineTest").apply { mkdirs() }
                val actualPngFile = File(failureDir, "actual.png")
                val baselinePngFile = File(failureDir, "baseline.png")
                writeUiBaselinePng(pixels.data, TARGET_SIZE, TARGET_SIZE, actualPngFile)
                writeUiBaselinePng(baseline, TARGET_SIZE, TARGET_SIZE, baselinePngFile)
                assertTrue(
                    false,
                    "Headless UI glyph render diverged from baseline: ${result.diffPixelCount} pixels differ " +
                        "(max channel diff ${result.maxChannelDiff}). Compare ${actualPngFile.absolutePath} " +
                        "against ${baselinePngFile.absolutePath}. Re-record ${baselineFile.absolutePath} if this " +
                        "is an intentional renderer change."
                )
            }
        }
    }

    @Test
    fun headlessUiGlyphRenderSupportsRunsLargerThanOneDynamicMesh() {
        withHeadlessUiRenderer { renderer ->
            val font = BitmapFont()
            val target = renderer.createRenderTarget(512, 512)
            val glyphs = buildList {
                repeat(12) { row ->
                    addAll(
                        glyphRun(
                            label = "AWAKE_SHOWCASE_ROW_${row.toString().padStart(2, '0')}_0123456789",
                            font = font,
                            x = 12f,
                            y = 12f + row * 38f,
                            scale = 1.5f,
                            color = Color(0.92f, 0.95f, 1f, 1f)
                        )
                    )
                }
            }

            assertTrue(glyphs.size > Renderer.MAX_UI_QUADS, "test data must exceed one UI glyph mesh capacity")
            renderer.renderUiGlyphsToTexture(target, glyphs, font)
            val pixels = runBlocking { renderer.readPixels(target) }
            assertTrue(
                pixels.data.any { it.toInt() != 0 },
                "large headless glyph runs should render non-empty output instead of tripping the mesh capacity guard"
            )
        }
    }

    @Test
    fun headlessUiGlyphRenderSupportsLargeMipChainFont() {
        withHeadlessUiRenderer { renderer ->
            val font = UiFonts.trueSans()
            val target = renderer.createRenderTarget(128, 128)
            val glyphs = glyphRun("AWAKE", font, x = 8f, y = 8f, scale = 2f, color = Color(0.96f, 0.97f, 1f, 1f))

            renderer.renderUiGlyphsToTexture(target, glyphs, font)
            val pixels = runBlocking { renderer.readPixels(target) }
            assertTrue(
                pixels.data.any { it.toInt() != 0 },
                "TrueSans (large mip chain, ~11 levels) should render non-empty output, not sample an UNDEFINED-layout mip"
            )
        }
    }

    private fun withHeadlessUiRenderer(block: (Renderer) -> Unit) {
        block(sharedFixture().renderer)
    }

    private class HeadlessUiRendererFixture(
        val graphicsDevice: GraphicsDevice,
        val swapchainManager: SwapchainManager,
        val pipelineLayoutMaterial: Material,
        val renderPipeline: RenderPipeline,
        val lineRenderPipeline: LineRenderPipeline,
        val transferContext: TransferContext,
        val renderer: Renderer
    ) {
        fun destroy() {
            renderer.destroy()
            lineRenderPipeline.destroy()
            renderPipeline.destroy()
            VulkanDescriptors.vkDestroyDescriptorSetLayout(graphicsDevice.device, pipelineLayoutMaterial.descriptorSetLayout.handle)
            transferContext.destroy()
            graphicsDevice.destroy()
        }
    }

    private companion object {
        const val TARGET_SIZE = 128
        const val MAX_FRAMES_IN_FLIGHT = 1
        const val SAMPLE_VERTEX_STRIDE = 8 * Float.SIZE_BYTES
        const val BASELINE_RESOURCE_PATH = "baselines/ui-glyph-headless.rgba"

        private var cachedFixture: HeadlessUiRendererFixture? = null

        /**
         * `RendererHeadlessPixelBaselineTest` already proves the full headless create/destroy
         * path end to end. This suite is narrower: it proves UI glyph rendering on that same
         * offscreen path. Reusing one fixture keeps the glyph proof stable on MoltenVK, where
         * tearing down and recreating a validation-enabled headless instance twice in one JVM
         * test class has been flaky on macOS.
         */
        fun sharedFixture(): HeadlessUiRendererFixture {
            cachedFixture?.let { return it }

            val graphicsDevice = GraphicsDevice()
            graphicsDevice.createHeadless()
            val swapchainManager = SwapchainManager(graphicsDevice, MAX_FRAMES_IN_FLIGHT)
            swapchainManager.createHeadless(TARGET_SIZE, TARGET_SIZE)
            val pipelineLayoutMaterial = Material(graphicsDevice)
            val renderPipeline = RenderPipeline(
                graphicsDevice,
                swapchainManager,
                pipelineLayoutMaterial.descriptorSetLayout,
                runBlocking { readResourceBytes("assets/shader/vulkan/triangle.vert.spv") },
                runBlocking { readResourceBytes("assets/shader/vulkan/triangle.frag.spv") },
                SAMPLE_VERTEX_STRIDE
            )
            val lineRenderPipeline = LineRenderPipeline(
                graphicsDevice,
                swapchainManager,
                renderPipeline.renderPass,
                runBlocking { readResourceBytes("assets/shader/vulkan/debug_line.vert.spv") },
                runBlocking { readResourceBytes("assets/shader/vulkan/debug_line.frag.spv") }
            )
            val transferContext = TransferContext(graphicsDevice)
            val renderer = Renderer(
                graphicsDevice,
                swapchainManager,
                renderPipeline,
                lineRenderPipeline,
                transferContext,
                runBlocking { readResourceBytes("assets/shader/vulkan/ui_quad.vert.spv") },
                runBlocking { readResourceBytes("assets/shader/vulkan/ui_quad.frag.spv") },
                runBlocking { readResourceBytes("assets/shader/vulkan/ui_glyph.vert.spv") },
                runBlocking { readResourceBytes("assets/shader/vulkan/ui_glyph.frag.spv") },
                runBlocking { readResourceBytes("assets/shader/vulkan/ui_texture.vert.spv") },
                runBlocking { readResourceBytes("assets/shader/vulkan/ui_texture.frag.spv") },
                runBlocking { readResourceBytes("assets/shader/vulkan/ui_rounded_quad.vert.spv") },
                runBlocking { readResourceBytes("assets/shader/vulkan/ui_rounded_quad.frag.spv") },
                MAX_FRAMES_IN_FLIGHT
            )

            return HeadlessUiRendererFixture(
                graphicsDevice = graphicsDevice,
                swapchainManager = swapchainManager,
                pipelineLayoutMaterial = pipelineLayoutMaterial,
                renderPipeline = renderPipeline,
                lineRenderPipeline = lineRenderPipeline,
                transferContext = transferContext,
                renderer = renderer
            ).also { cachedFixture = it }
        }

        @AfterClass
        @JvmStatic
        fun destroySharedFixture() {
            cachedFixture?.destroy()
            cachedFixture = null
        }

        fun baselineOutputFile(): File {
            val cwd = File(System.getProperty("user.dir"))
            val repoRelative = File(cwd, "awake/backend/vulkan/src/desktopTest/resources/$BASELINE_RESOURCE_PATH")
            return if (repoRelative.parentFile.exists()) {
                repoRelative
            } else {
                File(cwd, "src/desktopTest/resources/$BASELINE_RESOURCE_PATH")
            }
        }

        fun glyphRun(
            label: String,
            font: io.github.ronjunevaldoz.awake.ui.font.UiFont,
            x: Float,
            y: Float,
            scale: Float,
            color: Color
        ): List<UiDrawPrimitive.Glyph> {
            val glyphSize = font.cellSize * scale
            var cursorX = x
            return buildList {
                label.forEach { char ->
                    val uv = font.uvFor(char) ?: return@forEach
                    add(
                        UiDrawPrimitive.Glyph(
                            x = cursorX,
                            y = y,
                            w = glyphSize,
                            h = glyphSize,
                            u0 = uv.u0,
                            v0 = uv.v0,
                            u1 = uv.u1,
                            v1 = uv.v1,
                            color = color
                        )
                    )
                    cursorX += glyphSize
                }
            }
        }
    }
}
