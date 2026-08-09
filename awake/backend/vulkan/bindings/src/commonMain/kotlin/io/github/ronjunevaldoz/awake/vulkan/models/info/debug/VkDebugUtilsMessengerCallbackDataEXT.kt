// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.models.info.debug

import io.github.ronjunevaldoz.awake.vulkan.VkArray
import io.github.ronjunevaldoz.awake.vulkan.VkFlags
import io.github.ronjunevaldoz.awake.vulkan.VkMutator
import io.github.ronjunevaldoz.awake.vulkan.enums.VkStructureType
import kotlin.jvm.JvmOverloads

@VkMutator
class VkDebugUtilsMessengerCallbackDataEXT @JvmOverloads constructor(
    val sType: VkStructureType = VkStructureType.VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CALLBACK_DATA_EXT,
    val pNext: Any? = null,
    val flags: VkDebugUtilsMessengerCallbackDataFlagsEXT = 0,
    val pMessageIdName: String? = "",
    val messageIdNumber: Int = 0,
    val pMessage: String = "",
    @VkArray(sizeAlias = "queueLabelCount")
    val pQueueLabels: Array<VkDebugUtilsLabelEXT>? = null,
    @VkArray(sizeAlias = "cmdBufLabelCount")
    val pCmdBufLabels: Array<VkDebugUtilsLabelEXT>? = null,
    @VkArray(sizeAlias = "objectCount")
    val pObjects: Array<VkDebugUtilsObjectNameInfoEXT>? = null,
)

typealias VkDebugUtilsMessengerCallbackDataFlagsEXT = VkFlags
