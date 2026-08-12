// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets
import io.github.ronjunevaldoz.awake.ui.api.sp
import io.github.ronjunevaldoz.awake.ui.api.theme.FontWeight
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.BoxScope
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.RowScope
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceBorder
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.UiSeparatorOrientation
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.padding
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.separator
import io.github.ronjunevaldoz.awake.ui.headless.surface
import io.github.ronjunevaldoz.awake.ui.headless.text

private fun sidebarStyle(scope: UiScope): SurfaceStyle = SurfaceStyle(
    background = scope.themeValues.colors.card,
    foreground = scope.themeValues.colors.foreground,
    border = SurfaceBorder(1f.dp, scope.themeValues.colors.border),
    contentPadding = UiInsets(ShadcnSidebarMetrics.contentPadding),
)

fun UiScope.shadcnSidebar(
    id: String,
    modifier: Modifier = Modifier,
    expanded: Boolean = true,
    header: (ColumnScope.() -> Unit)? = null,
    footer: (ColumnScope.() -> Unit)? = null,
    content: ColumnScope.(UiBounds) -> Unit,
): UiBounds = surface(id, modifier, sidebarStyle(this)) {
    if (expanded) {
        header?.invoke(this)
        content(it)
        footer?.invoke(this)
    }
}


fun UiScope.shadcnSidebarGroup(
    modifier: Modifier = Modifier,
    label: String? = null,
    content: ColumnScope.() -> Unit,
) {
    column(
        modifier.fillMaxWidth().padding(horizontal = 4f.dp, vertical = 8f.dp),
        Arrangement.spacedBy(ShadcnSidebarMetrics.groupGap)
    ) {
        label?.let {
            text(
                label = it.uppercase(),
                visuals = SurfaceStyle(
                    foreground = themeValues.colors.mutedForeground,
                    // Sidebar group titles are Tailwind `text-xs` (12px) and `font-medium`.
                    textSize = 12f.sp,
                    fontWeight = FontWeight.Medium,
                    contentPadding = UiInsets(horizontal = 8f.dp, vertical = 0f.dp)
                )
            )
        }
        content()
    }
}

fun UiScope.shadcnSidebarMenu(
    modifier: Modifier = Modifier,
    content: ColumnScope.() -> Unit,
) {
    column(
        modifier.fillMaxWidth(),
        Arrangement.spacedBy(ShadcnSidebarMetrics.menuGap)
    ) { content() }
}

fun UiScope.shadcnSidebarMenuItem(
    id: String,
    label: String,
    active: Boolean = false,
    modifier: Modifier = Modifier,
    badge: String? = null,
    onClick: () -> Unit = {},
): Boolean = shadcnButton(
    id = id,
    label = if (badge == null) label else "$label  $badge",
    modifier = modifier.fillMaxWidth().height(ShadcnSidebarMetrics.menuButtonHeight),
    variant = if (active) ShadcnButtonVariant.Secondary else ShadcnButtonVariant.Ghost,
    centered = false,
    onClick = onClick,
)

fun UiScope.shadcnSidebarMenuSub(
    modifier: Modifier = Modifier,
    content: ColumnScope.() -> Unit,
) {
    row(modifier = modifier.fillMaxWidth().padding(start = ShadcnSidebarMetrics.submenuIndent)) {
        separator(
            color = themeValues.colors.border,
            orientation = UiSeparatorOrientation.Vertical
        )
        column(
            modifier = Modifier.fillMaxWidth(),
            Arrangement.spacedBy(ShadcnSidebarMetrics.submenuGap)
        ) {
            content()
        }
    }
}

fun UiScope.shadcnSidebarMenuSubItem(
    id: String,
    label: String,
    active: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
): Boolean = shadcnButton(
    id = id,
    label = label,
    modifier = modifier.fillMaxWidth().height(ShadcnSidebarMetrics.submenuButtonHeight),
    variant = if (active) ShadcnButtonVariant.Secondary else ShadcnButtonVariant.Ghost,
    centered = false,
    onClick = onClick,
)
