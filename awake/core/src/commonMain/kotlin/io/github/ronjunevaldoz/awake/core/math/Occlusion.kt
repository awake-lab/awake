// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.math

import kotlin.math.max
import kotlin.math.min

// ponytail: single-occluder full-containment only -- a candidate is culled only when ONE
// occluder's screen rect fully covers it, never a union of several partial occluders. A grid-
// based coverage buffer would catch that case too, at the cost of a resolution constant, a
// per-frame clear, and cell rasterization. Upgrade path if a real scene needs multi-occluder
// union: replace this file's flat occluder list with that grid.

/** A world-space [Aabb]'s screen-space footprint, from [screenBounds] -- the rectangle its 8
 * corners project to, plus how close the box's nearest SURFACE POINT (not corner) is to the
 * eye. [nearestDistance] is Euclidean distance to [Camera.eye], not true view-space depth --
 * proportionate for a containment test, not a z-buffer. */
data class ScreenBounds(
    val minX: Float,
    val minY: Float,
    val maxX: Float,
    val maxY: Float,
    val nearestDistance: Float,
)

/** Projects [worldBounds]'s 8 corners through [viewProjection] via [projectToViewport], fitting
 * a screen rect around them. `null` when ANY corner is behind the camera -- a box straddling the
 * near plane has no well-defined screen rect, and treating it as occluded (or as occluding
 * something else) risks culling something actually visible, the same false-negative-only bias
 * [Frustum.intersects] already keeps. */
fun Camera.screenBounds(
    worldBounds: Aabb,
    viewProjection: Mat4,
    viewportWidth: Float = 1f,
    viewportHeight: Float = 1f,
): ScreenBounds? {
    var minX = Float.MAX_VALUE
    var minY = Float.MAX_VALUE
    var maxX = -Float.MAX_VALUE
    var maxY = -Float.MAX_VALUE
    for (corner in worldBounds.corners()) {
        val projected = projectToViewport(corner, viewProjection, viewportWidth, viewportHeight) ?: return null
        minX = min(minX, projected.x)
        minY = min(minY, projected.y)
        maxX = max(maxX, projected.x)
        maxY = max(maxY, projected.y)
    }
    return ScreenBounds(minX, minY, maxX, maxY, worldBounds.nearestDistanceTo(eye))
}

/** Distance from [point] to this box's nearest SURFACE point, not its nearest corner -- for a
 * wide/tall box (a wall, a floor) the closest corner is almost always far off to one side, while
 * the closest point on the box's actual surface can be directly in front of [point]. Using the
 * corner minimum here would make a wide occluder look farther away than a small candidate
 * sitting right behind it, which is exactly backwards for a containment test. Standard clamped-
 * point AABB distance: clamp [point] into the box per axis, then measure to that clamped point
 * (zero when [point] is inside the box). */
private fun Aabb.nearestDistanceTo(point: Vec3): Float {
    val clampedX = point.x.coerceIn(min.x, max.x)
    val clampedY = point.y.coerceIn(min.y, max.y)
    val clampedZ = point.z.coerceIn(min.z, max.z)
    return (point - Vec3(clampedX, clampedY, clampedZ)).length3()
}

/** True when [candidate]'s rect sits entirely inside [occluder]'s rect AND [occluder] is nearer
 * to the eye -- the only case a single flat occluder can safely hide a candidate behind it (see
 * this file's own `ponytail:` note for what a partial/multi-occluder case needs instead). */
fun isOccludedBy(candidate: ScreenBounds, occluder: ScreenBounds): Boolean =
    candidate.nearestDistance > occluder.nearestDistance &&
        candidate.minX >= occluder.minX &&
        candidate.maxX <= occluder.maxX &&
        candidate.minY >= occluder.minY &&
        candidate.maxY <= occluder.maxY
