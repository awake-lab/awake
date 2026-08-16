// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.createUiScope
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ShadcnHeadlessButtonTest {
    @Test
    fun headlessFacadeButtonResolvesTheInstalledShadcnTheme() {
        var clicked = false
        val frame = renderShadcnComponent(width = 200f, height = 120f) {
            clicked = shadcnButton(id = "confirm", label = "Confirm")
        }

        assertFalse(clicked)
        assertTrue(
            frame.primitives
                .filterIsInstance<UiDrawPrimitive.RoundedQuad>()
                .any { it.color == ShadcnTheme.colors.primary },
            "the design-system variant must resolve through UiScope.themeValues",
        )
    }

    @Test
    fun bareButtonUsesNaturalWidthUntilFillMaxWidthIsRequested() {
        val frame = renderShadcnComponent(width = 200f, height = 120f) {
            shadcnButton(id = "natural", label = "Confirm")
            primitive.context.createUiScope(UiBounds(0f, 50f, 200f, 120f)).shadcnButton(
                id = "full",
                label = "Confirm",
                modifier = Modifier.fillMaxWidth(),
            )
        }

        val semantics = frame.semantics
        val button = semantics.first { it.id == "natural" }
        val fullButton = semantics.first { it.id == "full" }
        assertTrue(button.bounds.width < 200f, "bare buttons should not claim the entire parent width")
        assertTrue(fullButton.bounds.width >= 199f, "fillMaxWidth() must remain an explicit opt-in")
    }
}
