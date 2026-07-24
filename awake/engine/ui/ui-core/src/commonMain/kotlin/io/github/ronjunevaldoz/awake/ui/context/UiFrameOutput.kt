// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.context

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiSemanticNode

data class UiInputOwnership(
    val isCaptured: Boolean = false,
    val isOverScrollable: Boolean = false,
    val isScrollConsumed: Boolean = false,
    val isTextInputFocused: Boolean = false
)

data class UiPlatformEffects(
    val requestKeyboard: Boolean = false
)

data class UiFrameOutput(
    val primitives: List<UiDrawPrimitive>,
    val semantics: List<UiSemanticNode>,
    val ownership: UiInputOwnership = UiInputOwnership(),
    val effects: UiPlatformEffects = UiPlatformEffects()
)
