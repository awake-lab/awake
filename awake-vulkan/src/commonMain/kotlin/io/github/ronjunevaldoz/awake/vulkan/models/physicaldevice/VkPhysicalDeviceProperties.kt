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

package io.github.ronjunevaldoz.awake.vulkan.models.physicaldevice

import io.github.ronjunevaldoz.awake.vulkan.VK_MAX_PHYSICAL_DEVICE_NAME_SIZE
import io.github.ronjunevaldoz.awake.vulkan.VK_UUID_SIZE
import io.github.ronjunevaldoz.awake.vulkan.VkConstArray
import io.github.ronjunevaldoz.awake.vulkan.VkMutator
import io.github.ronjunevaldoz.awake.vulkan.enums.VkPhysicalDeviceType
import kotlin.jvm.JvmOverloads

@VkMutator
class VkPhysicalDeviceProperties @JvmOverloads constructor(
    val apiVersion: Int = 0,
    val driverVersion: Int = 0,
    val vendorID: Int = 0,
    val deviceID: Int = 0,
    val deviceType: VkPhysicalDeviceType = VkPhysicalDeviceType.VK_PHYSICAL_DEVICE_TYPE_OTHER,
    @VkConstArray("VK_MAX_PHYSICAL_DEVICE_NAME_SIZE")
    val deviceName: CharArray = CharArray(VK_MAX_PHYSICAL_DEVICE_NAME_SIZE),
    @VkConstArray("VK_UUID_SIZE")
    val pipelineCacheUUID: ByteArray = ByteArray(VK_UUID_SIZE),
    val limits: VkPhysicalDeviceLimits = VkPhysicalDeviceLimits(),
    val sparseProperties: VkPhysicalDeviceSparseProperties = VkPhysicalDeviceSparseProperties()
)