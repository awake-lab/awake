// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.layouts

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiPrimitiveScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.canvas
import io.github.ronjunevaldoz.awake.ui.childColumn
import io.github.ronjunevaldoz.awake.ui.context.resolveMeasuredContentCached
import io.github.ronjunevaldoz.awake.ui.graphics.clip
import io.github.ronjunevaldoz.awake.ui.graphics.drawFillAndBorder
import io.github.ronjunevaldoz.awake.ui.layout.horizontalPx
import io.github.ronjunevaldoz.awake.ui.layout.inset
import io.github.ronjunevaldoz.awake.ui.layout.verticalPx
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.resolveClickable
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.scope.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.scope.fillWidthOrNull
import io.github.ronjunevaldoz.awake.ui.scope.recordSemantic
import io.github.ronjunevaldoz.awake.ui.scope.resolveStyle
import io.github.ronjunevaldoz.awake.ui.style.MutableStyleState
import io.github.ronjunevaldoz.awake.ui.style.ResolvedStyle
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle
import io.github.ronjunevaldoz.awake.ui.theme.UiDefaultTheme
import io.github.ronjunevaldoz.awake.ui.toPx

/**
 * Replaces the deleted `UiTheme.components.surface` ambient fallback -- a Panel-role column's
 * "does this resolve to a real surface" check ([hasResolvedVisuals] in Column.kt) and
 * [surfaceCore]'s own unset-field fallback both used to read that live off [io.github.ronjunevaldoz.awake.ui.context.LocalTheme],
 * so a themed app (e.g. shadcn) got its OWN card look here even though this call never asked for
 * one. Reproducing that per-theme value would mean `ui-core` reaching into a design-system module
 * it must not depend on, so this is fixed at [UiDefaultTheme]'s own neutral values instead --
 * exactly what [io.github.ronjunevaldoz.awake.ui.theme.CoreUiComponentStyles.surface] (also now
 * deleted) computed for the untouched, no-theme-pushed case. A themed app that wants its own
 * panel default still can -- by supplying an explicit `style`/`defaults`, same as every other
 * caller already does.
 */
internal val neutralSurfaceDefaults: Style = Style {
    background(UiDefaultTheme.colors.background)
    foreground(UiDefaultTheme.colors.foreground)
    contentPadding(8f.dp)
}

