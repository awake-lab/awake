// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.gettingstarted

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage

internal val ThemingPage = ShowcasePage(
    id = "theming",
    title = "Theming",
    category = ShowcaseCategory.GettingStarted,
    description = "Live preset, base color, accent, and dark mode theme controls.",
    usageCode = "val theme = shadcnTheme(preset = ShadcnStylePreset.Vega, dark = true)",
    previewWidth = 920,
    previewHeight = 640,
    notes = listOf("Re-themes content pane live while maintaining shell chrome."),
    hero = { state -> drawUiShowcaseControlsPreview(state) },
)
