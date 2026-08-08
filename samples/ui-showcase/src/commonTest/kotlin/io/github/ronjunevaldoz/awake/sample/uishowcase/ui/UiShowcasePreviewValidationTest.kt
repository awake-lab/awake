// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewOverlapRule
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewValidationConfig
import io.github.ronjunevaldoz.awake.testing.ui.validateAwakeUiPreview
import kotlin.test.Test

class UiShowcasePreviewValidationTest {

    @Test
    fun showcasePreviewsPassSharedUiValidationRules() {
        // Skipped where previewMetadataFor returns the ios-dummy placeholder (no reflection).
        if (!previewMetadataIsReal()) return
        UiShowcasePreviewEntries.forEach { entry ->
            val metadata = previewMetadataFor(entry)
            val frame = entry.render(metadata)
            validateAwakeUiPreview(
                metadata = metadata,
                frame = frame,
                config = configFor(metadata.id),
            ).requireClean()
        }
    }
}

private fun configFor(previewId: String): AwakeUiPreviewValidationConfig =
    when (previewId) {
        "ui-showcase-theming" -> AwakeUiPreviewValidationConfig(
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
        else -> AwakeUiPreviewValidationConfig(
            minContentPaddingPx = 4f,
            paddingAllowIds = setOf("ui-showcase-preview-$previewId"),
        )
    }
