// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.context

import io.github.ronjunevaldoz.awake.ui.UiAlignment
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.styling.UiInsets
import io.github.ronjunevaldoz.awake.ui.UiSemanticNode
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.WidgetState
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.layouts.AbsoluteScope
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.UiSpacing
import io.github.ronjunevaldoz.awake.ui.layouts.baseSpacingPx
import io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme
import io.github.ronjunevaldoz.awake.ui.toPx

/**
 * Minimal immediate-mode UI context -- ImGui's own architecture (hot/active id tracking, no
 * retained widget tree, no ECS entities). Pure layout and spatial hit-testing engine.
 */
class UiContext internal constructor(
    private val measuring: Boolean = false
) {
    constructor() : this(measuring = false)

    private val stacks = UiContextStacks()
    private val stateStore = UiStateStore()
    private val runtime = UiRuntimeCoordinator(stateStore = stateStore)
    private val measurement = UiMeasurementRuntime()
    private val layouts = UiLayoutFactory(this)

    val currentTheme: UiTheme get() = stacks.currentTheme
    val currentTextStyle: TextStyle get() = stacks.currentTextStyle
    val currentFont get() = stacks.currentFont
    val currentShapeSpec get() = stacks.currentShapeSpec
    val inputState: UiInputState get() = runtime.inputState

    fun pushTheme(theme: UiTheme) = stacks.pushTheme(theme)
    fun popTheme() = stacks.popTheme()

    fun pushTextStyle(style: TextStyle) = stacks.pushTextStyle(style)
    fun popTextStyle() = stacks.popTextStyle()

    fun pushFont(font: UiFont) = stacks.pushFont(font)
    fun popFont() = stacks.popFont()

    fun pushShapeSpec(spec: io.github.ronjunevaldoz.awake.ui.UiShapeSpec?) = stacks.pushShapeSpec(spec)
    fun popShapeSpec() = stacks.popShapeSpec()

    /**
     * Resets the context for a new frame. Accepts [UiInputState] to remain
     * decoupled from hardware input modules.
     */
    fun beginFrame(frame: UiFrameInput) {
        runtime.beginFrame(
            screenWidth = frame.viewportWidth,
            screenHeight = frame.viewportHeight,
            inputState = frame.input,
            deltaSeconds = frame.deltaSeconds
        )
        measurement.beginFrame()
    }

    @Deprecated(
        message = "Use beginFrame(UiFrameInput(...)) to keep the frame lifecycle input bundled as a single value.",
        replaceWith = ReplaceWith(
            "beginFrame(UiFrameInput(viewportWidth = screenWidth, viewportHeight = screenHeight, input = inputState, deltaSeconds = deltaSeconds))",
            imports = ["io.github.ronjunevaldoz.awake.ui.context.UiFrameInput"]
        )
    )
    fun beginFrame(
        screenWidth: Float,
        screenHeight: Float,
        inputState: UiInputState,
        deltaSeconds: Float = 1f / 60f
    ) {
        beginFrame(
            UiFrameInput(
                viewportWidth = screenWidth,
                viewportHeight = screenHeight,
                input = inputState,
                deltaSeconds = deltaSeconds
            )
        )
    }

    fun createColumn(
        x: Float,
        y: Float,
        width: Float,
        height: Float? = null,
        gap: Float = UiSpacing.sm.toPx(),
        verticalArrangement: Arrangement = defaultArrangement(),
        testTag: String? = null,
        hasBoundedFillWidth: Boolean = true,
        hasBoundedFillHeight: Boolean = height != null,
        overlayOnly: Boolean = false,
        plannedSlots: List<UiSlot>? = null
    ): ColumnScope = layouts.createColumn(
        x = x,
        y = y,
        width = width,
        height = height,
        gap = gap,
        verticalArrangement = verticalArrangement,
        testTag = testTag,
        hasBoundedFillWidth = hasBoundedFillWidth,
        hasBoundedFillHeight = hasBoundedFillHeight,
        overlayOnly = overlayOnly,
        plannedSlots = plannedSlots
    )

    fun createColumn(
        slot: UiSlot,
        gap: Float = UiSpacing.sm.toPx(),
        insets: UiInsets = UiInsets.Zero,
        verticalArrangement: Arrangement = defaultArrangement(),
        testTag: String? = null,
        hasBoundedFillWidth: Boolean = true,
        hasBoundedFillHeight: Boolean = true,
        overlayOnly: Boolean = false,
        plannedSlots: List<UiSlot>? = null
    ): ColumnScope = layouts.createColumn(
        slot = slot,
        gap = gap,
        insets = insets,
        verticalArrangement = verticalArrangement,
        testTag = testTag,
        hasBoundedFillWidth = hasBoundedFillWidth,
        hasBoundedFillHeight = hasBoundedFillHeight,
        overlayOnly = overlayOnly,
        plannedSlots = plannedSlots
    )

    fun createAbsolute(
        x: Float,
        y: Float,
        testTag: String? = null,
        overlayOnly: Boolean = false
    ): AbsoluteScope = layouts.createAbsolute(x, y, testTag, overlayOnly)

    fun createAbsolute(
        slot: UiSlot,
        insets: UiInsets = UiInsets.Zero,
        testTag: String? = null,
        overlayOnly: Boolean = false
    ): AbsoluteScope = layouts.createAbsolute(slot, insets, testTag, overlayOnly)

    fun createRow(
        x: Float,
        y: Float,
        height: Float,
        width: Float? = null,
        gap: Float = UiSpacing.sm.toPx(),
        horizontalArrangement: Arrangement = defaultArrangement(),
        testTag: String? = null,
        hasBoundedFillWidth: Boolean = width != null,
        hasBoundedFillHeight: Boolean = true,
        overlayOnly: Boolean = false,
        plannedSlots: List<UiSlot>? = null
    ): RowScope = layouts.createRow(
        x = x,
        y = y,
        height = height,
        width = width,
        gap = gap,
        horizontalArrangement = horizontalArrangement,
        testTag = testTag,
        hasBoundedFillWidth = hasBoundedFillWidth,
        hasBoundedFillHeight = hasBoundedFillHeight,
        overlayOnly = overlayOnly,
        plannedSlots = plannedSlots
    )

    fun createRow(
        slot: UiSlot,
        gap: Float = UiSpacing.sm.toPx(),
        insets: UiInsets = UiInsets.Zero,
        horizontalArrangement: Arrangement = defaultArrangement(),
        testTag: String? = null,
        hasBoundedFillWidth: Boolean = true,
        hasBoundedFillHeight: Boolean = true,
        overlayOnly: Boolean = false,
        plannedSlots: List<UiSlot>? = null
    ): RowScope = layouts.createRow(
        slot = slot,
        gap = gap,
        insets = insets,
        horizontalArrangement = horizontalArrangement,
        testTag = testTag,
        hasBoundedFillWidth = hasBoundedFillWidth,
        hasBoundedFillHeight = hasBoundedFillHeight,
        overlayOnly = overlayOnly,
        plannedSlots = plannedSlots
    )

    fun createBox(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        contentAlignment: UiAlignment = UiAlignment.TopStart,
        testTag: String? = null,
        hasBoundedFillWidth: Boolean = true,
        hasBoundedFillHeight: Boolean = true,
        overlayOnly: Boolean = false
    ): BoxScope = layouts.createBox(
        x = x,
        y = y,
        width = width,
        height = height,
        contentAlignment = contentAlignment,
        testTag = testTag,
        hasBoundedFillWidth = hasBoundedFillWidth,
        hasBoundedFillHeight = hasBoundedFillHeight,
        overlayOnly = overlayOnly
    )

    fun createBox(
        slot: UiSlot,
        insets: UiInsets = UiInsets.Zero,
        contentAlignment: UiAlignment = UiAlignment.TopStart,
        testTag: String? = null,
        hasBoundedFillWidth: Boolean = true,
        hasBoundedFillHeight: Boolean = true,
        overlayOnly: Boolean = false
    ): BoxScope = layouts.createBox(
        slot = slot,
        insets = insets,
        contentAlignment = contentAlignment,
        testTag = testTag,
        hasBoundedFillWidth = hasBoundedFillWidth,
        hasBoundedFillHeight = hasBoundedFillHeight,
        overlayOnly = overlayOnly
    )

    fun column(
        slot: UiSlot,
        insets: UiInsets = UiInsets.Zero,
        verticalArrangement: Arrangement = defaultArrangement(),
        testTag: String? = null,
        content: ColumnScope.() -> Unit
    ) {
        createColumn(
            slot = slot,
            gap = verticalArrangement.baseSpacingPx(),
            insets = insets,
            verticalArrangement = verticalArrangement,
            testTag = testTag
        ).content()
    }

    fun row(
        slot: UiSlot,
        insets: UiInsets = UiInsets.Zero,
        horizontalArrangement: Arrangement = defaultArrangement(),
        testTag: String? = null,
        content: RowScope.() -> Unit
    ) {
        createRow(
            slot = slot,
            gap = horizontalArrangement.baseSpacingPx(),
            insets = insets,
            horizontalArrangement = horizontalArrangement,
            testTag = testTag
        ).content()
    }

    fun box(
        slot: UiSlot,
        insets: UiInsets = UiInsets.Zero,
        contentAlignment: UiAlignment = UiAlignment.TopStart,
        testTag: String? = null,
        content: BoxScope.() -> Unit
    ) {
        createBox(
            slot = slot,
            insets = insets,
            contentAlignment = contentAlignment,
            testTag = testTag
        ).content()
    }

    fun absolute(
        slot: UiSlot,
        insets: UiInsets = UiInsets.Zero,
        testTag: String? = null,
        content: AbsoluteScope.() -> Unit
    ) {
        createAbsolute(
            slot = slot,
            insets = insets,
            testTag = testTag
        ).content()
    }

    @Deprecated(
        message = "Use finishFrame().ownership instead of reading intermediate input ownership directly from UiContext."
    )
    fun inputResult(): UiInputResult = runtime.inputResult()

    @Deprecated(
        message = "Use finishFrame().primitives as the single public frame result."
    )
    fun endFrame(): List<UiDrawPrimitive> = runtime.endFrame()

    fun finishFrame(): UiFrameOutput = runtime.finishFrame()

    internal fun onOverScrollableInternal() {
        runtime.onOverScrollable(measuring)
    }

    @Deprecated(
        message = "Scrollable widget ownership should be coordinated from UiScope helpers, not public UiContext."
    )
    fun onOverScrollable() = onOverScrollableInternal()

    internal fun onScrollConsumedInternal() {
        runtime.onScrollConsumed(measuring)
    }

    @Deprecated(
        message = "Scrollable widget ownership should be coordinated from UiScope helpers, not public UiContext."
    )
    fun onScrollConsumed() = onScrollConsumedInternal()

    @Deprecated(
        message = "Use finishFrame().semantics as the single public frame result."
    )
    fun semanticNodes(): List<UiSemanticNode> = runtime.semanticNodes()

    internal fun hitTestInternal(slot: UiSlot): Boolean =
        runtime.hitTest(slot, measuring)

    internal fun isActiveInternal(id: String): Boolean = runtime.isActive(id)

    internal fun tryClaimActiveInternal(id: String, hovered: Boolean) {
        runtime.tryClaimActive(id, hovered, measuring)
    }

    internal fun releaseActiveIfMatchesInternal(id: String) {
        runtime.releaseActiveIfMatches(id, measuring)
    }

    internal fun isFocusedInternal(id: String): Boolean = runtime.isFocused(id)

    internal fun requestFocusInternal(id: String) {
        runtime.requestFocus(id, measuring)
    }

    internal fun clearFocusIfMatchesInternal(id: String) {
        runtime.clearFocusIfMatches(id, measuring)
    }

    internal fun emitInternal(p: UiDrawPrimitive) {
        runtime.emit(p, measuring)
    }

    internal fun emitOverlayInternal(p: UiDrawPrimitive) {
        runtime.emitOverlay(p, measuring)
    }

    internal fun widgetStateInternal(id: String): WidgetState = runtime.widgetState(id)

    internal fun recordSemanticInternal(node: UiSemanticNode) {
        runtime.recordSemantic(node, measuring)
    }

    internal fun pushClipInternal(rect: UiSlot): UiSlot = runtime.pushClip(rect)

    @Deprecated(
        message = "Prefer clip helpers or UiScope-scoped clipping instead of manipulating UiContext clip stacks directly."
    )
    fun pushClip(rect: UiSlot): UiSlot = pushClipInternal(rect)

    internal fun popClipInternal(): UiSlot = runtime.popClip()

    @Deprecated(
        message = "Prefer clip helpers or UiScope-scoped clipping instead of manipulating UiContext clip stacks directly."
    )
    fun popClip(): UiSlot = popClipInternal()

    internal fun pointerDownEdgeInternal(): Boolean = runtime.pointerDownEdge()

    @Deprecated(
        message = "Pointer edge state should be read from UiScope helpers inside composition."
    )
    fun pointerDownEdge(): Boolean = pointerDownEdgeInternal()

    internal fun setActiveInternal(id: String?) {
        runtime.setActive(id)
    }

    @Deprecated(
        message = "Active-state mutation belongs to widgets and scopes, not public UiContext callers."
    )
    fun setActive(id: String?) {
        setActiveInternal(id)
    }

    @Deprecated(
        message = "Focus queries should go through UiScope helpers inside composition."
    )
    fun isFocused(id: String): Boolean = isFocusedInternal(id)

    @Deprecated(
        message = "Focus mutation should go through UiScope helpers inside composition."
    )
    fun requestFocus(id: String) = requestFocusInternal(id)

    @Deprecated(
        message = "Focus mutation should go through UiScope helpers inside composition."
    )
    fun clearFocusIfMatches(id: String) = clearFocusIfMatchesInternal(id)

    internal fun frameDeltaSecondsInternal(): Float = runtime.frameDeltaSeconds

    @Deprecated(
        message = "Frame metrics should be read from UiScope helpers inside composition."
    )
    fun frameDeltaSeconds(): Float = frameDeltaSecondsInternal()

    internal fun frameBoundsInternal(): UiSlot = runtime.fullFrameRect

    @Deprecated(
        message = "Frame metrics should be read from UiScope helpers inside composition."
    )
    fun frameBounds(): UiSlot = frameBoundsInternal()

    internal fun isMeasuringInternal(): Boolean = measuring

    @Deprecated(
        message = "Measurement mode is engine plumbing; prefer UiScope/layout helpers instead of branching on UiContext."
    )
    fun isMeasuring(): Boolean = isMeasuringInternal()

    internal fun recordMeasuredSlot(slot: UiSlot) {
        measurement.record(slot, measuring)
    }

    internal fun measuredContentSnapshot(): UiMeasuredContent = measurement.snapshot()

    internal fun measureColumnContentInternal(
        width: Float,
        gap: Float = UiSpacing.sm.toPx(),
        insets: UiInsets = UiInsets.Zero,
        content: ColumnScope.(slot: UiSlot) -> Unit
    ): UiMeasuredContent = measurement.measureColumnContent(
        width = width,
        gap = gap,
        insets = insets,
        sourceContext = this,
        content = content
    )

    @Deprecated(
        message = "Measurement should be coordinated from UiScope/layout helpers, not from the public UiContext surface."
    )
    fun measureColumnContent(
        width: Float,
        gap: Float = UiSpacing.sm.toPx(),
        insets: UiInsets = UiInsets.Zero,
        content: ColumnScope.(slot: UiSlot) -> Unit
    ): UiMeasuredContent = measureColumnContentInternal(
        width = width,
        gap = gap,
        insets = insets,
        content = content
    )

    internal fun measureRowContentInternal(
        height: Float,
        gap: Float,
        insets: UiInsets = UiInsets.Zero,
        content: RowScope.(slot: UiSlot) -> Unit
    ): UiMeasuredContent = measurement.measureRowContent(
        height = height,
        gap = gap,
        insets = insets,
        sourceContext = this,
        content = content
    )

    @Deprecated(
        message = "Measurement should be coordinated from UiScope/layout helpers, not from the public UiContext surface."
    )
    fun measureRowContent(
        height: Float,
        gap: Float,
        insets: UiInsets = UiInsets.Zero,
        content: RowScope.(slot: UiSlot) -> Unit
    ): UiMeasuredContent = measureRowContentInternal(
        height = height,
        gap = gap,
        insets = insets,
        content = content
    )

    internal fun pointerXInternal(): Float = runtime.inputState.pointerX
    internal fun pointerYInternal(): Float = runtime.inputState.pointerY
    internal fun pointerDownInternal(): Boolean = runtime.inputState.pointerDown

    @Deprecated(
        message = "Pointer coordinates should be read from UiScope helpers inside composition."
    )
    fun pointerX(): Float = pointerXInternal()

    @Deprecated(
        message = "Pointer coordinates should be read from UiScope helpers inside composition."
    )
    fun pointerY(): Float = pointerYInternal()

    @Deprecated(
        message = "Pointer state should be read from UiScope helpers inside composition."
    )
    fun pointerDown(): Boolean = pointerDownInternal()
}
