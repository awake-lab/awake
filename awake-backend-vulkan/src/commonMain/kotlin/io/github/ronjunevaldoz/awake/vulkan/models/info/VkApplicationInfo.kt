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

package io.github.ronjunevaldoz.awake.vulkan.models.info

import io.github.ronjunevaldoz.awake.vulkan.Version
import io.github.ronjunevaldoz.awake.vulkan.Version.Companion.vkVersion
import io.github.ronjunevaldoz.awake.vulkan.enums.VkStructureType

data class VkApplicationInfo(
    var sType: VkStructureType = VkStructureType.VK_STRUCTURE_TYPE_APPLICATION_INFO,
    var pNext: Any? = null,
    var pApplicationName: String,
    var applicationVersion: Int = Version(1, 0, 0).vkVersion,
    var pEngineName: String,
    var engineVersion: Int = Version(1, 0, 0).vkVersion,
    var apiVersion: Int = Version(1, 0, 0).vkVersion
)