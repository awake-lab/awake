// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.engine.application.GameServiceLookup
import io.github.ronjunevaldoz.awake.engine.application.GameUiRuntime
import io.github.ronjunevaldoz.awake.engine.application.frame
import io.github.ronjunevaldoz.awake.engine.application.gameUi
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.align
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.padding
import io.github.ronjunevaldoz.awake.ui.layout.toDimension
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import io.github.ronjunevaldoz.awake.ui.layout.*

private fun unusedGameServices() = object : GameServiceLookup {
    override fun <T : Any> service(type: kotlin.reflect.KClass<T>): T? = null
    override fun <T : Any> requireService(type: kotlin.reflect.KClass<T>): T = error("unused")
}

class CanvasResponsiveLayoutTest {

    @Test
    fun canvasExposesResponsiveWidthClassesAndAlignment() {
        val runtime = GameUiRuntime(services = unusedGameServices(), spec = gameUi { })
        runtime.render(0.016f, 360f, 240f)

        var widthClass: UiWidthSizeClass? = null
        var panelSlot: UiBounds? = null

        runtime.frame { constraints ->
            widthClass = constraints.widthSizeClass
            panelSlot = surface(
                id = "overlay-panel",
                modifier = (Modifier
                    .align(UiAlignment.BottomEnd)
                    .padding(start = 0f.dp, top = 0f.dp, end = 16f.dp, bottom = 12f.dp)).width(120f.toDimension()).height(
                        Dimension.WrapContent)) {
                text("Status")
            }
        }

        val primitives = runtime.uiContext.endFrame()
        assertEquals(UiWidthSizeClass.Compact, widthClass)
        assertEquals(UiBounds(224f, 196f, 120f, 32f), panelSlot)
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.Glyph>().isNotEmpty())
        assertTrue(primitives.any { it is UiDrawPrimitive.RoundedQuad })
    }

    @Test
    fun canvasSupportsStackedResponsiveColumns() {
        val runtime = GameUiRuntime(services = unusedGameServices(), spec = gameUi { })
        runtime.render(0.016f, 900f, 600f)

        var widthClass: UiWidthSizeClass? = null
        var columnSlot: UiBounds? = null

        runtime.frame { constraints ->
            widthClass = constraints.widthSizeClass
            columnSlot = column(
                modifier = (Modifier
                    .align(UiAlignment.TopStart)
                    .padding(20f.dp)).width(320f.toDimension()).height(Dimension.WrapContent)) {
                surface(id = "one", modifier = Modifier.width(Dimension.FillMax).height(Dimension.WrapContent)) {
                    text("One")
                }
                surface(id = "two", modifier = Modifier.width(Dimension.FillMax).height(Dimension.WrapContent)) {
                    text("Two")
                }
            }
        }

        val primitives = runtime.uiContext.endFrame()
        assertEquals(UiWidthSizeClass.Expanded, widthClass)
        assertEquals(UiBounds(20f, 20f, 320f, 72f), columnSlot)
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.Glyph>().isNotEmpty())
        assertTrue(primitives.any { it is UiDrawPrimitive.RoundedQuad })
    }

    @Test
    fun canvasUsesDensityIndependentWidthClasses() {
        val originalScale = UiDensity.scale
        UiDensity.scale = 2f
        try {
            val runtime = GameUiRuntime(services = unusedGameServices(), spec = gameUi { })
            runtime.render(0.016f, 900f, 600f)

            var widthClass: UiWidthSizeClass? = null
            var maxWidth: Float? = null
            var maxWidthPx: Float? = null

            runtime.frame { constraints ->
                widthClass = constraints.widthSizeClass
                maxWidth = constraints.maxWidth
                maxWidthPx = constraints.maxWidthPx
            }

            assertEquals(UiWidthSizeClass.Compact, widthClass)
            assertEquals(450f, maxWidth)
            assertEquals(900f, maxWidthPx)
        } finally {
            UiDensity.scale = originalScale
        }
    }
}
