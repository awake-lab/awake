// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.utils

import io.github.ronjunevaldoz.awake.vulkan.enums.VkResult

data class VkResultException(override val message: String, val result: VkResult) :
    RuntimeException()