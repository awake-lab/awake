// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.input.InputSnapshot
import io.github.ronjunevaldoz.awake.core.input.TextEditAction

/**
 * Pure spatial and event state required for UI hit-testing and interaction.
 * Decoupled from hardware-specific input modules.
 */
data class UiInputState(
    val pointerX: Float = -1f,
    val pointerY: Float = -1f,
    val pointerDown: Boolean = false,
    val scrollDeltaX: Float = 0f,
    val scrollDeltaY: Float = 0f,
    val typedText: String = "",
    val editActions: List<UiTextEditAction> = emptyList()
)

fun InputSnapshot.toUiInputState(): UiInputState = UiInputState(
    pointerX = pointerX,
    pointerY = pointerY,
    pointerDown = pointerDown,
    scrollDeltaX = scrollDeltaX,
    scrollDeltaY = scrollDeltaY,
    typedText = typedText,
    editActions = editActions.map { it.toUiAction() }
)

private fun TextEditAction.toUiAction(): UiTextEditAction = when (this) {
    TextEditAction.Backspace -> UiTextEditAction.Backspace
    TextEditAction.Delete -> UiTextEditAction.Delete
    TextEditAction.Enter -> UiTextEditAction.Enter
    TextEditAction.ArrowLeft -> UiTextEditAction.ArrowLeft
    TextEditAction.ArrowRight -> UiTextEditAction.ArrowRight
    TextEditAction.ArrowUp -> UiTextEditAction.ArrowUp
    TextEditAction.ArrowDown -> UiTextEditAction.ArrowDown
    TextEditAction.Home -> UiTextEditAction.Home
    TextEditAction.End -> UiTextEditAction.End
}

/**
 * UI-internal mirror of text editing actions to avoid coupling to core.input.
 */
enum class UiTextEditAction {
    Backspace, Delete, Enter, ArrowLeft, ArrowRight, ArrowUp, ArrowDown, Home, End
}
