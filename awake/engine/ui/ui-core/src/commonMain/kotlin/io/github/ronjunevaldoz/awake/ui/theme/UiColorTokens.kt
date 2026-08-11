// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.theme

import io.github.ronjunevaldoz.awake.ui.api.theme.UiColorTokens
import io.github.ronjunevaldoz.awake.ui.style.Style

/** hovered -> muted, active -> accent, base -> background. */
fun UiColorTokens.neutralStyle(): Style =
    Style {
        background(background)
        foreground(foreground)
        hovered { background(muted) }
        active { background(accent) }
    }

/** Same state-varying shape as [neutralStyle], but for the destructive role. */
fun UiColorTokens.destructiveStyle(): Style =
    Style {
        background(destructive)
        foreground(destructiveForeground)
        hovered { background(brighten(destructive, 1.05f)) }
        active { background(brighten(destructive, 1.15f)) }
    }

private fun brighten(
    color: io.github.ronjunevaldoz.awake.core.colors.Color,
    brightness: Float,
): io.github.ronjunevaldoz.awake.core.colors.Color = color.brighten(brightness)
