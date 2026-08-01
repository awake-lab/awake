// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.testing.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UiInspectionTest {

    @Test
    fun reportsGlyphsWhenFontIsMissing() {
        val report = inspectUiFrame(
            primitives = listOf(
                UiDrawPrimitive.Glyph(
                    x = 10f,
                    y = 12f,
                    w = 12f,
                    h = 12f,
                    u0 = 0f,
                    v0 = 0f,
                    u1 = 1f,
                    v1 = 1f,
                    color = Color.White
                )
            ),
            frame = UiBounds(0f, 0f, 80f, 40f),
            font = null
        )

        assertFalse(report.isClean)
        assertEquals(UiInspectionIssueKind.GlyphMissingFont, report.issues.single().kind)
    }

    @Test
    fun reportsClipStackUnderflowAndUnbalancedPushes() {
        val report = inspectUiFrame(
            primitives = listOf(
                UiDrawPrimitive.ClipPop(io.github.ronjunevaldoz.awake.ui.layout.UiBounds(0f, 0f, 80f, 40f)),
                UiDrawPrimitive.ClipPush(io.github.ronjunevaldoz.awake.ui.layout.UiBounds(8f, 8f, 24f, 24f))
            ),
            frame = UiBounds(0f, 0f, 80f, 40f)
        )

        assertEquals(
            listOf(
                UiInspectionIssueKind.ClipStackUnderflow,
                UiInspectionIssueKind.ClipStackUnbalanced
            ),
            report.issues.map { it.kind }
        )
    }

    @Test
    fun cleanClippedFramePassesInspection() {
        val font = BitmapFont()
        val report = inspectUiFrame(
            primitives = listOf(
                UiDrawPrimitive.ClipPush(io.github.ronjunevaldoz.awake.ui.layout.UiBounds(0f, 0f, 40f, 20f)),
                UiDrawPrimitive.Glyph(
                    x = 8f,
                    y = 4f,
                    w = 12f,
                    h = 12f,
                    u0 = 0f,
                    v0 = 0f,
                    u1 = 0.25f,
                    v1 = 1f,
                    color = Color.White
                ),
                UiDrawPrimitive.ClipPop(io.github.ronjunevaldoz.awake.ui.layout.UiBounds(0f, 0f, 80f, 40f))
            ),
            frame = UiBounds(0f, 0f, 80f, 40f),
            font = font
        )

        assertTrue(report.isClean, report.summary())
    }

    @Test
    fun reportsOverlappingBounds() {
        val report = inspectNonOverlappingBounds(
            label = "sample cards",
            bounds = listOf(
                UiBounds(0f, 0f, 80f, 40f),
                UiBounds(60f, 20f, 80f, 40f),
                UiBounds(180f, 20f, 40f, 40f)
            )
        )

        assertFalse(report.isClean)
        assertTrue(report.summary().contains("sample cards overlap"))
    }
}
