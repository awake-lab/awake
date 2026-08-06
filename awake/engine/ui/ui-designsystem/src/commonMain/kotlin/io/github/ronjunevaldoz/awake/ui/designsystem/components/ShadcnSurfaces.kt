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
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnCardSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnCardVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnStyles
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnSurfaceVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.theme.ShadcnSpacing
import io.github.ronjunevaldoz.awake.ui.animateFloat
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.layouts.spacer
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.padding
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.weight
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.unstyled.separator
import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

/** Inserts the header/footer separator convention shared with [DropdownMenu]'s
 * item separator: a thin border-colored rule with a small gap on both sides,
 * [gap] sized by [ShadcnCardSize]. */
private fun ColumnScope.shadcnCardDivider(gap: Dp) {
    spacer(Modifier.height(gap))
    separator(color = theme.colors.border.withAlpha(0.72f))
    spacer(Modifier.height(gap))
}

/** Composes the shared header -> body -> footer structure on top of an already-styled
 * [surface] content slot. Header/footer are optional; body is required. Matches real
 * shadcn's `CardHeader`/`CardContent`/`CardFooter` composition. */
private fun ColumnScope.shadcnCardContent(
    slot: UiBounds,
    size: ShadcnCardSize,
    header: (ColumnScope.() -> Unit)?,
    footer: (ColumnScope.() -> Unit)?,
    body: ColumnScope.(slot: UiBounds) -> Unit
) {
    val gap = size.dividerGapDp.dp
    if (header != null) {
        header()
        shadcnCardDivider(gap)
    }
    body(slot)
    if (footer != null) {
        shadcnCardDivider(gap)
        footer()
    }
}

/**
 * Awake's renderer has no dedicated blur primitive (grepped `ui-core`/`ui-designsystem` and both
 * the Vulkan and WebGPU backends: zero blur/gaussian shader hits) -- a real soft-edged drop
 * shadow needs an offscreen blur pass, out of scope for a Card variant flag. This model is also
 * strictly immediate-mode -- [UiScope.emit] appends straight to the frame's primitive list with
 * no z-order/re-sort step (see [UiContext.emitInternal]), so a shadow can never paint "behind" a
 * rect that's already been emitted; it can only ever be emitted BEFORE and kept entirely outside
 * that rect. [shadcnCard] calls this only after its own [surface] has already emitted, so the
 * shadow bands below are laid out strictly outside [slot] (bottom + right edges only, like a
 * dropped shadow peeking out from behind a raised card) -- multiple bands at increasing offset
 * and decreasing alpha fake a soft gradient falloff via banding, a real visual upgrade over the
 * single flat strip this replaces without needing an offscreen blur pass or emission reordering.
 * Upgrade path: a real blurred-quad draw primitive backed by an offscreen blur pass, threaded
 * through every backend's UI shader, if a soft (not banded) falloff is ever needed.
 */
