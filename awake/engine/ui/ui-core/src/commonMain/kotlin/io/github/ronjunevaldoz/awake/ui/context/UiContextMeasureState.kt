// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.context

import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import kotlin.math.max
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

internal class UiContextMeasureState {
    internal var measuredMaxRight = 0f
    internal var measuredMaxBottom = 0f
    internal val measuredSlots = ArrayList<UiSlot>()
    internal val measuredWeights = ArrayList<LayoutWeight?>()

    fun beginFrame() {
        measuredMaxRight = 0f
        measuredMaxBottom = 0f
        measuredSlots.clear()
        measuredWeights.clear()
    }

    fun record(slot: UiSlot) {
        measuredMaxRight = max(measuredMaxRight, slot.x + slot.width)
        measuredMaxBottom = max(measuredMaxBottom, slot.y + slot.height)
        measuredSlots += slot
    }

    fun recordWeight(weight: LayoutWeight?) {
        measuredWeights += weight
    }

    fun measureColumnContent(
        width: Float,
        gap: Float,
        insets: UiInsets,
        sourceContext: UiContext,
        height: Float = 100_000f,
        content: ColumnScope.(slot: UiSlot) -> Unit
    ): UiMeasuredContent {
        val measureContext = createMeasureContext(sourceContext)
        val outerSlot = UiSlot(0f, 0f, width.coerceAtLeast(0f), height.coerceAtLeast(0f))
        val measureScope = measureContext.createColumn(
            slot = outerSlot,
            gap = gap,
            insets = insets
        )
        measureScope.content(outerSlot)
        return measureContext.measuredContentSnapshot()
    }

    fun measureRowContent(
        height: Float,
        gap: Float,
        insets: UiInsets,
        sourceContext: UiContext,
        width: Float = 100_000f,
        content: RowScope.(slot: UiSlot) -> Unit
    ): UiMeasuredContent {
        val measureContext = createMeasureContext(sourceContext)
        val outerSlot = UiSlot(0f, 0f, width.coerceAtLeast(0f), height.coerceAtLeast(0f))
        val measureScope = measureContext.createRow(
            slot = outerSlot,
            gap = gap,
            insets = insets
        )
        measureScope.content(outerSlot)
        return measureContext.measuredContentSnapshot()
    }

    private fun createMeasureContext(sourceContext: UiContext): UiContext {
        // Share the real state store rather than a fresh one -- a WrapContent/scroll trial pass
        // re-executes the same content, and any persisted (rememberStateValue) branch inside
        // that content (e.g. "which page is selected") must see the real current value, not
        // reset to its default. See RepoBugTest for the regression this fixes.
        val measureContext = UiContext(measuring = true, stateStore = sourceContext.stateStoreInternal())
        measureContext.beginFrame(
            UiFrameInput(
                viewportWidth = 100_000f,
                viewportHeight = 100_000f,
                input = UiInputState(),
                deltaSeconds = 0f
            )
        )
        measureContext.pushTextStyle(sourceContext.currentTextStyle)
        measureContext.pushFont(sourceContext.currentFont)
        measureContext.pushTheme(sourceContext.currentTheme)
        return measureContext
    }
}
