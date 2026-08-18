// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.context.LocalFont
import io.github.ronjunevaldoz.awake.ui.context.LocalTextStyle
import io.github.ronjunevaldoz.awake.ui.context.LocalTheme
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme

/**
 * Scope-level environment accessors. Widgets and compositions should consume theme/font/text
 * state from [UiPrimitiveScope] instead of treating [io.github.ronjunevaldoz.awake.ui.context.UiContext]
 * as a styling bag.
 */
val UiPrimitiveScope.theme: UiTheme
    get() = context.current(LocalTheme)

val UiPrimitiveScope.font: UiFont
    get() = context.current(LocalFont)

val UiPrimitiveScope.textStyle: TextStyle
    get() = context.current(LocalTextStyle)
