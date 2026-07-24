// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.context

import io.github.ronjunevaldoz.awake.ui.UiInputState

data class UiFrameInput(
    val viewportWidth: Float,
    val viewportHeight: Float,
    val input: UiInputState,
    val deltaSeconds: Float = 1f / 60f
)
