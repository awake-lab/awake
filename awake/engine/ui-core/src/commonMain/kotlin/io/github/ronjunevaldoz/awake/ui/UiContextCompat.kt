// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.layouts.AbsoluteScope
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.baseSpacingPx
import io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement
import io.github.ronjunevaldoz.awake.ui.layouts.resolveAgainst

/**
 * Compatibility helpers for root-scope test and preview code.
 *
 * The runtime API now prefers explicit [pushFont], [pushTheme], and [pushTextStyle] calls before
 * creating scopes. A large part of the test and preview surface still uses the older
 * root-construction shape that threaded font/theme through the root call itself, so we keep that
 * convenience here instead of duplicating manual stack setup across every proof surface.
 */
internal fun UiContext.resolveRootSlot(
    modifier: UiModifier,
    defaultWidth: Dimension = Dimension.FillMax,
    defaultHeight: Dimension = Dimension.FillMax
): UiSlot {
    val frame = frameBounds()
    val requestedWidth = modifier.width ?: defaultWidth
    val requestedHeight = modifier.height ?: defaultHeight
    val width = requestedWidth.resolveAgainst(frame.width)
    val height = requestedHeight.resolveAgainst(frame.height)
    return frame.place(
        width = width,
        height = height,
        alignment = modifier.alignment ?: UiAlignment.TopStart,
        insets = modifier.insets,
        offsetX = modifier.offsetX.toPx(),
        offsetY = modifier.offsetY.toPx()
    )
}

fun UiContext.createColumn(
    modifier: UiModifier = UiModifier(),
    verticalArrangement: Arrangement = defaultArrangement(),
    font: UiFont = currentFont,
    theme: UiTheme = currentTheme,
    textScale: Float = currentTextStyle.scale,
    overlayOnly: Boolean = false
): ColumnScope = createColumn(
    slot = resolveRootSlot(modifier),
    verticalArrangement = verticalArrangement,
    font = font,
    theme = theme,
    textScale = textScale,
    overlayOnly = overlayOnly
)

@Deprecated(
    message = "Use createColumn(modifier = ...) so authored root layout comes from UiModifier, not UiSlot geometry.",
    replaceWith = ReplaceWith("createColumn(modifier = modifier, verticalArrangement = verticalArrangement, font = font, theme = theme, textScale = textScale, overlayOnly = overlayOnly)")
)
fun UiContext.createColumn(
    slot: UiSlot,
    modifier: UiModifier = UiModifier(),
    verticalArrangement: Arrangement = defaultArrangement(),
    font: UiFont = currentFont,
    theme: UiTheme = currentTheme,
    textScale: Float = currentTextStyle.scale,
    overlayOnly: Boolean = false
): ColumnScope {
    pushFont(font)
    pushTheme(theme)
    if (textScale != currentTextStyle.scale) {
        pushTextStyle(TextStyle(scale = textScale))
    }
    return createColumn(
        slot = slot,
        gap = verticalArrangement.baseSpacingPx(),
        insets = modifier.insets,
        verticalArrangement = verticalArrangement,
        testTag = modifier.testTag,
        overlayOnly = overlayOnly
    )
}

fun UiContext.createAbsolute(
    modifier: UiModifier = UiModifier(),
    font: UiFont = currentFont,
    theme: UiTheme = currentTheme,
    textScale: Float = currentTextStyle.scale,
    overlayOnly: Boolean = false
): AbsoluteScope = createAbsolute(
    slot = resolveRootSlot(
        modifier = modifier,
        defaultWidth = Dimension.Fixed(0.dp),
        defaultHeight = Dimension.Fixed(0.dp)
    ),
    font = font,
    theme = theme,
    textScale = textScale,
    overlayOnly = overlayOnly
)

@Deprecated(
    message = "Use createAbsolute(modifier = ...) so authored root layout comes from UiModifier, not UiSlot geometry.",
    replaceWith = ReplaceWith("createAbsolute(modifier = modifier, font = font, theme = theme, textScale = textScale, overlayOnly = overlayOnly)")
)
fun UiContext.createAbsolute(
    slot: UiSlot,
    modifier: UiModifier = UiModifier(),
    font: UiFont = currentFont,
    theme: UiTheme = currentTheme,
    textScale: Float = currentTextStyle.scale,
    overlayOnly: Boolean = false
): AbsoluteScope {
    pushFont(font)
    pushTheme(theme)
    if (textScale != currentTextStyle.scale) {
        pushTextStyle(TextStyle(scale = textScale))
    }
    return createAbsolute(
        slot = slot,
        insets = modifier.insets,
        testTag = modifier.testTag,
        overlayOnly = overlayOnly
    )
}

fun UiContext.createRow(
    modifier: UiModifier = UiModifier(),
    horizontalArrangement: Arrangement = defaultArrangement(),
    font: UiFont = currentFont,
    theme: UiTheme = currentTheme,
    textScale: Float = currentTextStyle.scale,
    overlayOnly: Boolean = false
): RowScope {
    pushFont(font)
    pushTheme(theme)
    if (textScale != currentTextStyle.scale) {
        pushTextStyle(TextStyle(scale = textScale))
    }
    return createRow(
        slot = resolveRootSlot(modifier),
        gap = horizontalArrangement.baseSpacingPx(),
        horizontalArrangement = horizontalArrangement,
        testTag = modifier.testTag,
        overlayOnly = overlayOnly
    )
}

fun UiContext.createBox(
    modifier: UiModifier = UiModifier(),
    contentAlignment: UiAlignment = UiAlignment.TopStart,
    font: UiFont = currentFont,
    theme: UiTheme = currentTheme,
    textScale: Float = currentTextStyle.scale,
    overlayOnly: Boolean = false
): BoxScope {
    pushFont(font)
    pushTheme(theme)
    if (textScale != currentTextStyle.scale) {
        pushTextStyle(TextStyle(scale = textScale))
    }
    return createBox(
        slot = resolveRootSlot(modifier),
        contentAlignment = contentAlignment,
        testTag = modifier.testTag,
        overlayOnly = overlayOnly
    )
}

fun UiContext.column(
    modifier: UiModifier = UiModifier(),
    verticalArrangement: Arrangement = defaultArrangement(),
    font: UiFont = currentFont,
    theme: UiTheme = currentTheme,
    textScale: Float = currentTextStyle.scale,
    block: ColumnScope.() -> Unit
) {
    column(
        slot = resolveRootSlot(modifier),
        verticalArrangement = verticalArrangement,
        font = font,
        theme = theme,
        textScale = textScale,
        block = block
    )
}

@Deprecated(
    message = "Use column(modifier = ...) so authored root layout comes from UiModifier, not UiSlot geometry.",
    replaceWith = ReplaceWith("column(modifier = modifier, verticalArrangement = verticalArrangement, font = font, theme = theme, textScale = textScale, block = block)")
)
fun UiContext.column(
    slot: UiSlot,
    modifier: UiModifier = UiModifier(),
    verticalArrangement: Arrangement = defaultArrangement(),
    font: UiFont = currentFont,
    theme: UiTheme = currentTheme,
    textScale: Float = currentTextStyle.scale,
    block: ColumnScope.() -> Unit
) {
    pushFont(font)
    pushTheme(theme)
    if (textScale != currentTextStyle.scale) {
        pushTextStyle(TextStyle(scale = textScale))
    }
    createColumn(
        slot = slot,
        modifier = modifier,
        verticalArrangement = verticalArrangement
    ).block()
}
