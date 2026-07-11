// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan

import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanWindow

actual fun createSurface(instance: Long, window: Any): Long =
    VulkanWindow.glfwCreateWindowSurface(instance, window as Long)

actual fun destroySurfaceWindow(window: Any) {
    val handle = window as Long
    VulkanWindow.glfwDestroyWindow(handle)
    VulkanWindow.glfwTerminate()
}
