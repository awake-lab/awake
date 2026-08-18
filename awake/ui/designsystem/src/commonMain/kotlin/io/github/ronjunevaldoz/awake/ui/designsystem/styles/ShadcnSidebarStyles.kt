// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.styles

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.sp
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnThemeValues
import io.github.ronjunevaldoz.awake.ui.font.FontWeight
import io.github.ronjunevaldoz.awake.ui.style.Style

internal fun shadcnSidebarStyle(values: ShadcnThemeValues): Style = Style {
    background(values.colors.card)
    foreground(values.colors.foreground)
    contentPadding(8f.dp)
}

internal fun shadcnSidebarActionStyle(values: ShadcnThemeValues): Style = Style {
    background(Color.Transparent)
    foreground(values.colors.foreground)
    shape(values.shapes.md)
    contentPadding(horizontal = 6f.dp, vertical = 4f.dp)
    hovered {
        background(values.colors.accent)
        foreground(values.colors.accentForeground)
    }
}

internal fun shadcnSidebarHeaderBadgeStyle(values: ShadcnThemeValues): Style = Style {
    background(values.colors.primary)
    foreground(values.colors.primaryForeground)
    shape(values.shapes.md)
    contentPadding(6f.dp)
}

internal fun shadcnSidebarAvatarStyle(values: ShadcnThemeValues): Style = Style {
    background(values.colors.secondary)
    foreground(values.colors.secondaryForeground)
    shape(16f.dp)
    contentPadding(6f.dp)
}

internal fun shadcnSidebarTitleStyle(values: ShadcnThemeValues): Style = Style {
    textSize(values.typography.label)
    fontWeight(FontWeight.SemiBold)
    foreground(values.colors.foreground)
}

// 11sp has no matching io.github.ronjunevaldoz.awake.ui.api.theme.UiTypography step (12/14/16/
// 20/24/30) -- left as a literal per docs/audits/2026-08-17-ui-refactor-vs-recreate-audit.md's F8.
internal fun shadcnSidebarSupportingTextStyle(values: ShadcnThemeValues): Style = Style {
    textSize(11f.sp)
    foreground(values.colors.mutedForeground)
}

internal fun shadcnSidebarGroupLabelStyle(values: ShadcnThemeValues): Style = Style {
    foreground(values.colors.mutedForeground)
    textSize(values.typography.caption)
    fontWeight(FontWeight.Medium)
    contentPadding(horizontal = 8f.dp, vertical = 0f.dp)
}

internal fun shadcnSidebarMenuItemStyle(values: ShadcnThemeValues, active: Boolean): Style = Style {
    background(if (active) values.colors.secondary else Color.Transparent)
    foreground(if (active) values.colors.secondaryForeground else values.colors.foreground)
    shape(values.shapes.md)
    contentPadding(horizontal = 8f.dp, vertical = 0f.dp)
    hovered {
        background(values.colors.accent)
        foreground(values.colors.accentForeground)
    }
}

// 14sp matches typography.label numerically, but this fn takes no ShadcnThemeValues to read it
// from -- adding one is a signature change beyond F8's literal sweep, so left as a literal.
internal fun shadcnSidebarMenuLabelStyle(active: Boolean): Style = Style {
    textSize(14f.sp)
    fontWeight(if (active) FontWeight.Medium else FontWeight.Normal)
}

// 13sp has no matching UiTypography step (12/14/16/20/24/30) -- left as a literal per F8.
internal fun shadcnSidebarSubmenuLabelStyle(active: Boolean): Style = Style {
    textSize(13f.sp)
    fontWeight(if (active) FontWeight.Medium else FontWeight.Normal)
}
