// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.UiImageVector
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets
import io.github.ronjunevaldoz.awake.ui.api.sp
import io.github.ronjunevaldoz.awake.ui.font.FontWeight
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.UiSeparatorOrientation
import io.github.ronjunevaldoz.awake.ui.headless.button
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxHeight
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.icon
import io.github.ronjunevaldoz.awake.ui.headless.padding
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.separator
import io.github.ronjunevaldoz.awake.ui.headless.size
import io.github.ronjunevaldoz.awake.ui.headless.surface
import io.github.ronjunevaldoz.awake.ui.headless.text
import io.github.ronjunevaldoz.awake.ui.headless.weight
import io.github.ronjunevaldoz.awake.ui.style.Style

private fun sidebarStyle(scope: UiScope): Style = Style {
    background(scope.themeValues.colors.card)
    foreground(scope.themeValues.colors.foreground)
    contentPadding(8f.dp)
}

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
        // Upstream sidebar.tsx: SidebarContent is `min-h-0 flex-1 overflow-auto`, header and footer
        // get neither. Content is the only slot that absorbs slack, and that is what keeps the
        // other two pinned to the sidebar's edges. The slots sit directly in the surface -- every
        // container that lays out a column now shares one weight planner, including the scroll
        // panel a scrolled sidebar routes through, so the wrapper column that used to work around
        // that is gone.
        column(modifier = Modifier.fillMaxWidth().weight(1f)) { contentSlot ->
            content(contentSlot)
        }
        footer?.invoke(this)
    }
}

fun UiScope.shadcnSidebarHeaderButton(
    id: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
): Boolean {
    val clicked = button(
        id = id,
        modifier = modifier.fillMaxWidth().height(48f.dp),
        style = Style {
            background(io.github.ronjunevaldoz.awake.core.colors.Color.Transparent)
            foreground(themeValues.colors.foreground)
            shape(themeValues.shapes.md)
            contentPadding(horizontal = 6f.dp, vertical = 4f.dp)
            hovered { background(themeValues.colors.accent); foreground(themeValues.colors.accentForeground) }
        },
    ) {
        row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = UiAlignment.Vertical.Center,
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        ) {
            row(
                horizontalArrangement = Arrangement.spacedBy(8f.dp),
                verticalAlignment = UiAlignment.Vertical.Center,
            ) {
                surface(
                    id = "$id.badge",
                    modifier = Modifier.size(32f.dp),
                    style = Style { background(themeValues.colors.primary); foreground(themeValues.colors.primaryForeground); shape(themeValues.shapes.md); contentPadding(6f.dp) },
                ) {
                    icon(ShadcnIcons.squares2x2)
                }
                column {
                    text(
                        label = title,
                        style = Style { textSize(14f.sp); fontWeight(FontWeight.SemiBold); foreground(themeValues.colors.foreground) },
                    )
                    text(
                        label = subtitle,
                        style = Style { textSize(11f.sp); foreground(themeValues.colors.mutedForeground) },
                    )
                }
            }
            icon(ShadcnIcons.chevronDown)
        }
    }
    if (clicked) onClick()
    return clicked
}

fun UiScope.shadcnSidebarFooterButton(
    id: String,
    name: String,
    email: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
): Boolean {
    val clicked = button(
        id = id,
        modifier = modifier.fillMaxWidth().height(48f.dp),
        style = Style {
            background(io.github.ronjunevaldoz.awake.core.colors.Color.Transparent)
            foreground(themeValues.colors.foreground)
            shape(themeValues.shapes.md)
            contentPadding(horizontal = 6f.dp, vertical = 4f.dp)
            hovered { background(themeValues.colors.accent); foreground(themeValues.colors.accentForeground) }
        },
    ) {
        row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = UiAlignment.Vertical.Center,
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        ) {
            row(
                horizontalArrangement = Arrangement.spacedBy(8f.dp),
                verticalAlignment = UiAlignment.Vertical.Center,
            ) {
                surface(
                    id = "$id.avatar",
                    modifier = Modifier.size(32f.dp),
                    style = Style { background(themeValues.colors.secondary); foreground(themeValues.colors.secondaryForeground); shape(16f.dp); contentPadding(6f.dp) },
                ) {
                    icon(ShadcnIcons.user)
                }
                column {
                    text(
                        label = name,
                        style = Style { textSize(14f.sp); fontWeight(FontWeight.SemiBold); foreground(themeValues.colors.foreground) },
                    )
                    text(
                        label = email,
                        style = Style { textSize(11f.sp); foreground(themeValues.colors.mutedForeground) },
                    )
                }
            }
            icon(ShadcnIcons.chevronDown)
        }
    }
    if (clicked) onClick()
    return clicked
}

