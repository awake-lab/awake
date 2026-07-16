// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

private const val PROPERTY_LABEL_GAP = 8f
private const val PROPERTY_LABEL_MAX_FRACTION = 0.45f
private const val PROPERTY_MIN_CONTROL_WIDTH_GLYPHS = 12
private data class PropertyInteraction(
    val slot: UiSlot,
    val hovered: Boolean,
    val active: Boolean,
    val clicked: Boolean
)

fun UiScope.propertyRow(label: String, height: Float, labelWidth: Dp = 64f.dp): UiSlot {
    val rowSlot = claimSlot(Dimension.FillMax, height.toDimension())
    val glyphPx = font?.let { resolveGlyphPx(it) } ?: 12f
    val labelWidthPx = resolvePropertyLabelWidthPx(
        rowWidthPx = rowSlot.width,
        label = label,
        requestedWidthPx = labelWidth.toPx(),
        glyphPx = glyphPx
    )
    val labelColor = theme.tokens.mutedForeground
    val resolvedFont = font
    if (resolvedFont != null) {
        val labelSlot = UiSlot(rowSlot.x, rowSlot.y + (rowSlot.height - glyphPx) / 2f, labelWidthPx, glyphPx)
        text(
            label = label,
            slot = labelSlot,
            font = resolvedFont,
            color = labelColor,
            centered = false,
            overflow = UiTextOverflow.Ellipsis
        )
    }
    return UiSlot(
        rowSlot.x + labelWidthPx + PROPERTY_LABEL_GAP,
        rowSlot.y,
        (rowSlot.width - labelWidthPx - PROPERTY_LABEL_GAP).coerceAtLeast(0f),
        rowSlot.height
    )
}

internal fun resolvePropertyLabelWidthPx(
    rowWidthPx: Float,
    label: String,
    requestedWidthPx: Float,
    glyphPx: Float
): Float {
    val availableLabelWidth = (rowWidthPx - PROPERTY_LABEL_GAP).coerceAtLeast(0f)
    val minimumControlWidth = minOf(
        (glyphPx * PROPERTY_MIN_CONTROL_WIDTH_GLYPHS).coerceAtLeast(96f),
        availableLabelWidth
    )
    val preferredLabelCap = (rowWidthPx - minimumControlWidth - PROPERTY_LABEL_GAP).coerceAtLeast(0f)
    val fractionalCap = (rowWidthPx * PROPERTY_LABEL_MAX_FRACTION).coerceAtLeast(0f)
    val maxLabelWidth = if (preferredLabelCap > 0f) {
        minOf(preferredLabelCap, fractionalCap)
    } else {
        availableLabelWidth
    }
    val baseWidth = maxOf(requestedWidthPx, label.length * glyphPx)
    return baseWidth.coerceAtMost(maxLabelWidth)
}

fun UiScope.propertyCheckbox(
    id: String,
    checked: Boolean,
    label: String,
    height: Float,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty,
    boxSize: Dp = 16f.dp
): Boolean {
    val interaction = propertyInteract(id, Dimension.FillMax, modifier.height ?: height.toDimension())
    val labelColor = theme.tokens.mutedForeground
    val resolvedFont = font
    if (resolvedFont != null) {
        val glyphPx = resolveGlyphPx(resolvedFont)
        val labelSlot = UiSlot(interaction.slot.x, interaction.slot.y + (interaction.slot.height - glyphPx) / 2f, interaction.slot.width, glyphPx)
        text(
            label = label,
            slot = labelSlot,
            font = resolvedFont,
            color = labelColor,
            centered = false,
            overflow = UiTextOverflow.Ellipsis
        )
    }

    val resolved = resolveStyle(
        style = style,
        defaults = theme.components.checkbox,
        state = MutableStyleState(hovered = interaction.hovered, active = interaction.active, selected = checked)
    )
    val boxPx = boxSize.toPx()
    val boxSlot = UiSlot(interaction.slot.x + interaction.slot.width - boxPx, interaction.slot.y + (interaction.slot.height - boxPx) / 2f, boxPx, boxPx)
    emitFillAndBorder(
        slot = boxSlot,
        fillColor = resolved.background ?: theme.tokens.background,
        radiusPx = resolved.shape.toPx(),
        borderWidth = resolved.borderWidth,
        borderColor = resolved.borderColor ?: theme.tokens.border,
        shapeSpec = resolved.shapeSpec
    )
    val newChecked = if (interaction.clicked) !checked else checked
    if (newChecked) {
        val inset = boxPx * 0.25f
        emitFillAndBorder(
            slot = UiSlot(boxSlot.x + inset, boxSlot.y + inset, boxSlot.width - inset * 2f, boxSlot.height - inset * 2f),
            fillColor = theme.tokens.accent,
            radiusPx = (resolved.shape.toPx() - inset).coerceAtLeast(0f),
            borderWidth = UiShape.none,
            borderColor = theme.tokens.accent,
            shapeSpec = resolved.shapeSpec
        )
    }
    return newChecked
}

private fun UiScope.propertyInteract(id: String, width: Dimension, height: Dimension): PropertyInteraction {
    val slot = claimSlot(width, height)
    val hovered = hitTest(slot)
    tryClaimActive(id, hovered)
    val wasActiveBeforeRelease = isActive(id)
    releaseActiveIfMatches(id)
    val active = isActive(id)
    return PropertyInteraction(
        slot = slot,
        hovered = hovered,
        active = active,
        clicked = wasActiveBeforeRelease && !active && hovered
    )
}
