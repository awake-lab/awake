// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnFieldGroup
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnFieldSet
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnFieldTextField
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxSize
import io.github.ronjunevaldoz.awake.ui.headless.width
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * `FieldSet`, `FieldGroup`, and `Field` are block-level in shadcn -- `fieldVariants` opens with
 * `flex w-full`, `FieldGroup` repeats `w-full`, and `FieldSet` is a `<fieldset>`. Awake's
 * recipes were plain columns, so their children wrap-content: a text field inside one collapsed
 * to roughly its label width and truncated its own value.
 */
class ShadcnFieldContainerWidthTest {

    private val frameWidth = 400f

    @Test
    fun aFieldInsideAGroupSpansTheAvailableWidth() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(frameWidth, 300f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.headlessRoot().column(modifier = Modifier.fillMaxSize()) {
            shadcnFieldSet(id = "set") {
                shadcnFieldGroup(id = "group") {
                    shadcnFieldTextField(
                        id = "email",
                        label = "Billing email",
                        value = "",
                        placeholder = "jane@example.com",
                    )
                }
            }
        }

        val frame = ui.finishFrame()
        val field = frame.semantics.first { it.id == "email" }
        assertTrue(
            field.bounds.width > frameWidth / 2f,
            "field collapsed to ${field.bounds.width}px inside a ${frameWidth}px frame -- " +
                "the group is wrap-content again",
        )
    }

    @Test
    fun anExplicitCallerWidthStillWins() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(frameWidth, 300f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.headlessRoot().column(modifier = Modifier.fillMaxSize()) {
            shadcnFieldGroup(id = "narrow-group", modifier = Modifier.width(120f.dp)) {
                shadcnFieldTextField(id = "name", label = "Name", value = "")
            }
        }

        val frame = ui.finishFrame()
        val field = frame.semantics.first { it.id == "name" }
        assertTrue(
            field.bounds.width <= 120f,
            "caller width was overridden by the block-level default (${field.bounds.width}px)",
        )
    }
}
