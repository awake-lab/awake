// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnEmpty
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnFieldSeparator
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnInputGroup
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnKbd
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ShadcnAdoptionRecipeTest {

    @Test
    fun testShadcnKbdRendersBounds() {
        var kbdBounds: UiBounds? = null
        renderShadcnComponent(width = 300f, height = 200f) { _ ->
            kbdBounds = shadcnKbd(id = "kbd", label = "⌘K")
        }
        assertNotNull(kbdBounds)
    }

    @Test
    fun testShadcnEmptyRendersBounds() {
        var emptyBounds: UiBounds? = null
        renderShadcnComponent(width = 400f, height = 300f) { _ ->
            emptyBounds = shadcnEmpty(
                title = "No items found",
                description = "Try adjusting your search criteria.",
            )
        }
        assertNotNull(emptyBounds)
    }

    @Test
    fun testShadcnFieldSeparatorWithLabel() {
        var sepBounds: UiBounds? = null
        renderShadcnComponent(width = 400f, height = 100f) { _ ->
            sepBounds = shadcnFieldSeparator(label = "OR")
        }
        assertNotNull(sepBounds)
    }

    @Test
    fun testShadcnInputGroup() {
        var value = ""
        renderShadcnComponent(width = 400f, height = 100f) { _ ->
            value = shadcnInputGroup(
                id = "input.email",
                value = "test@example.com",
                prefixText = "https://",
                suffixText = ".com",
            )
        }
        assertEquals("test@example.com", value)
    }
}
