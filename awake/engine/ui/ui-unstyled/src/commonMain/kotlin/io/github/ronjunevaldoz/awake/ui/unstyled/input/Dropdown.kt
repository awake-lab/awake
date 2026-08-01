// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.unstyled.input

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiPopupDefaults
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.fitTo
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.popup
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.recordSemantic
import io.github.ronjunevaldoz.awake.ui.rememberPopupState
import io.github.ronjunevaldoz.awake.ui.resolveStyle
import io.github.ronjunevaldoz.awake.ui.unstyled.UiIcons
import io.github.ronjunevaldoz.awake.ui.unstyled.button
import io.github.ronjunevaldoz.awake.ui.unstyled.buttonSlot
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*


// Real shadcn/ui slider shape: a thin track (not a full-height button-like bar) with a
// circular knob straddling it at the current value -- the claimed slot stays the full
// hit-test/hover target (so dragging doesn't require pixel-precise aim at a thin line), but
// only a slice of it is painted as the track, and the knob is drawn on top, not "no knob at
// all" (the previous version only drew a flat fill rectangle with no handle).


fun UiScope.dropdown(
    id: String,
    options: List<String>,
    selectedIndex: Int,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty
): Int? {
    val theme = context.currentTheme
    val expandedState = rememberPopupState(id, key = "expanded")
    val resolvedDefaults = theme.components.dropdown
    val selectedLabel = options.getOrNull(selectedIndex) ?: ""
    val (clicked, slot) = buttonSlot(
        id = "$id.trigger",
        modifier = modifier.height(modifier.heightDimension ?: Dimension.Fixed(36f.dp)),
        style = resolvedDefaults then style
    )
    if (clicked) {
        expandedState.toggle()
    }
    drawDropdownTriggerContent(
        slot = slot,
        label = selectedLabel,
        expanded = expandedState.expanded,
        style = resolvedDefaults then style,
        semanticId = "$id.label"
    )
    recordSemantic(
        role = UiSemanticRole.Dropdown,
        id = id,
        label = selectedLabel,
        bounds = slot,
        selected = expandedState.expanded
    )
    var picked: Int? = null
    val popupResult = popup(
        anchorSlot = slot,
        expanded = expandedState.expanded,
        width = Dimension.Fixed(slot.width.px),
        height = Dimension.WrapContent,
        verticalArrangement = Arrangement.spacedBy(0f.dp),
        positionProvider = UiPopupDefaults.dropdown()
    ) {
        options.forEachIndexed { index, option ->
            val optionStyle = if (index == selectedIndex) {
                Style.Companion {
                    background(theme.tokens.accent)
                    foreground(theme.tokens.accentForeground)
                }
            } else {
                Style.Empty
            }
            if (
                button(
                    id = "$id.option$index",
                    label = option,
                    modifier = Modifier
                        .width(slot.width.px)
                        .height(slot.height.px),
                    style = resolvedDefaults then style then optionStyle
                )
            ) {
                picked = index
            }
        }
    }
    if (popupResult.dismissed) {
        expandedState.close()
    }
    if (picked != null) {
        expandedState.close()
    }
    return picked
}

/** Select-trigger content: label left-aligned, expand chevron right-aligned -- matches the
 * real shadcn/ui Select trigger shape, not a big centered label ([buttonSlot]'s default).
 * Public so design-system layers building their own custom dropdown trigger (e.g. one that
 * also needs a popup menu shaped differently from [dropdown]'s own) can reuse the same
 * label/chevron layout instead of re-deriving it. */
fun UiScope.drawDropdownTriggerContent(
    slot: UiBounds,
    label: String,
    expanded: Boolean,
    style: Style,
    semanticId: String? = null
) {
    val theme = context.currentTheme
    val resolvedFont = context.currentFont
    val resolved = resolveStyle(defaults = style, state = MutableStyleState(
            hovered = hitTest(slot.toSlot()),
            active = expanded
        )
    )
    val textColor = resolved.foreground ?: theme.tokens.foreground
    // Raw px, not Dp -- `slot` (like every other widget's width/height param in this file)
    // is already raw-pixel space; subtracting a `.dp.toPx()` value here would density-scale
    // ONLY this padding and not `slot.width` itself, silently starving the label's available
    // width on any display where UiDensity.scale != 1 (confirmed via a real run: labels
    // truncated to just an ellipsis on a retina window before this fix).
    val horizontalPad = 10f
    val chevronGap = 6f
    val chevronSize = 8f
    text(
        label,
        slot = io.github.ronjunevaldoz.awake.ui.layout.UiBounds(
            x = slot.x + horizontalPad,
            y = slot.y,
            width = (slot.width - horizontalPad * 2 - chevronSize - chevronGap).coerceAtLeast(0f),
            height = slot.height
        ),
        font = resolvedFont,
        color = textColor,
        centered = false,
        verticallyCentered = true,
        overflow = UiTextOverflow.Ellipsis,
        textStyle = resolved.textStyle,
        semanticId = semanticId
    )
    val chevronSlot = io.github.ronjunevaldoz.awake.ui.layout.UiBounds(
        x = slot.x + slot.width - horizontalPad - chevronSize,
        y = slot.y + (slot.height - chevronSize * 0.6f) / 2f,
        width = chevronSize,
        height = chevronSize * 0.6f
    ).toSlot()
    UiIcons.chevronDown.fitTo(chevronSlot).forEach { vectorPath ->
        emit(UiDrawPrimitive.FilledPath(vectorPath.path, vectorPath.fill ?: textColor))
    }
}
