// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.models.info.debug

import io.github.ronjunevaldoz.awake.vulkan.VkBool32
import io.github.ronjunevaldoz.awake.vulkan.VkFlags
import io.github.ronjunevaldoz.awake.vulkan.enums.VkStructureType
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkDebugUtilsMessageSeverityFlagBitsEXT
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkDebugUtilsMessageSeverityFlagsEXT
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkDebugUtilsMessageTypeFlagBitsEXT
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkDebugUtilsMessageTypeFlagsEXT
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.formatted


typealias PFN_vkDebugUtilsMessengerCallbackEXT = (
    messageSeverity: VkDebugUtilsMessageSeverityFlagBitsEXT,
    messageTypes: VkDebugUtilsMessageTypeFlagsEXT,
    pCallbackData: VkDebugUtilsMessengerCallbackDataEXT,
    pUserData: Any?
) -> VkBool32

data class VkDebugUtilsMessengerCreateInfoEXT(
    val sType: VkStructureType = VkStructureType.VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT,
    val pNext: Any? = null,
    val flags: VkDebugUtilsMessengerCreateFlagsEXT = 0,
    val messageSeverity: VkDebugUtilsMessageSeverityFlagsEXT =
        VkDebugUtilsMessageSeverityFlagBitsEXT.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT.value or
                VkDebugUtilsMessageSeverityFlagBitsEXT.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT.value,
    val messageType: VkDebugUtilsMessageTypeFlagsEXT =
        VkDebugUtilsMessageTypeFlagBitsEXT.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT.value or
                VkDebugUtilsMessageTypeFlagBitsEXT.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT.value or
                VkDebugUtilsMessageTypeFlagBitsEXT.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT.value,
    val pfnUserCallback: PFN_vkDebugUtilsMessengerCallbackEXT = { _, _, _, _ -> false },
    val pUserData: Any? = null
)

typealias VkDebugUtilsMessengerCreateFlagsEXT = VkFlags

typealias LogCallback = (String, String) -> Unit

val DebugUtilsFormattedCallback: (LogCallback) -> PFN_vkDebugUtilsMessengerCallbackEXT =
    { logCallback ->
        { severity, messageType, callbackData, userData ->
            val severityString = severity.formatted
            val types = messageType.formatted
            val messageIdName = callbackData.pMessageIdName
            val messageIdNumber = callbackData.messageIdNumber
            val message = callbackData.pMessage

            val formattedMessage = message.split(",").joinToString("\n") {
                it.trim().split("|").joinToString("\n") { it.trim() }
            }

            val logMessage = buildString {
                appendLine("$types $severityString:")
                appendLine("[$messageIdName] Code $messageIdNumber:")
                appendLine(formattedMessage)
            }

            logCallback(severityString, logMessage)
            false
        }
    }
