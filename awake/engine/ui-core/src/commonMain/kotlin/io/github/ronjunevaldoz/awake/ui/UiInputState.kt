// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

/**
 * Pure spatial and event state required for UI hit-testing and interaction.
 * Decoupled from hardware-specific input modules.
 */
data class UiInputState(
    val pointerX: Float = -1f,
    val pointerY: Float = -1f,
    val pointerDown: Boolean = false,
    val scrollDeltaY: Float = 0f,
    val typedText: String = "",
    val editActions: List<UiTextEditAction> = emptyList()
)

/**
 * UI-internal mirror of text editing actions to avoid coupling to core.input.
 */
enum class UiTextEditAction {
    Backspace, Delete, Enter, ArrowLeft, ArrowRight, Home, End
}
