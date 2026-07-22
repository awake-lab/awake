// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.input

import android.view.KeyEvent

val DefaultAndroidGameplayKeys: Map<Int, Key> = linkedMapOf(
    KeyEvent.KEYCODE_W to Key.W,
    KeyEvent.KEYCODE_A to Key.A,
    KeyEvent.KEYCODE_S to Key.S,
    KeyEvent.KEYCODE_D to Key.D,
    KeyEvent.KEYCODE_DPAD_UP to Key.ArrowUp,
    KeyEvent.KEYCODE_DPAD_DOWN to Key.ArrowDown,
    KeyEvent.KEYCODE_DPAD_LEFT to Key.ArrowLeft,
    KeyEvent.KEYCODE_DPAD_RIGHT to Key.ArrowRight,
    KeyEvent.KEYCODE_SPACE to Key.Space,
    KeyEvent.KEYCODE_ESCAPE to Key.Escape
)

fun KeyEvent.syncAwakeKeyInput(
    down: Boolean,
    input: Input,
    keys: Map<Int, Key> = DefaultAndroidGameplayKeys
): Boolean {
    val key = keys[keyCode] ?: return false
    input.setKeyDown(key, down)
    return true
}
