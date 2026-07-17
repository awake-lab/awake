// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.input.Input

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
        defaultHeight = Dimension.Fixed(28f.px),
        modifier = modifier
    )
    val hovered = hitTest(slot)
    tryClaimActive(id, hovered)
    val dragging = isActive(id) && Input.pointerDown
    val newValue = if (dragging) sliderValueFromPointerX(Input.pointerX, slot.x, slot.width, min, max) else value
    releaseActiveIfMatches(id)

    val resolved = resolveStyle(
        style = style,
        defaults = theme.components.slider,
        state = MutableStyleState(hovered = hovered, active = dragging)
    )
    emitFillAndBorder(
        slot = slot,
        fillColor = resolved.background ?: theme.tokens.background,
        radiusPx = resolved.shape.toPx(),
        borderWidth = resolved.borderWidth,
        borderColor = resolved.borderColor ?: theme.tokens.border,
        shapeSpec = resolved.shapeSpec
    )
    val fraction = ((newValue - min) / (max - min)).coerceIn(0f, 1f)
    val handleWidth = (slot.width * fraction).coerceAtLeast(0f)
    if (handleWidth > 0f) {
        emitFillAndBorder(
            slot = UiSlot(slot.x, slot.y, handleWidth, slot.height),
            fillColor = theme.tokens.accent,
            radiusPx = resolved.shape.toPx(),
            borderWidth = UiShape.none,
            borderColor = TransparentColor,
            shapeSpec = resolved.shapeSpec
        )
    }
    if (label != null && font != null) {
        text(
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
        contentBounds = if (handleWidth > 0f) UiSlot(slot.x, slot.y, handleWidth, slot.height) else null
    )
    return newValue
}

fun UiScope.dropdown(
    id: String,
    options: List<String>,
    selectedIndex: Int,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Int? {
    val expandedState = rememberPopupState(id, key = "expanded")
    val resolvedDefaults = theme.components.dropdown
    val selectedLabel = options.getOrNull(selectedIndex) ?: ""
    val (clicked, slot) = buttonSlot(
        id = "$id.trigger",
        modifier = modifier.copy(height = modifier.height ?: Dimension.Fixed(36f.px)),
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
        gap = 0f,
        positionProvider = UiPopupDefaults.dropdown()
    ) {
        options.forEachIndexed { index, option ->
            val optionStyle = if (index == selectedIndex) {
                Style {
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
                    modifier = UiModifier()
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
    slot: UiSlot,
    label: String,
    expanded: Boolean,
    style: Style,
    semanticId: String? = null
) {
    val resolvedFont = font ?: return
    val resolved = resolveStyle(defaults = style, state = MutableStyleState(hovered = hitTest(slot), active = expanded))
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
        slot = UiSlot(
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
        textScale = resolved.textScale,
        textSize = resolved.textSize,
        semanticId = semanticId
    )
    val chevronSlot = UiSlot(
        x = slot.x + slot.width - horizontalPad - chevronSize,
        y = slot.y + (slot.height - chevronSize * 0.6f) / 2f,
        width = chevronSize,
        height = chevronSize * 0.6f
    )
    UiIcons.chevronDown.fitTo(chevronSlot).forEach { vectorPath ->
        emit(UiDrawPrimitive.FilledPath(vectorPath.path, vectorPath.fill ?: textColor))
    }
}
