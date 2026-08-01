// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.testing.ui.inspectSemanticContentFit
import io.github.ronjunevaldoz.awake.testing.ui.inspectSemanticNodes
import io.github.ronjunevaldoz.awake.testing.ui.inspectTextTruncation
import io.github.ronjunevaldoz.awake.testing.ui.requireSemanticNode
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.offset
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.unstyled.button
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UiSemanticWidgetsTest {

    @Test
    fun buttonEmitsSemanticButtonAndLabelNodes() {
        val font = UiFonts.default()
        val context = UiContext()
        context.beginFrame(240f, 96f, testSnapshot())
        context.createAbsolute(modifier = Modifier.offset(20f.dp, 20f.dp), font = font).button(
            id = "primary-action",
            modifier = Modifier.width(180f.px).height(44f.px),
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
        context.createAbsolute(modifier = Modifier.offset(12f.dp, 12f.dp), font = font).text(
            label = "This label is intentionally too wide for the slot",
            slot = UiBounds(12f, 12f, 80f, 16f),
            overflow = UiTextOverflow.Ellipsis,
            semanticId = "truncated.copy"
        )

        val semantics = context.semanticNodes()

        inspectSemanticNodes(semantics).requireClean()
        assertTrue(inspectTextTruncation(semantics).issues.any { it.nodeId == "truncated.copy" })
    }
}
