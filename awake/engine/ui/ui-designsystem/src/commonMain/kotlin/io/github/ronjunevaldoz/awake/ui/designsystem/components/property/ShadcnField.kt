package io.github.ronjunevaldoz.awake.ui.designsystem.components.property

import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.childBox
import io.github.ronjunevaldoz.awake.ui.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font
import io.github.ronjunevaldoz.awake.ui.font.measureTextWidth
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.spacer
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.resolveGlyphPx
import io.github.ronjunevaldoz.awake.ui.textStyle
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.unstyled.separator
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*


private const val PROPERTY_LABEL_GAP = 8f
private const val PROPERTY_LABEL_MAX_FRACTION = 0.45f
private const val PROPERTY_MIN_CONTROL_WIDTH_GLYPHS = 12
private val DefaultPropertyRowHeight = 40f.dp

internal data class FieldLayout(
    val labelSlot: UiSlot,
    val controlSlot: UiSlot
)

/**
 * Standard property row with a label and a control area.
 * Returns the slot reserved for the control.
 */
fun UiScope.shadcnField(
    modifier: UiModifier = Modifier,
    labelWidth: Dp = 64f.dp,
    labelContent: BoxScope.(slot: UiSlot) -> Unit,
    content: BoxScope.(slot: UiSlot) -> Unit
): UiSlot {
    val rowSlot = claimModifiedSlot(modifier.withSizeFallback(Dimension.FillMax, Dimension.Fixed(DefaultPropertyRowHeight)))
    val resolvedFont = font
    theme.typography.caption
    val glyphPx = resolveGlyphPx(resolvedFont)
    val layout = layoutField(
        rowSlot = rowSlot,
        labelWidthPx = resolveFieldLabelWidthPx(
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

/** [shadcnField] convenience with a fixed height. */
fun UiScope.shadcnField(
    height: Dp,
    labelWidth: Dp = 64f.dp,
    labelContent: BoxScope.(slot: UiSlot) -> Unit,
    content: BoxScope.(slot: UiSlot) -> Unit
): UiSlot = shadcnField(
    modifier = Modifier.height(height),
    labelWidth = labelWidth,
    labelContent = labelContent,
    content = content
)

/** [shadcnField] convenience with a plain string label. */
fun UiScope.shadcnField(
    label: String,
    modifier: UiModifier = Modifier,
    labelWidth: Dp = 64f.dp
): UiSlot {
    val rowSlot = claimModifiedSlot(modifier.withSizeFallback(Dimension.FillMax, Dimension.Fixed(DefaultPropertyRowHeight)))
    val resolvedFont = font
    val labelSize = theme.typography.caption
    val glyphPx = resolveGlyphPx(resolvedFont, textStyle = textStyle then TextStyle(size = labelSize))
    val layout = layoutField(
        rowSlot = rowSlot,
        labelWidthPx = resolveFieldLabelWidthPx(
            rowWidthPx = rowSlot.width,
            label = label,
            requestedWidthPx = labelWidth.toPx(),
            glyphPx = glyphPx,
            labelTextWidthPx = resolvedFont.measureTextWidth(label, glyphPx)
        )
    )
    val labelColor = theme.tokens.mutedForeground
    text(
        label = label,
        slot = layout.labelSlot,
        font = resolvedFont,
        color = labelColor,
        centered = false,
        verticallyCentered = true,
        overflow = UiTextOverflow.Ellipsis,
    )
    return layout.controlSlot
}

/** [shadcnField] convenience with a plain string label and a content lambda for the control. */
fun UiScope.shadcnField(
    label: String,
    modifier: UiModifier = Modifier,
    labelWidth: Dp = 64f.dp,
    content: BoxScope.(slot: UiSlot) -> Unit
): UiSlot {
    val slot = shadcnField(label, modifier, labelWidth)
    childBox(slot).content(slot)
    return slot
}

/** [shadcnField] convenience with a plain string label and fixed height. */
fun UiScope.shadcnField(
    label: String,
    height: Dp,
    labelWidth: Dp = 64f.dp,
    content: BoxScope.(slot: UiSlot) -> Unit
): UiSlot = shadcnField(label, Modifier.height(height), labelWidth, content)

internal fun resolveFieldLabelWidthPx(
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

/** Matches real shadcn's `Field`/`FieldGroup` split: [shadcnField] is a single label+control
 * row, [shadcnFieldGroup] groups several of them into one column. Call [shadcnFieldDivider]
 * between fields to get the same seam already established for
 * [io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCard]'s header/body/footer
 * dividers -- a 4dp spacer, a border-colored hairline, then another 4dp spacer. */
fun ColumnScope.shadcnFieldGroup(
    id: String? = null,
    modifier: UiModifier = Modifier,
    content: ColumnScope.() -> Unit
): UiSlot = column(id = id, modifier = modifier) { content() }

/** Shared seam between fields inside a [shadcnFieldGroup] -- same 4dp-spacer/1dp-hairline/4dp-spacer
 * convention as `shadcnCardDivider`/`shadcnSidebarDivider`. */
fun ColumnScope.shadcnFieldDivider() {
    spacer(Modifier.height(4f.dp))
    separator(color = theme.tokens.border.withAlpha(0.72f))
    spacer(Modifier.height(4f.dp))
}

private fun layoutField(rowSlot: UiSlot, labelWidthPx: Float): FieldLayout {
    val resolvedLabelWidth = labelWidthPx.coerceAtLeast(0f)
        .coerceAtMost((rowSlot.width - PROPERTY_LABEL_GAP).coerceAtLeast(0f))
    return FieldLayout(
        labelSlot = UiSlot(rowSlot.x, rowSlot.y, resolvedLabelWidth, rowSlot.height),
        controlSlot = UiSlot(
            rowSlot.x + resolvedLabelWidth + PROPERTY_LABEL_GAP,
            rowSlot.y,
            (rowSlot.width - resolvedLabelWidth - PROPERTY_LABEL_GAP).coerceAtLeast(0f),
            rowSlot.height
        )
    )
}
