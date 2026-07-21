package io.github.ronjunevaldoz.awake.ui.layouts

import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.UiAlignment
import io.github.ronjunevaldoz.awake.ui.UiContext
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.UiTheme
import io.github.ronjunevaldoz.awake.ui.font.UiFont

/**
 * Every [claimSlot] call returns the same fixed rect -- for a single fixed-position widget,
 * or deliberately overlapping/stacked content (a background quad behind a label, etc).
 */
class BoxScope internal constructor(
    context: UiContext,
    font: UiFont?,
    theme: UiTheme,
    private val x: Float,
    private val y: Float,
    private val width: Float,
    private val height: Float,
    internal val contentAlignment: UiAlignment = UiAlignment.TopStart,
    textScale: Float = 1f,
    emitToOverlay: Boolean = false
) : AbstractUiScope(context, font, theme, textScale, emitToOverlay), FillAwareScope {
    override val fillWidth: Float? = this.width
    override val fillHeight: Float? = this.height

    override fun claimSlot(width: Dimension, height: Dimension): UiSlot =
        UiSlot(x, y, this.width, this.height).also(context::recordMeasuredSlot)
}