// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnTableCaptionStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnTableCellStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnTableHeaderStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnTableStyle
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.surface
import io.github.ronjunevaldoz.awake.ui.headless.text
import io.github.ronjunevaldoz.awake.ui.headless.weight

/** Shadcn table scope backed by neutral row/cell primitives. Cells are intentionally text-only;
 * richer cells can be added
 * by composing a row directly once the neutral table contract grows slots. */

class ShadcnTableScope internal constructor(private val owner: ColumnScope) {
    private var rowIndex = 0

    fun row(content: ShadcnTableRowScope.() -> Unit) {
        val cells = ShadcnTableRowScope().also(content).values
        val rowNumber = rowIndex++
        with(owner) {
            row(horizontalArrangement = Arrangement.Start, modifier = Modifier.height(40f.dp)) {
                cells.forEachIndexed { index, value ->
                    text(
                        label = value,
                        modifier = Modifier.weight(1f),
                        style = shadcnTableCellStyle(themeValues),
                        semanticId = "cell.$rowNumber.$index",
                    )
                }
            }
            shadcnSeparator(id = "row.$rowNumber.separator")
        }
    }
}

class ShadcnTableRowScope internal constructor() {
    internal val values = mutableListOf<String>()
    fun cell(value: String) {
        values += value
    }
}

fun shadcnTableColumnWidthsPx(columns: List<ShadcnTableColumn>, availableWidthPx: Float): List<Float> {
    val totalWeight = columns.sumOf { it.weight.toDouble() }.toFloat()
    if (totalWeight <= 0f) return columns.map { 0f }
    return columns.map { (it.weight / totalWeight) * availableWidthPx }
}

fun UiScope.shadcnTable(
    id: String,
    columns: List<ShadcnTableColumn>,
    modifier: Modifier = Modifier,
    caption: String? = null,
    content: ShadcnTableScope.() -> Unit,
): UiBounds = surface(
    id = id,
    modifier = modifier,
    style = shadcnTableStyle(themeValues),
    verticalArrangement = Arrangement.spacedBy(0f.dp),
) {
    row(modifier = Modifier.height(40f.dp)) {
        columns.forEachIndexed { index, column ->
            text(
                column.header,
                modifier = Modifier.weight(column.weight),
                style = shadcnTableHeaderStyle(themeValues),
                semanticId = "$id.header.cell.$index",
            )
        }
    }
    shadcnSeparator()
    ShadcnTableScope(this).content()
    caption?.let { text(it, style = shadcnTableCaptionStyle(themeValues)) }
}
