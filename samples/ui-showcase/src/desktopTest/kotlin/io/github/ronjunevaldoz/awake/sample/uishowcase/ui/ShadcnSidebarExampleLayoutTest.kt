// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.testing.ui.renderAnnotatedUiPreviews
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Asserts on the `shadcn-sidebar-example` preview's own frame.
 *
 * A component-level suite in `:awake:ui:designsystem` covers the same sidebar four ways -- fixed
 * height, tall content, short content, `fillMaxHeight()` inside a row, and at density 2 -- and all
 * of them pass while the preview raster shows the footer collapsed on top of the first menu row.
 * Rather than keep guessing which condition differs, this drives the preview entry itself, so the
 * font, theme, density, root modifiers and frame lifecycle are the failing ones by construction.
 *
 * Measured from the raster's own JSON: sidebar y=32 h=880 (bottom 912), menu rows running to 660,
 * footer at y=187.9. The footer belongs near 800.
 */
class ShadcnSidebarExampleLayoutTest {

    @Test
    fun theAccountFooterSitsAtTheBottomOfTheSidebar() {
        val scene = renderAnnotatedUiPreviews(ShadcnSidebarExamplePreview).single()
        val nodes = scene.semantics.associateBy { it.id }

        val sidebar = requireNotNull(nodes["shadcn-sidebar-example"]) { "sidebar node missing" }.bounds
        val footer = requireNotNull(nodes["shadcn-sidebar-example.account"]) { "footer node missing" }.bounds
        val lastMenuRow = requireNotNull(nodes["shadcn-sidebar-example.settings"]) { "menu node missing" }.bounds

        val sidebarBottom = sidebar.y + sidebar.height
        val footerTop = footer.y
        val menuBottom = lastMenuRow.y + lastMenuRow.height

        assertTrue(
            footerTop >= menuBottom,
            "the footer must start below the last menu row, not paint over it: footer top " +
                "$footerTop against a menu ending at $menuBottom.",
        )
        assertTrue(
            footer.y + footer.height <= sidebarBottom + 1f,
            "the footer must stay inside the sidebar: bottom ${footer.y + footer.height} against " +
                "a sidebar ending at $sidebarBottom.",
        )
    }
}
