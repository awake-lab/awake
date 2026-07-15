// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.CoreUiComponentStyles
import io.github.ronjunevaldoz.awake.ui.CoreUiTheme
import io.github.ronjunevaldoz.awake.ui.UiColorTokens
import io.github.ronjunevaldoz.awake.ui.UiTheme

/**
 * Neutral preset themes intended for authored app and sample UI.
 *
 * These remain visually conservative, but they live in `ui-designsystem` so `ui-core` stays
 * limited to theme contracts plus a low-level fallback.
 */
object DefaultUiTheme : UiTheme by CoreUiTheme

object DarkUiTheme : UiTheme by DefaultUiTheme

object LightUiTheme : UiTheme {
    override val tokens: UiColorTokens = object : UiColorTokens {
        override val background = floatArrayOf(0.98f, 0.98f, 0.99f, 1f)
        override val foreground = floatArrayOf(0.1f, 0.1f, 0.12f, 1f)
        override val primary = floatArrayOf(0.2f, 0.2f, 0.24f, 1f)
        override val primaryForeground = floatArrayOf(0.98f, 0.98f, 0.99f, 1f)
        override val secondary = floatArrayOf(0.9f, 0.9f, 0.92f, 1f)
        override val secondaryForeground = floatArrayOf(0.1f, 0.1f, 0.12f, 1f)
        override val muted = floatArrayOf(0.9f, 0.9f, 0.92f, 1f)
        override val mutedForeground = floatArrayOf(0.4f, 0.4f, 0.45f, 1f)
        override val accent = floatArrayOf(0.85f, 0.85f, 0.88f, 1f)
        override val accentForeground = floatArrayOf(0.1f, 0.1f, 0.12f, 1f)
        override val destructive = floatArrayOf(0.8f, 0.2f, 0.2f, 1f)
        override val destructiveForeground = floatArrayOf(0.98f, 0.98f, 0.99f, 1f)
        override val border = floatArrayOf(0.8f, 0.8f, 0.83f, 1f)
    }

    override val components = CoreUiComponentStyles(tokens)
}
