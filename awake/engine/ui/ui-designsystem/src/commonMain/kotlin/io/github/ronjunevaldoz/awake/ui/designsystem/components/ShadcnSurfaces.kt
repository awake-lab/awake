// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiPopupDefaults
import io.github.ronjunevaldoz.awake.ui.UiPopupPositionProvider
import io.github.ronjunevaldoz.awake.ui.UiPopupProperties
import io.github.ronjunevaldoz.awake.ui.popup
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnResolvedTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnStyles
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnSurfaceVariant
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.spacer
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.unstyled.separator
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

/** Inserts the header/footer separator convention shared with [DropdownMenu]'s
 * item separator: a thin border-colored rule with a small gap on both sides. */
private fun ColumnScope.shadcnCardDivider() {
    spacer(Modifier.height(4f.dp))
    separator(color = theme.tokens.border.withAlpha(0.72f))
    spacer(Modifier.height(4f.dp))
}

/** Composes the shared header -> body -> footer structure on top of an already-styled
 * [surface] content slot. Header/footer are optional; body is required. Matches real
 * shadcn's `CardHeader`/`CardContent`/`CardFooter` composition. */
private fun ColumnScope.shadcnCardContent(
    slot: UiBounds,
    header: (ColumnScope.() -> Unit)?,
    footer: (ColumnScope.() -> Unit)?,
    body: ColumnScope.(slot: UiBounds) -> Unit
) {
    if (header != null) {
        header()
        shadcnCardDivider()
    }
    body(slot)
    if (footer != null) {
        shadcnCardDivider()
        footer()
    }
}

/** Real shadcn's `Surface`: a contained region (Card, Popover, Dialog) that owns its
 * background, border, and content padding. Composed from the [surface] primitive. */
fun UiScope.shadcnSurface(
    id: String,
    modifier: UiModifier = Modifier,
    variant: ShadcnSurfaceVariant? = null,
    style: Style = Style.Empty,
    content: ColumnScope.(slot: UiBounds) -> Unit
): UiBounds = surface(
    id = id,
    modifier = modifier,
    style = (if (variant == null) theme.asShadcnTheme().components.surface else ShadcnStyles.surface(theme.asShadcnTheme(), variant)) then style,
    content = { slot -> content(slot.toBounds()) }
).toBounds()

/** [shadcnSurface] override for [ColumnScope]. */
fun ColumnScope.shadcnSurface(
    id: String,
    modifier: UiModifier = Modifier,
    variant: ShadcnSurfaceVariant? = null,
    style: Style = Style.Empty,
    content: ColumnScope.(slot: UiBounds) -> Unit
): UiBounds = surface(
    id = id,
    modifier = modifier,
    style = (if (variant == null) theme.asShadcnTheme().components.surface else ShadcnStyles.surface(theme.asShadcnTheme(), variant)) then style,
    content = { slot -> content(slot.toBounds()) }
).toBounds()

/** [shadcnSurface] override for [RowScope]. */
fun RowScope.shadcnSurface(
    id: String,
    modifier: UiModifier = Modifier,
    variant: ShadcnSurfaceVariant? = null,
    style: Style = Style.Empty,
    content: ColumnScope.(slot: UiBounds) -> Unit
): UiBounds = surface(
    id = id,
    modifier = modifier,
    style = (if (variant == null) theme.asShadcnTheme().components.surface else ShadcnStyles.surface(theme.asShadcnTheme(), variant)) then style,
    content = { slot -> content(slot.toBounds()) }
).toBounds()

/** [shadcnSurface] override for [BoxScope]. */
fun BoxScope.shadcnSurface(
    id: String,
    modifier: UiModifier = Modifier,
    variant: ShadcnSurfaceVariant? = null,
    style: Style = Style.Empty,
    content: ColumnScope.(slot: UiBounds) -> Unit
): UiBounds = surface(
    id = id,
    modifier = modifier,
    style = (if (variant == null) theme.asShadcnTheme().components.surface else ShadcnStyles.surface(theme.asShadcnTheme(), variant)) then style,
    content = { slot -> content(slot.toBounds()) }
).toBounds()

