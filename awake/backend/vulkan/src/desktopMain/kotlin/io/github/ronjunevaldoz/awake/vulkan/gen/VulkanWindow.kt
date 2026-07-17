// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.gen

actual object VulkanWindow {
    // Kotlin objects init lazily/independently, so this can't rely on another object's
    // (e.g. Vulkan's) init block having loaded the library first -- caused a real
    // UnsatisfiedLinkError when touched before Vulkan in a test run.
    init {
        System.loadLibrary("awake-vulkan")
    }

    actual external fun glfwInit(): Boolean
    actual external fun glfwTerminate()
    actual external fun glfwWindowHint(hint: Int, value: Int)
    actual external fun glfwCreateWindow(width: Int, height: Int, title: String): Long
    actual external fun glfwDestroyWindow(window: Long)
    actual external fun glfwWindowShouldClose(window: Long): Boolean
    actual external fun glfwPollEvents()
    actual external fun glfwGetFramebufferWidth(window: Long): Int
    actual external fun glfwGetFramebufferHeight(window: Long): Int
    actual external fun glfwGetWindowWidth(window: Long): Int
    actual external fun glfwGetWindowHeight(window: Long): Int
    actual external fun glfwCreateWindowSurface(instance: Long, window: Long): Long
    actual external fun glfwGetRequiredInstanceExtensions(): Array<String>
    actual external fun glfwGetKey(window: Long, key: Int): Int
    actual external fun glfwGetMouseButton(window: Long, button: Int): Int
    actual external fun glfwGetCursorPos(window: Long): DoubleArray
    actual external fun glfwSetScrollCallback(window: Long)
    actual external fun glfwConsumeScrollDeltaY(window: Long): Double
}
