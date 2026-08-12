// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceBorder
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.separator
import io.github.ronjunevaldoz.awake.ui.headless.surface
import io.github.ronjunevaldoz.awake.ui.headless.text
import io.github.ronjunevaldoz.awake.ui.headless.weight

/** Shadcn table scope backed by neutral row/cell primitives. Cells are intentionally text-only;
 * richer cells can be added
 * by composing a row directly once the neutral table contract grows slots. */

class ShadcnTableScope internal constructor(private val owner: ColumnScope) {
    fun row(content: ShadcnTableRowScope.() -> Unit) {
        val cells = ShadcnTableRowScope().also(content).values
        with(owner) {
            row(horizontalArrangement = Arrangement.Start, modifier = Modifier.height(40f.dp)) {
                cells.forEachIndexed { index, value ->
                    text(
                        label = value,
                        modifier = Modifier.weight(1f),
                        visuals = SurfaceStyle(foreground = themeValues.colors.foreground, textSize = themeValues.typography.body),
                        semanticId = "cell.$index",
                    )
                }
            }
            separator()
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

fun ColumnScope.shadcnTable(
    id: String,
    columns: List<ShadcnTableColumn>,
    modifier: Modifier = Modifier,
    caption: String? = null,
    content: ShadcnTableScope.() -> Unit,
): UiBounds = surface(
    id = id,
    modifier = modifier,
    style = SurfaceStyle(
        border = SurfaceBorder(1f.dp, themeValues.colors.border),
        cornerRadius = themeValues.shapes.md,
        contentPadding = io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets(8f.dp),
    ),
    verticalArrangement = Arrangement.spacedBy(0f.dp),
) {
    row(modifier = Modifier.height(40f.dp)) {
        columns.forEachIndexed { index, column ->
            text(
                column.header,
                modifier = Modifier.weight(column.weight),
                visuals = SurfaceStyle(
                    foreground = themeValues.colors.mutedForeground,
                    textSize = themeValues.typography.label,
                ),
                semanticId = "$id.header.cell.$index",
            )
        }
    }
    separator()
    ShadcnTableScope(this).content()
    caption?.let { text(it, visuals = SurfaceStyle(foreground = themeValues.colors.mutedForeground, textSize = themeValues.typography.caption)) }
}
