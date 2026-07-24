package io.github.ronjunevaldoz.awake.ui.layouts

import io.github.ronjunevaldoz.awake.ui.modifier.Dimension
import io.github.ronjunevaldoz.awake.ui.UiAlignment
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot

/**
 * Every [claimSlot] call returns the same fixed rect -- for a single fixed-position widget,
 * or deliberately overlapping/stacked content (a background quad behind a label, etc).
 */
class BoxScope internal constructor(
    context: UiContext,
    private val x: Float,
    private val y: Float,
    private val width: Float,
    private val height: Float,
    internal val contentAlignment: UiAlignment = UiAlignment.TopStart,
    override val testTag: String? = null,
    override val hasBoundedFillWidth: Boolean = true,
    override val hasBoundedFillHeight: Boolean = true,
    emitToOverlay: Boolean = false
) : AbstractUiScope(context, emitToOverlay), FillAwareScope {
    override val fillWidth: Float? = this.width
    override val fillHeight: Float? = this.height

    override fun claimSlot(width: Dimension, height: Dimension): UiSlot =
        UiSlot(x, y, this.width, this.height).also(context::recordMeasuredSlot)
}
