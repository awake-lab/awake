// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.context

import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layouts.AbsoluteScope
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement

/**
 * The bookkeeping flags every [UiLayoutFactory] `create*` call repeats identically --
 * [testTag]/[hasBoundedFillWidth]/[hasBoundedFillHeight]/[overlayOnly] -- grouped into one
 * value instead of four separate parameters per function. Not a layout *sizing* constraint
 * (see [Dimension]) -- purely scope bookkeeping, hence "tracking" rather than "constraints".
 */
internal data class UiLayoutTracking(
    val testTag: String? = null,
    val hasBoundedFillWidth: Boolean = true,
    val hasBoundedFillHeight: Boolean = true,
    val overlayOnly: Boolean = false,
)

internal class UiLayoutFactory(
    private val context: UiContext,
) {
    fun createColumn(
        x: Float,
        y: Float,
        width: Float,
        height: Float? = null,
        verticalArrangement: Arrangement = defaultArrangement(),
        tracking: UiLayoutTracking = UiLayoutTracking(hasBoundedFillHeight = height != null),
        plannedSlots: List<UiBounds>? = null,
        horizontalAlignment: UiAlignment.Horizontal = UiAlignment.Horizontal.Start,
    ): ColumnScope = ColumnScope(
        context,
        x,
        y,
        width,
        height,
        verticalArrangement,
        tracking.testTag,
        tracking.hasBoundedFillWidth,
        tracking.hasBoundedFillHeight,
        tracking.overlayOnly,
        plannedSlots,
        horizontalAlignment,
    )

    fun createColumn(
        slot: UiBounds,
        insets: UiInsets = UiInsets.Zero,
        verticalArrangement: Arrangement = defaultArrangement(),
        tracking: UiLayoutTracking = UiLayoutTracking(),
        plannedSlots: List<UiBounds>? = null,
        horizontalAlignment: UiAlignment.Horizontal = UiAlignment.Horizontal.Start,
    ): ColumnScope {
        val content = slot.inset(insets)
        return createColumn(
            x = content.x,
            y = content.y,
            width = content.width,
            height = content.height,
            verticalArrangement = verticalArrangement,
            tracking = tracking,
            plannedSlots = plannedSlots,
            horizontalAlignment = horizontalAlignment,
        )
    }

    fun createAbsolute(
        x: Float,
        y: Float,
        testTag: String? = null,
        overlayOnly: Boolean = false,
    ): AbsoluteScope = AbsoluteScope(
        context = context,
        x = x,
        y = y,
        testTag = testTag,
        emitToOverlay = overlayOnly,
    )

    fun createAbsolute(
        slot: UiBounds,
        insets: UiInsets = UiInsets.Zero,
        testTag: String? = null,
        overlayOnly: Boolean = false,
    ): AbsoluteScope {
        val content = slot.inset(insets)
        return createAbsolute(content.x, content.y, testTag, overlayOnly)
    }

    fun createRow(
        x: Float,
        y: Float,
        height: Float,
        width: Float? = null,
        horizontalArrangement: Arrangement = defaultArrangement(),
        tracking: UiLayoutTracking = UiLayoutTracking(hasBoundedFillWidth = width != null),
        plannedSlots: List<UiBounds>? = null,
        verticalAlignment: UiAlignment.Vertical = UiAlignment.Vertical.Top,
    ): RowScope = RowScope(
        context,
        x,
        y,
        width,
        height,
        horizontalArrangement,
        tracking.testTag,
        tracking.hasBoundedFillWidth,
        tracking.hasBoundedFillHeight,
        tracking.overlayOnly,
        plannedSlots,
        verticalAlignment,
    )

    fun createRow(
        slot: UiBounds,
        insets: UiInsets = UiInsets.Zero,
        horizontalArrangement: Arrangement = defaultArrangement(),
        tracking: UiLayoutTracking = UiLayoutTracking(),
        plannedSlots: List<UiBounds>? = null,
        verticalAlignment: UiAlignment.Vertical = UiAlignment.Vertical.Top,
    ): RowScope {
        val content = slot.inset(insets)
        return createRow(
            x = content.x,
            y = content.y,
            height = content.height,
            width = content.width,
            horizontalArrangement = horizontalArrangement,
            tracking = tracking,
            plannedSlots = plannedSlots,
            verticalAlignment = verticalAlignment,
        )
    }

    fun createBox(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        contentAlignment: UiAlignment = UiAlignment.TopStart,
        tracking: UiLayoutTracking = UiLayoutTracking(),
    ): BoxScope = BoxScope(
        context,
        x,
        y,
        width,
        height,
        contentAlignment,
        tracking.testTag,
        tracking.hasBoundedFillWidth,
        tracking.hasBoundedFillHeight,
        tracking.overlayOnly,
    )

    fun createBox(
        slot: UiBounds,
        insets: UiInsets = UiInsets.Zero,
        contentAlignment: UiAlignment = UiAlignment.TopStart,
        tracking: UiLayoutTracking = UiLayoutTracking(),
    ): BoxScope {
        val content = slot.inset(insets)
        return createBox(
            x = content.x,
            y = content.y,
            width = content.width,
            height = content.height,
            contentAlignment = contentAlignment,
            tracking = tracking,
        )
    }
}
