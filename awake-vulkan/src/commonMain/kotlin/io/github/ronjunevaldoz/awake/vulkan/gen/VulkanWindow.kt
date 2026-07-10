/*
 * Awake
 * Awake.awake-vulkan.commonMain
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

/**
 * Phase 1b/1c: GLFW window + Vulkan surface creation for desktop, same jni-binding-
 * generator `.gen` package/pipeline as [VulkanBuffers]/[VulkanDescriptors]/[VulkanImages].
 * Desktop-only for now (`androidMain`/`iosMain` actuals are TODO stubs) -- Android and iOS
 * get their window handle from the platform (`Surface`/`CAMetalLayer`) instead of owning a
 * window themselves, so this object's `glfwCreateWindow`/`glfwPollEvents`/etc. are
 * meaningless there. A real cross-platform windowing abstraction is a separate, larger
 * task than this MVP needs yet (the demo app itself decides whether it owns a GLFW window
 * or receives a platform surface).
 */
expect object VulkanWindow {
    /** Must be called once before any other `glfw*` function. Returns `false` on failure. */
    fun glfwInit(): Boolean
    fun glfwTerminate()

    /** `clientApi` uses GLFW's own `GLFW_NO_API` (0x0) -- required before creating a
     * window meant for Vulkan rather than an OpenGL context. */
    fun glfwWindowHint(hint: Int, value: Int)

    fun glfwCreateWindow(width: Int, height: Int, title: String): Long
    fun glfwDestroyWindow(window: Long)
    fun glfwWindowShouldClose(window: Long): Boolean
    fun glfwPollEvents()
    fun glfwGetFramebufferWidth(window: Long): Int
    fun glfwGetFramebufferHeight(window: Long): Int

    /** Real `VkSurfaceKHR` handle for the given GLFW window on the given `VkInstance` --
     * the desktop equivalent of `Vulkan.vkCreateAndroidSurfaceKHR`. */
    fun glfwCreateWindowSurface(instance: Long, window: Long): Long

    /** Instance extensions the platform's Vulkan surface support requires (e.g.
     * `VK_KHR_surface` + `VK_EXT_metal_surface` on macOS/MoltenVK) -- must be passed into
     * `VkInstanceCreateInfo.ppEnabledExtensionNames` *before* calling
     * [glfwCreateWindowSurface], or it fails with `VK_ERROR_INITIALIZATION_FAILED`. */
    fun glfwGetRequiredInstanceExtensions(): Array<String>

    /** `GLFW_PRESS` (1) if the GLFW key code [key] is currently held, `GLFW_RELEASE` (0)
     * otherwise. Polled once per frame (see the desktop entry point) rather than
     * callback-based -- `glfwPollEvents()` already runs on the single render thread every
     * frame (see this project's `.claude/AGENTS.md` "Threading model" section), so a poll
     * there needs no callback/synchronization machinery a push-based API would. */
    fun glfwGetKey(window: Long, key: Int): Int

    /** Same polling contract as [glfwGetKey], for a GLFW mouse button code. */
    fun glfwGetMouseButton(window: Long, button: Int): Int

    /** Cursor position in screen coordinates as `[x, y]`. */
    fun glfwGetCursorPos(window: Long): DoubleArray
}
