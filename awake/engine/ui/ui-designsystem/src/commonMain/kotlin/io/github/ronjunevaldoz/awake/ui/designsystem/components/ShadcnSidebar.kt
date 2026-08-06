// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.animateFloat
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnResolvedTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnCardSize
import io.github.ronjunevaldoz.awake.ui.designsystem.theme.ShadcnSpacing
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layout.toBounds
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.layouts.spacer
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.padding
import io.github.ronjunevaldoz.awake.ui.modifier.weight
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.style.*
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.unstyled.SeparatorOrientation
import io.github.ronjunevaldoz.awake.ui.unstyled.separator
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextOverflow

/** Visual style for real shadcn's `Sidebar`: dedicated sidebar background/border tokens.
 * Content padding is 8dp (p-2 spec) matching real shadcn sidebar layout. */
private fun sidebarStyle(theme: ShadcnResolvedTheme): Style = Style {
    background(theme.sidebar, tokenId = "sidebar")
    foreground(theme.onSidebar, tokenId = "sidebar-foreground")
    borderWidth(1f.dp)
    borderColor(theme.sidebarBorder, tokenId = "sidebar-border")
    contentPadding(8f.dp)
}

/** Target width (dp) [shadcnSidebar] animates toward when [expanded]. */
private fun UiModifier.sidebarExpandedWidth(): Dp = (widthDimension as? Dimension.Fixed)?.dp ?: 240f.dp

/** Animates [shadcnSidebar]'s width toward 0 when collapsed. */
private fun UiScope.animatedSidebarWidthModifier(id: String, modifier: UiModifier, expanded: Boolean): UiModifier {
    val fullWidth = modifier.sidebarExpandedWidth()
    val animatedWidth = animateFloat("$id.expanded", if (expanded) fullWidth.value else 0f, initial = fullWidth.value)
    return modifier.width(Dimension.Fixed(Dp(animatedWidth)))
}

/** Real shadcn's `Sidebar`: a navigation shell with optional header/footer slots around a nav content area. */
fun UiScope.shadcnSidebar(
    id: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    expanded: Boolean = true,
    header: (ColumnScope.() -> Unit)? = null,
    footer: (ColumnScope.() -> Unit)? = null,
    content: ColumnScope.(slot: UiBounds) -> Unit
): UiBounds = surface(
    id = id,
    modifier = animatedSidebarWidthModifier(id, modifier, expanded),
    style = sidebarStyle(theme.asShadcnTheme()) then style,
    content = { slot -> if (expanded) shadcnCardContent(slot.toBounds(), ShadcnCardSize.Default, header, footer, content) }
).toBounds()

/** [shadcnSidebar] override for [ColumnScope]. */
fun ColumnScope.shadcnSidebar(
    id: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    expanded: Boolean = true,
    header: (ColumnScope.() -> Unit)? = null,
    footer: (ColumnScope.() -> Unit)? = null,
    content: ColumnScope.(slot: UiBounds) -> Unit
): UiBounds = surface(
    id = id,
    modifier = animatedSidebarWidthModifier(id, modifier, expanded),
    style = sidebarStyle(theme.asShadcnTheme()) then style,
    content = { slot -> if (expanded) shadcnCardContent(slot.toBounds(), ShadcnCardSize.Default, header, footer, content) }
).toBounds()

/** [shadcnSidebar] override for [RowScope]. */
fun RowScope.shadcnSidebar(
    id: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    expanded: Boolean = true,
    header: (ColumnScope.() -> Unit)? = null,
    footer: (ColumnScope.() -> Unit)? = null,
    content: ColumnScope.(slot: UiBounds) -> Unit
): UiBounds = surface(
    id = id,
    modifier = animatedSidebarWidthModifier(id, modifier, expanded),
    style = sidebarStyle(theme.asShadcnTheme()) then style,
    content = { slot -> if (expanded) shadcnCardContent(slot.toBounds(), ShadcnCardSize.Default, header, footer, content) }
).toBounds()

/** [shadcnSidebar] override for [BoxScope]. */
fun BoxScope.shadcnSidebar(
    id: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    expanded: Boolean = true,
    header: (ColumnScope.() -> Unit)? = null,
    footer: (ColumnScope.() -> Unit)? = null,
    content: ColumnScope.(slot: UiBounds) -> Unit
): UiBounds = surface(
    id = id,
    modifier = animatedSidebarWidthModifier(id, modifier, expanded),
    style = sidebarStyle(theme.asShadcnTheme()) then style,
    content = { slot -> if (expanded) shadcnCardContent(slot.toBounds(), ShadcnCardSize.Default, header, footer, content) }
).toBounds()

