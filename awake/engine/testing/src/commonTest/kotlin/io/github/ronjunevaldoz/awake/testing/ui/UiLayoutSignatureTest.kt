// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.testing.ui

import io.github.ronjunevaldoz.awake.ui.UiSemanticNode
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.UiSlot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class UiLayoutSignatureTest {

    private fun sampleNodes() = listOf(
        UiSemanticNode(role = UiSemanticRole.Button, bounds = UiSlot(0f, 0f, 100f, 40f), id = "primary"),
        UiSemanticNode(role = UiSemanticRole.Text, bounds = UiSlot(0f, 48f, 100f, 20f), id = "label")
    )

    @Test
    fun identicalLayoutsProduceTheSameSignature() {
        assertEquals(layoutSignature(sampleNodes()), layoutSignature(sampleNodes()))
    }

    @Test
    fun signatureIsIndependentOfRecordingOrder() {
        assertEquals(layoutSignature(sampleNodes()), layoutSignature(sampleNodes().reversed()))
    }

    @Test
    fun movingAWidgetChangesTheSignature() {
        val moved = sampleNodes().map { if (it.id == "primary") it.copy(bounds = it.bounds.copy(x = 10f)) else it }
        assertNotEquals(layoutSignature(sampleNodes()), layoutSignature(moved))
    }

    @Test
    fun describeLayoutListsEveryNodeSortedByPosition() {
        val description = describeLayout(sampleNodes())
        val lines = description.lines()
        assertEquals(2, lines.size)
        assertEquals(true, lines[0].contains("primary"), "topmost node should be listed first: $description")
    }
}
