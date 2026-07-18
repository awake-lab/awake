// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.input.TextEditAction
import platform.UIKit.UIView

/**
 * Wires [VulkanMetalView]'s `UIKeyInput` conformance (declared on the view itself, see its
 * class doc) to [Input] -- `UIKeyInput` rather than the full `UITextInput` protocol
 * deliberately: no selection ranges/marked text/autocomplete UI needed for a v1 that only
 * needs typed text + Backspace + Enter (see the task that added this file).
 */
fun UIView.syncAwakeTextInsert(text: String) {
    if (text == "\n") {
        Input.pushEditAction(TextEditAction.Enter)
    } else {
        Input.pushTypedText(text)
    }
}

fun UIView.syncAwakeTextDeleteBackward() {
    Input.pushEditAction(TextEditAction.Backspace)
}

/**
 * Polls [Input.textInputFocused] and calls `becomeFirstResponder`/`resignFirstResponder` on
 * the rising/falling edge only -- [wasFocused] is the caller's own previous-value slot (see
 * [VulkanMetalView]) so repeated calls while focus state is unchanged don't spam UIKit with
 * redundant first-responder churn every frame.
 */
fun UIView.syncAwakeTextInputFocus(wasFocused: Boolean): Boolean {
    val isFocused = Input.textInputFocused
    if (isFocused != wasFocused) {
        if (isFocused) becomeFirstResponder() else resignFirstResponder()
    }
    return isFocused
}
