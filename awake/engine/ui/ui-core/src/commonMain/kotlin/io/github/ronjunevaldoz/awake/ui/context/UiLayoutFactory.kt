// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.context

import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.layouts.AbsoluteScope
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.UiSpacing
import io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

internal class UiLayoutFactory(
    private val context: UiContext
) {
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
    ): ColumnScope = ColumnScope(
        context,
        x,
        y,
        width,
        height,
        gap,
        verticalArrangement,
        testTag,
        hasBoundedFillWidth,
        hasBoundedFillHeight,
        overlayOnly,
        plannedSlots
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
    ): ColumnScope {
        val content = slot.inset(insets)
        return createColumn(
            x = content.x,
            y = content.y,
            width = content.width,
            height = content.height,
            gap = gap,
            verticalArrangement = verticalArrangement,
            testTag = testTag,
            hasBoundedFillWidth = hasBoundedFillWidth,
            hasBoundedFillHeight = hasBoundedFillHeight,
            overlayOnly = overlayOnly,
            plannedSlots = plannedSlots
        )
    }

    fun createAbsolute(
        x: Float,
        y: Float,
        testTag: String? = null,
        overlayOnly: Boolean = false
    ): AbsoluteScope = AbsoluteScope(
        context = context,
        x = x,
        y = y,
        testTag = testTag,
        emitToOverlay = overlayOnly
    )

    fun createAbsolute(
        slot: UiSlot,
        insets: UiInsets = UiInsets.Zero,
        testTag: String? = null,
        overlayOnly: Boolean = false
    ): AbsoluteScope {
        val content = slot.inset(insets)
        return createAbsolute(content.x, content.y, testTag, overlayOnly)
    }

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
    ): RowScope = RowScope(
        context,
        x,
        y,
        width,
        height,
        gap,
        horizontalArrangement,
        testTag,
        hasBoundedFillWidth,
        hasBoundedFillHeight,
        overlayOnly,
        plannedSlots
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
    ): RowScope {
        val content = slot.inset(insets)
        return createRow(
            x = content.x,
            y = content.y,
            height = content.height,
            width = content.width,
            gap = gap,
            horizontalArrangement = horizontalArrangement,
            testTag = testTag,
            hasBoundedFillWidth = hasBoundedFillWidth,
            hasBoundedFillHeight = hasBoundedFillHeight,
            overlayOnly = overlayOnly,
            plannedSlots = plannedSlots
        )
    }

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
    ): BoxScope = BoxScope(
        context,
        x,
        y,
        width,
        height,
        contentAlignment,
        testTag,
        hasBoundedFillWidth,
        hasBoundedFillHeight,
        overlayOnly
    )

    fun createBox(
        slot: UiSlot,
        insets: UiInsets = UiInsets.Zero,
        contentAlignment: UiAlignment = UiAlignment.TopStart,
        testTag: String? = null,
        hasBoundedFillWidth: Boolean = true,
        hasBoundedFillHeight: Boolean = true,
        overlayOnly: Boolean = false
    ): BoxScope {
        val content = slot.inset(insets)
        return createBox(
            x = content.x,
            y = content.y,
            width = content.width,
            height = content.height,
            contentAlignment = contentAlignment,
            testTag = testTag,
            hasBoundedFillWidth = hasBoundedFillWidth,
            hasBoundedFillHeight = hasBoundedFillHeight,
            overlayOnly = overlayOnly
        )
    }
}
