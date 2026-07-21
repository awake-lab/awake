package io.github.ronjunevaldoz.awake.ui.unstyled.input

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
import io.github.ronjunevaldoz.awake.ui.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.core.graphics.emitFillAndBorder
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.recordSemantic
import io.github.ronjunevaldoz.awake.ui.resolveStyle
import io.github.ronjunevaldoz.awake.ui.sliderValueFromPointerX
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.basicText

private const val SLIDER_TRACK_HEIGHT_PX = 10f
private const val SLIDER_KNOB_DIAMETER_PX = 26f
fun UiScope.slider(
    id: String,
    min: Float,
    max: Float,
    value: Float,
    label: String? = null,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Float {
    val slot = claimModifiedSlot(
        defaultWidth = Dimension.FillMax,
        defaultHeight = Dimension.Fixed(32f.dp),
        modifier = modifier
    )
    val hovered = hitTest(slot)
    tryClaimActive(id, hovered)
    val pointerDown = context.pointerDown() // We need this in UiScope or context
    val dragging = isActive(id) && pointerDown
    val newValue = if (dragging) sliderValueFromPointerX(
        context.pointerX(),
        slot.x,
        slot.width,
        min,
        max
    ) else value
    releaseActiveIfMatches(id)

    val styleState = MutableStyleState(
        hovered = hovered || modifier.forceHover == true,
        active = dragging || modifier.forceActive == true
    )
    val resolved = resolveStyle(
        style = style,
        defaults = theme.components.slider,
        state = styleState
    )
    val trackSlot = UiSlot(
        slot.x,
        slot.y + (slot.height - SLIDER_TRACK_HEIGHT_PX) / 2f,
        slot.width,
        SLIDER_TRACK_HEIGHT_PX
    )
    emitFillAndBorder(
        slot = trackSlot,
        fillColor = resolved.background ?: theme.tokens.background,
        radiusPx = 0f,
        borderWidth = resolved.borderWidth,
        borderColor = resolved.borderColor ?: theme.tokens.border,
        shapeSpec = UiShapeSpec.Pill
    )
    val fraction = ((newValue - min) / (max - min)).coerceIn(0f, 1f)
    val handleWidth = (trackSlot.width * fraction).coerceAtLeast(0f)
    if (handleWidth > 0f) {
        emitFillAndBorder(
            slot = UiSlot(trackSlot.x, trackSlot.y, handleWidth, trackSlot.height),
            fillColor = theme.tokens.primary,
            radiusPx = 0f,
            borderWidth = UiShape.none,
            borderColor = Color.Transparent,
            shapeSpec = UiShapeSpec.Pill
        )
    }
    val knobCenterX = trackSlot.x + handleWidth
    emitFillAndBorder(
        slot = UiSlot(
            knobCenterX - SLIDER_KNOB_DIAMETER_PX / 2f,
            slot.y + (slot.height - SLIDER_KNOB_DIAMETER_PX) / 2f,
            SLIDER_KNOB_DIAMETER_PX,
            SLIDER_KNOB_DIAMETER_PX
        ),
        fillColor = theme.tokens.background,
        radiusPx = 0f,
        borderWidth = resolved.borderWidth.takeIf { it.value > 0f } ?: 1.5f.dp,
        borderColor = theme.tokens.primary,
        shapeSpec = UiShapeSpec.Pill
    )
    if (label != null && font != null) {
        basicText(
            label,
            slot = slot,
            font = font,
            color = resolved.foreground ?: theme.tokens.foreground,
            centered = true,
            overflow = UiTextOverflow.Ellipsis,
            textScale = resolved.textScale,
            textSize = resolved.textSize,
            semanticId = "$id.label"
        )
    }
    recordSemantic(
        role = UiSemanticRole.Slider,
        id = id,
        label = label,
        bounds = slot,
        contentBounds = if (handleWidth > 0f) UiSlot(
            slot.x,
            slot.y,
            handleWidth,
            slot.height
        ) else null
    )
    return newValue
}