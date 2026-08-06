// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.testing.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiPrimitiveTransform
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.containsPoint
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.font.UiFontSamplingMode
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.tessellateFill
import io.github.ronjunevaldoz.awake.ui.tessellateStroke
import io.github.ronjunevaldoz.awake.ui.toPath
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

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

    fun sampleAtlasRgba(font: UiFont, u: Float, v: Float): FloatArray {
        val atlasWidth = font.atlasWidth
        val atlasHeight = font.atlasHeight
        val atlasPixels = font.atlasPixelsRgba
        val x = (u.coerceIn(0f, 1f) * atlasWidth - 0.5f).coerceIn(0f, (atlasWidth - 1).toFloat())
        val y = (v.coerceIn(0f, 1f) * atlasHeight - 0.5f).coerceIn(0f, (atlasHeight - 1).toFloat())
        val x0 = x.toInt().coerceIn(0, atlasWidth - 1)
        val y0 = y.toInt().coerceIn(0, atlasHeight - 1)
        val x1 = (x0 + 1).coerceIn(0, atlasWidth - 1)
        val y1 = (y0 + 1).coerceIn(0, atlasHeight - 1)
        val tx = x - x0
        val ty = y - y0

        fun channelAt(px: Int, py: Int, channel: Int): Float =
            (atlasPixels[(py * atlasWidth + px) * 4 + channel].toInt() and 0xFF) / 255f

        fun sampleChannel(channel: Int): Float {
            val top = channelAt(x0, y0, channel) * (1f - tx) + channelAt(x1, y0, channel) * tx
            val bottom = channelAt(x0, y1, channel) * (1f - tx) + channelAt(x1, y1, channel) * tx
            return top * (1f - ty) + bottom * ty
        }

        return floatArrayOf(
            sampleChannel(0),
            sampleChannel(1),
            sampleChannel(2),
            sampleChannel(3)
        )
    }

    fun smoothstep(edge0: Float, edge1: Float, value: Float): Float {
        if (edge0 == edge1) {
            return if (value < edge0) 0f else 1f
        }
        val t = ((value - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
        return t * t * (3f - 2f * t)
    }

    fun median3(a: Float, b: Float, c: Float): Float = max(min(a, b), min(max(a, b), c))

    fun sampleGlyphAlpha(font: UiFont, glyph: UiDrawPrimitive.Glyph, u: Float, v: Float): Int {
        val rgba = sampleAtlasRgba(font, u, v)
        val coverage = when (font.samplingMode) {
            UiFontSamplingMode.CoverageAlpha -> rgba[3]
            UiFontSamplingMode.DistanceField -> {
                val signedDistance = median3(rgba[0], rgba[1], rgba[2])
                val glyphScale = min(glyph.w, glyph.h) / font.cellSize.toFloat()
                val edgeSoftness = 0.5f / max(1f, font.distanceFieldRangePx * glyphScale)
                smoothstep(0.5f - edgeSoftness, 0.5f + edgeSoftness, signedDistance)
            }
        }
        return (coverage * 255f).toInt().coerceIn(0, 255)
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

    fun fillRoundedQuad(x: Float, y: Float, w: Float, h: Float, rawRadius: Float, smoothing: Float, color: Color) {
        val x0 = max(x, clipX0).toInt().coerceIn(0, width)
        val y0 = max(y, clipY0).toInt().coerceIn(0, height)
        val x1 = min(x + w, clipX1).toInt().coerceIn(0, width)
        val y1 = min(y + h, clipY1).toInt().coerceIn(0, height)
        val halfW = w / 2f
        val halfH = h / 2f
        val radius = rawRadius.coerceAtMost(minOf(halfW, halfH))
        val centerX = x + halfW
        val centerY = y + halfH
        val pExp = 2.0f + 4.0f * smoothing.coerceIn(0f, 1f)

        val cr = (color.r * 255).toInt().coerceIn(0, 255)
        val cg = (color.g * 255).toInt().coerceIn(0, 255)
        val cb = (color.b * 255).toInt().coerceIn(0, 255)
        val ca = color.a

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
                val localX = kotlin.math.abs(sampleX - centerX)
                val localY = kotlin.math.abs(sampleY - centerY)
                val qx = localX - (halfW - radius)
                val qy = localY - (halfH - radius)

                val dist = if (smoothing > 0f && qx > 0f && qy > 0f) {
                    (qx.toDouble().pow(pExp.toDouble()) + qy.toDouble().pow(pExp.toDouble())).pow(1.0 / pExp.toDouble()).toFloat() - radius
                } else {
                    val maxQx = max(qx, 0f)
                    val maxQy = max(qy, 0f)
                    kotlin.math.sqrt(maxQx * maxQx + maxQy * maxQy) + min(max(qx, qy), 0f) - radius
                }

                if (dist <= 1.0f) {
                    val alphaFactor = (1.0f - smoothstep(-1.0f, 1.0f, dist)) * ca
                    if (alphaFactor > 0f) {
                        val offset = (py * width + px) * 4
                        val bgA = (pixels[offset + 3].toInt() and 0xFF) / 255f
                        val outA = alphaFactor + bgA * (1f - alphaFactor)
                        val r = (cr * alphaFactor + (pixels[offset].toInt() and 0xFF) * (1f - alphaFactor)).toInt().coerceIn(0, 255)
                        val g = (cg * alphaFactor + (pixels[offset + 1].toInt() and 0xFF) * (1f - alphaFactor)).toInt().coerceIn(0, 255)
                        val b = (cb * alphaFactor + (pixels[offset + 2].toInt() and 0xFF) * (1f - alphaFactor)).toInt().coerceIn(0, 255)
                        pixels[offset] = r.toByte()
                        pixels[offset + 1] = g.toByte()
                        pixels[offset + 2] = b.toByte()
                        pixels[offset + 3] = (outA * 255).toInt().coerceIn(0, 255).toByte()
                    }
                }
                px += 1
            }
            py += 1
        }
    }

    /** Minifying a glyph (source atlas footprint bigger than the on-screen quad -- the common
     * case for [io.github.ronjunevaldoz.awake.ui.font.UiFontSamplingMode.CoverageAlpha]
     * atlases like `BitmapFont`, whose raster resolution is baked well above typical render
     * sizes) with a single bilinear tap aliases badly: fine 8x8-grid structure falls between
     * samples and the glyph reads as noise instead of a letterform. There's no mip chain here,
     * so this box-filters instead -- averaging a small supersample grid per output pixel,
     * sized to the actual minification ratio on each axis (1 tap when not minifying, more as
     * the source footprint grows relative to the destination pixel, capped for performance). */
    fun drawGlyph(glyph: UiDrawPrimitive.Glyph, font: UiFont) {
        val x0 = max(glyph.x, clipX0).toInt().coerceIn(0, width)
        val y0 = max(glyph.y, clipY0).toInt().coerceIn(0, height)
        val x1 = min(glyph.x + glyph.w, clipX1).toInt().coerceIn(0, width)
        val y1 = min(glyph.y + glyph.h, clipY1).toInt().coerceIn(0, height)
        val r = (glyph.color[0] * 255).toInt().coerceIn(0, 255)
        val g = (glyph.color[1] * 255).toInt().coerceIn(0, 255)
        val b = (glyph.color[2] * 255).toInt().coerceIn(0, 255)
        val tintAlpha = (glyph.color.a * 255).toInt().coerceIn(0, 255)

        val sourceTexelsX = (glyph.u1 - glyph.u0) * font.atlasWidth
        val sourceTexelsY = (glyph.v1 - glyph.v0) * font.atlasHeight
        val samplesX = (sourceTexelsX / glyph.w.coerceAtLeast(1f)).toInt().coerceIn(1, 6)
        val samplesY = (sourceTexelsY / glyph.h.coerceAtLeast(1f)).toInt().coerceIn(1, 6)

        var py = y0
        while (py < y1) {
            var px = x0
            while (px < x1) {
                if (!passesPathClips(px + 0.5f, py + 0.5f)) {
                    px += 1
                    continue
                }
                var alphaSum = 0
                for (sy in 0 until samplesY) {
                    for (sx in 0 until samplesX) {
                        val sampleX = px + (sx + 0.5f) / samplesX
                        val sampleY = py + (sy + 0.5f) / samplesY
                        val u = ((sampleX - glyph.x) / glyph.w).coerceIn(0f, 0.9999f)
                        val v = ((sampleY - glyph.y) / glyph.h).coerceIn(0f, 0.9999f)
                        alphaSum += sampleGlyphAlpha(
                            font,
                            glyph,
                            glyph.u0 + (glyph.u1 - glyph.u0) * u,
                            glyph.v0 + (glyph.v1 - glyph.v0) * v
                        )
                    }
                }
                val sourceAlpha = alphaSum / (samplesX * samplesY)
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

    /** Applies [transform]'s scale-around-pivot to an axis-aligned rect -- mirrors the GPU
     * vertex shaders' `pivot + (position - pivot) * scale` math (see `ui_quad.vert`/`.wgsl`),
     * so this CPU rasterizer produces the same footprint the real backends draw for a scaled
     * [UiDrawPrimitive.Quad]/`RoundedQuad`/`Glyph`/`Texture`. `transform == null` (the common
     * case, no active `graphicsLayer` scale) returns the rect unchanged. */
    fun scaledRect(x: Float, y: Float, w: Float, h: Float, transform: UiPrimitiveTransform?): FloatArray {
        if (transform == null) return floatArrayOf(x, y, w, h)
        val scaledX = transform.pivotX + (x - transform.pivotX) * transform.scaleX
        val scaledY = transform.pivotY + (y - transform.pivotY) * transform.scaleY
        return floatArrayOf(scaledX, scaledY, w * transform.scaleX, h * transform.scaleY)
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
            is UiDrawPrimitive.Quad -> {
                val (x, y, w, h) = scaledRect(primitive.x, primitive.y, primitive.w, primitive.h, primitive.transform)
                fillRect(x, y, w, h, primitive.color)
            }
            is UiDrawPrimitive.GradientQuad -> fillGradientRect(primitive.x, primitive.y, primitive.w, primitive.h, primitive.gradient)
            is UiDrawPrimitive.RoundedQuad -> {
                val (x, y, w, h) = scaledRect(primitive.x, primitive.y, primitive.w, primitive.h, primitive.transform)
                val scale = min(primitive.transform?.scaleX ?: 1f, primitive.transform?.scaleY ?: 1f)
                fillRoundedQuad(x, y, w, h, primitive.radius * scale, primitive.smoothing, primitive.color)
            }
            is UiDrawPrimitive.FilledPath -> fillTriangleMesh(primitive.path.tessellateFill(), primitive.color)
            is UiDrawPrimitive.StrokedPath -> fillTriangleMesh(primitive.path.tessellateStroke(primitive.stroke), primitive.color)
            is UiDrawPrimitive.Glyph -> {
                val (x, y, w, h) = scaledRect(primitive.x, primitive.y, primitive.w, primitive.h, primitive.transform)
                val scaledGlyph = if (primitive.transform == null) primitive else primitive.copy(x = x, y = y, w = w, h = h)
                if (font != null) {
                    drawGlyph(scaledGlyph, font)
                } else {
                    val inset = min(w, h) * 0.25f
                    fillRect(x + inset, y + inset, w - inset * 2, h - inset * 2, primitive.color)
                }
            }
            is UiDrawPrimitive.Texture -> {
                val (x, y, w, h) = scaledRect(primitive.x, primitive.y, primitive.w, primitive.h, primitive.transform)
                fillRect(x, y, w, h, Color(0.5f, 0.5f, 0.5f, 1f))
            }
            is UiDrawPrimitive.ShadowQuad -> {
                // Simple solid fill for shadow in software rasterizer for now
                fillRect(
                    primitive.x + primitive.offsetX - primitive.spread,
                    primitive.y + primitive.offsetY - primitive.spread,
                    primitive.w + primitive.spread * 2f,
                    primitive.h + primitive.spread * 2f,
                    primitive.color
                )
            }
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
