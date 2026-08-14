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
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceBorder
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.UiSeparatorOrientation
import io.github.ronjunevaldoz.awake.ui.headless.buttonSlot
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

private fun sidebarStyle(scope: UiScope): SurfaceStyle = SurfaceStyle(
    background = scope.themeValues.colors.card,
    foreground = scope.themeValues.colors.foreground,
    border = SurfaceBorder(1f.dp, scope.themeValues.colors.border),
    contentPadding = UiInsets(
        top = 8f.dp,
        bottom = 8f.dp,
        start = 8f.dp,
        end = 8f.dp,
    ),
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
        // The slots sit in a plain column rather than directly in the surface. A weighted child
        // placed DIRECTLY in a surface still throws when that surface is nested inside a scroll
        // viewport -- surface()'s planner runs its trial and comes back with no weights, so it
        // plans nothing. Clearing the ancestor recording suppression (UiContext
        // .withOwnMeasurementScope) was necessary but not sufficient; the remaining cause is not
        // yet identified. A visual-free column() always reaches the measured path that honours
        // weight, so the slots go through one. Reproduce by deleting this wrapper and running
        // ShadcnButtonScrollClickInteractionTest.
        column(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
            header?.invoke(this)
            // Upstream sidebar.tsx: SidebarContent is `min-h-0 flex-1 overflow-auto`, header and
            // footer get neither. Content is the only slot that absorbs slack, and that is what
            // keeps the other two pinned to the sidebar's edges.
            column(modifier = Modifier.fillMaxWidth().weight(1f)) { contentSlot ->
                content(contentSlot)
            }
            footer?.invoke(this)
        }
    }
}

fun UiScope.shadcnSidebarHeaderButton(
    id: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
): Boolean {
    val clicked = buttonSlot(
        id = id,
        modifier = modifier.fillMaxWidth().height(48f.dp),
        visuals = io.github.ronjunevaldoz.awake.ui.headless.SurfaceVisuals(
            rest = SurfaceStyle(
                background = io.github.ronjunevaldoz.awake.core.colors.Color.Transparent,
                foreground = themeValues.colors.foreground,
                cornerRadius = themeValues.shapes.md,
                contentPadding = UiInsets(horizontal = 6f.dp, vertical = 4f.dp),
            ),
            hovered = SurfaceStyle(
                background = themeValues.colors.accent,
                foreground = themeValues.colors.accentForeground,
                cornerRadius = themeValues.shapes.md,
                contentPadding = UiInsets(horizontal = 6f.dp, vertical = 4f.dp),
            ),
        ),
    ) {
        row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = UiAlignment.Vertical.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            row(
                horizontalArrangement = Arrangement.spacedBy(8f.dp),
                verticalAlignment = UiAlignment.Vertical.Center,
            ) {
                surface(
                    id = "$id.badge",
                    modifier = Modifier.size(32f.dp),
                    style = SurfaceStyle(
                        background = themeValues.colors.primary,
                        foreground = themeValues.colors.primaryForeground,
                        cornerRadius = themeValues.shapes.md,
                        contentPadding = UiInsets(6f.dp),
                    ),
                ) {
                    icon(ShadcnIcons.squares2x2)
                }
                column {
                    text(
                        label = title,
                        visuals = SurfaceStyle(
                            textSize = 14f.sp,
                            fontWeight = FontWeight.SemiBold,
                            foreground = themeValues.colors.foreground,
                        ),
                    )
                    text(
                        label = subtitle,
                        visuals = SurfaceStyle(
                            textSize = 11f.sp,
                            foreground = themeValues.colors.mutedForeground,
                        ),
                    )
                }
            }
            icon(ShadcnIcons.chevronDown)
        }
    }
    if (clicked.clicked) onClick()
    return clicked.clicked
}

