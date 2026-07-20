// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.font.UiFont

fun UiContext.ui(
    x: Float,
    y: Float,
    width: Float,
    font: UiFont? = null,
    theme: UiTheme = CoreUiTheme,
    gap: Float = UiSpacing.sm.toPx(),
    textScale: Float = 1f,
    block: ColumnScope.() -> Unit
) {
    column(x, y, width, font, theme, gap, textScale).block()
}

fun UiContext.ui(
    slot: UiSlot,
    font: UiFont? = null,
    theme: UiTheme = CoreUiTheme,
    gap: Float = UiSpacing.sm.toPx(),
    textScale: Float = 1f,
    insets: UiInsets = UiInsets.Zero,
    block: ColumnScope.() -> Unit
) {
    column(slot, font, theme, gap, textScale, insets).block()
}

fun UiContext.uiAbsolute(
    x: Float,
    y: Float,
    font: UiFont? = null,
    theme: UiTheme = CoreUiTheme,
    textScale: Float = 1f,
    block: AbsoluteScope.() -> Unit
) {
    absolute(x, y, font, theme, textScale).block()
}

fun ColumnScope.ui(
    block: ColumnScope.() -> Unit
) {
    this.block()
}
