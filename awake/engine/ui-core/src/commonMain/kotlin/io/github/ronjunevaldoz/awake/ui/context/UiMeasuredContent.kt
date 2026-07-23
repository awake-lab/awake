// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.context

import io.github.ronjunevaldoz.awake.ui.scope.UiSlot

data class UiMeasuredContent(
    val width: Float,
    val height: Float,
    val slots: List<UiSlot> = emptyList()
)
