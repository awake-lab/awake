// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan

import io.github.ronjunevaldoz.awake.vulkan.models.info.VkAndroidSurfaceCreateInfoKHR

actual fun createSurface(instance: Long, window: Any): Long {
    val surfaceInfo = VkAndroidSurfaceCreateInfoKHR(window = window)
    return Vulkan.vkCreateAndroidSurfaceKHR(instance, surfaceInfo)
}

actual fun destroySurfaceWindow(window: Any) {
    // Android owns its own Surface/window lifecycle -- nothing to tear down here.
}
