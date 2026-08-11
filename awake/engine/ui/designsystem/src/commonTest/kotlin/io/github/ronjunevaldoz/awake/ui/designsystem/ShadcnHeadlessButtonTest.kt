// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.headless.createUiScope
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ShadcnHeadlessButtonTest {
    @Test
    fun headlessFacadeButtonResolvesTheInstalledShadcnTheme() {
        val context = UiContext()
        context.pushTheme(ShadcnTheme)
        context.beginFrame(200f, 120f, UiInputState())

        val clicked = context.createUiScope(UiBounds(0f, 0f, 200f, 120f)).shadcnButton(
            id = "confirm",
            label = "Confirm",
        )

        assertFalse(clicked)
        assertTrue(
            context.endFrame()
                .filterIsInstance<UiDrawPrimitive.RoundedQuad>()
                .any { it.color == ShadcnTheme.colors.primary },
            "the design-system variant must resolve through UiScope.themeValues",
        )
    }
}
