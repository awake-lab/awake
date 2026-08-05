// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.utils

import io.github.ronjunevaldoz.awake.core.graphics.Bitmap
import io.github.ronjunevaldoz.awake.core.graphics.createBitmap

object BitmapUtils {
    fun decode(bytes: ByteArray): Bitmap = createBitmap(bytes)
}
