// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnResolvedTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.theme.ShadcnSpacing
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.font
import io.github.ronjunevaldoz.awake.ui.font.measureTextWidth
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.layouts.spacer
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.padding
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.scope.resolveGlyphPx
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.tailwind.Tw
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.theme.FontWeight
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.unstyled.separator

/** shadcn's `h-10` header row / a plain content-hugging body row would both work visually --
 * one fixed height keeps header and body rows trivially aligned, at the cost of not shrinking
 * body rows to their own (usually shorter) content the way real shadcn's `p-2` cells do. */
private val TABLE_ROW_HEIGHT = 40f.dp

/** Real shadcn's `Table`: header row (muted, medium weight, bottom border), body [row]s (bottom
 * hairline, hover accent), optional bottom [caption]. Column widths are divided from
 * [columns]' weights over the table's own resolved width -- no sorting/selection, visual parity
 * only. Nesting mirrors [shadcnSidebarMenu]: a container extension + a per-item extension, here
 * expressed as [ShadcnTableScope.row]/[ShadcnTableRowScope.cell] instead of a flat item list,
 * since a table row's shape (N cells) isn't a single value the way a menu item's label is. */
fun ColumnScope.shadcnTable(
    id: String,
    columns: List<ShadcnTableColumn>,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    caption: String? = null,
    content: ShadcnTableScope.() -> Unit,
): UiBounds {
    val resolvedTheme = theme.asShadcnTheme()
    return column(
        id = id,
        modifier = modifier.width(modifier.widthDimension ?: Dimension.FillMax),
        style = style,
        verticalArrangement = Arrangement.spacedBy(0f.dp),
    ) {
        shadcnTableHeaderRow(id = "$id.header", columns = columns, resolvedTheme = resolvedTheme)
        ShadcnTableScope(this, id, columns, resolvedTheme).content()
        // shadcn's TableCaption sits BELOW the body (`caption-bottom`), muted + small.
        caption?.let {
            // Real TableCaption's `mt-4` = 16dp.
            spacer(Modifier.height(Tw.Spacing.s4))
            shadcnSupportingText(it, modifier = Modifier.padding(horizontal = ShadcnSpacing.sm, vertical = 0f.dp))
        }
    }
}

/** Builds [shadcnTable]'s body, one [row] at a time. */
class ShadcnTableScope internal constructor(
    private val columnScope: ColumnScope,
    private val tableId: String,
    private val columns: List<ShadcnTableColumn>,
    private val resolvedTheme: ShadcnResolvedTheme,
) {
    private var rowIndex = 0

    fun row(content: ShadcnTableRowScope.() -> Unit) {
        val index = rowIndex++
        val cells = ArrayList<String>(columns.size)
        ShadcnTableRowScope(cells).content()
        with(columnScope) {
            shadcnTableBodyRow(
                id = "$tableId.row.$index",
                columns = columns,
                cells = cells,
                resolvedTheme = resolvedTheme,
            )
        }
    }
}

/** Collects one [ShadcnTableScope.row]'s cell values in column order; cell rendering happens
 * once the whole row is known (see [ShadcnTableScope.row]), not per [cell] call. */
class ShadcnTableRowScope internal constructor(private val cells: MutableList<String>) {
    fun cell(text: String) {
        cells += text
    }
}

/** Divides [availableWidthPx] across [columns] by their relative [ShadcnTableColumn.weight],
 * the same proportional split `Modifier.weight` does in a row. Exposed `internal` so a test can
 * assert the ratio directly instead of reaching through a full render pass. */
internal fun shadcnTableColumnWidthsPx(columns: List<ShadcnTableColumn>, availableWidthPx: Float): List<Float> {
    val totalWeight = columns.sumOf { it.weight.toDouble() }.toFloat()
    if (totalWeight <= 0f) return columns.map { 0f }
    return columns.map { (it.weight / totalWeight) * availableWidthPx }
}

