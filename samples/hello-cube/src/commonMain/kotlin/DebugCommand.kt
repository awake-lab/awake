// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * A command sent over `DebugControlServer`'s (desktopMain) WebSocket channel -- see that
 * file's doc comment for the full protocol. Lives in commonMain (not desktopMain) even
 * though only the desktop debug server currently sends these, because parsing is pure logic
 * (see this project's `.claude/AGENTS.md` "no app-layer test doubles" rule: push logic into
 * small pure functions specifically so it's unit-testable without a GPU/WebSocket).
 *
 * A `type`-discriminator field parsed by hand, rather than kotlinx.serialization's
 * polymorphic/sealed-class JSON support -- for a 4-command protocol this small, a manual
 * `when` is less machinery than a `SerializersModule` + per-subclass `@Serializable`
 * registration, and keeps the "unknown/malformed command" case (returns `null`) an explicit,
 * easy-to-follow branch rather than a deserialization exception to catch.
 */
sealed interface DebugCommand {
    data class SwitchDemo(val index: Int) : DebugCommand
    data class SetCameraEye(val x: Float, val y: Float, val z: Float) : DebugCommand
    data class SetCameraCenter(val x: Float, val y: Float, val z: Float) : DebugCommand
    data class SetMinimap(val enabled: Boolean) : DebugCommand
    data object GetState : DebugCommand
}

/** Parses one WebSocket text frame into a [DebugCommand], or `null` if it's malformed/unknown
 * (an unrecognized `type`, or a JSON parse failure) -- callers should just skip a `null`
 * rather than crash the debug channel over one bad frame. */
fun parseDebugCommand(text: String): DebugCommand? {
    val obj = runCatching { Json.parseToJsonElement(text).jsonObject }.getOrNull() ?: return null
    return when (obj["type"]?.jsonPrimitive?.contentOrNull) {
        "switchDemo" -> obj["index"]?.jsonPrimitive?.int?.let { DebugCommand.SwitchDemo(it) }
        "setCameraEye" -> obj.readVec3()?.let { (x, y, z) -> DebugCommand.SetCameraEye(x, y, z) }
        "setCameraCenter" -> obj.readVec3()?.let { (x, y, z) -> DebugCommand.SetCameraCenter(x, y, z) }
        "setMinimap" -> obj["enabled"]?.jsonPrimitive?.contentOrNull?.toBooleanStrictOrNull()
            ?.let { DebugCommand.SetMinimap(it) }
        "getState" -> DebugCommand.GetState
        else -> null
    }
}

private fun JsonObject.readVec3(): Triple<Float, Float, Float>? {
    val x = this["x"]?.jsonPrimitive?.contentOrNull?.toFloatOrNull() ?: return null
    val y = this["y"]?.jsonPrimitive?.contentOrNull?.toFloatOrNull() ?: return null
    val z = this["z"]?.jsonPrimitive?.contentOrNull?.toFloatOrNull() ?: return null
    return Triple(x, y, z)
}
