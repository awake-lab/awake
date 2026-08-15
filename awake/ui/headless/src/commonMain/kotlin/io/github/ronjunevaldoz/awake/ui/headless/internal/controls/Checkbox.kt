// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless.internal.controls

import io.github.ronjunevaldoz.awake.ui.UiPrimitiveScope
import io.github.ronjunevaldoz.awake.ui.font
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.graphics.emitCheckmark
import io.github.ronjunevaldoz.awake.ui.graphics.emitInsetDash
import io.github.ronjunevaldoz.awake.ui.graphics.emitRadioDot
import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.paintSurface
import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.resolveInteractiveSurface
import io.github.ronjunevaldoz.awake.ui.headless.internal.layout.withIntrinsicLabelWidth
import io.github.ronjunevaldoz.awake.ui.headless.internal.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.headless.internal.text.text
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.scope.recordSemantic
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.withGraphicsLayerAlpha

// Dp, not raw px: every coordinate it is added to below (`boxPx`, `surface.interaction.slot`)
// already went through `.dp.toPx()`, so a raw literal here would stay 8 physical pixels while
// everything around it doubled on a 2x display -- a visually half-size gap.
private val CHECKBOX_LABEL_GAP = 8f.dp

// A real iOS-style switch, not a stretched checkbox -- fixed compact size (a switch has one
// natural size, unlike a button/row that should fill available width), pill-shaped track, and
// a sliding circular knob instead of checkbox's centered inset-square "check" mark.

fun UiPrimitiveScope.checkbox(
    id: String,
    checked: Boolean,
    label: String? = null,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    boxSize: Dp = 16f.dp,
    indeterminate: Boolean = false,
    enabled: Boolean = true,
): Boolean {
    val theme = theme
    val defaults = theme.components.checkbox
    val sizedModifier = modifier.withSizeFallback(
        label?.let {
            withIntrinsicLabelWidth(
                modifier = modifier,
                label = it,
                style = style,
                defaults = defaults,
                extraWidth = boxSize + CHECKBOX_LABEL_GAP,
            ).widthDimension
        } ?: Dimension.FillMax,
        Dimension.Fixed(24f.dp),
    )
    val surface = resolveInteractiveSurface(
        id = id,
        modifier = sizedModifier,
        style = style,
        defaults = defaults,
        selected = checked,
        disabled = !enabled,
        enabled = enabled,
    )
    val boxPx = boxSize.toPx()
    val boxSlot = UiBounds(
        surface.interaction.slot.x,
        surface.interaction.slot.y + (surface.interaction.slot.height - boxPx) / 2f,
        boxPx,
        boxPx,
    )
    // Reference's `disabled:opacity-50` treatment, same single group-alpha shape as
    // `Buttons.kt`'s `buttonSlotInternal` -- covers the box fill/border, the check/dash mark,
    // and the label as one composited unit so the label drawn on top of the box's own paint
    // never gets double-dimmed.
    return withGraphicsLayerAlpha(if (enabled) 1f else 0.5f) {
        paintSurface(
            slot = boxSlot,
            resolved = if (checked || indeterminate) {
                // CSS checked state paints the same primary surface through the border; an
                // inset one-pixel border would shrink the black fill and produce a visibly
                // smaller indicator than the reference.
                surface.resolved.copy(borderWidth = UiShape.none)
            } else {
                surface.resolved
            },
            fillColor = if (checked || indeterminate) theme.colors.primary else surface.resolved.background,
            borderColor = if (checked || indeterminate) theme.colors.primary else surface.resolved.borderColor,
        )
        // Mirrors real shadcn's triStateToggleable: clicking an Indeterminate box always lands
        // on checked=true, same as clicking an Off box -- only an On box flips to false.
        val newChecked = if (surface.interaction.clicked) {
            if (indeterminate) true else !checked
        } else {
            checked
        }
        val inset = boxPx * 0.25f
        if (indeterminate) {
            emitInsetDash(boxSlot, inset)
        } else if (newChecked) {
            emitCheckmark(boxSlot)
        }
        val resolvedFont = font
        if (label != null) {
            val gapPx = CHECKBOX_LABEL_GAP.toPx()
            val labelSlot = UiBounds(
                boxSlot.x + boxPx + gapPx,
                surface.interaction.slot.y,
                surface.interaction.slot.width - boxPx - gapPx,
                surface.interaction.slot.height,
            )
            text(
                label,
                slot = labelSlot,
                font = resolvedFont,
                // The resolved foreground styles the indicator itself. The caption is a
                // separate content node and must retain the normal text color when the box is
                // selected; using primaryForeground here made a checked checkbox's label white
                // on the page background.
                color = if (enabled) theme.colors.foreground else theme.colors.mutedForeground,
                centered = false,
                verticallyCentered = true,
                overflow = UiTextOverflow.Ellipsis,
                textStyle = surface.resolved.textStyle,
                semanticId = "$id.label",
            )
        }
        recordSemantic(
            role = UiSemanticRole.Checkbox,
            id = id,
            label = label,
            bounds = surface.interaction.slot,
            contentBounds = boxSlot,
            selected = newChecked,
            indeterminate = indeterminate,
        )
        newChecked
    }
}

/**
 * Radio indicator primitive. Radio buttons share checkbox interaction semantics, but their
 * selected affordance is a centered dot; reusing [checkbox] here rendered a checkmark in a
 * circle, which is visibly different from shadcn's `RadioGroupItem`.
 */
fun UiPrimitiveScope.radio(
    id: String,
    selected: Boolean,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    enabled: Boolean = true,
    boxSize: Dp = 16f.dp,
): Boolean {
    val theme = theme
    val surface = resolveInteractiveSurface(
        id = id,
        // A shadcn RadioGroupItem is `size-4`, not a full-width checkbox row. Its authored
        // label is a sibling in the caller's `flex items-center gap-2` row, so claiming the
        // remaining width here makes both the semantic crop and the actual layout diverge.
        modifier = modifier.withSizeFallback(Dimension.Fixed(boxSize), Dimension.Fixed(boxSize)),
        style = style,
        defaults = theme.components.checkbox,
        selected = selected,
        disabled = !enabled,
        enabled = enabled,
    )
    val boxPx = boxSize.toPx()
    val boxSlot = UiBounds(
        surface.interaction.slot.x,
        surface.interaction.slot.y + (surface.interaction.slot.height - boxPx) / 2f,
        boxPx,
        boxPx,
    )
    return withGraphicsLayerAlpha(if (enabled) 1f else 0.5f) {
        paintSurface(
            slot = boxSlot,
            resolved = surface.resolved.copy(shapeSpec = io.github.ronjunevaldoz.awake.ui.UiShapeSpec.Circle),
            fillColor = surface.resolved.background ?: theme.colors.background,
            borderColor = surface.resolved.borderColor ?: theme.colors.border,
            shapeSpec = io.github.ronjunevaldoz.awake.ui.UiShapeSpec.Circle,
        )
        if (selected) {
            emitRadioDot(boxSlot, theme.colors.primary)
        }
        val next = if (surface.interaction.clicked && enabled) true else selected
        recordSemantic(
            role = UiSemanticRole.Radio,
            id = id,
            bounds = surface.interaction.slot,
            contentBounds = boxSlot,
            selected = next,
        )
        next
    }
}
