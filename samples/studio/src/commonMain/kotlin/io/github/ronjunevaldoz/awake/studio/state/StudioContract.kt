// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.state

import io.github.ronjunevaldoz.awake.studio.examples.StudioExamples
import kotlin.math.PI

internal object StudioContract {
    enum class Tool { Layers, Grid, Environment, History, Panels }

    /** Orbit is continuous (yaw/pitch/distance); Front/Top are fixed axis-aligned presets --
     * see [io.github.ronjunevaldoz.awake.studio.state.CameraPresetMath]. */
    enum class CameraPresetMode { Orbit, Front, Top }

    enum class Projection { Perspective, Orthographic }

    data class ExampleState(val activeExampleId: String = StudioExamples.first().id)

    data class InspectorState(val selectedEntityId: Int? = null)

    data class ToolRailState(val activeTool: Tool = Tool.Layers)

    data class CameraState(
        val mode: CameraPresetMode = CameraPresetMode.Orbit,
        val yaw: Float = 0f,
        val pitch: Float = 0f,
        val distance: Float = DEFAULT_ORBIT_DISTANCE,
        val projection: Projection = Projection.Perspective,
    )

    data class State(
        val examples: ExampleState = ExampleState(),
        val inspector: InspectorState = InspectorState(),
        val toolRail: ToolRailState = ToolRailState(),
        val camera: CameraState = CameraState(),
    )

    sealed interface Intent {
        data class SelectExample(val id: String) : Intent
        data class SelectEntity(val id: Int?) : Intent
        data class SelectTool(val tool: Tool) : Intent
        data class SetCameraMode(val mode: CameraPresetMode) : Intent
        data class OrbitBy(val deltaYaw: Float, val deltaPitch: Float) : Intent
        data class SetProjection(val projection: Projection) : Intent
    }

    sealed interface Effect {
        // World mutation is a side effect; a reducer shouldn't perform it directly.
        data class LoadExample(val exampleId: String) : Effect
    }

    // Distance chosen to match every authored example scene's own eye-to-center distance, so
    // switching to Orbit (the default mode) doesn't jump the camera on load.
    const val DEFAULT_ORBIT_DISTANCE = 3f

    /** Matches `awake:scene:controls`' `CameraSystem` -- short of a right angle, where
     * `setLookAt`'s cross product would degenerate (core-math skill Rule 5). */
    val PITCH_LIMIT_RADIANS = (PITCH_LIMIT_DEGREES * DEGREES_TO_RADIANS).toFloat()

    private const val PITCH_LIMIT_DEGREES = 85.0
    private const val DEGREES_TO_RADIANS = PI / 180.0
}
