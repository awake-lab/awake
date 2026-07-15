// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.input

import android.view.MotionEvent

fun MotionEvent.syncAwakePointerInput(): Boolean {
    when (actionMasked) {
        MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
            Input.setPointer(down = true, x = x, y = y)
            return true
        }
        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
            Input.setPointer(down = false, x = x, y = y)
            return true
        }
    }
    return false
}
