// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.models

import io.github.ronjunevaldoz.awake.vulkan.VkUnionMember

sealed class VkClearValue

/**
 * VkClearValue value = {.color {ref}}
 */
@VkUnionMember("color")
sealed class VkClearColorValue : VkClearValue() {

    @VkUnionMember("float32", true)
    class Float32(val values: FloatArray = FloatArray(4)) : VkClearColorValue() {

        init {
            require(values.size == 4) { "float32 array must have a size of 4" }
        }
    }

    @VkUnionMember("int32", true)
    class Int32(val values: IntArray = IntArray(4)) : VkClearColorValue() {
        init {
            require(values.size == 4) { "int32 array must have a size of 4" }
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    @VkUnionMember("uint32", true)
    class UInt32(val values: UIntArray = UIntArray(4)) : VkClearColorValue() {
        init {
            require(values.size == 4) { "uint32 array must have a size of 4" }
        }
    }

    companion object {
        fun rgba(r: Float, g: Float, b: Float, a: Float) = Float32().apply {
            values[0] = r
            values[1] = g
            values[2] = b
            values[3] = a
        }

        fun rgba(r: Int, g: Int, b: Int, a: Int) = Int32().apply {
            values[0] = r
            values[1] = g
            values[2] = b
            values[3] = a
        }
    }
}

/**
 * VkClearValue value = {.depthStencil {ref}}
 */
@VkUnionMember("depthStencil")
data class VkClearDepthStencilValue(
    val depth: Float,
    val stencil: Int,
) : VkClearValue()
