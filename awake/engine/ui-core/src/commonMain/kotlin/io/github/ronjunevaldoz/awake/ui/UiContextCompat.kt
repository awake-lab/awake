// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.layouts.AbsoluteScope
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.baseSpacingPx
import io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement
import io.github.ronjunevaldoz.awake.ui.layouts.resolveAgainst
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

/**
 * Compatibility helpers for root-scope test and preview code.
 *
 * The runtime API now prefers explicit [pushFont], [pushTheme], and [pushTextStyle] calls before
 * creating scopes. A large part of the test and preview surface still uses the older
 * root-construction shape that threaded font/theme through the root call itself, so we keep that
 * convenience here instead of duplicating manual stack setup across every proof surface.
 *
 * These helpers are transitional and should not be used by new code. Prefer the direct
 * `UiContext.create*` APIs plus explicit environment setup.
 */
internal fun UiContext.resolveRootSlot(
    modifier: UiModifier,
    defaultWidth: Dimension = Dimension.FillMax,
    defaultHeight: Dimension = Dimension.FillMax
): UiSlot {
    val frame = frameBounds()
    val requestedWidth = modifier.widthDimension ?: defaultWidth
    val requestedHeight = modifier.heightDimension ?: defaultHeight
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

private inline fun <T> UiContext.withRootEnvironment(
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
    message = "Compatibility root helper slated for future removal. Prefer explicit pushFont/pushTheme/pushTextStyle plus direct UiContext.createColumn(...) APIs."
)
fun UiContext.createColumn(
    modifier: UiModifier = Modifier,
    verticalArrangement: Arrangement = defaultArrangement(),
    font: UiFont = currentFont,
    theme: UiTheme = currentTheme,
    textScale: Float = currentTextStyle.scale,
    overlayOnly: Boolean = false
): ColumnScope = withRootEnvironment(font, theme, textScale) {
    createColumn(
        slot = resolveRootSlot(modifier),
        gap = verticalArrangement.baseSpacingPx(),
        insets = modifier.insets,
        verticalArrangement = verticalArrangement,
        testTag = modifier.testTag,
        overlayOnly = overlayOnly
    )
}

@Deprecated(
    message = "Compatibility root helper slated for future removal. Prefer explicit pushFont/pushTheme/pushTextStyle plus direct UiContext.createAbsolute(...) APIs."
)
fun UiContext.createAbsolute(
    modifier: UiModifier = Modifier,
    font: UiFont = currentFont,
    theme: UiTheme = currentTheme,
    textScale: Float = currentTextStyle.scale,
    overlayOnly: Boolean = false
): AbsoluteScope = withRootEnvironment(font, theme, textScale) {
    createAbsolute(
        slot = resolveRootSlot(
            modifier = modifier,
            defaultWidth = Dimension.Fixed(0.dp),
            defaultHeight = Dimension.Fixed(0.dp)
        ),
        insets = modifier.insets,
        testTag = modifier.testTag,
        overlayOnly = overlayOnly
    )
}

@Deprecated(
    message = "Compatibility root helper slated for future removal. Prefer explicit pushFont/pushTheme/pushTextStyle plus direct UiContext.createRow(...) APIs."
)
fun UiContext.createRow(
    modifier: UiModifier = Modifier,
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

@Deprecated(
    message = "Compatibility root helper slated for future removal. Prefer explicit pushFont/pushTheme/pushTextStyle plus direct UiContext.createBox(...) APIs."
)
fun UiContext.createBox(
    modifier: UiModifier = Modifier,
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

@Deprecated(
    message = "Compatibility root helper slated for future removal. Prefer explicit pushFont/pushTheme/pushTextStyle plus direct UiContext.createColumn(...) APIs."
)
fun UiContext.column(
    modifier: UiModifier = Modifier,
    verticalArrangement: Arrangement = defaultArrangement(),
    font: UiFont = currentFont,
    theme: UiTheme = currentTheme,
    textScale: Float = currentTextStyle.scale,
    block: ColumnScope.() -> Unit
) {
    createColumn(
        modifier = modifier,
        verticalArrangement = verticalArrangement,
        font = font,
        theme = theme,
        textScale = textScale
    ).block()
}

