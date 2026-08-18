// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.testing.ui.renderUiComponent
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.headless.surface
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.style.Style
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Facade-level smoke coverage for [io.github.ronjunevaldoz.awake.ui.headless.surface] driven only
 * through the public `UiScope` API. No existing test file owned headless's own `surface` at any
 * level -- `PanelTest` exercises `ui-core`'s lower-level `layouts.surface` instead.
 */
class SurfaceFacadeTest {

    @Test
    fun rendersAtItsRequestedBoundsAndPaintsItsBackground() {
        var contentInvoked = false
        val frame = renderUiComponent(width = 200f, height = 100f) {
            surface(
                id = "surface.smoke",
                modifier = Modifier.width(120f.dp).height(60f.dp),
                style = Style { background(Color.Black) },
            ) { contentInvoked = true }
        }

        val bounds = frame.bounds("surface.smoke")
        assertEquals(120f, bounds.width)
        assertEquals(60f, bounds.height)
        assertTrue(contentInvoked, "surface must invoke its content slot")
        assertTrue(
            frame.primitives.filterIsInstance<UiDrawPrimitive.RoundedQuad>().any { it.color == Color.Black } ||
                frame.primitives.filterIsInstance<UiDrawPrimitive.Quad>().any { it.color == Color.Black },
            "surface must paint the resolved background",
        )
    }
}
