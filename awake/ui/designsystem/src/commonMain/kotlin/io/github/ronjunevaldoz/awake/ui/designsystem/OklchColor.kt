// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.core.colors.Color

typealias OklchColor = io.github.ronjunevaldoz.awake.ui.tailwind.OklchColor

fun oklch(
    lightness: Float,
    chroma: Float,
    hueDegrees: Float = 0f,
    alpha: Float = 1f,
): Color = io.github.ronjunevaldoz.awake.ui.tailwind.oklch(lightness, chroma, hueDegrees, alpha)
