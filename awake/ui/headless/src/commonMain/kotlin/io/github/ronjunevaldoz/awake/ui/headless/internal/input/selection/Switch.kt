// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless.internal.input.selection

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiPrimitiveScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.font.measureTextWidth
import io.github.ronjunevaldoz.awake.ui.graphics.emitFillAndBorder
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.scope.recordSemantic
import io.github.ronjunevaldoz.awake.ui.scope.resolveGlyphPx
import io.github.ronjunevaldoz.awake.ui.scope.resolveStyle
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.headless.internal.input.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.headless.internal.input.text.text
import io.github.ronjunevaldoz.awake.ui.headless.internal.paintSurface
import io.github.ronjunevaldoz.awake.ui.headless.internal.resolveInteractiveSurface
import io.github.ronjunevaldoz.awake.ui.withGraphicsLayerAlpha
import kotlin.math.ceil

// shadcn's Switch track is w-8 h-[1.15rem] (32dp x 18.4dp at 1rem=16dp), not a 44x24 Material
// default that had silently become the de-facto spec here -- see
// skills/awake-ui-authoring/SKILL.md's "an headless default is not a neutral placeholder" note.
// Authored in Dp, not raw pixel Floats -- converted at the point of use via .toPx().
private val TOGGLE_WIDTH = 32f.dp
private val TOGGLE_HEIGHT = 18.4f.dp

// shadcn's default thumb is size-4 (16dp). Its checked transform is `100% - 2px`; the
// unchecked thumb starts flush at the track's leading edge rather than using a symmetric inset.
private val TOGGLE_KNOB_DIAMETER = 16f.dp
private val TOGGLE_CHECKED_TRAILING_GAP = 2f.dp

