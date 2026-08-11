// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("ktlint:standard:function-naming")

package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme

fun UiPrimitiveScope.ProvideTextStyle(style: TextStyle, content: UiPrimitiveScope.() -> Unit) {
    context.pushTextStyle(style)
    this.content()
    context.popTextStyle()
}

fun UiPrimitiveScope.ProvideTheme(theme: UiTheme, content: UiPrimitiveScope.() -> Unit) {
    context.pushTheme(theme)
    this.content()
    context.popTheme()
}

fun UiPrimitiveScope.ProvideFont(font: UiFont, content: UiPrimitiveScope.() -> Unit) {
    context.pushFont(font)
    this.content()
    context.popFont()
}

fun UiPrimitiveScope.ProvideShapeSpec(spec: UiShapeSpec?, content: UiPrimitiveScope.() -> Unit) {
    context.pushShapeSpec(spec)
    this.content()
    context.popShapeSpec()
}
