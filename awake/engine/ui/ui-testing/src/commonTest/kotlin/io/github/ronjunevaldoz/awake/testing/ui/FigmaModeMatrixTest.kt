// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.testing.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FigmaModeMatrixTest {

    @Test
    fun defaultMatrixHas12Combinations() {
        val matrix = FigmaModeMatrix.defaultMatrix
        // 2 modes (Light, Dark) x 2 densities (Default, High) x 3 scales (1.0, 1.5, 2.0) = 12
        assertEquals(12, matrix.size)
    }

    @Test
    fun runMatrixAggregatesIssuesWithConfigPrefix() {
        val report = FigmaModeMatrix.runMatrix { config ->
            if (config.mode == FigmaMode.Dark) {
                UiSemanticReport(
                    listOf(
                        UiSemanticIssue(
                            kind = UiSemanticIssueKind.MismatchedToken,
                            nodeId = "test-node",
                            message = "contrast issue"
                        )
                    )
                )
            } else {
                UiSemanticReport(emptyList())
            }
        }

        // 6 Dark configurations out of 12
        assertEquals(6, report.issues.size)
        assertTrue(report.issues.all { it.message.contains("dark-") })
    }
}
