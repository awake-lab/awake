// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.gettingstarted

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage

internal val IntroductionPage = ShowcasePage(
    id = "introduction",
    title = "Introduction",
    category = ShowcaseCategory.GettingStarted,
    description = "A dedicated catalog sample for owned Awake UI components.",
    usageCode = "gameUi { theme(shadcnThemeValues()) }",
    previewHeight = 380,
    notes = listOf("Mirrors shadcn catalog chrome + sidebar + detail pane."),
    hero = { drawUiShowcaseOverviewPreview() },
)
