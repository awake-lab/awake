// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBreadcrumb
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBreadcrumbEllipsis
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBreadcrumbLink
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBreadcrumbPage
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBreadcrumbSeparator
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxSize
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.toUiInputState
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Covers `shadcnBreadcrumbLink`/`Page`/`Separator`/`Ellipsis` (Fix 2 in the shadcn-compose
 * parity audit) -- the [shadcnBreadcrumb] slot itself already had coverage elsewhere; this
 * only exercises the newly-added named children. */
class ShadcnBreadcrumbTest {

    @Test
    fun breadcrumbChildrenRenderInOrderWithoutOverlap() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(300f, 60f, testSnapshot())

        ui.headlessRoot().column(modifier = Modifier.fillMaxSize()) {
            row {
                shadcnBreadcrumbLink(id = "breadcrumb.home", label = "Home", onClick = {})
                shadcnBreadcrumbSeparator()
                shadcnBreadcrumbEllipsis()
                shadcnBreadcrumbSeparator()
                shadcnBreadcrumbPage("Current")
            }
        }

        val semantics = ui.finishFrame().semantics
        val home = assertNotNull(semantics.firstOrNull { it.label == "Home" })
        val current = assertNotNull(semantics.firstOrNull { it.label == "Current" })
        assertTrue(home.bounds.x + home.bounds.width <= current.bounds.x + 1f, "crumbs should lay out left to right")
    }

    @Test
    fun breadcrumbLinkFiresOnClickWhenPressedAndReleasedInside() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        var clicked = false

        fun frame(down: Boolean, x: Float, y: Float) {
            val input = Input()
            input.setPointer(down, x, y)
            ui.beginFrame(300f, 60f, input.updateSnapshot().toUiInputState())
            ui.headlessRoot().column(modifier = Modifier.fillMaxSize()) {
                row {
                    shadcnBreadcrumbLink(id = "breadcrumb.home", label = "Home", onClick = { clicked = true })
                }
            }
            ui.finishFrame()
        }

        // First frame: locate the link's bounds with the pointer parked off-screen.
        frame(down = false, x = -100f, y = -100f)
        val ui2 = UiContext()
        ui2.pushFont(BitmapFont())
        ui2.pushTheme(ShadcnTheme)
        ui2.beginFrame(300f, 60f, testSnapshot())
        ui2.headlessRoot().column(modifier = Modifier.fillMaxSize()) {
            row {
                shadcnBreadcrumbLink(id = "breadcrumb.home", label = "Home", onClick = {})
            }
        }
        val bounds = assertNotNull(ui2.finishFrame().semantics.firstOrNull { it.label == "Home" }).bounds
        val cx = bounds.x + bounds.width / 2f
        val cy = bounds.y + bounds.height / 2f

        frame(down = true, x = cx, y = cy)
        frame(down = false, x = cx, y = cy)
        assertTrue(clicked, "clicking a breadcrumb link should fire its onClick")
    }
}
