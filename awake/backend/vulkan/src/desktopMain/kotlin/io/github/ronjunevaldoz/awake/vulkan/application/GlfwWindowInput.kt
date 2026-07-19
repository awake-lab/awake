// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.application

import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanWindow

internal const val GLFW_PRESS = 1

/**
 * Seam between [pollGlfwInput]/[pollGlfwTextInput]'s translation logic and the real GLFW
 * native bindings ([VulkanWindow]) -- exists so a desktopTest can fake "this key is down this
 * frame" and assert the right [io.github.ronjunevaldoz.awake.core.input.Input] calls fire,
 * without a live window. A real GLFW window (and therefore OS-level focus behavior) still
 * can't be faked this way -- this only covers the keycode/repeat/shift translation, not
 * whether the OS actually delivers key events to the window in the first place.
 */
interface GlfwWindowInput {
    fun isKeyDown(glfwKey: Int): Boolean
    fun isMouseButtonDown(glfwButton: Int): Boolean
    fun cursorX(): Double
    fun cursorY(): Double
    fun framebufferScaleX(): Float
    fun framebufferScaleY(): Float
    fun consumeScrollDeltaY(): Double
}

private class RealGlfwWindowInput(private val window: Long) : GlfwWindowInput {
    override fun isKeyDown(glfwKey: Int): Boolean = VulkanWindow.glfwGetKey(window, glfwKey) == GLFW_PRESS
    override fun isMouseButtonDown(glfwButton: Int): Boolean = VulkanWindow.glfwGetMouseButton(window, glfwButton) == GLFW_PRESS
    override fun cursorX(): Double = VulkanWindow.glfwGetCursorPos(window)[0]
    override fun cursorY(): Double = VulkanWindow.glfwGetCursorPos(window)[1]
    override fun consumeScrollDeltaY(): Double = VulkanWindow.glfwConsumeScrollDeltaY(window)

    override fun framebufferScaleX(): Float = framebufferScale().first
    override fun framebufferScaleY(): Float = framebufferScale().second

    private fun framebufferScale(): Pair<Float, Float> {
        val windowWidth = VulkanWindow.glfwGetWindowWidth(window)
        val windowHeight = VulkanWindow.glfwGetWindowHeight(window)
        val framebufferWidth = VulkanWindow.glfwGetFramebufferWidth(window)
        val framebufferHeight = VulkanWindow.glfwGetFramebufferHeight(window)
        val scaleX = if (windowWidth != 0) framebufferWidth.toFloat() / windowWidth else 1f
        val scaleY = if (windowHeight != 0) framebufferHeight.toFloat() / windowHeight else 1f
        return scaleX to scaleY
    }
}

fun glfwWindowInput(window: Long): GlfwWindowInput = RealGlfwWindowInput(window)
