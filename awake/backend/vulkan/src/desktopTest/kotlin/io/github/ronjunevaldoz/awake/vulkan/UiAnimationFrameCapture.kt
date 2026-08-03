// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan

import io.github.ronjunevaldoz.awake.core.utils.readResourceBytes
import io.github.ronjunevaldoz.awake.render.texture.RenderTarget
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.vulkan.commands.TransferContext
import io.github.ronjunevaldoz.awake.vulkan.debug.LineRenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanDescriptors
import io.github.ronjunevaldoz.awake.vulkan.material.Material
import io.github.ronjunevaldoz.awake.vulkan.pipeline.RenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.pipeline.ShaderPair
import io.github.ronjunevaldoz.awake.vulkan.renderer.Renderer
import io.github.ronjunevaldoz.awake.vulkan.renderer.renderUiToTexture
import io.github.ronjunevaldoz.awake.vulkan.swapchain.SwapchainManager
import kotlinx.coroutines.runBlocking
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * Reusable "record a real animation as it actually renders" primitive for `desktopTest`
 * investigations -- see docs/reference/ui-validation.md's Canonical Test Surfaces entry for
 * when to reach for this instead of the logical-`UiBounds`-sampling throwaway-probe idiom.
 *
 * Every earlier animation investigation this session (row-centering, sidebar collapse,
 * typography) drove animation purely through logical measurement (`measureColumnContent`,
 * `UiBounds` samples) -- never through the real Vulkan draw path, so none of them could see a
 * render-backend artifact (frame-pacing/dirty-rect lag between computed clip bounds and what
 * actually gets presented). This closes that gap: constructs the same real headless Vulkan
 * renderer [RendererHeadlessUiGlyphBaselineTest] uses, and for each of N real frames, renders
 * whatever [UiDrawPrimitive]s the caller's own real animated scene produced that frame (via
 * [Renderer.renderUiToTexture]) and dumps it as a numbered PNG.
 *
 * Deliberately minimal -- not a video/gif exporter, just a loop that calls the real render path
 * N times and writes N PNGs to [outputDir] for manual/visual frame-by-frame inspection. Owns its
 * headless renderer construction/teardown; callers drive their own [io.github.ronjunevaldoz
 * .awake.ui.context.UiContext] frames and pass this class the resulting primitive list each
 * time (see `ShadcnCollapsibleRealRenderCollapseFrameCaptureTest` for the pattern).
 */
class UiAnimationFrameCapture private constructor(
    val renderer: Renderer,
    private val target: RenderTarget,
    private val width: Int,
    private val height: Int,
    private val outputDir: File,
    private val fixture: HeadlessUiRendererFixture
) : AutoCloseable {

    /** Renders [primitives] (one real frame's worth, as returned by `UiContext.endFrame()`)
     * through the real Vulkan UI pipelines and writes it to `outputDir/<namePrefix><frameIndex
     * padded>.png`. Returns the written [File] so callers can immediately read it back (e.g.
     * for a pixel-level jump check) without re-deriving the naming scheme. */
    fun captureFrame(frameIndex: Int, primitives: List<UiDrawPrimitive>, font: UiFont?, namePrefix: String = "frame-"): File {
        renderer.renderUiToTexture(target, primitives, font)
        val pixels = runBlocking { renderer.readPixels(target) }
        val file = File(outputDir, "$namePrefix${frameIndex.toString().padStart(3, '0')}.png")
        writeRgbaPng(pixels.data, width, height, file)
        return file
    }

    override fun close() = fixture.destroy()

    companion object {
        /** Builds a real headless Vulkan renderer + offscreen render target, same construction
         * this module's other headless tests use (`GraphicsDevice.createHeadless`/
         * `SwapchainManager.createHeadless`, no GLFW window, no real swapchain). [outputDir] is
         * created if missing. */
        fun create(width: Int, height: Int, outputDir: File): UiAnimationFrameCapture {
            outputDir.mkdirs()
            val fixture = buildHeadlessUiRendererFixture(width, height)
            val target = fixture.renderer.createRenderTarget(width, height)
            return UiAnimationFrameCapture(fixture.renderer, target, width, height, outputDir, fixture)
        }
    }
}

