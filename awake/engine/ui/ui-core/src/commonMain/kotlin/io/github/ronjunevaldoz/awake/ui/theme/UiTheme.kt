// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.theme

import io.github.ronjunevaldoz.awake.ui.UiComponentStyles

/**
 * A complete, swappable look for a [io.github.ronjunevaldoz.awake.ui.UiScope] -- assigned once at `ui.column(..., theme = ...)`.
 */
interface UiTheme {
    val tokens: UiColorTokens
    val components: UiComponentStyles
    val typography: UiTypography get() = UiTypography.Default
}
