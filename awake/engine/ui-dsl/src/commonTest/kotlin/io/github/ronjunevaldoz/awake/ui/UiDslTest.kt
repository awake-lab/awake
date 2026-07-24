// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.engine.application.GameServiceLookup
import io.github.ronjunevaldoz.awake.engine.application.GameUiRuntime
import io.github.ronjunevaldoz.awake.engine.application.canvas
import io.github.ronjunevaldoz.awake.engine.application.gameUi
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ext.column
import io.github.ronjunevaldoz.awake.ui.layouts.ext.row
import io.github.ronjunevaldoz.awake.ui.layouts.ext.spacer
import io.github.ronjunevaldoz.awake.ui.layouts.ext.surface
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.align
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.offset
import io.github.ronjunevaldoz.awake.ui.modifier.padding
import io.github.ronjunevaldoz.awake.ui.layout.toDimension
import io.github.ronjunevaldoz.awake.ui.modifier.verticalScroll
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.unstyled.UiButtonResult
import io.github.ronjunevaldoz.awake.ui.unstyled.buttonSlot
import io.github.ronjunevaldoz.awake.ui.unstyled.input.dropdown
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.unstyled.input.toggle.toggle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

/** Builds a one-off [UiInputState] for a test frame -- [Input] is a per-session instance
 * now (no longer a global object), so tests construct their own throwaway one instead of
 * writing into shared static state. */
private fun testSnapshot(
    x: Float = -100f,
    y: Float = -100f,
    down: Boolean = false,
    scrollDeltaY: Float = 0f
): UiInputState {
    val input = Input()
    input.setPointer(down, x, y)
    input.scrollDeltaY = scrollDeltaY
    return input.updateSnapshot().toUiInputState()
}

/** Too generic **/
class UiDslTest {

    @Test
    fun dslCanComposeInspectorPanelFromPublicFacade() {
        val ui = UiContext()
        ui.beginFrame(320f, 240f, testSnapshot())

        var panelSlot: UiSlot? = null
        var controlSlot: UiSlot? = null

        ui.pushFont(BitmapFont())
        ui.column(modifier = Modifier.offset(20f.dp, 20f.dp).width(200f.dp).height(220f.dp)) {
            surface(id = "inspector", modifier = Modifier.height(120f.toDimension())) { slot ->
                panelSlot = slot
                text("Inspector")
                row( modifier = Modifier.height(28f.dp)) { propertySlot ->
                    controlSlot = propertySlot
                    dropdown(
                        id = "mode",
                        options = listOf("Mesh", "Light"),
                        selectedIndex = 0,
                        modifier = Modifier.width(propertySlot.width.px)
                            .height(propertySlot.height.px)
                    )
                }
            }
        }

        val primitives = ui.endFrame()
        val resolvedPanelSlot = assertNotNull(panelSlot)
        assertNotNull(controlSlot)
        assertEquals(20f, resolvedPanelSlot.x)
        assertEquals(20f, resolvedPanelSlot.y)
        assertTrue(
            primitives.filterIsInstance<UiDrawPrimitive.Glyph>().isNotEmpty(),
            "dsl content should render text through the shared widget pipeline"
        )
    }

    @Test
    fun dslRowSpacerPreservesHorizontalLayoutProgression() {
        val ui = UiContext()
        ui.beginFrame(240f, 100f, testSnapshot())

        var first: UiButtonResult? = null
        var second: UiButtonResult? = null

        ui.pushFont(BitmapFont())
        ui.column(
            modifier = Modifier.offset(10f.dp, 20f.dp).width(220f.dp).height(80f.dp),
            verticalArrangement = Arrangement.spacedBy(0f.dp)
        ) {
            row( horizontalArrangement = Arrangement.spacedBy(4f.dp), modifier = Modifier.height(30f.dp)) {
                first = buttonSlot(
                    id = "one",
                    label = "One",
                    modifier = Modifier.width(60f.px).height(30f.px)
                )
                spacer(Modifier.width(12f.dp))
                second = buttonSlot(
                    id = "two",
                    label = "Two",
                    modifier = Modifier.width(60f.px).height(30f.px)
                )
            }
        }

        val firstSlot = assertNotNull(first).slot
        val secondSlot = assertNotNull(second).slot
        assertEquals(UiSlot(10f, 20f, 60f, 30f), firstSlot)
        assertEquals(UiSlot(90f, 20f, 60f, 30f), secondSlot)
    }

