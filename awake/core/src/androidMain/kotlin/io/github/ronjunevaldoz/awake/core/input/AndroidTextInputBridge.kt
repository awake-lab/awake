// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.input

import android.content.Context
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager

/** [InputConnection] fed to the IME by [io.github.ronjunevaldoz.awake.core.graphics.VulkanView]
 * .onCreateInputConnection -- a `SurfaceView` has no text field of its own for the IME to edit,
 * so this forwards committed text/deletes straight into [input] instead of maintaining an
 * `Editable`. */
class AwakeInputConnection(targetView: View, private val input: Input) : BaseInputConnection(targetView, false) {

    override fun commitText(text: CharSequence, newCursorPosition: Int): Boolean {
        input.pushTypedText(text.toString())
        return true
    }

    override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
        repeat(beforeLength) { input.pushEditAction(TextEditAction.Backspace) }
        return true
    }

    override fun sendKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_ENTER -> input.pushEditAction(TextEditAction.Enter)
                KeyEvent.KEYCODE_DEL -> input.pushEditAction(TextEditAction.Backspace)
            }
        }
        return super.sendKeyEvent(event)
    }
}

fun View.createAwakeInputConnection(outAttrs: EditorInfo, input: Input): InputConnection {
    outAttrs.inputType = android.text.InputType.TYPE_CLASS_TEXT
    outAttrs.imeOptions = EditorInfo.IME_ACTION_DONE
    return AwakeInputConnection(this, input)
}

/** Polls [Input.textInputFocused] once per frame and shows/hides the soft keyboard on its
 * rising/falling edge. */
class AndroidSoftKeyboardBridge(private val view: View, private val input: Input) {
    private var wasFocused = false

    fun syncSoftKeyboardVisibility() {
        val focused = input.textInputFocused
        if (focused == wasFocused) return
        wasFocused = focused
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (focused) {
            view.requestFocus()
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        } else {
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}
