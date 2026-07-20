// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui


// Real shadcn/ui slider shape: a thin track (not a full-height button-like bar) with a
// circular knob straddling it at the current value -- the claimed slot stays the full
// hit-test/hover target (so dragging doesn't require pixel-precise aim at a thin line), but
// only a slice of it is painted as the track, and the knob is drawn on top, not "no knob at
// all" (the previous version only drew a flat fill rectangle with no handle).
private const val SLIDER_TRACK_HEIGHT_PX = 6f
private const val SLIDER_KNOB_DIAMETER_PX = 16f

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
        defaultHeight = Dimension.Fixed(28f.dp),
        modifier = modifier
    )
    val hovered = hitTest(slot)
    tryClaimActive(id, hovered)
    val pointerDown = context.pointerDown() // We need this in UiScope or context
    val dragging = isActive(id) && pointerDown
    val newValue = if (dragging) sliderValueFromPointerX(context.pointerX(), slot.x, slot.width, min, max) else value
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
            borderColor = TransparentColor,
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
        modifier = modifier.copy(height = modifier.height ?: Dimension.Fixed(36f.dp)),
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
