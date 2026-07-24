// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.offset
import io.github.ronjunevaldoz.awake.ui.unstyled.components.icon
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

class IconWidgetsTest {

    @Test
    fun iconEmitsFilledPathsFromImageVector() {
        val ui = UiContext()
        ui.beginFrame(100f, 100f, testSnapshot())

        ui.createAbsolute(modifier = Modifier.offset(10f.dp, 12f.dp)).icon(
            imageVector = squareVector,
            width = Dimension.Fixed(16f.px),
            height = Dimension.Fixed(16f.px),
            tint = Color(0.8f, 0.2f, 0.1f, 1f)
        )

        val primitive = ui.endFrame().single()
        val path = assertIs<UiDrawPrimitive.FilledPath>(primitive)
        assertEquals(Color(0.8f, 0.2f, 0.1f, 1f), path.color)
        assertEquals(
            listOf(
                UiPathCommand.MoveTo(10f, 12f),
                UiPathCommand.LineTo(26f, 12f),
                UiPathCommand.LineTo(26f, 28f),
                UiPathCommand.LineTo(10f, 28f),
                UiPathCommand.Close
            ),
            path.path.commands
        )
    }

    @Test
    fun iconUsesExplicitPathFillWithoutTintOverride() {
        val ui = UiContext()
        ui.beginFrame(100f, 100f, testSnapshot())

        ui.createAbsolute(modifier = Modifier.offset(0f.dp, 0f.dp)).icon(multicolorVector, tint = Color(1f, 0f, 0f, 1f))

        val fill = assertIs<UiDrawPrimitive.FilledPath>(ui.endFrame().single())
        assertEquals(Color(0.2f, 0.7f, 0.3f, 1f), fill.color)
    }

    private companion object {
        val squareVector = uiImageVector(
            defaultWidth = 16f.dp,
            defaultHeight = 16f.dp,
            viewportWidth = 16f,
            viewportHeight = 16f
        ) {
            path {
                moveTo(0f, 0f)
                lineTo(16f, 0f)
                lineTo(16f, 16f)
                lineTo(0f, 16f)
                close()
            }
        }

        val multicolorVector = uiImageVector(
            defaultWidth = 12f.dp,
            defaultHeight = 12f.dp,
            viewportWidth = 12f,
            viewportHeight = 12f
        ) {
            path(fill = Color(0.2f, 0.7f, 0.3f, 1f)) {
                moveTo(0f, 0f)
                lineTo(12f, 0f)
                lineTo(12f, 12f)
                close()
            }
        }
    }
}
