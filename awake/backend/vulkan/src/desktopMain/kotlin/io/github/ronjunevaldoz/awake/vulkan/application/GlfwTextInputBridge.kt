// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.application

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.input.TextEditAction

private const val GLFW_KEY_LEFT_SHIFT = 340
private const val GLFW_KEY_RIGHT_SHIFT = 344
private const val GLFW_KEY_SPACE = 32
private const val GLFW_KEY_ENTER = 257
private const val GLFW_KEY_RIGHT = 262
private const val GLFW_KEY_LEFT = 263
private const val GLFW_KEY_HOME = 268
private const val GLFW_KEY_END = 269
private const val GLFW_KEY_BACKSPACE = 259
private const val GLFW_KEY_DELETE = 261

private const val REPEAT_INITIAL_DELAY_SECONDS = 0.5
private const val REPEAT_INTERVAL_SECONDS = 0.05

/** GLFW key codes -> printable character (unshifted). Shift flips letters to uppercase; the
 * rest of this v1 punctuation set intentionally ignores shift (see task doc, full shift-symbol
 * mapping is a follow-up). */
private val PrintableKeys: Map<Int, Char> = buildMap {
    for (code in 65..90) put(code, ('a' + (code - 65))) // A-Z -> lowercase unless shifted
    for (code in 48..57) put(code, ('0' + (code - 48))) // 0-9
    put(GLFW_KEY_SPACE, ' ')
    put(44, ',')
    put(45, '-')
    put(46, '.')
    put(47, '/')
    put(59, ';')
    put(61, '=')
    put(91, '[')
    put(93, ']')
    put(39, '\'')
}

private val EditKeys: Map<Int, TextEditAction> = mapOf(
    GLFW_KEY_BACKSPACE to TextEditAction.Backspace,
    GLFW_KEY_DELETE to TextEditAction.Delete,
    GLFW_KEY_ENTER to TextEditAction.Enter,
    GLFW_KEY_LEFT to TextEditAction.ArrowLeft,
    GLFW_KEY_RIGHT to TextEditAction.ArrowRight,
    GLFW_KEY_HOME to TextEditAction.Home,
    GLFW_KEY_END to TextEditAction.End
)

private class KeyRepeatState {
    var wasDown = false
    var heldSeconds = 0.0
    var nextRepeatAt = REPEAT_INITIAL_DELAY_SECONDS
}

// ponytail: module-level mutable state (one host process/window), matches the rest of this
// file's polling style -- promote to an instance if multi-window ever lands.
private val repeatStates = HashMap<Int, KeyRepeatState>()

/** Test-only: [repeatStates] is shared module state, so a desktopTest exercising hold-to-repeat
 * across multiple cases must clear it between them or an earlier test's held key leaks in. */
internal fun resetGlfwTextInputRepeatStateForTest() {
    repeatStates.clear()
}

/**
 * Polls a GLFW window's keyboard state each frame and feeds Awake's shared text-input API
 * ([Input.pushTypedText]/[Input.pushEditAction]) -- separate from [pollGlfwInput], which only
 * drives the small gameplay [io.github.ronjunevaldoz.awake.core.input.Key] set.
 *
 * Pure polling (matches [pollGlfwInput]'s style, no GLFW char/key callbacks): tracks rising
 * edges plus a simple hold-to-repeat cadence per key.
 */
fun pollGlfwTextInput(window: Long, input: Input, deltaSeconds: Double = 1.0 / 60.0): Unit =
    pollGlfwTextInput(glfwWindowInput(window), input, deltaSeconds)

/** Testable core: takes the [GlfwWindowInput] seam instead of a raw window handle, so a
 * desktopTest can fake key-hold state across frames and assert the resulting
 * [Input.pushTypedText]/[Input.pushEditAction] calls, including shift and hold-to-repeat. */
internal fun pollGlfwTextInput(reader: GlfwWindowInput, input: Input, deltaSeconds: Double = 1.0 / 60.0) {
    val shiftDown = reader.isKeyDown(GLFW_KEY_LEFT_SHIFT) || reader.isKeyDown(GLFW_KEY_RIGHT_SHIFT)

    PrintableKeys.forEach { (glfwKey, char) ->
        pollKey(reader, glfwKey, deltaSeconds) {
            val shifted = if (shiftDown && char.isLetter()) char.uppercaseChar() else char
            input.pushTypedText(shifted.toString())
        }
    }
    EditKeys.forEach { (glfwKey, action) ->
        pollKey(reader, glfwKey, deltaSeconds) { input.pushEditAction(action) }
    }
}

private inline fun pollKey(reader: GlfwWindowInput, glfwKey: Int, deltaSeconds: Double, fire: () -> Unit) {
    val state = repeatStates.getOrPut(glfwKey) { KeyRepeatState() }
    val down = reader.isKeyDown(glfwKey)
    if (!down) {
        state.wasDown = false
        state.heldSeconds = 0.0
        state.nextRepeatAt = REPEAT_INITIAL_DELAY_SECONDS
        return
    }
    if (!state.wasDown) {
        state.wasDown = true
        state.heldSeconds = 0.0
        state.nextRepeatAt = REPEAT_INITIAL_DELAY_SECONDS
        fire()
        return
    }
    state.heldSeconds += deltaSeconds
    if (state.heldSeconds >= state.nextRepeatAt) {
        state.nextRepeatAt += REPEAT_INTERVAL_SECONDS
        fire()
    }
}
