// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.context

/**
 * Aggregated input ownership result for a single UI frame.
 * TODO should use InputDispatcher?
 */
data class UiInputResult(
    val isCaptured: Boolean = false,
    val isOverScrollable: Boolean = false,
    val isScrollConsumed: Boolean = false,
    val isTextInputFocused: Boolean = false,
)
