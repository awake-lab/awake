// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.styles

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.Sp
import io.github.ronjunevaldoz.awake.ui.font.FontWeight
import io.github.ronjunevaldoz.awake.ui.style.Style

/** Canonical `foreground + textSize (+ optional fontWeight)` shape -- the repeated two-liner
 * every text-only recipe style used to hand-roll. [weight] left unset matches the previous
 * behavior of simply never calling `fontWeight()`. */
internal fun shadcnTextStyle(foreground: Color, size: Sp, weight: FontWeight? = null): Style = Style {
    foreground(foreground)
    textSize(size)
    weight?.let { fontWeight(it) }
}
