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
import io.github.ronjunevaldoz.awake.ui.withGraphicsLayerAlpha
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
private const val SLIDER_LABEL_GAP_PX = 8f
fun UiScope.slider(
    id: String,
    min: Float,
    max: Float,
    value: Float,
    label: String? = null,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    enabled: Boolean = true
): Float {
    val interaction = interact(
        id = id,
        modifier = modifier.withSizeFallback(Dimension.FillMax, Dimension.Fixed(32f.dp)),
        enabled = enabled
    )
    val slot = interaction.slot
    val pointerDown = pointerDown()
    // `isActive(id)` can only ever be true here if `interact()` above claimed it, which it
    // never does while `enabled` is false -- same single-gate shape as `interact()`'s own doc,
    // no separate `enabled` check needed to suppress dragging.
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
    val fraction = ((newValue - min) / (max - min)).coerceIn(0f, 1f)
    val handleWidth = (trackSlot.width * fraction).coerceAtLeast(0f)
    val knobCenterX = trackSlot.x + handleWidth
    // See `ShadcnButtons.kt`'s `buttonSlotInternal` doc for why this is one group alpha
    // around the whole painted widget (track/fill/knob/label), not a per-color tweak.
    withGraphicsLayerAlpha(if (enabled) 1f else 0.5f) {
        paintSurface(
            slot = trackSlot,
            resolved = surface.resolved.copy(shapeSpec = UiShapeSpec.Pill)
        )
        if (handleWidth > 0f) {
            emitFillAndBorder(
                slot = io.github.ronjunevaldoz.awake.ui.layout.UiBounds(trackSlot.x, trackSlot.y, handleWidth, trackSlot.height)
                    .toSlot(),
                fillColor = theme.colors.primary,
                radiusPx = 0f,
                borderWidth = UiShape.none,
                borderColor = Color.Transparent,
                shapeSpec = UiShapeSpec.Pill
            )
        }
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
            fillColor = theme.colors.background,
            borderColor = theme.colors.primary
        )
        if (label != null) {
            // Previously this drew centered over the whole slot, which shares its vertical
            // center with the knob (both centered in slot.height) -- the label text rendered
            // straight through the thumb. Anchoring it *above* the slot instead would need
            // vertical space this fixed-height widget doesn't reserve (slot.y can be 0, pushing
            // the label off-canvas), so anchor it beside the knob instead, on whichever side
            // has room, still vertically centered in the slot the knob already occupies.
            val knobEdgeGap = SLIDER_KNOB_DIAMETER_PX / 2f + SLIDER_LABEL_GAP_PX
            val rightSpace = (slot.x + slot.width) - (knobCenterX + knobEdgeGap)
            val leftSpace = (knobCenterX - knobEdgeGap) - slot.x
            // Whichever side of the knob has more room -- avoids an arbitrary width cap that
            // would truncate longer labels (e.g. "Exposure 100%") when the slot is wide enough
            // to easily fit them on either side.
            val labelWidth = maxOf(rightSpace, leftSpace, 0f)
            val labelX = if (rightSpace >= leftSpace) {
                knobCenterX + knobEdgeGap
            } else {
                (knobCenterX - knobEdgeGap - labelWidth).coerceAtLeast(slot.x)
            }
            text(
                label,
                slot = io.github.ronjunevaldoz.awake.ui.layout.UiBounds(
                    labelX,
                    slot.y,
                    labelWidth,
                    slot.height
                ),
                font = font,
                color = surface.resolved.foreground ?: theme.colors.foreground,
                centered = true,
                overflow = UiTextOverflow.Ellipsis,
                textStyle = surface.resolved.textStyle,
                semanticId = "$id.label"
            )
        }
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
