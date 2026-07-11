// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan

import io.github.ronjunevaldoz.awake.vulkan.models.info.VkInstanceCreateInfo
import kotlin.test.Test
import kotlin.test.assertNotEquals

/**
 * First real hardware/simulator verification for Phase 6 (MoltenVK cinterop) -- everything
 * before this was compile-time-verified only. Creates and destroys a real Vulkan instance
 * against the vendored MoltenVK.xcframework (awake-vulkan/ios-native/MoltenVK) running on
 * an iOS Simulator process, proving the whole chain actually links and runs, not just
 * compiles: cinterop-generated bindings -> MoltenVK.framework -> Metal.
 */
class VulkanMoltenVkSmokeTest {
    @Test
    fun vkCreateInstanceSucceedsAgainstRealMoltenVk() {
        val instance = Vulkan.vkCreateInstance(VkInstanceCreateInfo())
        assertNotEquals(0L, instance, "vkCreateInstance returned a null handle")
        Vulkan.vkDestroyInstance(instance)
    }
}
