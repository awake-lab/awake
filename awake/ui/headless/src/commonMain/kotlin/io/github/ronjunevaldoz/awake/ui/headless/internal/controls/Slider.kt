// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless.internal.controls

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiPrimitiveScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.canvas
import io.github.ronjunevaldoz.awake.ui.context.sliderValueFromPointerX
import io.github.ronjunevaldoz.awake.ui.graphics.drawFillAndBorder
import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.paintSurface
import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.resolveInteractiveSurface
import io.github.ronjunevaldoz.awake.ui.headless.internal.layout.interact
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.scope.pointerDown
import io.github.ronjunevaldoz.awake.ui.scope.pointerX
import io.github.ronjunevaldoz.awake.ui.scope.recordSemantic
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.withGraphicsLayerAlpha

// Dp, not raw px: added to/subtracted from `slot`/`trackSlot` coordinates that are already
// density-scaled, so a raw literal would render half-size at 2x.
private val SLIDER_TRACK_HEIGHT = 6f.dp

// shadcn v4's thumb is size-4 (16px) with a 1px primary border on a background fill.
private val SLIDER_KNOB_DIAMETER = 16f.dp
fun UiPrimitiveScope.slider(
    id: String,
    min: Float,
    max: Float,
    value: Float,
    label: String? = null,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    enabled: Boolean = true,
    showKnob: Boolean = true,
): Float {
    val interaction = interact(
        id = id,
        modifier = modifier.withSizeFallback(Dimension.FillMax, Dimension.Fixed(20f.dp)),
        enabled = enabled,
    )
    val slot = interaction.slot
    val trackHeightPx = SLIDER_TRACK_HEIGHT.toPx()
    val knobDiameterPx = SLIDER_KNOB_DIAMETER.toPx()
    val pointerDown = pointerDown()
    // Track (and thus the knob's travel range) is inset by half the knob's own diameter on
    // each side -- a knob centered at fraction 0 or 1 on a *full-width* track would extend
    // SLIDER_KNOB_DIAMETER_PX/2 past the widget's own slot bounds and get clipped by any
    // parent that clips content (a real reported bug: "knob cut when reach start or end").
    // Drag mapping below uses this same inset range so the pointer-to-value conversion matches
    // where the knob is actually drawn.
    val trackInsetPx = if (showKnob) knobDiameterPx / 2f else 0f
    val trackX = slot.x + trackInsetPx
    val trackWidth = (slot.width - trackInsetPx * 2f).coerceAtLeast(0f)
    // `isActive(id)` can only ever be true here if `interact()` above claimed it, which it
    // never does while `enabled` is false -- same single-gate shape as `interact()`'s own doc,
    // no separate `enabled` check needed to suppress dragging.
    val dragging = isActive(id) && pointerDown
    val newValue = if (dragging) {
        sliderValueFromPointerX(
            pointerX(),
            trackX,
            trackWidth,
            min,
            max,
        )
    } else {
        value
    }
    releaseActiveIfMatches(id)

    val surface = resolveInteractiveSurface(
        interaction = interaction,
        modifier = modifier,
        style = style,
        // Reads no ambient theme -- shadcnSlider's shadcnSliderStyle already supplies a complete
        // Style, including the track's border/hover/active feedback (added there so this
        // removal wouldn't silently drop them).
        defaults = Style.Empty,
        focused = false,
    )
    val trackSlot = UiBounds(
        trackX,
        slot.y + (slot.height - trackHeightPx) / 2f,
        trackWidth,
        trackHeightPx,
    )
    val fraction = ((newValue - min) / (max - min)).coerceIn(0f, 1f)
    val handleWidth = (trackSlot.width * fraction).coerceAtLeast(0f)
    val knobCenterX = trackSlot.x + handleWidth
    // See `ShadcnButtons.kt`'s `buttonSlotInternal` doc for why this is one group alpha
    // around the whole painted widget (track/fill/knob/label), not a per-color tweak.
    withGraphicsLayerAlpha(if (enabled) 1f else 0.5f) {
        paintSurface(
            slot = trackSlot,
            resolved = surface.resolved.copy(shapeSpec = UiShapeSpec.Pill),
        )
        if (handleWidth > 0f) {
            val fillSlot = UiBounds(
                trackSlot.x,
                trackSlot.y,
                handleWidth,
                trackSlot.height,
            )
            // resolved.foreground is the caller's accent color (shadcnSliderStyle sets
            // `foreground(colors.primary)`) -- the token is only a fallback for a bare
            // Style.Empty caller, not a hardcoded override.
            val fillColor = surface.resolved.foreground ?: theme.colors.primary
            canvas(fillSlot) {
                drawFillAndBorder(
                    slot = fillSlot,
                    fillColor = fillColor,
                    radiusPx = 0f,
                    borderWidth = UiShape.none,
                    borderColor = Color.Transparent,
                    shapeSpec = UiShapeSpec.Pill,
                )
            }
        }
        if (showKnob) {
            paintSurface(
                slot = UiBounds(
                    knobCenterX - knobDiameterPx / 2f,
                    slot.y + (slot.height - knobDiameterPx) / 2f,
                    knobDiameterPx,
                    knobDiameterPx,
                ),
                resolved = surface.resolved.copy(
                    borderWidth = surface.resolved.borderWidth.takeIf { it.value > 0f } ?: 1f.dp,
                    shapeSpec = UiShapeSpec.Pill,
                ),
                // Knob fill has no dedicated resolved field (background is already the track's
                // muted fill) -- the real shadcn thumb is always page-background with an
                // accent-colored border, so only the border needs to be caller-overridable.
                fillColor = theme.colors.background,
                borderColor = surface.resolved.foreground ?: theme.colors.primary,
            )
        }
    }
    recordSemantic(
        role = UiSemanticRole.Slider,
        id = id,
        label = label,
        bounds = slot,
        contentBounds = if (handleWidth > 0f) {
            UiBounds(
                slot.x,
                slot.y,
                handleWidth,
                slot.height,
            )
        } else {
            null
        },
    )
    return newValue
}
