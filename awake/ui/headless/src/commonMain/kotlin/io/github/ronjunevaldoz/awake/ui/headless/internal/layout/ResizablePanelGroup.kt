// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless.internal.layout

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiPrimitiveScope
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.WidgetState
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.boundDerivedContent
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.LayoutWeight
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.context.UiCursor
import io.github.ronjunevaldoz.awake.ui.layouts.AbstractUiScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.scope.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.scope.isMeasuring
import io.github.ronjunevaldoz.awake.ui.scope.pointerDown
import io.github.ronjunevaldoz.awake.ui.scope.pointerX
import io.github.ronjunevaldoz.awake.ui.scope.pointerY
import io.github.ronjunevaldoz.awake.ui.scope.recordSemantic
import io.github.ronjunevaldoz.awake.ui.scope.requestCursor
import io.github.ronjunevaldoz.awake.ui.toPx
import kotlin.math.floor

/** Axis a [resizablePanelGroup] lays panels/handles out along -- react-resizable-panels'
 * `orientation` prop, the upstream shadcn's `Resizable` wraps. */
enum class ResizableDirection { Horizontal, Vertical }

// Layout cost a handle reserves along the main axis, subtracted from the panels' shared budget
// before dividing it by fraction, AND the rect it hit-tests for hover/drag.
//
// Real shadcn is thinner in flow -- its Separator is `w-px` with the grab area in an absolutely
// positioned pseudo-element that costs no layout, and react-resizable-panels inflates the grab
// target further via hitAreaMargins. Splitting this into a 1dp layout cost plus a separate 5dp
// grab margin was tried and reverted: it re-proportioned every panel (they all size off the
// budget this is subtracted from) and, because the split required hand-rolling the hit test
// instead of going through interact(), the hover state the resize cursor reads went dead.
//
// Matching shadcn here needs interact() to accept a hit rect distinct from its layout rect. Until
// it does, one honest 4dp value beats a split that breaks two things to fix a 3dp discrepancy.
private val RESIZABLE_HANDLE_THICKNESS = 4f.dp

/** One [resizablePanelGroup] panel's identity -- collected for every panel, in declaration order,
 * by the dry counting walk (see [ResizablePanelGroupScope.collectedPanels]), so that a [handle]
 * can read/write the fraction of the panel *after* it without that panel having run yet in the
 * same content lambda. */
internal class ResizablePanelSpec(
    val fractionKey: String,
    val min: Float,
    val max: Float,
    val default: Float,
)

/**
 * Scope for [resizablePanelGroup]'s content -- a cursor-based walk along [direction] (mirrors
 * [io.github.ronjunevaldoz.awake.ui.layouts.RowScope]/[io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope]), except each
 * [panel]'s width comes from its own persisted fraction rather than trial-measured `.weight()`
 * shares, since dragging needs a live, mutable value. A [handle] couples only the two panels
 * immediately touching it -- dragging one handle never touches a third panel.
 *
 * Panels and handles strictly alternate (`panel, handle, panel, handle, panel, ...`), so the Nth
 * handle's neighbors are always [panelSpecs]`[N]` (before) and [panelSpecs]`[N + 1]` (after) --
 * [panelSpecs] is built once by the dry counting walk, before the real walk that resolves drags
 * runs, so both neighbors' min/max are known up front. [handle] resolves both fractions together
 * (each clamped to its own bounds, with the tighter of the two clamps applied symmetrically to
 * both) and writes both directly into [groupState] before either panel's own `panel()` call reads
 * it back -- unlike computing the "after" delta from the "before" delta alone, this can't drift
 * the pair out of budget when one of them hits its bound and the other doesn't.
 */
