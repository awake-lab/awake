/*
 * Awake
 * Awake.awake-vulkan.iosMain
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

package io.github.ronjunevaldoz.awake.vulkan

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.CPointed
import kotlinx.cinterop.interpretCPointer
import kotlinx.cinterop.objcPtr
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toCPointer
import kotlinx.cinterop.value
import platform.MoltenVK.VK_STRUCTURE_TYPE_METAL_SURFACE_CREATE_INFO_EXT
import platform.MoltenVK.VK_SUCCESS
import platform.MoltenVK.VkMetalSurfaceCreateInfoEXT
import platform.MoltenVK.VkSurfaceKHRVar
import platform.MoltenVK.vkCreateMetalSurfaceEXT
import platform.QuartzCore.CAMetalLayer

// iOS surface creation is CAMetalLayer-backed, via MoltenVK's VK_EXT_metal_surface (enabled
// automatically by mvk_vulkan.h -- see MoltenVK.def). [window] here is expected to be a real
// platform.QuartzCore.CAMetalLayer (e.g. VulkanMetalView.metalLayer) -- the C header (parsed
// as plain C, not Objective-C, by this project's cinterop .def) types VkMetalSurfaceCreateInfoEXT's
// pLayer as an opaque `const void*`, so the real ObjC layer instance is passed via
// .objcPtr().reinterpret() rather than any cinterop-generated CAMetalLayer wrapper type.
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

actual fun destroySurfaceWindow(window: Any) {
    // The CAMetalLayer/UIView's lifecycle is owned by UIKit (VulkanMetalView), same as
    // Android's own Surface -- nothing to tear down here beyond the VkSurfaceKHR itself,
    // which the caller destroys separately via vkDestroySurfaceKHR.
}
