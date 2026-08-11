package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.unstyled.overlayScrim
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

class UiScopeFacadeTest {
    @Test
    fun createsAHeadlessFacadeForTheRootRegion() {
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
        context.beginFrame(320f, 240f, UiInputState())
        val scope = context.createUiScope(UiBounds(0f, 0f, 320f, 240f))

        scope.overlayScrim(scope.frameBounds(), Color.Black)

        val overlay = context.endFrame().filterIsInstance<UiDrawPrimitive.Quad>().single()
        assertEquals(320f, overlay.w)
        assertEquals(240f, overlay.h)
        assertTrue(overlay.color == Color.Black)
    }

    @Test
    fun popupUsesHeadlessContractsAndColumnScope() {
        val context = UiContext()
        context.beginFrame(200f, 120f, UiInputState())
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
}
