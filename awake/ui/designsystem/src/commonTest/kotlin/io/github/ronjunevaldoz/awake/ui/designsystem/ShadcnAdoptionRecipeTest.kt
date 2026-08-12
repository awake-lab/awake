// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.controls.shadcnInputGroup
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldSeparator
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnEmpty
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnKbd
import io.github.ronjunevaldoz.awake.ui.headless.createUiScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ShadcnAdoptionRecipeTest {

    @Test
    fun testShadcnKbdRendersBounds() {
        val context = UiContext()
        val scope = context.createUiScope(UiBounds(0f, 0f, 300f, 200f))
        val kbdBounds = scope.shadcnKbd("⌘K")
        assertNotNull(kbdBounds)
    }

    @Test
    fun testShadcnEmptyRendersBounds() {
        val context = UiContext()
        val scope = context.createUiScope(UiBounds(0f, 0f, 400f, 300f))
        val emptyBounds = scope.shadcnEmpty(
            title = "No items found",
            description = "Try adjusting your search criteria.",
        )
        assertNotNull(emptyBounds)
    }

    @Test
    fun testShadcnFieldSeparatorWithLabel() {
        val context = UiContext()
        val scope = context.createUiScope(UiBounds(0f, 0f, 400f, 100f))
        val sepBounds = scope.shadcnFieldSeparator(label = "OR")
        assertNotNull(sepBounds)
    }

    @Test
    fun testShadcnInputGroup() {
        val context = UiContext()
        val scope = context.createUiScope(UiBounds(0f, 0f, 400f, 100f))
        val value = scope.shadcnInputGroup(
            id = "input.email",
            value = "test@example.com",
            prefixText = "https://",
            suffixText = ".com",
        )
        assertEquals("test@example.com", value)
    }
}
