// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.application

import io.github.ronjunevaldoz.awake.ui.context.UiCursor
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanWindow

// GLFW's standard cursor shape constants (glfw3.h) -- desktop-only, mirrors GLFW_PRESS's own
// "just the raw int this project's Kotlin side needs" style in GlfwWindowInput.kt. Old-style
// names (matching this project's minimum GLFW version); GLFW 3.4 added synonyms
// (GLFW_RESIZE_EW_CURSOR etc.) this project doesn't need.
internal const val GLFW_ARROW_CURSOR = 0x00036001
internal const val GLFW_HAND_CURSOR = 0x00036004
internal const val GLFW_HRESIZE_CURSOR = 0x00036005
internal const val GLFW_VRESIZE_CURSOR = 0x00036006
internal const val GLFW_IBEAM_CURSOR = 0x00036002

/** Maps [cursor] to the GLFW standard-cursor shape constant [VulkanWindow.glfwSetCursorShape]
 * expects -- pure, so it's tested without a live window (see that function's own doc comment
 * for why the actual GLFW call isn't). */
internal fun UiCursor.toGlfwCursorShape(): Int = when (this) {
    UiCursor.Default -> GLFW_ARROW_CURSOR
    UiCursor.ResizeHorizontal -> GLFW_HRESIZE_CURSOR
    UiCursor.ResizeVertical -> GLFW_VRESIZE_CURSOR
    UiCursor.Pointer -> GLFW_HAND_CURSOR
    UiCursor.Text -> GLFW_IBEAM_CURSOR
}

/** The desktop cursor application point: forwards [cursor] to GLFW for [window] via the
 * cached-standard-cursor native call (see [VulkanWindow.glfwSetCursorShape]'s doc comment for
 * the native-side caching). Called once per frame from the desktop game loop -- see
 * `runVulkanDesktopGame`'s `cursor` parameter in `VulkanDesktopHost.kt`. */
internal fun applyUiCursor(window: Long, cursor: UiCursor) {
    VulkanWindow.glfwSetCursorShape(window, cursor.toGlfwCursorShape())
}