class ResizablePanelGroupScope internal constructor(
    context: UiContext,
    val direction: ResizableDirection,
    private val groupState: WidgetState,
    private val bounds: UiBounds,
    private val availableMainAxisPx: Float,
    private val countingOnly: Boolean,
    private val panelSpecs: List<ResizablePanelSpec> = emptyList(),
    emitToOverlay: Boolean = false,
) : AbstractUiScope(context, emitToOverlay) {

    /** Populated only by [resizablePanelGroup]'s dry counting walk -- the number of [handle]
     * calls this content lambda makes, read to size [availableMainAxisPx]. */
    internal var handleCount: Int = 0
        private set

    /** Populated only by [resizablePanelGroup]'s dry counting walk -- every [panel]'s identity,
     * in declaration order, read to build [panelSpecs] for the real walk. */
    internal val collectedPanels: MutableList<ResizablePanelSpec> = mutableListOf()

    /** Populated only by the dry counting walk -- every [handle]'s id, in declaration order,
     * read by [resolveHandleDrags] so drags resolve before the real walk lays any panel out. */
    internal val collectedHandleIds: MutableList<String> = mutableListOf()

    private var cursorMain = if (direction == ResizableDirection.Horizontal) bounds.x else bounds.y
    private var handleIndex = 0

    override fun claimSlot(width: Dimension, height: Dimension, weight: LayoutWeight?): UiBounds {
        val w = resolveAxis(width, bounds.width)
        val h = resolveAxis(height, bounds.height)
        val slot = if (direction == ResizableDirection.Horizontal) {
            UiBounds(cursorMain, bounds.y, w, h)
        } else {
            UiBounds(bounds.x, cursorMain, w, h)
        }
        cursorMain += if (direction == ResizableDirection.Horizontal) w else h
        return slot
    }

    private fun resolveAxis(dimension: Dimension, fallback: Float): Float = when (dimension) {
        is Dimension.Fixed -> dimension.dp.toPx()
        Dimension.FillMax -> fallback
        Dimension.WrapContent -> error("WrapContent must be resolved by a measuring composite before claimSlot()")
    }

    /** One resizable region. [defaultSize]/[minSize]/[maxSize] are fractions (0..1) of the
     * group's main axis -- shadcn's `defaultSize="50%"` is `0.5f` here. Persisted under [id] so
     * a neighboring [handle]'s drag survives frame to frame. */
    fun panel(
        id: String,
        defaultSize: Float,
        minSize: Float = 0.1f,
        maxSize: Float = 1f,
        content: ColumnScope.(slot: UiBounds) -> Unit,
    ): UiBounds {
        val fractionKey = "$id.fraction"
        if (countingOnly) {
            collectedPanels.add(ResizablePanelSpec(fractionKey, minSize, maxSize, defaultSize))
            return UiBounds(0f, 0f, 0f, 0f)
        }
        val fraction = groupState.get(fractionKey, defaultSize)
        val sizePx = (fraction * availableMainAxisPx).coerceAtLeast(0f)
        val modifier = if (direction == ResizableDirection.Horizontal) {
            Modifier.width(sizePx.px).height(Dimension.FillMax)
        } else {
            Modifier.width(Dimension.FillMax).height(sizePx.px)
        }
        val slot = claimModifiedSlot(modifier)
        val panelScope = context.createColumn(slot = slot, overlayOnly = emitsToOverlay)
        panelScope.content(slot)
        recordSemantic(role = UiSemanticRole.Panel, id = id, bounds = slot)
        return slot
    }

    /** A draggable divider between the [panel] immediately before and after it. Real drag/resize
     * mechanics only -- the visible line and optional grip are the shadcn skin's job (see
     * `shadcnResizableHandle` in ui-designsystem). */
    fun handle(id: String, withHandle: Boolean = false): UiBounds {
        if (countingOnly) {
            handleCount++
            collectedHandleIds.add(id)
            return UiBounds(0f, 0f, 0f, 0f)
        }
        val modifier = if (direction == ResizableDirection.Horizontal) {
            Modifier.width(RESIZABLE_HANDLE_THICKNESS).height(Dimension.FillMax)
        } else {
            Modifier.width(Dimension.FillMax).height(RESIZABLE_HANDLE_THICKNESS)
        }
        // Back to interact(), deliberately. Hand-rolling this as claimModifiedSlot + a bare
        // hitTest against an inflated rect looked equivalent and was not: interact() is what
        // produces the hover state the resize cursor is requested from, and doing the hit test
        // directly left `hovered` false in the real app, so the custom cursor never appeared
        // even though dragging worked. The wider grab area matching react-resizable-panels'
        // hitAreaMargins needs interact() to support a hit rect distinct from its layout rect;
        // it is not something a caller can bolt on from outside.
        val interaction = interact(id = id, modifier = modifier)
        val slot = interaction.slot
        val hovered = interaction.hovered
        val pointerMain = if (direction == ResizableDirection.Horizontal) pointerX() else pointerY()
        val lastPointerKey = "$id.lastPointerMain"
        val dragging = isActive(id) && pointerDown()
        // Hovered OR mid-drag: a fast drag can outrun the pointer past this handle's own thin
        // hit strip on a given frame, and the resize affordance must not flicker off just
        // because the hover test briefly misses while the gesture is still active.
        if (hovered || dragging) {
            requestCursor(
                if (direction == ResizableDirection.Horizontal) {
                    UiCursor.ResizeHorizontal
                } else {
                    UiCursor.ResizeVertical
                },
            )
        }
        // Fraction mutation itself lives in [resolveHandleDrags], which runs before the real
        // walk so every panel -- including the one declared BEFORE this handle -- reads the
        // post-drag fraction in the same frame. Resolving it here (the old shape) updated the
        // before-panel one frame late, so during a live drag the panels after it jittered
        // sideways every frame. The one thing the walk still owns is the press-frame baseline:
        // interact() latches activation during this call, after the pre-pass already ran, so
        // the gesture's starting pointer position must be recorded here or the first movement
        // frame would measure its delta from wherever the pointer already moved to.
        // isMeasuring() guard: a trial pass shares the persisted groupState but has blank
        // input/activation state -- it must never touch drag bookkeeping (see the identical
        // gate in UiContext.animateFloat).
        if (!isMeasuring() && dragging) {
            groupState.set(lastPointerKey, groupState.get(lastPointerKey, pointerMain))
        }
        handleIndex++
        recordSemantic(role = UiSemanticRole.Separator, id = id, bounds = slot)
        // Reference's always-visible separator line (resizable.tsx: `w-px bg-border` full
        // cross-axis) -- a hairline centered in the grab strip, drawn whether or not the grip
        // box is requested. Before this the strip was fully invisible without `withHandle`.
        // Snapped to whole pixels: the strip's center is fraction-derived, and a 1px quad at
        // a fractional coordinate anti-aliases into an invisible smear after the first drag.
        val lineThickness = 1f.dp.toPx()
        if (direction == ResizableDirection.Horizontal) {
            emit(
                UiDrawPrimitive.Quad(
                    x = floor(slot.x + (slot.width - lineThickness) / 2f),
                    y = slot.y,
                    w = lineThickness,
                    h = slot.height,
                    color = theme.colors.border,
                ),
            )
        } else {
            emit(
                UiDrawPrimitive.Quad(
                    x = slot.x,
                    y = floor(slot.y + (slot.height - lineThickness) / 2f),
                    w = slot.width,
                    h = lineThickness,
                    color = theme.colors.border,
                ),
            )
        }
        if (withHandle) {
            // Matches shadcn/ui's own grip box (resizable.tsx: `h-4 w-3 rounded-xs border
            // bg-border`, holding a GripVerticalIcon) -- small and squared-off, not the oversized
            // filled capsule this used to draw. RoundedQuad has no stroke of its own, so the
            // `border` class's visual (a hairline the same border color as the fill) is
            // approximated by insetting a slightly darker/inset second quad is not attempted here;
            // one flat fill is the honest approximation until RoundedQuad grows a stroke param.
            // Long axis follows the line: upright on a vertical divider, rotated flat on a
            // horizontal one (reference rotates the same grip box 90deg per orientation).
            val gripWidth = (if (direction == ResizableDirection.Horizontal) 12f else 16f).dp.toPx()
            val gripHeight = (if (direction == ResizableDirection.Horizontal) 16f else 12f).dp.toPx()
            val gripX = slot.x + (slot.width - gripWidth) / 2f
            val gripY = slot.y + (slot.height - gripHeight) / 2f
            emit(
                UiDrawPrimitive.RoundedQuad(
                    x = gripX,
                    y = gripY,
                    w = gripWidth,
                    h = gripHeight,
                    color = theme.colors.border,
                    radius = 2f,
                ),
            )
        }
        return slot
    }
}

