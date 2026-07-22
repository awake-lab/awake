// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.context

import io.github.ronjunevaldoz.awake.ui.CoreUiTheme
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme

internal class UiContextStacks {
    private val themeStack = mutableListOf<UiTheme>(CoreUiTheme)
    private val textStyleStack = mutableListOf(TextStyle.Default)
    private val fontStack = mutableListOf(UiFonts.default())

    val currentTheme: UiTheme get() = themeStack.last()
    val currentTextStyle: TextStyle get() = textStyleStack.last()
    val currentFont: UiFont get() = fontStack.last()

    fun pushTheme(theme: UiTheme) { themeStack.add(theme) }
    fun popTheme() { if (themeStack.size > 1) themeStack.removeAt(themeStack.size - 1) }

    fun pushTextStyle(style: TextStyle) { textStyleStack.add(textStyleStack.last() then style) }
    fun popTextStyle() { if (textStyleStack.size > 1) textStyleStack.removeAt(textStyleStack.size - 1) }

    fun pushFont(font: UiFont) { fontStack.add(font) }
    fun popFont() { if (fontStack.size > 1) fontStack.removeAt(fontStack.size - 1) }
}
