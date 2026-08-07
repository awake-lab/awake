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
import io.github.ronjunevaldoz.awake.ui.scope.recordSemantic
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

private const val SLIDER_TRACK_HEIGHT_PX = 6f
private const val SLIDER_KNOB_DIAMETER_PX = 20f
fun UiScope.slider(
    id: String,
    min: Float,
    max: Float,
    value: Float,
    label: String? = null,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    enabled: Boolean = true,
    showKnob: Boolean = true
): Float {
    val interaction = interact(
        id = id,
        modifier = modifier.withSizeFallback(Dimension.FillMax, Dimension.Fixed(20f.dp)),
        enabled = enabled
    )
    val slot = interaction.slot
    val pointerDown = pointerDown()
    // Track (and thus the knob's travel range) is inset by half the knob's own diameter on
    // each side -- a knob centered at fraction 0 or 1 on a *full-width* track would extend
    // SLIDER_KNOB_DIAMETER_PX/2 past the widget's own slot bounds and get clipped by any
    // parent that clips content (a real reported bug: "knob cut when reach start or end").
    // Drag mapping below uses this same inset range so the pointer-to-value conversion matches
    // where the knob is actually drawn.
    val trackInsetPx = if (showKnob) SLIDER_KNOB_DIAMETER_PX / 2f else 0f
    val trackX = slot.x + trackInsetPx
    val trackWidth = (slot.width - trackInsetPx * 2f).coerceAtLeast(0f)
    // `isActive(id)` can only ever be true here if `interact()` above claimed it, which it
    // never does while `enabled` is false -- same single-gate shape as `interact()`'s own doc,
    // no separate `enabled` check needed to suppress dragging.
    val dragging = isActive(id) && pointerDown
    val newValue = if (dragging) sliderValueFromPointerX(
        pointerX(),
        trackX,
        trackWidth,
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
        trackX,
        slot.y + (slot.height - SLIDER_TRACK_HEIGHT_PX) / 2f,
        trackWidth,
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
        if (showKnob) {
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
        }
    }
    recordSemantic(
        role = UiSemanticRole.Slider,
        id = id,
        label = label,
        bounds = slot.toBounds(),
        contentBounds = if (handleWidth > 0f) UiBounds(
            slot.x,
            slot.y,
            handleWidth,
            slot.height
        ) else null
    )
    return newValue
}
