// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebar
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarGroup
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenu
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenuItem
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.rememberScrollState
import io.github.ronjunevaldoz.awake.ui.headless.verticalScroll
import io.github.ronjunevaldoz.awake.ui.headless.width
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Follow-up to the "click stops working after spamming click" live report: the prior probe
 * (plain rapid clicks on a static button) found nothing. This drives the untested combo the
 * user actually described -- pointer-down on a real [shadcnSidebarMenuItem]-based button
 * (built on [io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton], same as
 * `shadcnButtonKeepsClickSemantics`) INSIDE a real scrolled [shadcnSidebar] +
 * `Modifier.verticalScroll`, with real `UiScrollState.scrollBy` calls interleaved between the
 * down and up frames -- matching the real construction shape used by
 * `UiShowcaseSidebarGapProbeTest`/`ShadcnCollapsibleScrolledCollapseFrameCaptureTest`.
 *
 * Real bounds are read every frame from [UiContext.semanticNodes] (not hand-computed layout
 * math), so pointer coordinates always target where a button's slot ACTUALLY is that frame --
 * exactly what a real OS pointer would be doing relative to scrolled screen-space content.
 */
class ShadcnButtonScrollClickInteractionTest {

    private val pages = (0 until 12).map { "item-$it" }

    private fun boundsOf(ui: UiContext, id: String) =
        ui.semanticNodes().first { it.role == UiSemanticRole.Button && it.id == id }.bounds

    private var lastClickedIds: Set<String> = emptySet()

    private fun buildFrameCapturingClicks(ui: UiContext, x: Float, y: Float, down: Boolean, scrollDeltaY: Float) {
        val clicked = mutableSetOf<String>()
        ui.beginFrame(300f, 220f, testSnapshot(x = x, y = y, down = down, scrollDeltaY = scrollDeltaY))
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        val root = ui.headlessRoot()
        val scroll = root.rememberScrollState("interleave-sidebar-scroll")
        root.shadcnSidebar(
            id = "interleave-sidebar",
            modifier = Modifier.verticalScroll(scroll).width(260f.dp).height(220f.dp),
        ) {
            shadcnSidebarGroup {
                shadcnSidebarMenu {
                    pages.forEach { id ->
                        shadcnSidebarMenuItem(id = id, label = id, active = false, onClick = { clicked += id })
                    }
                }
            }
        }
        ui.endFrame()
        lastClickedIds = clicked
    }

    @Test
    fun scrollDuringHold_thenReleaseOffTarget_doesNotClickEitherButton() {
        val ui = UiContext()
        buildFrameCapturingClicks(ui, x = -100f, y = -100f, down = false, scrollDeltaY = 0f) // settle mount

        val target = boundsOf(ui, "item-3")
        val px = target.x + target.width / 2f
        val py = target.y + target.height / 2f

        // Frame N: pointer-down squarely on item-3.
        buildFrameCapturingClicks(ui, px, py, down = true, scrollDeltaY = 0f)
        assertTrue(lastClickedIds.isEmpty(), "down edge must never itself fire clicked")

        // Frames N+1..N+3: scroll while still held, at the SAME stationary pointer position --
        // item-3 physically moves out from under the pointer. Negative scrollDeltaY scrolls
        // content DOWN (increases offsetY) -- see ScrollContainers.kt's `-scrollDeltaY * speed`.
        repeat(3) {
            buildFrameCapturingClicks(ui, px, py, down = true, scrollDeltaY = -40f)
            assertTrue(lastClickedIds.isEmpty(), "must never click while pointer is still down")
        }

        val movedTarget = boundsOf(ui, "item-3")
        assertTrue(
            movedTarget.y != target.y,
            "test setup bug: item-3 didn't actually move -- scroll had no effect, sidebar isn't scrollable at this size",
        )

        // Whatever item now sits at the stationary pointer's (px, py) after scrolling.
        val relocatedId = pages.firstOrNull { id ->
            val b = boundsOf(ui, id)
            px in b.x..(b.x + b.width) && py in b.y..(b.y + b.height)
        }

        // Release at the same stationary (px, py): item-3 has scrolled away (release-outside,
        // must not click), and whatever new item is now under the pointer must ALSO not click,
        // since activeId is bound to item-3's own id, not to "whatever is at this pixel".
        buildFrameCapturingClicks(ui, px, py, down = false, scrollDeltaY = 0f)
        assertFalse(lastClickedIds.contains("item-3"), "release-outside after scroll must not click the scrolled-away original target")
        if (relocatedId != null && relocatedId != "item-3") {
            assertFalse(
                lastClickedIds.contains(relocatedId),
                "activeId is bound to item-3's own id -- content that scrolled INTO the old pixel position must not steal the click",
            )
        }
        assertTrue(lastClickedIds.isEmpty(), "no click at all should fire from this release-outside sequence")
    }

