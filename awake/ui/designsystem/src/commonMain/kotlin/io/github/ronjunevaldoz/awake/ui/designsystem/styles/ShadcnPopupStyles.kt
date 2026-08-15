// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.styles

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.sp
import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.style.Style

internal fun shadcnDropdownSurfaceStyle(values: UiThemeValues): Style = Style { background(values.colors.popover); foreground(values.colors.popoverForeground); border(1f.dp, values.colors.border); shape(values.shapes.md); contentPadding(4f.dp) }
internal fun shadcnDropdownItemStyle(values: UiThemeValues, selected: Boolean, destructive: Boolean): Style = Style {
    background(if (selected) values.colors.accent else values.colors.popover)
    foreground(if (selected) values.colors.accentForeground else if (destructive) values.colors.destructive else values.colors.popoverForeground)
    shape(values.shapes.sm); contentPadding(horizontal = 8f.dp, vertical = 6f.dp); textSize(14f.sp)
    hovered { background(if (destructive) values.colors.destructive.withAlpha(0.1f) else values.colors.accent); foreground(if (destructive) values.colors.destructive else values.colors.accentForeground) }
}
internal fun shadcnTooltipStyle(values: UiThemeValues): Style = Style { background(values.colors.foreground); foreground(values.colors.background); shape(values.shapes.md); contentPadding(horizontal = 12f.dp, vertical = 6f.dp); textSize(12f.sp); lineHeight(16f.sp) }
internal fun shadcnAlertDialogSurfaceStyle(values: UiThemeValues): Style = Style { background(values.colors.card); foreground(values.colors.cardForeground); border(1f.dp, values.colors.border); shape(values.shapes.lg); contentPadding(24f.dp) }
internal fun shadcnDialogTitleStyle(values: UiThemeValues): Style = Style { textSize(values.typography.title) }
internal fun shadcnDialogBodyStyle(values: UiThemeValues): Style = Style { textSize(values.typography.body) }
