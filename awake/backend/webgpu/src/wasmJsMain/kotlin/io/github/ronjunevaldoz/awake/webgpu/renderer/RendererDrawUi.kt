// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.renderer

import io.github.ronjunevaldoz.awake.render.passes.ui.UiBatchCoalescer
import io.github.ronjunevaldoz.awake.render.passes.ui.UiStagedRun
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.webgpu.renderer.Renderer.UiRun.ClipRun
import io.github.ronjunevaldoz.awake.webgpu.renderer.Renderer.UiRun.GlyphRun
import io.github.ronjunevaldoz.awake.webgpu.renderer.Renderer.UiRun.QuadRun
import io.github.ronjunevaldoz.awake.webgpu.renderer.Renderer.UiRun.RoundedQuadRun
import io.github.ronjunevaldoz.awake.webgpu.renderer.Renderer.UiRun.TextureRun

/**
 * UI primitive staging for the WebGPU backend.
 */
internal fun Renderer.performDrawUi(primitives: List<UiDrawPrimitive>, font: UiFont?) {
    ensureUiQuadPipeline()
    if (font != null) ensureGlyphPipeline(font)
    if (primitives.any { it is UiDrawPrimitive.Texture }) ensureTextureQuadPipeline()
    if (primitives.any { it is UiDrawPrimitive.RoundedQuad || it is UiDrawPrimitive.ShadowQuad }) ensureRoundedQuadPipeline()

    val stagedRuns = UiBatchCoalescer.coalesce(primitives, Renderer.MAX_UI_QUADS)
    val runs = mutableListOf<Renderer.UiRun>()
    var quadRunCount = 0
    var roundedQuadRunCount = 0
    var glyphRunCount = 0

    for (staged in stagedRuns) {
        when (staged) {
            is UiStagedRun.QuadRun -> {
                val mesh = quadMeshForRun(quadRunCount++)
                mesh.update(staged.vertices, staged.indices)
                runs += QuadRun(mesh)
            }
            is UiStagedRun.RoundedQuadRun -> {
                val mesh = roundedQuadMeshForRun(roundedQuadRunCount++)
                mesh.update(staged.vertices, staged.indices)
                runs += RoundedQuadRun(mesh)
            }
            is UiStagedRun.GlyphRun -> {
                val mesh = glyphMeshForRun(glyphRunCount++)
                mesh.update(staged.vertices, staged.indices)
                runs += GlyphRun(mesh)
            }
            is UiStagedRun.TextureRun -> {
                runs += TextureRun(staged.primitives.map {
                    Renderer.TexturedPrimitiveRun(it.texture, it.vertices, it.indices)
                })
            }
            is UiStagedRun.ClipRun -> {
                runs += ClipRun(staged.rect)
            }
        }
    }
    uiRuns = runs
}
