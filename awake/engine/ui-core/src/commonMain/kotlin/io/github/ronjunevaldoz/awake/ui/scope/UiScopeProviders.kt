// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.font.UiFont

fun UiScope.ProvideTextStyle(style: TextStyle, content: UiScope.() -> Unit) {
    context.pushTextStyle(style)
    this.content()
    context.popTextStyle()
}

fun UiScope.ProvideTheme(theme: UiTheme, content: UiScope.() -> Unit) {
    context.pushTheme(theme)
    this.content()
    context.popTheme()
}

fun UiScope.ProvideFont(font: UiFont, content: UiScope.() -> Unit) {
    context.pushFont(font)
    this.content()
    context.popFont()
}
