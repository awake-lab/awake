// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.testing.ui.requireSemanticNode
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenuItem
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenuSubItem
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.column
import kotlin.test.Test
import kotlin.test.assertEquals

class ShadcnSidebarMenuItemTest {

    /**
     * Regression test for the "active sidebar item paints wrong" report: real shadcn's active
     * sidebar item is `bg-sidebar-accent` -- a subtle fill that persists whether or not the
     * cursor happens to be resting on it. Pointer is parked away from the item on purpose so
     * this captures the item's *resting* look, not a coincidental hover.
     */
    @Test
    fun activeItemBackgroundMatchesSidebarAccentAtRest() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(320f, 200f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.headlessRoot().column {
            shadcnSidebarMenuItem(id = "item", label = "Scene", active = true)
        }

        val semantics = ui.finishFrame().semantics
        val node = requireSemanticNode(semantics, id = "item", role = UiSemanticRole.Button)
        assertEquals(
            ShadcnTheme.sidebarAccent,
            node.backgroundColor,
            "an active sidebar menu item must paint the sidebar-accent background at rest, " +
                "not fall back to the Ghost variant's transparent resting fill",
        )
    }

    @Test
    fun inactiveItemStaysTransparentAtRest() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(320f, 200f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.headlessRoot().column {
            shadcnSidebarMenuItem(id = "item", label = "Scene", active = false)
        }

        val semantics = ui.finishFrame().semantics
        val node = requireSemanticNode(semantics, id = "item", role = UiSemanticRole.Button)
        assertEquals(
            io.github.ronjunevaldoz.awake.core.colors.Color.Transparent,
            node.backgroundColor,
            "an inactive sidebar menu item should stay chromeless until hovered",
        )
    }

    @Test
    fun activeSubItemBackgroundMatchesSidebarAccentAtRest() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(320f, 200f, testSnapshot(x = -100f, y = -100f, down = false))

        ui.headlessRoot().column {
            shadcnSidebarMenuSubItem(id = "sub-item", label = "Detail", active = true)
        }

        val semantics = ui.finishFrame().semantics
        val node = requireSemanticNode(semantics, id = "sub-item", role = UiSemanticRole.Button)
        assertEquals(
            ShadcnTheme.sidebarAccent,
            node.backgroundColor,
            "an active sidebar sub-item must paint the sidebar-accent background at rest too",
        )
    }
}