/** Same fixture shape as [RendererHeadlessUiGlyphBaselineTest]'s private `HeadlessUiRendererFixture`,
 * pulled out standalone so [UiAnimationFrameCapture] can own its own renderer lifecycle without
 * depending on that test class's private nested type. */
class HeadlessUiRendererFixture(
    private val graphicsDevice: GraphicsDevice,
    private val swapchainManager: SwapchainManager,
    private val pipelineLayoutMaterial: Material,
    private val renderPipeline: RenderPipeline,
    private val lineRenderPipeline: LineRenderPipeline,
    private val transferContext: TransferContext,
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

private const val UI_ANIMATION_MAX_FRAMES_IN_FLIGHT = 1
private const val UI_ANIMATION_SAMPLE_VERTEX_STRIDE = 8 * Float.SIZE_BYTES

private suspend fun loadUiAnimationShaderPair(vertexPath: String, fragmentPath: String): ShaderPair =
    ShaderPair(readResourceBytes(vertexPath), readResourceBytes(fragmentPath))

fun buildHeadlessUiRendererFixture(width: Int, height: Int): HeadlessUiRendererFixture {
    val graphicsDevice = GraphicsDevice()
    graphicsDevice.createHeadless()
    val swapchainManager = SwapchainManager(graphicsDevice, UI_ANIMATION_MAX_FRAMES_IN_FLIGHT)
    swapchainManager.createHeadless(width, height)
    val pipelineLayoutMaterial = Material(graphicsDevice)
    val renderPipeline = RenderPipeline(
        graphicsDevice,
        swapchainManager,
        pipelineLayoutMaterial.descriptorSetLayout,
        runBlocking {
            loadUiAnimationShaderPair("assets/shader/vulkan/triangle.vert.spv", "assets/shader/vulkan/triangle.frag.spv")
        },
        UI_ANIMATION_SAMPLE_VERTEX_STRIDE
    )
    val lineRenderPipeline = LineRenderPipeline(
        graphicsDevice,
        swapchainManager,
        renderPipeline.renderPass,
        runBlocking {
            loadUiAnimationShaderPair("assets/shader/vulkan/debug_line.vert.spv", "assets/shader/vulkan/debug_line.frag.spv")
        }
    )
    val transferContext = TransferContext(graphicsDevice)
    val renderer = Renderer(
        graphicsDevice,
        swapchainManager,
        renderPipeline,
        lineRenderPipeline,
        transferContext,
        runBlocking { loadUiAnimationShaderPair("assets/shader/vulkan/ui_quad.vert.spv", "assets/shader/vulkan/ui_quad.frag.spv") },
        runBlocking { loadUiAnimationShaderPair("assets/shader/vulkan/ui_glyph.vert.spv", "assets/shader/vulkan/ui_glyph.frag.spv") },
        runBlocking {
            loadUiAnimationShaderPair("assets/shader/vulkan/ui_texture.vert.spv", "assets/shader/vulkan/ui_texture.frag.spv")
        },
        runBlocking {
            loadUiAnimationShaderPair(
                "assets/shader/vulkan/ui_rounded_quad.vert.spv",
                "assets/shader/vulkan/ui_rounded_quad.frag.spv"
            )
        },
        UI_ANIMATION_MAX_FRAMES_IN_FLIGHT
    )
    return HeadlessUiRendererFixture(
        graphicsDevice,
        swapchainManager,
        pipelineLayoutMaterial,
        renderPipeline,
        lineRenderPipeline,
        transferContext,
        renderer
    )
}

/** Same RGBA-bytes-to-PNG encode every other headless pixel test in this module uses (see
 * e.g. `RendererHeadlessPixelBaselineTest.writeRgbaPng`) -- duplicated rather than shared since
 * each is a file-private `private fun` in Kotlin. */
private fun writeRgbaPng(pixels: ByteArray, width: Int, height: Int, file: File) {
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
