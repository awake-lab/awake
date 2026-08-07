// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan

import io.github.ronjunevaldoz.awake.core.input.Input
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.UIKit.UITouch
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
fun UIView.syncAwakePointerInput(
    touches: Set<*>,
    down: Boolean,
    input: Input,
): Boolean {
    val touch = touches.firstOrNull() as? UITouch ?: return false
    val scale = contentScaleFactor.toFloat()
    touch.locationInView(this).useContents {
        input.setPointer(down = down, x = x.toFloat() * scale, y = y.toFloat() * scale)
    }
    return true
}
