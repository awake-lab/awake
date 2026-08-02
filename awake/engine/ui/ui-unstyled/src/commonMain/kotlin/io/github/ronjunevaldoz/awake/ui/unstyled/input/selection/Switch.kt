package io.github.ronjunevaldoz.awake.ui.unstyled.input.selection

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.graphics.emitFillAndBorder
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.fillWidthOrNull
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.recordSemantic
import io.github.ronjunevaldoz.awake.ui.withGraphicsLayerAlpha
import io.github.ronjunevaldoz.awake.ui.unstyled.paintSurface
import io.github.ronjunevaldoz.awake.ui.unstyled.resolveInteractiveSurface
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

private const val TOGGLE_WIDTH_PX = 40f
private const val TOGGLE_HEIGHT_PX = 22f
private const val TOGGLE_KNOB_INSET_PX = 2f
private const val TOGGLE_LABEL_GAP = 8f
fun UiScope.switch(
    id: String,
    checked: Boolean,
    label: String? = null,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    enabled: Boolean = true
): Boolean {
    val theme = context.currentTheme
    val surface = resolveInteractiveSurface(
        id = id,
        style = style,
        defaults = theme.components.toggle,
        modifier = modifier.withSizeFallback(Dimension.Fixed(TOGGLE_WIDTH_PX.dp), Dimension.Fixed(TOGGLE_HEIGHT_PX.dp)),
        selected = checked,
        enabled = enabled
    )
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
            slot = surface.interaction.slot,
            resolved = surface.resolved,
            fillColor = trackFill,
            borderColor = surface.resolved.borderColor ?: theme.colors.border,
            shapeSpec = UiShapeSpec.Pill
        )
        val knobDiameter = surface.interaction.slot.height - TOGGLE_KNOB_INSET_PX * 2f
        val knobX = if (newChecked) {
            surface.interaction.slot.x + surface.interaction.slot.width - TOGGLE_KNOB_INSET_PX - knobDiameter
        } else {
            surface.interaction.slot.x + TOGGLE_KNOB_INSET_PX
        }
        emitFillAndBorder(
            slot = io.github.ronjunevaldoz.awake.ui.layout.UiBounds(knobX, surface.interaction.slot.y + TOGGLE_KNOB_INSET_PX, knobDiameter, knobDiameter)
                .toSlot(),
            fillColor = theme.colors.background,
            radiusPx = 0f,
            borderWidth = UiShape.none,
            borderColor = Color.Transparent,
            shapeSpec = UiShapeSpec.Pill
        )
        if (label != null) {
            val labelWidth = (fillWidthOrNull()?.let { it - surface.interaction.slot.width - TOGGLE_LABEL_GAP }
                ?: 160f).coerceAtLeast(0f)
            text(
                label,
                slot = io.github.ronjunevaldoz.awake.ui.layout.UiBounds(
                    surface.interaction.slot.x + surface.interaction.slot.width + TOGGLE_LABEL_GAP,
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
