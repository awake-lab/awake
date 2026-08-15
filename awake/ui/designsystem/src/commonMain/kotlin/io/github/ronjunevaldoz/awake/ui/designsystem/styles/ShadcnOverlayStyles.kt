// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.styles

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.style.Style

internal fun shadcnSheetSurfaceStyle(values: UiThemeValues): Style = Style { background(values.colors.background); foreground(values.colors.foreground); border(1f.dp, values.colors.border); contentPadding(16f.dp) }
internal fun shadcnDrawerSurfaceStyle(values: UiThemeValues): Style = shadcnSheetSurfaceStyle(values)
internal fun shadcnDialogSurfaceStyle(values: UiThemeValues): Style = Style { background(values.colors.card); foreground(values.colors.cardForeground); border(1f.dp, values.colors.border); shape(values.shapes.lg); contentPadding(24f.dp) }
