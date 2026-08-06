// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.unstyled.input.selection

import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.graphics.emitInsetAccent
import io.github.ronjunevaldoz.awake.ui.graphics.emitInsetDash
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.scope.recordSemantic
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.unstyled.paintSurface
import io.github.ronjunevaldoz.awake.ui.unstyled.resolveInteractiveSurface
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

private const val CHECKBOX_LABEL_GAP = 8f

// A real iOS-style switch, not a stretched checkbox -- fixed compact size (a switch has one
// natural size, unlike a button/row that should fill available width), pill-shaped track, and
// a sliding circular knob instead of checkbox's centered inset-square "check" mark.


fun UiScope.checkbox(
    id: String,
    checked: Boolean,
    label: String? = null,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    boxSize: Dp = 16f.dp,
    indeterminate: Boolean = false
): Boolean {
    val theme = context.currentTheme
    val surface = resolveInteractiveSurface(
        id = id,
        modifier = modifier.withSizeFallback(Dimension.FillMax, Dimension.Fixed(24f.dp)),
        style = style,
        defaults = theme.components.checkbox,
        selected = checked
    )
    val boxPx = boxSize.toPx()
    val boxSlot = io.github.ronjunevaldoz.awake.ui.layout.UiBounds(
        surface.interaction.slot.x,
        surface.interaction.slot.y + (surface.interaction.slot.height - boxPx) / 2f,
        boxPx,
        boxPx
    ).toSlot()
    paintSurface(slot = boxSlot, resolved = surface.resolved)
    // Mirrors real shadcn's triStateToggleable: clicking an Indeterminate box always lands
    // on checked=true, same as clicking an Off box -- only an On box flips to false.
    val newChecked = if (surface.interaction.clicked) {
        if (indeterminate) true else !checked
    } else checked
    val inset = boxPx * 0.25f
    if (indeterminate) {
        emitInsetDash(boxSlot, inset)
    } else if (newChecked) {
        emitInsetAccent(boxSlot, inset, surface.resolved.shape.toPx(), surface.resolved.shapeSpec)
    }
    val resolvedFont = context.currentFont
    if (label != null) {
        val labelSlot = io.github.ronjunevaldoz.awake.ui.layout.UiBounds(
            boxSlot.x + boxPx + CHECKBOX_LABEL_GAP,
            surface.interaction.slot.y,
            surface.interaction.slot.width - boxPx - CHECKBOX_LABEL_GAP,
            surface.interaction.slot.height
        ).toSlot()
        text(
            label,
            slot = labelSlot.toBounds(),
            font = resolvedFont,
            color = surface.resolved.foreground ?: theme.colors.foreground,
            centered = false,
            verticallyCentered = true,
            overflow = UiTextOverflow.Ellipsis,
            textStyle = surface.resolved.textStyle,
            semanticId = "$id.label"
        )
    }
    recordSemantic(
        role = UiSemanticRole.Checkbox,
        id = id,
        label = label,
        bounds = surface.interaction.slot.toBounds(),
        contentBounds = boxSlot.toBounds(),
        selected = newChecked
    )
    return newChecked
}
