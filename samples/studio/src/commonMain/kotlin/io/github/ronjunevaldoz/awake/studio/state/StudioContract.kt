// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.state

import io.github.ronjunevaldoz.awake.scene.controls.components.CameraMode
import io.github.ronjunevaldoz.awake.studio.examples.StudioExamples

internal object StudioContract {
    enum class Projection { Perspective, Orthographic }

    /**
     * What a drag in the viewport does.
     *
     * Real modes, not decoration: the gizmo reads this to decide which handles to draw and what a
     * drag means. The rail this replaced held five tools that only ever looked pressed.
     */
    enum class Tool { Select, Move, Rotate, Scale }

    /**
     * Edit authors the scene; Play runs it.
     *
     * The split is what makes gameplay systems (spin, the skinned-mesh driver) safe to have at
     * all in an editor: in Edit they do not tick, so a transform typed into the inspector stays
     * where it was put instead of being overwritten by an animation the next frame.
     */
    enum class Mode { Edit, Play }

    data class ExampleState(val activeExampleId: String = StudioExamples.first().id)

    data class InspectorState(val selectedEntityId: Int? = null)

    data class ToolState(val active: Tool = Tool.Move)

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
        val mode: Mode = Mode.Edit,
        val examples: ExampleState = ExampleState(),
        val inspector: InspectorState = InspectorState(),
        val tools: ToolState = ToolState(),
        val camera: CameraState = CameraState(),
        val console: ConsoleState = ConsoleState(),
    )

    sealed interface Intent {
        data class SelectExample(val id: String) : Intent
        data class SelectEntity(val id: Int?) : Intent
        data class SetCameraMode(val mode: CameraMode) : Intent
        data class SetProjection(val projection: Projection) : Intent
        data class SelectTool(val tool: Tool) : Intent
        data class SetMode(val mode: Mode) : Intent
        data object SaveScene : Intent
        // One-shot: snaps the editor (Scene-view) camera's pose to match the scene's own
        // authored camera, like Blender's "View from Camera" -- not a live lock, the editor
        // camera is free to orbit away again right after.
        data object AlignViewToCamera : Intent
        data class AppendConsole(val level: ConsoleLevel, val message: String) : Intent
        data object ClearConsole : Intent
    }

    sealed interface Effect {
        // World mutation is a side effect; a reducer shouldn't perform it directly.
        data class LoadExample(val exampleId: String) : Effect

        // Reading the live world and writing a file are both side effects, for the same reason.
        data object SaveScene : Effect

        // Mutating the editor camera's CameraComponent orbit state is a side effect too.
        data object AlignViewToCamera : Effect
    }
}
