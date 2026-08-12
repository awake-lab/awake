// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.context

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiSemanticNode

data class UiInputOwnership(
    val isCaptured: Boolean = false,
    val isOverScrollable: Boolean = false,
    val isScrollConsumed: Boolean = false,
    val isTextInputFocused: Boolean = false,
)

/**
 * Gameplay must ignore keys when the UI owns the pointer OR is taking text.
 *
 * [UiInputOwnership.isCaptured] is pointer capture only, so gating gameplay on it alone let
 * typing W/A/S/D into a focused text field both insert the characters and walk the player.
 */
val UiInputOwnership.blocksGameplayKeys: Boolean get() = isCaptured || isTextInputFocused

data class UiPlatformEffects(
    val requestKeyboard: Boolean = false,
    /** See [UiCursor] -- the platform embedding this [UiContext] applies this via its own
     * windowing API (e.g. `glfwSetCursor` on desktop); `ui-core` never calls a platform API
     * itself. */
    val cursor: UiCursor = UiCursor.Default,
)

data class UiFrameOutput(
    val primitives: List<UiDrawPrimitive>,
    val semantics: List<UiSemanticNode>,
    val ownership: UiInputOwnership = UiInputOwnership(),
    val effects: UiPlatformEffects = UiPlatformEffects(),
)
