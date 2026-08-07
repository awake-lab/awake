// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.controls.shadcnInput
import io.github.ronjunevaldoz.awake.ui.designsystem.components.controls.shadcnSelect
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.input.text.text
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
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
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(300f, 80f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.createAbsolute(x = 20f, y = 20f)
            .shadcnInput(
                id = "search",
                value = "hello",
                modifier = Modifier.width(240f.dp).height(40f.dp),
                leadingIcon = { text("S", modifier = Modifier.width(12f.dp)) },
            )

        val semantics = ui.finishFrame().semantics
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
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(300f, 80f, testSnapshot(x = -100f, y = -100f, down = false))

        val real = "secret"
        val returned = ui.createAbsolute(x = 20f, y = 20f)
            .shadcnInput(
                id = "password",
                value = real,
                modifier = Modifier.width(240f.dp).height(40f.dp),
                visualTransformation = { "*".repeat(it.length) },
            )

        // `value`/return value carry the real typed text untouched.
        assertEquals(real, returned)

        val semantics = ui.finishFrame().semantics
        // What's actually drawn (the "$id.value" text node) is masked.
        assertEquals("*".repeat(real.length), semantics.first { it.id == "password.value" }.label)
        // The field's own top-level semantic node still reports the real value.
        assertEquals(real, semantics.first { it.id == "password" }.label)
    }

    @Test
    fun selectWithNullIndexShowsPlaceholder() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(300f, 200f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.createAbsolute(x = 20f, y = 20f)
            .shadcnSelect(
                id = "fruit",
                options = listOf("Apple", "Banana"),
                selectedIndex = null,
                modifier = Modifier.width(200f.dp),
                placeholder = "Choose a fruit",
            )

        val semantics = ui.finishFrame().semantics
        assertEquals("Choose a fruit", semantics.first { it.id == "fruit.label" }.label)
    }
}
