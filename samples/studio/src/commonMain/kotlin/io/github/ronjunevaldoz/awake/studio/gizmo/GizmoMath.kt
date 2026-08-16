// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.gizmo

import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.math.Vec4
import io.github.ronjunevaldoz.awake.core.math.transformPosition
import kotlin.math.sqrt

/** A point in the viewport's own pixels, origin top-left -- the same space the UI frame and
 * [io.github.ronjunevaldoz.awake.render.renderer.RenderViewport] use. */
data class ScreenPoint(val x: Float, val y: Float)

/** The three translate axes. Rotate/scale handles reuse the same hit-testing once they exist. */
enum class GizmoAxis(val direction: Vec3, val color: FloatArray) {
    X(Vec3(1f, 0f, 0f), floatArrayOf(1f, 0.2f, 0.2f, 1f)),
    Y(Vec3(0f, 1f, 0f), floatArrayOf(0.2f, 1f, 0.2f, 1f)),
    Z(Vec3(0f, 0f, 1f), floatArrayOf(0.2f, 0.4f, 1f, 1f)),
}

/**
 * One viewport's worth of projection context: the camera, its matrix, and the pixel size of the
 * region it draws into.
 *
 * These four always travel together -- passing them separately to every hit-test and drag meant
 * five parameters per call and five chances to pass a stale one. Build it once per frame from the
 * primary camera and the viewport panel's bounds.
 *
 * Everything here projects rather than unprojects. A ray cast would need the inverse
 * view-projection, and this repo's `Mat4` has no inverse -- writing one (plus its tests) to pick
 * an object is more machinery than the job needs, because every point being tested (an entity's
 * origin, a handle's tip) is already known in world space. Screen-space distance answers the same
 * question with the matrix that already exists.
 */
data class ViewportProjection(
    val camera: Camera,
    val viewProjection: Mat4,
    val width: Float,
    val height: Float,
)

/** Projects a world point into viewport pixels, or `null` when it is behind the camera. */
fun ViewportProjection.toScreen(world: Vec3): ScreenPoint? {
    // Rejected by view-space depth against the camera, not by the clip w: with this repo's
    // matrix convention w stays positive for points BEHIND the eye (verified by dumping clip
    // coordinates), so a behind-camera handle would project to a mirrored on-screen position and
    // look draggable from a place it is not.
    if (!isInFrontOfCamera(world, camera)) return null
    val clip = viewProjection.transformPosition(Vec4(world.x, world.y, world.z, 1f))
    if (clip.w == 0f) return null
    val ndcX = clip.x / clip.w
    val ndcY = clip.y / clip.w
    // NDC is +Y up in both backends' convention as it reaches here (the clip-space Y flip is the
    // projection's own job, see Renderer.clipSpace), and viewport pixels are +Y down.
    return ScreenPoint(
        x = (ndcX * 0.5f + 0.5f) * width,
        y = (0.5f - ndcY * 0.5f) * height,
    )
}

/**
 * The axis handle under [pointer], or `null` when the pointer is not on one.
 *
 * Hit-tested in screen space against the drawn handle segment itself, so this is exact for the
 * thing the user is actually aiming at -- unlike [pickEntityAt], which approximates.
 */
fun ViewportProjection.hitTestHandle(
    origin: Vec3,
    handleLength: Float,
    pointer: ScreenPoint,
    tolerance: Float = HANDLE_TOLERANCE_PX,
): GizmoAxis? {
    val originScreen = toScreen(origin) ?: return null
    var best: GizmoAxis? = null
    var bestDistance = tolerance
    GizmoAxis.entries.forEach { axis ->
        val tip = Vec3(
            origin.x + axis.direction.x * handleLength,
            origin.y + axis.direction.y * handleLength,
            origin.z + axis.direction.z * handleLength,
        )
        val tipScreen = toScreen(tip) ?: return@forEach
        val distance = distanceToSegment(pointer, originScreen, tipScreen)
        if (distance < bestDistance) {
            bestDistance = distance
            best = axis
        }
    }
    return best
}

/**
 * How far to move along [axis] for a pointer that moved [dragX]/[dragY] pixels.
 *
 * The axis is projected to screen and the drag is projected onto that direction, so dragging
 * along the handle moves the object and dragging across it does nothing. Returns `0` when the
 * axis points nearly at the camera: its screen projection collapses to a point, and dividing by
 * that length would send the object to infinity for a one-pixel drag.
 */
