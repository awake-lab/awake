// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.context

import io.github.ronjunevaldoz.awake.ui.layout.LayoutWeight
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot

data class UiMeasuredContent(
    val width: Float,
    val height: Float,
    val slots: List<UiSlot> = emptyList(),
    /** Parallel to [slots] -- `weights[i]` is the [LayoutWeight] claimed alongside `slots[i]`,
     * or null if that child claimed its slot without a weight. */
    val weights: List<LayoutWeight?> = emptyList()
)
