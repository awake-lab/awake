// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.input.InputSnapshot
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import kotlin.math.max

/**
 * Minimal immediate-mode UI context -- ImGui's own architecture (hot/active id tracking, no
 * retained widget tree, no ECS entities), deliberately not declarative/Compose-style (no
 * composer/recomposer needed for this small a widget set) and not backed by [io.github
 * .ronjunevaldoz.awake.ecs.World] (widgets have no persistent gameplay state).
 *
 * Ids are caller-supplied stable strings (e.g. `"debug-toggle"`) -- no auto-disambiguation
 * (`##` suffixes) yet, a known simplification for this widget count.
 */
class UiContext private constructor(
    private val measuring: Boolean = false
) {
    constructor() : this(measuring = false)

    private var activeId: String? = null
    private var focusedId: String? = null
    private var pointerDownLastFrame = false
    private var pointerDownEdgeThisFrame = false
    private var focusClaimedThisFrame = false
    private val widgetStates = HashMap<String, WidgetState>()
    private val primitives = ArrayList<UiDrawPrimitive>()
    private val overlayPrimitives = ArrayList<UiDrawPrimitive>()
    private val semanticNodes = ArrayList<UiSemanticNode>()
    private var fullFrameRect = UiSlot(0f, 0f, 0f, 0f)
    private val clipStack = ArrayList<UiSlot>()
    private var frameDeltaSeconds: Float = 1f / 60f
    private var measuredMaxRight = 0f
    private var measuredMaxBottom = 0f
    private var isOverScrollableThisFrame = false
    private var isScrollConsumedThisFrame = false

    // Frame-local input state
    private lateinit var frameInputSnapshot: InputSnapshot

    fun beginFrame(
        screenWidth: Float,
        screenHeight: Float,
        inputSnapshot: InputSnapshot,
        deltaSeconds: Float = 1f / 60f
    ) {
        primitives.clear()
        overlayPrimitives.clear()
        semanticNodes.clear()
        fullFrameRect = UiSlot(0f, 0f, screenWidth, screenHeight)
        clipStack.clear()
        frameDeltaSeconds = deltaSeconds.coerceAtLeast(0f)
        measuredMaxRight = 0f
        measuredMaxBottom = 0f
        pointerDownEdgeThisFrame = inputSnapshot.pointerDown && !pointerDownLastFrame
        focusClaimedThisFrame = false
        isOverScrollableThisFrame = false
        isScrollConsumedThisFrame = false
        frameInputSnapshot = inputSnapshot
    }

    /** reserves a vertical auto-stacking layout region -- see [ColumnScope]. */
    fun column(
        x: Float,
        y: Float,
        width: Float,
        font: UiFont? = null,
        theme: UiTheme = CoreUiTheme,
        gap: Float = UiSpacing.sm.toPx(),
        textScale: Float = 1f,
        overlayOnly: Boolean = false
    ): ColumnScope = ColumnScope(this, font, theme, x, y, width, gap, textScale, overlayOnly)

    fun column(
        slot: UiSlot,
        font: UiFont? = null,
        theme: UiTheme = CoreUiTheme,
        gap: Float = UiSpacing.sm.toPx(),
        textScale: Float = 1f,
        insets: UiInsets = UiInsets.Zero,
        overlayOnly: Boolean = false
    ): ColumnScope {
        val content = slot.inset(insets)
        return column(content.x, content.y, content.width, font, theme, gap, textScale, overlayOnly)
    }

    /** One-shot manual placement at an exact x/y -- e.g. the HUD text readout or a minimap
     * thumbnail that isn't part of any auto-layout column. Goes through the exact same
     * [UiScope] surface as every other widget; not a special case. */
    fun absolute(
        x: Float,
        y: Float,
        font: UiFont? = null,
        theme: UiTheme = CoreUiTheme,
        textScale: Float = 1f,
        overlayOnly: Boolean = false
    ): AbsoluteScope = AbsoluteScope(this, font, theme, x, y, textScale, overlayOnly)

    fun absolute(
        slot: UiSlot,
        font: UiFont? = null,
        theme: UiTheme = CoreUiTheme,
        textScale: Float = 1f,
        insets: UiInsets = UiInsets.Zero,
        overlayOnly: Boolean = false
    ): AbsoluteScope {
        val content = slot.inset(insets)
        return absolute(content.x, content.y, font, theme, textScale, overlayOnly)
    }

    /** Reserves a horizontal auto-stacking layout region -- see [RowScope]. */
    fun row(
        x: Float,
        y: Float,
        height: Float,
        font: UiFont? = null,
        theme: UiTheme = CoreUiTheme,
        gap: Float = UiSpacing.sm.toPx(),
        textScale: Float = 1f,
        overlayOnly: Boolean = false
    ): RowScope = RowScope(this, font, theme, x, y, height, gap, textScale, overlayOnly)

    fun row(
        slot: UiSlot,
        font: UiFont? = null,
        theme: UiTheme = CoreUiTheme,
        gap: Float = UiSpacing.sm.toPx(),
        textScale: Float = 1f,
        insets: UiInsets = UiInsets.Zero,
        overlayOnly: Boolean = false
    ): RowScope {
        val content = slot.inset(insets)
        return row(content.x, content.y, content.height, font, theme, gap, textScale, overlayOnly)
    }

    /** Reserves a fixed-rect region -- see [BoxScope]. */
    fun box(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        font: UiFont? = null,
        theme: UiTheme = CoreUiTheme,
        textScale: Float = 1f,
        contentAlignment: UiAlignment = UiAlignment.TopStart,
        overlayOnly: Boolean = false
    ): BoxScope = BoxScope(this, font, theme, x, y, width, height, contentAlignment, textScale, overlayOnly)

    fun box(
        slot: UiSlot,
        font: UiFont? = null,
        theme: UiTheme = CoreUiTheme,
        textScale: Float = 1f,
        insets: UiInsets = UiInsets.Zero,
        contentAlignment: UiAlignment = UiAlignment.TopStart,
        overlayOnly: Boolean = false
    ): BoxScope {
        val content = slot.inset(insets)
        return box(content.x, content.y, content.width, content.height, font, theme, textScale, contentAlignment, overlayOnly)
    }

    /** Aggregated result of this frame's input interactions. Call after all widgets have
     * executed for the current frame. */
    fun inputResult(): UiInputResult = UiInputResult(
        isCaptured = activeId != null,
        isOverScrollable = isOverScrollableThisFrame,
        isScrollConsumed = isScrollConsumedThisFrame,
        isTextInputFocused = focusedId != null
    )

    /** Collects this frame's [UiDrawPrimitive]s for the renderer. */
    fun endFrame(): List<UiDrawPrimitive> {
        if (pointerDownEdgeThisFrame && !focusClaimedThisFrame) {
            focusedId = null
        }
        pointerDownLastFrame = frameInputSnapshot.pointerDown
        return primitives + overlayPrimitives
    }

    /** Called by scrollable widgets (e.g. scrollPanel) whenever the pointer is within
     * their viewport bounds this frame. */
    fun onOverScrollable() {
        if (!measuring) {
            isOverScrollableThisFrame = true
        }
    }

    /** Marks the current frame's scroll delta as consumed by a UI widget. */
    fun onScrollConsumed() {
        if (!measuring) {
            isScrollConsumedThisFrame = true
        }
    }

    fun semanticNodes(): List<UiSemanticNode> = semanticNodes.toList()

    // --- Shared state accessors, delegated to by AbstractUiScope in Layout.kt. Internal, not
    // part of the widget-authoring surface -- that's UiScope itself.

    internal fun hitTestInternal(slot: UiSlot): Boolean =
        !measuring && frameInputSnapshot.pointerX in slot.x..(slot.x + slot.width) && frameInputSnapshot.pointerY in slot.y..(slot.y + slot.height)

    internal fun isActiveInternal(id: String): Boolean = activeId == id

    internal fun tryClaimActiveInternal(id: String, hovered: Boolean) {
        if (measuring) return
        if (hovered && frameInputSnapshot.pointerDown && activeId == null) activeId = id
    }

    internal fun releaseActiveIfMatchesInternal(id: String) {
        if (measuring) return
        if (!frameInputSnapshot.pointerDown && activeId == id) activeId = null
    }

    /** Whether a fresh pointer-down happened this frame (down now, wasn't down last frame) --
     * a text-input widget uses this to distinguish "just clicked into me" from "still holding
     * the mouse button down from an earlier frame." */
    internal fun pointerDownEdgeInternal(): Boolean = pointerDownEdgeThisFrame

    internal fun isFocusedInternal(id: String): Boolean = focusedId == id

    /** Grants persistent keyboard focus to [id], surviving across frames until something else
     * claims it, [clearFocusIfMatchesInternal] releases it, or a fresh click lands outside
     * every focusable widget this frame (see [endFrame]'s unclaimed-click-edge check). */
    internal fun requestFocusInternal(id: String) {
        if (measuring) return
        focusedId = id
        focusClaimedThisFrame = true
    }

    internal fun clearFocusIfMatchesInternal(id: String) {
        if (measuring) return
        if (focusedId == id) focusedId = null
    }

    internal fun emitInternal(p: UiDrawPrimitive) {
        if (measuring) return
        primitives += p
    }

    internal fun emitOverlayInternal(p: UiDrawPrimitive) {
        if (measuring) return
        overlayPrimitives += p
    }

    internal fun widgetStateInternal(id: String): WidgetState = widgetStates.getOrPut(id) { WidgetState() }

    internal fun recordSemanticInternal(node: UiSemanticNode) {
        if (measuring) return
        semanticNodes += node
    }

    /** Intersects [rect] against whatever clip is currently active (or the full frame extent
     * if the stack is empty) and pushes the RESOLVED rect -- nesting is resolved here, once,
     * so [UiScope.clip]'s emitted [UiDrawPrimitive.ClipPush] always carries a rect a backend
     * can apply naively, with no clip-stack awareness of its own. */
    internal fun pushClipInternal(rect: UiSlot): UiSlot {
        val current = clipStack.lastOrNull() ?: fullFrameRect
        val resolved = current.intersect(rect)
        clipStack += resolved
        return resolved
    }

    fun pushClip(rect: UiSlot): UiSlot = pushClipInternal(rect)

    /** Pops the clip stack and returns the rect that should be restored -- the next entry
     * down, or the full frame extent if the stack is now empty. */
    internal fun popClipInternal(): UiSlot {
        if (clipStack.isNotEmpty()) clipStack.removeAt(clipStack.size - 1)
        return clipStack.lastOrNull() ?: fullFrameRect
    }

    fun popClip(): UiSlot = popClipInternal()

    fun pointerDownEdge(): Boolean = pointerDownEdgeInternal()

    fun isFocused(id: String): Boolean = isFocusedInternal(id)

    fun requestFocus(id: String) = requestFocusInternal(id)

    fun clearFocusIfMatches(id: String) = clearFocusIfMatchesInternal(id)

    fun frameDeltaSeconds(): Float = frameDeltaSecondsInternal()

    fun frameBounds(): UiSlot = fullFrameRect

    fun isMeasuring(): Boolean = measuring

    internal fun recordMeasuredSlot(slot: UiSlot) {
        if (!measuring) return
        measuredMaxRight = max(measuredMaxRight, slot.x + slot.width)
        measuredMaxBottom = max(measuredMaxBottom, slot.y + slot.height)
    }

    internal fun frameDeltaSecondsInternal(): Float = frameDeltaSeconds

    fun measureColumnContent(
        width: Float,
        font: UiFont?,
        theme: UiTheme,
        gap: Float,
        textScale: Float,
        insets: UiInsets = UiInsets.Zero,
        content: ColumnScope.(slot: UiSlot) -> Unit
    ): UiMeasuredContent {
        val measureContext = UiContext(measuring = true)
        val outerSlot = UiSlot(0f, 0f, width.coerceAtLeast(0f), 100_000f)
        measureContext.beginFrame(
            screenWidth = outerSlot.width.coerceAtLeast(1f),
            screenHeight = outerSlot.height,
            inputSnapshot = InputSnapshot(
                pointerX = -1f,
                pointerY = -1f,
                pointerDown = false,
                scrollDeltaY = 0f,
                keysDown = emptySet(),
                typedText = "",
                editActions = emptyList()
            ),
            deltaSeconds = 0f
        )
        val measureScope = measureContext.column(
            slot = outerSlot,
            font = font,
            theme = theme,
            gap = gap,
            textScale = textScale,
            insets = insets
        )
        measureScope.content(outerSlot)
        return UiMeasuredContent(
            width = measureContext.measuredMaxRight,
            height = measureContext.measuredMaxBottom
        )
    }

    val inputSnapshot: InputSnapshot get() = frameInputSnapshot

    fun pointerX(): Float = frameInputSnapshot.pointerX
    fun pointerY(): Float = frameInputSnapshot.pointerY
    fun pointerDown(): Boolean = frameInputSnapshot.pointerDown
}

