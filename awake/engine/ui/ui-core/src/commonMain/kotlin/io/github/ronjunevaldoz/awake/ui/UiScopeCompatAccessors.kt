// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme

/**
 * Scope-level environment accessors. Widgets and compositions should consume theme/font/text
 * state from [UiScope] instead of treating [io.github.ronjunevaldoz.awake.ui.context.UiContext]
 * as a styling bag.
 */
val UiScope.theme: UiTheme
    get() = context.currentTheme

val UiScope.font: UiFont
    get() = context.currentFont

val UiScope.textStyle: TextStyle
    get() = context.currentTextStyle

val UiScope.resolvedThemeCaptionStyle: TextStyle
    get() = context.currentTextStyle then TextStyle(size = context.currentTheme.typography.caption)
