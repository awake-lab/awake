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
@Deprecated(
    message = "Compatibility scope accessor slated for future removal. Prefer context.currentTheme explicitly."
)
val UiScope.theme: UiTheme
    get() = context.currentTheme

@Deprecated(
    message = "Compatibility scope accessor slated for future removal. Prefer context.currentFont explicitly."
)
val UiScope.font: UiFont
    get() = context.currentFont

@Deprecated(
    message = "Compatibility scope accessor slated for future removal. Prefer context.currentTextStyle explicitly."
)
val UiScope.textStyle: TextStyle
    get() = context.currentTextStyle

@Deprecated(
    message = "Compatibility scope accessor slated for future removal. Prefer composing the caption style from context.currentTheme/context.currentTextStyle explicitly."
)
val UiScope.resolvedThemeCaptionStyle: TextStyle
    get() = context.currentTextStyle then TextStyle(size = context.currentTheme.typography.caption)
