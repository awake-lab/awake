/*
 * Awake
 * Awake.awake-vulkan.commonMain
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

package io.github.ronjunevaldoz.awake.vulkan.models.info.debug

import io.github.ronjunevaldoz.awake.vulkan.VkArray
import io.github.ronjunevaldoz.awake.vulkan.VkFlags
import io.github.ronjunevaldoz.awake.vulkan.VkMutator
import io.github.ronjunevaldoz.awake.vulkan.enums.VkStructureType

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
    val pObjects: Array<VkDebugUtilsObjectNameInfoEXT>? = null
)

typealias VkDebugUtilsMessengerCallbackDataFlagsEXT = VkFlags