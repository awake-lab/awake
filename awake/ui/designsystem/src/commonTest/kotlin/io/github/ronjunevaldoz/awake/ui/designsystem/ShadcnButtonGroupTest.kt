// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnButtonGroupOrientation
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButtonGroup
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButtonGroupSeparator
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxSize
import io.github.ronjunevaldoz.awake.ui.modifier.width
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShadcnButtonGroupTest {

    private val frameWidth = 600f

    @Test
    fun horizontalButtonGroupWrapsContentWidthInAParentColumn() {
        val frame = renderShadcnComponent(width = frameWidth, height = 300f, font = BitmapFont()) {
            column(modifier = Modifier.fillMaxSize()) {
                shadcnButtonGroup(id = "button-group-horiz") {
                    shadcnButton(id = "btn-1", label = "Save")
                    shadcnButtonGroupSeparator("sep-1")
                    shadcnButton(id = "btn-2", label = "Cancel")
                }
            }
        }
        val group = frame.semantics.first { it.id == "button-group-horiz" }
        assertTrue(
            group.bounds.width < frameWidth / 2f,
            "horizontal button group expanded to ${group.bounds.width}px inside a ${frameWidth}px frame -- expected w-fit wrap-content",
        )
    }

    @Test
    fun verticalButtonGroupWrapsContentWidthAndButtonsFillMaxWidth() {
        val frame = renderShadcnComponent(width = frameWidth, height = 300f, font = BitmapFont()) {
            column(modifier = Modifier.fillMaxSize()) {
                shadcnButtonGroup(
                    id = "button-group-vert",
                    orientation = ShadcnButtonGroupOrientation.Vertical,
                ) {
                    shadcnButton(id = "btn-top", label = "Top")
                    shadcnButtonGroupSeparator("sep-vert-1")
                    shadcnButton(id = "btn-center", label = "Longer Center Label")
                    shadcnButtonGroupSeparator("sep-vert-2")
                    shadcnButton(id = "btn-bottom", label = "Bottom")
                }
            }
        }
        val group = frame.semantics.first { it.id == "button-group-vert" }
        val topBtn = frame.semantics.first { it.id == "btn-top" }
        val centerBtn = frame.semantics.first { it.id == "btn-center" }
        val bottomBtn = frame.semantics.first { it.id == "btn-bottom" }

        assertTrue(
            group.bounds.width < frameWidth / 2f,
            "vertical button group expanded to ${group.bounds.width}px inside a ${frameWidth}px frame -- expected w-fit wrap-content",
        )
        assertEquals(
            centerBtn.bounds.width,
            topBtn.bounds.width,
            "top button did not fill max width of vertical group -- expected ${centerBtn.bounds.width}px but got ${topBtn.bounds.width}px",
        )
        assertEquals(
            centerBtn.bounds.width,
            bottomBtn.bounds.width,
            "bottom button did not fill max width of vertical group -- expected ${centerBtn.bounds.width}px but got ${bottomBtn.bounds.width}px",
        )
    }

    @Test
    fun verticalButtonGroupEnforcesMinimumWidthBaseline() {
        val frame = renderShadcnComponent(width = frameWidth, height = 300f, font = BitmapFont()) {
            column(modifier = Modifier.fillMaxSize()) {
                shadcnButtonGroup(
                    id = "button-group-icon-vert",
                    orientation = ShadcnButtonGroupOrientation.Vertical,
                ) {
                    shadcnButton(id = "btn-zoom-in", label = "+")
                    shadcnButtonGroupSeparator("sep-zoom")
                    shadcnButton(id = "btn-zoom-out", label = "-")
                }
            }
        }
        val group = frame.semantics.first { it.id == "button-group-icon-vert" }
        assertTrue(
            group.bounds.width >= 36f,
            "vertical icon button group collapsed below 36px (${group.bounds.width}px) -- expected minWidth = 36dp",
        )
    }

    @Test
    fun explicitCallerWidthOverridesDefaults() {
        val frame = renderShadcnComponent(width = frameWidth, height = 300f, font = BitmapFont()) {
            column(modifier = Modifier.fillMaxSize()) {
                shadcnButtonGroup(
                    id = "button-group-fixed",
                    modifier = Modifier.width(120f.dp),
                ) {
                    shadcnButton(id = "btn-fixed-1", label = "Option A")
                    shadcnButtonGroupSeparator("sep-fixed")
                    shadcnButton(id = "btn-fixed-2", label = "Option B")
                }
            }
        }
        val group = frame.semantics.first { it.id == "button-group-fixed" }
        assertEquals(
            120f,
            group.bounds.width,
            "caller explicit width was not honored (${group.bounds.width}px)",
        )
    }
}
