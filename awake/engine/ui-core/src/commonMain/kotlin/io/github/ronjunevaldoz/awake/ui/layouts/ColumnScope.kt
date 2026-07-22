package io.github.ronjunevaldoz.awake.ui.layouts

import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.UiContext
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.UiTheme
import io.github.ronjunevaldoz.awake.ui.font.UiFont

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
    emitToOverlay: Boolean = false
) : AbstractUiScope(context, emitToOverlay), FillAwareScope {
    override val fillWidth: Float = width
    override val fillHeight: Float?
        get() = height?.let { (it - (cursorY - startY)).coerceAtLeast(0f) }

    var cursorY: Float = startY
        private set

    override fun claimSlot(width: Dimension, height: Dimension): UiSlot {
        val resolvedWidth = width.resolve { this.width }
        val resolvedHeight = height.resolve {
            val availableHeight = this.height ?: (context.frameBounds().height - cursorY)
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
        return slot
    }
}