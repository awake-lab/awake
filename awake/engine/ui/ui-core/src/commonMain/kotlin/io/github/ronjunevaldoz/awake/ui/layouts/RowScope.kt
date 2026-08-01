package io.github.ronjunevaldoz.awake.ui.layouts

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layout.*

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
    val horizontalArrangement: Arrangement = defaultArrangement(),
    override val testTag: String? = null,
    override val hasBoundedFillWidth: Boolean = width != null,
    override val hasBoundedFillHeight: Boolean = true,
    emitToOverlay: Boolean = false,
    private val plannedSlots: List<UiBounds>? = null,
    /** Container-level cross-axis default -- matches Compose's `Row(verticalAlignment = ...)`.
     * Every child not carrying its own explicit `.align(...)` falls back to this via
     * `claimModifiedSlot()`'s `defaultAlignment()`, which widens that child's alignment
     * container up to this row's own full height first (see
     * `UiScopeMetrics.crossAxisAlignmentContainer`) so there's real slack to center/end into. */
    val verticalAlignment: UiAlignment.Vertical = UiAlignment.Vertical.Top
) : AbstractUiScope(context, emitToOverlay), FillAwareScope {
    /** Cursor advance between successive children -- derived from [horizontalArrangement] rather
     * than threaded separately, since it was always just `horizontalArrangement.baseSpacingPx()`
     * at every real call site (see UiLayoutFactory). */
    val gap: Float = horizontalArrangement.baseSpacingPx()
    var cursorX: Float = startX
        private set
    private var plannedIndex: Int = 0

    override val fillWidth: Float?
        get() = width?.let { (it - (cursorX - startX)).coerceAtLeast(0f) }
    override val fillHeight: Float? = height

    override fun claimSlot(width: Dimension, height: Dimension, weight: LayoutWeight?): UiBounds {
        plannedSlots?.let { slots ->
            val slot = slots[plannedIndex++]
            context.recordMeasuredSlot(slot)
            context.recordMeasuredWeight(weight)
            context.recordMeasuredMainAxisFill(weight == null && width == Dimension.FillMax)
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
        val slot = UiBounds(
            cursorX,
            y,
            resolvedWidth,
            resolvedHeight
        )
        cursorX += resolvedWidth + gap
        // height == FillMax means this child stretches to whatever height the row ends up
        // with (its cross axis) -- it has no intrinsic height of its own, so it must not
        // dictate the row's own WrapContent height back up. Symmetric with ColumnScope's
        // contributesToWrapWidth = width != Dimension.FillMax (its cross axis) above -- see
        // UiContextMeasureState.record(). Without this, a weight()-tagged column() inside a
        // WrapContent-height row (which defaults to FillMax height, not WrapContent -- see
        // RowScope.column()) inherits that row's own sizing-trial placeholder bound as its
        // "real" height, inflating the row's resolved WrapContent height (task #34).
        context.recordMeasuredSlot(slot, contributesToWrapHeight = height != Dimension.FillMax)
        context.recordMeasuredWeight(weight)
        // Main axis for a row is width -- a non-weighted child that claimed Dimension.FillMax
        // here has no real "intrinsic" width of its own (its trial size is just whatever the
        // cursor happened to have left at trial time, not a real occupied claim) -- see
        // resolveWeightedMainAxis()'s doc comment for why this must not count as "occupied"
        // space that starves a weighted sibling.
        context.recordMeasuredMainAxisFill(weight == null && effectiveWidth == Dimension.FillMax)
        return slot
    }
}
