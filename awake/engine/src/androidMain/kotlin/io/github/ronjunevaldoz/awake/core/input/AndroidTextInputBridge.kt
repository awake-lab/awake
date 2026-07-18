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
 * so this forwards committed text/deletes straight into [Input] instead of maintaining an
 * `Editable`. v1: no selection/composing-region handling, `beforeLength` chars of
 * `deleteSurroundingText` each become one [TextEditAction.Backspace] pulse rather than tracking
 * cursor position precisely -- good enough for a single-line text field. */
class AwakeInputConnection(targetView: View) : BaseInputConnection(targetView, false) {

    override fun commitText(text: CharSequence, newCursorPosition: Int): Boolean {
        Input.pushTypedText(text.toString())
        return true
    }

    override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
        repeat(beforeLength) { Input.pushEditAction(TextEditAction.Backspace) }
        return true
    }

    override fun sendKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_ENTER -> Input.pushEditAction(TextEditAction.Enter)
                KeyEvent.KEYCODE_DEL -> Input.pushEditAction(TextEditAction.Backspace)
            }
        }
        return super.sendKeyEvent(event)
    }
}

fun View.createAwakeInputConnection(outAttrs: EditorInfo): InputConnection {
    outAttrs.inputType = android.text.InputType.TYPE_CLASS_TEXT
    outAttrs.imeOptions = EditorInfo.IME_ACTION_DONE
    return AwakeInputConnection(this)
}

/** Polls [Input.textInputFocused] once per frame and shows/hides the soft keyboard on its
 * rising/falling edge -- [Input] has no callback mechanism (see its own doc comment: it's a
 * polled, not callback-driven, model), so the render loop's per-frame tick is the only place
 * to notice the flip. Tracks [wasFocused] itself rather than pushing that bookkeeping onto
 * [Input] since it's purely an Android IME-visibility concern. Must run on the UI thread --
 * `showSoftInput`/`requestFocus` require it -- so callers on the render thread should post this
 * to the view instead of calling it directly. */
class AndroidSoftKeyboardBridge(private val view: View) {
    private var wasFocused = false

    fun syncSoftKeyboardVisibility() {
        val focused = Input.textInputFocused
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
