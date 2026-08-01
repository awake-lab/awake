package io.github.ronjunevaldoz.awake.ui.unstyled.input

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.graphics.emitFillAndBorder
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font
import io.github.ronjunevaldoz.awake.ui.pointerDown
import io.github.ronjunevaldoz.awake.ui.pointerX
import io.github.ronjunevaldoz.awake.ui.recordSemantic
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.context.sliderValueFromPointerX
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.unstyled.interact
import io.github.ronjunevaldoz.awake.ui.unstyled.paintSurface
import io.github.ronjunevaldoz.awake.ui.unstyled.resolveInteractiveSurface
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

private const val SLIDER_TRACK_HEIGHT_PX = 10f
private const val SLIDER_KNOB_DIAMETER_PX = 26f
fun UiScope.slider(
    id: String,
    min: Float,
    max: Float,
    value: Float,
    label: String? = null,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty
): Float {
    val interaction = interact(
        id = id,
        modifier = modifier.withSizeFallback(Dimension.FillMax, Dimension.Fixed(32f.dp))
    )
    val slot = interaction.slot
    val pointerDown = pointerDown()
    val dragging = isActive(id) && pointerDown
    val newValue = if (dragging) sliderValueFromPointerX(
        pointerX(),
        slot.x,
        slot.width,
        min,
        max
    ) else value
    releaseActiveIfMatches(id)

    val surface = resolveInteractiveSurface(
        interaction = interaction,
        modifier = modifier,
        style = style,
        defaults = theme.components.slider,
        focused = false
    )
    val trackSlot = io.github.ronjunevaldoz.awake.ui.layout.UiBounds(
        slot.x,
        slot.y + (slot.height - SLIDER_TRACK_HEIGHT_PX) / 2f,
        slot.width,
        SLIDER_TRACK_HEIGHT_PX
    ).toSlot()
    paintSurface(
        slot = trackSlot,
        resolved = surface.resolved.copy(shapeSpec = UiShapeSpec.Pill)
    )
    val fraction = ((newValue - min) / (max - min)).coerceIn(0f, 1f)
    val handleWidth = (trackSlot.width * fraction).coerceAtLeast(0f)
    if (handleWidth > 0f) {
        emitFillAndBorder(
            slot = io.github.ronjunevaldoz.awake.ui.layout.UiBounds(trackSlot.x, trackSlot.y, handleWidth, trackSlot.height)
                .toSlot(),
            fillColor = theme.tokens.primary,
            radiusPx = 0f,
            borderWidth = UiShape.none,
            borderColor = Color.Transparent,
            shapeSpec = UiShapeSpec.Pill
        )
    }
    val knobCenterX = trackSlot.x + handleWidth
    paintSurface(
        slot = io.github.ronjunevaldoz.awake.ui.layout.UiBounds(
            knobCenterX - SLIDER_KNOB_DIAMETER_PX / 2f,
            slot.y + (slot.height - SLIDER_KNOB_DIAMETER_PX) / 2f,
            SLIDER_KNOB_DIAMETER_PX,
            SLIDER_KNOB_DIAMETER_PX
        ).toSlot(),
        resolved = surface.resolved.copy(
            borderWidth = surface.resolved.borderWidth.takeIf { it.value > 0f } ?: 1.5f.dp,
            shapeSpec = UiShapeSpec.Pill
        ),
        fillColor = theme.tokens.background,
        borderColor = theme.tokens.primary
    )
    if (label != null) {
        text(
            label,
            slot = slot.toBounds(),
            font = font,
            color = surface.resolved.foreground ?: theme.tokens.foreground,
            centered = true,
            overflow = UiTextOverflow.Ellipsis,
            textStyle = surface.resolved.textStyle,
            semanticId = "$id.label"
        )
    }
    recordSemantic(
        role = UiSemanticRole.Slider,
        id = id,
        label = label,
        bounds = slot.toBounds(),
        contentBounds = if (handleWidth > 0f) io.github.ronjunevaldoz.awake.ui.layout.UiBounds(
            slot.x,
            slot.y,
            handleWidth,
            slot.height
        ) else null
    )
    return newValue
}
