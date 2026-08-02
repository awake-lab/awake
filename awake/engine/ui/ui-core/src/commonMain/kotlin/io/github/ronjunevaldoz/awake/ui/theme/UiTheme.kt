// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.theme

/**
 * A complete, swappable look for a [io.github.ronjunevaldoz.awake.ui.UiScope] -- assigned once at `ui.column(..., theme = ...)`.
 */
interface UiTheme {
    val tokens: UiColorTokens
    val components: UiComponentStyles
    val typography: UiTypography get() = UiTypography.Default

    /** Third theme pillar alongside [tokens] (color) and [typography] -- see [UiShapeTokens].
     * Defaults to the neutral [UiFallbackShapeTokens] so every existing [UiTheme] implementer
     * keeps compiling unchanged; a real design-system theme should override this with its own
     * named radius scale, same as it already overrides [tokens]. */
    val shapes: UiShapeTokens get() = UiFallbackShapeTokens
}
