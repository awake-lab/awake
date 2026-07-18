// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.application

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.input.Key
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanWindow

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
private const val GLFW_PRESS = 1

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
) {
    keys.forEach { (glfwKey, key) ->
        Input.setKeyDown(key, VulkanWindow.glfwGetKey(window, glfwKey) == GLFW_PRESS)
    }

    val cursor = VulkanWindow.glfwGetCursorPos(window)
    val scale = framebufferScale(window)
    val leftButtonDown = VulkanWindow.glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS
    Input.setPointer(
        down = leftButtonDown,
        x = cursor[0].toFloat() * scale.first,
        y = cursor[1].toFloat() * scale.second
    )
    val rawScroll = VulkanWindow.glfwConsumeScrollDeltaY(window).toFloat()
    Input.scrollDeltaY = rawScroll
    if (rawScroll != 0f) {
        System.err.println("[DEBUG] pollGlfwInput: rawScroll=$rawScroll")
    }
    if (keys.values.any { Input.isKeyDown(it) }) {
        System.err.println("[DEBUG] pollGlfwInput: gameplay key down=${keys.filterValues { Input.isKeyDown(it) }}")
    }
}

private fun framebufferScale(window: Long): Pair<Float, Float> {
    val windowWidth = VulkanWindow.glfwGetWindowWidth(window)
    val windowHeight = VulkanWindow.glfwGetWindowHeight(window)
    val framebufferWidth = VulkanWindow.glfwGetFramebufferWidth(window)
    val framebufferHeight = VulkanWindow.glfwGetFramebufferHeight(window)
    val scaleX = if (windowWidth != 0) framebufferWidth.toFloat() / windowWidth else 1f
    val scaleY = if (windowHeight != 0) framebufferHeight.toFloat() / windowHeight else 1f
    return scaleX to scaleY
}
