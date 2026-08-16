// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.testing.ui.renderUiComponent
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnInput
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSelect
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.text
import io.github.ronjunevaldoz.awake.ui.headless.width
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Proof of the two audited shadcn-compose gaps closed here: [shadcnInput]'s leading/trailing
 * icon slots + visualTransformation, and [shadcnSelect]'s nullable `selectedIndex`/placeholder.
 */
class ShadcnInputIconSelectPlaceholderTest {

    @Test
    fun leadingIconDoesNotOverlapTypedText() {
        val semantics = renderUiComponent(width = 300f, height = 80f, theme = ShadcnTheme, font = BitmapFont()) {
            shadcnInput(
                id = "search",
                value = "hello",
                modifier = Modifier.width(240f.dp).height(40f.dp),
                leadingIcon = { text("S", modifier = Modifier.width(12f.dp)) },
            )
        }.semantics
        val icon = semantics.first { it.label == "S" }
        val value = semantics.first { it.id == "search.value" }
        val iconBounds = icon.contentBounds!!
        val valueBounds = value.contentBounds!!
        assertTrue(
            valueBounds.x >= iconBounds.x + iconBounds.width,
            "typed text must start after the leading icon slot, not underneath it",
        )
    }

    @Test
    fun visualTransformationMasksDisplayButNotStoredValue() {
        val real = "secret"
        var returned = ""
        val semantics = renderUiComponent(width = 300f, height = 80f, theme = ShadcnTheme, font = BitmapFont()) {
            returned = shadcnInput(
                id = "password",
                value = real,
                modifier = Modifier.width(240f.dp).height(40f.dp),
                visualTransformation = { "*".repeat(it.length) },
            )
        }.semantics

        // `value`/return value carry the real typed text untouched.
        assertEquals(real, returned)
        // What's actually drawn (the "$id.value" text node) is masked.
        assertEquals("*".repeat(real.length), semantics.first { it.id == "password.value" }.label)
        // The field's own top-level semantic node still reports the real value.
        assertEquals(real, semantics.first { it.id == "password" }.label)
    }

    @Test
    fun selectWithNullIndexShowsPlaceholder() {
        val semantics = renderUiComponent(width = 300f, height = 200f, theme = ShadcnTheme, font = BitmapFont()) {
            shadcnSelect(
                id = "fruit",
                options = listOf("Apple", "Banana"),
                selectedIndex = null,
                modifier = Modifier.width(200f.dp),
                placeholder = "Choose a fruit",
            )
        }.semantics
        assertEquals("Choose a fruit", semantics.first { it.id == "fruit.label" }.label)
    }
}
