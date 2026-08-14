// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.layout

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnAccordion
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.headless.rememberStateValue

private data class AccordionItem(val id: String, val title: String, val body: String)

private val SampleAccordionItems = listOf(
    AccordionItem("item-1", "Is it accessible?", "Yes. It adheres to the WAI-ARIA design pattern for accordion components."),
    AccordionItem("item-2", "Is it headless?", "Yes. It's headless by default, giving you full control over the visual presentation."),
    AccordionItem("item-3", "Can it be animated?", "Yes. Height transitions animate smoothly between collapsed and expanded states."),
)

internal val AccordionPage = ShowcasePage(
    id = "accordion",
    title = "Accordion",
    category = ShowcaseCategory.Layout,
    description = "A vertically stacked set of interactive headings that each reveal a section of content.",
    usageCode = """shadcnAccordion(items = items, selectedId = selected, onSelectId = { selected = it })""",
    referenceExample = "registry/new-york-v4/examples/accordion-demo.tsx",
    previewHeight = 420,
    notes = listOf("Collapsible group supporting WAI-ARIA single selection."),
    hero = {
        var selectedId: String? by rememberStateValue("showcase-accordion-demo", "selected") { "item-1" }
        shadcnAccordion(
            items = SampleAccordionItems,
            selectedId = selectedId,
            onSelectId = { selectedId = it },
            idProvider = { it.id },
            titleProvider = { it.title },
        ) { item ->
            shadcnText(item.body)
        }
    },
)
