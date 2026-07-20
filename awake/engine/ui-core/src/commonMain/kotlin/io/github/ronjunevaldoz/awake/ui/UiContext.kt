// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.font.UiFont
import kotlin.math.max

/**
 * Minimal immediate-mode UI context -- ImGui's own architecture (hot/active id tracking, no
 * retained widget tree, no ECS entities). Pure layout and spatial hit-testing engine.
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

    private lateinit var frameInputState: UiInputState

    /**
     * Resets the context for a new frame. Accepts [UiInputState] to remain 
     * decoupled from hardware input modules.
     */
    fun beginFrame(
        screenWidth: Float,
        screenHeight: Float,
        inputState: UiInputState,
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
        pointerDownEdgeThisFrame = inputState.pointerDown && !pointerDownLastFrame
        focusClaimedThisFrame = false
        isOverScrollableThisFrame = false
        isScrollConsumedThisFrame = false
        
        frameInputState = inputState
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
        pointerDownLastFrame = frameInputState.pointerDown
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

    // --- Shared state accessors, delegated to by AbstractUiScope in Layout.kt.

    internal fun hitTestInternal(slot: UiSlot): Boolean =
        !measuring && frameInputState.pointerX in slot.x..(slot.x + slot.width) && frameInputState.pointerY in slot.y..(slot.y + slot.height)

    internal fun isActiveInternal(id: String): Boolean = activeId == id

    internal fun tryClaimActiveInternal(id: String, hovered: Boolean) {
        if (measuring) return
        if (hovered && frameInputState.pointerDown && activeId == null) activeId = id
    }

    internal fun releaseActiveIfMatchesInternal(id: String) {
        if (measuring) return
        if (!frameInputState.pointerDown && activeId == id) activeId = null
    }

    internal fun isFocusedInternal(id: String): Boolean = focusedId == id

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

    internal fun pushClipInternal(rect: UiSlot): UiSlot {
        val current = clipStack.lastOrNull() ?: fullFrameRect
        val resolved = current.intersect(rect)
        clipStack += resolved
        return resolved
    }

    fun pushClip(rect: UiSlot): UiSlot = pushClipInternal(rect)

    internal fun popClipInternal(): UiSlot {
        if (clipStack.isNotEmpty()) clipStack.removeAt(clipStack.size - 1)
        return clipStack.lastOrNull() ?: fullFrameRect
    }

    fun popClip(): UiSlot = popClipInternal()

    fun pointerDownEdge(): Boolean = pointerDownEdgeThisFrame

    fun setActive(id: String?) {
        activeId = id
    }

    fun isFocused(id: String): Boolean = isFocusedInternal(id)

    fun requestFocus(id: String) = requestFocusInternal(id)

    fun clearFocusIfMatches(id: String) = clearFocusIfMatchesInternal(id)

    fun frameDeltaSeconds(): Float = frameDeltaSeconds

    fun frameBounds(): UiSlot = fullFrameRect

    fun isMeasuring(): Boolean = measuring

    internal fun recordMeasuredSlot(slot: UiSlot) {
        if (!measuring) return
        measuredMaxRight = max(measuredMaxRight, slot.x + slot.width)
        measuredMaxBottom = max(measuredMaxBottom, slot.y + slot.height)
    }

    fun measureColumnContent(
        width: Float,
        font: UiFont?,
        theme: UiTheme,
        gap: Float =  UiSpacing.sm.toPx(),
        textScale: Float = 1f,
        insets: UiInsets = UiInsets.Zero,
        content: ColumnScope.(slot: UiSlot) -> Unit
    ): UiMeasuredContent {
        val measureContext = UiContext(measuring = true)
        val outerSlot = UiSlot(0f, 0f, width.coerceAtLeast(0f), 100_000f)
        measureContext.beginFrame(
            screenWidth = outerSlot.width.coerceAtLeast(1f),
            screenHeight = outerSlot.height,
            inputState = UiInputState(),
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

    fun measureRowContent(
        height: Float,
        font: UiFont?,
        theme: UiTheme,
        gap: Float,
        textScale: Float,
        insets: UiInsets = UiInsets.Zero,
        content: RowScope.(slot: UiSlot) -> Unit
    ): UiMeasuredContent {
        val measureContext = UiContext(measuring = true)
        val outerSlot = UiSlot(0f, 0f, 100_000f, height.coerceAtLeast(0f))
        measureContext.beginFrame(
            screenWidth = outerSlot.width,
            screenHeight = outerSlot.height.coerceAtLeast(1f),
            inputState = UiInputState(),
            deltaSeconds = 0f
        )
        val measureScope = measureContext.row(
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

    val inputState: UiInputState get() = frameInputState

    fun pointerX(): Float = frameInputState.pointerX
    fun pointerY(): Float = frameInputState.pointerY
    fun pointerDown(): Boolean = frameInputState.pointerDown
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
 * top-level function. */
fun sliderValueFromPointerX(pointerX: Float, trackX: Float, trackW: Float, min: Float, max: Float): Float {
    if (trackW <= 0f) return min
    val fraction = ((pointerX - trackX) / trackW).coerceIn(0f, 1f)
    return min + fraction * (max - min)
}
