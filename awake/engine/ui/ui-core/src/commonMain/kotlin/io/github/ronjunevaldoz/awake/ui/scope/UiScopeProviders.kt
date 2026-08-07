// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("ktlint:standard:function-naming")

package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme

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

fun UiScope.ProvideShapeSpec(spec: UiShapeSpec?, content: UiScope.() -> Unit) {
    context.pushShapeSpec(spec)
    this.content()
    context.popShapeSpec()
}
