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

    fun record(slot: UiSlot, contributesToWrapWidth: Boolean = true) {
        measureState.record(slot, contributesToWrapWidth)
    }

    fun recordWeight(weight: LayoutWeight?) {
        measureState.recordWeight(weight)
    }

    fun snapshot(): UiMeasuredContent = UiMeasuredContent(
        // Hug the non-fill content's own extent when there is any -- only fall back to the
        // (possibly FillMax-inflated) full max when *every* child was FillMax-width, so a lone
        // wrap-bounded child (see UiContextMeasureState.measuredMaxRightExcludingFill) doesn't
        // collapse a WrapContent container to zero.
        width = measureState.measuredMaxRightExcludingFill.takeIf { it > 0f } ?: measureState.measuredMaxRight,
        height = measureState.measuredMaxBottom,
        slots = measureState.measuredSlots.toList(),
        weights = measureState.measuredWeights.toList()
    )

    fun measureColumnContent(
        width: Float,
        gap: Float = UiSpacing.sm.toPx(),
        insets: UiInsets = UiInsets.Zero,
        height: Float = 100_000f,
        sourceContext: UiContext,
        content: ColumnScope.(slot: UiSlot) -> Unit
    ): UiMeasuredContent = measureState.measureColumnContent(
        width = width,
        gap = gap,
        insets = insets,
        height = height,
        sourceContext = sourceContext,
        content = content
    )

    fun measureRowContent(
        height: Float,
        gap: Float,
        insets: UiInsets = UiInsets.Zero,
        width: Float = 100_000f,
        sourceContext: UiContext,
        content: RowScope.(slot: UiSlot) -> Unit
    ): UiMeasuredContent = measureState.measureRowContent(
        height = height,
        gap = gap,
        insets = insets,
        width = width,
        sourceContext = sourceContext,
        content = content
    )
}
