// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.input.InputSnapshot
import io.github.ronjunevaldoz.awake.engine.application.GameServiceLookup
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Builds a one-off [InputSnapshot] for a test frame -- [Input] is a per-session instance
 * now (no longer a global object), so tests construct their own throwaway one instead of
 * writing into shared static state. */
private fun testSnapshot(x: Float = -100f, y: Float = -100f, down: Boolean = false, scrollDeltaY: Float = 0f): InputSnapshot {
    val input = Input()
    input.setPointer(down, x, y)
    input.scrollDeltaY = scrollDeltaY
    return input.updateSnapshot()
}

class UiDslTest {

    @Test
    fun propertyLabelWidthCanExpandForLongLabelsWhenRowHasSpace() {
        val width = resolvePropertyLabelWidthPx(
            rowWidthPx = 320f,
            label = "Exposure Compensation",
            requestedWidthPx = 64f,
            glyphPx = 8f
        )

        assertEquals(144f, width, "wide rows should let long property labels claim more than the 64px default")
    }

    @Test
    fun propertyLabelWidthShrinksBeforeStarvingTheControlArea() {
        val width = resolvePropertyLabelWidthPx(
            rowWidthPx = 150f,
            label = "Exposure Compensation",
            requestedWidthPx = 64f,
            glyphPx = 8f
        )

        assertEquals(46f, width, "narrow rows should trim the label column so the control keeps usable width")
    }

