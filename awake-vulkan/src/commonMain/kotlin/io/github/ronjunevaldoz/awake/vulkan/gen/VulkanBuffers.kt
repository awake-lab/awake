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

package io.github.ronjunevaldoz.awake.vulkan.gen

import io.github.ronjunevaldoz.awake.vulkan.models.info.VkBufferCreateInfo

/**
 * Phase 1d Vulkan API surface generated via jni-binding-generator (vendored in
 * tools/jni-binding-generator), not the legacy awake-vulkan-generator that backs
 * io.github.ronjunevaldoz.awake.vulkan.Vulkan. Kept in a separate package/object
 * deliberately: the generateJniBindings Gradle task uses `--package-filter` scoped to
 * this package, so it never touches the legacy Vulkan.kt (which has several return/param
 * shapes jni-binding-generator doesn't support as *function-level* types yet, e.g.
 * `Array<VkLayerProperties>` as a return type — only as a struct *field*, which is what
 * Phase 1a's D10 fix actually added). See docs/decisions/D10-codegen-derisk-findings.md.
 */
expect object VulkanBuffers {
    fun vkCreateBuffer(device: Long, createInfo: VkBufferCreateInfo): Long
    fun vkDestroyBuffer(device: Long, buffer: Long)
}