// Dp, not raw px: it is added to `trackSlot`/`surface.interaction.slot` coordinates that are
// already density-scaled, so a raw literal would render a half-size gap at 2x.
private val TOGGLE_LABEL_GAP = 8f.dp
fun UiPrimitiveScope.switch(
    id: String,
    checked: Boolean,
    label: String? = null,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    enabled: Boolean = true,
): Boolean {
    val theme = context.currentTheme
    // Measured before claiming a slot, not a fixed guess: the label paints at
    // trackSlot.x + trackWidth + gap regardless of what width the widget claims, so if the
    // claimed slot is narrower than track+gap+label, row()'s layout allocates too little space
    // and the next sibling gets placed on top of this label's tail. A caller-visible bug, not
    // theoretical -- two switches side by side with no explicit .width() on either overlapped.
    // Measured with the SAME textStyle the label is later painted with (theme.components.toggle
    // then style) -- measuring against the ambient context.currentTextStyle instead caused the
    // opposite bug: toggle's textSize token differs from ambient body text, so the claimed slot
    // undershot the real render width and text() silently ellipsized the label.
    val labelTextStyle = resolveStyle(style = style, defaults = theme.components.toggle).textStyle
    val labelWidthPx = label?.let {
        context.currentFont.measureTextWidth(
            it,
            resolveGlyphPx(textStyle = labelTextStyle),
        )
    } ?: 0f
    val gapPx = if (label != null) TOGGLE_LABEL_GAP.toPx() else 0f
    // ceil, not the raw sum: this width round-trips through Dp (-> .px below -> Dimension.Fixed
    // -> the resolved slot -> .toPx() at paint time), then has trackWidthPx/gapPx subtracted
    // back out below to recover the label's paint-time slot width -- an add-then-subtract via a
    // Dp round trip is not bit-exact in IEEE 754 float, and can land a couple ULP under the
    // value measureTextWidth measured, enough to fail layoutBitmapText's exact
    // `measured <= maxWidthPx` truncation check when the claim leaves zero margin. Painted text
    // is already whole-pixel-quantized (resolveGlyphPx/pixelPerfectPixel), so sub-pixel
    // precision here was never meaningful -- rounding up to the next pixel buys headroom no
    // float round-trip noise gets near.
    val fallbackWidthPx = ceil(TOGGLE_WIDTH.toPx() + gapPx + labelWidthPx)
    // The switch track is always a fixed TOGGLE_WIDTH × TOGGLE_HEIGHT pill — it never
    // stretches to fill the caller's modifier width. Pass the size-fallback dimension so that
    // a bare switch() still claims exactly track+label size; when the caller adds .width(N)
    // the widget expands to N while the painted track remains fixed at 32x18.4dp.
    val surface = resolveInteractiveSurface(
        id = id,
        style = style,
        defaults = theme.components.toggle,
        modifier = modifier.withSizeFallback(
            Dimension.Fixed(fallbackWidthPx.px),
            Dimension.Fixed(TOGGLE_HEIGHT),
        ),
        selected = checked,
        disabled = !enabled,
        enabled = enabled,
    )
    // Track slot is always fixed-size, anchored at the START of the claimed slot.
    // When the caller passes .width(260dp), the full slot is 260dp wide but we only paint
    // the 32dp track on the left side; the label gets the remaining space to the right.
    val trackSlot = UiBounds(
        x = surface.interaction.slot.x,
        y = surface.interaction.slot.y + (surface.interaction.slot.height - TOGGLE_HEIGHT.toPx()) / 2f,
        width = TOGGLE_WIDTH.toPx(),
        height = TOGGLE_HEIGHT.toPx(),
    )
    val newChecked = if (surface.interaction.clicked) !checked else checked
    // Both states are hardcoded tokens, not resolved.background -- a Switch's on/off track
    // color is structural to what a switch communicates, not something a caller-supplied
    // style should be able to accidentally collapse to the page background (verified via a
    // real render: falling back to resolved.background/theme.tokens.background left the
    // unchecked track literally invisible whenever a style plumbed in a background matching
    // the page, e.g. shadcnSwitch borrowing a text-field style).
    val trackFill = if (newChecked) {
        theme.colors.primary
    } else {
        surface.resolved.background ?: theme.colors.muted
    }
    // See `ShadcnButtons.kt`'s `buttonSlotInternal` doc for why this is one group alpha
    // around the whole painted widget, not a per-color tweak.
    withGraphicsLayerAlpha(if (enabled) 1f else 0.5f) {
        // The track is always a true stadium/pill regardless of what shape the caller's style
        // resolves to -- same reasoning as the color above, and consistent with the knob below,
        // which already hardcodes UiShapeSpec.Pill instead of trusting the resolved style.
        paintSurface(
            slot = trackSlot,
            resolved = surface.resolved,
            fillColor = trackFill,
            borderColor = surface.resolved.borderColor ?: theme.colors.border,
            shapeSpec = UiShapeSpec.Pill,
        )
        val knobDiameter = TOGGLE_KNOB_DIAMETER.toPx()
        val knobY = trackSlot.y + (trackSlot.height - knobDiameter) / 2f
        val knobX = if (newChecked) {
            trackSlot.x + trackSlot.width - knobDiameter - TOGGLE_CHECKED_TRAILING_GAP.toPx()
        } else {
            trackSlot.x
        }
        emitFillAndBorder(
            slot = UiBounds(
                knobX,
                knobY,
                knobDiameter,
                knobDiameter,
            ),
            fillColor = theme.colors.background,
            radiusPx = 0f,
            borderWidth = UiShape.none,
            borderColor = Color.Transparent,
            shapeSpec = UiShapeSpec.Pill,
        )
        if (label != null) {
            val trackWidthPx = TOGGLE_WIDTH.toPx()
            // Slot width is already claimed as track+gap+measured label (see fallbackWidthPx
            // above), so the remainder here is that same measured width, not a guess.
            val labelWidth =
                (surface.interaction.slot.width - trackWidthPx - gapPx).coerceAtLeast(0f)
            text(
                label,
                slot = UiBounds(
                    trackSlot.x + trackWidthPx + gapPx,
                    surface.interaction.slot.y,
                    labelWidth,
                    surface.interaction.slot.height,
                ),
                font = context.currentFont,
                color = surface.resolved.foreground ?: theme.colors.foreground,
                centered = false,
                verticallyCentered = true,
                overflow = UiTextOverflow.Ellipsis,
                textStyle = surface.resolved.textStyle,
                semanticId = "$id.label",
            )
        }
    }
    recordSemantic(
        role = UiSemanticRole.Switch,
        id = id,
        label = label,
        bounds = surface.interaction.slot,
        truncated = false,
        selected = newChecked,
    )
    return newChecked
}
