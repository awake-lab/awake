// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.layout

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnMuted
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnScrollArea
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.rememberScrollState
import io.github.ronjunevaldoz.awake.ui.headless.spacer
import io.github.ronjunevaldoz.awake.ui.headless.uiScope
import io.github.ronjunevaldoz.awake.ui.headless.verticalScroll
import io.github.ronjunevaldoz.awake.ui.headless.width

internal val ScrollAreaPage = ShowcasePage(
    id = "scroll-area",
    title = "Scroll Area",
    category = ShowcaseCategory.Layout,
    description = "Augments native scroll functionality for custom, cross-browser styling.",
    usageCode = """shadcnScrollArea(id = "sa", modifier = Modifier.height(168f.dp).verticalScroll(state)) { ... }""",
    referenceExample = "registry/new-york-v4/examples/scroll-area-demo.tsx",
    previewHeight = 400,
    notes = listOf("The scroll thumb appears only when content exceeds the viewport."),
    hero = {
        // Starts partially scrolled so viewport clipping and the thumb are visible immediately.
        val scrollState = uiScope().rememberScrollState(
            id = "showcase-scroll-state",
            initialOffsetY = 34f,
        )
        shadcnMuted("Viewport clipping and the scrollbar thumb are both live here.")
        spacer(Modifier.height(8f.dp))
        uiScope().shadcnScrollArea(
            id = "showcase-scroll-panel",
            modifier = Modifier
                .width(420f.dp)
                .height(168f.dp)
                .verticalScroll(scrollState),
        ) {
            repeat(8) { index ->
                shadcnButton(
                    id = "showcase-scroll-item-$index",
                    label = "Scene action row ${index + 1}",
                    modifier = Modifier.width(360f.dp).height(32f.dp),
                    variant = if (index % 2 == 0) ShadcnButtonVariant.Outline else ShadcnButtonVariant.Ghost,
                )
            }
        }
    },
)
