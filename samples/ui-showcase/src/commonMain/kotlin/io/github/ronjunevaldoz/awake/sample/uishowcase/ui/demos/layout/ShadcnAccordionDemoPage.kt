// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.demos.layout

import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnAccordion
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBodyText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.spacer
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.rememberStateValue

private data class AccordionItem(val id: String, val title: String, val body: String)

private val SampleAccordionItems = listOf(
    AccordionItem("item-1", "Is it accessible?", "Yes. It adheres to the WAI-ARIA design pattern for accordion components."),
    AccordionItem("item-2", "Is it headless?", "Yes. It's headless by default, giving you full control over the visual presentation."),
    AccordionItem("item-3", "Can it be animated?", "Yes. Height transitions animate smoothly between collapsed and expanded states."),
)

internal fun ColumnScope.drawShadcnAccordionDemoPreview() {
    var selectedId: String? by rememberStateValue("showcase-accordion-demo", "selected") { "item-1" }

    shadcnBadge(id = "showcase-badge-accordion", label = "ACCORDION", variant = ShadcnBadgeVariant.Primary)
    shadcnSupportingText("A vertically stacked set of interactive headings that reveal sections of content.")
    spacer(Modifier.height(8f.dp))

    shadcnAccordion(
        items = SampleAccordionItems,
        selectedId = selectedId,
        onSelectId = { selectedId = it },
        idProvider = { it.id },
        titleProvider = { it.title },
    ) { item ->
        shadcnBodyText(item.body)
    }
}
