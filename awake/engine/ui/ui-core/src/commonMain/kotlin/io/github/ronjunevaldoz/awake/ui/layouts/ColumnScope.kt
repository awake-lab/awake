// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.layouts

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layout.LayoutWeight
import io.github.ronjunevaldoz.awake.ui.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds

/**
 * Vertical auto-stacking layout -- replaces every hand-written `PANEL_ROW_*_Y` constant a
 * consumer used to maintain by hand. Each [claimSlot] call reserves the next row and advances
 * the cursor by that row's height plus [gap].
 */
class ColumnScope internal constructor(
    context: UiContext,
    private val x: Float,
    private val startY: Float,
    val width: Float,
    val height: Float? = null,
    val verticalArrangement: Arrangement = defaultArrangement(),
    override val testTag: String? = null,
    override val hasBoundedFillWidth: Boolean = true,
    override val hasBoundedFillHeight: Boolean = height != null,
    emitToOverlay: Boolean = false,
    private val plannedSlots: List<UiBounds>? = null,
    /** Container-level cross-axis default -- matches Compose's `Column(horizontalAlignment =
     * ...)`. See [RowScope.verticalAlignment] for the matching row-side explanation. */
    val horizontalAlignment: UiAlignment.Horizontal = UiAlignment.Horizontal.Start,
) : AbstractUiScope(context, emitToOverlay),
    FillAwareScope {
    /** Cursor advance between successive children -- derived from [verticalArrangement] rather
     * than threaded separately, since it was always just `verticalArrangement.baseSpacingPx()`
     * at every real call site (see UiLayoutFactory). */
    val gap: Float = verticalArrangement.baseSpacingPx()
    override val fillWidth: Float = width
    override val fillHeight: Float?
        get() = height?.let { (it - (cursorY - startY)).coerceAtLeast(0f) }

    var cursorY: Float = startY
        private set
    private var plannedIndex: Int = 0

    override fun claimSlot(width: Dimension, height: Dimension, weight: LayoutWeight?): UiBounds {
        plannedSlots?.let { slots ->
            val slot = slots[plannedIndex++]
            context.recordMeasuredSlot(slot)
            context.recordMeasuredWeight(weight)
            context.recordMeasuredMainAxisFill(weight == null && height == Dimension.FillMax)
            return slot
        }
        val resolvedWidth = width.resolve { this.width }
        // See RowScope.claimSlot -- symmetric WrapContent-to-FillMax substitution on the main
        // (height) axis for a weighted child.
        val effectiveHeight = if (weight != null && height == Dimension.WrapContent) Dimension.FillMax else height
        val resolvedHeight = effectiveHeight.resolve {
            val availableHeight = this.height ?: (context.frameBoundsInternal().height - cursorY)
            (availableHeight - (cursorY - startY)).coerceAtLeast(0f)
        }
        val slot = UiBounds(
            x,
            cursorY,
            resolvedWidth,
            resolvedHeight,
        )
        cursorY += resolvedHeight + gap
        // width == FillMax means this child fills whatever width the column ends up with -- it
        // has no intrinsic width of its own, so it must not dictate the column's own WrapContent
        // width back up. See UiContextMeasureState.record().
        context.recordMeasuredSlot(slot, contributesToWrapWidth = width != Dimension.FillMax)
        context.recordMeasuredWeight(weight)
        // Main axis for a column is height -- see RowScope.claimSlot's matching comment
        // (height-side counterpart) for why a non-weighted FillMax child's trial size must not
        // count as "occupied" in resolveWeightedMainAxis().
        context.recordMeasuredMainAxisFill(weight == null && effectiveHeight == Dimension.FillMax)
        return slot
    }
}
