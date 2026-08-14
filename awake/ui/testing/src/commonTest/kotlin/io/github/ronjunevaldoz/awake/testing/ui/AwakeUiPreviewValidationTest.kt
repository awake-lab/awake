// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.testing.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiSemanticNode
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AwakeUiPreviewValidationTest {

    @Test
    fun requiresSemanticNodesByDefault() {
        val report = validateAwakeUiPreview(
            metadata = AwakeUiPreviewMetadata(
                id = "empty",
                title = "Empty",
                group = "Tests",
                summary = "No semantics",
                width = 120,
                height = 48,
            ),
            frame = AwakeUiPreviewFrame(
                primitives = emptyList(),
                background = Color.Black,
                font = BitmapFont(),
            ),
        )

        assertFalse(report.isClean)
        assertTrue(report.summary().contains("no semantic nodes"))
    }

    @Test
    fun canAllowIntentionalTruncation() {
        val report = validateAwakeUiPreview(
            metadata = AwakeUiPreviewMetadata(
                id = "truncated",
                title = "Truncated",
                group = "Tests",
                summary = "Allowed text truncation",
                width = 160,
                height = 48,
            ),
            frame = AwakeUiPreviewFrame(
                primitives = listOf(
                    UiDrawPrimitive.RoundedQuad(
                        x = 0f,
                        y = 0f,
                        w = 160f,
                        h = 48f,
                        radius = 8f,
                        color = Color.Black,
                    ),
                ),
                background = Color.Black,
                font = BitmapFont(),
                semantics = listOf(
                    UiSemanticNode(
                        role = UiSemanticRole.Text,
                        id = "headline",
                        label = "Very long title",
                        bounds = UiBounds(0f, 0f, 160f, 48f),
                        contentBounds = UiBounds(12f, 12f, 120f, 16f),
                        clippedBounds = UiBounds(12f, 12f, 120f, 16f),
                        truncated = true,
                    ),
                ),
            ),
            config = AwakeUiPreviewValidationConfig(
                allowTruncatedTextIds = setOf("headline"),
            ),
        )

        assertTrue(report.isClean, report.summary())
    }

    @Test
    fun validatesExplicitOverlapGroups() {
        val report = validateAwakeUiPreview(
            metadata = AwakeUiPreviewMetadata(
                id = "controls",
                title = "Controls",
                group = "Tests",
                summary = "Overlap regression",
                width = 280,
                height = 64,
            ),
            frame = AwakeUiPreviewFrame(
                primitives = emptyList(),
                background = Color.Black,
                font = BitmapFont(),
                semantics = listOf(
                    UiSemanticNode(
                        role = UiSemanticRole.Dropdown,
                        id = "left",
                        bounds = UiBounds(0f, 0f, 160f, 40f),
                    ),
                    UiSemanticNode(
                        role = UiSemanticRole.Dropdown,
                        id = "right",
                        bounds = UiBounds(120f, 0f, 160f, 40f),
                    ),
                ),
            ),
            config = AwakeUiPreviewValidationConfig(
                overlapRules = listOf(
                    AwakeUiPreviewOverlapRule(
                        label = "toolbar controls",
                        nodeIds = setOf("left", "right"),
                    ),
                ),
            ),
        )

        assertFalse(report.isClean)
        assertTrue(report.summary().contains("toolbar controls"))
    }
}
