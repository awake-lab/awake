// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.context

import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.UiSpacing
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

internal class UiMeasurementRuntime(
    private val measureState: UiContextMeasureState = UiContextMeasureState()
) {
    fun beginFrame() {
        measureState.beginFrame()
    }

    fun record(slot: UiSlot) {
        measureState.record(slot)
    }

    fun snapshot(): UiMeasuredContent = UiMeasuredContent(
        width = measureState.measuredMaxRight,
        height = measureState.measuredMaxBottom,
        slots = measureState.measuredSlots.toList()
    )

    fun measureColumnContent(
        width: Float,
        gap: Float = UiSpacing.sm.toPx(),
        insets: UiInsets = UiInsets.Zero,
        sourceContext: UiContext,
        content: ColumnScope.(slot: UiSlot) -> Unit
    ): UiMeasuredContent = measureState.measureColumnContent(
        width = width,
        gap = gap,
        insets = insets,
        sourceContext = sourceContext,
        content = content
    )

    fun measureRowContent(
        height: Float,
        gap: Float,
        insets: UiInsets = UiInsets.Zero,
        sourceContext: UiContext,
        content: RowScope.(slot: UiSlot) -> Unit
    ): UiMeasuredContent = measureState.measureRowContent(
        height = height,
        gap = gap,
        insets = insets,
        sourceContext = sourceContext,
        content = content
    )
}