/**
 * Real shadcn/react-resizable-panels' `PanelGroup`: divides [modifier]'s resolved bounds along
 * [direction] among [ResizablePanelGroupScope.panel] children separated by draggable
 * [ResizablePanelGroupScope.handle]s.
 */
fun UiPrimitiveScope.resizablePanelGroup(
    id: String,
    direction: ResizableDirection = ResizableDirection.Horizontal,
    modifier: UiModifier = Modifier,
    content: ResizablePanelGroupScope.() -> Unit,
): UiBounds {
    val slot = claimModifiedSlot(modifier.withSizeFallback(Dimension.FillMax, Dimension.FillMax))
    val groupState = widgetState(id)
    val mainAxisTotal = if (direction == ResizableDirection.Horizontal) slot.width else slot.height

    // Dry pass: count handles and collect every panel's id/min/max, so their combined handle
    // thickness can be subtracted from the main-axis budget before any panel's own fraction ->
    // pixel conversion runs, AND so a handle in the real pass already knows both of its
    // neighboring panels' bounds (the panel *after* it hasn't run yet in that same walk).
    val counter = ResizablePanelGroupScope(
        context,
        direction,
        groupState,
        slot,
        mainAxisTotal,
        countingOnly = true,
        emitToOverlay = emitsToOverlay,
    )
    counter.content()
    val availableMainAxisPx =
        (mainAxisTotal - counter.handleCount * RESIZABLE_HANDLE_THICKNESS.toPx()).coerceAtLeast(0f)

    resolveHandleDrags(
        direction = direction,
        groupState = groupState,
        handleIds = counter.collectedHandleIds,
        panelSpecs = counter.collectedPanels,
        availableMainAxisPx = availableMainAxisPx,
    )

    val real = ResizablePanelGroupScope(
        context,
        direction,
        groupState,
        slot,
        availableMainAxisPx,
        countingOnly = false,
        panelSpecs = counter.collectedPanels,
        emitToOverlay = emitsToOverlay,
    )
    // Panel/handle claims are `fraction x this group's own resolved bound` -- not intrinsic
    // sizes. Left visible to an enclosing WrapContent parent's measure trial, the handle's claim
    // at its fraction-derived x reported a phantom content width that re-sized the parent on
    // every drag (the showcase card grew by the drag delta).
    boundDerivedContent { real.content() }
    return slot
}

