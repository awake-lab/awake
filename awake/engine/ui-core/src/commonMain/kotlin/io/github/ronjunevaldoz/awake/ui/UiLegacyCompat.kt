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
import io.github.ronjunevaldoz.awake.ui.layouts.ext.row as extRow
import io.github.ronjunevaldoz.awake.ui.layouts.ext.spacer as extSpacer
import io.github.ronjunevaldoz.awake.ui.layouts.ext.surface as extSurface
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme

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

@Deprecated("Use createAbsolute(modifier = ...) with UiModifier offsets.")
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

@Deprecated("Use createColumn(modifier = ...) with UiModifier offsets and sizing.")
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

@Deprecated("Use column(modifier = ...) with UiModifier offsets and sizing.")
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
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = extSurface(id, width, height, verticalArrangement, radius, borderWidth, style, modifier, content)

fun ColumnScope.surface(
    id: String,
    width: Dimension,
    height: Dimension,
    verticalArrangement: Arrangement = io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement(),
    radius: Dp = UiShape.md,
    borderWidth: Dp = UiShape.none,
    style: Style = Style.Empty,
    modifier: UiModifier = UiModifier(),
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = extSurface(id, height, width, verticalArrangement, radius, borderWidth, style, modifier, content)

fun RowScope.surface(
    id: String,
    width: Dimension,
    height: Dimension = Dimension.FillMax,
    verticalArrangement: Arrangement = io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement(),
    radius: Dp = UiShape.md,
    borderWidth: Dp = UiShape.none,
    style: Style = Style.Empty,
    modifier: UiModifier = UiModifier(),
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = extSurface(id, width, height, verticalArrangement, radius, borderWidth, style, modifier, content)

fun AbsoluteScope.surface(
    id: String,
    width: Dimension,
    height: Dimension,
    verticalArrangement: Arrangement = io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement(),
    radius: Dp = UiShape.md,
    borderWidth: Dp = UiShape.none,
    style: Style = Style.Empty,
    modifier: UiModifier = UiModifier(),
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = extSurface(id, width, height, verticalArrangement, radius, borderWidth, style, modifier, content)

fun BoxScope.surface(
    id: String,
    width: Dimension = Dimension.WrapContent,
    height: Dimension = Dimension.WrapContent,
    verticalArrangement: Arrangement = io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement(),
    radius: Dp = UiShape.md,
    borderWidth: Dp = UiShape.none,
    style: Style = Style.Empty,
    modifier: UiModifier = UiModifier(),
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = extSurface(id, width, height, verticalArrangement, radius, borderWidth, style, modifier, content)

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
