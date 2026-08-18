// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.style.MutableStyleState
import io.github.ronjunevaldoz.awake.ui.style.Style
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import io.github.ronjunevaldoz.awake.ui.context.UiFrameInput

@io.github.ronjunevaldoz.awake.testing.ui.UiLowLevelTest("Checks Headless facade mapping to low-level scope mechanics")
class UiScopeFacadeTest {
    @Test
    fun createsAHeadlessFacadeForTheRootRegion() {
        val scope = UiContext().createUiScope(UiBounds(0f, 0f, 320f, 240f))

        assertNotNull(scope)
        assertNotNull(scope)
    }

    @Test
    fun keepsThemeAndTypographyProvidersOutOfTheHeadlessFacade() {
        val scope = UiContext().createUiScope(UiBounds(0f, 0f, 320f, 240f))
        assertNotNull(scope)
    }

    @Test
    fun wrapsEachCoreLayoutReceiverBehindAHeadlessType() {
        val context = UiContext()

        assertNotNull(context.createColumn(0f, 0f, 320f).asHeadlessScope())
        assertNotNull(context.createRow(0f, 0f, height = 240f).asHeadlessScope())
        assertNotNull(context.createBox(0f, 0f, 320f, 240f).asHeadlessScope())
        assertNotNull(context.createAbsolute(0f, 0f).asHeadlessScope())
    }

    @Test
    fun facadeCanUseHeadlessOverlayBehaviorWithoutExposingThePrimitiveScope() {
        val context = UiContext()
        context.beginFrame(UiFrameInput(viewportWidth = 320f, viewportHeight = 240f, input = UiInputState()))
        val scope = context.createUiScope(UiBounds(0f, 0f, 320f, 240f))

        scope.overlayScrim(scope.frameBounds(), Color.Black)

        val overlay = context.finishFrame().primitives.filterIsInstance<UiDrawPrimitive.Quad>().single()
        assertEquals(320f, overlay.w)
        assertEquals(240f, overlay.h)
        assertTrue(overlay.color == Color.Black)
    }

    @Test
    fun popupUsesHeadlessContractsAndColumnScope() {
        val context = UiContext()
        context.beginFrame(UiFrameInput(viewportWidth = 200f, viewportHeight = 120f, input = UiInputState()))
        val scope = context.createUiScope(UiBounds(0f, 0f, 200f, 120f))

        var receivedColumnScope = false
        val result = scope.popup(
            anchorSlot = UiBounds(20f, 20f, 40f, 20f),
            expanded = true,
            width = Dimension.Fixed(80f.px),
            height = Dimension.Fixed(40f.px),
        ) { _ ->
            receivedColumnScope = true
        }

        assertEquals(UiBounds(20f, 40f, 80f, 40f), result.slot)
        assertTrue(receivedColumnScope)
    }

    @Test
    fun layoutsUseHeadlessScopesAndComposeStyleModifier() {
        val context = UiContext()
        context.beginFrame(UiFrameInput(viewportWidth = 200f, viewportHeight = 120f, input = UiInputState()))
        val scope = context.createUiScope(UiBounds(0f, 0f, 200f, 120f))

        var receivedRowScope = false
        val slot = scope.column(modifier = Modifier.fillMaxSize()) {
            row(modifier = Modifier.fillMaxWidth()) {
                receivedRowScope = true
            }
        }

        assertEquals(UiBounds(0f, 0f, 200f, 120f), slot)
        assertTrue(receivedRowScope)
    }

    @Test
    fun surfaceUsesHeadlessVisualContracts() {
        val context = UiContext()
        context.beginFrame(UiFrameInput(viewportWidth = 200f, viewportHeight = 120f, input = UiInputState()))
        val scope = context.createUiScope(UiBounds(0f, 0f, 200f, 120f))

        val slot = scope.surface(
            id = "panel",
            modifier = Modifier.width(80f.px).height(40f.px),
            style = Style { background(Color.Black); shape(4f.px) },
        ) { }

        assertEquals(UiBounds(0f, 0f, 80f, 40f), slot)
        assertTrue(context.finishFrame().primitives.filterIsInstance<UiDrawPrimitive.RoundedQuad>().any { it.color == Color.Black })
    }

    @Test
    fun buttonUsesHeadlessModifierAndNeutralStyle() {
        val context = UiContext()
        context.beginFrame(UiFrameInput(viewportWidth = 200f, viewportHeight = 120f, input = UiInputState()))
        val scope = context.createUiScope(UiBounds(0f, 0f, 200f, 120f))

        val clicked = scope.button(
            id = "confirm",
            label = "Confirm",
            modifier = Modifier.width(100f.px).height(40f.px),
            style = Style { background(Color.Black) },
        )

        assertTrue(!clicked)
        assertTrue(context.finishFrame().primitives.filterIsInstance<UiDrawPrimitive.Quad>().any { it.color == Color.Black })
    }

    @Test
    fun buttonUsesTheStyleNativeApi() {
        val context = UiContext()
        context.beginFrame(UiFrameInput(viewportWidth = 200f, viewportHeight = 120f, input = UiInputState()))
        val scope = context.createUiScope(UiBounds(0f, 0f, 200f, 120f))

        scope.button(
            id = "confirm-style",
            label = "Confirm",
            modifier = Modifier.width(100f.px).height(40f.px),
            style = Style { background(Color.Black) },
        )

        assertTrue(context.finishFrame().primitives.filterIsInstance<UiDrawPrimitive.Quad>().any { it.color == Color.Black })
    }

    @Test
    fun textureQuadForwardsTheMaterialIntoTheSlotItClaims() {
        val context = UiContext()
        context.beginFrame(UiFrameInput(viewportWidth = 200f, viewportHeight = 120f, input = UiInputState()))
        val scope = context.createUiScope(UiBounds(0f, 0f, 200f, 120f))
        val material = Any()

        scope.textureQuad(material, modifier = Modifier.width(80f.px).height(45f.px))

        val texture = context.finishFrame().primitives.filterIsInstance<UiDrawPrimitive.Texture>().single()
        assertEquals(UiBounds(0f, 0f, 80f, 45f), UiBounds(texture.x, texture.y, texture.w, texture.h))
        assertTrue(texture.material === material)
    }

    @Test
    fun styleMapsInteractionStatesWithoutNamingARecipe() {
        val style = Style {
            background(Color.Black)
            hovered { background(Color.White) }
            active { background(Color.Transparent) }
            disabled { background(Color(0.2f, 0.2f, 0.2f)) }
        }

        assertEquals(Color.Black, style.resolve().background)
        assertEquals(Color.White, style.resolve(MutableStyleState(hovered = true)).background)
        assertEquals(Color.Transparent, style.resolve(MutableStyleState(active = true)).background)
        assertEquals(Color(0.2f, 0.2f, 0.2f), style.resolve(MutableStyleState(disabled = true)).background)
    }
}
