// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.texture

/**
 * Raw pixel data a game supplies to a backend's game-application bootstrap (see
 * `VulkanGameApplication`) -- backend-neutral input, not a rendering abstraction itself.
 * [data] is expected in the same tightly-packed RGBA8 layout the existing demo texture uses
 * today; a backend without real texture support (e.g. WebGPU's `Material`, still `TODO()`)
 * is free to ignore this entirely.
 */
data class TextureAsset(val data: ByteArray, val width: Int, val height: Int) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TextureAsset) return false
        return data.contentEquals(other.data) && width == other.width && height == other.height
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + width
        result = 31 * result + height
        return result
    }
}