fun UiPrimitiveScope.surface(
    id: String,
    verticalArrangement: Arrangement = defaultArrangement(),
    style: Style = Style.Empty,
    modifier: UiModifier = Modifier,
    clipContent: Boolean = false,
    cacheKey: Any? = null,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds = smartColumn(
    id = id,
    gap = verticalArrangement.baseSpacingPx(),
    verticalArrangement = verticalArrangement,
    style = Style {
        shape(UiShape.md)
        borderWidth(UiShape.none)
    } then style,
    modifier = modifier,
    clipContent = clipContent,
    cacheKey = cacheKey,
    role = UiSemanticRole.Panel,
    content = content,
)

/**
 * Spans the parent's CROSS axis when the caller authored no size on it.
 *
 * Written once and called from the two scope overloads below rather than spelled out in each.
 * Duplicating this sentence per scope is exactly how the weight rule drifted -- RowScope.column
 * deferred a weighted child's main axis and ColumnScope.column did not, for months.
 *
 * The overloads themselves have to stay. Resolution is STATIC, so a call whose receiver is typed
 * UiPrimitiveScope deliberately gets no default even when the runtime instance happens to be a
 * ColumnScope. Collapsing them into one function with a `when (this)` looks equivalent and is not:
 * it hands a default to every such call site, which moved four suites (PanelTest, two signature
 * matrices, and the parity screenshots).
 */
private fun UiModifier.fillingCrossAxis(axis: CrossAxis): UiModifier = when (axis) {
    CrossAxis.Width -> width(widthDimension ?: Dimension.FillMax)
    CrossAxis.Height -> height(heightDimension ?: Dimension.FillMax)
}

private enum class CrossAxis { Width, Height }

fun ColumnScope.surface(
    id: String,
    verticalArrangement: Arrangement = defaultArrangement(),
    style: Style = Style.Empty,
    modifier: UiModifier = Modifier,
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds = (this as UiPrimitiveScope).surface(
    id = id,
    verticalArrangement = verticalArrangement,
    style = style,
    modifier = modifier.fillingCrossAxis(CrossAxis.Width),
    clipContent = clipContent,
    content = content,
)

fun RowScope.surface(
    id: String,
    verticalArrangement: Arrangement = defaultArrangement(),
    style: Style = Style.Empty,
    modifier: UiModifier = Modifier,
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds = (this as UiPrimitiveScope).surface(
    id = id,
    verticalArrangement = verticalArrangement,
    style = style,
    modifier = modifier.fillingCrossAxis(CrossAxis.Height),
    clipContent = clipContent,
    content = content,
)

/**
 * [surface] plus the two extra knobs an interactive widget composed on top of it needs -- kept
 * as a SEPARATE function, not new parameters on [surface] itself, because [surface] already has
 * another same-named overload (the plain, non-interactive `smartColumn`-based one, above) whose
 * optional-param set partially overlaps a common call shape (`Column.kt`'s
 * `resolveVisualSurface` used to call `surface(id, verticalArrangement, modifier, clipContent,
 * content)` -- no `style`, matching both). Adding params to a same-named low-level overload
 * once already produced a silent, wrong overload pick (not a compile error) that manifested as
 * an infinite WrapContent trial / OOM, caught only by the full test suite, not by inspection.
 * `resolveVisualSurface` now calls [surfaceCore] directly instead, so that ambiguous pair no
 * longer exists at all -- a new name carries zero of that risk for whatever needs this shape
 * next.
 *
 * @param semanticRole a composed-clickable surface (`Modifier.clickable(...)`) is semantically a
 * Button, a Checkbox, etc, not a generic Panel.
 * @param resolvedSlot the visual-container half of the Compose-style clickable+Surface split
 * (see the `awake-ui-authoring` skill's "4 independent pieces" note): a caller that already
 * claimed its own slot -- typically via `interact()`, the gesture/interaction-state piece --
 * hands it in here so this never claims a second one for the same widget. Necessary, not just
 * tidy: `row()`/`column()` `claimSlot()` mutates the cursor as a side effect, so `interact()`
 * then a slot-claiming call back to back without this would silently eat two cursor slots for
 * one widget.
 */
fun UiPrimitiveScope.interactiveSurface(
    id: String,
    verticalArrangement: Arrangement = defaultArrangement(),
    modifier: UiModifier = Modifier,
    clipContent: Boolean = false,
    cacheKey: Any? = null,
    semanticRole: UiSemanticRole = UiSemanticRole.Panel,
    resolvedSlot: UiBounds? = null,
    // surface()'s own baseline is neutralSurfaceDefaults (background/foreground/comfortable
    // content padding) -- correct for a panel, wrong for most interactive widgets built on top
    // of this (a plain surface's padding silently bled through a button that never asked for
    // it). Pass Style.Empty (or a widget-appropriate baseline) when the caller's own
    // `modifier.styleable(...)` already supplies a complete style. In practice every real
    // Headless caller of this function (see `headless.Surface.kt`'s `interactiveSurface`) already
    // does, so this default is never actually reached -- kept only so a direct `ui-core` caller
    // that skips supplying one still gets a sane baseline instead of an unstyled invisible panel.
    defaults: Style = neutralSurfaceDefaults,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds = surfaceCore(
    id = id,
    verticalArrangement = verticalArrangement,
    modifier = modifier,
    clipContent = clipContent,
    cacheKey = cacheKey,
    semanticRole = semanticRole,
    resolvedSlot = resolvedSlot,
    defaults = defaults,
    content = content,
)

internal fun UiPrimitiveScope.surfaceCore(
    id: String,
    verticalArrangement: Arrangement,
    modifier: UiModifier,
    clipContent: Boolean,
    cacheKey: Any?,
    semanticRole: UiSemanticRole,
    resolvedSlot: UiBounds?,
    // Load-bearing default, unlike interactiveSurface's own (see its doc) -- Column.kt's
    // resolveVisualSurface() (a plain `column()`/`surface()` call whose own style already
    // resolves a background/border/shape) calls this directly without overriding it, so every
    // such call in the app inherits neutralSurfaceDefaults for whatever it left unset.
    defaults: Style = neutralSurfaceDefaults,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds {
    val requestedWidth = modifier.widthDimension ?: Dimension.WrapContent
    // A FillMax width is "no opinion, fill whatever's available" -- it has no real intrinsic
    // width of its own for a bounded/real layout pass, but an ancestor sizing ITSELF from
    // WrapContent (see UiContext.wrapContentPass) needs exactly this surface's own natural
    // content width to hug around (e.g. a vertical shadcnButtonGroup's members are all
    // fillMaxWidth(), and the group must still wrap to the widest one). Report it for that one
    // trial only, by measuring content the same way a real WrapContent width already does below
    // -- a real Fixed width, or FillMax outside a wrap trial, is never touched. Mirrors
    // ui-headless's withIntrinsicLabelWidth, which applies the identical exception to a button's
    // label-only sizing pass -- see that function's doc.
    val reportsNaturalWidthDuringWrapTrial =
        resolvedSlot == null && requestedWidth == Dimension.FillMax && context.isWrapContentPassInternal()
    val width = if (reportsNaturalWidthDuringWrapTrial) Dimension.WrapContent else requestedWidth
    val height = modifier.heightDimension ?: Dimension.WrapContent
    val gap = verticalArrangement.baseSpacingPx()
    val effectiveStyle = modifier.styleable ?: Style.Empty
    val containerTag = modifier.testTag ?: id
    val hasWrapContent = resolvedSlot == null && (width == Dimension.WrapContent || height == Dimension.WrapContent)

    // Only perform early slot claim and hit test if we don't have WrapContent dimensions and the
    // caller didn't already claim one itself (resolvedSlot). WrapContent dimensions must be
    // measured first before claiming any slot.
    val (initialSlot, initialHovered) = when {
        resolvedSlot != null -> resolvedSlot to hitTest(resolvedSlot)
        !hasWrapContent -> {
            val slot = claimModifiedSlot(modifier.withSizeFallback(width, height))
            slot to hitTest(slot)
        }
        else -> null to false
    }

    // A surface's `foreground` IS its content colour (shadcn's bg-*/text-* pairing), so it has to
    // beat whatever colour was merely inherited. Style.resolve() seeds its builder FROM
    // LocalTextStyle, so `resolved.textStyle.color` is non-null the moment any ancestor
    // sets one -- testing it for null only ever succeeded at the top of the tree, and every nested
    // surface silently kept the ancestor's colour instead of its own. A Primary badge measured
    // 17:1 standalone and 1.1:1 inside a card, which is dark-on-dark. Comparing against the
    // inherited value distinguishes "declared on this surface" from "merely inherited", so an
    // explicit per-call text colour still wins.
    val inheritedTextColor = context.current(io.github.ronjunevaldoz.awake.ui.context.LocalTextStyle).color
    fun resolveVisuals(hovered: Boolean): Pair<ResolvedStyle, TextStyle> {
        val styleState = MutableStyleState(
            hovered = modifier.forceHover ?: hovered,
            active = modifier.forceActive ?: isActive(id),
            focused = modifier.forceFocus ?: context.isFocusedInternal(id),
        )
        val resolved = resolveStyle(
            style = effectiveStyle,
            defaults = defaults,
            state = styleState,
        )
        // The surface text style participates in measurement as well as painting. Without this,
        // compact surfaces such as badges are measured with the parent text metrics and then
        // drawn with their own caption metrics, producing clipped pills and a border that
        // collapses into a line. Keep the same resolved foreground propagation for both passes.
        val declaresOwnTextColor =
            resolved.textStyle.color != null && resolved.textStyle.color != inheritedTextColor
        val contentTextStyle = if (!declaresOwnTextColor && resolved.foreground != null) {
            resolved.textStyle.copy(color = resolved.foreground)
        } else {
            resolved.textStyle
        }
        return resolved to contentTextStyle
    }

    // hasWrapContent means no real slot exists yet -- initialHovered is a placeholder (always
    // false) rather than a real hit test, so resolving hover/active/focused against it here would
    // always paint the resting state (e.g. a hovered shadcn primary button never picking up its
    // `primary/90` hover fill). Good enough for sizing purposes (padding/border/text metrics
    // essentially never vary by hover), but not for the visuals actually painted below -- those
    // get re-resolved against the real slot once it exists. Mirrors
    // resolveInteractiveSurface/interact()'s claim-slot-then-hit-test order in ui-headless.
    var (resolved, contentTextStyle) = resolveVisuals(initialHovered)
    val paddingWidth = resolved.contentPadding.horizontalPx()
    val paddingHeight = resolved.contentPadding.verticalPx()
    val effectiveCacheKey = cacheKey ?: context.current(io.github.ronjunevaldoz.awake.ui.context.LocalCacheKey)
    val measured = if (hasWrapContent) {
        val maxContentWidth = when (width) {
            is Dimension.Fixed -> (width.dp.toPx() - paddingWidth).coerceAtLeast(0f)
            Dimension.FillMax -> (fillWidthOrNull()?.minus(paddingWidth))?.coerceAtLeast(0f) ?: 0f
            Dimension.WrapContent -> (fillWidthOrNull()?.minus(paddingWidth))?.coerceAtLeast(0f) ?: 4096f
        }
        context.pushLocal(io.github.ronjunevaldoz.awake.ui.context.LocalTextStyle, contentTextStyle)
        try {
            context.resolveMeasuredContentCached(
                id = id,
                cacheKey = effectiveCacheKey,
                availableWidth = maxContentWidth,
                gap = gap,
            ) {
                context.measureColumnContentInternal(
                    width = maxContentWidth,
                    gap = gap,
                    content = content,
                )
            }
        } finally {
            context.popLocal(io.github.ronjunevaldoz.awake.ui.context.LocalTextStyle)
        }
    } else {
        null
    }
    val resolvedWidth = when (width) {
        Dimension.WrapContent -> Dimension.Fixed((requireNotNull(measured).width + paddingWidth).px)
        else -> width
    }
    val resolvedHeight = when (height) {
        Dimension.WrapContent -> {
            Dimension.Fixed((requireNotNull(measured).height + paddingHeight).px)
        }
        else -> height
    }
    val slot = initialSlot ?: claimModifiedSlot(modifier.width(resolvedWidth).height(resolvedHeight))
    if (initialSlot == null) {
        // The slot above didn't exist when resolveVisuals(initialHovered) ran -- redo it now
        // against the real bounds so the painted/recorded state reflects the actual pointer
        // position instead of the sizing-pass placeholder.
        val (finalResolved, finalContentTextStyle) = resolveVisuals(hitTest(slot))
        resolved = finalResolved
        contentTextStyle = finalContentTextStyle
    }
    resolveClickable(id = id, slot = slot, modifier = modifier)
    resolved.shadow?.let { shadow ->
        emit(
            UiDrawPrimitive.ShadowQuad(
                x = slot.x,
                y = slot.y,
                w = slot.width,
                h = slot.height,
                radius = resolved.shape.toPx(),
                offsetX = shadow.offsetX.toPx(),
                offsetY = shadow.offsetY.toPx(),
                blurRadius = shadow.blurRadius.toPx(),
                spread = shadow.spread.toPx(),
                color = shadow.color,
                tokenId = shadow.tokenId,
            ),
        )
    }
    // Resolved here, on the surrounding UiPrimitiveScope, not inside canvas{} -- CanvasScope is
    // a pure draw surface with no theme/context access (Option B,
    // docs/tasks/2026-08-18-ui-capability-scopes-plan.md).
    val resolvedBorderColor = resolved.borderColor
        ?: context.current(io.github.ronjunevaldoz.awake.ui.context.LocalTheme).colors.border
    canvas(slot) {
        drawFillAndBorder(
            slot = slot,
            fillColor = resolved.background ?: Color.Transparent,
            radiusPx = resolved.shape.toPx(),
            borderWidth = resolved.borderWidth,
            borderColor = resolvedBorderColor,
            shapeSpec = resolved.shapeSpec,
            fillTokenId = resolved.backgroundToken,
            borderTokenId = resolved.borderColorToken,
        )
    }
    recordSemantic(
        role = semanticRole,
        id = id,
        bounds = slot,
        backgroundColor = resolved.background,
        backgroundToken = resolved.backgroundToken,
        foregroundColor = resolved.foreground,
        foregroundToken = resolved.foregroundToken,
        borderColor = resolved.borderColor,
        borderToken = resolved.borderColorToken,
        borderRadius = resolved.shape.toPx(),
    )
    // A surface's `foreground` is its content colour, so it has to reach the text inside it.
    // Without this, text() found no colour on the inherited text style and fell back to the
    // theme's own foreground -- which on an inverted surface is the same colour as the
    // background it is sitting on. shadcn's tooltip (bg-foreground/text-background) rendered as
    // an unreadable dark-on-dark pill because of it. An explicit textStyle colour still wins.
    context.pushLocal(io.github.ronjunevaldoz.awake.ui.context.LocalTextStyle, contentTextStyle)
    val effectiveShape = resolved.shapeSpec ?: UiShapeSpec.RoundedRectangle(resolved.shape)
    context.pushShapeSpec(effectiveShape)

    // A surface is a column that paints. It has to divide its height the same way a plain
    // column does -- without this it dropped every child weight, so whether a container
    // distributed space came down to whether it happened to have a background.
    val contentArrangement = Arrangement.spacedBy(gap.px)
    val contentInsets = resolved.contentPadding
    val plannedSlots = planWeightedColumnSlots(
        slot = slot.inset(contentInsets),
        arrangement = contentArrangement,
        content = content,
    )
    val contentScope = childColumn(
        slot,
        verticalArrangement = contentArrangement,
        modifier = UiModifier(insets = contentInsets, testTag = containerTag),
        hasBoundedFillWidth = width != Dimension.WrapContent,
        hasBoundedFillHeight = height != Dimension.WrapContent,
        plannedSlots = plannedSlots,
    )
    // Any non-zero shape/radius must clip its own children, not just round the background
    // paint -- otherwise square-cornered content (a badge, a button row) can visibly poke
    // out past the curve at large radii. `clipContent` remains available for callers that
    // want clipping on an otherwise-square surface (e.g. Dialog/DropdownMenu clip *and* have
    // a real corner radius, so this condition already covers them too).
    //
    // Matches UiPrimitiveScope.row()/UiPrimitiveScope.column()'s own `withMeasuredRecordingSuppressed { scope.
    // content(slot) }` (see the comment there) -- surface() is just as much a composite widget
    // as a plain row()/column(), so its own real children's claimSlot() calls must not leak
    // into an ancestor row/column's in-progress measuredSlots/measuredWeights/fillsMainAxis
    // trial when a fixed-width surface() (shadcnSidebar/shadcnSurface/shadcnCard, all route
    // through here) sits next to a weight()-tagged sibling that forces the ancestor into its
    // trial-measurement (hasWeightedChild/plannedSlots) path. Without this, real content inside
    // the surface (e.g. a `text()` call) adds an extra, spurious entry to those parallel lists,
    // desyncing resolveWeightedMainAxis()'s index pairing and -- worse -- the ancestor's
    // plannedSlots consumption order, silently handing later siblings the wrong slot.
    context.pushLocal(io.github.ronjunevaldoz.awake.ui.context.LocalCacheKey, effectiveCacheKey)
    try {
        context.withMeasuredRecordingSuppressed {
            if (clipContent || resolved.shape.toPx() > 0f || resolved.shapeSpec != null) {
                clip(effectiveShape, slot) { contentScope.content(slot) }
            } else {
                contentScope.content(slot)
            }
        }
    } finally {
        context.popLocal(io.github.ronjunevaldoz.awake.ui.context.LocalCacheKey)
    }
    context.popShapeSpec()
    context.popLocal(io.github.ronjunevaldoz.awake.ui.context.LocalTextStyle)
    return slot
}
