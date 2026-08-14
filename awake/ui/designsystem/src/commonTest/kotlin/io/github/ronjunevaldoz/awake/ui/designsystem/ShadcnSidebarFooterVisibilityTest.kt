// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.UiDensity
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebar
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarFooterButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarGroup
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenu
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenuItem
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxHeight
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.spacer
import io.github.ronjunevaldoz.awake.ui.headless.surface
import io.github.ronjunevaldoz.awake.ui.headless.width
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * A `footer` slot has to stay inside the sidebar. That is the whole distinction between passing
 * something as `footer` and just appending it to `content`.
 *
 * `shadcnSidebar` laid header, content and footer out as plain siblings, so tall content simply
 * displaced the footer past the sidebar's own bottom edge and it rendered off-screen. Requiring
 * every caller to size its content to leave room would make the slot's guarantee depend on the
 * caller honouring it -- the showcase passed `fillMaxHeight()`, which claims the entire sidebar.
 */
class ShadcnSidebarFooterVisibilityTest {

    /** footerBottom, sidebarBottom, footerHeight -- the last so tolerances scale with density. */
    private fun footerBottomAndSidebarBottom(contentHeightDp: Float): Triple<Float, Float, Float> {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(400f, 600f, testSnapshot())
        val sidebar = ui.headlessRoot().shadcnSidebar(
            id = "sidebar",
            modifier = Modifier.width(SIDEBAR_WIDTH.dp).height(SIDEBAR_HEIGHT.dp),
            header = { spacer(Modifier.width(SIDEBAR_WIDTH.dp).height(32f.dp)) },
            footer = {
                shadcnSidebarFooterButton(id = FOOTER_ID, name = "shadcn", email = "m@example.com")
            },
        ) {
            // The real recipes, not a bare spacer. A spacer claims its height directly; a group of
            // menu rows is nested columns whose own sizing is what the weighted parent has to
            // resolve against, and only that shape reproduces the collapse.
            shadcnSidebarGroup(label = "Platform") {
                shadcnSidebarMenu {
                    repeat((contentHeightDp / MENU_ROW_HEIGHT).toInt().coerceAtLeast(1)) { index ->
                        shadcnSidebarMenuItem(id = "item-$index", label = "Item $index")
                    }
                }
            }
        }
        val output = ui.finishFrame()
        // By id, not "whatever sits lowest" -- the sidebar surface itself is the lowest node, so
        // a positional lookup passes no matter where the footer actually landed.
        val footer = requireNotNull(output.semantics.firstOrNull { it.id == FOOTER_ID }) {
            "no semantic node for '$FOOTER_ID'; ids present: ${output.semantics.mapNotNull { it.id }}"
        }
        return Triple(
            footer.bounds.y + footer.bounds.height,
            sidebar.y + sidebar.height,
            footer.bounds.height,
        )
    }

    @Test
    fun contentTallerThanTheSidebarDoesNotPushTheFooterOutOfIt() {
        // Content asks for well over the sidebar's own height -- the case a scrolling menu hits.
        val (footerBottom, sidebarBottom, footerHeight) = footerBottomAndSidebarBottom(contentHeightDp = 900f)

        assertTrue(
            footerBottom <= sidebarBottom + 1f,
            "the footer must stay within the sidebar: its bottom is at $footerBottom against a " +
                "sidebar bottom of $sidebarBottom. Content is absorbing the slack that the " +
                "footer needs.",
        )
        // "Inside the sidebar" is not enough on its own -- a footer collapsed to the TOP of the
        // sidebar, painted over the first menu row, also satisfies it. Pin it to the bottom edge.
        assertTrue(
            footerBottom >= sidebarBottom - footerHeight,
            "the footer must sit at the BOTTOM of the sidebar, not wherever the content column " +
                "happened to stop: bottom is at $footerBottom in a sidebar ending at " +
                "$sidebarBottom. A footer this high is overlapping the menu.",
        )
    }

    @Test
    fun contentShorterThanTheSidebarStillPinsTheFooterToTheBottom() {
        // The shadcn example's own shape: a handful of menu rows in a tall sidebar. The footer has
        // to be pushed DOWN to the bottom edge, which only happens if the content slot expands to
        // absorb the slack. If it merely wraps its rows, the footer follows immediately behind and
        // lands over the menu -- what the shadcn-sidebar-example preview renders.
        val (footerBottom, sidebarBottom, footerHeight) = footerBottomAndSidebarBottom(contentHeightDp = 100f)

        assertTrue(
            footerBottom >= sidebarBottom - footerHeight,
            "short content must not drag the footer up with it: footer bottom $footerBottom in a " +
                "sidebar ending at $sidebarBottom.",
        )
    }

