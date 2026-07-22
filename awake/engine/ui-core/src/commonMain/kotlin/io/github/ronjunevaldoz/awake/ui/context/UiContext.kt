// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.context

import io.github.ronjunevaldoz.awake.ui.TextStyle
import io.github.ronjunevaldoz.awake.ui.UiAlignment
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.UiInsets
import io.github.ronjunevaldoz.awake.ui.UiSemanticNode
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.UiTheme
import io.github.ronjunevaldoz.awake.ui.WidgetState
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.layouts.AbsoluteScope
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.UiSpacing
import io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement
import io.github.ronjunevaldoz.awake.ui.toPx
import kotlin.reflect.KClass

/**
 * Minimal immediate-mode UI context -- ImGui's own architecture (hot/active id tracking, no
 * retained widget tree, no ECS entities). Pure layout and spatial hit-testing engine.
 */
class UiContext internal constructor(
    private val measuring: Boolean = false
) {
    constructor() : this(measuring = false)

    private val stacks = UiContextStacks()
    private val runtime = UiRuntimeCoordinator()
    private val measurement = UiMeasurementRuntime()
    private val services = UiContextServiceRegistry()
    private val layouts = UiLayoutFactory(this)

    val currentTheme: UiTheme get() = stacks.currentTheme
    val currentTextStyle: TextStyle get() = stacks.currentTextStyle
    val currentFont get() = stacks.currentFont
    val inputState: UiInputState get() = runtime.inputState

    fun pushTheme(theme: UiTheme) = stacks.pushTheme(theme)
    fun popTheme() = stacks.popTheme()

    fun pushTextStyle(style: TextStyle) = stacks.pushTextStyle(style)
    fun popTextStyle() = stacks.popTextStyle()

    fun pushFont(font: UiFont) = stacks.pushFont(font)
    fun popFont() = stacks.popFont()

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
        runtime.beginFrame(screenWidth, screenHeight, inputState, deltaSeconds)
        measurement.beginFrame()
        services.clear()
    }

    fun bindServiceResolver(resolver: ((KClass<*>) -> Any?)?) {
        services.bind(resolver)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> resolveService(type: KClass<T>): T? = services.resolve(type) as? T

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

    fun inputResult(): UiInputResult = runtime.inputResult()

    fun endFrame(): List<UiDrawPrimitive> = runtime.endFrame()

    fun onOverScrollable() {
        runtime.onOverScrollable(measuring)
    }

    fun onScrollConsumed() {
        runtime.onScrollConsumed(measuring)
    }

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

    fun pushClip(rect: UiSlot): UiSlot = pushClipInternal(rect)

    internal fun popClipInternal(): UiSlot = runtime.popClip()

    fun popClip(): UiSlot = popClipInternal()

    fun pointerDownEdge(): Boolean = runtime.pointerDownEdge()

    fun setActive(id: String?) {
        runtime.setActive(id)
    }

    fun isFocused(id: String): Boolean = isFocusedInternal(id)

    fun requestFocus(id: String) = requestFocusInternal(id)

    fun clearFocusIfMatches(id: String) = clearFocusIfMatchesInternal(id)

    fun frameDeltaSeconds(): Float = runtime.frameDeltaSeconds

    fun frameBounds(): UiSlot = runtime.fullFrameRect

    fun isMeasuring(): Boolean = measuring

    internal fun recordMeasuredSlot(slot: UiSlot) {
        measurement.record(slot, measuring)
    }

    internal fun measuredContentSnapshot(): UiMeasuredContent = measurement.snapshot()

    fun measureColumnContent(
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

    fun measureRowContent(
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

    fun pointerX(): Float = runtime.inputState.pointerX
    fun pointerY(): Float = runtime.inputState.pointerY
    fun pointerDown(): Boolean = runtime.inputState.pointerDown
}
