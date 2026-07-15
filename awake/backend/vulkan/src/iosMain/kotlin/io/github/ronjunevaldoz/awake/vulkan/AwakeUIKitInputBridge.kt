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
    down: Boolean
): Boolean {
    val touch = touches.firstOrNull() as? UITouch ?: return false
    touch.locationInView(this).useContents {
        Input.setPointer(down = down, x = x.toFloat(), y = y.toFloat())
    }
    return true
}
