// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.testing.ui.renderUiComponent
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.icon
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class IconWidgetsTest {

    @Test
    fun iconEmitsFilledPathsFromImageVector() {
        val frame = renderUiComponent(width = 100f, height = 100f) {
            primitive.context.createAbsolute(x = 10f, y = 12f).icon(
                imageVector = squareVector,
                modifier = Modifier.width(Dimension.Fixed(16f.px)).height(Dimension.Fixed(16f.px)),
                tint = Color(0.8f, 0.2f, 0.1f, 1f),
            )
        }

        val primitive = frame.primitives.single()
        val path = assertIs<UiDrawPrimitive.FilledPath>(primitive)
        assertEquals(Color(0.8f, 0.2f, 0.1f, 1f), path.color)
        assertEquals(
            listOf(
                UiPathCommand.MoveTo(10f, 12f),
                UiPathCommand.LineTo(26f, 12f),
                UiPathCommand.LineTo(26f, 28f),
                UiPathCommand.LineTo(10f, 28f),
                UiPathCommand.Close,
            ),
            path.path.commands,
        )
    }

    @Test
    fun iconUsesExplicitPathFillWithoutTintOverride() {
        val frame = renderUiComponent(width = 100f, height = 100f) {
            primitive.context.createAbsolute(x = 0f, y = 0f)
                .icon(multicolorVector, tint = Color(1f, 0f, 0f, 1f))
        }

        val fill = assertIs<UiDrawPrimitive.FilledPath>(frame.primitives.single())
        assertEquals(Color(0.2f, 0.7f, 0.3f, 1f), fill.color)
    }

    private companion object {
        val squareVector = uiImageVector(
            defaultWidth = 16f.dp,
            defaultHeight = 16f.dp,
            viewportWidth = 16f,
            viewportHeight = 16f,
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
            viewportHeight = 12f,
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