private fun ColumnScope.shadcnTableHeaderRow(
    id: String,
    columns: List<ShadcnTableColumn>,
    resolvedTheme: ShadcnResolvedTheme,
) {
    val headerTextStyle = TextStyle(
        color = resolvedTheme.colors.mutedForeground,
        size = resolvedTheme.typography.label,
        weight = FontWeight.Medium,
    )
    row(modifier = Modifier.width(Dimension.FillMax).height(TABLE_ROW_HEIGHT), horizontalArrangement = Arrangement.spacedBy(0f.dp)) { rowSlot ->
        val widths = shadcnTableColumnWidthsPx(columns, rowSlot.width)
        var x = rowSlot.x
        columns.forEachIndexed { index, column ->
            shadcnTableCellText(
                bounds = UiBounds(x, rowSlot.y, widths[index], rowSlot.height),
                label = column.header,
                align = column.align,
                textStyle = headerTextStyle,
                semanticId = "$id.cell.$index",
            )
            x += widths[index]
        }
    }
    separator(thickness = 1f.dp, color = resolvedTheme.colors.border)
}

private fun ColumnScope.shadcnTableBodyRow(
    id: String,
    columns: List<ShadcnTableColumn>,
    cells: List<String>,
    resolvedTheme: ShadcnResolvedTheme,
) {
    val bodyTextStyle = TextStyle(color = resolvedTheme.colors.foreground, size = resolvedTheme.typography.body)
    surface(
        id = id,
        modifier = Modifier.width(Dimension.FillMax).height(TABLE_ROW_HEIGHT),
        style = Style {
            background(Color.Transparent)
            // Real shadcn's `hover:bg-muted/50`. Zero the inherited surface chrome (card
            // background/border/radius/padding) below -- a table row is flush content, not a card.
            hovered { background(resolvedTheme.colors.muted.withAlpha(alpha = 0.5f)) }
            contentPadding(0f.dp)
            shape(0f.dp)
            borderWidth(0f.dp)
        },
    ) { rowSlot ->
        val widths = shadcnTableColumnWidthsPx(columns, rowSlot.width)
        var x = rowSlot.x
        columns.forEachIndexed { index, column ->
            shadcnTableCellText(
                bounds = UiBounds(x, rowSlot.y, widths[index], rowSlot.height),
                label = cells.getOrNull(index).orEmpty(),
                align = column.align,
                textStyle = bodyTextStyle,
                semanticId = "$id.cell.$index",
            )
            x += widths[index]
        }
    }
    separator(thickness = 1f.dp, color = resolvedTheme.colors.border)
}

/** Draws one cell's text inside [bounds] (a column's slice of the row), inset by
 * shadcn's `p-2`/8dp cell padding and anchored to [align]'s edge -- there's no live
 * `Modifier.weight`/`.align` interplay here since [bounds] is already the resolved pixel
 * rect, so this stays a plain measure-then-place instead of routing through claimSlot().
 * [textStyle] carries its own color (header/body callers each set one) so this stays a
 * 5-param helper instead of threading color separately. */
private fun UiScope.shadcnTableCellText(
    bounds: UiBounds,
    label: String,
    align: ShadcnTableCellAlign,
    textStyle: TextStyle,
    semanticId: String,
) {
    val resolvedFont = font
    val paddingPx = ShadcnSpacing.sm.toPx()
    val innerWidth = (bounds.width - paddingPx * 2f).coerceAtLeast(0f)
    val glyphPx = resolveGlyphPx(resolvedFont, textStyle)
    val textWidth = resolvedFont.measureTextWidth(label, glyphPx).coerceAtMost(innerWidth).coerceAtLeast(1f)
    val x = when (align) {
        ShadcnTableCellAlign.Start -> bounds.x + paddingPx
        ShadcnTableCellAlign.End -> bounds.x + bounds.width - paddingPx - textWidth
    }
    text(
        label = label,
        slot = UiBounds(x, bounds.y, textWidth, bounds.height),
        font = resolvedFont,
        // Explicit, not left to text()'s own color fallback chain: that chain checks its own
        // theme-foreground default *before* textStyle.color (see Text.kt/drawResolvedText), so
        // an unset `color` here would silently override header/body's own textStyle.color.
        color = textStyle.color,
        verticallyCentered = true,
        overflow = UiTextOverflow.Ellipsis,
        textStyle = textStyle,
        semanticId = semanticId,
    )
}
