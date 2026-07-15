// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.font.BitmapFont

fun UiContext.ui(
    x: Float,
    y: Float,
    width: Float,
    font: BitmapFont? = null,
    theme: UiTheme = CoreUiTheme,
    gap: Float = UiSpacing.sm.toPx(),
    textScale: Float = 1f,
    block: UiColumnDslScope.() -> Unit
) {
    UiColumnDslScope(column(x, y, width, font, theme, gap, textScale)).block()
}

fun UiContext.ui(
    slot: UiSlot,
    font: BitmapFont? = null,
    theme: UiTheme = CoreUiTheme,
    gap: Float = UiSpacing.sm.toPx(),
    textScale: Float = 1f,
    insets: UiInsets = UiInsets.Zero,
    block: UiColumnDslScope.() -> Unit
) {
    UiColumnDslScope(column(slot, font, theme, gap, textScale, insets)).block()
}

fun UiContext.uiAbsolute(
    x: Float,
    y: Float,
    font: BitmapFont? = null,
    theme: UiTheme = CoreUiTheme,
    textScale: Float = 1f,
    block: UiAbsoluteDslScope.() -> Unit
) {
    UiAbsoluteDslScope(absolute(x, y, font, theme, textScale)).block()
}

fun ColumnScope.ui(
    block: UiColumnDslScope.() -> Unit
) {
    UiColumnDslScope(this).block()
}
