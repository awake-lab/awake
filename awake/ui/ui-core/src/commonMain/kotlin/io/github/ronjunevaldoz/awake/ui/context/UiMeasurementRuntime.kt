// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.context

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.LayoutWeight
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.toPx

internal class UiMeasurementRuntime(
    private val measureState: UiContextMeasureState = UiContextMeasureState(),
) {
    fun beginFrame() {
        measureState.beginFrame()
    }

    fun record(
        slot: UiBounds,
        contributesToWrapWidth: Boolean = true,
        contributesToWrapHeight: Boolean = true,
        contributesToChildList: Boolean = true,
    ) {
        measureState.record(slot, contributesToWrapWidth, contributesToWrapHeight, contributesToChildList)
    }

    fun recordWeight(weight: LayoutWeight?, contributesToChildList: Boolean = true) {
        measureState.recordWeight(weight, contributesToChildList)
    }

    fun recordMainAxisFill(fillsMainAxis: Boolean, contributesToChildList: Boolean = true) {
        measureState.recordMainAxisFill(fillsMainAxis, contributesToChildList)
    }

    fun snapshot(): UiMeasuredContent = UiMeasuredContent(
        // Hug the non-fill content's own extent when there is any -- only fall back to the
        // (possibly FillMax-inflated) full max when *every* child was FillMax-width, so a lone
        // wrap-bounded child (see UiContextMeasureState.measuredMaxRightExcludingFill) doesn't
        // collapse a WrapContent container to zero.
        width = measureState.measuredMaxRightExcludingFill.takeIf { it > 0f } ?: measureState.measuredMaxRight,
        // Symmetric with width above -- hug the non-fill content's own extent when there is any,
        // only falling back to the (possibly FillMax-inflated) full max when *every* child was
        // FillMax-height (e.g. a lone stretch-to-fill child with no real intrinsic height of its
        // own to hug).
        height = measureState.measuredMaxBottomExcludingFill.takeIf { it > 0f } ?: measureState.measuredMaxBottom,
        slots = measureState.measuredSlots.toList(),
        weights = measureState.measuredWeights.toList(),
        fillsMainAxis = measureState.measuredFillsMainAxis.toList(),
    )

    fun measureColumnContent(
        width: Float,
        gap: Float = 8f.dp.toPx(),
        insets: UiInsets = UiInsets.Zero,
        height: Float = UNBOUNDED_MAIN_AXIS,
        sourceContext: UiContext,
        wrapContentPass: Boolean = false,
        content: ColumnScope.(slot: UiBounds) -> Unit,
    ): UiMeasuredContent = measureState.measureColumnContent(
        width = width,
        gap = gap,
        insets = insets,
        height = height,
        sourceContext = sourceContext,
        wrapContentPass = wrapContentPass,
        content = content,
    )

    fun measureRowContent(
        height: Float,
        gap: Float,
        insets: UiInsets = UiInsets.Zero,
        width: Float = UNBOUNDED_MAIN_AXIS,
        sourceContext: UiContext,
        wrapContentPass: Boolean = false,
        content: RowScope.(slot: UiBounds) -> Unit,
    ): UiMeasuredContent = measureState.measureRowContent(
        height = height,
        gap = gap,
        insets = insets,
        width = width,
        sourceContext = sourceContext,
        wrapContentPass = wrapContentPass,
        content = content,
    )
}
