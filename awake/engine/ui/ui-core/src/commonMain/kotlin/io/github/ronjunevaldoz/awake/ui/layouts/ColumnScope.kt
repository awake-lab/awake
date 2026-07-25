package io.github.ronjunevaldoz.awake.ui.layouts

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

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
    val gap: Float,
    val verticalArrangement: Arrangement = defaultArrangement(),
    override val testTag: String? = null,
    override val hasBoundedFillWidth: Boolean = true,
    override val hasBoundedFillHeight: Boolean = height != null,
    emitToOverlay: Boolean = false,
    private val plannedSlots: List<UiSlot>? = null
) : AbstractUiScope(context, emitToOverlay), FillAwareScope {
    override val fillWidth: Float = width
    override val fillHeight: Float?
        get() = height?.let { (it - (cursorY - startY)).coerceAtLeast(0f) }

    var cursorY: Float = startY
        private set
    private var plannedIndex: Int = 0

    override fun claimSlot(width: Dimension, height: Dimension, weight: LayoutWeight?): UiSlot {
        plannedSlots?.let { slots ->
            val slot = slots[plannedIndex++]
            context.recordMeasuredSlot(slot)
            context.recordMeasuredWeight(weight)
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
        val slot = UiSlot(
            x,
            cursorY,
            resolvedWidth,
            resolvedHeight
        )
        cursorY += resolvedHeight + gap
        context.recordMeasuredSlot(slot)
        context.recordMeasuredWeight(weight)
        return slot
    }
}
