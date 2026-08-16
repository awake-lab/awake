// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewOverlapRule
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewValidationConfig
import io.github.ronjunevaldoz.awake.testing.ui.validateAwakeUiPreview
import kotlin.test.Test
import kotlin.test.assertTrue

class UiShowcasePreviewValidationTest {

    @Test
    fun showcasePreviewsPassSharedUiValidationRules() {
        UiShowcasePreviewEntries.forEach { entry ->
            validateAwakeUiPreview(
                metadata = entry.metadata,
                frame = entry.render(entry.metadata),
                config = configFor(entry.page.id),
            ).requireClean()
        }
    }

    /** Placeholder pages are registered gaps, not forgotten ones: each must name what is
     * missing and point at the shadcn source that defines the target. */
    @Test
    fun placeholderPagesDeclareWhatIsMissing() {
        ShowcasePages.filter { it.status == ShowcaseStatus.Placeholder }.forEach { page ->
            assertTrue(page.referenceExample.isNotBlank(), "${page.id} has no reference example")
            assertTrue(
                page.notes.any { it.startsWith("Missing: ") },
                "${page.id} does not say what is missing",
            )
        }
    }
}

private fun configFor(pageId: String): AwakeUiPreviewValidationConfig =
    when (pageId) {
        "theming" -> AwakeUiPreviewValidationConfig(
            minContentPaddingPx = 4f,
            paddingAllowIds = setOf("showcase-preview-theming"),
            overlapRules = listOf(
                AwakeUiPreviewOverlapRule(
                    label = "theme control dropdowns",
                    nodeIds = setOf(
                        "showcase-style-preset",
                        "showcase-base-color",
                        "showcase-accent",
                        "showcase-theme-mode",
                    ),
                ),
            ),
        )
        "avatar" -> AwakeUiPreviewValidationConfig(
            minContentPaddingPx = 4f,
            paddingAllowIds = setOf("ui-showcase-preview-avatar"),
            contentFitTolerancePx = 10f,
        )
        "button-group" -> AwakeUiPreviewValidationConfig(
            minContentPaddingPx = 4f,
            paddingAllowIds = setOf("ui-showcase-preview-button-group"),
            contentFitTolerancePx = 10f,
        )
        else -> AwakeUiPreviewValidationConfig(
            minContentPaddingPx = 4f,
            paddingAllowIds = setOf("ui-showcase-preview-$pageId"),
        )
    }
