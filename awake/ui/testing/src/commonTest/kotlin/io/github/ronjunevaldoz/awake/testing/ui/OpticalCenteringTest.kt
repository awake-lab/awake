// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.testing.ui

import io.github.ronjunevaldoz.awake.ui.UiSemanticNode
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OpticalCenteringTest {

    @Test
    fun inspectOpticalCenteringValidatesMetrics() {
        val font = BitmapFont()
        val centeredNode = UiSemanticNode(
            id = "title",
            role = UiSemanticRole.Text,
            bounds = UiBounds(x = 0f, y = 0f, width = 100f, height = 12f),
            label = "CENTERED",
        )

        val report = inspectOpticalCentering(listOf(centeredNode), font, tolerancePx = 5f)
        assertTrue(report.isClean, "Optically centered node within tolerance should be clean")
    }

    @Test
    fun inspectOpticalCenteringFlagsDeviatingText() {
        val font = BitmapFont()
        val shiftedNode = UiSemanticNode(
            id = "off-center",
            role = UiSemanticRole.Text,
            bounds = UiBounds(x = 0f, y = 50f, width = 100f, height = 30f),
            label = "OFF",
        )

        val report = inspectOpticalCentering(listOf(shiftedNode), font, tolerancePx = 1f)
        // With bounds.y = 50f and container center = 56f, capHeight center offset gives deviation > 1px
        assertFalse(report.isClean, "Off-center text should generate a semantic issue")
    }
}