/**
 * Applies every in-progress handle drag to its neighboring panels' fractions BEFORE the real
 * walk lays anything out, so all panels -- including one declared before its handle -- render
 * the post-drag split in the same frame. The walk itself only records the press-frame baseline
 * (see [ResizablePanelGroupScope.handle]); activation is read from interact()'s persisted
 * state, so a gesture's first movement resolves here one frame after the press latches.
 */
private fun UiPrimitiveScope.resolveHandleDrags(
    direction: ResizableDirection,
    groupState: WidgetState,
    handleIds: List<String>,
    panelSpecs: List<ResizablePanelSpec>,
    availableMainAxisPx: Float,
) {
    if (isMeasuring() || availableMainAxisPx <= 0f) return
    val pointerMain = if (direction == ResizableDirection.Horizontal) pointerX() else pointerY()
    handleIds.forEachIndexed { index, id ->
        val lastPointerKey = "$id.lastPointerMain"
        if (!(isActive(id) && pointerDown())) {
            groupState.remove(lastPointerKey)
            return@forEachIndexed
        }
        val previousPointerMain = groupState.get(lastPointerKey, pointerMain)
        groupState.set(lastPointerKey, pointerMain)
        val before = panelSpecs.getOrNull(index) ?: return@forEachIndexed
        val after = panelSpecs.getOrNull(index + 1) ?: return@forEachIndexed
        val rawDelta = (pointerMain - previousPointerMain) / availableMainAxisPx
        val beforeOld = groupState.get(before.fractionKey, before.default)
        val afterOld = groupState.get(after.fractionKey, after.default)
        var appliedDelta = (beforeOld + rawDelta).coerceIn(before.min, before.max) - beforeOld
        val afterNew = (afterOld - appliedDelta).coerceIn(after.min, after.max)
        // If `after` clamped harder than `before` did, the pair no longer sums to zero --
        // re-derive `before`'s delta from `after`'s actual, already-clamped change so both
        // panels move by exactly the same magnitude and the group's total never drifts.
        appliedDelta = afterOld - afterNew
        groupState.set(before.fractionKey, beforeOld + appliedDelta)
        groupState.set(after.fractionKey, afterNew)
    }
}
