// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.state

import io.github.ronjunevaldoz.awake.studio.examples.StudioExamples

internal object StudioContract {
    enum class Tool { Layers, Grid, Environment, History, Panels }

    data class ExampleState(val activeExampleId: String = StudioExamples.first().id)

    data class InspectorState(val selectedEntityId: Int? = null)

    data class ToolRailState(val activeTool: Tool = Tool.Layers)

    data class State(
        val examples: ExampleState = ExampleState(),
        val inspector: InspectorState = InspectorState(),
        val toolRail: ToolRailState = ToolRailState(),
    )

    sealed interface Intent {
        data class SelectExample(val id: String) : Intent
        data class SelectEntity(val id: Int?) : Intent
        data class SelectTool(val tool: Tool) : Intent
    }

    sealed interface Effect {
        // World mutation is a side effect; a reducer shouldn't perform it directly.
        data class LoadExample(val exampleId: String) : Effect
    }
}
