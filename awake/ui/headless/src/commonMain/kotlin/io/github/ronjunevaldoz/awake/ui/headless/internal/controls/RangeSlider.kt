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
import io.github.ronjunevaldoz.awake.ui.headless.internal.layout.UiInteraction
import io.github.ronjunevaldoz.awake.ui.headless.internal.layout.interact
import io.github.ronjunevaldoz.awake.ui.layouts.box
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxSize
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.scope.pointerDown
import io.github.ronjunevaldoz.awake.ui.scope.pointerX
import io.github.ronjunevaldoz.awake.ui.scope.recordSemantic
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.headless.withDisabledAlpha
import kotlin.math.abs

private const val RANGE_SLIDER_TRACK_HEIGHT_PX = 6f

// Matches Slider.kt's SLIDER_KNOB_DIAMETER: shadcn v4's size-4 thumb.
private const val RANGE_SLIDER_KNOB_DIAMETER_PX = 16f

// ponytail: fixed 1% of the [min, max] span as the minimum start/end gap -- matches shadcn's
// own range slider "handles can't cross" behavior without modeling step size; revisit if a
// caller needs a configurable minGap.
private const val RANGE_SLIDER_MIN_GAP_FRACTION = 0.01f

/**
 * Dual-thumb variant of [slider]: two independently draggable knobs on one shared track, with
 * the fill spanning only between them (not from the track start). Mirrors [slider]'s
 * stateless/immediate-mode shape -- returns the new `(start, end)` pair, caller reassigns to its
 * own persisted state.
 *
 * Unlike [slider], [label] is recorded for accessibility only (via [recordSemantic]), not
 * painted -- there's no single "beside the knob" spot with two knobs sharing a track. Callers
 * that want a visible label/value row above the track (as shadcn's own range slider shows) should
 * build that row themselves above this widget, the same way `shadcnFieldSlider` composes a label
 * beside [slider].
 */
