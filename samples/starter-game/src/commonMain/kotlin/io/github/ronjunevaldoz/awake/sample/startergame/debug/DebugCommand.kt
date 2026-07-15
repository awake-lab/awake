// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.startergame.debug

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

sealed interface StarterDebugCommand {
    data class SwitchScene(val sceneId: String) : StarterDebugCommand
    data class SetTipsVisible(val enabled: Boolean) : StarterDebugCommand
    data object GetState : StarterDebugCommand
}

@Serializable
data class StarterDebugSnapshot(
    val activeSceneId: String,
    val activeSceneLabel: String,
    val sceneLabels: List<String>,
    val tipsVisible: Boolean
)

fun parseStarterDebugCommand(text: String): StarterDebugCommand? {
    val obj = runCatching { Json.parseToJsonElement(text).jsonObject }.getOrNull() ?: return null
    return when (obj["type"]?.jsonPrimitive?.contentOrNull) {
        "switchScene" -> obj["sceneId"]?.jsonPrimitive?.contentOrNull?.let(StarterDebugCommand::SwitchScene)
        "setTipsVisible" -> obj["enabled"]?.jsonPrimitive?.contentOrNull?.toBooleanStrictOrNull()
            ?.let(StarterDebugCommand::SetTipsVisible)
        "getState" -> StarterDebugCommand.GetState
        else -> null
    }
}
