// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.testing.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.containsPoint
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.tessellateFill
import io.github.ronjunevaldoz.awake.ui.tessellateStroke
import io.github.ronjunevaldoz.awake.ui.toPath
import kotlin.math.max
import kotlin.math.min

/**
 * Software-rasterizes a [UiDrawPrimitive] list into a tightly-packed RGBA8 buffer for
 * preview/docs/snapshot review. This is deliberately backend-agnostic and simpler than the
 * real GPU output: it exists to make UI regressions visible and diffable.
 */
fun List<UiDrawPrimitive>.rasterize(
    width: Int,
    height: Int,
    background: Color = Color(0.1f, 0.1f, 0.12f, 1f),
    font: UiFont? = null
): ByteArray {
    val pixels = ByteArray(width * height * 4)
    var i = 0
    while (i < pixels.size) {
        pixels[i] = (background[0] * 255).toInt().toByte()
        pixels[i + 1] = (background[1] * 255).toInt().toByte()
        pixels[i + 2] = (background[2] * 255).toInt().toByte()
        pixels[i + 3] = (background.a * 255).toInt().toByte()
        i += 4
    }

    var clipX0 = 0f
    var clipY0 = 0f
    var clipX1 = width.toFloat()
    var clipY1 = height.toFloat()
    data class ClipSnapshot(val rect: FloatArray, val activePathCount: Int)
    val clipStack = ArrayDeque<ClipSnapshot>()
    val activePathClips = ArrayList<io.github.ronjunevaldoz.awake.ui.UiPath>()

    fun passesPathClips(x: Float, y: Float): Boolean = activePathClips.all { it.containsPoint(x, y) }

    fun fillRect(x: Float, y: Float, w: Float, h: Float, color: Color) {
        val x0 = max(x, clipX0).toInt().coerceIn(0, width)
        val y0 = max(y, clipY0).toInt().coerceIn(0, height)
        val x1 = min(x + w, clipX1).toInt().coerceIn(0, width)
        val y1 = min(y + h, clipY1).toInt().coerceIn(0, height)
        val r = (color[0] * 255).toInt().coerceIn(0, 255)
        val g = (color[1] * 255).toInt().coerceIn(0, 255)
        val b = (color[2] * 255).toInt().coerceIn(0, 255)
        val a = (color.a * 255).toInt().coerceIn(0, 255)
        var py = y0
        while (py < y1) {
            var px = x0
            while (px < x1) {
                if (!passesPathClips(px + 0.5f, py + 0.5f)) {
                    px += 1
                    continue
                }
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

    fun sampleGlyphAlpha(bitmapFont: UiFont, u: Float, v: Float): Int {
        val atlasWidth = bitmapFont.atlasWidth
        val atlasHeight = bitmapFont.atlasHeight
        val atlasPixels = bitmapFont.atlasPixelsRgba
        val x = (u.coerceIn(0f, 1f) * atlasWidth - 0.5f).coerceIn(0f, (atlasWidth - 1).toFloat())
        val y = (v.coerceIn(0f, 1f) * atlasHeight - 0.5f).coerceIn(0f, (atlasHeight - 1).toFloat())
        val x0 = x.toInt().coerceIn(0, atlasWidth - 1)
        val y0 = y.toInt().coerceIn(0, atlasHeight - 1)
        val x1 = (x0 + 1).coerceIn(0, atlasWidth - 1)
        val y1 = (y0 + 1).coerceIn(0, atlasHeight - 1)
        val tx = x - x0
        val ty = y - y0

        fun alphaAt(px: Int, py: Int): Float = (atlasPixels[(py * atlasWidth + px) * 4 + 3].toInt() and 0xFF) / 255f

        val top = alphaAt(x0, y0) * (1f - tx) + alphaAt(x1, y0) * tx
        val bottom = alphaAt(x0, y1) * (1f - tx) + alphaAt(x1, y1) * tx
        return (((top * (1f - ty) + bottom * ty) * 255f).toInt()).coerceIn(0, 255)
    }

    fun fillGradientRect(x: Float, y: Float, w: Float, h: Float, gradient: io.github.ronjunevaldoz.awake.ui.UiLinearGradient) {
        val x0 = max(x, clipX0).toInt().coerceIn(0, width)
        val y0 = max(y, clipY0).toInt().coerceIn(0, height)
        val x1 = min(x + w, clipX1).toInt().coerceIn(0, width)
        val y1 = min(y + h, clipY1).toInt().coerceIn(0, height)
        var py = y0
        while (py < y1) {
            var px = x0
            while (px < x1) {
                val sampleX = ((px + 0.5f - x) / w).coerceIn(0f, 1f)
                val sampleY = ((py + 0.5f - y) / h).coerceIn(0f, 1f)
                if (!passesPathClips(px + 0.5f, py + 0.5f)) {
                    px += 1
                    continue
                }
                val top = lerpColor(gradient.topLeft, gradient.topRight, sampleX)
                val bottom = lerpColor(gradient.bottomLeft, gradient.bottomRight, sampleX)
                val color = lerpColor(top, bottom, sampleY)
                val offset = (py * width + px) * 4
                pixels[offset] = (color.r * 255).toInt().coerceIn(0, 255).toByte()
                pixels[offset + 1] = (color.g * 255).toInt().coerceIn(0, 255).toByte()
                pixels[offset + 2] = (color.b * 255).toInt().coerceIn(0, 255).toByte()
                pixels[offset + 3] = (color.a * 255).toInt().coerceIn(0, 255).toByte()
                px += 1
            }
            py += 1
        }
    }

    fun drawGlyph(glyph: UiDrawPrimitive.Glyph, bitmapFont: UiFont) {
        val x0 = max(glyph.x, clipX0).toInt().coerceIn(0, width)
        val y0 = max(glyph.y, clipY0).toInt().coerceIn(0, height)
        val x1 = min(glyph.x + glyph.w, clipX1).toInt().coerceIn(0, width)
        val y1 = min(glyph.y + glyph.h, clipY1).toInt().coerceIn(0, height)
        val r = (glyph.color[0] * 255).toInt().coerceIn(0, 255)
        val g = (glyph.color[1] * 255).toInt().coerceIn(0, 255)
        val b = (glyph.color[2] * 255).toInt().coerceIn(0, 255)
        val tintAlpha = (glyph.color.a * 255).toInt().coerceIn(0, 255)

        var py = y0
        while (py < y1) {
            var px = x0
            while (px < x1) {
                val sampleX = px + 0.5f
                val sampleY = py + 0.5f
                if (!passesPathClips(sampleX, sampleY)) {
                    px += 1
                    continue
                }
                val u = ((sampleX - glyph.x) / glyph.w).coerceIn(0f, 0.9999f)
                val v = ((sampleY - glyph.y) / glyph.h).coerceIn(0f, 0.9999f)
                val sourceAlpha = sampleGlyphAlpha(
                    bitmapFont,
                    glyph.u0 + (glyph.u1 - glyph.u0) * u,
                    glyph.v0 + (glyph.v1 - glyph.v0) * v
                )
                if (sourceAlpha == 0) {
                    px += 1
                    continue
                }
                val alpha = (sourceAlpha * tintAlpha) / 255
                val offset = (py * width + px) * 4
                pixels[offset] = r.toByte()
                pixels[offset + 1] = g.toByte()
                pixels[offset + 2] = b.toByte()
                pixels[offset + 3] = alpha.toByte()
                px += 1
            }
            py += 1
        }
    }

    fun fillTriangle(ax: Float, ay: Float, bx: Float, by: Float, cx: Float, cy: Float, color: Color) {
        val minX = max(min(ax, min(bx, cx)), clipX0).toInt().coerceIn(0, width)
        val minY = max(min(ay, min(by, cy)), clipY0).toInt().coerceIn(0, height)
        val maxX = min(max(ax, max(bx, cx)), clipX1).toInt().coerceIn(0, width)
        val maxY = min(max(ay, max(by, cy)), clipY1).toInt().coerceIn(0, height)
        val r = (color[0] * 255).toInt().coerceIn(0, 255)
        val g = (color[1] * 255).toInt().coerceIn(0, 255)
        val b = (color[2] * 255).toInt().coerceIn(0, 255)
        val a = (color.a * 255).toInt().coerceIn(0, 255)

        fun edge(x0: Float, y0: Float, x1: Float, y1: Float, px: Float, py: Float): Float =
            (px - x0) * (y1 - y0) - (py - y0) * (x1 - x0)

        var py = minY
        while (py < maxY) {
            var px = minX
            while (px < maxX) {
                val sampleX = px + 0.5f
                val sampleY = py + 0.5f
                val w0 = edge(ax, ay, bx, by, sampleX, sampleY)
                val w1 = edge(bx, by, cx, cy, sampleX, sampleY)
                val w2 = edge(cx, cy, ax, ay, sampleX, sampleY)
                val inside = (w0 >= 0f && w1 >= 0f && w2 >= 0f) || (w0 <= 0f && w1 <= 0f && w2 <= 0f)
                if (inside && passesPathClips(sampleX, sampleY)) {
                    val offset = (py * width + px) * 4
                    pixels[offset] = r.toByte()
                    pixels[offset + 1] = g.toByte()
                    pixels[offset + 2] = b.toByte()
                    pixels[offset + 3] = a.toByte()
                }
                px += 1
            }
            py += 1
        }
    }

    fun fillTriangleMesh(path: io.github.ronjunevaldoz.awake.ui.UiTriangleMesh, color: Color) {
        var index = 0
        while (index + 2 < path.indices.size) {
            val a = path.points[path.indices[index]]
            val b = path.points[path.indices[index + 1]]
            val c = path.points[path.indices[index + 2]]
            fillTriangle(a.x, a.y, b.x, b.y, c.x, c.y, color)
            index += 3
        }
    }

    for (primitive in this) {
        when (primitive) {
            is UiDrawPrimitive.Quad -> fillRect(primitive.x, primitive.y, primitive.w, primitive.h, primitive.color)
            is UiDrawPrimitive.GradientQuad -> fillGradientRect(primitive.x, primitive.y, primitive.w, primitive.h, primitive.gradient)
            is UiDrawPrimitive.RoundedQuad -> fillTriangleMesh(
                UiShapeSpec.RoundedRectangle(primitive.radius.px)
                    .toPath(io.github.ronjunevaldoz.awake.ui.UiSlot(primitive.x, primitive.y, primitive.w, primitive.h))
                    .tessellateFill(),
                primitive.color
            )
            is UiDrawPrimitive.FilledPath -> fillTriangleMesh(primitive.path.tessellateFill(), primitive.color)
            is UiDrawPrimitive.StrokedPath -> fillTriangleMesh(primitive.path.tessellateStroke(primitive.stroke), primitive.color)
            is UiDrawPrimitive.Glyph -> {
                if (font != null) {
                    drawGlyph(primitive, font)
                } else {
                    val inset = min(primitive.w, primitive.h) * 0.25f
                    fillRect(primitive.x + inset, primitive.y + inset, primitive.w - inset * 2, primitive.h - inset * 2, primitive.color)
                }
            }
            is UiDrawPrimitive.Texture -> fillRect(primitive.x, primitive.y, primitive.w, primitive.h, Color(0.5f, 0.5f, 0.5f, 1f))
            is UiDrawPrimitive.ClipPathPush -> {
                clipStack.addLast(ClipSnapshot(floatArrayOf(clipX0, clipY0, clipX1, clipY1), activePathClips.size))
                clipX0 = max(clipX0, primitive.boundsRect.x)
                clipY0 = max(clipY0, primitive.boundsRect.y)
                clipX1 = min(clipX1, primitive.boundsRect.x + primitive.boundsRect.width)
                clipY1 = min(clipY1, primitive.boundsRect.y + primitive.boundsRect.height)
                activePathClips += primitive.path
            }
            is UiDrawPrimitive.ClipPush -> {
                clipStack.addLast(ClipSnapshot(floatArrayOf(clipX0, clipY0, clipX1, clipY1), activePathClips.size))
                clipX0 = max(clipX0, primitive.rect.x)
                clipY0 = max(clipY0, primitive.rect.y)
                clipX1 = min(clipX1, primitive.rect.x + primitive.rect.width)
                clipY1 = min(clipY1, primitive.rect.y + primitive.rect.height)
            }
            is UiDrawPrimitive.ClipPop -> {
                clipStack.removeLastOrNull()?.let { restored ->
                    clipX0 = restored.rect[0]
                    clipY0 = restored.rect[1]
                    clipX1 = restored.rect[2]
                    clipY1 = restored.rect[3]
                    while (activePathClips.size > restored.activePathCount) {
                        activePathClips.removeAt(activePathClips.lastIndex)
                    }
                }
            }
        }
    }
    return pixels
}

private fun lerpColor(start: Color, end: Color, fraction: Float): Color = Color(
    r = start.r + (end.r - start.r) * fraction,
    g = start.g + (end.g - start.g) * fraction,
    b = start.b + (end.b - start.b) * fraction,
    a = start.a + (end.a - start.a) * fraction
)
