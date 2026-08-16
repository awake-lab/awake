// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.gizmo

import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.ClipSpace
import io.github.ronjunevaldoz.awake.core.math.Vec3
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val WIDTH = 800f
private const val HEIGHT = 600f
private const val HANDLE = 1f

/** Looks down -Z at the origin from a little way back, so +X is screen-right and +Y screen-up. */
private val camera = Camera(
    eye = Vec3(0f, 0f, 6f),
    center = Vec3(0f, 0f, 0f),
    fovYRadians = 1f,
    near = 0.1f,
    far = 100f,
)

private val projection = ViewportProjection(
    camera = camera,
    viewProjection = camera.viewProjectionMatrix(WIDTH / HEIGHT, ClipSpace.WebGpu),
    width = WIDTH,
    height = HEIGHT,
)

class GizmoMathTest {

    @Test
    fun theOriginProjectsToTheCentreOfTheViewport() {
        val screen = assertNotNull(projection.toScreen(Vec3(0f, 0f, 0f)))
        assertEquals(WIDTH / 2f, screen.x, 0.5f)
        assertEquals(HEIGHT / 2f, screen.y, 0.5f)
    }

    @Test
    fun screenYGrowsDownwardWhileWorldYGrowsUpward() {
        val above = assertNotNull(projection.toScreen(Vec3(0f, 1f, 0f)))
        assertTrue(above.y < HEIGHT / 2f, "world +Y must project above the centre, was ${above.y}")
        val right = assertNotNull(projection.toScreen(Vec3(1f, 0f, 0f)))
        assertTrue(right.x > WIDTH / 2f, "world +X must project right of the centre, was ${right.x}")
    }

    /** Behind the camera has no screen position; projecting anyway mirrors it to the wrong side,
     * which would make a handle draggable from a place it isn't. */
    @Test
    fun aPointBehindTheCameraDoesNotProject() {
        assertNull(projection.toScreen(Vec3(0f, 0f, 10f)))
    }

    @Test
    fun pointingAtTheXHandlePicksTheXAxis() {
        val origin = Vec3(0f, 0f, 0f)
        val tip = assertNotNull(projection.toScreen(Vec3(HANDLE, 0f, 0f)))
        val midway = ScreenPoint((WIDTH / 2f + tip.x) / 2f, (HEIGHT / 2f + tip.y) / 2f)
        assertEquals(
            GizmoAxis.X,
            projection.hitTestHandle(origin, HANDLE, midway),
        )
    }

    @Test
    fun pointingAwayFromEveryHandleHitsNothing() {
        assertNull(
            projection.hitTestHandle(Vec3(0f, 0f, 0f), HANDLE, ScreenPoint(20f, 20f)),
        )
    }

    @Test
    fun draggingRightMovesAlongPositiveXAndDraggingLeftMovesBack() {
        val origin = Vec3(0f, 0f, 0f)
        val right = projection.dragAlongAxis(origin, GizmoAxis.X, HANDLE, dragX = 40f, dragY = 0f)
        assertTrue(right > 0f, "dragging right along the X handle must increase X, was $right")
        val left = projection.dragAlongAxis(origin, GizmoAxis.X, HANDLE, dragX = -40f, dragY = 0f)
        assertEquals(-right, left, 1e-3f, "the drag must be symmetric")
    }

    /** Dragging across an axis must not move along it, or every drag would smear all three. */
    @Test
    fun draggingPerpendicularToAnAxisDoesNotMoveAlongIt() {
        val moved = projection.dragAlongAxis(Vec3(0f, 0f, 0f), GizmoAxis.X, HANDLE, dragX = 0f, dragY = 50f)
        assertTrue(abs(moved) < 1e-3f, "a vertical drag must not move along X, was $moved")
    }

    /** The Z axis points straight at this camera, so its screen projection collapses. Dividing by
     * that length would fling the object away on a one-pixel drag. */
    @Test
    fun anAxisPointingAtTheCameraRefusesToDrag() {
        val moved = projection.dragAlongAxis(Vec3(0f, 0f, 0f), GizmoAxis.Z, HANDLE, dragX = 30f, dragY = 30f)
        assertEquals(0f, moved, "a camera-facing axis must not drag")
    }

    @Test
    fun clickingNearerOneEntityPicksThatOne() {
        val candidates = listOf(1 to Vec3(-1f, 0f, 0f), 2 to Vec3(1f, 0f, 0f))
        val rightOfCentre = assertNotNull(projection.toScreen(Vec3(1f, 0f, 0f)))
        assertEquals(2, projection.pickEntityAt(rightOfCentre, candidates))
    }

    @Test
    fun clickingEmptySpacePicksNothing() {
        val candidates = listOf(1 to Vec3(0f, 0f, 0f))
        assertNull(projection.pickEntityAt(ScreenPoint(5f, 5f), candidates))
    }
}
