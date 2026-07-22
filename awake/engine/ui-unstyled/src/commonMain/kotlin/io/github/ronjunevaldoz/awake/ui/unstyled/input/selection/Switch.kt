package io.github.ronjunevaldoz.awake.ui.unstyled.input.selection

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.MutableStyleState
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.core.graphics.emitFillAndBorder
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.fillWidthOrNull
import io.github.ronjunevaldoz.awake.ui.recordSemantic
import io.github.ronjunevaldoz.awake.ui.resolveStyle
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.unstyled.interact

private const val TOGGLE_WIDTH_PX = 40f
private const val TOGGLE_HEIGHT_PX = 22f
private const val TOGGLE_KNOB_INSET_PX = 2f
private const val TOGGLE_LABEL_GAP = 8f
fun UiScope.switch(
    id: String,
    checked: Boolean,
    label: String? = null,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Boolean {
    val theme = context.currentTheme
    val interaction = interact(
        id = id,
        width = Dimension.Fixed(TOGGLE_WIDTH_PX.dp),
        height = Dimension.Fixed(TOGGLE_HEIGHT_PX.dp),
        modifier = modifier
    )
    val styleState = MutableStyleState(
        hovered = interaction.hovered || modifier.forceHover == true,
        active = interaction.active || modifier.forceActive == true,
        selected = checked
    )
    val resolved = resolveStyle(
        style = style,
        defaults = theme.components.toggle,
        state = styleState
    )
    val newChecked = if (interaction.clicked) !checked else checked
    val trackFill =
        if (newChecked) theme.tokens.primary else (resolved.background ?: theme.tokens.background)
    emitFillAndBorder(
        slot = interaction.slot,
        fillColor = trackFill,
        radiusPx = 0f,
        borderWidth = resolved.borderWidth,
        borderColor = resolved.borderColor ?: theme.tokens.border,
        shapeSpec = UiShapeSpec.Pill
    )
    val knobDiameter = interaction.slot.height - TOGGLE_KNOB_INSET_PX * 2f
    val knobX = if (newChecked) {
        interaction.slot.x + interaction.slot.width - TOGGLE_KNOB_INSET_PX - knobDiameter
    } else {
        interaction.slot.x + TOGGLE_KNOB_INSET_PX
    }
    emitFillAndBorder(
        slot = UiSlot(knobX, interaction.slot.y + TOGGLE_KNOB_INSET_PX, knobDiameter, knobDiameter),
        fillColor = theme.tokens.background,
        radiusPx = 0f,
        borderWidth = UiShape.none,
        borderColor = Color.Transparent,
        shapeSpec = UiShapeSpec.Pill
    )
    if (label != null) {
        val labelWidth = (fillWidthOrNull()?.let { it - interaction.slot.width - TOGGLE_LABEL_GAP }
            ?: 160f).coerceAtLeast(0f)
        text(
            label,
            slot = UiSlot(
                interaction.slot.x + interaction.slot.width + TOGGLE_LABEL_GAP,
                interaction.slot.y,
                labelWidth,
                interaction.slot.height
            ),
            font = context.currentFont,
            color = resolved.foreground ?: theme.tokens.foreground,
            centered = false,
            verticallyCentered = true,
            overflow = UiTextOverflow.Ellipsis,
            textStyle = resolved.textStyle,
            semanticId = "$id.label"
        )
    }
    recordSemantic(
        role = UiSemanticRole.Switch,
        id = id,
        label = label,
        bounds = interaction.slot,
        truncated = false,
        selected = newChecked
    )
    return newChecked
}
