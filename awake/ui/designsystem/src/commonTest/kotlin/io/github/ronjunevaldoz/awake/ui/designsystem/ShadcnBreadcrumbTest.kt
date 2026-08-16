// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.testing.ui.uiTestSession
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
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Covers `shadcnBreadcrumbLink`/`Page`/`Separator`/`Ellipsis` (Fix 2 in the shadcn-compose
 * parity audit) -- the [shadcnBreadcrumb] slot itself already had coverage elsewhere; this
 * only exercises the newly-added named children. */
class ShadcnBreadcrumbTest {

    @Test
    fun breadcrumbChildrenRenderInOrderWithoutOverlap() {
        val semantics = renderShadcnComponent(width = 300f, height = 60f, font = BitmapFont()) {
            column(modifier = Modifier.fillMaxSize()) {
                row {
                    shadcnBreadcrumbLink(id = "breadcrumb.home", label = "Home", onClick = {})
                    shadcnBreadcrumbSeparator()
                    shadcnBreadcrumbEllipsis()
                    shadcnBreadcrumbSeparator()
                    shadcnBreadcrumbPage("Current")
                }
            }
        }.semantics
        val home = assertNotNull(semantics.firstOrNull { it.label == "Home" })
        val current = assertNotNull(semantics.firstOrNull { it.label == "Current" })
        assertTrue(home.bounds.x + home.bounds.width <= current.bounds.x + 1f, "crumbs should lay out left to right")
    }

    @Test
    fun breadcrumbLinkFiresOnClickWhenPressedAndReleasedInside() = uiTestSession(
        width = 300f,
        height = 60f,
                font = BitmapFont(),
    ) {
        var clicked = false

        fun linkFrame(down: Boolean, x: Float, y: Float) = frame(x = x, y = y, down = down) {
            column(modifier = Modifier.fillMaxSize()) {
                row {
                    shadcnBreadcrumbLink(id = "breadcrumb.home", label = "Home", onClick = { clicked = true })
                }
            }
        }

        // First frame: locate the link's bounds with the pointer parked off-screen.
        val located = linkFrame(down = false, x = -100f, y = -100f)
        val bounds = assertNotNull(located.semantics.firstOrNull { it.label == "Home" }).bounds
        val cx = bounds.x + bounds.width / 2f
        val cy = bounds.y + bounds.height / 2f

        linkFrame(down = true, x = cx, y = cy)
        linkFrame(down = false, x = cx, y = cy)
        assertTrue(clicked, "clicking a breadcrumb link should fire its onClick")
    }
}
