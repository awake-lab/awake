// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.testing.ui.renderUiComponent
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.textField
import io.github.ronjunevaldoz.awake.ui.headless.width
import io.github.ronjunevaldoz.awake.ui.api.dp
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Facade-level smoke coverage for [io.github.ronjunevaldoz.awake.ui.headless.textField] driven
 * only through the public `UiScope` API -- no `headless.internal.*` import, unlike
 * `TextFieldWidgetTest`, which already owns exhaustive keyboard-editing coverage at that level.
 */
class TextFieldFacadeTest {

    @Test
    fun rendersASemanticNodeAtItsRequestedBoundsAndPassesTheValueThroughUnedited() {
        val frame = renderUiComponent(width = 200f, height = 100f) {
            textField(
                id = "field.smoke",
                value = "Hello",
                placeholder = "Type here",
                modifier = Modifier.width(160f.dp).height(36f.dp),
            )
        }

        val bounds = frame.bounds("field.smoke")
        assertEquals(160f, bounds.width)
        assertEquals(36f, bounds.height)
    }
}
