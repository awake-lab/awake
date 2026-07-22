package io.github.ronjunevaldoz.awake.ui.designsystem.components.property

import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.TextStyle
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.childBox
import io.github.ronjunevaldoz.awake.ui.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font
import io.github.ronjunevaldoz.awake.ui.font.measureTextWidth
import io.github.ronjunevaldoz.awake.ui.height
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.resolveGlyphPx
import io.github.ronjunevaldoz.awake.ui.textStyle
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.basicText


private const val PROPERTY_LABEL_GAP = 8f
private const val PROPERTY_LABEL_MAX_FRACTION = 0.45f
private const val PROPERTY_MIN_CONTROL_WIDTH_GLYPHS = 12
private val DefaultPropertyRowHeight = 40f.dp

internal data class PropertyInteraction(
    val slot: UiSlot,
    val hovered: Boolean,
    val active: Boolean,
    val clicked: Boolean
)

internal data class PropertyRowLayout(
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
    theme.typography.caption
    val glyphPx = resolveGlyphPx(resolvedFont)
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
    val glyphPx = resolveGlyphPx(resolvedFont, textStyle = textStyle then TextStyle(size = labelSize))
    val layout = layoutPropertyRow(
        rowSlot = rowSlot,
        labelWidthPx = resolvePropertyLabelWidthPx(
            rowWidthPx = rowSlot.width,
            label = label,
            requestedWidthPx = labelWidth.toPx(),
            glyphPx = glyphPx,
            labelTextWidthPx = resolvedFont.measureTextWidth(label, glyphPx)
        )
    )
    val labelColor = theme.tokens.mutedForeground
    basicText(
        label = label,
        slot = layout.labelSlot,
        font = resolvedFont,
        color = labelColor,
        centered = false,
        overflow = UiTextOverflow.Ellipsis,
    )
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
    val preferredLabelCap =
        (rowWidthPx - minimumControlWidth - PROPERTY_LABEL_GAP).coerceAtLeast(0f)
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
    val resolvedLabelWidth = labelWidthPx.coerceAtLeast(0f)
        .coerceAtMost((rowSlot.width - PROPERTY_LABEL_GAP).coerceAtLeast(0f))
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