// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.layout

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnTableCellAlign
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnTableColumn
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnTable

private val InvoiceColumns = listOf(
    ShadcnTableColumn(header = "Invoice"),
    ShadcnTableColumn(header = "Status"),
    ShadcnTableColumn(header = "Method"),
    ShadcnTableColumn(header = "Amount", align = ShadcnTableCellAlign.End),
)

private val InvoiceRows = listOf(
    listOf("INV001", "Paid", "Credit Card", "\$250.00"),
    listOf("INV002", "Pending", "PayPal", "\$150.00"),
    listOf("INV003", "Unpaid", "Bank Transfer", "\$350.00"),
)

internal val TablePage = ShowcasePage(
    id = "table",
    title = "Table",
    category = ShowcaseCategory.Layout,
    description = "A responsive table component with weighted columns and an optional caption.",
    usageCode = """shadcnTable(id = "t", columns = columns) { row { cell("INV001") } }""",
    referenceExample = "registry/new-york-v4/examples/table-demo.tsx",
    previewHeight = 420,
    notes = listOf("Cells are text-only by contract; richer cells compose a row directly."),
    hero = {
        shadcnTable(
            id = "showcase-table",
            columns = InvoiceColumns,
            caption = "A list of your recent invoices.",
        ) {
            InvoiceRows.forEach { values ->
                row { values.forEach { cell(it) } }
            }
        }
    },
)
