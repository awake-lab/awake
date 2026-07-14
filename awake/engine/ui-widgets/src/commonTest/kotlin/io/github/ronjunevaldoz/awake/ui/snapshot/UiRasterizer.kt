// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.snapshot

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import kotlin.math.max
import kotlin.math.min

/**
 * Software-rasterizes a [UiContext][io.github.ronjunevaldoz.awake.ui.UiContext] frame's
 * primitive list into a tightly-packed RGBA8 buffer -- purely for *visual review* of a UI
 * test's own output (layout, theme colors, contrast between a widget's background and its
 * label), not a GPU-accuracy check. These UI tests never touch a real `Renderer` (they only
 * inspect the primitive list a widget function returns), so there is nothing to screenshot
 * unless something rasterizes that list itself; this is deliberately simple (flat rects, no
 * real glyph shapes -- there's no font atlas available in a plain unit test) rather than
 * trying to match either GPU backend's actual output pixel-for-pixel.
 *
 * [RoundedQuad] draws as a flat rect (same "backend may fall back to flat" allowance
 * documented on [UiDrawPrimitive.RoundedQuad] itself) and [Glyph][UiDrawPrimitive.Glyph]
 * draws as a small inset block in its own color rather than real glyph shapes -- what matters
 * for a contrast/occlusion review is whether that block is even visible against whatever
 * ends up underneath it, not the exact letterforms. [Texture] draws as a flat gray
 * placeholder (no way to sample its opaque `material` outside a real renderer).
 * [ClipPush]/[ClipPop] narrow the active paint region for primitives between them, matching
 * every backend's own "just set scissor to this rect" contract.
 */
fun List<UiDrawPrimitive>.rasterize(
    width: Int,
    height: Int,
    background: FloatArray = floatArrayOf(0.1f, 0.1f, 0.12f, 1f)
): ByteArray {
    val pixels = ByteArray(width * height * 4)
    var i = 0
    while (i < pixels.size) {
        pixels[i] = (background[0] * 255).toInt().toByte()
        pixels[i + 1] = (background[1] * 255).toInt().toByte()
        pixels[i + 2] = (background[2] * 255).toInt().toByte()
        pixels[i + 3] = (if (background.size > 3) background[3] else 1f).let { (it * 255).toInt().toByte() }
        i += 4
    }

    var clipX0 = 0f
    var clipY0 = 0f
    var clipX1 = width.toFloat()
    var clipY1 = height.toFloat()
    val clipStack = ArrayDeque<FloatArray>()

    fun fillRect(x: Float, y: Float, w: Float, h: Float, color: FloatArray) {
        val x0 = max(x, clipX0).toInt().coerceIn(0, width)
        val y0 = max(y, clipY0).toInt().coerceIn(0, height)
        val x1 = min(x + w, clipX1).toInt().coerceIn(0, width)
        val y1 = min(y + h, clipY1).toInt().coerceIn(0, height)
        val r = (color[0] * 255).toInt().coerceIn(0, 255)
        val g = (color[1] * 255).toInt().coerceIn(0, 255)
        val b = (color[2] * 255).toInt().coerceIn(0, 255)
        val a = ((if (color.size > 3) color[3] else 1f) * 255).toInt().coerceIn(0, 255)
        var py = y0
        while (py < y1) {
            var px = x0
            while (px < x1) {
                val offset = (py * width + px) * 4
                pixels[offset] = r.toByte()
                pixels[offset + 1] = g.toByte()
                pixels[offset + 2] = b.toByte()
                pixels[offset + 3] = a.toByte()
                px += 1
            }
            py += 1
        }
    }

    for (primitive in this) {
        when (primitive) {
            is UiDrawPrimitive.Quad -> fillRect(primitive.x, primitive.y, primitive.w, primitive.h, primitive.color)
            is UiDrawPrimitive.RoundedQuad -> fillRect(primitive.x, primitive.y, primitive.w, primitive.h, primitive.color)
            is UiDrawPrimitive.Glyph -> {
                // Inset block, not the full glyph quad -- a full-size flat-colored rect per
                // glyph would look identical to a Quad and defeat the point of reviewing
                // "is the text visible against its background."
                val inset = min(primitive.w, primitive.h) * 0.25f
                fillRect(primitive.x + inset, primitive.y + inset, primitive.w - inset * 2, primitive.h - inset * 2, primitive.color)
            }
            is UiDrawPrimitive.Texture -> fillRect(primitive.x, primitive.y, primitive.w, primitive.h, floatArrayOf(0.5f, 0.5f, 0.5f, 1f))
            is UiDrawPrimitive.ClipPush -> {
                clipStack.addLast(floatArrayOf(clipX0, clipY0, clipX1, clipY1))
                clipX0 = max(clipX0, primitive.rect.x)
                clipY0 = max(clipY0, primitive.rect.y)
                clipX1 = min(clipX1, primitive.rect.x + primitive.rect.width)
                clipY1 = min(clipY1, primitive.rect.y + primitive.rect.height)
            }
            is UiDrawPrimitive.ClipPop -> {
                clipStack.removeLastOrNull()?.let { restored ->
                    clipX0 = restored[0]
                    clipY0 = restored[1]
                    clipX1 = restored[2]
                    clipY1 = restored[3]
                }
            }
        }
    }
    return pixels
}
