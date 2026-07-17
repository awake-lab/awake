// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.font.measureTextWidth

private const val PROPERTY_LABEL_GAP = 8f
private const val PROPERTY_LABEL_MAX_FRACTION = 0.45f
private const val PROPERTY_MIN_CONTROL_WIDTH_GLYPHS = 12

private data class PropertyInteraction(
    val slot: UiSlot,
    val hovered: Boolean,
    val active: Boolean,
    val clicked: Boolean
)

private data class PropertyRowLayout(
    val labelSlot: UiSlot,
    val controlSlot: UiSlot
)

fun UiScope.propertyRow(
    height: Float,
    labelWidth: Dp = 64f.dp,
    labelContent: UiAbsoluteDslScope.(slot: UiSlot) -> Unit,
    content: UiAbsoluteDslScope.(slot: UiSlot) -> Unit
): UiSlot {
    val rowSlot = claimSlot(Dimension.FillMax, height.toDimension())
    val resolvedFont = font
    val glyphPx = resolvedFont?.let { resolveGlyphPx(it) } ?: 12f
    val layout = layoutPropertyRow(
        rowSlot = rowSlot,
        labelWidthPx = resolvePropertyLabelWidthPx(
            rowWidthPx = rowSlot.width,
            label = "",
            requestedWidthPx = labelWidth.toPx(),
            glyphPx = glyphPx,
            labelTextWidthPx = labelWidth.toPx()
        )
    )
    UiAbsoluteDslScope(context.absolute(layout.labelSlot, resolvedFont, theme, textScale))
        .labelContent(layout.labelSlot)
    UiAbsoluteDslScope(context.absolute(layout.controlSlot, resolvedFont, theme, textScale))
        .content(layout.controlSlot)
    return layout.controlSlot
}

fun UiScope.propertyRow(label: String, height: Float, labelWidth: Dp = 64f.dp): UiSlot {
    val rowSlot = claimSlot(Dimension.FillMax, height.toDimension())
    val resolvedFont = font
    val glyphPx = resolvedFont?.let { resolveGlyphPx(it) } ?: 12f
    val layout = layoutPropertyRow(
        rowSlot = rowSlot,
        labelWidthPx = resolvePropertyLabelWidthPx(
        rowWidthPx = rowSlot.width,
        label = label,
        requestedWidthPx = labelWidth.toPx(),
        glyphPx = glyphPx,
        labelTextWidthPx = resolvedFont?.measureTextWidth(label, glyphPx) ?: label.length * glyphPx
    )
    )
    val labelColor = theme.tokens.mutedForeground
    if (resolvedFont != null) {
        text(
            label = label,
            slot = layout.labelSlot,
            font = resolvedFont,
            color = labelColor,
            centered = false,
            overflow = UiTextOverflow.Ellipsis
        )
    }
    return layout.controlSlot
}

internal fun resolvePropertyLabelWidthPx(
    rowWidthPx: Float,
    label: String,
    requestedWidthPx: Float,
    glyphPx: Float,
    labelTextWidthPx: Float = label.length * glyphPx
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
    val baseWidth = maxOf(requestedWidthPx, labelTextWidthPx)
    return baseWidth.coerceAtMost(maxLabelWidth)
}

private fun layoutPropertyRow(rowSlot: UiSlot, labelWidthPx: Float): PropertyRowLayout {
    val resolvedLabelWidth = labelWidthPx.coerceAtLeast(0f).coerceAtMost((rowSlot.width - PROPERTY_LABEL_GAP).coerceAtLeast(0f))
    return PropertyRowLayout(
        labelSlot = UiSlot(rowSlot.x, rowSlot.y, resolvedLabelWidth, rowSlot.height),
        controlSlot = UiSlot(
            rowSlot.x + resolvedLabelWidth + PROPERTY_LABEL_GAP,
            rowSlot.y,
            (rowSlot.width - resolvedLabelWidth - PROPERTY_LABEL_GAP).coerceAtLeast(0f),
            rowSlot.height
        )
    )
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
