// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.application

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.input.Key

private const val GLFW_KEY_SPACE = 32
private const val GLFW_KEY_ESCAPE = 256
private const val GLFW_KEY_F1 = 290
private const val GLFW_KEY_F2 = 291
private const val GLFW_KEY_F3 = 292
private const val GLFW_KEY_F4 = 293
private const val GLFW_KEY_F5 = 294
private const val GLFW_KEY_RIGHT = 262
private const val GLFW_KEY_LEFT = 263
private const val GLFW_KEY_DOWN = 264
private const val GLFW_KEY_UP = 265
private const val GLFW_KEY_A = 65
private const val GLFW_KEY_D = 68
private const val GLFW_KEY_S = 83
private const val GLFW_KEY_W = 87
private const val GLFW_MOUSE_BUTTON_LEFT = 0

val DefaultGlfwGameplayKeys: Map<Int, Key> = linkedMapOf(
    GLFW_KEY_W to Key.W,
    GLFW_KEY_A to Key.A,
    GLFW_KEY_S to Key.S,
    GLFW_KEY_D to Key.D,
    GLFW_KEY_UP to Key.ArrowUp,
    GLFW_KEY_DOWN to Key.ArrowDown,
    GLFW_KEY_LEFT to Key.ArrowLeft,
    GLFW_KEY_RIGHT to Key.ArrowRight,
    GLFW_KEY_SPACE to Key.Space,
    GLFW_KEY_ESCAPE to Key.Escape,
    GLFW_KEY_F1 to Key.F1,
    GLFW_KEY_F2 to Key.F2,
    GLFW_KEY_F3 to Key.F3,
    GLFW_KEY_F4 to Key.F4,
    GLFW_KEY_F5 to Key.F5
)

/**
 * Polls a GLFW window's keyboard, pointer, and wheel state into Awake's shared [Input].
 *
 * The pointer coordinates are normalized into framebuffer-pixel space so the result matches
 * the coordinates the UI runtime and renderer use on HiDPI displays.
 */
fun pollGlfwInput(
    window: Long,
    input: Input,
    keys: Map<Int, Key> = DefaultGlfwGameplayKeys
): Unit = pollGlfwInput(glfwWindowInput(window), input, keys)

/** Testable core: takes the [GlfwWindowInput] seam instead of a raw window handle, so a
 * desktopTest can fake key/pointer/scroll state and assert the resulting [Input] calls. */
internal fun pollGlfwInput(
    reader: GlfwWindowInput,
    input: Input,
    keys: Map<Int, Key> = DefaultGlfwGameplayKeys
) {
    keys.forEach { (glfwKey, key) ->
        input.setKeyDown(key, reader.isKeyDown(glfwKey))
    }

    input.setPointer(
        down = reader.isMouseButtonDown(GLFW_MOUSE_BUTTON_LEFT),
        x = reader.cursorX().toFloat() * reader.framebufferScaleX(),
        y = reader.cursorY().toFloat() * reader.framebufferScaleY()
    )
    // Accumulate the hardware delta until the runtime snapshots it.
    input.scrollDeltaY += reader.consumeScrollDeltaY().toFloat()
}
