// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.theme

/**
 * A complete, swappable look for a [io.github.ronjunevaldoz.awake.ui.UiPrimitiveScope] -- assigned once at `ui.column(..., theme = ...)`.
 */
interface UiTheme {
    val colors: UiColorTokens
    val components: UiComponentStyles
    val typography: UiTypography get() = UiTypography.Default

    /** Third theme pillar alongside [colors] (color) and [typography] -- see [UiShapeTokens].
     * Defaults to the neutral [UiFallbackShapeTokens] so every existing [UiTheme] implementer
     * keeps compiling unchanged; a real design-system theme should override this with its own
     * named radius scale, same as it already overrides [colors]. */
    val shapes: UiShapeTokens get() = UiFallbackShapeTokens
}