fun UiScope.shadcnSidebarFooterButton(
    id: String,
    name: String,
    email: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
): Boolean {
    val clicked = buttonSlot(
        id = id,
        modifier = modifier.fillMaxWidth().height(48f.dp),
        visuals = io.github.ronjunevaldoz.awake.ui.headless.SurfaceVisuals(
            rest = SurfaceStyle(
                background = io.github.ronjunevaldoz.awake.core.colors.Color.Transparent,
                foreground = themeValues.colors.foreground,
                cornerRadius = themeValues.shapes.md,
                contentPadding = UiInsets(horizontal = 6f.dp, vertical = 4f.dp),
            ),
            hovered = SurfaceStyle(
                background = themeValues.colors.accent,
                foreground = themeValues.colors.accentForeground,
                cornerRadius = themeValues.shapes.md,
                contentPadding = UiInsets(horizontal = 6f.dp, vertical = 4f.dp),
            ),
        ),
    ) {
        row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = UiAlignment.Vertical.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            row(
                horizontalArrangement = Arrangement.spacedBy(8f.dp),
                verticalAlignment = UiAlignment.Vertical.Center,
            ) {
                surface(
                    id = "$id.avatar",
                    modifier = Modifier.size(32f.dp),
                    style = SurfaceStyle(
                        background = themeValues.colors.secondary,
                        foreground = themeValues.colors.secondaryForeground,
                        cornerRadius = 16f.dp,
                        contentPadding = UiInsets(6f.dp),
                    ),
                ) {
                    icon(ShadcnIcons.user)
                }
                column {
                    text(
                        label = name,
                        visuals = SurfaceStyle(
                            textSize = 14f.sp,
                            fontWeight = FontWeight.SemiBold,
                            foreground = themeValues.colors.foreground,
                        ),
                    )
                    text(
                        label = email,
                        visuals = SurfaceStyle(
                            textSize = 11f.sp,
                            foreground = themeValues.colors.mutedForeground,
                        ),
                    )
                }
            }
            icon(ShadcnIcons.chevronDown)
        }
    }
    if (clicked.clicked) onClick()
    return clicked.clicked
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
                visuals = SurfaceStyle(
                    foreground = themeValues.colors.mutedForeground,
                    // Sidebar group titles are Tailwind `text-xs` (12px) and `font-medium`.
                    textSize = 12f.sp,
                    fontWeight = FontWeight.Medium,
                    contentPadding = UiInsets(horizontal = 8f.dp, vertical = 0f.dp),
                ),
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
    val insets = UiInsets(horizontal = 8f.dp, vertical = 0f.dp)
    val clicked = buttonSlot(
        id = id,
        modifier = modifier.fillMaxWidth().height(ShadcnSidebarMetrics.menuButtonHeight),
        visuals = io.github.ronjunevaldoz.awake.ui.headless.SurfaceVisuals(
            rest = SurfaceStyle(
                background = if (active) themeValues.colors.secondary else io.github.ronjunevaldoz.awake.core.colors.Color.Transparent,
                foreground = if (active) themeValues.colors.secondaryForeground else themeValues.colors.foreground,
                cornerRadius = themeValues.shapes.md,
                contentPadding = insets,
            ),
            hovered = SurfaceStyle(
                background = themeValues.colors.accent,
                foreground = themeValues.colors.accentForeground,
                cornerRadius = themeValues.shapes.md,
                contentPadding = insets,
            ),
        ),
    ) {
        row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = UiAlignment.Vertical.Center,
            modifier = Modifier.fillMaxWidth(),
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
                    visuals = SurfaceStyle(
                        textSize = 14f.sp,
                        fontWeight = if (active) FontWeight.Medium else FontWeight.Normal,
                    ),
                )
            }
            if (badge != null) {
                text(
                    label = badge,
                    visuals = SurfaceStyle(
                        textSize = 11f.sp,
                        foreground = themeValues.colors.mutedForeground,
                    ),
                )
            }
        }
    }
    if (clicked.clicked) onClick()
    return clicked.clicked
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
    val insets = UiInsets(horizontal = 8f.dp, vertical = 0f.dp)
    val clicked = buttonSlot(
        id = id,
        modifier = modifier.fillMaxWidth().height(ShadcnSidebarMetrics.submenuButtonHeight),
        visuals = io.github.ronjunevaldoz.awake.ui.headless.SurfaceVisuals(
            rest = SurfaceStyle(
                background = if (active) themeValues.colors.secondary else io.github.ronjunevaldoz.awake.core.colors.Color.Transparent,
                foreground = if (active) themeValues.colors.secondaryForeground else themeValues.colors.foreground,
                cornerRadius = themeValues.shapes.md,
                contentPadding = insets,
            ),
            hovered = SurfaceStyle(
                background = themeValues.colors.accent,
                foreground = themeValues.colors.accentForeground,
                cornerRadius = themeValues.shapes.md,
                contentPadding = insets,
            ),
        ),
    ) {
        row(
            horizontalArrangement = Arrangement.spacedBy(8f.dp),
            verticalAlignment = UiAlignment.Vertical.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (icon != null) {
                icon(icon)
            }
            text(
                label = label,
                visuals = SurfaceStyle(
                    textSize = 13f.sp,
                    fontWeight = if (active) FontWeight.Medium else FontWeight.Normal,
                ),
            )
        }
    }
    if (clicked.clicked) onClick()
    return clicked.clicked
}