    @Test
    fun scrollAwayThenScrollBack_beforeRelease_stillClicksOriginalTarget() {
        val ui = UiContext()
        buildFrameCapturingClicks(ui, x = -100f, y = -100f, down = false, scrollDeltaY = 0f)

        val target = boundsOf(ui, "item-3")
        val px = target.x + target.width / 2f
        val py = target.y + target.height / 2f

        buildFrameCapturingClicks(ui, px, py, down = true, scrollDeltaY = 0f)
        buildFrameCapturingClicks(ui, px, py, down = true, scrollDeltaY = -40f) // scroll down/away
        buildFrameCapturingClicks(ui, px, py, down = true, scrollDeltaY = 40f) // scroll back up to original offset
        assertTrue(lastClickedIds.isEmpty())

        val restoredTarget = boundsOf(ui, "item-3")
        assertEquals(target.y, restoredTarget.y, 0.01f, "test setup bug: net-zero scroll didn't restore original offset")

        buildFrameCapturingClicks(ui, px, py, down = false, scrollDeltaY = 0f)
        assertTrue(lastClickedIds.contains("item-3"), "hover restored by the time of release must still register the click")
    }

    @Test
    fun releaseImmediatelyThenScrollAfterward_noSpuriousClickDuringScroll() {
        val ui = UiContext()
        buildFrameCapturingClicks(ui, x = -100f, y = -100f, down = false, scrollDeltaY = 0f)

        val target = boundsOf(ui, "item-3")
        val px = target.x + target.width / 2f
        val py = target.y + target.height / 2f

        buildFrameCapturingClicks(ui, px, py, down = true, scrollDeltaY = 0f)
        buildFrameCapturingClicks(ui, px, py, down = false, scrollDeltaY = 0f)
        assertTrue(lastClickedIds.contains("item-3"))

        // Pointer moves away and scroll continues -- must be totally quiet, no residual click.
        repeat(5) {
            buildFrameCapturingClicks(ui, -100f, -100f, down = false, scrollDeltaY = 30f)
            assertTrue(lastClickedIds.isEmpty(), "scrolling after an unrelated release must never fire a click")
        }
    }

    /** Rules out a hidden drag-to-scroll path: [io.github.ronjunevaldoz.awake.ui.ScrollContainers]
     * only consumes real `scrollDeltaX`/`scrollDeltaY` wheel input (see its `hitTest(slot)` +
     * `state.scrollBy` block) -- pointer movement alone while held down over the scroll area,
     * with zero wheel delta, must never move the scroll offset, so a click-and-drag gesture can
     * never get misread as a scroll gesture. */
    @Test
    fun dragWithoutWheelDelta_neverScrolls() {
        val ui = UiContext()
        buildFrameCapturingClicks(ui, x = -100f, y = -100f, down = false, scrollDeltaY = 0f)
        val before = boundsOf(ui, "item-3").y

        val target = boundsOf(ui, "item-3")
        buildFrameCapturingClicks(ui, target.x + 5f, target.y + 5f, down = true, scrollDeltaY = 0f)
        // "Drag" the pointer down the sidebar with the button held, zero wheel delta each frame.
        for (i in 1..10) {
            buildFrameCapturingClicks(ui, target.x + 5f, target.y + 5f + i * 8f, down = true, scrollDeltaY = 0f)
        }
        buildFrameCapturingClicks(ui, target.x + 5f, target.y + 85f, down = false, scrollDeltaY = 0f)

        val after = boundsOf(ui, "item-3").y
        assertEquals(before, after, 0.01f, "pointer drag with zero wheel delta must never scroll the sidebar")
    }
}
