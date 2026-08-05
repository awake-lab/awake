// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan_generator.vulkan


inline fun <reified T : Any> generateJavaToVulkanCpp() {
    val clazz = T::class.java
    createVulkanAccessor(clazz)
    createVulkanMutator(clazz)
}
