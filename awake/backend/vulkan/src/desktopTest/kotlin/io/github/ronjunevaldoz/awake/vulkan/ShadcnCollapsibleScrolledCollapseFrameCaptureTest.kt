// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiScrollState
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCollapsible
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebar
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarGroup
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenu
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenuItem
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.verticalScroll
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.rememberScrollState
import io.github.ronjunevaldoz.awake.ui.toUiInputState
import java.io.File
import kotlin.test.Test

/**
 * Follow-up to [ShadcnCollapsibleRealRenderCollapseFrameCaptureTest]: that test (and the two
 * logical-bounds probes before it) only ever collapsed a category from the TOP of an unscrolled
 * sidebar. The still-untested real scenario from the live report: a sidebar that's genuinely
 * scrolled down (same `ShowcasePagesByCategory` category/page shape as
 * `samples/ui-showcase/.../UiShowcaseChrome.kt`'s real `drawUiShowcaseSidebarMenu`, wired through
 * the same real `Modifier.verticalScroll(state)` + `shadcnSidebarGroup`/`shadcnSidebarMenu`/
 * `shadcnSidebarMenuItem` shape), collapsing a category ABOVE the current scroll position. Per
 * `UiScrollState.update()`'s unconditional `offsetY.coerceIn(0f, maxOffsetY)` every frame, a
 * shrinking `contentHeight` while scrolled near the bottom forces `offsetY` down in lockstep --
 * this checks whether that's actually smooth (same eased height driving both) or has a real
 * ordering-bug discontinuity.
 *
 * Not a pass/fail assertion (see docs/reference/ui-validation.md's Canonical Test Surfaces entry
 * for this tool) -- dumps the real rendered PNG sequence to
 * `build/ui-animation-capture/shadcn-collapsible-scrolled-collapse/` AND prints per-frame
 * `offsetY`/`maxOffsetY`/`contentHeight` read directly off the real [UiScrollState] for a
 * human/agent to inspect for a jump.
 */
class ShadcnCollapsibleScrolledCollapseFrameCaptureTest {

    @Test
    fun capturesRealRenderedScrolledCollapseSequence() {
        val font = UiFonts.default(cellSize = 12)
        val ui = UiContext()
        val input = Input()
        input.setPointer(down = false, x = -100f, y = -100f)

        // Same category/page-count shape as samples/ui-showcase's real
        // ShowcasePagesByCategory (7 categories, 31 pages total, first-occurrence order) so the
        // sidebar genuinely needs to scroll the way the real catalog nav does.
        val categories = listOf(
            "Getting Started" to 3,
            "Typography" to 2,
            "Patterns" to 5,
            "Inputs" to 6,
            "Layout" to 8,
            "Overlays" to 4,
            "Animations" to 3,
        )
        val expandedByCategory = categories.associate { (title, _) -> title to true }.toMutableMap()
        lateinit var sidebarScroll: UiScrollState

        fun frame(): List<UiDrawPrimitive> {
            ui.beginFrame(480f, 800f, input.updateSnapshot().toUiInputState())
            ui.pushFont(font)
            ui.pushTheme(ShadcnTheme)
            sidebarScroll = ui.rememberScrollState("scrolled-capture-sidebar-scroll")
            ui.createBox(x = 0f, y = 0f, width = 480f, height = 800f).shadcnSidebar(
                id = "scrolled-capture-sidebar",
                modifier = Modifier.verticalScroll(sidebarScroll).width(280f.dp).height(Dimension.FillMax),
            ) {
                categories.forEach { (title, pageCount) ->
                    shadcnCollapsible(
                        id = "scrolled-capture-category-$title",
                        title = title,
                        expanded = expandedByCategory.getValue(title),
                        onExpandedChange = { expandedByCategory[title] = it },
                    ) {
                        shadcnSidebarGroup {
                            shadcnSidebarMenu {
                                repeat(pageCount) { index ->
                                    shadcnSidebarMenuItem(
                                        id = "scrolled-capture-page-$title-$index",
                                        label = "$title Page $index",
                                        active = false,
                                    )
                                }
                            }
                        }
                    }
                }
            }
            return ui.endFrame()
        }

        val outputDir = File("build/ui-animation-capture/shadcn-collapsible-scrolled-collapse")
        UiAnimationFrameCapture.create(480, 800, outputDir).use { capture ->
            // Let all 7 categories' expand animations fully settle before touching scroll.
            repeat(30) { frame() }

            val settledMaxOffset = sidebarScroll.maxOffsetY
            check(settledMaxOffset > 0f) {
                "Test setup bug: sidebar content (${sidebarScroll.contentHeight}px) doesn't " +
                    "exceed viewport (${sidebarScroll.viewportHeight}px) -- add more categories/pages."
            }
            // Pre-scroll near the bottom of the (currently full-height) content.
            sidebarScroll.scrollTo(offsetY = settledMaxOffset - 4f)
            repeat(5) { frame() } // let the scroll offset itself settle/re-clamp once.

            println(
                "Pre-collapse: offsetY=${sidebarScroll.offsetY} maxOffsetY=${sidebarScroll.maxOffsetY} " +
                    "contentHeight=${sidebarScroll.contentHeight} viewportHeight=${sidebarScroll.viewportHeight}",
            )

            // Collapse "Getting Started", the topmost category -- well above the current scroll
            // position -- so its shrinking height genuinely reduces contentHeight while scrolled.
            expandedByCategory["Getting Started"] = false

            var previousOffsetY = sidebarScroll.offsetY
            repeat(FRAME_COUNT) { index ->
                val primitives = frame()
                capture.captureFrame(index, primitives, font)
                val offsetY = sidebarScroll.offsetY
                val delta = offsetY - previousOffsetY
                println(
                    "frame=$index offsetY=$offsetY maxOffsetY=${sidebarScroll.maxOffsetY} " +
                        "contentHeight=${sidebarScroll.contentHeight} deltaOffsetY=$delta",
                )
                previousOffsetY = offsetY
            }
        }

        println(
            "Real rendered scrolled-collapse sequence written to " +
                "${File(outputDir, "frame-000.png").absolutePath.substringBeforeLast(File.separator)} " +
                "($FRAME_COUNT frames) -- inspect for a visible jump/snap between consecutive frames, " +
                "and the printed offsetY/maxOffsetY/contentHeight log above for the exact numbers.",
        )
    }

    private companion object {
        const val FRAME_COUNT = 30
    }
}
