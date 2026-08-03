// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.demos

import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.Grid
import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.sample.scene3d.Scene3DDemo
import io.github.ronjunevaldoz.awake.ui.designsystem.components.controls.shadcnSlider
import io.github.ronjunevaldoz.awake.ui.designsystem.components.selection.shadcnSwitch
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * A real cube spinning in place over a real reference ground grid. The cube's mesh comes from
 * [rotatingCubeGeometry] (copied out of the now-retired hello-cube sample, see that file's doc
 * comment); the ground grid is [Grid.lines] -- an existing, already-unit-tested
 * `awake-base` helper for exactly this ("a square reference/floor grid", see its own doc
 * comment) -- drawn as world-space debug lines rather than a second mesh+material, since a
 * flat reference grid has no fill to shade. Both are driven onto the screen via
 * [io.github.ronjunevaldoz.awake.engine.application.GameUiRuntime.provideDrawCalls]/
 * `drawDebugLines`, the hooks that let a [io.github.ronjunevaldoz.awake.engine.application.GameUiRuntime]
 * (a UI-only game runtime, with no 3D scene/camera of its own) still submit real geometry to
 * the one [Renderer.draw] call that drives the swapchain frame every frame.
 *
 * "Orbit"/"Zoom" control the *camera* (it orbits/dollies around the cube); the cube's own spin
 * is a separate, continuous auto-rotation advanced every frame by [update] -- independent of
 * either slider, so the demo is visibly "rotating" the moment it's selected, not only once a
 * slider is dragged.
 */
internal object RotatingCubeDemo {
    private var orbitDegrees = 35f
    private var zoom = 6f
    private var wireframe = false
    private var spinRadians = 0f

    private var cubeMesh: Mesh? = null
    private var material: Material? = null

    private const val SPIN_RADIANS_PER_SECOND = 0.8f
    private const val GRID_SIZE = 10f
    private const val GRID_DIVISIONS = 10

    val entry = Scene3DDemo(
        id = "rotating-cube",
        title = "Rotating cube",
        renderViewport = {
            // Real geometry is drawn by the 3D pass Scene3DPlaygroundFeature wires up (see
            // its own doc comment) -- this viewport column only needs to exist so the shell
            // lays out a center pane for it; the demo has nothing UI-authored to put here.
        },
        renderControls = {
            orbitDegrees = shadcnSlider(id = "cube-orbit", min = 0f, max = 360f, value = orbitDegrees, label = "Orbit")
            zoom = shadcnSlider(id = "cube-zoom", min = 2f, max = 15f, value = zoom, label = "Zoom")
            wireframe = shadcnSwitch(id = "cube-wireframe", checked = wireframe, label = "Wireframe")
            text(label = "Cube spins automatically; sliders move the camera.")
        }
    )

    fun isActive(activeDemoId: String) = activeDemoId == entry.id

    /** Builds this demo's mesh/material the first time it's needed (a UI-only game has no
     * upfront asset list to build these from, see [Renderer.createMesh]'s own doc comment),
     * advances the cube's own spin by [deltaSeconds], and stages this frame's debug lines --
     * the ground grid always, plus (while [wireframe] is on) the cube's own edges instead of
     * its solid mesh. Call once per frame, only while this demo is active; the solid draw
     * calls come from [cameraAndDrawCalls] afterwards. */
    fun update(renderer: Renderer, deltaSeconds: Float) {
        if (cubeMesh == null) cubeMesh = renderer.createMesh(rotatingCubeGeometry)
        if (material == null) material = renderer.createMaterial()

        spinRadians += deltaSeconds * SPIN_RADIANS_PER_SECOND

        val gridLines = Grid.lines(size = GRID_SIZE, divisions = GRID_DIVISIONS)
            .map { (a, b) -> LineSegment(a, b, GRID_COLOR) }
        val lines = if (wireframe) gridLines + wireframeCubeEdges() else gridLines
        renderer.drawDebugLines(lines)
    }

    /** Camera orbiting the cube per [orbitDegrees]/[zoom]; the solid cube draw call, only when
     * [wireframe] is off (its edges are drawn as debug lines instead, see [update]). The
     * ground grid has no solid draw call at all -- it's debug lines only, see [update]. */
    fun cameraAndDrawCalls(): Pair<Camera, List<DrawCall>> {
        val orbitRad = orbitDegrees * DEGREES_TO_RADIANS
        // Top-down POV: eye sits mostly overhead (tall Y term) with only a small horizontal
        // radius, so "Orbit" reads as a slight compass-direction tilt around the cube rather
        // than the eye-level, steeply-foreshortened view a large horizontal-to-vertical ratio
        // gives (the grid used to look almost edge-on instead of a floor plan).
        val eye = Vec3(zoom * 0.35f * sin(orbitRad), zoom * 1.6f, zoom * 0.35f * cos(orbitRad))
        val camera = Camera(
            eye = eye,
            center = Vec3(0f, 0.5f, 0f),
            fovYRadians = 45f * DEGREES_TO_RADIANS,
            near = 0.1f,
            far = 100f
        )
        val cube = if (!wireframe) {
            cubeMesh?.let { mesh -> material?.let { mat -> DrawCall(mesh, mat, Mat4().rotateY(spinRadians)) } }
        } else {
            null
        }
        return camera to listOfNotNull(cube)
    }

    private fun wireframeCubeEdges(): List<LineSegment> {
        val sin = sin(spinRadians)
        val cos = cos(spinRadians)
        val worldCorners = rotatingCubeLocalCorners.map { (x, y, z) ->
            // Same rotateY convention as cameraAndDrawCalls' cube model matrix (both rotate
            // the unit cube in place around the origin -- no translation, so the cube sits
            // half-embedded in the y=0 grid, same as its solid-mesh counterpart would).
            Vec3(x * cos + z * sin, y, -x * sin + z * cos)
        }
        return rotatingCubeEdgeIndices.map { (startIndex, endIndex) ->
            LineSegment(worldCorners[startIndex], worldCorners[endIndex], WIREFRAME_COLOR)
        }
    }

    private val WIREFRAME_COLOR = floatArrayOf(1f, 1f, 1f, 1f)
    private val GRID_COLOR = floatArrayOf(0.55f, 0.55f, 0.6f, 1f)
    private const val DEGREES_TO_RADIANS = (PI / 180.0).toFloat()
}