    @Test
    fun dslToggleUsesSharedWidgetBehavior() {
        val ui = UiContext()
        ui.beginFrame(180f, 80f, testSnapshot())

        var checked = true
        ui.pushFont(BitmapFont())
        ui.column(modifier = Modifier.offset(20f.dp, 20f.dp).width(140f.dp).height(80f.dp)) {
            checked = toggle(
                id = "grid",
                checked = checked,
                label = "GRID",
                modifier = Modifier.fillMaxWidth().height(32f.px)
            )
        }

        val primitives = ui.endFrame()
        assertTrue(
            primitives.any { it is UiDrawPrimitive.Quad || it is UiDrawPrimitive.RoundedQuad },
            "toggle should emit its background shape"
        )
        assertTrue(
            primitives.filterIsInstance<UiDrawPrimitive.Glyph>().isNotEmpty(),
            "toggle should render its label through the shared glyph pipeline"
        )
        assertFalse(!checked, "toggle should remain unchanged without interaction")
    }

    @Test
    fun propertyRowSupportsSlotBasedLabels() {
        val ui = UiContext()
        ui.beginFrame(320f, 160f, testSnapshot())

        var controlSlot: UiSlot? = null

        ui.column(modifier = Modifier.offset(20f.dp, 20f.dp).width(220f.dp).height(160f.dp)) {
            surface(id = "slot-panel", modifier = Modifier.height(100f.toDimension())) {
                row( modifier = Modifier.height(28f.dp)) { slot ->
                    controlSlot = slot
                    dropdown(
                        id = "camera-mode",
                        options = listOf("Orbit", "Fly"),
                        selectedIndex = 0,
                        modifier = Modifier.width(slot.width.px).height(slot.height.px)
                    )
                }
            }
        }

        assertNotNull(controlSlot)
        val primitives = ui.endFrame()
        assertTrue(
            primitives.filterIsInstance<UiDrawPrimitive.Glyph>().isNotEmpty(),
            "slot-based property labels should still render through the shared text pipeline"
        )
    }

    @Test
    fun canvasExposesResponsiveWidthClassesAndAlignment() {
        val runtime = GameUiRuntime(
            services = object : GameServiceLookup {
                override fun <T : Any> service(type: kotlin.reflect.KClass<T>): T? = null
                override fun <T : Any> requireService(type: kotlin.reflect.KClass<T>): T =
                    error("unused")
            },
            spec = gameUi { }
        )
        // Set viewport dimensions on runtime
        runtime.render(0.016f, 360f, 240f)

        var widthClass: UiWidthSizeClass? = null
        var panelSlot: UiSlot? = null

        runtime.canvas { constraints ->
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
        assertEquals(UiSlot(224f, 196f, 120f, 32f), panelSlot)
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.Glyph>().isNotEmpty())
        assertTrue(primitives.any { it is UiDrawPrimitive.RoundedQuad })
    }

    @Test
    fun canvasSupportsStackedResponsiveColumns() {
        val runtime = GameUiRuntime(
            services = object : GameServiceLookup {
                override fun <T : Any> service(type: kotlin.reflect.KClass<T>): T? = null
                override fun <T : Any> requireService(type: kotlin.reflect.KClass<T>): T =
                    error("unused")
            },
            spec = gameUi { }
        )
        runtime.render(0.016f, 900f, 600f)

        var widthClass: UiWidthSizeClass? = null
        var columnSlot: UiSlot? = null

        runtime.canvas { constraints ->
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
        assertEquals(UiSlot(20f, 20f, 320f, 72f), columnSlot)
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.Glyph>().isNotEmpty())
        assertTrue(primitives.any { it is UiDrawPrimitive.RoundedQuad })
    }

    @Test
    fun canvasUsesDensityIndependentWidthClasses() {
        val originalScale = UiDensity.scale
        UiDensity.scale = 2f
        try {
            val runtime = GameUiRuntime(
                services = object : GameServiceLookup {
                    override fun <T : Any> service(type: kotlin.reflect.KClass<T>): T? = null
                    override fun <T : Any> requireService(type: kotlin.reflect.KClass<T>): T =
                        error("unused")
                },
                spec = gameUi { }
            )
            runtime.render(0.016f, 900f, 600f)

            var widthClass: UiWidthSizeClass? = null
            var maxWidth: Float? = null
            var maxWidthPx: Float? = null

            runtime.canvas { constraints ->
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

    @Test
    fun dslScrollPanelDelegatesToSharedWidgetPrimitive() {
        val ui = UiContext()
        val scrollState = UiScrollState()

        ui.beginFrame(220f, 200f, testSnapshot(x = 24f, y = 24f, scrollDeltaY = -1f))
        ui.column(modifier = Modifier.offset(12f.dp, 12f.dp).width(160f.dp).height(176f.dp)) {
            scrollPanel(
                id = "dsl-scroll",
                modifier = Modifier
                    .width(Dimension.FillMax)
                    .height(80f.toDimension())
                    .verticalScroll(
                        state = scrollState,
                        config = UiScrollConfig(scrollSpeed = 20f)
                    )
            ) {
                repeat(8) { index ->
                    text("Row $index")
                }
            }
        }
        ui.endFrame()

        assertEquals(20f, scrollState.offsetY)
        assertTrue(scrollState.canScroll)
    }
}
