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

import io.github.ronjunevaldoz.awake.vulkan.VkBool32
import io.github.ronjunevaldoz.awake.vulkan.VkMutator
import kotlin.jvm.JvmOverloads

@VkMutator
data class VkPhysicalDeviceSparseProperties @JvmOverloads constructor(
    val residencyStandard2DBlockShape: VkBool32 = false,
    val residencyStandard2DMultisampleBlockShape: VkBool32 = false,
    val residencyStandard3DBlockShape: VkBool32 = false,
    val residencyAlignedMipSize: VkBool32 = false,
    val residencyNonResidentStrict: VkBool32 = false,
)