    @Test
    fun aFillMaxHeightSidebarStillPinsTheFooterToTheBottomAtDensity2() {
        // Previews render at UiDensity.scale = 2, and that is the only structural difference left
        // between this suite (all green) and the shadcn-sidebar-example raster (footer collapsed
        // over the menu). If weight distribution is density-sensitive, it shows up here.
        val previous = UiDensity.scale
        UiDensity.scale = 2f
        try {
            aFillMaxHeightSidebarStillPinsTheFooterToTheBottom()
        } finally {
            UiDensity.scale = previous
        }
    }

    @Test
    fun aFillMaxHeightSidebarStillPinsTheFooterToTheBottom() {
        // How every real caller sizes it -- fillMaxHeight() inside a row, not a fixed height. The
        // sidebar's own height then resolves from its parent rather than from its modifier, and
        // the weighted content slot has to divide THAT. This is the shadcn-sidebar-example
        // preview's exact shape.
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(600f, SIDEBAR_HEIGHT, testSnapshot())
        var sidebar: io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds? = null
        ui.headlessRoot().row(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
            sidebar = shadcnSidebar(
                id = "sidebar",
                modifier = Modifier.width(SIDEBAR_WIDTH.dp).fillMaxHeight(),
                header = { spacer(Modifier.width(SIDEBAR_WIDTH.dp).height(32f.dp)) },
                footer = {
                    shadcnSidebarFooterButton(id = FOOTER_ID, name = "shadcn", email = "m@example.com")
                },
            ) {
                shadcnSidebarGroup(label = "Platform") {
                    shadcnSidebarMenu {
                        repeat(4) { index ->
                            shadcnSidebarMenuItem(id = "item-$index", label = "Item $index")
                        }
                    }
                }
            }
        }
        val output = ui.finishFrame()
        val footer = requireNotNull(output.semantics.firstOrNull { it.id == FOOTER_ID }) {
            "no semantic node for '$FOOTER_ID'"
        }
        val sidebarBottom = requireNotNull(sidebar).let { it.y + it.height }
        val footerBottom = footer.bounds.y + footer.bounds.height
        val footerHeight = footer.bounds.height

        assertTrue(
            footerBottom >= sidebarBottom - footerHeight,
            "footer bottom $footerBottom in a sidebar ending at $sidebarBottom -- it collapsed to " +
                "the top and is painting over the menu.",
        )
    }

    @Test
    fun aSidebarNestedInsideAWrappingSurfaceStillPinsTheFooterToTheBottom() {
        // The showcase preview's shape: the sidebar is not a direct child of the frame, it sits
        // inside a wrap-height surface inside a column. Every other case here puts it at the root,
        // and all of them pass while the preview renders the footer collapsed under the header --
        // so the wrapping parent is the remaining difference.
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(600f, 900f, testSnapshot())
        var sidebar: io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds? = null
        ui.headlessRoot().surface(id = "wrapper", modifier = Modifier.fillMaxWidth()) {
            sidebar = shadcnSidebar(
                id = "sidebar",
                modifier = Modifier.width(SIDEBAR_WIDTH.dp).height(SIDEBAR_HEIGHT.dp),
                header = { spacer(Modifier.width(SIDEBAR_WIDTH.dp).height(32f.dp)) },
                footer = {
                    shadcnSidebarFooterButton(id = FOOTER_ID, name = "shadcn", email = "m@example.com")
                },
            ) {
                shadcnSidebarGroup(label = "Platform") {
                    shadcnSidebarMenu {
                        repeat(4) { index ->
                            shadcnSidebarMenuItem(id = "item-$index", label = "Item $index")
                        }
                    }
                }
            }
        }
        val output = ui.finishFrame()
        val footer = requireNotNull(output.semantics.firstOrNull { it.id == FOOTER_ID }) {
            "no semantic node for '$FOOTER_ID'"
        }
        val sidebarBottom = requireNotNull(sidebar).let { it.y + it.height }
        val footerBottom = footer.bounds.y + footer.bounds.height

        assertTrue(
            footerBottom >= sidebarBottom - footer.bounds.height,
            "footer ${footer.bounds.y}..$footerBottom in a sidebar ending at $sidebarBottom -- it " +
                "collapsed toward the top and is painting over the menu.",
        )
    }

    private companion object {
        const val SIDEBAR_WIDTH = 260f
        const val SIDEBAR_HEIGHT = 400f
        const val FOOTER_ID = "sidebar-footer-button"
        const val MENU_ROW_HEIGHT = 32f
    }
}
