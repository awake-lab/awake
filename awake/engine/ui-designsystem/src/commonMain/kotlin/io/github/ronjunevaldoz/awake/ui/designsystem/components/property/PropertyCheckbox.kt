package io.github.ronjunevaldoz.awake.ui.designsystem.components.property

import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.MutableStyleState
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.core.graphics.emitFillAndBorder
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font
import io.github.ronjunevaldoz.awake.ui.resolveGlyphPx
import io.github.ronjunevaldoz.awake.ui.resolveStyle
import io.github.ronjunevaldoz.awake.ui.resolvedThemeCaptionStyle
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text


private val DefaultPropertyCheckboxHeight = 28f.dp
fun UiScope.propertyCheckbox(
    id: String,
    checked: Boolean,
    label: String,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty,
    boxSize: Dp = 16f.dp
): Boolean {
    val interaction = propertyInteract(
        id = id,
        width = Dimension.FillMax,
        height = modifier.height ?: Dimension.Fixed(DefaultPropertyCheckboxHeight),
        modifier = modifier
    )
    val labelColor = theme.tokens.mutedForeground
    val resolvedFont = font
    val resolvedTextStyle = resolvedThemeCaptionStyle
    val glyphPx = resolveGlyphPx(resolvedFont, resolvedTextStyle)
    val labelSlot = UiSlot(
        interaction.slot.x,
        interaction.slot.y + (interaction.slot.height - glyphPx) / 2f,
        interaction.slot.width,
        glyphPx
    )
    text(
        label = label,
        slot = labelSlot,
        font = resolvedFont,
        color = labelColor,
        centered = false,
        overflow = UiTextOverflow.Ellipsis,
        textStyle = resolvedTextStyle
    )

    val resolved = resolveStyle(
        style = style,
        defaults = theme.components.checkbox,
        state = MutableStyleState(
            hovered = interaction.hovered,
            active = interaction.active,
            selected = checked
        )
    )
    val boxPx = boxSize.toPx()
    val boxSlot = UiSlot(
        interaction.slot.x + interaction.slot.width - boxPx,
        interaction.slot.y + (interaction.slot.height - boxPx) / 2f,
        boxPx,
        boxPx
    )
    emitFillAndBorder(
        slot = boxSlot,
        fillColor = resolved.background ?: theme.tokens.background,
        radiusPx = resolved.shape.toPx(),
        borderWidth = resolved.borderWidth,
        borderColor = resolved.borderColor ?: theme.tokens.border,
        shapeSpec = resolved.shapeSpec
    )
    val newChecked = if (interaction.clicked) !checked else checked
    if (newChecked) {
        val inset = boxPx * 0.25f
        emitFillAndBorder(
            slot = UiSlot(
                boxSlot.x + inset,
                boxSlot.y + inset,
                boxSlot.width - inset * 2f,
                boxSlot.height - inset * 2f
            ),
            fillColor = theme.tokens.primary,
            radiusPx = (resolved.shape.toPx() - inset).coerceAtLeast(0f),
            borderWidth = UiShape.none,
            borderColor = theme.tokens.primary,
            shapeSpec = resolved.shapeSpec
        )
    }
    return newChecked
}
