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

    actual fun glfwCreateWindowSurface(instance: Long, window: Long): Long {
        TODO("Not applicable on Android -- see VulkanWindow.kt's doc comment.")
    }

    // A genuinely safe no-op (unlike the other glfw* functions above, which are real
    // programming errors if ever called on Android): "no GLFW-required extensions" is a
    // true statement on every non-GLFW platform, letting cross-platform instance-creation
    // code call this unconditionally without a platform check -- see VulkanApplication's
    // createInstance() in awake-demo.
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
}
