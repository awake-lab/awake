// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.theme

import io.github.ronjunevaldoz.awake.ui.api.theme.UiColorTokens
import io.github.ronjunevaldoz.awake.ui.api.theme.UiShapeTokens
import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.api.theme.UiTypography

/**
 * A complete, swappable look for a [io.github.ronjunevaldoz.awake.ui.UiPrimitiveScope] -- assigned once at `ui.column(..., theme = ...)`.
 */
interface UiTheme : UiThemeValues {
    override val colors: UiColorTokens
    val components: UiComponentStyles
    override val typography: UiTypography get() = UiTypography.Default

    /** Third theme pillar alongside [colors] (color) and [typography] -- see [UiShapeTokens].
     * Defaults to the neutral [UiFallbackShapeTokens] so every existing [UiTheme] implementer
     * keeps compiling unchanged; a real design-system theme should override this with its own
     * named radius scale, same as it already overrides [colors]. */
    override val shapes: UiShapeTokens get() = UiFallbackShapeTokens
}

/** Optional component-style override supplied by a theme above Core. */
interface UiComponentStylesProvider {
    val componentStyles: UiComponentStyles
}

/**
 * Adapts a runtime-free theme value set for the Core context stack.
 *
 * Design-system modules must publish [UiThemeValues], not Core's [UiTheme]. Core supplies the
 * neutral component-style fallback while Headless consumes the value contracts directly.
 */
/**
 * Core runtime adapter for public, runtime-free theme contracts.
 *
 * Design-system modules should continue publishing [UiThemeValues]; callers that install a
 * theme into a Core-owned runtime (for example GameUiDsl) may use this facade instead of
 * depending on Core component recipes.
 */
fun UiThemeValues.asRuntimeTheme(): UiTheme = this as? UiTheme ?: object : UiTheme {
    override val colors = this@asRuntimeTheme.colors
    override val typography = this@asRuntimeTheme.typography
    override val shapes = this@asRuntimeTheme.shapes
    override val components: UiComponentStyles =
        (this@asRuntimeTheme as? UiComponentStylesProvider)?.componentStyles
            ?: CoreUiComponentStyles(colors, typography)
}
