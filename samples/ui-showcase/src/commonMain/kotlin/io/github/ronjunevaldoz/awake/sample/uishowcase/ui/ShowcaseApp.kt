// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.engine.platformauthoring.ui.AppUiRuntime
import io.github.ronjunevaldoz.awake.engine.platformauthoring.ui.AppUiSpec
import io.github.ronjunevaldoz.awake.engine.platformauthoring.ui.appUi
import io.github.ronjunevaldoz.awake.engine.platformauthoring.ui.headlessFrame
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebar
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarFooterButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarHeaderButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnThemeValues
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxHeight
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.padding
import io.github.ronjunevaldoz.awake.ui.headless.rememberScrollState
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.verticalScroll
import io.github.ronjunevaldoz.awake.ui.headless.width

private val ShowcaseChromeTheme = shadcnThemeValues(dark = false)

internal fun uiShowcaseUiSpec(state: UiShowcaseRuntimeState): AppUiSpec = appUi {
    theme(ShowcaseChromeTheme)
    overlay {
        drawUiShowcaseOverlay(
            state = state,
        )
    }
}

internal fun AppUiRuntime.drawUiShowcaseOverlay(
    state: UiShowcaseRuntimeState,
) {
    val showcaseTheme = state.showcaseTheme()
    // Captured before the nested UiScope block: this@drawUiShowcaseOverlay's GameUiRuntime and
    // shadcnTheme's UiScope now share one @AwakeUiDsl marker (see B7), so viewportWidth is no
    // longer implicitly reachable once inside it.
    val viewportWidthPx = viewportWidth
    headlessFrame {
        shadcnTheme(theme = showcaseTheme) {
            val sidebarScroll = rememberScrollState("ui-showcase-scroll-side")
            val contentScroll = rememberScrollState("ui-showcase-scroll-content")
            val compact = viewportWidthPx < 720f
            val outerPadding = if (compact) 16f.dp else 24f.dp
            val sidebarWidth = 264f.dp
            val railGap = 20f.dp

            if (compact) {
                column(
                    verticalArrangement = Arrangement.spacedBy(12f.dp),
                    modifier = Modifier.padding(outerPadding).fillMaxWidth().fillMaxHeight(),
                ) {
                    shadcnSidebar(
                        id = "ui-showcase-mobile-sidebar",
                        modifier = Modifier.verticalScroll(sidebarScroll).fillMaxHeight(),
                    ) {
                        drawUiShowcaseSidebar(compact = true)
                    }

                    column(
                        modifier = Modifier.verticalScroll(contentScroll).fillMaxWidth()
                            .fillMaxHeight(),
                    ) {
                        shadcnSurface(
                            id = "ui-showcase-mobile-content",
                            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                        ) {
                            drawUiShowcasePageContent(state, showInlineMenu = true)
                        }
                    }
                }
            } else {
                row(
                    id = "ui-showcase-main-row",
                    cacheKey = "main",
                    horizontalArrangement = Arrangement.spacedBy(railGap),
                    modifier = Modifier.padding(outerPadding).fillMaxWidth().fillMaxHeight(),
                ) {
                    shadcnSidebar(
                        id = "ui-showcase-sidebar",
                        modifier = Modifier.width(sidebarWidth).fillMaxHeight(),
                        // Team switcher in menu body to prevent weighted body collapse (see ShowcaseShellSidebarTest).
                        footer = {
                            shadcnSidebarFooterButton(
                                id = "ui-showcase-user-profile",
                                name = "shadcn",
                                email = "m@example.com",
                            )
                        },
                    ) {
                        shadcnSidebarHeaderButton(
                            id = "ui-showcase-team-switcher",
                            title = "Acme Inc",
                            subtitle = "Enterprise",
                        )
                        // The category/page menu is the part that overflows a short viewport -- scroll
                        // just this weighted slice instead of the whole body, so the switcher above and
                        // the footer slot below stay pinned instead of the tail of the menu (e.g. the
                        // Combobox entry) painting through the footer.
                        column(
                            modifier = Modifier.fillMaxWidth().fillMaxHeight()
                                .verticalScroll(sidebarScroll)
                        ) {
                            drawUiShowcaseSidebar(compact = false)
                        }
                    }

                    column(
                        modifier = Modifier.verticalScroll(contentScroll).fillMaxWidth()
                            .fillMaxHeight(),
                    ) {
                        shadcnSurface(
                            id = "ui-showcase-content",
                            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                        ) {
                            drawUiShowcasePageContent(state, showInlineMenu = false)
                        }
                    }
                }
            }
        }
    }
}
