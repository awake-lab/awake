// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.engine.gameauthoring.GameUiRuntime
import io.github.ronjunevaldoz.awake.engine.gameauthoring.GameUiSpec
import io.github.ronjunevaldoz.awake.engine.gameauthoring.gameUi
import io.github.ronjunevaldoz.awake.engine.gameauthoring.headlessFrame
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnHeadline
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebar
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxHeight
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.rememberScrollState
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.verticalScroll
import io.github.ronjunevaldoz.awake.ui.headless.width
import io.github.ronjunevaldoz.awake.ui.theme.asRuntimeTheme

private val ShowcaseChromeTheme = shadcnTheme(dark = false)

internal fun uiShowcaseUiSpec(state: UiShowcaseRuntimeState): GameUiSpec {
    return gameUi {
        theme(ShowcaseChromeTheme)
        overlay {
            drawUiShowcaseOverlay(
                state = state,
            )
        }
    }
}

internal fun GameUiRuntime.drawUiShowcaseOverlay(
    state: UiShowcaseRuntimeState,
) {
    val showcaseTheme = state.showcaseTheme()
    uiContext.pushTheme(showcaseTheme.asRuntimeTheme())
    headlessFrame {
        val sidebarScroll = rememberScrollState("ui-showcase-scroll-side")
        val contentScroll = rememberScrollState("ui-showcase-scroll-content")
        val compact = viewportWidth < 720f
        val outerPadding = if (compact) 16f.dp else 24f.dp
        val sidebarWidth = 264f.dp
        val railGap = 20f.dp

        if (compact) {
            column(
                verticalArrangement = Arrangement.spacedBy(12f.dp),
                modifier = Modifier.fillMaxWidth().fillMaxHeight(),
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
                horizontalArrangement = Arrangement.spacedBy(railGap),
                modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            ) {
                shadcnSidebar(
                    id = "ui-showcase-sidebar",
                    modifier = Modifier.verticalScroll(sidebarScroll).width(sidebarWidth)
                        .fillMaxHeight(),
                    header = {
                        shadcnBadge(
                            id = "ui-showcase-brand",
                            label = "SHADCN",
                            variant = ShadcnBadgeVariant.Primary
                        )
                        shadcnHeadline("Catalog")
                        shadcnSupportingText(
                            if (compact) {
                                "Choose one page at a time."
                            } else {
                                "Grouped component and pattern pages, following the shadcn-compose catalog layout."
                            },
                        )
                    }
                ) {
                    drawUiShowcaseSidebar(compact = false)
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
    uiContext.popTheme()
}
