// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan

import io.github.ronjunevaldoz.awake.vulkan.models.VkExtent2D

/**
 * Creates a `VkSurfaceKHR` for the given Vulkan [instance] and platform-native [window] handle
 * -- an `android.view.Surface` on Android, or a GLFW window handle (`Long`) on desktop. There's
 * no common supertype between the two (hence `Any`, cast internally by each actual).
 */
expect fun createSurface(instance: Long, window: Any): Long

/**
 * Returns the current drawable size, in framebuffer pixels, for platform surfaces whose
 * Vulkan capabilities report a variable extent.
 */
expect fun surfaceFramebufferExtent(window: Any): VkExtent2D?

/**
 * Tears down whatever platform-native window resources [window] represents, if any.
 * A no-op on Android, which owns its own `Surface`/window lifecycle; on desktop this
 * destroys the GLFW window and terminates GLFW.
 */
expect fun destroySurfaceWindow(window: Any)
