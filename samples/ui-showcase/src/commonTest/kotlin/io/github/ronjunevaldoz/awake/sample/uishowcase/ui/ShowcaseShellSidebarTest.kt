// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.ui.UiSemanticNode
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebar
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarFooterButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarHeaderButton
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnThemeValues
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.createUiScope
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxHeight
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.padding
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.width
import io.github.ronjunevaldoz.awake.ui.toUiInputState
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Renders the desktop shell exactly as `drawUiShowcaseOverlay` composes it -- header slot,
 * footer slot, and the body wrapped in its own scrolling `fillMaxHeight` column.
 *
 * The pre-existing sidebar tests all render `drawUiShowcaseSidebar` in a simpler container
 * (no header/footer, no inner column), so none of them covered the composition the app
 * actually ships. That gap let the real sidebar render its chrome and nothing else.
 *
 * CURRENTLY FAILING, hence `@Ignore`. The body slot -- `column(Modifier.fillMaxWidth().weight(1f))`
 * inside `shadcnSidebar` -- resolves to 0 height, so the footer lands directly under the header
 * and the whole menu paints outside any viewport (`sidebarScroll.viewportHeight == 0` against
 * `contentHeight == 1870`). The behaviour predates the catalog restructure: the shell composition
 * is byte-identical at the parent commit.
 *
 * ISOLATED to the `header` slot, by bisect over the real recipes:
 *
 *   footer slot only .................... body 756px  OK
 *   header + footer ..................... body 0px    COLLAPSED
 *   header + footer, inside a row ....... body 0px    COLLAPSED
 *   header + footer, row, fillMaxHeight . body 0px    COLLAPSED
 *   PLAIN 48dp surface header + footer .. body 700px  OK
 *   header rendered in body + footer .... body 748px  OK
 *
 * The last two lines are the finding: same slot, same 48dp measured height, but
 * `shadcnSidebarHeaderButton` collapses the weighted body and a plain surface does not. The
 * sidebar's own layout, the two-pane row, `fillMaxHeight`, and the menu content are all
 * innocent -- a plain header works with every one of them.
 *
 * Also ruled out: the caller's inner scrolling column, `shadcnCollapsible` in the menu,
 * animation settling (60 frames), and the generic weight path (header + `weight(1f)` + footer
 * in a bare `surface` resolves correctly at root and in a row, with content that fits and
 * content overflowing 5x).
 *
 * The app works around this by rendering its team switcher as the first body child instead of
 * passing it to `header`, which costs the pinned-header behaviour. Un-ignore and drop that
 * workaround once `shadcnSidebarHeaderButton` stops collapsing the slot. Do not weaken the
 * assertions to make it green -- counting semantic nodes passes even while nothing paints,
 * which is how this went unnoticed.
 */
class ShowcaseShellSidebarTest {

    /** The composition the app actually ships: switcher in the body, footer in its slot. */
    @Test
    fun theShippedSidebarKeepsARealBodyBetweenItsChrome() {
        val nodes = renderShell(headerInSlot = false)
        val sidebar = nodes.first { it.id == "ui-showcase-sidebar" }
        val switcher = nodes.first { it.id == "ui-showcase-team-switcher" }
        val footer = nodes.first { it.id == "ui-showcase-user-profile" }

        val bodyHeight = footer.bounds.y - switcher.bounds.y
        assertTrue(bodyHeight > 100f, "sidebar body collapsed to ${bodyHeight}px")
        assertTrue(
            nodes.any { it.id?.startsWith("ui-showcase-page-") == true },
            "sidebar rendered no page items",
        )
        assertTrue(
            footer.bounds.y + footer.bounds.height <= sidebar.bounds.y + sidebar.bounds.height + 1f,
            "footer escaped the sidebar",
        )
    }

    @Ignore // Fails today: the body slot measures 0. See this class doc.
    @Test
    fun theDesktopSidebarRendersItsCategoryMenu() {
        val semantics = renderShell(headerInSlot = true)
        val sidebar = semantics.first { it.id == "ui-showcase-sidebar" }
        val header = semantics.first { it.id == "ui-showcase-team-switcher" }
        val footer = semantics.first { it.id == "ui-showcase-user-profile" }

        val bodyHeight = footer.bounds.y - (header.bounds.y + header.bounds.height)
        assertTrue(
            bodyHeight > 100f,
            "sidebar body collapsed to ${bodyHeight}px: header ends at " +
                "${header.bounds.y + header.bounds.height}, footer starts at ${footer.bounds.y}",
        )
        assertTrue(
            footer.bounds.y + footer.bounds.height <= sidebar.bounds.y + sidebar.bounds.height + 1f,
            "footer escaped the sidebar",
        )
    }

    private fun renderShell(headerInSlot: Boolean): List<UiSemanticNode> {
        val ui = UiContext()
        val input = Input()
        input.setPointer(down = false, x = -100f, y = -100f)

        ui.beginFrame(1440f, 900f, input.updateSnapshot().toUiInputState())
        ui.pushFont(BitmapFont())
        ui.pushTheme(shadcnThemeValues(dark = false))

        ui.createUiScope(UiBounds(0f, 0f, 1440f, 900f)).row(
            modifier = Modifier.padding(24f.dp).fillMaxWidth().fillMaxHeight(),
            horizontalArrangement = Arrangement.spacedBy(20f.dp),
        ) {
            shadcnSidebar(
                id = "ui-showcase-sidebar",
                modifier = Modifier.width(264f.dp).fillMaxHeight(),
                header = if (!headerInSlot) {
                    null
                } else {
                    {
                        shadcnSidebarHeaderButton(
                            id = "ui-showcase-team-switcher",
                            title = "Acme Inc",
                            subtitle = "Enterprise",
                        )
                    }
                },
                footer = {
                    shadcnSidebarFooterButton(
                        id = "ui-showcase-user-profile",
                        name = "shadcn",
                        email = "m@example.com",
                    )
                },
            ) {
                if (!headerInSlot) {
                    shadcnSidebarHeaderButton(
                        id = "ui-showcase-team-switcher",
                        title = "Acme Inc",
                        subtitle = "Enterprise",
                    )
                }
                drawUiShowcaseSidebar(compact = false)
            }
        }

        ui.endFrame()
        return ui.semanticNodes()
    }
}
