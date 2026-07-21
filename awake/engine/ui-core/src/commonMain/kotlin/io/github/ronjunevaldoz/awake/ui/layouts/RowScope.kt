package io.github.ronjunevaldoz.awake.ui.layouts

import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.UiContext
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.UiTheme
import io.github.ronjunevaldoz.awake.ui.font.UiFont

/**
 * Horizontal counterpart to [ColumnScope] -- advances an X cursor instead of Y. Each
 * [claimSlot] call reserves the next column and advances the cursor by that column's width
 * plus [gap].
 */
class RowScope internal constructor(
    context: UiContext,
    font: UiFont?,
    theme: UiTheme,
    startX: Float,
    private val y: Float,
    private val height: Float,
    private val gap: Float,
    textScale: Float = 1f,
    emitToOverlay: Boolean = false
) : AbstractUiScope(context, font, theme, textScale, emitToOverlay), FillAwareScope {
    var cursorX: Float = startX
        private set

    override val fillWidth: Float? = null
    override val fillHeight: Float? = height

    override fun claimSlot(width: Dimension, height: Dimension): UiSlot {
        val resolvedWidth = width.resolve { error("FillMax width has no meaning in a RowScope") }
        val resolvedHeight = height.resolve { this.height }
        val slot = UiSlot(
            cursorX,
            y,
            resolvedWidth,
            resolvedHeight
        )
        cursorX += resolvedWidth + gap
        context.recordMeasuredSlot(slot)
        return slot
    }
}