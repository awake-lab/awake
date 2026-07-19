// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.application

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.input.Key

private const val GLFW_KEY_SPACE = 32
private const val GLFW_KEY_ESCAPE = 256
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
    GLFW_KEY_ESCAPE to Key.Escape
)

/**
 * Polls a GLFW window's keyboard, pointer, and wheel state into Awake's shared [Input].
 *
 * The pointer coordinates are normalized into framebuffer-pixel space so the result matches
 * the coordinates the UI runtime and renderer use on HiDPI displays.
 */
fun pollGlfwInput(
    window: Long,
    keys: Map<Int, Key> = DefaultGlfwGameplayKeys
): Unit = pollGlfwInput(glfwWindowInput(window), keys)

/** Testable core: takes the [GlfwWindowInput] seam instead of a raw window handle, so a
 * desktopTest can fake key/pointer/scroll state and assert the resulting [Input] calls. */
internal fun pollGlfwInput(
    reader: GlfwWindowInput,
    keys: Map<Int, Key> = DefaultGlfwGameplayKeys
) {
    keys.forEach { (glfwKey, key) ->
        Input.setKeyDown(key, reader.isKeyDown(glfwKey))
    }

    Input.setPointer(
        down = reader.isMouseButtonDown(GLFW_MOUSE_BUTTON_LEFT),
        x = reader.cursorX().toFloat() * reader.framebufferScaleX(),
        y = reader.cursorY().toFloat() * reader.framebufferScaleY()
    )
    // Clear and populate the frame's stable scroll delta
    Input.scrollDeltaY = reader.consumeScrollDeltaY().toFloat()
}
