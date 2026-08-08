// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
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

    /** Brings [window] to the front and gives it OS input focus (`glfwFocusWindow`). Needed
     * on top of default focus-on-show behavior for a bare JVM process with no macOS app
     * bundle/`NSApplication` activation, which can leave the window frontmost but not the
     * OS-focused one -- keyboard/scroll input silently fails to reach it in that state even
     * though mouse clicks still route correctly (mouse events go to whatever window is under
     * the cursor regardless of app-level focus). */
    fun glfwFocusWindow(window: Long)
    fun glfwWindowShouldClose(window: Long): Boolean
    fun glfwPollEvents()
    fun glfwGetFramebufferWidth(window: Long): Int
    fun glfwGetFramebufferHeight(window: Long): Int

    /** Window size in screen/logical coordinates (`glfwGetWindowSize`) -- NOT the same as
     * [glfwGetFramebufferWidth]/[glfwGetFramebufferHeight] on a Retina/HiDPI display, where
     * the framebuffer is a device-pixel-ratio multiple of this. Needed to scale raw
     * `glfwGetCursorPos` screen coordinates (also logical points) up to the framebuffer-pixel
     * space [io.github.ronjunevaldoz.awake.core.input.Input.pointerX]/[pointerY] and
     * `UiContext.hitTest` are defined in. */
    fun glfwGetWindowWidth(window: Long): Int
    fun glfwGetWindowHeight(window: Long): Int

    /** Real `VkSurfaceKHR` handle for the given GLFW window on the given `VkInstance` --
     * the desktop equivalent of `Vulkan.vkCreateAndroidSurfaceKHR`. */
    fun glfwCreateWindowSurface(instance: Long, window: Long): Long

    /** Instance extensions the platform's Vulkan surface support requires (e.g.
     * `VK_KHR_surface` + `VK_EXT_metal_surface` on macOS/MoltenVK) -- must be passed into
     * `VkInstanceCreateInfo.ppEnabledExtensionNames` *before* calling
     * [glfwCreateWindowSurface], or it fails with `VK_ERROR_INITIALIZATION_FAILED`. */
    fun glfwGetRequiredInstanceExtensions(): Array<String>

    /** `GLFW_PRESS` (1) if `key` is currently held on `window`, `GLFW_RELEASE` (0) otherwise.
     * Polled once per frame rather than callback-based, since `glfwPollEvents()` already runs
     * on the single render thread every frame. */
    fun glfwGetKey(window: Long, key: Int): Int

    /** Same polling contract as [glfwGetKey], for a GLFW mouse button code. */
    fun glfwGetMouseButton(window: Long, button: Int): Int

    /** Cursor position in screen coordinates as `[x, y]`. */
    fun glfwGetCursorPos(window: Long): DoubleArray

    /** Registers GLFW's scroll callback (`glfwSetScrollCallback`) for [window], which
     * accumulates every `yoffset` tick (trackpad pinch on macOS surfaces through this exact
     * callback, with different-feeling deltas than a mouse wheel but the same callback/API)
     * into a native accumulator -- see [glfwConsumeScrollDeltaY]. Must be called once, after
     * [glfwCreateWindow] succeeds and before the first [glfwPollEvents] of the main loop; the
     * callback itself fires synchronously inside `glfwPollEvents()`, same thread, so no
     * further synchronization is needed once wired (see `docs/architecture.md`'s
     * threading-model rules). This does NOT fit [glfwGetKey]'s polled-getter pattern -- GLFW
     * scroll input is push/callback-based, not a simple state query. */
    fun glfwSetScrollCallback(window: Long)

    /** Polled getter, same per-frame-poll contract as [glfwGetKey]/[glfwGetCursorPos]:
     * returns the native scroll accumulator [glfwSetScrollCallback] feeds and resets it to
     * `0.0`, so a caller polling this once per frame gets exactly the ticks that happened
     * since its last poll (no double-counting, no dropped ticks between polls). */
    fun glfwConsumeScrollDeltaY(window: Long): Double
}
