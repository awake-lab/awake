// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("ktlint:standard:function-naming")

package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme

// Every provider pops in a finally. Without it, content that throws leaves its value on the stack
// for the rest of the frame, and every later widget renders with a theme or text style it never
// asked for -- a failure that surfaces nowhere near the throw. Scope is the whole point of these
// helpers, so it has to hold on the failing path too.

fun UiPrimitiveScope.ProvideTextStyle(style: TextStyle, content: UiPrimitiveScope.() -> Unit) {
    context.pushTextStyle(style)
    try {
        this.content()
    } finally {
        context.popTextStyle()
    }
}

fun UiPrimitiveScope.ProvideTheme(theme: UiTheme, content: UiPrimitiveScope.() -> Unit) {
    context.pushTheme(theme)
    try {
        this.content()
    } finally {
        context.popTheme()
    }
}

fun UiPrimitiveScope.ProvideFont(font: UiFont, content: UiPrimitiveScope.() -> Unit) {
    context.pushFont(font)
    try {
        this.content()
    } finally {
        context.popFont()
    }
}

fun UiPrimitiveScope.ProvideShapeSpec(spec: UiShapeSpec?, content: UiPrimitiveScope.() -> Unit) {
    context.pushShapeSpec(spec)
    try {
        this.content()
    } finally {
        context.popShapeSpec()
    }
}
