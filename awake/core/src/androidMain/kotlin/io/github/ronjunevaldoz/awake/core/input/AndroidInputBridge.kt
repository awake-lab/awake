// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.input

import android.view.MotionEvent

fun MotionEvent.syncAwakePointerInput(input: Input): Boolean {
    // Only a real mouse reports a button state -- finger touches always leave it 0, so this is
    // a no-op for touch. Long-press is deliberately NOT mapped to a secondary click here: that
    // is a gesture with its own timing/slop rules, not a device signal this bridge can read.
    val secondary = buttonState and MotionEvent.BUTTON_SECONDARY != 0
    when (actionMasked) {
        MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
            input.setSecondaryPointer(secondary)
            // A right-click must not also read as a primary press, or it would activate
            // whatever widget it opened the context menu over.
            input.setPointer(down = !secondary, x = x, y = y)
            return true
        }
        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
            input.setSecondaryPointer(false)
            input.setPointer(down = false, x = x, y = y)
            return true
        }
    }
    return false
}
