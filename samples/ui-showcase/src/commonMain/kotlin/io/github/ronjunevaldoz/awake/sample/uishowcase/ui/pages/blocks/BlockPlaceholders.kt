// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.blocks

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.showcasePlaceholder

/**
 * Composite shadcn blocks Awake has no primitive for yet. They are registered so the catalog
 * reports the gap instead of hiding it -- `ShowcaseCatalogTest` asserts every one of these has
 * a reference path, so a page can only leave this list by actually being built.
 */
internal val FormBlockPage = showcasePlaceholder(
    id = "form",
    title = "Form",
    category = ShowcaseCategory.Blocks,
    description = "Validated form composition with per-field error state and submit gating.",
    missing = "no form/validation primitive in ui-headless -- shadcnField covers layout only",
    referenceExample = "registry/new-york-v4/examples/form-demo.tsx",
)

internal val ButtonGroupBlockPage = showcasePlaceholder(
    id = "button-group",
    title = "Button Group",
    category = ShowcaseCategory.Blocks,
    description = "Buttons joined into one segmented control with shared borders and radii.",
    missing = "no grouped-border recipe; shadcnButton owns its own corners",
    referenceExample = "registry/new-york-v4/examples/button-group-demo.tsx",
)

internal val ItemBlockPage = showcasePlaceholder(
    id = "item",
    title = "Item",
    category = ShowcaseCategory.Blocks,
    description = "Media object row with leading slot, title/description stack, and trailing actions.",
    missing = "no item recipe in ui-designsystem",
    referenceExample = "registry/new-york-v4/examples/item-demo.tsx",
)

internal val ChartBlockPage = showcasePlaceholder(
    id = "chart",
    title = "Chart",
    category = ShowcaseCategory.Blocks,
    description = "Bar, line, and area charts built on a shared chart container.",
    missing = "no charting layer; the headless canvas primitive is the intended foundation",
    referenceExample = "registry/new-york-v4/examples/chart-bar-demo.tsx",
)

internal val CarouselBlockPage = showcasePlaceholder(
    id = "carousel",
    title = "Carousel",
    category = ShowcaseCategory.Blocks,
    description = "A horizontally paged content slider with previous/next controls.",
    missing = "no paged-scroll primitive; ScrollState has no snap support yet",
    referenceExample = "registry/new-york-v4/examples/carousel-demo.tsx",
)

internal val DatePickerBlockPage = showcasePlaceholder(
    id = "date-picker",
    title = "Date Picker",
    category = ShowcaseCategory.Blocks,
    description = "A calendar popover for selecting a single date or a range.",
    missing = "no calendar recipe and no date primitive in ui-headless",
    referenceExample = "registry/new-york-v4/examples/date-picker-demo.tsx",
)

internal val BlockPlaceholderPages = listOf(
    FormBlockPage,
    ButtonGroupBlockPage,
    ItemBlockPage,
    ChartBlockPage,
    CarouselBlockPage,
    DatePickerBlockPage,
)
