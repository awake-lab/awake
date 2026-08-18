// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("MatchingDeclarationName") // ShadcnSidebarMetrics is a supporting type; the
// file's primary export is the shadcnSidebar* recipes below

package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.UiImageVector
import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnSidebarActionStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnSidebarAvatarStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnSidebarGroupLabelStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnSidebarHeaderBadgeStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnSidebarMenuItemStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnSidebarMenuLabelStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnSidebarStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnSidebarSubmenuLabelStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnSidebarSupportingTextStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnSidebarTitleStyle
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
import io.github.ronjunevaldoz.ui.heroicons.icon.HeroIcons

/**
 * Measurements from `new-york-v4/ui/sidebar.tsx` in the pinned shadcn-ui checkout.
 *
 * These are component-recipe values, not generic layout defaults. The showcase may choose its
 * own outer width and content, but a shadcn Sidebar's internal padding and menu rhythm must use
 * this one contract.
 */
internal object ShadcnSidebarMetrics {
    val contentPadding: Dp = 8f.dp
    val groupGap: Dp = 4f.dp
    val menuGap: Dp = 4f.dp
    val menuButtonHeight: Dp = 32f.dp
    val submenuIndent: Dp = 14f.dp
    val submenuGap: Dp = 2f.dp
    val submenuButtonHeight: Dp = 28f.dp
}

fun UiScope.shadcnSidebar(
    id: String,
    modifier: Modifier = Modifier,
    expanded: Boolean = true,
    header: (ColumnScope.() -> Unit)? = null,
    footer: (ColumnScope.() -> Unit)? = null,
    content: ColumnScope.(UiBounds) -> Unit,
): UiBounds = surface(id, modifier, shadcnSidebarStyle(themeValues)) {
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
        style = shadcnSidebarActionStyle(themeValues),
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
                    style = shadcnSidebarHeaderBadgeStyle(themeValues),
                ) {
                    icon(HeroIcons.Solid20Mini.squares2x2)
                }
                column {
                    text(
                        label = title,
                        style = shadcnSidebarTitleStyle(themeValues),
                    )
                    text(
                        label = subtitle,
                        style = shadcnSidebarSupportingTextStyle(themeValues),
                    )
                }
            }
            icon(HeroIcons.Solid20Mini.chevronDown)
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
        style = shadcnSidebarActionStyle(themeValues),
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
                    style = shadcnSidebarAvatarStyle(themeValues),
                ) {
                    icon(HeroIcons.Solid20Mini.user)
                }
                column {
                    text(
                        label = name,
                        style = shadcnSidebarTitleStyle(themeValues),
                    )
                    text(
                        label = email,
                        style = shadcnSidebarSupportingTextStyle(themeValues),
                    )
                }
            }
            icon(HeroIcons.Solid20Mini.chevronDown)
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
                style = shadcnSidebarGroupLabelStyle(themeValues),
            )
        }
        content()
    }
}

fun UiScope.shadcnSidebarMenu(
    modifier: Modifier = Modifier,
    // Opt-in cross-frame trial cache (see UiScope.column) -- menus are the deepest static
    // subtrees in a shell, so a stable key here removes their per-frame measure trials.
    id: String? = null,
    cacheKey: Any? = null,
    content: ColumnScope.() -> Unit,
) {
    column(
        id = id,
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ShadcnSidebarMetrics.menuGap),
        cacheKey = cacheKey,
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
        style = shadcnSidebarMenuItemStyle(themeValues, active),
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
                    style = shadcnSidebarMenuLabelStyle(active),
                )
            }
            if (badge != null) {
                text(
                    label = badge,
                    style = shadcnSidebarSupportingTextStyle(themeValues),
                )
            }
        }
    }
    if (clicked) onClick()
    return clicked
}

fun UiScope.shadcnSidebarMenuSub(
    modifier: Modifier = Modifier,
    id: String? = null,
    cacheKey: Any? = null,
    content: ColumnScope.() -> Unit,
) {
    row(modifier = modifier.fillMaxWidth().padding(start = ShadcnSidebarMetrics.submenuIndent)) {
        separator(
            color = themeValues.colors.border,
            orientation = UiSeparatorOrientation.Vertical,
        )
        column(
            id = id,
            modifier = Modifier.fillMaxWidth().padding(start = 6f.dp),
            verticalArrangement = Arrangement.spacedBy(ShadcnSidebarMetrics.submenuGap),
            cacheKey = cacheKey,
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
        style = shadcnSidebarMenuItemStyle(themeValues, active),
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
                style = shadcnSidebarSubmenuLabelStyle(active),
            )
        }
    }
    if (clicked) onClick()
    return clicked
}