    @Test
    fun dslCanComposeInspectorPanelFromPublicFacade() {
        val ui = UiContext()
        ui.beginFrame(320f, 240f, testSnapshot())

        var panelSlot: UiSlot? = null
        var controlSlot: UiSlot? = null

        ui.ui(x = 20f, y = 20f, width = 200f, font = BitmapFont()) {
            panel(id = "inspector", height = 120f.toDimension()) { slot ->
                panelSlot = slot
                text("Inspector")
                propertyRow(label = "Mode", height = 28f.dp) { propertySlot ->
                    controlSlot = propertySlot
                    dropdown(
                        id = "mode",
                        options = listOf("Mesh", "Light"),
                        selectedIndex = 0,
                        modifier = UiModifier().width(propertySlot.width.px).height(propertySlot.height.px)
                    )
                }
                propertyCheckbox(
                    id = "visible",
                    checked = true,
                    label = "Visible",
                    height = 28f.dp
                )
            }
        }

        val primitives = ui.endFrame()
        val resolvedPanelSlot = assertNotNull(panelSlot)
        val resolvedControlSlot = assertNotNull(controlSlot)
        assertEquals(20f, resolvedPanelSlot.x)
        assertEquals(20f, resolvedPanelSlot.y)
        assertEquals(68f, resolvedControlSlot.x, "compact property rows should shrink the label column before starving the control area")
        assertEquals(40f, resolvedControlSlot.x - (resolvedPanelSlot.x + UiSpacing.sm.toPx()))
        assertIs<UiDrawPrimitive.RoundedQuad>(primitives.first(), "panel background should use the themed rounded shape")
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.Glyph>().isNotEmpty(), "dsl content should render text through the shared widget pipeline")
    }

    @Test
    fun dslRowSpacerPreservesHorizontalLayoutProgression() {
        val ui = UiContext()
        ui.beginFrame(240f, 100f, testSnapshot())

        var first: UiButtonResult? = null
        var second: UiButtonResult? = null

        ui.ui(x = 10f, y = 20f, width = 220f, font = BitmapFont(), gap = 0f) {
            row(height = 30f.dp, gap = 4f) {
                first = buttonSlot(id = "one", label = "One", modifier = UiModifier().width(60f.px).height(30f.px))
                spacer(UiModifier().width(12f.dp))
                second = buttonSlot(id = "two", label = "Two", modifier = UiModifier().width(60f.px).height(30f.px))
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
        ui.ui(x = 20f, y = 20f, width = 140f, font = BitmapFont()) {
            checked = toggle(
                id = "grid",
                checked = checked,
                label = "GRID",
                modifier = UiModifier().fillMaxWidth().height(32f.px)
            )
        }

        val primitives = ui.endFrame()
        assertTrue(primitives.any { it is UiDrawPrimitive.Quad || it is UiDrawPrimitive.RoundedQuad }, "toggle should emit its background shape")
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.Glyph>().isNotEmpty(), "toggle should render its label through the shared glyph pipeline")
        assertFalse(!checked, "toggle should remain unchanged without interaction")
    }

    @Test
    fun dslSupportsReusableSectionHelpers() {
        val ui = UiContext()
        ui.beginFrame(260f, 180f, testSnapshot())

        ui.ui(x = 20f, y = 20f, width = 220f, font = BitmapFont()) {
            panel(id = "custom", height = 120f.toDimension()) {
                sectionTitle("Scene")
                propertyDropdown("scene-mode", "Mode", listOf("Play", "Pause"), 0)
                propertyToggle("grid", "Show Grid", checked = true)
            }
        }

        val primitives = ui.endFrame()
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.Glyph>().isNotEmpty(), "custom helpers should still render through the public DSL surface")
        assertTrue(primitives.any { it is UiDrawPrimitive.RoundedQuad || it is UiDrawPrimitive.Quad }, "custom helpers should be able to compose built-in panel and control primitives")
    }

    @Test
    fun propertyRowSupportsSlotBasedLabels() {
        val ui = UiContext()
        ui.beginFrame(320f, 160f, testSnapshot())

        var controlSlot: UiSlot? = null

        ui.ui(x = 20f, y = 20f, width = 220f, font = BitmapFont()) {
            panel(id = "slot-panel", height = 100f.toDimension()) {
                propertyRow(
                    height = 28f.dp,
                    labelWidth = 80f.dp,
                    labelContent = {
                        text("Camera")
                    }
                ) { slot ->
                    controlSlot = slot
                    dropdown(
                        id = "camera-mode",
                        options = listOf("Orbit", "Fly"),
                        selectedIndex = 0,
                        modifier = UiModifier().width(slot.width.px).height(slot.height.px)
                    )
                }
            }
        }

        val resolvedControlSlot = assertNotNull(controlSlot)
        val primitives = ui.endFrame()
        assertEquals(80f + UiSpacing.sm.toPx(), resolvedControlSlot.x)
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.Glyph>().isNotEmpty(), "slot-based property labels should still render through the shared text pipeline")
    }

    @Test
    fun propertyControlsCanUseModifierFirstSizingWithoutOverlap() {
        val ui = UiContext()
        ui.beginFrame(320f, 180f, testSnapshot())

        val controlSlots = mutableListOf<UiSlot>()

        ui.ui(x = 20f, y = 20f, width = 240f, font = BitmapFont()) {
            panel(id = "modifier-props", height = Dimension.WrapContent) {
                propertyRow(
                    label = "Mode",
                    modifier = UiModifier().height(32f.px)
                ) { slot ->
                    controlSlots += slot
                    dropdown(
                        id = "mode",
                        options = listOf("Orbit", "Fly"),
                        selectedIndex = 0,
                        modifier = UiModifier().width(slot.width.px).height(slot.height.px)
                    )
                }
                propertyRow(
                    label = "Grid",
                    modifier = UiModifier().height(32f.px)
                ) { slot ->
                    controlSlots += slot
                    val toggled = toggle(
                        id = "grid",
                        checked = true,
                        modifier = UiModifier().width(slot.width.px).height(slot.height.px)
                    )
                    assertTrue(toggled)
                }
                propertyRow(
                    label = "Speed",
                    modifier = UiModifier().height(32f.px)
                ) { slot ->
                    controlSlots += slot
                    val sliderValue = slider(
                        id = "speed",
                        min = 1f,
                        max = 10f,
                        value = 4f,
                        modifier = UiModifier().width(slot.width.px).height(slot.height.px)
                    )
                    assertEquals(4f, sliderValue)
                }
            }
        }

        ui.endFrame()
        assertTrue(controlSlots.size >= 3)
        val renderedSlots = controlSlots.takeLast(3)
        renderedSlots.zipWithNext().forEach { (previous, next) ->
            assertTrue(previous.y + previous.height <= next.y || next.y + next.height <= previous.y, "property control rows should not overlap when modifier drives their height")
        }
    }

    @Test
    fun shellPaneBuildsReusableOverlayChrome() {
        val runtime = GameUiRuntime(
            services = object : GameServiceLookup {
                override fun <T : Any> service(type: kotlin.reflect.KClass<T>): T? = null
                override fun <T : Any> requireService(type: kotlin.reflect.KClass<T>): T = error("unused")
            },
            spec = gameUi { }
        )
        runtime.uiContext.beginFrame(320f, 240f, testSnapshot())

        runtime.shellPane(
            slot = UiSlot(20f, 20f, 180f, 120f),
            id = "shell"
        ) {
            sectionTitle("Shell")
            textLines(listOf("One", "Two"))
        }

        val primitives = runtime.uiContext.endFrame()
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.Glyph>().isNotEmpty())
        assertTrue(primitives.any { it is UiDrawPrimitive.RoundedQuad })
    }

    @Test
    fun overlayShellExposesNamedAnchoredRegionsWithoutManualBoundsMath() {
        val runtime = GameUiRuntime(
            services = object : GameServiceLookup {
                override fun <T : Any> service(type: kotlin.reflect.KClass<T>): T? = null
                override fun <T : Any> requireService(type: kotlin.reflect.KClass<T>): T = error("unused")
            },
            spec = gameUi { }
        )
        runtime.uiContext.beginFrame(360f, 240f, testSnapshot())

        var topRightSlot: UiSlot? = null
        var bottomLeftSlot: UiSlot? = null

        runtime.overlayShell(viewportWidth = 360f, viewportHeight = 240f) {
            place(
                anchor = UiAnchor.TopRight,
                width = 120f.dp,
                height = 80f.dp,
                margin = UiInsets(start = 0f.dp, top = 12f.dp, end = 16f.dp, bottom = 0f.dp)
            ) { slot ->
                topRightSlot = slot
                shellPane(slot = slot, id = "top-right") {
                    text("TR")
                }
            }
            place(
                anchor = UiAnchor.BottomLeft,
                width = 140f.dp,
                height = 60f.dp,
                margin = UiInsets(start = 20f.dp, top = 0f.dp, end = 0f.dp, bottom = 8f.dp)
            ) { slot ->
                bottomLeftSlot = slot
                shellPane(slot = slot, id = "bottom-left") {
                    text("BL")
                }
            }
        }

        val primitives = runtime.uiContext.endFrame()
        assertEquals(
            UiSlot(224f, 12f, 120f, 80f),
            topRightSlot
        )
        assertEquals(
            UiSlot(20f, 172f, 140f, 60f),
            bottomLeftSlot
        )
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.Glyph>().isNotEmpty())
        assertTrue(primitives.any { it is UiDrawPrimitive.RoundedQuad })
    }

    @Test
    fun overlayShellSupportsModifierDrivenAnchoredPlacement() {
        val runtime = GameUiRuntime(
            services = object : GameServiceLookup {
                override fun <T : Any> service(type: kotlin.reflect.KClass<T>): T? = null
                override fun <T : Any> requireService(type: kotlin.reflect.KClass<T>): T = error("unused")
            },
            spec = gameUi { }
        )
        runtime.uiContext.beginFrame(360f, 240f, testSnapshot())

        var topRightSlot: UiSlot? = null
        runtime.overlayShell(viewportWidth = 360f, viewportHeight = 240f) {
            place(
                anchor = UiAnchor.TopRight,
                modifier = UiModifier().width(120f.px).height(80f.px),
                margin = UiInsets(start = 0f.dp, top = 12f.dp, end = 16f.dp, bottom = 0f.dp)
            ) { slot ->
                topRightSlot = slot
                shellPane(slot = slot, id = "modifier-top-right") {
                    text("TR")
                }
            }
        }

        runtime.uiContext.endFrame()
        assertEquals(UiSlot(224f, 12f, 120f, 80f), topRightSlot)
    }

    @Test
    fun overlayShellPaneAutoFitsContentForBottomAnchors() {
        val runtime = GameUiRuntime(
            services = object : GameServiceLookup {
                override fun <T : Any> service(type: kotlin.reflect.KClass<T>): T? = null
                override fun <T : Any> requireService(type: kotlin.reflect.KClass<T>): T = error("unused")
            },
            spec = gameUi { }
        )
        runtime.uiContext.beginFrame(360f, 240f, testSnapshot())

        var bottomLeftSlot: UiSlot? = null

        runtime.overlayShell(viewportWidth = 360f, viewportHeight = 240f) {
            pane(
                anchor = UiAnchor.BottomLeft,
                maxWidth = 180f.dp,
                margin = UiInsets(start = 20f.dp, bottom = 12f.dp)
            ) { slot ->
                bottomLeftSlot = slot
                text("Debug")
                text("Frame: 16ms")
            }
        }

        val slot = requireNotNull(bottomLeftSlot)
        val primitives = runtime.uiContext.endFrame()

        assertEquals(48f, slot.height, "two 12px rows with one 8px gap plus panel padding should auto-fit the pane body")
        assertEquals(228f, slot.y + slot.height, "the visible panel should honor the requested bottom margin")
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.Glyph>().isNotEmpty())
        assertTrue(primitives.any { it is UiDrawPrimitive.RoundedQuad })
    }

    @Test
    fun overlayBoxExposesResponsiveWidthClassesAndAlignment() {
        val runtime = GameUiRuntime(
            services = object : GameServiceLookup {
                override fun <T : Any> service(type: kotlin.reflect.KClass<T>): T? = null
                override fun <T : Any> requireService(type: kotlin.reflect.KClass<T>): T = error("unused")
            },
            spec = gameUi { }
        )
        runtime.uiContext.beginFrame(360f, 240f, testSnapshot())

        var widthClass: UiWidthSizeClass? = null
        var panelSlot: UiSlot? = null

        runtime.overlayBox(viewportWidth = 360f, viewportHeight = 240f) { constraints ->
            widthClass = constraints.widthSizeClass
            panelSlot = panel(
                id = "overlay-panel",
                width = 120f.toDimension(),
                height = Dimension.WrapContent,
                modifier = UiModifier()
                    .align(UiAlignment.BottomEnd)
                    .padding(start = 0f.dp, top = 0f.dp, end = 16f.dp, bottom = 12f.dp)
            ) {
                text("Status")
            }
        }

        val primitives = runtime.uiContext.endFrame()
        assertEquals(UiWidthSizeClass.Compact, widthClass)
        assertEquals(UiSlot(224f, 200f, 120f, 28f), panelSlot)
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.Glyph>().isNotEmpty())
        assertTrue(primitives.any { it is UiDrawPrimitive.RoundedQuad })
    }

    @Test
    fun overlayBoxSupportsStackedResponsiveColumns() {
        val runtime = GameUiRuntime(
            services = object : GameServiceLookup {
                override fun <T : Any> service(type: kotlin.reflect.KClass<T>): T? = null
                override fun <T : Any> requireService(type: kotlin.reflect.KClass<T>): T = error("unused")
            },
            spec = gameUi { }
        )
        runtime.uiContext.beginFrame(900f, 600f, testSnapshot())

        var widthClass: UiWidthSizeClass? = null
        var columnSlot: UiSlot? = null

        runtime.overlayBox(viewportWidth = 900f, viewportHeight = 600f) { constraints ->
            widthClass = constraints.widthSizeClass
            columnSlot = column(
                width = 320f.toDimension(),
                height = Dimension.WrapContent,
                modifier = UiModifier()
                    .align(UiAlignment.TopStart)
                    .padding(20f.dp)
            ) {
                panel(id = "one", width = Dimension.FillMax, height = Dimension.WrapContent) {
                    text("One")
                }
                panel(id = "two", width = Dimension.FillMax, height = Dimension.WrapContent) {
                    text("Two")
                }
            }
        }

        val primitives = runtime.uiContext.endFrame()
        assertEquals(UiWidthSizeClass.Expanded, widthClass)
        assertEquals(UiSlot(20f, 20f, 320f, 64f), columnSlot)
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.Glyph>().isNotEmpty())
        assertTrue(primitives.any { it is UiDrawPrimitive.RoundedQuad })
    }

    @Test
    fun overlayBoxUsesDensityIndependentWidthClasses() {
        val originalScale = UiDensity.scale
        UiDensity.scale = 2f
        try {
            val runtime = GameUiRuntime(
                services = object : GameServiceLookup {
                    override fun <T : Any> service(type: kotlin.reflect.KClass<T>): T? = null
                    override fun <T : Any> requireService(type: kotlin.reflect.KClass<T>): T = error("unused")
                },
                spec = gameUi { }
            )
            runtime.uiContext.beginFrame(900f, 600f, testSnapshot())

            var widthClass: UiWidthSizeClass? = null
            var maxWidth: Float? = null
            var maxWidthPx: Float? = null

            runtime.overlayBox(viewportWidth = 900f, viewportHeight = 600f) { constraints ->
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
    fun supportingTextWrapsInsideWrapContentPanels() {
        val ui = UiContext()
        ui.beginFrame(280f, 220f, testSnapshot())

        var panelSlot: UiSlot? = null

        ui.ui(x = 20f, y = 20f, width = 180f, font = BitmapFont()) {
            panel(id = "copy", height = Dimension.WrapContent) { slot ->
                panelSlot = slot
                text("Copy")
                supportingText(
                    "Shared supporting copy should wrap cleanly and grow the panel instead of spilling outside its bounds.",
                    maxLines = 4
                )
            }
        }

        val primitives = ui.endFrame()
        val glyphs = primitives.filterIsInstance<UiDrawPrimitive.Glyph>()
        val resolvedPanel = assertNotNull(panelSlot)
        assertTrue(resolvedPanel.height > 32f, "wrap-content panels should grow to fit multi-line supporting copy")
        assertTrue(glyphs.any { it.y > resolvedPanel.y + 16f }, "wrapped supporting copy should render on more than one text row")
    }

    @Test
    fun dslScrollPanelDelegatesToSharedWidgetPrimitive() {
        val ui = UiContext()
        val scrollState = UiScrollState()

        ui.beginFrame(220f, 200f, testSnapshot(x = 24f, y = 24f, scrollDeltaY = -1f))
        ui.ui(x = 12f, y = 12f, width = 160f, font = BitmapFont()) {
            scrollPanel(
                id = "dsl-scroll",
                width = Dimension.FillMax,
                height = 80f.toDimension(),
                state = scrollState,
                scrollSpeed = 20f
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

    @Test
    fun dialogUsesNeutralDarkScrimByDefault() {
        val ui = UiContext()
        ui.beginFrame(320f, 240f, testSnapshot())

        ui.ui(x = 20f, y = 20f, width = 240f, font = BitmapFont()) {
            dialog(
                id = "dialog",
                expanded = true
            ) {
                text("Dialog body")
            }
        }

        val scrim = ui.endFrame().filterIsInstance<UiDrawPrimitive.Quad>().firstOrNull()
        assertEquals(
            Color.Black.withAlpha(0.48f),
            scrim?.color,
            "dialogs should default to a neutral dark scrim so light themes do not wash the scene out"
        )
    }
}
