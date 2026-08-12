// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.unstyled

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiPrimitiveScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.WidgetState
import io.github.ronjunevaldoz.awake.ui.api.dp
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
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.toPx

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

/** One [resizablePanelGroup] panel's identity, captured by [ResizablePanelGroupScope.panel]
 * immediately before a [ResizablePanelGroupScope.handle] call so that handle's drag can read/
 * write this panel's fraction without needing to know about a panel declared *after* it in the
 * same content lambda. */
private class ResizablePanelSpec(
    val fractionKey: String,
    val min: Float,
    val max: Float,
    val default: Float,
)

/** A handle's pointer-driven fraction delta, computed in [ResizablePanelGroupScope.handle] and
 * consumed by the very next [ResizablePanelGroupScope.panel] call. */
private class PendingHandleDrag(val mirroredDeltaFraction: Float)

/**
 * Scope for [resizablePanelGroup]'s content -- a cursor-based walk along [direction] (mirrors
 * [io.github.ronjunevaldoz.awake.ui.layouts.RowScope]/[io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope]), except each
 * [panel]'s width comes from its own persisted fraction rather than trial-measured `.weight()`
 * shares, since dragging needs a live, mutable value. A [handle] couples only the two panels
 * immediately touching it -- dragging one handle never touches a third panel.
 *
 * ponytail: a handle's drag applies to the panel *before* it immediately, but the mirrored
 * shrink/grow on the panel *after* it only lands once that next `panel()` call runs later in the
 * same synchronous walk -- both settle within the same frame, but if `before` and `after` hit
 * their own min/max on the *same* frame the pair can drift slightly out of budget. Upgrade path:
 * resolve both panels' fractions together before drawing either, the way row()/column() resolve
 * weighted children in one trial pass first.
 */
class ResizablePanelGroupScope internal constructor(
    context: UiContext,
    val direction: ResizableDirection,
    private val groupState: WidgetState,
    private val bounds: UiBounds,
    private val availableMainAxisPx: Float,
    private val countingOnly: Boolean,
    emitToOverlay: Boolean = false,
) : AbstractUiScope(context, emitToOverlay) {

    /** Populated only by [resizablePanelGroup]'s dry counting walk -- the number of [handle]
     * calls this content lambda makes, read to size [availableMainAxisPx]. */
    internal var handleCount: Int = 0
        private set

    private var cursorMain = if (direction == ResizableDirection.Horizontal) bounds.x else bounds.y
    private var lastPanel: ResizablePanelSpec? = null
    private var pendingDrag: PendingHandleDrag? = null

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
        if (countingOnly) return UiBounds(0f, 0f, 0f, 0f)
        val fractionKey = "$id.fraction"
        var fraction = groupState.get(fractionKey, defaultSize)
        pendingDrag?.let { drag ->
            fraction = (fraction + drag.mirroredDeltaFraction).coerceIn(minSize, maxSize)
            groupState.set(fractionKey, fraction)
            pendingDrag = null
        }
        lastPanel = ResizablePanelSpec(fractionKey, minSize, maxSize, defaultSize)
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
        // A WrapContent/scroll trial-measurement pass re-executes this whole content lambda
        // against a scratch UiContext that shares the real, persisted groupState but starts with
        // its own blank input/activation state (see UiContextMeasureState.createMeasureContext),
        // so `dragging` above always reads false there. Left unguarded, the trial's own `else`
        // branch below would delete lastPointerKey out from under the real pass that runs right
        // after it in the same frame, permanently zeroing every real frame's pointer delta --
        // the actual "dragging does nothing" bug. Guard this side effect like every other
        // stateful UiContext operation (see UiContext.animateFloat's identical isMeasuring() gate).
        pendingDrag = if (isMeasuring()) {
            null
        } else if (dragging) {
            val previousPointerMain = groupState.get(lastPointerKey, pointerMain)
            groupState.set(lastPointerKey, pointerMain)
            val before = lastPanel
            if (before != null && availableMainAxisPx > 0f) {
                val deltaFraction = (pointerMain - previousPointerMain) / availableMainAxisPx
                val oldFraction = groupState.get(before.fractionKey, before.default)
                val newFraction = (oldFraction + deltaFraction).coerceIn(before.min, before.max)
                groupState.set(before.fractionKey, newFraction)
                PendingHandleDrag(mirroredDeltaFraction = -(newFraction - oldFraction))
            } else {
                null
            }
        } else {
            groupState.remove(lastPointerKey)
            null
        }
        recordSemantic(role = UiSemanticRole.Separator, id = id, bounds = slot)
        if (withHandle) {
            val pillWidth = 12f.dp.toPx()
            val pillHeight = 20f.dp.toPx()
            val pillX = slot.x + (slot.width - pillWidth) / 2f
            val pillY = slot.y + (slot.height - pillHeight) / 2f
            emit(
                UiDrawPrimitive.RoundedQuad(
                    x = pillX,
                    y = pillY,
                    w = pillWidth,
                    h = pillHeight,
                    color = context.currentTheme.colors.border,
                    radius = 4f,
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

    // Dry pass: count handles only, so their combined thickness can be subtracted from the
    // main-axis budget before any panel's own fraction -> pixel conversion runs -- a panel
    // drawn before the group knows the true handle count would overclaim space (see the
    // ResizablePanelGroupScope class doc's ponytail note for the sibling tradeoff this mirrors).
    val counter = ResizablePanelGroupScope(
        context,
        direction,
        groupState,
        slot,
        mainAxisTotal,
        countingOnly = true,
        emitsToOverlay,
    )
    counter.content()
    val availableMainAxisPx =
        (mainAxisTotal - counter.handleCount * RESIZABLE_HANDLE_THICKNESS.toPx()).coerceAtLeast(0f)

    val real = ResizablePanelGroupScope(
        context,
        direction,
        groupState,
        slot,
        availableMainAxisPx,
        countingOnly = false,
        emitsToOverlay,
    )
    real.content()
    return slot
}
