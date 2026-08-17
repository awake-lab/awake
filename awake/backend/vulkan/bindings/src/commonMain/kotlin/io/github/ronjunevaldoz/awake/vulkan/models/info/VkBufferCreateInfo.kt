// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.models.info

import io.github.ronjunevaldoz.awake.vulkan.VkDeviceSize
import io.github.ronjunevaldoz.awake.vulkan.VkFlags
import io.github.ronjunevaldoz.awake.vulkan.enums.VkSharingMode

/**
 * Marshalled by jni-binding-generator (not the legacy awake-vulkan-generator), so unlike
 * other *CreateInfo classes this deliberately omits `sType`/`pNext`: they are a compile-time
 * constant per struct type and are set directly in the hand-written native body instead of
 * being passed across JNI. This also sidesteps a real hazard: jni-binding-generator marshals
 * enum fields via ordinal position, and `VkStructureType`'s ordinal only matches its real
 * Vulkan value up to entry 48 (extension types jump to values like 1000094000) — see the
 * Phase 1d note in docs/MVP_PLAN.md. `sharingMode` is safe here because `VkSharingMode` has
 * exactly two entries whose ordinal matches its value (0/1) and the Vulkan spec has never
 * extended it.
 */
class VkBufferCreateInfo(
    val size: VkDeviceSize,
    val usage: VkBufferUsageFlags,
    val flags: VkBufferCreateFlags = 0,
    val sharingMode: VkSharingMode = VkSharingMode.VK_SHARING_MODE_EXCLUSIVE,
)

typealias VkBufferCreateFlags = VkFlags
typealias VkBufferUsageFlags = VkFlags

object VkBufferUsageFlagBits {
    const val VK_BUFFER_USAGE_TRANSFER_SRC_BIT = 0x00000001
    const val VK_BUFFER_USAGE_TRANSFER_DST_BIT = 0x00000002
    const val VK_BUFFER_USAGE_VERTEX_BUFFER_BIT = 0x00000080
    const val VK_BUFFER_USAGE_INDEX_BUFFER_BIT = 0x00000040
    const val VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT = 0x00000010
    const val VK_BUFFER_USAGE_STORAGE_BUFFER_BIT = 0x00000020
}
