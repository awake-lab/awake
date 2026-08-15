// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.layouts

import io.github.ronjunevaldoz.awake.ui.UiPrimitiveScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.childColumn
import io.github.ronjunevaldoz.awake.ui.context.UiMeasuredContent
import io.github.ronjunevaldoz.awake.ui.context.resolveHasWeightedChild
import io.github.ronjunevaldoz.awake.ui.layout.horizontalPx
import io.github.ronjunevaldoz.awake.ui.layout.verticalPx
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.styleable
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.scope.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.scope.fillWidthOrNull
import io.github.ronjunevaldoz.awake.ui.scope.recordSemantic
import io.github.ronjunevaldoz.awake.ui.scope.resolveStyle
import io.github.ronjunevaldoz.awake.ui.scrollPanel
import io.github.ronjunevaldoz.awake.ui.style.MutableStyleState
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.toPx

/**
 * Dispatches [column] and [surface] to one of three explicit container strategies, chosen by
 * inspecting [modifier]. This is the one place that decides which strategy applies -- each
 * strategy itself is a plain, linearly-readable function, not a branch buried in a larger one.
 */
internal fun UiPrimitiveScope.smartColumn(
    id: String?,
    gap: Float,
    verticalArrangement: Arrangement,
    style: Style,
    modifier: UiModifier,
    clipContent: Boolean = false,
    role: UiSemanticRole = UiSemanticRole.None,
    // ponytail: only the plain-measured-column strategy below actually applies this -- the
    // scrollable/visual-surface strategies delegate to scrollPanel()/surface(), which are a
    // separate widget surface (out of this task's row()/column() scope) and always keep their
    // own Start default. Thread it through those too if a scrollable/surfaced column ever needs
    // a non-default cross-axis alignment.
    horizontalAlignment: UiAlignment.Horizontal = UiAlignment.Horizontal.Start,
    // Opt-in cross-frame hasWeightedChild cache -- see UiPrimitiveScope.column()'s doc comment. Only
    // resolveMeasuredColumn()'s plain-measured strategy below consults this; the scrollable/
    // visual-surface strategies delegate to scrollPanel()/surface(), a separate widget surface
    // out of this task's row()/column() scope, so cacheKey is simply unused (not incorrect) on
    // those paths.
    cacheKey: Any? = null,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds {
    val scrollId = id ?: modifier.testTag ?: modifier.scrollState?.id
    if (modifier.scrollState != null && scrollId != null && scrollId.isNotEmpty()) {
        return resolveScrollableContainer(scrollId, modifier, style, verticalArrangement, content)
    }

    val effectiveStyle = style then (modifier.styleable ?: Style.Empty)
    if (hasResolvedVisuals(modifier, effectiveStyle, role, id) && id != null) {
        return resolveVisualSurface(id, modifier, effectiveStyle, verticalArrangement, clipContent, content)
    }

    return resolveMeasuredColumn(id, modifier, effectiveStyle, verticalArrangement, role, horizontalAlignment, cacheKey, content)
}

/** True if [effectiveStyle] (merged with this role's theme defaults) resolves to a real
 * background/border/shape -- the signal that a container should paint itself as a surface
 * rather than lay out as a plain, invisible column. */
private fun UiPrimitiveScope.hasResolvedVisuals(
    modifier: UiModifier,
    effectiveStyle: Style,
    role: UiSemanticRole,
    id: String?,
): Boolean {
    // Avoid claiming a slot (which may be WrapContent) just to check hover. Use the forced
    // hover when provided, otherwise assume not hovered for this initial style resolution;
    // actual hover is checked later once a slot is claimed.
    val styleState = MutableStyleState(
        hovered = modifier.forceHover ?: false,
        active = modifier.forceActive ?: (id?.let { isActive(it) } ?: false),
        focused = modifier.forceFocus ?: (id?.let { context.isFocused(it) } ?: false),
    )
    val visualDefaults = if (role == UiSemanticRole.Panel) {
        context.current(io.github.ronjunevaldoz.awake.ui.context.LocalTheme).components.surface
    } else Style.Empty
    return (visualDefaults then effectiveStyle).resolve(
        styleState,
        context.current(io.github.ronjunevaldoz.awake.ui.context.LocalTextStyle),
    ).let {
        it.background != null || it.borderWidth.toPx() > 0f || it.shape.toPx() > 0f
    }
}

private fun UiPrimitiveScope.resolveScrollableContainer(
    id: String,
    modifier: UiModifier,
    style: Style,
    verticalArrangement: Arrangement,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds = scrollPanel(
    id = id,
    modifier = modifier,
    style = style,
    verticalArrangement = verticalArrangement,
    content = content,
).slot

private fun UiPrimitiveScope.resolveVisualSurface(
    id: String,
    modifier: UiModifier,
    effectiveStyle: Style,
    verticalArrangement: Arrangement,
    clipContent: Boolean,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds {
    val requestedWidth = modifier.widthDimension ?: Dimension.WrapContent
    val requestedHeight = modifier.heightDimension ?: Dimension.WrapContent
    return surface(
        id = id,
        verticalArrangement = verticalArrangement,
        modifier = modifier.styleable(effectiveStyle).width(requestedWidth).height(requestedHeight),
        clipContent = clipContent,
        content = content,
    )
}

private fun UiPrimitiveScope.resolveMeasuredColumn(
    id: String?,
    modifier: UiModifier,
    effectiveStyle: Style,
    verticalArrangement: Arrangement,
    role: UiSemanticRole,
    horizontalAlignment: UiAlignment.Horizontal,
    cacheKey: Any? = null,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds {
    val insets = modifier.insets
    // A weight()-tagged column's width (its host row's main axis) is never actually decided by
    // its own WrapContent content -- it's decided later by the row's weight-distribution pass
    // (see UiPrimitiveScope.row()/resolveWeightedMainAxis()). Falling through to WrapContent here like
    // any other column would let this function's own content trial below permanently bake a
    // Fixed(measuredWidth) into the modifier -- and because Dimension.Fixed.resolveAgainst()
    // always returns its own value regardless of the container slot it's placed into (see
    // Layout.kt), that bogus Fixed width then overrides whatever real share the row's weight
    // distribution assigns downstream. FillMax defers width resolution to claimSlot()/
    // claimModifiedSlot(), which already special-case a weighted child correctly.
    val isWeighted = modifier.layoutWeight != null
    val requestedWidth = modifier.widthDimension ?: (if (isWeighted) Dimension.FillMax else Dimension.WrapContent)
    // Compose's rule: fillMaxHeight() under an unbounded constraint is a no-op -- the child keeps
    // its intrinsic size rather than adopting infinity. Without this, a FillMax child (and a
    // weighted one, which resolves through FillMax) of a wrap-height parent resolves against
    // UNBOUNDED_MAIN_AXIS and bakes 100000 in as a real height. Weight has nothing to divide
    // under a wrap parent either -- there is no slack to take a share of.
    val declaredHeight = modifier.heightDimension ?: Dimension.WrapContent
    val wantsMainAxisFill = declaredHeight == Dimension.FillMax || isWeighted
    val requestedHeight =
        // Scopes that are not fill-aware (absolute placement) keep the old behaviour: they hand
        // out a real slot, so FillMax there is not resolving against a sentinel.
        // Not for a scroll viewport. Wrapping there would size the viewport to its own content,
        // so it could never scroll -- silently. requireBoundedAxis() throws for that case on
        // purpose (167d75a7), and this must not quietly undo it.
        if (wantsMainAxisFill &&
            modifier.scrollState == null &&
            (this as? FillAwareScope)?.hasBoundedFillHeight == false
        ) {
            Dimension.WrapContent
        } else {
            declaredHeight
        }

    val measured = if (requestedWidth == Dimension.WrapContent || requestedHeight == Dimension.WrapContent) {
        val availableWidth = when (requestedWidth) {
            is Dimension.Fixed -> requestedWidth.dp.toPx()
            Dimension.FillMax, Dimension.WrapContent ->
                if (isWeighted) {
                    // Bound this column's own cross-axis (height) content trial by the row's
                    // real, static configured width -- not fillWidthOrNull()'s cursor-shrunk
                    // "space left after earlier siblings claimed theirs" reading, which starves
                    // every weighted sibling after the first toward (or below) zero during any
                    // un-resolved trial pass (real final placement always goes through
                    // resolveWeightedMainAxis()/plannedSlots, so this bound only needs to be
                    // sane, not exact).
                    (this as? RowScope)?.width ?: fillWidthOrNull() ?: 4096f
                } else {
                    fillWidthOrNull() ?: 4096f
                }
        }
        context.measureColumnContent(
            width = (availableWidth - insets.horizontalPx()).coerceAtLeast(0f),
            gap = verticalArrangement.baseSpacingPx(),
            insets = insets,
            content = content,
        )
    } else {
        null
    }

    val resolvedWidth = when (requestedWidth) {
        Dimension.WrapContent -> Dimension.Fixed((requireNotNull(measured).width + insets.horizontalPx()).px)
        else -> requestedWidth
    }
    val resolvedHeight = when (requestedHeight) {
        Dimension.WrapContent -> {
            val contentHeight = (requireNotNull(measured).height - insets.top.toPx()).coerceAtLeast(0f)
            Dimension.Fixed((contentHeight + insets.verticalPx()).px)
        }
        else -> requestedHeight
    }

    val rawSlot = column(
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        modifier = modifier.width(resolvedWidth).height(resolvedHeight),
        style = effectiveStyle,
        // We already ran a full trial measurement of [content] above (to size this WrapContent
        // column) -- hand it to column() so its own unconditional weight-detection trial (see
        // the "every row/column now pays for one throwaway trial measurement" comment there) can
        // reuse it instead of re-executing [content] a second time whenever this column turns out
        // to have no weighted children/no measured-distribution arrangement (the overwhelming
        // common case -- e.g. every shadcnField/shadcnFieldGroup/shadcnFieldSet in a real form).
        // column() still redoes its own trial at the real, tightened slot width if this reused
        // one turns out to actually need plannedSlots -- that path's math depends on the final
        // resolved width, not this trial's (possibly wider) available-width bound.
        precomputedMeasured = measured,
        id = id,
        cacheKey = cacheKey,
        content = content,
    )
    if (role != UiSemanticRole.None && id != null) {
        val styleState = MutableStyleState(
            hovered = modifier.forceHover ?: hitTest(rawSlot),
            active = modifier.forceActive ?: isActive(id),
            focused = modifier.forceFocus ?: context.isFocused(id),
        )
        val resolved = resolveStyle(
            style = effectiveStyle,
            defaults = if (role == UiSemanticRole.Panel) {
                context.current(io.github.ronjunevaldoz.awake.ui.context.LocalTheme).components.surface
            } else Style.Empty,
            state = styleState,
        )
        recordSemantic(
            role = role,
            id = id,
            bounds = rawSlot,
            backgroundColor = resolved.background,
            backgroundToken = resolved.backgroundToken,
            foregroundColor = resolved.foreground,
            foregroundToken = resolved.foregroundToken,
            borderColor = resolved.borderColor,
            borderToken = resolved.borderColorToken,
            borderRadius = resolved.shape.toPx(),
        )
    }
    return rawSlot
}

fun ColumnScope.column(
    id: String? = null,
    verticalArrangement: Arrangement = defaultArrangement(),
    horizontalAlignment: UiAlignment.Horizontal = UiAlignment.Horizontal.Start,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    // Opt-in cross-frame hasWeightedChild cache -- forwarded to smartColumn()/
    // resolveMeasuredColumn()/UiPrimitiveScope.column(). Defaults to null (existing call sites
    // unaffected); pair with a stable [id] to opt in. See UiPrimitiveScope.column()'s doc comment.
    cacheKey: Any? = null,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds = (this as UiPrimitiveScope).smartColumn(
    id,
    verticalArrangement.baseSpacingPx(),
    verticalArrangement,
    style,
    // Height is this column's main axis (its host column's vertical axis), so this mirrors
    // RowScope.column()'s width-side fallback exactly: a plain child hugs its content, but a
    // weight()-tagged one must defer to FillMax. withSizeFallback() bakes a concrete Dimension
    // in BEFORE resolveMeasuredColumn()'s own weight-aware deferral can look at it, so an
    // unconditional WrapContent here permanently fixes the child at its measured content height
    // -- which then wins over the share the parent's weight distribution already assigned it.
    // Symptom: the weighted child reported its content height (or 0 when empty) and every
    // header/content/footer panel collapsed, most visibly a sidebar footer painted over its menu.
    modifier.withSizeFallback(
        Dimension.FillMax,
        if (modifier.layoutWeight != null) Dimension.FillMax else Dimension.WrapContent,
    ),
    clipContent = false,
    horizontalAlignment = horizontalAlignment,
    cacheKey = cacheKey,
    content = content,
)

fun RowScope.column(
    id: String? = null,
    verticalArrangement: Arrangement = defaultArrangement(),
    horizontalAlignment: UiAlignment.Horizontal = UiAlignment.Horizontal.Start,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    // Opt-in cross-frame hasWeightedChild cache -- forwarded to smartColumn()/
    // resolveMeasuredColumn()/UiPrimitiveScope.column(). Defaults to null (existing call sites
    // unaffected); pair with a stable [id] to opt in. See UiPrimitiveScope.column()'s doc comment.
    cacheKey: Any? = null,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds = (this as UiPrimitiveScope).smartColumn(
    id,
    verticalArrangement.baseSpacingPx(),
    verticalArrangement,
    style,
    // Width is this column's main axis (the row's horizontal axis) -- a plain column hugs its
    // own content (WrapContent) by default, but a weight()-tagged one must default to FillMax
    // instead: resolveMeasuredColumn()'s own weight-aware WrapContent deferral (see isWeighted
    // there) only ever sees a *null* widthDimension to override -- withSizeFallback() already
    // bakes in a concrete Dimension before that check runs, so leaving this unconditional here
    // would silently defeat it, right back to a Fixed(measuredContentWidth) that resolveAgainst()
    // then honors over the row's real weighted share (see UiScopeMetrics.claimModifiedSlot).
    modifier.withSizeFallback(
        if (modifier.layoutWeight != null) Dimension.FillMax else Dimension.WrapContent,
        Dimension.FillMax,
    ),
    clipContent = false,
    horizontalAlignment = horizontalAlignment,
    cacheKey = cacheKey,
    content = content,
)

fun AbsoluteScope.column(
    id: String? = null,
    verticalArrangement: Arrangement = defaultArrangement(),
    horizontalAlignment: UiAlignment.Horizontal = UiAlignment.Horizontal.Start,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    // Opt-in cross-frame hasWeightedChild cache -- forwarded to smartColumn()/
    // resolveMeasuredColumn()/UiPrimitiveScope.column(). Defaults to null (existing call sites
    // unaffected); pair with a stable [id] to opt in. See UiPrimitiveScope.column()'s doc comment.
    cacheKey: Any? = null,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds = (this as UiPrimitiveScope).smartColumn(
    id,
    verticalArrangement.baseSpacingPx(),
    verticalArrangement,
    style,
    modifier.withSizeFallback(Dimension.WrapContent, Dimension.WrapContent),
    clipContent = false,
    horizontalAlignment = horizontalAlignment,
    cacheKey = cacheKey,
    content = content,
)

fun BoxScope.column(
    id: String? = null,
    verticalArrangement: Arrangement = defaultArrangement(),
    horizontalAlignment: UiAlignment.Horizontal = UiAlignment.Horizontal.Start,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    // Opt-in cross-frame hasWeightedChild cache -- forwarded to smartColumn()/
    // resolveMeasuredColumn()/UiPrimitiveScope.column(). Defaults to null (existing call sites
    // unaffected); pair with a stable [id] to opt in. See UiPrimitiveScope.column()'s doc comment.
    cacheKey: Any? = null,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds = (this as UiPrimitiveScope).smartColumn(
    id,
    verticalArrangement.baseSpacingPx(),
    verticalArrangement,
    style,
    modifier.withSizeFallback(Dimension.WrapContent, Dimension.WrapContent),
    clipContent = false,
    horizontalAlignment = horizontalAlignment,
    cacheKey = cacheKey,
    content = content,
)

fun UiPrimitiveScope.column(
    verticalArrangement: Arrangement = defaultArrangement(),
    horizontalAlignment: UiAlignment.Horizontal = UiAlignment.Horizontal.Start,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    // Set only by resolveMeasuredColumn(), which already ran a full trial of [content] to size
    // this WrapContent column -- see the call site comment there. Every other caller leaves this
    // null and pays the same unconditional trial this function has always run.
    precomputedMeasured: UiMeasuredContent? = null,
    // Opt-in cross-frame cache for the hasWeightedChild trial below (see
    // docs/tasks/2026-08-02-trial-measure-cross-frame-cache.md). Both default to null, meaning
    // every existing call site is completely unaffected. Supply a caller-stable [id] and a
    // [cacheKey] that changes whenever this content's `.weight()`-usage structure could change
    // (e.g. a selected-page enum) -- NOT a per-frame value like scroll offset/animation progress,
    // that reintroduces the exact staleness risk this cache's debug consistency check
    // (UiWeightCacheConsistencyCheck) exists to catch -- to skip the trial on cache hits.
    id: String? = null,
    cacheKey: Any? = null,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds {
    // Headless receivers call this primitive overload directly. When there is no precomputed
    // trial, route through the measured composite so default WrapContent columns can resolve
    // their content before claiming a slot. resolveMeasuredColumn() re-enters here only with
    // precomputedMeasured, preserving its planned-slot fast path.
    val hasExplicitFixedDimensions = modifier.widthDimension != null &&
        modifier.heightDimension != null &&
        modifier.widthDimension != Dimension.WrapContent &&
        modifier.heightDimension != Dimension.WrapContent
    if (precomputedMeasured == null && (!hasExplicitFixedDimensions || modifier.scrollState != null)) {
        return smartColumn(
            id = id,
            gap = verticalArrangement.baseSpacingPx(),
            verticalArrangement = verticalArrangement,
            style = style,
            modifier = modifier,
            horizontalAlignment = horizontalAlignment,
            cacheKey = cacheKey,
            content = content,
        )
    }
    val sizedModifier = modifier.withSizeFallback(Dimension.FillMax, Dimension.WrapContent)
    val slot = claimModifiedSlot(sizedModifier)
    val styleState = MutableStyleState(
        hovered = modifier.forceHover ?: hitTest(slot),
        active = modifier.forceActive ?: false,
        focused = modifier.forceFocus ?: false,
    )
    val textStyle = (style then (modifier.styleable ?: Style.Empty)).resolve(
        styleState,
        context.current(io.github.ronjunevaldoz.awake.ui.context.LocalTextStyle),
    ).textStyle

    context.pushLocal(io.github.ronjunevaldoz.awake.ui.context.LocalTextStyle, textStyle)
    val requestedWidth = sizedModifier.widthDimension ?: Dimension.FillMax
    val requestedHeight = sizedModifier.heightDimension ?: Dimension.WrapContent
    val effectiveArrangement = verticalArrangement
    // ponytail: see the matching comment in UiPrimitiveScope.row() -- a plain Start/SpacedBy column
    // with no weighted children still takes the original fast childColumn() path below.
    // [precomputedMeasured] (when supplied) already answers "does this content have a weighted
    // child" -- weighted-ness is structural (which children called weight()), not dependent on
    // the width/height bound a trial measured against, so it's always safe to reuse for this
    // check alone, even though it was measured against resolveMeasuredColumn's wider
    // available-width bound rather than this column's real, tightened [slot].
    // See the matching comment in UiPrimitiveScope.row() -- requiresMeasuredDistribution() alone already
    // forces the plannedSlots branch below regardless of hasWeightedChild's value, so `||`
    // short-circuits the trial to the right whenever it does, skipping a full throwaway
    // execution of [content] the branch decision below never needed anyway.
    val hasWeightedChild = effectiveArrangement.requiresMeasuredDistribution() ||
        if (precomputedMeasured != null) {
            precomputedMeasured.weights.any { it != null }
        } else {
            context.resolveHasWeightedChild(id, cacheKey) {
                context.measureColumnContent(
                    width = slot.width,
                    gap = effectiveArrangement.baseSpacingPx(),
                    height = slot.height,
                    content = content,
                ).weights.any { it != null }
            }
        }
    val scope = if (hasWeightedChild) {
        // The plannedSlots branch below positions children using real, final pixel sizes --
        // unlike the hasWeightedChild check above, this genuinely needs a trial at this column's
        // actual resolved [slot] (not resolveMeasuredColumn's provisional available-width bound),
        // so always re-measure here regardless of whether a precomputed trial was supplied.
        val measured = context.measureColumnContent(
            width = slot.width,
            // See the matching comment in UiPrimitiveScope.row() -- real gap so a FillMax child's trial
            // height already accounts for the gap before its next sibling.
            gap = effectiveArrangement.baseSpacingPx(),
            height = slot.height,
            content = content,
        )
        val childHeights = resolveWeightedMainAxis(
            measuredSizes = measured.slots.map { it.height },
            weights = measured.weights,
            containerSize = slot.height,
            gap = effectiveArrangement.baseSpacingPx(),
            fillsMainAxis = measured.fillsMainAxis,
        )
        val occupiedHeight = childHeights.sum() + effectiveArrangement.baseSpacingPx() * (childHeights.size - 1).coerceAtLeast(0)
        val plan = effectiveArrangement.plan(slot.height, childHeights.size, occupiedHeight)
        var y = slot.y + plan.leadingSpacePx
        val arrangedSlots = childHeights.mapIndexed { index, height ->
            UiBounds(slot.x, y, measured.slots[index].width, height).also {
                y += height + plan.betweenSpacePx
            }
        }
        context.createColumn(
            slot = slot,
            // Note: gap is no longer threaded here -- this branch always supplies plannedSlots,
            // and ColumnScope.claimSlot()'s plannedSlots-present path never consults gap for
            // cursor math (each slot is pre-computed via plan()/arrangedSlots above), so the
            // derived `effectiveArrangement.baseSpacingPx()` gap this column ends up with is
            // dead weight either way -- unused, not incorrect.
            verticalArrangement = effectiveArrangement,
            hasBoundedFillWidth = requestedWidth != Dimension.WrapContent,
            hasBoundedFillHeight = requestedHeight != Dimension.WrapContent,
            overlayOnly = emitsToOverlay,
            plannedSlots = arrangedSlots,
            horizontalAlignment = horizontalAlignment,
        )
    } else {
        childColumn(
            slot,
            verticalArrangement = effectiveArrangement,
            modifier = UiModifier(testTag = modifier.testTag),
            hasBoundedFillWidth = requestedWidth != Dimension.WrapContent,
            hasBoundedFillHeight = requestedHeight != Dimension.WrapContent,
            horizontalAlignment = horizontalAlignment,
        )
    }
    // See the matching comment in UiPrimitiveScope.row() -- suppress recording while rendering this
    // column's own real children so a composite (e.g. weighted) child's grandchildren don't
    // corrupt an ancestor row/column's own in-progress measured.slots/weights trial.
    context.withMeasuredRecordingSuppressed { scope.content(slot) }
    context.popLocal(io.github.ronjunevaldoz.awake.ui.context.LocalTextStyle)
    return slot
}