/** Real shadcn's `Card`: a dedicated header/body/footer composition, not just a
 * background/border flavor of [shadcnSurface]. Header and footer are optional slots
 * separated from the body by the shared divider convention (see [DropdownMenu]'s item
 * separator); body is required. Uses the base theme surface style directly -- it isn't
 * a [ShadcnSurfaceVariant] flavor, just a plain surface with header/body/footer structure. */
fun UiScope.shadcnCard(
    id: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    header: (ColumnScope.() -> Unit)? = null,
    footer: (ColumnScope.() -> Unit)? = null,
    body: ColumnScope.(slot: UiBounds) -> Unit
): UiBounds = surface(
    id = id,
    modifier = modifier,
    style = theme.asShadcnTheme().components.surface then style,
    content = { slot -> shadcnCardContent(slot.toBounds(), header, footer, body) }
).toBounds()

/** [shadcnCard] override for [ColumnScope]. */
fun ColumnScope.shadcnCard(
    id: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    header: (ColumnScope.() -> Unit)? = null,
    footer: (ColumnScope.() -> Unit)? = null,
    body: ColumnScope.(slot: UiBounds) -> Unit
): UiBounds = surface(
    id = id,
    modifier = modifier,
    style = theme.asShadcnTheme().components.surface then style,
    content = { slot -> shadcnCardContent(slot.toBounds(), header, footer, body) }
).toBounds()

/** [shadcnCard] override for [RowScope]. */
fun RowScope.shadcnCard(
    id: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    header: (ColumnScope.() -> Unit)? = null,
    footer: (ColumnScope.() -> Unit)? = null,
    body: ColumnScope.(slot: UiBounds) -> Unit
): UiBounds = surface(
    id = id,
    modifier = modifier,
    style = theme.asShadcnTheme().components.surface then style,
    content = { slot -> shadcnCardContent(slot.toBounds(), header, footer, body) }
).toBounds()

/** [shadcnCard] override for [BoxScope]. */
fun BoxScope.shadcnCard(
    id: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    header: (ColumnScope.() -> Unit)? = null,
    footer: (ColumnScope.() -> Unit)? = null,
    body: ColumnScope.(slot: UiBounds) -> Unit
): UiBounds = surface(
    id = id,
    modifier = modifier,
    style = theme.asShadcnTheme().components.surface then style,
    content = { slot -> shadcnCardContent(slot.toBounds(), header, footer, body) }
).toBounds()

/** Own visual style for real shadcn's `Sidebar`: the dedicated sidebar background/border
 * tokens, not routed through [ShadcnSurfaceVariant] -- same "no shared enum" call as
 * [shadcnCard]'s `components.surface`. */
private fun sidebarStyle(theme: ShadcnResolvedTheme): Style = Style {
    background(theme.sidebar)
    foreground(theme.onSidebar)
    borderWidth(1f.dp)
    borderColor(theme.sidebarBorder)
    shape(theme.radii.xl)
    contentPadding(theme.metrics.surfacePadding)
}

/** Real shadcn's `Sidebar`: a navigation shell with optional header/footer slots around a
 * required, typically scrollable nav [content] area (see `SidebarHeader`/`SidebarContent`/
 * `SidebarFooter`). Header/footer share the same divider convention as [shadcnCard]. Scrolling
 * is wired the same way every other surface in this module wires it: apply
 * `Modifier.verticalScroll(state)` to [modifier], there's no bespoke scroll plumbing here. */
fun UiScope.shadcnSidebar(
    id: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    header: (ColumnScope.() -> Unit)? = null,
    footer: (ColumnScope.() -> Unit)? = null,
    content: ColumnScope.(slot: UiBounds) -> Unit
): UiBounds = surface(
    id = id,
    modifier = modifier,
    style = sidebarStyle(theme.asShadcnTheme()) then style,
    content = { slot -> shadcnCardContent(slot.toBounds(), header, footer, content) }
).toBounds()

/** [shadcnSidebar] override for [ColumnScope]. */
fun ColumnScope.shadcnSidebar(
    id: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    header: (ColumnScope.() -> Unit)? = null,
    footer: (ColumnScope.() -> Unit)? = null,
    content: ColumnScope.(slot: UiBounds) -> Unit
): UiBounds = surface(
    id = id,
    modifier = modifier,
    style = sidebarStyle(theme.asShadcnTheme()) then style,
    content = { slot -> shadcnCardContent(slot.toBounds(), header, footer, content) }
).toBounds()

