// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.testing.ui

import io.github.ronjunevaldoz.awake.ui.headless.text
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AwakeUiStateMatrixTest {

    @Test
    fun uiTestSessionRunsTheRootProviderForEachFrame() {
        var providerCalls = 0
        uiTestSession(
            rootProvider = { content ->
                providerCalls += 1
                content()
            },
        ) {
            frame { text("First") }
            frame { text("Second") }
        }
        assertEquals(2, providerCalls)
    }

    @Test
    fun headlessMatrixRunsTheCallerProvidedRootProviderForEveryState() {
        var providerCalls = 0
        val samples = AwakeUiPreviewMetadata(
            id = "matrix",
            title = "Matrix",
            group = "Tests",
            summary = "Verifies root composition",
            width = 120,
            height = 48,
        ).headlessComponentStateMatrix(
            rootProvider = { content ->
                providerCalls += 1
                content()
            },
        ) {
            text("State")
        }

        assertEquals(4, providerCalls)
        assertEquals(4, samples.size)
        assertTrue(samples.all { sample -> sample.frame.semantics.isNotEmpty() })
    }
}
