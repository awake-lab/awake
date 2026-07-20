// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.font.measureTextWidth

private const val PROPERTY_LABEL_GAP = 8f
private const val PROPERTY_LABEL_MAX_FRACTION = 0.45f
private const val PROPERTY_MIN_CONTROL_WIDTH_GLYPHS = 12
private val DefaultPropertyRowHeight = 40f.dp
private val DefaultPropertyCheckboxHeight = 28f.dp

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

/**
 * Standard property row with a label and a control area.
 * Returns the slot reserved for the control.
 */
fun UiScope.propertyRow(
    modifier: UiModifier = UiModifier(),
    labelWidth: Dp = 64f.dp,
    labelContent: BoxScope.(slot: UiSlot) -> Unit,
    content: BoxScope.(slot: UiSlot) -> Unit
): UiSlot {
    val rowSlot = claimModifiedSlot(
        defaultWidth = Dimension.FillMax,
        defaultHeight = Dimension.Fixed(DefaultPropertyRowHeight),
        modifier = modifier
    )
    val resolvedFont = font
    val glyphPx = resolvedFont?.let { resolveGlyphPx(it, textScale, theme.typography.caption) } ?: 12f
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
    childBox(layout.labelSlot).labelContent(layout.labelSlot)
    childBox(layout.controlSlot).content(layout.controlSlot)
    return layout.controlSlot
}

/** [propertyRow] convenience with a fixed height. */
fun UiScope.propertyRow(
    height: Dp,
    labelWidth: Dp = 64f.dp,
    labelContent: BoxScope.(slot: UiSlot) -> Unit,
    content: BoxScope.(slot: UiSlot) -> Unit
): UiSlot = propertyRow(
    modifier = UiModifier().height(height),
    labelWidth = labelWidth,
    labelContent = labelContent,
    content = content
)

/** [propertyRow] convenience with a plain string label. */
fun UiScope.propertyRow(
    label: String,
    modifier: UiModifier = UiModifier(),
    labelWidth: Dp = 64f.dp
): UiSlot {
    val rowSlot = claimModifiedSlot(
        defaultWidth = Dimension.FillMax,
        defaultHeight = Dimension.Fixed(DefaultPropertyRowHeight),
        modifier = modifier
    )
    val resolvedFont = font
    val labelSize = theme.typography.caption
    val glyphPx = resolvedFont?.let { resolveGlyphPx(it, textScale, labelSize) } ?: 12f
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
            overflow = UiTextOverflow.Ellipsis,
            textSize = labelSize
        )
    }
    return layout.controlSlot
}

/** [propertyRow] convenience with a plain string label and a content lambda for the control. */
fun UiScope.propertyRow(
    label: String,
    modifier: UiModifier = UiModifier(),
    labelWidth: Dp = 64f.dp,
    content: BoxScope.(slot: UiSlot) -> Unit
): UiSlot {
    val slot = propertyRow(label, modifier, labelWidth)
    childBox(slot).content(slot)
    return slot
}

/** [propertyRow] convenience with a plain string label and fixed height. */
fun UiScope.propertyRow(
    label: String,
    height: Dp,
    labelWidth: Dp = 64f.dp,
    content: BoxScope.(slot: UiSlot) -> Unit
): UiSlot = propertyRow(label, UiModifier().height(height), labelWidth, content)

fun UiScope.propertyCheckbox(
    id: String,
    checked: Boolean,
    label: String,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty,
    boxSize: Dp = 16f.dp
): Boolean {
    val interaction = propertyInteract(
        id = id,
        width = Dimension.FillMax,
        height = modifier.height ?: Dimension.Fixed(DefaultPropertyCheckboxHeight),
        modifier = modifier
    )
    val labelColor = theme.tokens.mutedForeground
    val resolvedFont = font
    if (resolvedFont != null) {
        val labelSize = theme.typography.caption
        val glyphPx = resolveGlyphPx(resolvedFont, textScale, labelSize)
        val labelSlot = UiSlot(interaction.slot.x, interaction.slot.y + (interaction.slot.height - glyphPx) / 2f, interaction.slot.width, glyphPx)
        text(
            label = label,
            slot = labelSlot,
            font = resolvedFont,
            color = labelColor,
            centered = false,
            overflow = UiTextOverflow.Ellipsis,
            textSize = labelSize
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
            fillColor = theme.tokens.primary,
            radiusPx = (resolved.shape.toPx() - inset).coerceAtLeast(0f),
            borderWidth = UiShape.none,
            borderColor = theme.tokens.primary,
            shapeSpec = resolved.shapeSpec
        )
    }
    return newChecked
}

/** [propertyCheckbox] convenience with fixed height. */
fun UiScope.propertyCheckbox(
    id: String,
    checked: Boolean,
    label: String,
    height: Dp,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty,
    boxSize: Dp = 16f.dp
): Boolean = propertyCheckbox(
    id = id,
    checked = checked,
    label = label,
    modifier = modifier.height(height),
    style = style,
    boxSize = boxSize
)

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

private fun UiScope.propertyInteract(
    id: String,
    width: Dimension,
    height: Dimension,
    modifier: UiModifier = UiModifier()
): PropertyInteraction {
    val slot = claimModifiedSlot(width, height, modifier)
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