/** [shadcnSidebar] override for [RowScope]. */
fun RowScope.shadcnSidebar(
    id: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    header: (ColumnScope.() -> Unit)? = null,
    footer: (ColumnScope.() -> Unit)? = null,
    content: ColumnScope.(slot: UiBounds) -> Unit
): UiBounds = surface(
    id = id,
    modifier = modifier,
    style = sidebarStyle(theme.asShadcnTheme()) then style,
    content = { slot -> shadcnCardContent(slot.toBounds(), header, footer, content) }
).toBounds()

/** [shadcnSidebar] override for [BoxScope]. */
fun BoxScope.shadcnSidebar(
    id: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    header: (ColumnScope.() -> Unit)? = null,
    footer: (ColumnScope.() -> Unit)? = null,
    content: ColumnScope.(slot: UiBounds) -> Unit
): UiBounds = surface(
    id = id,
    modifier = modifier,
    style = sidebarStyle(theme.asShadcnTheme()) then style,
    content = { slot -> shadcnCardContent(slot.toBounds(), header, footer, content) }
).toBounds()

/** Own visual style for real shadcn's `Popover` panel chrome -- the dedicated popover
 * background/border tokens pulled straight off [ShadcnResolvedTheme], not routed through
 * [ShadcnSurfaceVariant] -- same "no shared enum" call as [shadcnCard]'s `components.surface`
 * and [shadcnSidebar]'s [sidebarStyle]. Mirrors what `ShadcnSurfaceVariant.Popover` used to
 * resolve to before this extraction. */
internal fun popoverStyle(theme: ShadcnResolvedTheme): Style = Style {
    background(theme.popover)
    foreground(theme.onPopover)
    borderWidth(1f.dp)
    borderColor(theme.tokens.border)
    shape(theme.radii.xl)
    contentPadding(theme.metrics.panelPadding)
}

/** Result of a [shadcnPopover] call: the resolved content slot (null while collapsed) and
 * whether this frame's interaction dismissed it. Mirrors [io.github.ronjunevaldoz.awake.ui.UiPopupResult] /
 * `UiDropdownMenuResult`'s shape -- a dedicated type per component rather than reusing the
 * primitive's result across design-system components. */
data class UiPopoverResult(
    val slot: UiBounds?,
    val dismissed: Boolean
)

/** Real shadcn's `Popover`: a trigger-anchored floating panel (Radix's `Popover.Content`),
 * not a plain background/border flavor of [shadcnSurface]. Awake's immediate-mode model
 * already has the caller render its own trigger widget and own the `expanded` state -- same
 * split [shadcnDropdownMenu] already uses -- so this only owns anchored positioning, dismiss,
 * and the popover's own panel chrome; it never renders a trigger itself. Built directly on
 * the [popup] primitive that already implements anchoring/positioning/dismiss, with
 * [UiPopupDefaults.popover] as the default placement (centered under the anchor, matching
 * Radix/shadcn's `side="bottom" align="center"`). Only a [UiScope] overload exists -- like
 * [shadcnDropdownMenu], this is driven by an external [anchorSlot] rather than composed
 * inline in a content scope, so there's nothing for Column/Row/Box overloads to add. */
fun UiScope.shadcnPopover(
    id: String,
    anchorSlot: UiBounds,
    expanded: Boolean,
    width: Dimension = Dimension.WrapContent,
    height: Dimension = Dimension.WrapContent,
    positionProvider: UiPopupPositionProvider = UiPopupDefaults.popover(),
    properties: UiPopupProperties = UiPopupProperties(),
    style: Style = Style.Empty,
    content: ColumnScope.(slot: UiBounds) -> Unit
): UiPopoverResult {
    val resolvedTheme = theme.asShadcnTheme()
    val popupResult = popup(
        anchorSlot = anchorSlot,
        expanded = expanded,
        width = width,
        height = height,
        positionProvider = positionProvider,
        properties = properties
    ) { popupSlot ->
        surface(
            id = "$id.content",
            style = popoverStyle(resolvedTheme) then style,
            modifier = Modifier.width(Dimension.Fixed(popupSlot.width.px)).height(height),
            content = { slot -> content(slot.toBounds()) }
        )
    }
    return UiPopoverResult(slot = popupResult.slot?.toBounds(), dismissed = popupResult.dismissed)
}
