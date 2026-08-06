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

    // ──────────────────────────────────────────────────────────
    // inspectTextCentering
    // ──────────────────────────────────────────────────────────

    @Test
    fun textCenteringPassesWhenContentCenterMatchesNodeCenter() {
        // 40×40 node, 8×12 content block perfectly centred at (16,14)→(24,26)
        val report = inspectTextCentering(
            listOf(
                UiSemanticNode(
                    role = UiSemanticRole.Text,
                    id = "otp.slot.0",
                    label = "4",
                    bounds = UiBounds(0f, 0f, 40f, 40f),
                    contentBounds = UiBounds(16f, 14f, 8f, 12f) // center = (20,20)
                )
            ),
            tolerancePx = 1f
        )

        assertTrue(report.isClean, report.summary())
    }

    @Test
    fun textCenteringFailsWhenContentIsHorizontallyOffCenter() {
        // content starts at x=0 (left edge) — not centred
        val report = inspectTextCentering(
            listOf(
                UiSemanticNode(
                    role = UiSemanticRole.Text,
                    id = "otp.slot.0",
                    label = "4",
                    bounds = UiBounds(0f, 0f, 40f, 40f),
                    contentBounds = UiBounds(0f, 14f, 8f, 12f) // contentCX=4, nodeCX=20
                )
            ),
            tolerancePx = 1f
        )

        assertFalse(report.isClean, "expected ContentNotCentered issue")
        assertEquals(UiSemanticIssueKind.ContentNotCentered, report.issues.single().kind)
        assertTrue(report.issues.single().message.contains("horizontally"))
    }

    @Test
    fun textCenteringFailsWhenContentIsVerticallyOffCenter() {
        // content sits at y=0 (top) — not centred vertically
        val report = inspectTextCentering(
            listOf(
                UiSemanticNode(
                    role = UiSemanticRole.Text,
                    id = "otp.slot.0",
                    label = "4",
                    bounds = UiBounds(0f, 0f, 40f, 40f),
                    contentBounds = UiBounds(16f, 0f, 8f, 12f) // contentCY=6, nodeCY=20
                )
            ),
            tolerancePx = 1f
        )

        assertFalse(report.isClean, "expected ContentNotCentered issue")
        assertEquals(UiSemanticIssueKind.ContentNotCentered, report.issues.single().kind)
        assertTrue(report.issues.single().message.contains("vertically"))
    }

    @Test
    fun textCenteringRespectsAllowIds() {
        // This node is off-centre, but its id is in allowIds — should pass
        val report = inspectTextCentering(
            listOf(
                UiSemanticNode(
                    role = UiSemanticRole.Text,
                    id = "left-label",
                    label = "Name",
                    bounds = UiBounds(0f, 0f, 200f, 24f),
                    contentBounds = UiBounds(8f, 4f, 40f, 16f) // deliberately left-aligned
                )
            ),
            tolerancePx = 1f,
            allowIds = setOf("left-label")
        )

        assertTrue(report.isClean, "node in allowIds should be exempt: ${report.summary()}")
    }

    @Test
    fun textCenteringIgnoresNonTextNodes() {
        // Panel node with no contentBounds — must not be checked
        val report = inspectTextCentering(
            listOf(
                UiSemanticNode(
                    role = UiSemanticRole.Panel,
                    id = "otp.slot.0",
                    bounds = UiBounds(0f, 0f, 36f, 40f)
                )
            )
        )

        assertTrue(report.isClean, "non-Text nodes must be ignored by inspectTextCentering")
    }
}
