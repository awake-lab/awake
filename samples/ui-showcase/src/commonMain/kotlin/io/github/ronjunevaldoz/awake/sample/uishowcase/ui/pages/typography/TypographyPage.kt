// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.typography

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBlockquote
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCode
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnH1
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnH2
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnH3
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnH4
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnLarge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnLead
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnMuted
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSectionTitle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSmall
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnTextLines
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.spacer

internal val TypographyPage = ShowcasePage(
    id = "typography",
    title = "Typography",
    category = ShowcaseCategory.Typography,
    description = "Styles for headings, paragraphs, lists, quotes, and inline code.",
    usageCode = """shadcnH1("The Joke Tax Chronicles")""",
    referenceExample = "registry/new-york-v4/examples/typography-demo.tsx",
    previewWidth = 820,
    previewHeight = 760,
    notes = listOf("Every specimen here is a real recipe, not a restyled text() call."),
    hero = {
        shadcnH1("The Joke Tax Chronicles")
        shadcnLead("A modern guide to the taxation of humour in the kingdom.")
        spacer(Modifier.height(12f.dp))
        shadcnH2("The King's Plan")
        shadcnText("Body text is the default reading size for paragraphs and descriptions.")
        shadcnH3("The Joke Tax")
        shadcnText("Headings step down in size and weight without changing the reading rhythm.")
        shadcnH4("Jokester's Revolt")
        shadcnBlockquote("After the king's edict, jokes were smuggled in wagons of hay.")
        spacer(Modifier.height(12f.dp))
        shadcnLarge("Large text")
        shadcnSmall("Small text")
        shadcnMuted("Muted supporting copy.")
        shadcnCode("shadcnCode(\"inline\")")
        spacer(Modifier.height(12f.dp))
        shadcnSectionTitle(title = "Section Title", description = "Title plus supporting description in one recipe.")
        shadcnTextLines(listOf("shadcnTextLines renders a pre-split block", "one line per entry"))
    },
)
