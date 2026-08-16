// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.state

import io.github.ronjunevaldoz.awake.scene.controls.components.CameraMode
import io.github.ronjunevaldoz.awake.studio.examples.StudioExamples

internal object StudioContract {
    enum class Projection { Perspective, Orthographic }

    data class ExampleState(val activeExampleId: String = StudioExamples.first().id)

    data class InspectorState(val selectedEntityId: Int? = null)

    enum class ConsoleLevel { Info, Warning, Error }

    data class ConsoleEntry(val level: ConsoleLevel, val message: String)

    data class ConsoleState(val entries: List<ConsoleEntry> = emptyList())

    /** Mirrors [CameraComponent]'s own fields. Studio used to carry a parallel
     * `CameraPresetMode` (Orbit/Front/Top) with hand-rolled preset math, which left the engine's
     * Cinematic and TopDown unreachable and drag working only in Orbit. */
    data class CameraState(
        val mode: CameraMode = CameraMode.ThirdPerson,
        val projection: Projection = Projection.Perspective,
    )

    data class State(
        val examples: ExampleState = ExampleState(),
        val inspector: InspectorState = InspectorState(),
        val camera: CameraState = CameraState(),
        val console: ConsoleState = ConsoleState(),
    )

    sealed interface Intent {
        data class SelectExample(val id: String) : Intent
        data class SelectEntity(val id: Int?) : Intent
        data class SetCameraMode(val mode: CameraMode) : Intent
        data class SetProjection(val projection: Projection) : Intent
        data class AppendConsole(val level: ConsoleLevel, val message: String) : Intent
        data object ClearConsole : Intent
    }

    sealed interface Effect {
        // World mutation is a side effect; a reducer shouldn't perform it directly.
        data class LoadExample(val exampleId: String) : Effect
    }
}
