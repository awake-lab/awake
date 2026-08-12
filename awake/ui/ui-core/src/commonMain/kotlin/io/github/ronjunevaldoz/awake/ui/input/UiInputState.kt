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
    val secondaryPointerDown: Boolean = false,
    val scrollDeltaX: Float = 0f,
    val scrollDeltaY: Float = 0f,
    val typedText: String = "",
    val editActions: List<UiTextEditAction> = emptyList(),
    /** Space was pressed this frame -- the one raw key edge exposed to widget activation
     * (Radix/shadcn's Space-activates-the-focused-control convention). Not Enter too: `Enter`
     * has no [io.github.ronjunevaldoz.awake.core.input.Key] entry, only a
     * [io.github.ronjunevaldoz.awake.core.input.TextEditAction] for text-field submit, so it
     * isn't observable here yet -- widening `Key` to add it is a separate, larger change. */
    val activatePressed: Boolean = false,
)

fun InputSnapshot.toUiInputState(): UiInputState = UiInputState(
    pointerX = pointerX,
    pointerY = pointerY,
    pointerDown = pointerDown,
    secondaryPointerDown = secondaryPointerDown,
    scrollDeltaX = scrollDeltaX,
    scrollDeltaY = scrollDeltaY,
    typedText = typedText,
    editActions = editActions.map { it.toUiAction() },
    activatePressed = wasPressed(io.github.ronjunevaldoz.awake.core.input.Key.Space),
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
    Backspace,
    Delete,
    Enter,
    ArrowLeft,
    ArrowRight,
    ArrowUp,
    ArrowDown,
    Home,
    End,
}
