// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.theme.UiColorTokens
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme
import io.github.ronjunevaldoz.awake.ui.theme.UiTypography

/**
 * Neutral fallback theme for low-level UI APIs.
 *
 * This lives in `ui-core` because root scopes need a no-extra-dependency default, but it is
 * intentionally generic. Named authored themes belong in `ui-designsystem`.
 */
object CoreUiTheme : UiTheme {
    override val tokens = object : UiColorTokens {
        override val background = Color(0.25f, 0.25f, 0.28f, 0.9f)
        override val foreground = Color.White
        override val primary = Color(0.5f, 0.5f, 0.6f, 0.9f)
        override val primaryForeground = Color.White
        override val secondary = Color(0.35f, 0.35f, 0.4f, 0.9f)
        override val secondaryForeground = Color.White
        override val muted = Color(0.35f, 0.35f, 0.4f, 0.9f)
        override val mutedForeground = Color(0.8f, 0.8f, 0.8f, 1f)
        override val accent = Color(0.5f, 0.5f, 0.6f, 0.9f)
        override val accentForeground = Color.White
        override val destructive = Color(0.8f, 0.2f, 0.2f, 0.9f)
        override val destructiveForeground = Color.White
        override val border = Color(0.4f, 0.4f, 0.45f, 0.9f)
    }
    override val typography: UiTypography = UiTypography.Default
    override val components: UiComponentStyles = CoreUiComponentStyles(tokens, typography)
}
