// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.gen

// Android owns its window via android.view.Surface/SurfaceView, not GLFW -- see
// Vulkan.vkCreateAndroidSurfaceKHR. This object is desktop-only; see VulkanWindow.kt's
// doc comment.
actual object VulkanWindow {
    actual fun glfwInit(): Boolean {
        TODO("Not applicable on Android -- see VulkanWindow.kt's doc comment.")
    }

    actual fun glfwTerminate() {
        TODO("Not applicable on Android -- see VulkanWindow.kt's doc comment.")
    }

    actual fun glfwWindowHint(hint: Int, value: Int) {
        TODO("Not applicable on Android -- see VulkanWindow.kt's doc comment.")
    }

    actual fun glfwCreateWindow(width: Int, height: Int, title: String): Long {
        TODO("Not applicable on Android -- see VulkanWindow.kt's doc comment.")
    }

    actual fun glfwDestroyWindow(window: Long) {
        TODO("Not applicable on Android -- see VulkanWindow.kt's doc comment.")
    }

    actual fun glfwFocusWindow(window: Long) {
        TODO("Not applicable on Android -- see VulkanWindow.kt's doc comment.")
    }

    actual fun glfwWindowShouldClose(window: Long): Boolean {
        TODO("Not applicable on Android -- see VulkanWindow.kt's doc comment.")
    }

    actual fun glfwPollEvents() {
        TODO("Not applicable on Android -- see VulkanWindow.kt's doc comment.")
    }

    actual fun glfwGetFramebufferWidth(window: Long): Int {
        TODO("Not applicable on Android -- see VulkanWindow.kt's doc comment.")
    }

    actual fun glfwGetFramebufferHeight(window: Long): Int {
        TODO("Not applicable on Android -- see VulkanWindow.kt's doc comment.")
    }

    actual fun glfwGetWindowWidth(window: Long): Int {
        TODO("Not applicable on Android -- see VulkanWindow.kt's doc comment.")
    }

    actual fun glfwGetWindowHeight(window: Long): Int {
        TODO("Not applicable on Android -- see VulkanWindow.kt's doc comment.")
    }

    actual fun glfwCreateWindowSurface(instance: Long, window: Long): Long {
        TODO("Not applicable on Android -- see VulkanWindow.kt's doc comment.")
    }

    // Safe no-op unlike the TODOs above: "no GLFW-required extensions" holds on any
    // non-GLFW platform, so cross-platform instance-creation code can call it unconditionally.
    actual fun glfwGetRequiredInstanceExtensions(): Array<String> = emptyArray()

    actual fun glfwGetKey(window: Long, key: Int): Int {
        TODO("Not applicable on Android -- see VulkanWindow.kt's doc comment.")
    }

    actual fun glfwGetMouseButton(window: Long, button: Int): Int {
        TODO("Not applicable on Android -- see VulkanWindow.kt's doc comment.")
    }

    actual fun glfwGetCursorPos(window: Long): DoubleArray {
        TODO("Not applicable on Android -- see VulkanWindow.kt's doc comment.")
    }

    actual fun glfwSetScrollCallback(window: Long) {
        TODO("Not applicable on Android -- see VulkanWindow.kt's doc comment.")
    }

    actual fun glfwConsumeScrollDeltaY(window: Long): Double {
        TODO("Not applicable on Android -- see VulkanWindow.kt's doc comment.")
    }

    // No-op rather than an unimplemented stub: Android has no GLFW cursor to set, and unlike
    // the unimplemented functions above (queried every frame, so a stub would immediately
    // crash a real run), a caller polling UiFrameOutput.effects.cursor and forwarding it here
    // should be able to do so unconditionally across platforms.
    actual fun glfwSetCursorShape(window: Long, shape: Int) = Unit
}
