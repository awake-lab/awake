// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.utils

import io.github.ronjunevaldoz.awake.core.graphics.Bitmap
import io.github.ronjunevaldoz.awake.core.graphics.createBitmap

object BitmapUtils {
    // suspend since web decoding goes through createImageBitmap(), which is async.
    suspend fun decode(bytes: ByteArray): Bitmap = createBitmap(bytes)
}
