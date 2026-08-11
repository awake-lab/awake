// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.context

import io.github.ronjunevaldoz.awake.ui.api.layout.LayoutWeight
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds

data class UiMeasuredContent(
    val width: Float,
    val height: Float,
    val slots: List<UiBounds> = emptyList(),
    /** Parallel to [slots] -- `weights[i]` is the [LayoutWeight] claimed alongside `slots[i]`,
     * or null if that child claimed its slot without a weight. */
    val weights: List<LayoutWeight?> = emptyList(),
    /** Parallel to [slots]/[weights] -- `fillsMainAxis[i]` is true when that (non-weighted)
     * child claimed `Dimension.FillMax` on the row/column's own main axis (width for a row,
     * height for a column). Lets [io.github.ronjunevaldoz.awake.ui.layouts.resolveWeightedMainAxis]
     * tell a real "occupied" claim (a fixed/intrinsic-size sibling) apart from a FillMax sibling's
     * trial size, which is an artifact of the trial measuring against the container's full size
     * before any weighted sibling has claimed its share -- see that function's doc comment. */
    val fillsMainAxis: List<Boolean> = emptyList(),
)
