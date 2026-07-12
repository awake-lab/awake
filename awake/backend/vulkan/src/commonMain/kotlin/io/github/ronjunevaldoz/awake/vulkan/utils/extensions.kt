// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.utils

import io.github.ronjunevaldoz.awake.vulkan.Vulkan
import io.github.ronjunevaldoz.awake.vulkan.VulkanExtension


fun getAppExtProps(layerName: String? = null): List<String> {
    val extensionProperties = Vulkan.vkEnumerateInstanceExtensionProperties(layerName)
    return extensionProperties.map { it.extensionName }.toSet().toList()
}

fun getAppLayerProps(): List<String> {
    val layerProperties = Vulkan.vkEnumerateInstanceLayerProperties()
    return layerProperties.map { it.layerName }.toSet().toList()
}

/**
 * Check all possible extension from app etc..
 */
fun isAppExtSupported(vararg extensions: VulkanExtension): Boolean {
    val extensionProperties = Vulkan.vkEnumerateInstanceExtensionProperties()
    val supportedExtensions: Set<String> = extensionProperties.map { it.extensionName }.toSet()
    for (extension in extensions) {
        if (extension.extensionName in supportedExtensions) {
            return true
        }
    }
    return false
}

/**
 * Check all possible extension from device etc..
 */
fun isDeviceExtSupported(physicalDevice: Long, vararg extensions: VulkanExtension): Boolean {
    val extensionProperties = Vulkan.vkEnumerateDeviceExtensionProperties(physicalDevice)
    val supportedExtensions: Set<String> = extensionProperties.map { it.extensionName }.toSet()
    for (extension in extensions) {
        if (extension.extensionName in supportedExtensions) {
            return true
        }
    }
    return false
}