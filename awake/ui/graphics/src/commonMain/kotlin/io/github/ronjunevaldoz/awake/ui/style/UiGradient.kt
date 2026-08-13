// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color

data class UiLinearGradient(
    val topLeft: Color,
    val topRight: Color,
    val bottomRight: Color,
    val bottomLeft: Color,
) {
    companion object {
        fun vertical(top: Color, bottom: Color): UiLinearGradient = UiLinearGradient(
            topLeft = top,
            topRight = top,
            bottomRight = bottom,
            bottomLeft = bottom,
        )

        fun horizontal(start: Color, end: Color): UiLinearGradient = UiLinearGradient(
            topLeft = start,
            topRight = end,
            bottomRight = end,
            bottomLeft = start,
        )
    }
}

typealias LinearGradient = UiLinearGradient
typealias Gradient = UiLinearGradient
