// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCheckbox
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnRadioButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnRadioGroup
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.offset
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.text
import io.github.ronjunevaldoz.awake.ui.headless.width
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Proof of the two gaps closed against real shadcn-compose:
 * tri-state `indeterminate` checkbox, and a `shadcnRadioGroup` content slot letting a caller
 * compose an icon/description row around a bare [shadcnRadioButton].
 */
class ShadcnCheckboxRadioSlotTest {

    @Test
    fun shadcnCheckboxIndeterminateDrawsDashInsteadOfCheckAccent() {
        val frame = renderShadcnComponent(width = 200f, height = 80f) {
            shadcnCheckbox(
                id = "select-all",
                checked = false,
                indeterminate = true,
                modifier = Modifier.width(20f.dp).height(20f.dp),
            )
        }

        val quads = frame.primitives.filterIsInstance<UiDrawPrimitive.RoundedQuad>()
        assertTrue(
            quads.any { it.w > it.h * 2f },
            "indeterminate checkbox should draw a wide dash mark, not a checkmark-shaped accent",
        )
    }

    @Test
    fun shadcnRadioGroupContentSlotComposesIconAndDescriptionAroundBareRadioButton() {
        var selected = "a"
        val frame = renderShadcnComponent(width = 320f, height = 160f) {
            column(modifier = Modifier.offset(20f.dp, 20f.dp).width(280f.dp)) {
                shadcnRadioGroup(id = "plan") {
                    row {
                        shadcnRadioButton(
                            id = "plan.a",
                            selected = selected == "a",
                            onClick = { selected = "a" },
                        )
                        column {
                            text("Pro plan")
                            text("Billed monthly, cancel anytime")
                        }
                    }
                }
            }
        }

        val semantics = frame.semantics
        assertNotNull(
            semantics.firstOrNull { it.label == "Pro plan" },
            "content slot's title row should render",
        )
        assertNotNull(
            semantics.firstOrNull { it.label == "Billed monthly, cancel anytime" },
            "content slot's description line should render",
        )
        assertNotNull(
            semantics.firstOrNull { it.id == "plan.a" },
            "bare radio button should render its own semantic node",
        )
    }

    @Test
    fun shadcnRadioGroupOptionsKeepEachLabelInlineAndVerticallyCenteredWithItsRadio() {
        val frame = renderShadcnComponent(width = 320f, height = 160f) {
            column(modifier = Modifier.offset(20f.dp, 20f.dp).width(280f.dp)) {
                shadcnRadioGroup(
                    id = "appearance",
                    options = listOf("System", "Light"),
                    selectedIndex = 0,
                )
            }
        }

        val semantics = frame.semantics
        val radio = semantics.first { it.id == "appearance.0" }
        val label = semantics.first { it.label == "System" }
        assertEquals(UiSemanticRole.Radio, radio.role)
        assertTrue(
            label.bounds.x >= radio.bounds.x + radio.bounds.width + 8f,
            "radio label must sit inline after its control, separated by the shadcn 8dp gap",
        )
        assertTrue(
            kotlin.math.abs(
                (label.bounds.y + label.bounds.height / 2f) -
                    (radio.bounds.y + radio.bounds.height / 2f),
            ) <= 1f,
            "radio label and control must share a vertical center",
        )
    }
}