fun ViewportProjection.dragAlongAxis(
    origin: Vec3,
    axis: GizmoAxis,
    handleLength: Float,
    dragX: Float,
    dragY: Float,
): Float {
    val originScreen = toScreen(origin) ?: return 0f
    val tip = Vec3(
        origin.x + axis.direction.x * handleLength,
        origin.y + axis.direction.y * handleLength,
        origin.z + axis.direction.z * handleLength,
    )
    val tipScreen = toScreen(tip) ?: return 0f
    val axisX = tipScreen.x - originScreen.x
    val axisY = tipScreen.y - originScreen.y
    val lengthSquared = axisX * axisX + axisY * axisY
    if (lengthSquared < MIN_AXIS_SCREEN_LENGTH_SQUARED) return 0f
    // Fraction of the handle's own screen length the drag covered, scaled back to world units.
    return ((dragX * axisX + dragY * axisY) / lengthSquared) * handleLength
}

/**
 * The nearest entity origin to [pointer], or `null` when nothing is within [tolerance].
 *
 * ponytail: picks by projected ORIGIN distance, not by the mesh's real silhouette, so a large
 * flat object (a ground plane) is only pickable near its centre and a small object in front of a
 * big one can lose. Upgrade path is a real ray/AABB test, which needs both a `Mat4` inverse and
 * per-mesh bounds that `MeshRenderer` does not carry today; do that when picking accuracy is
 * what's actually blocking someone.
 */
fun ViewportProjection.pickEntityAt(
    pointer: ScreenPoint,
    candidates: List<Pair<Int, Vec3>>,
    tolerance: Float = PICK_TOLERANCE_PX,
): Int? {
    var best: Int? = null
    var bestDistance = tolerance
    candidates.forEach { (entityId, position) ->
        val screen = toScreen(position) ?: return@forEach
        val dx = screen.x - pointer.x
        val dy = screen.y - pointer.y
        val distance = sqrt(dx * dx + dy * dy)
        if (distance < bestDistance) {
            bestDistance = distance
            best = entityId
        }
    }
    return best
}

/** Dot of (world - eye) with the camera's forward, against the near plane -- written out rather
 * than using Vec3 helpers so this allocates nothing on a path that runs every frame. */
private fun isInFrontOfCamera(world: Vec3, camera: Camera): Boolean {
    val forwardX = camera.center.x - camera.eye.x
    val forwardY = camera.center.y - camera.eye.y
    val forwardZ = camera.center.z - camera.eye.z
    val length = sqrt(forwardX * forwardX + forwardY * forwardY + forwardZ * forwardZ)
    if (length < 1e-6f) return false
    val toPointX = world.x - camera.eye.x
    val toPointY = world.y - camera.eye.y
    val toPointZ = world.z - camera.eye.z
    val depth = (toPointX * forwardX + toPointY * forwardY + toPointZ * forwardZ) / length
    return depth > camera.near
}

private fun distanceToSegment(point: ScreenPoint, start: ScreenPoint, end: ScreenPoint): Float {
    val segmentX = end.x - start.x
    val segmentY = end.y - start.y
    val lengthSquared = segmentX * segmentX + segmentY * segmentY
    if (lengthSquared < MIN_AXIS_SCREEN_LENGTH_SQUARED) {
        return sqrt((point.x - start.x) * (point.x - start.x) + (point.y - start.y) * (point.y - start.y))
    }
    val t = (((point.x - start.x) * segmentX + (point.y - start.y) * segmentY) / lengthSquared)
        .coerceIn(0f, 1f)
    val closestX = start.x + segmentX * t
    val closestY = start.y + segmentY * t
    return sqrt((point.x - closestX) * (point.x - closestX) + (point.y - closestY) * (point.y - closestY))
}

/** Generous: a 1px line is unhittable with a mouse, and these are the only draggable things in
 * the viewport, so over-claiming costs nothing. */
private const val HANDLE_TOLERANCE_PX = 10f
private const val PICK_TOLERANCE_PX = 60f

/** Below this the axis is pointing at the camera and its screen direction is noise. */
private const val MIN_AXIS_SCREEN_LENGTH_SQUARED = 4f
