package io.github.ronjunevaldoz.awake.ui.layouts

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

/**
 * Horizontal counterpart to [ColumnScope] -- advances an X cursor instead of Y. Each
 * [claimSlot] call reserves the next column and advances the cursor by that column's width
 * plus [gap].
 */
class RowScope internal constructor(
    context: UiContext,
    private val startX: Float,
    private val y: Float,
    val width: Float? = null,
    val height: Float,
    val gap: Float,
    val horizontalArrangement: Arrangement = defaultArrangement(),
    override val testTag: String? = null,
    override val hasBoundedFillWidth: Boolean = width != null,
    override val hasBoundedFillHeight: Boolean = true,
    emitToOverlay: Boolean = false,
    private val plannedSlots: List<UiSlot>? = null,
    /** Container-level cross-axis default -- matches Compose's `Row(verticalAlignment = ...)`.
     * Every child not carrying its own explicit `.align(...)` falls back to this via
     * `claimModifiedSlot()`'s `defaultAlignment()`, which widens that child's alignment
     * container up to this row's own full height first (see
     * `UiScopeMetrics.crossAxisAlignmentContainer`) so there's real slack to center/end into. */
    val verticalAlignment: UiAlignment.Vertical = UiAlignment.Vertical.Top
) : AbstractUiScope(context, emitToOverlay), FillAwareScope {
    var cursorX: Float = startX
        private set
    private var plannedIndex: Int = 0

    override val fillWidth: Float?
        get() = width?.let { (it - (cursorX - startX)).coerceAtLeast(0f) }
    override val fillHeight: Float? = height

    override fun claimSlot(width: Dimension, height: Dimension, weight: LayoutWeight?): UiSlot {
        plannedSlots?.let { slots ->
            val slot = slots[plannedIndex++]
            context.recordMeasuredSlot(slot)
            context.recordMeasuredWeight(weight)
            return slot
        }
        // A weighted child's own width defaults to WrapContent (see claimModifiedSlot), which
        // Dimension.resolve() can't handle -- weight() replaces it with the width axis's normal
        // FillMax behavior here so [io.github.ronjunevaldoz.awake.ui.layouts.UiScope.row]'s weight-distribution pass
        // (over the resulting measured widths) has something meaningful to work with.
        val effectiveWidth = if (weight != null && width == Dimension.WrapContent) Dimension.FillMax else width
        val resolvedWidth = effectiveWidth.resolve {
            val availableWidth = this.width ?: (context.frameBoundsInternal().width - cursorX)
            (availableWidth - (cursorX - startX)).coerceAtLeast(0f)
        }
        val resolvedHeight = height.resolve { this.height }
        val slot = UiSlot(
            cursorX,
            y,
            resolvedWidth,
            resolvedHeight
        )
        cursorX += resolvedWidth + gap
        context.recordMeasuredSlot(slot)
        context.recordMeasuredWeight(weight)
        return slot
    }
}
