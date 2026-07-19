// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.application

/** Fake [GlfwWindowInput] for desktopTest -- lets a test set "these keys are down this frame"
 * and assert the resulting [io.github.ronjunevaldoz.awake.core.input.Input] calls, without a
 * live GLFW window. */
class FakeGlfwWindowInput : GlfwWindowInput {
    val keysDown = mutableSetOf<Int>()
    var mouseDown = false
    var cursorXValue = 0.0
    var cursorYValue = 0.0
    var scaleX = 1f
    var scaleY = 1f
    var pendingScrollDeltaY = 0.0

    override fun isKeyDown(glfwKey: Int): Boolean = glfwKey in keysDown
    override fun isMouseButtonDown(glfwButton: Int): Boolean = mouseDown
    override fun cursorX(): Double = cursorXValue
    override fun cursorY(): Double = cursorYValue
    override fun framebufferScaleX(): Float = scaleX
    override fun framebufferScaleY(): Float = scaleY

    override fun consumeScrollDeltaY(): Double {
        val value = pendingScrollDeltaY
        pendingScrollDeltaY = 0.0
        return value
    }
}
