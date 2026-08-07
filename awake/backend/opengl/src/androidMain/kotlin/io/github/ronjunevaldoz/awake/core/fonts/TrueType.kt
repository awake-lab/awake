// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.fonts

import io.github.ronjunevaldoz.awake.core.rendering.Texture

class NativeTrueType(filePath: String, fontSize: Float) : TrueType {
    override val texture: Texture

    override fun drawText(text: String) {
        TODO("Not yet implemented")
    }

    override fun dispose() {
        texture.delete()
    }

    init {
        require(filePath.isNotBlank()) { "Font path must not be blank" }
        val fontBitmap = FontBitmap(fontSize, true)
        texture = fontBitmap.texture
    }
}

actual fun createTrueType(path: String, size: Float): TrueType = NativeTrueType(path, size)
