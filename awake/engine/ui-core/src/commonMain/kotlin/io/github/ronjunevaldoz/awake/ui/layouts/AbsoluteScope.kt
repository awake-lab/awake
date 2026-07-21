package io.github.ronjunevaldoz.awake.ui.layouts

import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.UiContext
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.UiTheme
import io.github.ronjunevaldoz.awake.ui.font.UiFont

/**
 * Manual placement at an exact x/y, ignoring whatever width/height a widget requests as a
 * layout hint -- the escape hatch for HUD text or a minimap thumbnail that isn't part of any
 * auto-layout column, still going through the same [io.github.ronjunevaldoz.awake.ui.UiScope] surface as everything else.
 */
class AbsoluteScope internal constructor(
    context: UiContext,
    font: UiFont?,
    theme: UiTheme,
    private val x: Float,
    private val y: Float,
    textScale: Float = 1f,
    emitToOverlay: Boolean = false
) : AbstractUiScope(context, font, theme, textScale, emitToOverlay), FillAwareScope {
    override val fillWidth: Float? = null
    override val fillHeight: Float? = null

    // Unlike ColumnScope/RowScope, the original claimSlot(width: Float, height: Float) never
    // special-cased any value here -- it passed width/height straight through, and a caller
    // passing 0f (e.g. text()'s own default slot, which doesn't need a real width when not
    // centered) got a harmless zero-width UiSlot back. FillMax has no configured size to
    // resolve against on this scope, so it resolves to 0f -- the same literal passthrough
    // behavior, not a new error mode.
    override fun claimSlot(width: Dimension, height: Dimension): UiSlot =
        UiSlot(
            x,
            y,
            width.resolve { 4096f },
            height.resolve { 4096f }).also(context::recordMeasuredSlot)
}