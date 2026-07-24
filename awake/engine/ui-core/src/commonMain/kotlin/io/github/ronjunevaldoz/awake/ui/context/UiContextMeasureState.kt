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

    fun beginFrame() {
        measuredMaxRight = 0f
        measuredMaxBottom = 0f
        measuredSlots.clear()
    }

    fun record(slot: UiSlot, measuring: Boolean) {
        if (!measuring) return
        measuredMaxRight = max(measuredMaxRight, slot.x + slot.width)
        measuredMaxBottom = max(measuredMaxBottom, slot.y + slot.height)
        measuredSlots += slot
    }

    fun measureColumnContent(
        width: Float,
        gap: Float,
        insets: UiInsets,
        sourceContext: UiContext,
        content: ColumnScope.(slot: UiSlot) -> Unit
    ): UiMeasuredContent {
        val measureContext = createMeasureContext(sourceContext)
        val outerSlot = UiSlot(0f, 0f, width.coerceAtLeast(0f), 100_000f)
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
        content: RowScope.(slot: UiSlot) -> Unit
    ): UiMeasuredContent {
        val measureContext = createMeasureContext(sourceContext)
        val outerSlot = UiSlot(0f, 0f, 100_000f, height.coerceAtLeast(0f))
        val measureScope = measureContext.createRow(
            slot = outerSlot,
            gap = gap,
            insets = insets
        )
        measureScope.content(outerSlot)
        return measureContext.measuredContentSnapshot()
    }

    private fun createMeasureContext(sourceContext: UiContext): UiContext {
        val measureContext = UiContext(measuring = true)
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
