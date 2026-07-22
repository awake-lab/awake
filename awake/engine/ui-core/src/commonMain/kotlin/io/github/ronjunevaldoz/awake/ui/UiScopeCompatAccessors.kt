// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme

/**
 * Compatibility accessors for widgets that still read theme/font/textStyle directly from
 * a [UiScope]. New code can use `context.current...` explicitly, but keeping these accessors
 * avoids forcing broad churn across higher-level UI modules during the refactor.
 */
val UiScope.theme: UiTheme
    get() = context.currentTheme

val UiScope.font: UiFont
    get() = context.currentFont

val UiScope.textStyle: TextStyle
    get() = context.currentTextStyle

val UiScope.resolvedThemeCaptionStyle: TextStyle
    get() = context.currentTextStyle then TextStyle(size = context.currentTheme.typography.caption)
