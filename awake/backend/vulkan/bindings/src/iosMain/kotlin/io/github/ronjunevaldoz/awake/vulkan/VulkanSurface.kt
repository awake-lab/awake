// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan

import io.github.ronjunevaldoz.awake.vulkan.models.VkExtent2D
import kotlinx.cinterop.CPointed
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.interpretCPointer
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.objcPtr
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toCPointer
import kotlinx.cinterop.useContents
import kotlinx.cinterop.value
import platform.MoltenVK.VK_STRUCTURE_TYPE_METAL_SURFACE_CREATE_INFO_EXT
import platform.MoltenVK.VK_SUCCESS
import platform.MoltenVK.VkMetalSurfaceCreateInfoEXT
import platform.MoltenVK.VkSurfaceKHRVar
import platform.MoltenVK.vkCreateMetalSurfaceEXT
import platform.QuartzCore.CAMetalLayer

// iOS surface creation is CAMetalLayer-backed via MoltenVK's VK_EXT_metal_surface. `window` must
// be a real CAMetalLayer; pLayer is an opaque `const void*` in the cinterop-parsed C header, so
// the ObjC layer is passed via .objcPtr() rather than a generated wrapper type.
@OptIn(ExperimentalForeignApi::class)
actual fun createSurface(instance: Long, window: Any): Long = memScoped {
    val metalLayer = window as CAMetalLayer
    val nativeCreateInfo = alloc<VkMetalSurfaceCreateInfoEXT>().apply {
        sType = VK_STRUCTURE_TYPE_METAL_SURFACE_CREATE_INFO_EXT
        pNext = null
        flags = 0u
        pLayer = interpretCPointer<CPointed>(metalLayer.objcPtr())
    }
    val surfaceVar = alloc<VkSurfaceKHRVar>()
    val result = vkCreateMetalSurfaceEXT(instance.toCPointer(), nativeCreateInfo.ptr, null, surfaceVar.ptr)
    check(result == VK_SUCCESS) { "vkCreateMetalSurfaceEXT failed: $result" }
    surfaceVar.value!!.rawValue.toLong()
}

@OptIn(ExperimentalForeignApi::class)
actual fun surfaceFramebufferExtent(window: Any): VkExtent2D? {
    val metalLayer = window as CAMetalLayer
    return metalLayer.drawableSize.useContents {
        VkExtent2D(width = width.toInt(), height = height.toInt())
    }
}

actual fun destroySurfaceWindow(window: Any) {
    // The CAMetalLayer/UIView's lifecycle is owned by UIKit (VulkanMetalView), same as
    // Android's own Surface -- nothing to tear down here beyond the VkSurfaceKHR itself,
    // which the caller destroys separately via vkDestroySurfaceKHR.
}
