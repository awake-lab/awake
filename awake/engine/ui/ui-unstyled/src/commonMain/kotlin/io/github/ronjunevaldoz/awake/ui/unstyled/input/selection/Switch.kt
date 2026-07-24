package io.github.ronjunevaldoz.awake.ui.unstyled.input.selection

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.core.graphics.emitFillAndBorder
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.fillWidthOrNull
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.recordSemantic
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
    style: Style = Style.Empty
): Boolean {
    val theme = context.currentTheme
    val surface = resolveInteractiveSurface(
        id = id,
        width = Dimension.Fixed(TOGGLE_WIDTH_PX.dp),
        height = Dimension.Fixed(TOGGLE_HEIGHT_PX.dp),
        style = style,
        defaults = theme.components.toggle,
        modifier = modifier,
        selected = checked
    )
    val newChecked = if (surface.interaction.clicked) !checked else checked
    val trackFill =
        if (newChecked) theme.tokens.primary else (surface.resolved.background ?: theme.tokens.background)
    paintSurface(
        slot = surface.interaction.slot,
        resolved = surface.resolved,
        fillColor = trackFill,
        borderColor = surface.resolved.borderColor ?: theme.tokens.border
    )
    val knobDiameter = surface.interaction.slot.height - TOGGLE_KNOB_INSET_PX * 2f
    val knobX = if (newChecked) {
        surface.interaction.slot.x + surface.interaction.slot.width - TOGGLE_KNOB_INSET_PX - knobDiameter
    } else {
        surface.interaction.slot.x + TOGGLE_KNOB_INSET_PX
    }
    emitFillAndBorder(
        slot = UiSlot(knobX, surface.interaction.slot.y + TOGGLE_KNOB_INSET_PX, knobDiameter, knobDiameter),
        fillColor = theme.tokens.background,
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
            slot = UiSlot(
                surface.interaction.slot.x + surface.interaction.slot.width + TOGGLE_LABEL_GAP,
                surface.interaction.slot.y,
                labelWidth,
                surface.interaction.slot.height
            ),
            font = context.currentFont,
            color = surface.resolved.foreground ?: theme.tokens.foreground,
            centered = false,
            verticallyCentered = true,
            overflow = UiTextOverflow.Ellipsis,
            textStyle = surface.resolved.textStyle,
            semanticId = "$id.label"
        )
    }
    recordSemantic(
        role = UiSemanticRole.Switch,
        id = id,
        label = label,
        bounds = surface.interaction.slot,
        truncated = false,
        selected = newChecked
    )
    return newChecked
}
