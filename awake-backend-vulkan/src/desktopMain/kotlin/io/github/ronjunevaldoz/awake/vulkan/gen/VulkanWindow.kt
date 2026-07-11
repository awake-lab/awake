// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.gen

actual object VulkanWindow {
    // Every `object` with `external fun` needs the native library loaded before its first
    // call -- Kotlin objects are lazily initialized independently, so relying on some
    // *other* object's (e.g. Vulkan's) init block to have already run first is not safe
    // (surfaced as a real UnsatisfiedLinkError when this object was touched before
    // Vulkan in a test run). Same fix belongs on VulkanBuffers/VulkanDescriptors/
    // VulkanImages if they're ever exercised standalone before Vulkan; deferred since
    // every current real call site already goes through Vulkan first in practice.
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
    actual external fun glfwCreateWindowSurface(instance: Long, window: Long): Long
    actual external fun glfwGetRequiredInstanceExtensions(): Array<String>
    actual external fun glfwGetKey(window: Long, key: Int): Int
    actual external fun glfwGetMouseButton(window: Long, button: Int): Int
    actual external fun glfwGetCursorPos(window: Long): DoubleArray
}
