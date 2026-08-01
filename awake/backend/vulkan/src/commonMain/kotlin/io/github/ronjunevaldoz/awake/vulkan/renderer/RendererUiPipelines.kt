// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.renderer

import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.vulkan.Vulkan
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkFilter
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkFramebufferCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkSamplerAddressMode
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkSamplerCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkSamplerMipmapMode
import io.github.ronjunevaldoz.awake.vulkan.texture.Texture
import io.github.ronjunevaldoz.awake.vulkan.ui.DynamicMesh
import io.github.ronjunevaldoz.awake.vulkan.ui.UiGlyphRenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.ui.UiRenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.ui.UiRoundedQuadRenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.ui.UiTextureRenderPipeline

/** Lazy construction of every UI-overlay graphics pipeline [Renderer.drawUi]/
 * [Renderer.renderUiGlyphsToTexture] need -- each is built (and its render-pass-derived
 * resources allocated) only on the first call that actually needs it, so a game that never
 * draws UI, glyphs, textured quads, or rounded quads never pays for the pipeline it doesn't
 * use. See [Renderer]'s class doc comment for why this lives in a sibling file as `internal`
 * extension functions rather than as members. */

/** Builds [Renderer.uiRenderPipeline]/[Renderer.uiFramebuffers] on the first [Renderer.drawUi]
 * call of any kind -- quad rendering doesn't need a font, so this doesn't wait for one.
 * Cached after the first build (a game calls `drawUi` every frame). */
internal fun Renderer.ensureUiQuadPipeline() {
    if (uiRenderPipeline != null) return
    val pipeline = UiRenderPipeline(graphicsDevice, swapchainManager, uiShaders.vertex, uiShaders.fragment)
    uiRenderPipeline = pipeline
    uiFramebuffers = buildUiFramebuffers(pipeline.renderPass)
}

/** One framebuffer per swapchain image view, against [renderPass] -- shared by
 * [ensureUiQuadPipeline] (first build) and `recreateSwapChain` (rebuild after the swapchain's
 * image views change). */
internal fun Renderer.buildUiFramebuffers(renderPass: Long): List<Long> = swapchainManager.imageViews.map { imageView ->
    Vulkan.vkCreateFramebuffer(
        device,
        VkFramebufferCreateInfo(
            renderPass = renderPass,
            pAttachments = arrayOf(imageView),
            width = swapchainManager.extent.width,
            height = swapchainManager.extent.height,
            layers = 1
        )
    )
}.toList()

/** Builds [Renderer.uiGlyphRenderPipeline]/[Renderer.fontTexture] on the first
 * [Renderer.drawUi] call that passes a non-null [font] -- cached after that (a game calls
 * `drawUi` with the same font every frame). Requires [ensureUiQuadPipeline] to already have
 * run (needs its render pass). */
internal fun Renderer.ensureGlyphPipeline(font: UiFont) {
    if (currentUiFont !== font) {
        uiGlyphRenderPipeline?.destroy()
        uiGlyphRenderPipeline = null
        offscreenGlyphRenderPipeline?.destroy()
        offscreenGlyphRenderPipeline = null
        fontTexture?.destroy()
        fontTexture = null
        currentUiFont = font
    }
    if (uiGlyphRenderPipeline != null) return
    val renderPass = requireNotNull(uiRenderPipeline) { "ensureUiQuadPipeline() must run first." }.renderPass
    uiGlyphRenderPipeline = buildUiGlyphRenderPipeline(renderPass, font)
}

/** Same lazy/font-swap-invalidation shape as [ensureGlyphPipeline], but against
 * [Renderer.offscreenGlyphRenderPipeline]/[Renderer.renderPipeline]'s render pass instead of
 * the swapchain UI pass -- used by [Renderer.renderUiGlyphsToTexture]'s headless/offscreen
 * glyph baseline path, which has no swapchain UI pipeline to reuse. Kept as its own field
 * (not merged with [Renderer.uiGlyphRenderPipeline]) because the two pipelines are bound to
 * different render passes and can be live at the same time. */
