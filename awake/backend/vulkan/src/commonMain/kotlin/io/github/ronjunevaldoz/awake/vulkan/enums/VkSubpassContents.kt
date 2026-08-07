// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.enums

enum class VkSubpassContents(override val value: Int) : VkEnum {
    VK_SUBPASS_CONTENTS_INLINE(0),
    VK_SUBPASS_CONTENTS_SECONDARY_COMMAND_BUFFERS(1),
    VK_SUBPASS_CONTENTS_MAX_ENUM(0x7FFFFFFF),
}
