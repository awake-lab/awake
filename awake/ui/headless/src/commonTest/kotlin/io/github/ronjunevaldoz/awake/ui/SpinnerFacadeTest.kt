// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.testing.ui.renderUiComponent
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.spinner
import io.github.ronjunevaldoz.awake.ui.headless.width
import io.github.ronjunevaldoz.awake.ui.api.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Facade-level smoke coverage for [io.github.ronjunevaldoz.awake.ui.headless.spinner] driven only
 * through the public `UiScope` API -- no `headless.internal.*` import, unlike `SpinnerTest`.
 */
class SpinnerFacadeTest {

    @Test
    fun rendersASemanticNodeAndPaintsSomething() {
        val frame = renderUiComponent(width = 100f, height = 100f) {
            spinner(id = "spinner.smoke", modifier = Modifier.width(24f.dp).height(24f.dp))
        }

        val bounds = frame.bounds("spinner.smoke")
        assertEquals(24f, bounds.width)
        assertEquals(24f, bounds.height)
        assertTrue(frame.primitives.isNotEmpty(), "spinner must paint its orbiting dots")
    }
}
