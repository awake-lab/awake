// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.api.theme.UiColorTokens
import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues

/**
 * Neutral preset themes intended for authored app and sample UI.
 *
 * These remain visually conservative, but they live in `ui-designsystem` so `ui-core` stays
 * limited to theme contracts plus a low-level fallback.
 */
object ShadcnDefaultTheme : UiThemeValues by ShadcnTheme

object DarkUiTheme : UiThemeValues by ShadcnDefaultTheme

object LightUiTheme : UiThemeValues {
    override val colors: UiColorTokens = object : UiColorTokens {
        override val background = oklch(1f, 0f)
        override val foreground = oklch(0.145f, 0f)
        override val card = oklch(1f, 0f)
        override val cardForeground = oklch(0.145f, 0f)
        override val popover = oklch(1f, 0f)
        override val popoverForeground = oklch(0.145f, 0f)
        override val primary = oklch(0.205f, 0f)
        override val primaryForeground = oklch(0.985f, 0f)
        override val secondary = oklch(0.97f, 0f)
        override val secondaryForeground = oklch(0.205f, 0f)
        override val muted = oklch(0.97f, 0f)
        override val mutedForeground = oklch(0.556f, 0f)
        override val accent = oklch(0.97f, 0f)
        override val accentForeground = oklch(0.205f, 0f)
        override val destructive = oklch(0.577f, 0.245f, 27.325f)
        override val destructiveForeground = oklch(0.985f, 0f)
        override val border = oklch(0.922f, 0f)
    }

    override val typography = ShadcnTheme.typography
    override val shapes = ShadcnTheme.shapes
}