data class UiMeasuredContent(
    val width: Float,
    val height: Float
)

/**
 * Aggregated input ownership result for a single UI frame.
 */
data class UiInputResult(
    /** Whether a widget has explicitly captured the pointer (e.g. mid-drag on a slider). */
    val isCaptured: Boolean = false,
    /** Whether the pointer is currently hovering over a scrollable region. */
    val isOverScrollable: Boolean = false,
    /** Whether the scroll delta was actually used by a UI widget this frame. */
    val isScrollConsumed: Boolean = false,
    /** Whether a text-input widget has keyboard focus this frame. */
    val isTextInputFocused: Boolean = false
)

/** Pure value-from-pointer-position math for the built-in `slider`, pulled out to a
 * top-level function so it's unit-testable without an [Input]/GPU-backed [UiContext]
 * instance (see this project's "no app-layer test doubles, push logic into pure functions"
 * convention). Maps [pointerX]'s position within the track `[trackX, trackX + trackW]` to a
 * value in `[min, max]`, clamping (not extrapolating) for a [pointerX] outside the track's
 * bounds -- a drag that overshoots the track while still held should pin at [min]/[max], not
 * keep increasing past them. */
fun sliderValueFromPointerX(pointerX: Float, trackX: Float, trackW: Float, min: Float, max: Float): Float {
    if (trackW <= 0f) return min
    val fraction = ((pointerX - trackX) / trackW).coerceIn(0f, 1f)
    return min + fraction * (max - min)
}
