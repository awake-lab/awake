// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.testing.ui

import io.github.ronjunevaldoz.awake.ui.UiSemanticNode
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UiSemanticInspectionTest {

    @Test
    fun reportsDuplicateSemanticIds() {
        val report = inspectSemanticNodes(
            listOf(
                UiSemanticNode(role = UiSemanticRole.Button, id = "save", bounds = UiBounds(0f, 0f, 80f, 36f)),
                UiSemanticNode(role = UiSemanticRole.Text, id = "save", bounds = UiBounds(8f, 8f, 64f, 20f))
            )
        )

        assertFalse(report.isClean)
        assertEquals(UiSemanticIssueKind.DuplicateSemanticId, report.issues.single().kind)
    }

    @Test
    fun reportsTruncatedTextNodes() {
        val report = inspectTextTruncation(
            listOf(
                UiSemanticNode(
                    role = UiSemanticRole.Text,
                    id = "header.title",
                    label = "Awake UI Showcase",
                    bounds = UiBounds(0f, 0f, 120f, 24f),
                    truncated = true,
                    lineCount = 1
                )
            )
        )

        assertFalse(report.isClean)
        assertEquals(UiSemanticIssueKind.TextTruncated, report.issues.single().kind)
    }

    @Test
    fun reportsOverlappingSemanticPeers() {
        val report = inspectSemanticOverlaps(
            label = "header cards",
            nodes = listOf(
                UiSemanticNode(role = UiSemanticRole.Panel, id = "left", bounds = UiBounds(0f, 0f, 180f, 96f)),
                UiSemanticNode(role = UiSemanticRole.Panel, id = "right", bounds = UiBounds(140f, 0f, 180f, 96f))
            )
        )

        assertFalse(report.isClean)
        assertTrue(report.summary().contains("header cards overlap"))
    }

    @Test
    fun contentFitPassesWhenTextStaysInsideBounds() {
        val report = inspectSemanticContentFit(
            listOf(
                UiSemanticNode(
                    role = UiSemanticRole.Text,
                    id = "body.copy",
                    bounds = UiBounds(0f, 0f, 180f, 48f),
                    contentBounds = UiBounds(8f, 8f, 120f, 16f),
                    clippedBounds = UiBounds(8f, 8f, 120f, 16f)
                )
            ),
            tolerancePx = 1f
        )

        assertTrue(report.isClean, report.summary())
    }
}
