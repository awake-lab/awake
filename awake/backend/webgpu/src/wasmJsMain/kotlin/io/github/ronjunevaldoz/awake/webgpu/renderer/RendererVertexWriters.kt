// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.renderer

import io.github.ronjunevaldoz.awake.core.colors.Color as AwakeColor

/** Pure vertex-buffer writers -- no `Renderer` state, just packing one vertex's floats into
 * an offset in a shared `FloatArray`. Extracted verbatim, package-visible (not `Renderer`
 * extension functions -- these don't touch any `Renderer` field) since `performDrawUi`
 * ([RendererDrawUi.kt]) and `performDrawDebugLines` ([RendererDraw3D.kt]) both call these. */

internal fun writeLineVertex(out: FloatArray, offset: Int, x: Float, y: Float, z: Float, color: FloatArray) {
    out[offset] = x
    out[offset + 1] = y
    out[offset + 2] = z
    out[offset + 3] = color[0]
    out[offset + 4] = color[1]
    out[offset + 5] = color[2]
    out[offset + 6] = if (color.size > 3) color[3] else 1f
}

internal fun writeVertex(out: FloatArray, offset: Int, x: Float, y: Float, color: AwakeColor) {
    out[offset] = x
    out[offset + 1] = y
    out[offset + 2] = color.r
    out[offset + 3] = color.g
    out[offset + 4] = color.b
    out[offset + 5] = color.a
}

internal fun writeGlyphVertex(out: FloatArray, offset: Int, x: Float, y: Float, u: Float, v: Float, color: AwakeColor) {
    out[offset] = x
    out[offset + 1] = y
    out[offset + 2] = u
    out[offset + 3] = v
    out[offset + 4] = color.r
    out[offset + 5] = color.g
    out[offset + 6] = color.b
    out[offset + 7] = color.a
}

/** Writes one rounded-quad vertex -- pos(vec2) + localPos(vec2) + halfSize(vec2) +
 * radius(float) + color(vec4), mirrors Vulkan's `writeRoundedQuadVertex`. */
internal fun writeRoundedQuadVertex(
    out: FloatArray,
    offset: Int,
    x: Float,
    y: Float,
    localX: Float,
    localY: Float,
    halfW: Float,
    halfH: Float,
    radius: Float,
    color: AwakeColor
) {
    out[offset] = x
    out[offset + 1] = y
    out[offset + 2] = localX
    out[offset + 3] = localY
    out[offset + 4] = halfW
    out[offset + 5] = halfH
    out[offset + 6] = radius
    out[offset + 7] = color.r
    out[offset + 8] = color.g
    out[offset + 9] = color.b
    out[offset + 10] = color.a
}
