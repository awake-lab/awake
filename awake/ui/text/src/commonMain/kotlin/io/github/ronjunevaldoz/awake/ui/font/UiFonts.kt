// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.font

object UiFonts {
    // Memoized: Renderer.ensureGlyphPipeline() caches its glyph pipeline by UiFont reference
    // identity, on the assumption that independent callers asking for "the default font" get
    // the same instance. Two callers each independently calling default() and getting a fresh
    // PackedUiFont was silently defeating that cache -- every drawUi() call from a different
    // caller evicted and rebuilt the other's pipeline (a full font-atlas GPU upload), every
    // frame, forever. See awake-render-backend-engineer's UiGlyphRenderPipeline fix.
    private val defaultCache = mutableMapOf<Int, UiFont>()
    /** Stable bundled atlas used by all targets unless a caller explicitly supplies another font. */
    fun default(cellSize: Int = 12): UiFont = defaultCache.getOrPut(cellSize) {
        trueSans(cellSize = cellSize)
    }

    fun bitmap(cellSize: Int = 12): UiFont = BitmapFont(cellSize = cellSize)

    fun trueSans(cellSize: Int = 12): UiFont =
        PackedUiFont(RobotoRegularUiFontData, cellSize = cellSize)

    fun msdf(cellSize: Int = 12): UiFont = MsdfFont(cellSize = cellSize)
}

typealias Fonts = UiFonts
