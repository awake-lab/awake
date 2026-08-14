// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Asserts the sidebar footer's position in the showcase preview's own frame.
 *
 * `ShadcnSidebarFooterVisibilityTest` in `:awake:ui:designsystem` covers the same sidebar five
 * ways -- fixed height, tall content, short content, `fillMaxHeight()` in a row, at density 2, and
 * nested inside a wrapping surface -- and every one of them passes while this fails. So the defect
 * is not the sidebar recipe's shape, and not the wrapping parent.
 *
 * It caught the real one: the header is a `shadcnSidebarHeaderButton`, and a button's content was
 * being counted as direct children of the sidebar column, which desynced the weighted content
 * slot's index and collapsed it to zero height (fixed in Buttons.kt via compositeContent()).
 */
class ShadcnSidebarPreviewLayoutTest {

    @Test
    fun theAccountFooterSitsBelowTheMenuAndInsideTheSidebar() {
        val entry = showcasePreviewEntry("sidebar")
        val nodes = entry.render(entry.metadata).semantics.associateBy { it.id }

        fun bounds(id: String) = requireNotNull(nodes[id]) {
            "no semantic node '$id'; ids present: ${nodes.keys.filterNotNull().sorted()}"
        }.bounds

        val sidebar = bounds("showcase-sidebar")
        val footer = bounds("showcase-sidebar-user")
        val lastMenuRow = bounds("showcase-sidebar-item-3")

        val sidebarBottom = sidebar.y + sidebar.height
        val footerBottom = footer.y + footer.height
        val where = "sidebar ${sidebar.y}..$sidebarBottom, footer ${footer.y}..$footerBottom, " +
            "last menu row ends at ${lastMenuRow.y + lastMenuRow.height}"

        assertTrue(
            footerBottom <= sidebarBottom + 1f,
            "the footer must stay inside the sidebar -- $where",
        )
        // "Inside" alone is also satisfied by a footer collapsed to the top, painted over the
        // first menu row, which is exactly what the preview used to render. Pin it to the bottom
        // edge. Note the menu's own bounds may extend past the content slot: the slot scrolls, so
        // overflow is clipped rather than shortened, and its nodes keep their unclipped geometry.
        assertTrue(
            footerBottom >= sidebarBottom - footer.height,
            "the footer must sit at the BOTTOM of the sidebar, not wherever the content column " +
                "stopped -- $where",
        )
    }
}
