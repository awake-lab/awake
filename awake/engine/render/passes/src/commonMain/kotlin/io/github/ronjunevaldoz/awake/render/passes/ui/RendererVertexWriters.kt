// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.passes.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiPrimitiveTransform

/**
 * Pure vertex-buffer writers shared across Vulkan and WebGPU renderers.
 *
 * Each function writes one vertex's worth of floats into a caller-supplied [FloatArray]
 * at the specified [offset].
 */

/** Identity transform (scale 1, pivot origin) -- used when transform is null. */
val IDENTITY_TRANSFORM = UiPrimitiveTransform(scaleX = 1f, scaleY = 1f, pivotX = 0f, pivotY = 0f)

/** Writes a standard 2D colored quad vertex (10 floats). */
fun writeVertex(
    out: FloatArray,
    offset: Int,
    x: Float,
    y: Float,
    color: Color,
    transform: UiPrimitiveTransform? = null,
) {
    out[offset] = x
    out[offset + 1] = y
    out[offset + 2] = color.r
    out[offset + 3] = color.g
    out[offset + 4] = color.b
    out[offset + 5] = color.a
    val t = transform ?: IDENTITY_TRANSFORM
    out[offset + 6] = t.scaleX
    out[offset + 7] = t.scaleY
    out[offset + 8] = t.pivotX
    out[offset + 9] = t.pivotY
}

/** Writes a textured glyph quad vertex (12 floats). */
fun writeGlyphVertex(
    out: FloatArray,
    offset: Int,
    x: Float,
    y: Float,
    u: Float,
    v: Float,
    color: Color,
    transform: UiPrimitiveTransform? = null,
) {
    out[offset] = x
    out[offset + 1] = y
    out[offset + 2] = u
    out[offset + 3] = v
    out[offset + 4] = color.r
    out[offset + 5] = color.g
    out[offset + 6] = color.b
    out[offset + 7] = color.a
    val t = transform ?: IDENTITY_TRANSFORM
    out[offset + 8] = t.scaleX
    out[offset + 9] = t.scaleY
    out[offset + 10] = t.pivotX
    out[offset + 11] = t.pivotY
}

/** Writes a 3D debug line vertex (7 floats). */
fun writeLineVertex(
    out: FloatArray,
    offset: Int,
    x: Float,
    y: Float,
    z: Float,
    color: FloatArray,
) {
    out[offset] = x
    out[offset + 1] = y
    out[offset + 2] = z
    out[offset + 3] = color[0]
    out[offset + 4] = color[1]
    out[offset + 5] = color[2]
    out[offset + 6] = if (color.size > 3) color[3] else 1f
}

/** Writes a rounded-quad vertex with anti-aliased smoothing (16 floats). */
fun writeRoundedQuadVertex(
    out: FloatArray,
    offset: Int,
    x: Float,
    y: Float,
    localX: Float,
    localY: Float,
    halfW: Float,
    halfH: Float,
    radius: Float,
    smoothing: Float = 0.0f,
    color: Color,
    transform: UiPrimitiveTransform? = null,
) {
    out[offset] = x
    out[offset + 1] = y
    out[offset + 2] = localX
    out[offset + 3] = localY
    out[offset + 4] = halfW
    out[offset + 5] = halfH
    out[offset + 6] = radius
    out[offset + 7] = smoothing
    out[offset + 8] = color.r
    out[offset + 9] = color.g
    out[offset + 10] = color.b
    out[offset + 11] = color.a
    val t = transform ?: IDENTITY_TRANSFORM
    out[offset + 12] = t.scaleX
    out[offset + 13] = t.scaleY
    out[offset + 14] = t.pivotX
    out[offset + 15] = t.pivotY
}

/** Overload for rounded quad without explicit smoothing (defaults smoothing to 0f). */
fun writeRoundedQuadVertex(
    out: FloatArray,
    offset: Int,
    x: Float,
    y: Float,
    localX: Float,
    localY: Float,
    halfW: Float,
    halfH: Float,
    radius: Float,
    color: Color,
    transform: UiPrimitiveTransform? = null,
) = writeRoundedQuadVertex(
    out = out,
    offset = offset,
    x = x,
    y = y,
    localX = localX,
    localY = localY,
    halfW = halfW,
    halfH = halfH,
    radius = radius,
    smoothing = 0.0f,
    color = color,
    transform = transform,
)