internal fun Renderer.ensureOffscreenGlyphPipeline(font: UiFont) {
    if (currentUiFont !== font) {
        offscreenGlyphRenderPipeline?.destroy()
        offscreenGlyphRenderPipeline = null
    }
    if (offscreenGlyphRenderPipeline != null) return
    offscreenGlyphRenderPipeline = buildUiGlyphRenderPipeline(renderPipeline.renderPass, font)
}

/** Shared [UiGlyphRenderPipeline] construction for [ensureGlyphPipeline]/
 * [ensureOffscreenGlyphPipeline] -- the two wrappers differ only in which render pass they
 * target and which field they cache the result in; the pipeline-construction logic itself
 * (same shader code, same atlas texture lookup) was previously duplicated between them. */
private fun Renderer.buildUiGlyphRenderPipeline(renderPass: Long, font: UiFont): UiGlyphRenderPipeline =
    UiGlyphRenderPipeline(
        graphicsDevice,
        swapchainManager,
        renderPass,
        uiGlyphShaders.vertex,
        uiGlyphShaders.fragment,
        ensureFontTexture(font),
        font
    )

internal fun Renderer.ensureFontTexture(font: UiFont): Texture {
    fontTexture?.let { return it }
    // Both atlas types use linear sampling -- matches the WebGPU backend's
    // UiGlyphRenderPipeline, which always samples Linear regardless of samplingMode.
    // Nearest-neighbor on a CoverageAlpha atlas made minified text look thin: at typical
    // UI sizes the on-screen glyph is much smaller than the baked atlas cell, and nearest
    // sampling picks single texels that miss thin strokes entirely instead of blending
    // them in. generate_ui_font_atlas.py crops each glyph with a few px of raster
    // headroom so linear filtering blends into blank atlas margin, not a neighboring
    // glyph's cell.
    val glyphFilter = VkFilter.VK_FILTER_LINEAR
    return Texture(
        graphicsDevice,
        transferContext::runOneTimeCommands,
        font.atlasPixelsRgba,
        font.atlasWidth,
        font.atlasHeight,
        samplerCreateInfo = VkSamplerCreateInfo(
            magFilter = glyphFilter,
            minFilter = glyphFilter,
            addressModeU = VkSamplerAddressMode.VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE,
            addressModeV = VkSamplerAddressMode.VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE,
            addressModeW = VkSamplerAddressMode.VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE,
            mipmapMode = VkSamplerMipmapMode.VK_SAMPLER_MIPMAP_MODE_NEAREST
        )
    ).also { fontTexture = it }
}

/** Builds [Renderer.uiTextureRenderPipeline]/[Renderer.textureQuadMesh] on the first
 * [Renderer.drawUi] call that has any [io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive.Texture]
 * primitives -- cached after that. Requires [ensureUiQuadPipeline] to already have run (needs
 * its render pass), same precondition [ensureGlyphPipeline] has. */
internal fun Renderer.ensureTextureQuadPipeline() {
    if (uiTextureRenderPipeline != null) return
    val renderPass = requireNotNull(uiRenderPipeline) { "ensureUiQuadPipeline() must run first." }.renderPass
    uiTextureRenderPipeline = UiTextureRenderPipeline(
        graphicsDevice,
        swapchainManager,
        renderPass,
        uiTextureShaders.vertex,
        uiTextureShaders.fragment
    )
    textureQuadMesh = DynamicMesh(graphicsDevice, Renderer.MAX_UI_QUADS, DynamicMesh.GLYPH_FLOATS_PER_VERTEX)
}

/** Builds [Renderer.uiRoundedQuadRenderPipeline] on the first [Renderer.drawUi] call that has
 * any [io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive.RoundedQuad] primitives -- cached
 * after that. Requires [ensureUiQuadPipeline] to already have run (needs its render pass),
 * same precondition [ensureGlyphPipeline]/[ensureTextureQuadPipeline] have. */
internal fun Renderer.ensureRoundedQuadPipeline() {
    if (uiRoundedQuadRenderPipeline != null) return
    val renderPass = requireNotNull(uiRenderPipeline) { "ensureUiQuadPipeline() must run first." }.renderPass
    uiRoundedQuadRenderPipeline = UiRoundedQuadRenderPipeline(
        graphicsDevice,
        swapchainManager,
        renderPass,
        uiRoundedQuadShaders.vertex,
        uiRoundedQuadShaders.fragment
    )
}
