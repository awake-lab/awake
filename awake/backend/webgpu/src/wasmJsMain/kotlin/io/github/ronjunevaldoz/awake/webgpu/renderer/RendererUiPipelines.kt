// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.renderer

import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.webgpu.ui.UiGlyphRenderPipeline
import io.github.ronjunevaldoz.awake.webgpu.ui.UiRenderPipeline
import io.github.ronjunevaldoz.awake.webgpu.ui.UiRoundedQuadRenderPipeline
import io.github.ronjunevaldoz.awake.webgpu.ui.UiTextureRenderPipeline
import io.ygdrasil.webgpu.BindGroupDescriptor
import io.ygdrasil.webgpu.BindGroupEntry
import io.ygdrasil.webgpu.BufferBinding
import io.ygdrasil.webgpu.BufferDescriptor
import io.ygdrasil.webgpu.GPUBufferUsage
import io.ygdrasil.webgpu.GPURenderPipeline

/** Lazy construction of every UI-overlay graphics pipeline `performDrawUi` ([RendererDrawUi.kt])
 * needs -- each is built only on the first call that actually needs it, so a game that never
 * draws UI, glyphs, textured quads, or rounded quads never pays for the pipeline it doesn't
 * use. See [Renderer]'s class doc comment for why this lives in a sibling file as `internal`
 * extension functions rather than as members. */

/** Builds [Renderer.uiRenderPipeline] on the first `drawUi` call of any kind -- quad
 * rendering doesn't need a font, so this doesn't wait for one. Cached after the first build
 * (a game calls `drawUi` every frame). */
internal fun Renderer.ensureUiQuadPipeline() {
    if (uiRenderPipeline != null) return
    uiRenderPipeline = UiRenderPipeline(graphicsDevice, swapchainManager, uiShaderCode)
}

/** Builds [Renderer.uiGlyphRenderPipeline] on the first `drawUi` call that passes a non-null
 * [font] -- cached after that (a game calls `drawUi` with the same font every frame). */
internal fun Renderer.ensureGlyphPipeline(font: UiFont) {
    if (currentUiFont !== font) {
        uiGlyphRenderPipeline?.destroy()
        uiGlyphRenderPipeline = null
        currentUiFont = font
    }
    if (uiGlyphRenderPipeline != null) return
    uiGlyphRenderPipeline = UiGlyphRenderPipeline(graphicsDevice, swapchainManager, uiGlyphShaderCode, font)
}

/** Builds [Renderer.uiTextureRenderPipeline] on the first `drawUi` call that has any
 * [io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive.Texture] primitives -- cached after that. */
internal fun Renderer.ensureTextureQuadPipeline() {
    if (uiTextureRenderPipeline != null) return
    uiTextureRenderPipeline = UiTextureRenderPipeline(graphicsDevice, swapchainManager, uiTextureShaderCode)
}

/** Builds [Renderer.uiRoundedQuadRenderPipeline] on the first `drawUi` call that has any
 * [io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive.RoundedQuad] primitives outside an active
 * convex-path clip -- cached after that. Mirrors Vulkan's `ensureRoundedQuadPipeline()`. */
internal fun Renderer.ensureRoundedQuadPipeline() {
    if (uiRoundedQuadRenderPipeline != null) return
    uiRoundedQuadRenderPipeline = UiRoundedQuadRenderPipeline(graphicsDevice, swapchainManager, uiRoundedQuadShaderCode)
}

/** Builds [Renderer.uniformBuffer]/[Renderer.uniformBindGroup] against [pipeline] on first
 * use -- shared by [Renderer.renderToTexture] and `performDraw` ([RendererDraw3D.kt]), both
 * 3D draw paths that write the same single MVP uniform before each draw call (see
 * [Renderer]'s class doc comment for why one shared uniform buffer only actually works for a
 * single draw call per frame today). */
internal fun Renderer.ensureUniformResources(pipeline: GPURenderPipeline) {
    if (uniformBuffer != null) return
    val device = graphicsDevice.wgpuContext.device
    val buffer = device.createBuffer(
        BufferDescriptor(
            size = (16 * Float.SIZE_BYTES).toULong(),
            usage = GPUBufferUsage.Uniform or GPUBufferUsage.CopyDst
        )
    )
    uniformBuffer = buffer
    uniformBindGroup = device.createBindGroup(
        BindGroupDescriptor(
            layout = pipeline.getBindGroupLayout(0u),
            entries = listOf(
                BindGroupEntry(binding = 0u, resource = BufferBinding(buffer = buffer))
            )
        )
    )
}
