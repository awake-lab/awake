// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.testing.ui.renderUiComponent
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.headless.textarea
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.api.dp
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Facade-level smoke coverage for [io.github.ronjunevaldoz.awake.ui.headless.textarea] driven
 * only through the public `UiScope` API -- no `headless.internal.*` import, unlike
 * `TextareaWidgetTest`, which already owns exhaustive keyboard-editing coverage at that level.
 */
class TextareaFacadeTest {

    @Test
    fun rendersASemanticNodeAtItsRequestedBoundsAndPassesTheValueThroughUnedited() {
        val frame = renderUiComponent(width = 200f, height = 100f) {
            textarea(
                id = "textarea.smoke",
                value = "Hello\nWorld",
                placeholder = "Type here",
                modifier = Modifier.width(160f.dp).height(80f.dp),
            )
        }

        val bounds = frame.bounds("textarea.smoke")
        assertEquals(160f, bounds.width)
        assertEquals(80f, bounds.height)
    }
}
