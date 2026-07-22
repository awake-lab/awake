// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.input

import android.view.MotionEvent

fun MotionEvent.syncAwakePointerInput(input: Input): Boolean {
    when (actionMasked) {
        MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
            input.setPointer(down = true, x = x, y = y)
            return true
        }
        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
            input.setPointer(down = false, x = x, y = y)
            return true
        }
    }
    return false
}
