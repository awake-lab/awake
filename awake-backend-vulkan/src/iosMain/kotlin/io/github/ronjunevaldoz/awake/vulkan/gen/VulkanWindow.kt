// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.gen

// iOS owns its window/layer via UIKit/CAMetalLayer, not GLFW -- see VulkanWindow.kt's
// doc comment.
actual object VulkanWindow {
    actual fun glfwInit(): Boolean {
        TODO("Not applicable on iOS -- see VulkanWindow.kt's doc comment.")
    }

    actual fun glfwTerminate() {
        TODO("Not applicable on iOS -- see VulkanWindow.kt's doc comment.")
    }

    actual fun glfwWindowHint(hint: Int, value: Int) {
        TODO("Not applicable on iOS -- see VulkanWindow.kt's doc comment.")
    }

    actual fun glfwCreateWindow(width: Int, height: Int, title: String): Long {
        TODO("Not applicable on iOS -- see VulkanWindow.kt's doc comment.")
    }

    actual fun glfwDestroyWindow(window: Long) {
        TODO("Not applicable on iOS -- see VulkanWindow.kt's doc comment.")
    }

    actual fun glfwWindowShouldClose(window: Long): Boolean {
        TODO("Not applicable on iOS -- see VulkanWindow.kt's doc comment.")
    }

    actual fun glfwPollEvents() {
        TODO("Not applicable on iOS -- see VulkanWindow.kt's doc comment.")
    }

    actual fun glfwGetFramebufferWidth(window: Long): Int {
        TODO("Not applicable on iOS -- see VulkanWindow.kt's doc comment.")
    }

    actual fun glfwGetFramebufferHeight(window: Long): Int {
        TODO("Not applicable on iOS -- see VulkanWindow.kt's doc comment.")
    }

    actual fun glfwCreateWindowSurface(instance: Long, window: Long): Long {
        TODO("Not applicable on iOS -- see VulkanWindow.kt's doc comment.")
    }

    // See the Android actual's identical override for why this one is a safe no-op
    // rather than a TODO -- "no GLFW-required extensions" is true on every non-GLFW
    // platform, letting cross-platform code call this unconditionally.
    actual fun glfwGetRequiredInstanceExtensions(): Array<String> = emptyArray()

    actual fun glfwGetKey(window: Long, key: Int): Int {
        TODO("Not applicable on iOS -- see VulkanWindow.kt's doc comment.")
    }

    actual fun glfwGetMouseButton(window: Long, button: Int): Int {
        TODO("Not applicable on iOS -- see VulkanWindow.kt's doc comment.")
    }

    actual fun glfwGetCursorPos(window: Long): DoubleArray {
        TODO("Not applicable on iOS -- see VulkanWindow.kt's doc comment.")
    }
}
