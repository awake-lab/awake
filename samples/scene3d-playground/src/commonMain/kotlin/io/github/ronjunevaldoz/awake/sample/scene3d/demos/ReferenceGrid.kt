// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.demos

import io.github.ronjunevaldoz.awake.core.math.Grid
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.render.renderer.Renderer

private const val GRID_SIZE = 10f
private const val GRID_DIVISIONS = 10
private const val AXIS_LENGTH = 2f

private val GRID_COLOR = floatArrayOf(0.55f, 0.55f, 0.6f, 1f)
internal val AXIS_COLOR_X = floatArrayOf(0.9f, 0.15f, 0.15f, 1f)
internal val AXIS_COLOR_Y = floatArrayOf(0.15f, 0.75f, 0.15f, 1f)
internal val AXIS_COLOR_Z = floatArrayOf(0.15f, 0.35f, 0.9f, 1f)

/** Nudges the axis lines just above the grid plane so they don't z-fight with it. */
private const val AXIS_LIFT = 0.01f

/**
 * Static geometry, so it is built once instead of re-allocating ~100 objects every frame in
 * every demo's `onUpdate`.
 */
private val REFERENCE_LINES: List<LineSegment> =
    Grid.lines(size = GRID_SIZE, divisions = GRID_DIVISIONS)
        .map { (a, b) -> LineSegment(a, b, GRID_COLOR) } +
        listOf(
            LineSegment(Vec3(0f, AXIS_LIFT, 0f), Vec3(AXIS_LENGTH, AXIS_LIFT, 0f), AXIS_COLOR_X),
            LineSegment(Vec3(0f, AXIS_LIFT, 0f), Vec3(0f, AXIS_LENGTH, 0f), AXIS_COLOR_Y),
            LineSegment(Vec3(0f, AXIS_LIFT, 0f), Vec3(0f, AXIS_LIFT, AXIS_LENGTH), AXIS_COLOR_Z)
        )

/**
 * Draws a standard reference grid and X/Y/Z axes.
 */
internal fun Renderer.drawReferenceGrid() {
    drawDebugLines(REFERENCE_LINES)
}
