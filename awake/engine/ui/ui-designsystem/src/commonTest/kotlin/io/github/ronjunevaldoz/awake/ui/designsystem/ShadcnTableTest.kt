// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.testing.ui.inspectUiFrame
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnTableCellAlign
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnTableColumn
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnTable
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnTableColumnWidthsPx
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.width
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ShadcnTableTest {

    @Test
    fun columnWidthsSplitProportionallyToWeight() {
        val columns = listOf(
            ShadcnTableColumn("Name", weight = 2f),
            ShadcnTableColumn("Email", weight = 1f),
            ShadcnTableColumn("Role", weight = 1f),
        )
        val widths = shadcnTableColumnWidthsPx(columns, availableWidthPx = 400f)
        assertEquals(3, widths.size)
        assertTrue(widths[0] in 199f..201f, "weight 2/4 of 400 must be ~200, was ${widths[0]}")
        assertTrue(widths[1] in 99f..101f, "weight 1/4 of 400 must be ~100, was ${widths[1]}")
        assertTrue(widths[2] in 99f..101f, "weight 1/4 of 400 must be ~100, was ${widths[2]}")
    }

    @Test
    fun shadcnTableRendersCleanWithMutedHeaderAndHairlines() {
        val ui = UiContext()
        val font = BitmapFont()
        ui.pushFont(font)
        ui.pushTheme(ShadcnTheme)

        ui.beginFrame(400f, 300f, testSnapshot())
        ui.createAbsolute(x = 0f, y = 0f).column(modifier = Modifier.width(400f.dp)) {
            shadcnTable(
                id = "people",
                columns = listOf(
                    ShadcnTableColumn("Name", weight = 2f),
                    ShadcnTableColumn("Email", weight = 1f),
                    ShadcnTableColumn("Role", weight = 1f, align = ShadcnTableCellAlign.End),
                ),
            ) {
                row {
                    cell("Ada Lovelace")
                    cell("ada@example.com")
                    cell("Admin")
                }
                row {
                    cell("Alan Turing")
                    cell("alan@example.com")
                    cell("Member")
                }
            }
        }
        val output = ui.finishFrame()

        val report = inspectUiFrame(output.primitives, UiBounds(0f, 0f, 400f, 300f), font)
        assertTrue(report.isClean, report.summary())

        val headerCell = assertNotNull(output.semantics.firstOrNull { it.id == "people.header.cell.0" })
        assertEquals(ShadcnTheme.colors.mutedForeground, headerCell.foregroundColor)

        // One hairline below the header row + one below each of the two body rows.
        val quadCount = output.primitives.filterIsInstance<UiDrawPrimitive.Quad>().size
        assertEquals(3, quadCount)
    }
}