/** A labeled section within a [shadcnSidebar]'s body, matching real shadcn's `SidebarGroup`. */
fun ColumnScope.shadcnSidebarGroup(
    modifier: UiModifier = Modifier,
    label: String? = null,
    content: ColumnScope.() -> Unit
) {
    column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(ShadcnSpacing.xs)) {
        if (label != null) {
            shadcnSupportingText(label)
        }
        content()
    }
}

/** A vertical list container for [shadcnSidebarMenuItem] entries, matching real shadcn's `SidebarMenu`. */
fun ColumnScope.shadcnSidebarMenu(
    modifier: UiModifier = Modifier,
    content: ColumnScope.() -> Unit
) {
    column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(ShadcnSpacing.xs)) {
        content()
    }
}

/** One clickable destination inside a [shadcnSidebarMenu], matching real shadcn's `SidebarMenuItem`.
 * Items apply 8dp horizontal padding (`px-2` spec) and 32dp height (`h-8` spec). */
fun ColumnScope.shadcnSidebarMenuItem(
    id: String,
    label: String,
    active: Boolean = false,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    badge: String? = null,
    onClick: () -> Unit = {}
): Boolean {
    val resolvedTheme = theme.asShadcnTheme()
    return shadcnButton(
        id = id,
        modifier = modifier.fillMaxWidth().height(32f.dp),
        variant = if (active) ShadcnButtonVariant.Primary else ShadcnButtonVariant.Ghost,
        style = (
            Style {
                contentPadding(horizontal = 8f.dp, vertical = 0f.dp)
                if (active) {
                    background(resolvedTheme.sidebarAccent)
                    foreground(resolvedTheme.onSidebarAccent)
                }
            }
        ) then style,
        centered = false,
        verticallyCentered = true,
        onClick = onClick
    ) { slot ->
        row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = UiAlignment.Vertical.Center,
            modifier = Modifier.width(Dimension.FillMax).height(Dimension.Fixed(slot.height.dp))
        ) {
            shadcnText(
                label,
                modifier = Modifier.weight(1f),
                muted = !active,
                maxLines = 1,
                overflow = UiTextOverflow.Ellipsis
            )
            badge?.let { shadcnBadge(it) }
        }
    }
}

/** Indented sub-menu tree container matching real shadcn's `SidebarMenuSub`.
 * Renders a left vertical border line (`border-l border-sidebar-border`) with left padding (`pl-3.5`). */
fun ColumnScope.shadcnSidebarMenuSub(
    modifier: UiModifier = Modifier,
    content: ColumnScope.() -> Unit
) {
    val palette = theme.asShadcnTheme().palette
    row(
        modifier = modifier.fillMaxWidth().padding(start = 14f.dp, top = 0f.dp, end = 0f.dp, bottom = 0f.dp)
    ) {
        separator(
            thickness = 1f.dp,
            color = palette.border.withAlpha(0.7f),
            modifier = Modifier.width(1f.dp).height(Dimension.FillMax),
            orientation = SeparatorOrientation.Vertical
        )
        spacer(Modifier.width(8f.dp))
        column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2f.dp)
        ) {
            content()
        }
    }
}

/** Individual sub-menu destination item matching real shadcn's `SidebarMenuSubItem`. */
fun ColumnScope.shadcnSidebarMenuSubItem(
    id: String,
    label: String,
    active: Boolean = false,
    modifier: UiModifier = Modifier,
    onClick: () -> Unit = {}
): Boolean = shadcnButton(
    id = id,
    label = label,
    modifier = modifier.fillMaxWidth().height(28f.dp),
    variant = if (active) ShadcnButtonVariant.Primary else ShadcnButtonVariant.Ghost,
    style = Style {
        contentPadding(horizontal = 8f.dp, vertical = 0f.dp)
        if (active) {
            background(theme.asShadcnTheme().sidebarAccent)
            foreground(theme.asShadcnTheme().onSidebarAccent)
        }
    },
    centered = false,
    verticallyCentered = true,
    onClick = onClick
)
