// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

/**
 * A complete, swappable look for a [UiScope] -- assigned once at `ui.column(..., theme = ...)`.
 */
interface UiTheme {
    val tokens: UiColorTokens
    val components: UiComponentStyles
    val typography: UiTypography get() = UiTypography.Default
}
