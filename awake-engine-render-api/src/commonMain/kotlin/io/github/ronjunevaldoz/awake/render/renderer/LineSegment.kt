// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.renderer

import io.github.ronjunevaldoz.awake.core.math.Vec3

/**
 * One world-space debug line (e.g. a [io.github.ronjunevaldoz.awake.core.math.Frustum]
 * wireframe edge) -- unlike [io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive] (screen-space
 * pixels), these are transformed by the scene's own view-projection matrix and drawn inside
 * the main 3D render pass so they get real depth-testing against scene geometry.
 */
data class LineSegment(
    val start: Vec3,
    val end: Vec3,
    val color: FloatArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LineSegment) return false
        return start == other.start && end == other.end && color.contentEquals(other.color)
    }

    override fun hashCode(): Int {
        var result = start.hashCode()
        result = 31 * result + end.hashCode()
        result = 31 * result + color.contentHashCode()
        return result
    }
}
