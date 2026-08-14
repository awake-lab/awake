// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.ui.UiScrollConfig
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebar
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarFooterButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarHeaderButton
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnTheme
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.createUiScope
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxHeight
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.padding
import io.github.ronjunevaldoz.awake.ui.headless.rememberScrollState
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.verticalScroll
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
 * Ruled out as the sole trigger: the caller's inner scrolling column, `fillMaxHeight` vs an
 * explicit sidebar height, `shadcnCollapsible` in the menu, and animation settling (60 frames).
 * `ShadcnSidebarFooterVisibilityTest` passes because it renders the sidebar at the frame root
 * with plain menu rows, which is not how any two-pane app composes it.
 *
 * Un-ignore once the weighted slot resolves; do not weaken the assertions to make it green --
 * counting semantic nodes passes even while nothing paints, which is how this went unnoticed.
 */
class ShowcaseShellSidebarTest {

    @Ignore // Fails today: the body slot measures 0. See this class doc.
    @Test
    fun theDesktopSidebarRendersItsCategoryMenu() {
        val ui = UiContext()
        val input = Input()
        input.setPointer(down = false, x = -100f, y = -100f)

        ui.beginFrame(1440f, 900f, input.updateSnapshot().toUiInputState())
        val sidebarScroll = ui.rememberScrollState("ui-showcase-scroll-side")
        ui.pushFont(BitmapFont())
        ui.pushTheme(shadcnTheme(dark = false))

        ui.createUiScope(UiBounds(0f, 0f, 1440f, 900f)).row(
            modifier = Modifier.padding(24f.dp).fillMaxWidth().fillMaxHeight(),
            horizontalArrangement = Arrangement.spacedBy(20f.dp),
        ) {
            shadcnSidebar(
                id = "ui-showcase-sidebar",
                modifier = Modifier.width(264f.dp).fillMaxHeight(),
                header = {
                    shadcnSidebarHeaderButton(
                        id = "ui-showcase-team-switcher",
                        title = "Acme Inc",
                        subtitle = "Enterprise",
                    )
                },
                footer = {
                    shadcnSidebarFooterButton(
                        id = "ui-showcase-user-profile",
                        name = "shadcn",
                        email = "m@example.com",
                    )
                },
            ) {
                column(
                    modifier = Modifier.verticalScroll(sidebarScroll, UiScrollConfig.Hidden)
                        .fillMaxWidth().fillMaxHeight(),
                ) {
                    drawUiShowcaseSidebar(compact = false)
                }
            }
        }

        ui.endFrame()
        val semantics = ui.semanticNodes()

        // Semantic nodes exist even when the body measures to zero, so counting them proves
        // nothing. The visible symptom is geometric: the body slot has to occupy the space
        // between header and footer.
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
            "footer escaped the sidebar: footer bottom ${footer.bounds.y + footer.bounds.height}, " +
                "sidebar bottom ${sidebar.bounds.y + sidebar.bounds.height}",
        )
    }
}
