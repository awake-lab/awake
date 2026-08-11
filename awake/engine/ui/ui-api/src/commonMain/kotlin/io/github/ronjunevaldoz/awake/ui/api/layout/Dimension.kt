// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.api.layout

import io.github.ronjunevaldoz.awake.ui.api.Dp

/** A widget's platform-independent sizing intent. Runtime resolution belongs to ui-core. */
sealed class Dimension {
    data class Fixed(val dp: Dp) : Dimension()

    /** Fills the corresponding bounded axis supplied by the runtime layout scope. */
    data object FillMax : Dimension()

    /** Sizes a composite container from the measured extent of its content. */
    data object WrapContent : Dimension()
}

/** Proportional main-axis sizing intent for a row or column child. */
data class LayoutWeight(val weight: Float, val fill: Boolean = true)

/** Converts a positive authored size to fixed sizing; zero and negatives request fill. */
fun Dp.toDimension(): Dimension = if (value > 0f) Dimension.Fixed(this) else Dimension.FillMax