private fun UiScope.emitCardElevationShadow(slot: UiBounds) {
    val bands = listOf(2f.dp.toPx() to 0.20f, 4f.dp.toPx() to 0.13f, 6f.dp.toPx() to 0.07f)
    for ((offset, alpha) in bands) {
        val shadowColor = Color.Black.withAlpha(alpha)
        emit(UiDrawPrimitive.Quad(slot.x, slot.y + slot.height, slot.width + offset, offset, shadowColor))
        emit(UiDrawPrimitive.Quad(slot.x + slot.width, slot.y, offset, slot.height + offset, shadowColor))
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
 * a [ShadcnSurfaceVariant] flavor, just a plain surface with header/body/footer structure.
 * [variant] adds [ShadcnCardVariant.Elevated]'s shadow (see [emitCardElevationShadow] for why
 * it's a scoped-down approximation); [size] controls header/footer divider spacing. */
fun UiScope.shadcnCard(
    id: String,
    modifier: UiModifier = Modifier,
    variant: ShadcnCardVariant = ShadcnCardVariant.Default,
    size: ShadcnCardSize = ShadcnCardSize.Default,
    style: Style = Style.Empty,
    header: (ColumnScope.() -> Unit)? = null,
    footer: (ColumnScope.() -> Unit)? = null,
    body: ColumnScope.(slot: UiBounds) -> Unit
): UiBounds {
    val bounds = surface(
        id = id,
        modifier = modifier,
        style = theme.asShadcnTheme().components.surface then style,
        content = { slot -> shadcnCardContent(slot.toBounds(), size, header, footer, body) }
    ).toBounds()
    if (variant == ShadcnCardVariant.Elevated) emitCardElevationShadow(bounds)
    return bounds
}

/** [shadcnCard] override for [ColumnScope]. */
fun ColumnScope.shadcnCard(
    id: String,
    modifier: UiModifier = Modifier,
    variant: ShadcnCardVariant = ShadcnCardVariant.Default,
    size: ShadcnCardSize = ShadcnCardSize.Default,
    style: Style = Style.Empty,
    header: (ColumnScope.() -> Unit)? = null,
    footer: (ColumnScope.() -> Unit)? = null,
    body: ColumnScope.(slot: UiBounds) -> Unit
): UiBounds {
    val bounds = surface(
        id = id,
        modifier = modifier,
        style = theme.asShadcnTheme().components.surface then style,
        content = { slot -> shadcnCardContent(slot.toBounds(), size, header, footer, body) }
    ).toBounds()
    if (variant == ShadcnCardVariant.Elevated) emitCardElevationShadow(bounds)
    return bounds
}

/** [shadcnCard] override for [RowScope]. */
fun RowScope.shadcnCard(
    id: String,
    modifier: UiModifier = Modifier,
    variant: ShadcnCardVariant = ShadcnCardVariant.Default,
    size: ShadcnCardSize = ShadcnCardSize.Default,
    style: Style = Style.Empty,
    header: (ColumnScope.() -> Unit)? = null,
    footer: (ColumnScope.() -> Unit)? = null,
    body: ColumnScope.(slot: UiBounds) -> Unit
): UiBounds {
    val bounds = surface(
        id = id,
        modifier = modifier,
        style = theme.asShadcnTheme().components.surface then style,
        content = { slot -> shadcnCardContent(slot.toBounds(), size, header, footer, body) }
    ).toBounds()
    if (variant == ShadcnCardVariant.Elevated) emitCardElevationShadow(bounds)
    return bounds
}

/** [shadcnCard] override for [BoxScope]. */
fun BoxScope.shadcnCard(
    id: String,
    modifier: UiModifier = Modifier,
    variant: ShadcnCardVariant = ShadcnCardVariant.Default,
    size: ShadcnCardSize = ShadcnCardSize.Default,
    style: Style = Style.Empty,
    header: (ColumnScope.() -> Unit)? = null,
    footer: (ColumnScope.() -> Unit)? = null,
    body: ColumnScope.(slot: UiBounds) -> Unit
): UiBounds {
    val bounds = surface(
        id = id,
        modifier = modifier,
        style = theme.asShadcnTheme().components.surface then style,
        content = { slot -> shadcnCardContent(slot.toBounds(), size, header, footer, body) }
    ).toBounds()
    if (variant == ShadcnCardVariant.Elevated) emitCardElevationShadow(bounds)
    return bounds
}

/** Own visual style for real shadcn's `Sidebar`: the dedicated sidebar background/border
 * tokens, not routed through [ShadcnSurfaceVariant] -- same "no shared enum" call as
 * [shadcnCard]'s `components.surface`. Real shadcn's Sidebar has no shape/clip at all (a flush
 * docked rail, confirmed against `docs/previews/sidebar_expanded_light.png` in the reference
 * clone) -- unlike [shadcnCard]/[shadcnSurface]'s `radii.xl`, this deliberately applies no
 * `shape(...)`. A caller that wants a rounded floating-card look (e.g. `samples/starter-game`'s
 * top-left nav rail, which isn't a docked full-height sidebar) opts in via its own `style`. */
private fun sidebarStyle(theme: ShadcnResolvedTheme): Style = Style {
    background(theme.sidebar, tokenId = "sidebar")
    foreground(theme.onSidebar, tokenId = "sidebar-foreground")
    borderWidth(1f.dp)
    borderColor(theme.sidebarBorder, tokenId = "sidebar-border")
    contentPadding(theme.metrics.surfacePadding)
}

/** Target width (dp) [shadcnSidebar] animates toward when [expanded]. Read off the caller's own
 * `modifier.width(...)` so there's no second `width`-shaped param duplicating what `UiModifier`
 * already expresses (see `docs/reference/ui-ownership.md`'s modifier-first rule) -- falls back to
 * shadcn's own 240dp default only when the caller left width unset. */
private fun UiModifier.sidebarExpandedWidth(): Dp = (widthDimension as? Dimension.Fixed)?.dp ?: 240f.dp

/** Animates [shadcnSidebar]'s width toward 0 when collapsed, matching real shadcn's
 * `animateDpAsState(if (expanded) width else 0.dp)`. Content itself is gated on the raw
 * [expanded] boolean (not the animated width) the same way real shadcn's `Box { if
 * (state.expanded) Column(...) }` does -- content disappears immediately on toggle while the
 * rail keeps animating shut, rather than laying out at fractional widths mid-animation. */
private fun UiScope.animatedSidebarWidthModifier(id: String, modifier: UiModifier, expanded: Boolean): UiModifier {
    val fullWidth = modifier.sidebarExpandedWidth()
    val animatedWidth = animateFloat("$id.expanded", if (expanded) fullWidth.value else 0f, initial = fullWidth.value)
    return modifier.width(Dimension.Fixed(Dp(animatedWidth)))
}

/** Real shadcn's `Sidebar`: a navigation shell with optional header/footer slots around a
 * required, typically scrollable nav [content] area (see `SidebarHeader`/`SidebarContent`/
 * `SidebarFooter`). Header/footer share the same divider convention as [shadcnCard]. Scrolling
 * is wired the same way every other surface in this module wires it: apply
 * `Modifier.verticalScroll(state)` to [modifier], there's no bespoke scroll plumbing here.
 *
 * [expanded] matches real shadcn's `SidebarProvider`/`useSidebar()` expand/collapse state, but
 * adapted to Awake's own idiom instead of a ported Compose `CompositionLocal` provider (Awake's
 * UI stack has no `CompositionLocal` equivalent -- see [shadcnCollapsible] for the same
 * caller-owned-state shape already used elsewhere in this module): the caller hoists a plain
 * `Boolean` (however it likes -- `rememberStateValue`, a view-model field, ...) and toggles it
 * from any ordinary `shadcnButton` onClick; there is no dedicated trigger/provider composable
 * here because a plain button already covers it. */
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

/** A labeled section within a [shadcnSidebar]'s body, matching real shadcn's `SidebarGroup`/
 * `SidebarGroupLabel`. Real shadcn's `SidebarGroup` doesn't itself collapse -- per-category
 * collapse (what `samples/ui-showcase`'s nav needs) stays layered on top via the existing
 * [shadcnCollapsible] wrapping this, rather than a second collapse mechanism baked in here. */
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

/** A vertical list container for [shadcnSidebarMenuItem] entries, matching real shadcn's
 * `SidebarMenu`. Callers add items as ordinary [shadcnSidebarMenuItem] calls inside [content]
 * rather than a fixed items-list param, so mixed rendering (e.g. this session's ui-showcase nav
 * nesting a whole group inside a [shadcnCollapsible]) stays expressible. */
fun ColumnScope.shadcnSidebarMenu(
    modifier: UiModifier = Modifier,
    content: ColumnScope.() -> Unit
) {
    column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(ShadcnSpacing.xs)) {
        content()
    }
}

/** One clickable destination inside a [shadcnSidebarMenu], matching real shadcn's
 * `SidebarMenuItem`. Built on [shadcnButton] rather than a bespoke clickable row -- same
 * "reuse the existing widget" call as [shadcnCollapsible]'s trigger convenience wrapper. [active]
 * highlights using the dedicated [ShadcnResolvedTheme.sidebarAccent]/[ShadcnResolvedTheme.onSidebarAccent]
 * tokens (not the generic `primary`/`accent` tokens shadcn's other selected states use) --
 * matching real shadcn's own separate sidebar-accent palette entry. [badge] renders as a
 * trailing [shadcnBadge], e.g. "New". Returns true if clicked this frame. */
fun ColumnScope.shadcnSidebarMenuItem(
    id: String,
    label: String,
    active: Boolean,
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
            if (active) {
                Style {
                    background(resolvedTheme.sidebarAccent)
                    foreground(resolvedTheme.onSidebarAccent)
                }
            } else {
                Style.Empty
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
            modifier = Modifier.width(1f.dp).height(Dimension.FillMax)
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
        if (active) {
            background(theme.asShadcnTheme().sidebarAccent)
            foreground(theme.asShadcnTheme().onSidebarAccent)
        }
        contentPadding(horizontal = 8f.dp, vertical = 0f.dp)
    },
    centered = false,
    verticallyCentered = true,
    onClick = onClick
)

/** Own visual style for real shadcn's `Popover` panel chrome -- the dedicated popover
 * background/border tokens pulled straight off [ShadcnResolvedTheme], not routed through
 * [ShadcnSurfaceVariant] -- same "no shared enum" call as [shadcnCard]'s `components.surface`
 * and [shadcnSidebar]'s [sidebarStyle]. Mirrors what `ShadcnSurfaceVariant.Popover` used to
 * resolve to before this extraction. */
internal fun popoverStyle(theme: ShadcnResolvedTheme): Style = Style {
    background(theme.popover)
    foreground(theme.onPopover)
    borderWidth(1f.dp)
    borderColor(theme.colors.border)
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
        id = id,
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
