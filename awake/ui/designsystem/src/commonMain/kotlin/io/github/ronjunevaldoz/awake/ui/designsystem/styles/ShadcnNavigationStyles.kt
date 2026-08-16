// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.styles

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.sp
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnThemeValues
import io.github.ronjunevaldoz.awake.ui.font.FontWeight
import io.github.ronjunevaldoz.awake.ui.style.Style

internal fun shadcnCollapsibleTriggerStyle(values: ShadcnThemeValues): Style = Style {
    background(Color.Transparent)
    foreground(values.colors.foreground)
    shape(values.shapes.md)
    hovered { background(values.colors.accent); foreground(values.colors.accentForeground) }
}

internal fun shadcnCollapsibleTitleStyle(values: ShadcnThemeValues): Style = Style {
    textSize(values.typography.body)
}

internal fun shadcnCollapsibleCardStyle(values: ShadcnThemeValues): Style = Style {
    background(values.colors.card)
    foreground(values.colors.foreground)
    border(1f.dp, values.colors.border)
    shape(values.shapes.lg)
}

internal fun shadcnAccordionContentStyle(): Style = Style { contentPadding(0f.dp, 0f.dp, 0f.dp, 16f.dp) }

internal fun shadcnTabsTrackStyle(values: ShadcnThemeValues): Style = Style {
    background(values.colors.muted)
    shape(values.shapes.lg)
    contentPadding(3f.dp)
}

internal fun shadcnTabStyle(values: ShadcnThemeValues, active: Boolean): Style = Style {
    background(if (active) values.colors.background else Color.Transparent)
    foreground(if (active) values.colors.foreground else values.colors.foreground.withAlpha(0.6f))
    border(1f.dp, Color.Transparent)
    shape(values.shapes.md)
    contentPadding(horizontal = 8f.dp, vertical = 4f.dp)
    if (active) shadow(Color.Black.withAlpha(0.12f), offsetY = 1f.dp, blurRadius = 2f.dp)
    textSize(14f.sp)
    fontWeight(FontWeight.Medium)
}

internal fun shadcnBreadcrumbItemStyle(values: ShadcnThemeValues, current: Boolean): Style = Style {
    foreground(if (current) values.colors.foreground else values.colors.mutedForeground)
    textSize(values.typography.caption)
}

internal fun shadcnBreadcrumbMutedStyle(values: ShadcnThemeValues): Style =
    shadcnBreadcrumbItemStyle(values, current = false)

internal fun shadcnBreadcrumbContainerStyle(): Style = Style.Empty
