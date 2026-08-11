// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.headless.UiPopupDefaults
import io.github.ronjunevaldoz.awake.ui.api.UiPopupPositionProvider
import io.github.ronjunevaldoz.awake.ui.api.UiPopupProperties
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnResolvedTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnCardSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnCardVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnStyles
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnSurfaceVariant
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.spacer
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.popup
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.unstyled.separator

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
internal fun ColumnScope.shadcnCardContent(
    slot: UiBounds,
    size: ShadcnCardSize,
    header: (ColumnScope.() -> Unit)?,
    footer: (ColumnScope.() -> Unit)?,
    body: ColumnScope.(slot: UiBounds) -> Unit,
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
        emit(
            UiDrawPrimitive.Quad(
                slot.x,
                slot.y + slot.height,
                slot.width + offset,
                offset,
                shadowColor,
            ),
        )
        emit(
            UiDrawPrimitive.Quad(
                slot.x + slot.width,
                slot.y,
                offset,
                slot.height + offset,
                shadowColor,
            ),
        )
    }
}

/** Real shadcn's `Surface`: a contained region (Card, Popover, Dialog) that owns its
 * background, border, and content padding. Composed from the [surface] primitive. */
@Deprecated(
    message = "shadcn/ui does not define a Surface component. Use headless surface() from ui-layouts for headless container panels, or shadcnCard() for Card panels.",
    replaceWith = ReplaceWith(
        "surface(id = id, modifier = modifier, style = style, content = content)",
        "io.github.ronjunevaldoz.awake.ui.layouts.surface",
    ),
)
fun UiScope.shadcnSurface(
    id: String,
    modifier: UiModifier = Modifier,
    variant: ShadcnSurfaceVariant? = null,
    style: Style = Style.Empty,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds = surface(
    id = id,
    modifier = modifier,
    style = (
        if (variant == null) {
            theme.asShadcnTheme().components.surface
        } else {
            ShadcnStyles.surface(
                theme.asShadcnTheme(),
                variant,
            )
        }
        ) then style,
    content = { slot -> content(slot) },
)

@Deprecated(
    message = "shadcn/ui does not define a Surface component. Use headless surface() from ui-layouts for headless container panels, or shadcnCard() for Card panels.",
    replaceWith = ReplaceWith(
        "surface(id = id, modifier = modifier, style = style, content = content)",
        "io.github.ronjunevaldoz.awake.ui.layouts.surface",
    ),
)
fun ColumnScope.shadcnSurface(
    id: String,
    modifier: UiModifier = Modifier,
    variant: ShadcnSurfaceVariant? = null,
    style: Style = Style.Empty,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds = surface(
    id = id,
    modifier = modifier,
    style = (
        if (variant == null) {
            theme.asShadcnTheme().components.surface
        } else {
            ShadcnStyles.surface(
                theme.asShadcnTheme(),
                variant,
            )
        }
        ) then style,
    content = { slot -> content(slot) },
)

@Deprecated(
    message = "shadcn/ui does not define a Surface component. Use headless surface() from ui-layouts for headless container panels, or shadcnCard() for Card panels.",
    replaceWith = ReplaceWith(
        "surface(id = id, modifier = modifier, style = style, content = content)",
        "io.github.ronjunevaldoz.awake.ui.layouts.surface",
    ),
)
fun RowScope.shadcnSurface(
    id: String,
    modifier: UiModifier = Modifier,
    variant: ShadcnSurfaceVariant? = null,
    style: Style = Style.Empty,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds = surface(
    id = id,
    modifier = modifier,
    style = (
        if (variant == null) {
            theme.asShadcnTheme().components.surface
        } else {
            ShadcnStyles.surface(
                theme.asShadcnTheme(),
                variant,
            )
        }
        ) then style,
    content = { slot -> content(slot) },
)

@Deprecated(
    message = "shadcn/ui does not define a Surface component. Use headless surface() from ui-layouts for headless container panels, or shadcnCard() for Card panels.",
    replaceWith = ReplaceWith(
        "surface(id = id, modifier = modifier, style = style, content = content)",
        "io.github.ronjunevaldoz.awake.ui.layouts.surface",
    ),
)
fun BoxScope.shadcnSurface(
    id: String,
    modifier: UiModifier = Modifier,
    variant: ShadcnSurfaceVariant? = null,
    style: Style = Style.Empty,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds = surface(
    id = id,
    modifier = modifier,
    style = (
        if (variant == null) {
            theme.asShadcnTheme().components.surface
        } else {
            ShadcnStyles.surface(
                theme.asShadcnTheme(),
                variant,
            )
        }
        ) then style,
    content = { slot -> content(slot) },
)

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
    body: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds {
    val bounds = surface(
        id = id,
        modifier = modifier,
        style = theme.asShadcnTheme().components.surface then style,
        content = { slot -> shadcnCardContent(slot, size, header, footer, body) },
    )
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
    body: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds {
    val bounds = surface(
        id = id,
        modifier = modifier,
        style = theme.asShadcnTheme().components.surface then style,
        content = { slot -> shadcnCardContent(slot, size, header, footer, body) },
    )
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
    body: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds {
    val bounds = surface(
        id = id,
        modifier = modifier,
        style = theme.asShadcnTheme().components.surface then style,
        content = { slot -> shadcnCardContent(slot, size, header, footer, body) },
    )
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
    body: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds {
    val bounds = surface(
        id = id,
        modifier = modifier,
        style = theme.asShadcnTheme().components.surface then style,
        content = { slot -> shadcnCardContent(slot, size, header, footer, body) },
    )
    if (variant == ShadcnCardVariant.Elevated) emitCardElevationShadow(bounds)
    return bounds
}

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
    // Real shadcn's PopoverContent is p-4 (16dp), a deliberately smaller inset than
    // Card/Dialog's p-6 -- [ShadcnMetrics.surfacePadding] is popover's own field for exactly
    // this reason, not a reuse of [panelPadding].
    contentPadding(theme.metrics.surfacePadding)
}

/** Result of a [shadcnPopover] call: the resolved content slot (null while collapsed) and
 * whether this frame's interaction dismissed it. Mirrors [io.github.ronjunevaldoz.awake.ui.UiPopupResult] /
 * `UiDropdownMenuResult`'s shape -- a dedicated type per component rather than reusing the
 * primitive's result across design-system components. */
data class UiPopoverResult(
    val slot: UiBounds?,
    val dismissed: Boolean,
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
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiPopoverResult {
    val resolvedTheme = theme.asShadcnTheme()
    val popupResult = popup(
        id = id,
        anchorSlot = anchorSlot,
        expanded = expanded,
        width = width,
        height = height,
        positionProvider = positionProvider,
        properties = properties,
    ) { popupSlot ->
        surface(
            id = "$id.content",
            style = popoverStyle(resolvedTheme) then style,
            modifier = Modifier.width(Dimension.Fixed(popupSlot.width.px)).height(height),
            content = { slot -> content(slot) },
        )
    }
    return UiPopoverResult(slot = popupResult.slot, dismissed = popupResult.dismissed)
}