fun UiPrimitiveScope.rangeSlider(
    id: String,
    min: Float,
    max: Float,
    valueStart: Float,
    valueEnd: Float,
    label: String? = null,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    enabled: Boolean = true,
): Pair<Float, Float> {
    var resultStart = valueStart
    var resultEnd = valueEnd
    box(
        modifier = modifier.withSizeFallback(
            Dimension.FillMax,
            Dimension.Fixed(20f.dp),
        ),
    ) { boxSlot ->
        val startId = "$id.start"
        val endId = "$id.end"
        val pointerDown = pointerDown()
        val px = pointerX()
        val epsilon = (max - min) * RANGE_SLIDER_MIN_GAP_FRACTION

        // Inset by half the knob diameter on each side -- see [slider]'s identical fix: a knob
        // centered at fraction 0 or 1 on a full-width track extends past the widget's own
        // bounds and gets clipped by any parent that clips content.
        val trackInsetPx = RANGE_SLIDER_KNOB_DIAMETER_PX / 2f
        val trackSlot = UiBounds(
            boxSlot.x + trackInsetPx,
            boxSlot.y + (boxSlot.height - RANGE_SLIDER_TRACK_HEIGHT_PX) / 2f,
            (boxSlot.width - trackInsetPx * 2f).coerceAtLeast(0f),
            RANGE_SLIDER_TRACK_HEIGHT_PX,
        )

        val startFraction = ((valueStart - min) / (max - min)).coerceIn(0f, 1f)
        val endFraction = ((valueEnd - min) / (max - min)).coerceIn(0f, 1f)
        val startKnobX = trackSlot.x + trackSlot.width * startFraction
        val endKnobX = trackSlot.x + trackSlot.width * endFraction

        // Both knobs share the whole widget as their hit/drag region (same as `slider`'s single
        // knob), so on a fresh pointer-down whichever `interact()` call runs first wins the claim
        // (see `tryClaimActive`'s `activeId == null` gate) -- claim in order of proximity to the
        // pointer so a click nearer one knob drags that one, not always the start knob.
        val startCloser = abs(px - startKnobX) <= abs(px - endKnobX)
        // BoxScope.claimSlot() ignores whatever width/height dimensions `interact()`'s default
        // modifier requests and always returns the same fixed rect -- but `claimModifiedSlot()`
        // still tries to resolve those requested dimensions against that rect afterward, and
        // a bare `Modifier`'s implicit WrapContent always errors there (see
        // `Dimension.resolveAgainst`). Passing an explicit `fillMaxSize()` modifier (matching
        // the box's own FillMax/FillMax claim) sidesteps that, same as `slider`'s own explicit
        // `withSizeFallback` modifier on its single `interact()` call.
        val knobModifier = Modifier.fillMaxSize()
        val startInteraction: UiInteraction
        val endInteraction: UiInteraction
        if (startCloser) {
            startInteraction = interact(id = startId, modifier = knobModifier, enabled = enabled)
            endInteraction = interact(id = endId, modifier = knobModifier, enabled = enabled)
        } else {
            endInteraction = interact(id = endId, modifier = knobModifier, enabled = enabled)
            startInteraction = interact(id = startId, modifier = knobModifier, enabled = enabled)
        }

        val draggingStart = isActive(startId) && pointerDown
        val draggingEnd = isActive(endId) && pointerDown
        val rawStart = if (draggingStart) {
            sliderValueFromPointerX(
                px,
                trackSlot.x,
                trackSlot.width,
                min,
                max,
            )
        } else {
            valueStart
        }
        val rawEnd = if (draggingEnd) {
            sliderValueFromPointerX(
                px,
                trackSlot.x,
                trackSlot.width,
                min,
                max,
            )
        } else {
            valueEnd
        }
        releaseActiveIfMatches(startId)
        releaseActiveIfMatches(endId)

        val newStart =
            (if (draggingStart) minOf(rawStart, valueEnd - epsilon) else valueStart).coerceIn(
                min,
                max,
            )
        val newEnd =
            (if (draggingEnd) maxOf(rawEnd, valueStart + epsilon) else valueEnd).coerceIn(min, max)
        resultStart = newStart
        resultEnd = newEnd

        val surface = resolveInteractiveSurface(
            interaction = UiInteraction(
                slot = boxSlot,
                hovered = startInteraction.hovered || endInteraction.hovered,
                active = startInteraction.active || endInteraction.active,
                clicked = false,
            ),
            modifier = modifier,
            style = style,
            // Reads no ambient theme -- see Slider.kt's identical `defaults` doc; shadcnRangeSlider
            // shares shadcnSliderStyle with shadcnSlider.
            defaults = Style.Empty,
            focused = false,
        )

        val lowFraction = ((newStart - min) / (max - min)).coerceIn(0f, 1f)
        val highFraction = ((newEnd - min) / (max - min)).coerceIn(0f, 1f)
        val fillX = trackSlot.x + trackSlot.width * lowFraction
        val fillWidth = (trackSlot.width * (highFraction - lowFraction)).coerceAtLeast(0f)
        val newStartKnobX = trackSlot.x + trackSlot.width * lowFraction
        val newEndKnobX = trackSlot.x + trackSlot.width * highFraction

        withDisabledAlpha(enabled) {
            paintSurface(
                slot = trackSlot,
                resolved = surface.resolved.copy(shapeSpec = UiShapeSpec.Pill),
            )
            if (fillWidth > 0f) {
                val fillSlot = UiBounds(fillX, trackSlot.y, fillWidth, trackSlot.height)
                // resolved.foreground is the caller's accent color (shares shadcnSliderStyle
                // with slider()) -- the token is only a fallback for a bare Style.Empty
                // caller, not a hardcoded override.
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
            for (knobCenterX in listOf(newStartKnobX, newEndKnobX)) {
                paintSurface(
                    slot = UiBounds(
                        knobCenterX - RANGE_SLIDER_KNOB_DIAMETER_PX / 2f,
                        boxSlot.y + (boxSlot.height - RANGE_SLIDER_KNOB_DIAMETER_PX) / 2f,
                        RANGE_SLIDER_KNOB_DIAMETER_PX,
                        RANGE_SLIDER_KNOB_DIAMETER_PX,
                    ),
                    resolved = surface.resolved.copy(
                        borderWidth = surface.resolved.borderWidth.takeIf { it.value > 0f }
                            ?: 1f.dp,
                        shapeSpec = UiShapeSpec.Pill,
                    ),
                    // See slider()'s identical knob-fill comment -- no dedicated resolved field
                    // for the knob's own fill, only the accent border is caller-overridable.
                    fillColor = theme.colors.background,
                    borderColor = surface.resolved.foreground ?: theme.colors.primary,
                )
            }
        }

        recordSemantic(
            role = UiSemanticRole.Slider,
            id = id,
            label = label,
            bounds = boxSlot,
            contentBounds = if (fillWidth > 0f) {
                UiBounds(
                    fillX,
                    trackSlot.y,
                    fillWidth,
                    trackSlot.height,
                )
            } else {
                null
            },
        )
    }
    return resultStart to resultEnd
}
