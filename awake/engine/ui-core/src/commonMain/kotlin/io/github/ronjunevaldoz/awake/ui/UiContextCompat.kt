// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.layouts.AbsoluteScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.UiSpacing
import io.github.ronjunevaldoz.awake.ui.toPx

/**
 * Compatibility helpers for root-scope test and preview code.
 *
 * The runtime API now prefers explicit [pushFont], [pushTheme], and [pushTextStyle] calls before
 * creating scopes. A large part of the test and preview surface still uses the older
 * root-construction shape that threaded font/theme through the root call itself, so we keep that
 * convenience here instead of duplicating manual stack setup across every proof surface.
 */
fun UiContext.createColumn(
    x: Float,
    y: Float,
    width: Float,
    height: Float? = null,
    gap: Float = UiSpacing.sm.toPx(),
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
        x = x,
        y = y,
        width = width,
        height = height,
        gap = gap,
        overlayOnly = overlayOnly
    )
}

fun UiContext.createAbsolute(
    x: Float,
    y: Float,
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
        x = x,
        y = y,
        overlayOnly = overlayOnly
    )
}

fun UiContext.column(
    x: Float,
    y: Float,
    width: Float,
    gap: Float = UiSpacing.sm.toPx(),
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
        x = x,
        y = y,
        width = width,
        gap = gap
    ).block()
}
