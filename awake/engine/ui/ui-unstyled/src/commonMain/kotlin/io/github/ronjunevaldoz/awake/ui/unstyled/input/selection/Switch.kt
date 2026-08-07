package io.github.ronjunevaldoz.awake.ui.unstyled.input.selection

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.graphics.emitFillAndBorder
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.scope.fillWidthOrNull
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.scope.recordSemantic
import io.github.ronjunevaldoz.awake.ui.withGraphicsLayerAlpha
import io.github.ronjunevaldoz.awake.ui.unstyled.paintSurface
import io.github.ronjunevaldoz.awake.ui.unstyled.resolveInteractiveSurface
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

private const val TOGGLE_WIDTH_PX = 44f
private const val TOGGLE_HEIGHT_PX = 24f
private const val TOGGLE_KNOB_INSET_PX = 2f
// Dp, not raw px: it is added to `trackSlot`/`surface.interaction.slot` coordinates that are
// already density-scaled, so a raw literal would render a half-size gap at 2x.
private val TOGGLE_LABEL_GAP = 8f.dp
private val SWITCH_LABEL_WIDTH_GUESS = 160f.dp
fun UiScope.switch(
    id: String,
    checked: Boolean,
    label: String? = null,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    enabled: Boolean = true
): Boolean {
    val theme = context.currentTheme
    // The switch track is always a fixed TOGGLE_WIDTH_PX × TOGGLE_HEIGHT_PX pill — it never
    // stretches to fill the caller's modifier width. Pass the size-fallback dimension so that
    // a bare switch() still claims exactly the track size; when the caller adds .width(N)
    // the widget expands to N while the painted track remains fixed at 44×24dp.
    val surface = resolveInteractiveSurface(
        id = id,
        style = style,
        defaults = theme.components.toggle,
        modifier = modifier.withSizeFallback(Dimension.Fixed(TOGGLE_WIDTH_PX.dp), Dimension.Fixed(TOGGLE_HEIGHT_PX.dp)),
        selected = checked,
        enabled = enabled
    )
    // Track slot is always fixed-size, anchored at the START of the claimed slot.
    // When the caller passes .width(260dp), the full slot is 260dp wide but we only paint
    // the 44dp track on the left side; the label gets the remaining space to the right.
    val trackSlot = io.github.ronjunevaldoz.awake.ui.layout.UiBounds(
        x = surface.interaction.slot.x,
        y = surface.interaction.slot.y + (surface.interaction.slot.height - TOGGLE_HEIGHT_PX.dp.toPx()) / 2f,
        width = TOGGLE_WIDTH_PX.dp.toPx(),
        height = TOGGLE_HEIGHT_PX.dp.toPx()
    ).toSlot()
    val newChecked = if (surface.interaction.clicked) !checked else checked
    // Both states are hardcoded tokens, not resolved.background -- a Switch's on/off track
    // color is structural to what a switch communicates, not something a caller-supplied
    // style should be able to accidentally collapse to the page background (verified via a
    // real render: falling back to resolved.background/theme.tokens.background left the
    // unchecked track literally invisible whenever a style plumbed in a background matching
    // the page, e.g. shadcnSwitch borrowing a text-field style).
    val trackFill = if (newChecked) theme.colors.primary else theme.colors.muted
    // See `ShadcnButtons.kt`'s `buttonSlotInternal` doc for why this is one group alpha
    // around the whole painted widget, not a per-color tweak.
    withGraphicsLayerAlpha(if (enabled) 1f else 0.5f) {
        // The track is always a true stadium/pill regardless of what shape the caller's style
        // resolves to -- same reasoning as the color above, and consistent with the knob below,
        // which already hardcodes UiShapeSpec.Pill instead of trusting the resolved style.
        paintSurface(
            slot = trackSlot,
            resolved = surface.resolved,
            fillColor = trackFill,
            borderColor = surface.resolved.borderColor ?: theme.colors.border,
            shapeSpec = UiShapeSpec.Pill
        )
        val knobDiameter = trackSlot.height - TOGGLE_KNOB_INSET_PX * 2f
        val knobX = if (newChecked) {
            trackSlot.x + trackSlot.width - TOGGLE_KNOB_INSET_PX - knobDiameter
        } else {
            trackSlot.x + TOGGLE_KNOB_INSET_PX
        }
        emitFillAndBorder(
            slot = io.github.ronjunevaldoz.awake.ui.layout.UiBounds(knobX, trackSlot.y + TOGGLE_KNOB_INSET_PX, knobDiameter, knobDiameter)
                .toSlot(),
            fillColor = theme.colors.background,
            radiusPx = 0f,
            borderWidth = UiShape.none,
            borderColor = Color.Transparent,
            shapeSpec = UiShapeSpec.Pill
        )
        if (label != null) {
            val trackWidthPx = TOGGLE_WIDTH_PX.dp.toPx()
            val gapPx = TOGGLE_LABEL_GAP.toPx()
            val availableWidth = surface.interaction.slot.width
            val labelWidth = if (availableWidth > trackWidthPx + gapPx) {
                availableWidth - trackWidthPx - gapPx
            } else {
                // Last-resort guess, not a measured or tokenized value: the widget claimed only
                // the fixed track size and no ancestor exposes a fill width, so there is nothing
                // to derive a label box from. 160dp is simply "wide enough for a typical short
                // switch label"; text() ellipsizes anything longer. Replace this with a real
                // measurement (font.measureTextWidth) if labels ever start truncating here.
                (this@switch.fillWidthOrNull()?.let { it - trackWidthPx - gapPx }
                    ?: SWITCH_LABEL_WIDTH_GUESS.toPx()).coerceAtLeast(0f)
            }
            text(
                label,
                slot = io.github.ronjunevaldoz.awake.ui.layout.UiBounds(
                    trackSlot.x + trackWidthPx + gapPx,
                    surface.interaction.slot.y,
                    labelWidth,
                    surface.interaction.slot.height
                ),
                font = context.currentFont,
                color = surface.resolved.foreground ?: theme.colors.foreground,
                centered = false,
                verticallyCentered = true,
                overflow = UiTextOverflow.Ellipsis,
                textStyle = surface.resolved.textStyle,
                semanticId = "$id.label"
            )
        }
    }
    recordSemantic(
        role = UiSemanticRole.Switch,
        id = id,
        label = label,
        bounds = surface.interaction.slot.toBounds(),
        truncated = false,
        selected = newChecked
    )
    return newChecked
}
