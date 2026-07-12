// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.enums

import io.github.ronjunevaldoz.awake.vulkan.VkFlags

interface VkEnum {
    val value: Int
}

infix fun VkEnum.or(other: Int): Int {
    return this.value or other
}

infix fun VkEnum.or(other: VkEnum): Int {
    return this.value or other.value
}

infix fun VkEnum.has(bit: VkEnum): Boolean {
    return this.value and bit.value != 0
}

infix fun VkEnum.and(other: VkEnum): Int {
    return this.value and other.value
}

infix fun VkEnum.set(bit: VkEnum): VkFlags {
    return this.value or bit.value
}

infix fun VkEnum.clear(bit: VkSampleCountFlagBits): VkFlags {
    return this.value and bit.value.inv()
}
