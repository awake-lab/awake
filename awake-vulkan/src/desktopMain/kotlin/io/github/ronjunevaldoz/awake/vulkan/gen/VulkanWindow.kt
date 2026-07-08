/*
 * Awake
 * Awake.awake-vulkan.desktopMain
 *
 * Copyright (c) ronjunevaldoz 2023.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
}
