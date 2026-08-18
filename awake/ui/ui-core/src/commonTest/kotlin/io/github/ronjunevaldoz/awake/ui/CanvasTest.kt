// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.scope.resolveGlyphPx
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import io.github.ronjunevaldoz.awake.ui.context.UiFrameInput
import io.github.ronjunevaldoz.awake.ui.context.LocalFont

class CanvasTest {

    @Test
    fun canvasTranslatesPrimitiveCoordinatesIntoBounds() {
        val ui = UiContext()
        ui.beginFrame(UiFrameInput(viewportWidth = 200f, viewportHeight = 120f, input = testSnapshot()))
        val root = ui.createAbsolute(x = 20f, y = 30f)

        root.canvas(
            modifier = Modifier.width(Dimension.Fixed(80f.px)).height(Dimension.Fixed(60f.px)),
        ) {
            drawRect(x = 5f, y = 7f, width = 10f, height = 12f, color = Color.White)
            drawGradientRect(
                x = 0f,
                y = 0f,
                width = 20f,
                height = 16f,
                gradient = UiLinearGradient.horizontal(Color.White, Color.Black),
            )
        }

        val primitives = ui.finishFrame().primitives
        val rect = assertIs<UiDrawPrimitive.Quad>(primitives[0])
        assertEquals(25f, rect.x)
        assertEquals(37f, rect.y)
        assertEquals(10f, rect.w)
        assertEquals(12f, rect.h)

        val gradient = assertIs<UiDrawPrimitive.GradientQuad>(primitives[1])
        assertEquals(20f, gradient.x)
        assertEquals(30f, gradient.y)
        assertEquals(20f, gradient.w)
        assertEquals(16f, gradient.h)
    }

    @Test
    fun canvasTextAndTextureUseCanvasLocalCoordinates() {
        val ui = UiContext()
        ui.beginFrame(UiFrameInput(viewportWidth = 240f, viewportHeight = 160f, input = testSnapshot()))
        val material = Any()
        val root = ui.createAbsolute(x = 40f, y = 50f)
        val glyphPx = root.resolveGlyphPx()
        val glyphRect = ui.current(LocalFont).uvFor('A')!!

        root.canvas(
            modifier = Modifier.width(Dimension.Fixed(120f.px)).height(Dimension.Fixed(80f.px)),
        ) {
            drawText(text = "A", x = 8f, y = 10f, color = Color.White)
            drawImage(x = 12f, y = 18f, width = 24f, height = 20f, material = material)
        }

        val primitives = ui.finishFrame().primitives
        val glyph = primitives.filterIsInstance<UiDrawPrimitive.Glyph>().single()
        assertEquals(48f + glyphRect.offsetXEm * glyphPx, glyph.x)
        assertEquals(60f + glyphRect.offsetYEm * glyphPx, glyph.y)
        assertEquals(Color.White, glyph.color)

        val texture = primitives.filterIsInstance<UiDrawPrimitive.Texture>().single()
        assertEquals(52f, texture.x)
        assertEquals(68f, texture.y)
        assertEquals(24f, texture.w)
        assertEquals(20f, texture.h)
        assertSame(material, texture.material)
    }

    @Test
    fun canvasPathAndClipStayRelativeToCanvasBounds() {
        val ui = UiContext()
        ui.beginFrame(UiFrameInput(viewportWidth = 240f, viewportHeight = 160f, input = testSnapshot()))

        ui.createAbsolute(x = 10f, y = 15f).canvas(
            modifier = Modifier.width(Dimension.Fixed(100f.px)).height(Dimension.Fixed(80f.px)),
        ) {
            fillPath(
                path = uiPath {
                    moveTo(1f, 2f)
                    lineTo(21f, 2f)
                    lineTo(21f, 18f)
                    close()
                },
                color = Color.White,
            )
            clipShape(
                shape = UiShapeSpec.CutCorner(6f.dp),
                x = 4f,
                y = 6f,
                width = 30f,
                height = 24f,
            ) {
                drawRect(0f, 0f, 10f, 10f, Color.Black)
            }
        }

        val primitives = ui.finishFrame().primitives
        val path = primitives.filterIsInstance<UiDrawPrimitive.FilledPath>().first()
        val pathBounds = path.path.bounds()
        assertEquals(11f, pathBounds.x)
        assertEquals(17f, pathBounds.y)

        val clipPush = primitives.filterIsInstance<UiDrawPrimitive.ClipPathPush>().single()
        assertEquals(UiBounds(14f, 21f, 30f, 24f), clipPush.boundsRect)
    }

    @Test
    fun nestedCanvasOffsetsChildBoundsFromParentCanvas() {
        val ui = UiContext()
        ui.beginFrame(UiFrameInput(viewportWidth = 300f, viewportHeight = 200f, input = testSnapshot()))

        ui.createAbsolute(x = 50f, y = 60f).canvas(
            modifier = Modifier.width(Dimension.Fixed(120f.px)).height(Dimension.Fixed(90f.px)),
        ) {
            nested(x = 10f, y = 12f, width = 40f, height = 30f) {
                drawLine(0f, 0f, 10f, 8f, color = Color.White)
                drawCircle(4f, 6f, diameter = 12f, color = Color.Black)
            }
        }

        val primitives = ui.finishFrame().primitives
        val stroke = primitives.filterIsInstance<UiDrawPrimitive.StrokedPath>().single()
        val strokeBounds = stroke.path.bounds()
        assertEquals(60f, strokeBounds.x)
        assertEquals(72f, strokeBounds.y)

        val circle = primitives.filterIsInstance<UiDrawPrimitive.RoundedQuad>().single()
        assertEquals(64f, circle.x, 0.001f)
        assertEquals(78f, circle.y, 0.001f)
        assertEquals(12f, circle.w, 0.001f)
        assertEquals(12f, circle.h, 0.001f)
    }
}
