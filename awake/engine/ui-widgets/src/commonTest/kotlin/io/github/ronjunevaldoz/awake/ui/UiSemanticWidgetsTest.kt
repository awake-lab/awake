// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.testing.ui.inspectSemanticContentFit
import io.github.ronjunevaldoz.awake.testing.ui.inspectSemanticNodes
import io.github.ronjunevaldoz.awake.testing.ui.inspectTextTruncation
import io.github.ronjunevaldoz.awake.testing.ui.requireSemanticNode
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UiSemanticWidgetsTest {

    @Test
    fun buttonEmitsSemanticButtonAndLabelNodes() {
        val font = UiFonts.default()
        val context = UiContext()
        context.beginFrame(240f, 96f, testSnapshot())
        context.absolute(20f, 20f, font = font).button(
            id = "primary-action",
            modifier = UiModifier().width(180f.px).height(44f.px),
            label = "Awake Button"
        )

        val semantics = context.semanticNodes()

        inspectSemanticNodes(semantics).requireClean()
        inspectSemanticContentFit(semantics, tolerancePx = 1f).requireClean()
        val buttonNode = requireSemanticNode(semantics, id = "primary-action", role = UiSemanticRole.Button)
        val labelNode = requireSemanticNode(semantics, id = "primary-action.label", role = UiSemanticRole.Text)

        assertEquals("Awake Button", buttonNode.label)
        assertEquals("Awake Button", labelNode.label)
        assertTrue(labelNode.contentBounds != null)
    }

    @Test
    fun ellipsizedTextIsMarkedAsTruncated() {
        val font = UiFonts.default()
        val context = UiContext()
        context.beginFrame(180f, 64f, testSnapshot())
        context.absolute(12f, 12f, font = font).text(
            label = "This label is intentionally too wide for the slot",
            slot = UiSlot(12f, 12f, 80f, 16f),
            overflow = UiTextOverflow.Ellipsis,
            semanticId = "truncated.copy"
        )

        val semantics = context.semanticNodes()

        inspectSemanticNodes(semantics).requireClean()
        assertTrue(inspectTextTruncation(semantics).issues.any { it.nodeId == "truncated.copy" })
    }
}
