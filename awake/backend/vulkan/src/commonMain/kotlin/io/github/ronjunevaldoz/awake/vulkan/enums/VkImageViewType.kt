// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.enums

enum class VkImageViewType(val value: Int) {
    VK_IMAGE_VIEW_TYPE_1D(0),
    VK_IMAGE_VIEW_TYPE_2D(1),
    VK_IMAGE_VIEW_TYPE_3D(2),
    VK_IMAGE_VIEW_TYPE_CUBE(3),
    VK_IMAGE_VIEW_TYPE_1D_ARRAY(4),
    VK_IMAGE_VIEW_TYPE_2D_ARRAY(5),
    VK_IMAGE_VIEW_TYPE_CUBE_ARRAY(6),
    VK_IMAGE_VIEW_TYPE_MAX_ENUM(0x7FFFFFFF),
    ;

    companion object {
        // Helper function to convert integer value to VkImageViewType
        fun fromInt(value: Int) = values().find { it.value == value }
    }
}