fun UiScope.shadcnSidebarGroup(
    modifier: Modifier = Modifier,
    label: String? = null,
    content: ColumnScope.() -> Unit,
) {
    column(
        modifier.fillMaxWidth().padding(horizontal = 4f.dp, vertical = 8f.dp),
        Arrangement.spacedBy(ShadcnSidebarMetrics.groupGap),
    ) {
        label?.let {
            text(
                label = it.uppercase(),
                style = Style {
                    foreground(themeValues.colors.mutedForeground)
                    // Sidebar group titles are Tailwind `text-xs` (12px) and `font-medium`.
                    textSize(12f.sp)
                    fontWeight(FontWeight.Medium)
                    contentPadding(horizontal = 8f.dp, vertical = 0f.dp)
                },
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
        Arrangement.spacedBy(ShadcnSidebarMetrics.menuGap),
    ) { content() }
}

fun UiScope.shadcnSidebarMenuItem(
    id: String,
    label: String,
    active: Boolean = false,
    modifier: Modifier = Modifier,
    icon: UiImageVector? = null,
    badge: String? = null,
    onClick: () -> Unit = {},
): Boolean {
    val clicked = button(
        id = id,
        modifier = modifier.fillMaxWidth().height(ShadcnSidebarMetrics.menuButtonHeight),
        style = Style {
            background(if (active) themeValues.colors.secondary else io.github.ronjunevaldoz.awake.core.colors.Color.Transparent)
            foreground(if (active) themeValues.colors.secondaryForeground else themeValues.colors.foreground)
            shape(themeValues.shapes.md)
            contentPadding(horizontal = 8f.dp, vertical = 0f.dp)
            hovered { background(themeValues.colors.accent); foreground(themeValues.colors.accentForeground) }
        },
    ) {
        row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = UiAlignment.Vertical.Center,
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        ) {
            row(
                horizontalArrangement = Arrangement.spacedBy(8f.dp),
                verticalAlignment = UiAlignment.Vertical.Center,
            ) {
                if (icon != null) {
                    icon(icon)
                }
                text(
                    label = label,
                    style = Style { textSize(14f.sp); fontWeight(if (active) FontWeight.Medium else FontWeight.Normal) },
                )
            }
            if (badge != null) {
                text(
                    label = badge,
                    style = Style { textSize(11f.sp); foreground(themeValues.colors.mutedForeground) },
                )
            }
        }
    }
    if (clicked) onClick()
    return clicked
}

fun UiScope.shadcnSidebarMenuSub(
    modifier: Modifier = Modifier,
    content: ColumnScope.() -> Unit,
) {
    row(modifier = modifier.fillMaxWidth().padding(start = ShadcnSidebarMetrics.submenuIndent)) {
        separator(
            color = themeValues.colors.border,
            orientation = UiSeparatorOrientation.Vertical,
        )
        column(
            modifier = Modifier.fillMaxWidth().padding(start = 6f.dp),
            Arrangement.spacedBy(ShadcnSidebarMetrics.submenuGap),
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
    icon: UiImageVector? = null,
    onClick: () -> Unit = {},
): Boolean {
    val clicked = button(
        id = id,
        modifier = modifier.fillMaxWidth().height(ShadcnSidebarMetrics.submenuButtonHeight),
        style = Style {
            background(if (active) themeValues.colors.secondary else io.github.ronjunevaldoz.awake.core.colors.Color.Transparent)
            foreground(if (active) themeValues.colors.secondaryForeground else themeValues.colors.foreground)
            shape(themeValues.shapes.md)
            contentPadding(horizontal = 8f.dp, vertical = 0f.dp)
            hovered { background(themeValues.colors.accent); foreground(themeValues.colors.accentForeground) }
        },
    ) {
        row(
            horizontalArrangement = Arrangement.spacedBy(8f.dp),
            verticalAlignment = UiAlignment.Vertical.Center,
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        ) {
            if (icon != null) {
                icon(icon)
            }
            text(
                label = label,
                style = Style { textSize(13f.sp); fontWeight(if (active) FontWeight.Medium else FontWeight.Normal) },
            )
        }
    }
    if (clicked) onClick()
    return clicked
}
