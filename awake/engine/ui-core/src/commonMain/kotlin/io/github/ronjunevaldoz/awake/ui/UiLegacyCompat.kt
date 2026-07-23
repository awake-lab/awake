// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.core.graphics.clip as coreClip
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.layouts.AbsoluteScope
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.UiSpacing
import io.github.ronjunevaldoz.awake.ui.layouts.deprecatedGapArrangement
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.layouts.ext.row as extRow
import io.github.ronjunevaldoz.awake.ui.layouts.ext.spacer as extSpacer
import io.github.ronjunevaldoz.awake.ui.layouts.ext.surface as extSurface
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme

/**
 * Transitional compatibility overloads for older root-style UiContext usage.
 *
 * These APIs exist to keep older tests, previews, and downstream call sites compiling while the
 * new modifier-first root API settles. New code should avoid these overloads and instead use the
 * direct `UiContext.create*` / `UiContext.column(...)` APIs with explicit environment setup.
 */
private inline fun <T> UiContext.withLegacyRootEnvironment(
    font: UiFont,
    theme: UiTheme,
    textScale: Float,
    content: UiContext.() -> T
): T {
    pushFont(font)
    pushTheme(theme)
    if (textScale != currentTextStyle.scale) {
        pushTextStyle(TextStyle(scale = textScale))
    }
    return content()
}

@Deprecated(
    message = "Legacy root helper slated for future removal. Prefer createAbsolute(modifier = ...) plus explicit pushFont/pushTheme/pushTextStyle when needed."
)
fun UiContext.createAbsolute(
    x: Float,
    y: Float,
    font: UiFont = currentFont,
    theme: UiTheme = currentTheme,
    textScale: Float = currentTextStyle.scale,
    overlayOnly: Boolean = false
): AbsoluteScope = withLegacyRootEnvironment(font, theme, textScale) {
    createAbsolute(x = x, y = y, overlayOnly = overlayOnly)
}

@Deprecated(
    message = "Legacy root helper slated for future removal. Prefer createColumn(modifier = ...) plus explicit pushFont/pushTheme/pushTextStyle when needed."
)
fun UiContext.createColumn(
    x: Float,
    y: Float,
    width: Float,
    height: Float? = null,
    gap: Float = UiSpacing.sm.toPx(),
    verticalArrangement: Arrangement = deprecatedGapArrangement(gap),
    font: UiFont = currentFont,
    theme: UiTheme = currentTheme,
    textScale: Float = currentTextStyle.scale,
    overlayOnly: Boolean = false
): ColumnScope = withLegacyRootEnvironment(font, theme, textScale) {
    createColumn(
        x = x,
        y = y,
        width = width,
        height = height,
        gap = gap,
        verticalArrangement = verticalArrangement,
        overlayOnly = overlayOnly
    )
}

@Deprecated(
    message = "Legacy root helper slated for future removal. Prefer column(modifier = ...) plus explicit pushFont/pushTheme/pushTextStyle when needed."
)
fun UiContext.column(
    x: Float,
    y: Float,
    width: Float,
    height: Float? = null,
    gap: Float = UiSpacing.sm.toPx(),
    verticalArrangement: Arrangement = deprecatedGapArrangement(gap),
    font: UiFont = currentFont,
    theme: UiTheme = currentTheme,
    textScale: Float = currentTextStyle.scale,
    block: ColumnScope.() -> Unit
) {
    createColumn(
        x = x,
        y = y,
        width = width,
        height = height,
        gap = gap,
        verticalArrangement = verticalArrangement,
        font = font,
        theme = theme,
        textScale = textScale
    ).block()
}

@Deprecated(
    message = "Legacy root helper slated for future removal. Prefer createBox(modifier = ...) plus explicit pushFont/pushTheme/pushTextStyle when needed."
)
fun UiContext.createBox(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    contentAlignment: UiAlignment = UiAlignment.TopStart,
    font: UiFont = currentFont,
    theme: UiTheme = currentTheme,
    textScale: Float = currentTextStyle.scale,
    overlayOnly: Boolean = false
): BoxScope = withLegacyRootEnvironment(font, theme, textScale) {
    createBox(
        x = x,
        y = y,
        width = width,
        height = height,
        contentAlignment = contentAlignment,
        overlayOnly = overlayOnly
    )
}

fun UiScope.clip(rect: UiSlot, content: UiScope.() -> Unit) = coreClip(rect, content)

fun UiScope.clip(shape: UiShapeSpec, rect: UiSlot, content: UiScope.() -> Unit) =
    coreClip(shape, rect, content)

fun UiScope.surface(
    id: String,
    width: Dimension,
    height: Dimension,
    verticalArrangement: Arrangement = io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement(),
    radius: Dp = UiShape.md,
    borderWidth: Dp = UiShape.none,
    style: Style = Style.Empty,
    modifier: UiModifier = UiModifier(),
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = extSurface(id, width, height, verticalArrangement, radius, borderWidth, style, modifier, clipContent, content)

fun ColumnScope.surface(
    id: String,
    width: Dimension,
    height: Dimension,
    verticalArrangement: Arrangement = io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement(),
    radius: Dp = UiShape.md,
    borderWidth: Dp = UiShape.none,
    style: Style = Style.Empty,
    modifier: UiModifier = UiModifier(),
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = extSurface(id, height, width, verticalArrangement, radius, borderWidth, style, modifier, clipContent, content)

fun RowScope.surface(
    id: String,
    width: Dimension,
    height: Dimension = Dimension.FillMax,
    verticalArrangement: Arrangement = io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement(),
    radius: Dp = UiShape.md,
    borderWidth: Dp = UiShape.none,
    style: Style = Style.Empty,
    modifier: UiModifier = UiModifier(),
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = extSurface(id, width, height, verticalArrangement, radius, borderWidth, style, modifier, clipContent, content)

fun AbsoluteScope.surface(
    id: String,
    width: Dimension,
    height: Dimension,
    verticalArrangement: Arrangement = io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement(),
    radius: Dp = UiShape.md,
    borderWidth: Dp = UiShape.none,
    style: Style = Style.Empty,
    modifier: UiModifier = UiModifier(),
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = extSurface(id, width, height, verticalArrangement, radius, borderWidth, style, modifier, clipContent, content)

fun BoxScope.surface(
    id: String,
    width: Dimension = Dimension.WrapContent,
    height: Dimension = Dimension.WrapContent,
    verticalArrangement: Arrangement = io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement(),
    radius: Dp = UiShape.md,
    borderWidth: Dp = UiShape.none,
    style: Style = Style.Empty,
    modifier: UiModifier = UiModifier(),
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = extSurface(id, width, height, verticalArrangement, radius, borderWidth, style, modifier, clipContent, content)

fun ColumnScope.row(
    height: Dp,
    width: Dimension = Dimension.FillMax,
    gap: Float = UiSpacing.sm.toPx(),
    modifier: UiModifier = UiModifier(),
    content: RowScope.() -> Unit
): UiSlot = extRow(
    height = height,
    width = width,
    horizontalArrangement = deprecatedGapArrangement(gap),
    modifier = modifier
) { content() }

fun ColumnScope.spacer(modifier: UiModifier) = extSpacer(modifier)

fun RowScope.spacer(modifier: UiModifier) = extSpacer(modifier)
