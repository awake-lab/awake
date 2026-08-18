// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.testing.ui.renderUiComponent
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.headless.progress
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.api.dp
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Facade-level smoke coverage for [io.github.ronjunevaldoz.awake.ui.headless.progress] driven
 * only through the public `UiScope` API -- no `headless.internal.*` import, unlike `ProgressTest`.
 */
class ProgressFacadeTest {

    @Test
    fun rendersASemanticNodeAtItsRequestedBounds() {
        val frame = renderUiComponent(width = 200f, height = 100f) {
            progress(id = "progress.smoke", value = 0.5f, modifier = Modifier.width(160f.dp).height(8f.dp))
        }

        val bounds = frame.bounds("progress.smoke")
        assertEquals(160f, bounds.width)
        assertEquals(8f, bounds.height)
    }
}
