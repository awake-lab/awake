// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.engine.application.GameUiRuntime
import io.github.ronjunevaldoz.awake.engine.application.GameUiSpec
import io.github.ronjunevaldoz.awake.engine.application.canvas
import io.github.ronjunevaldoz.awake.engine.application.gameUi
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScrollConfig
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnSurfaceVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.fillMaxSize
import io.github.ronjunevaldoz.awake.ui.layouts.ext.box
import io.github.ronjunevaldoz.awake.ui.layouts.ext.column
import io.github.ronjunevaldoz.awake.ui.layouts.ext.row
import io.github.ronjunevaldoz.awake.ui.layouts.ext.surface
import io.github.ronjunevaldoz.awake.ui.padding
import io.github.ronjunevaldoz.awake.ui.rememberScrollState
import io.github.ronjunevaldoz.awake.ui.toDimension
import io.github.ronjunevaldoz.awake.ui.verticalScroll

private val ShowcaseChromeTheme = awakeShadcnTheme(dark = false)

internal fun uiShowcaseUiSpec(state: UiShowcaseRuntimeState): GameUiSpec {
    return gameUi {
        theme(ShowcaseChromeTheme)
        overlay {
            drawUiShowcaseOverlay(
                state = state
            )
        }
    }
}

internal fun GameUiRuntime.drawUiShowcaseOverlay(
    state: UiShowcaseRuntimeState
) {
    val chromeTheme = ShowcaseChromeTheme
    val showcaseTheme = state.showcaseTheme()
    val sidebarScroll = uiContext.rememberScrollState("ui-showcase-scroll-side")
    val contentScroll = uiContext.rememberScrollState("ui-showcase-scroll-content")

    canvas(theme = chromeTheme) { constraints ->
        val compact = constraints.isCompact
        val outerPadding = if (compact) 16f.dp else 24f.dp
        val sidebarWidth = 264f.dp.toDimension()
        val railGap = 20f

        box(modifier = UiModifier().fillMaxSize().padding(outerPadding)) {
            if (compact) {
                column(width = Dimension.FillMax, height = Dimension.FillMax, gap = 12f) {
                    awakeShadcnSurface(
                        id = "ui-showcase-mobile-sidebar",
                        height = Dimension.WrapContent,
                        variant = AwakeShadcnSurfaceVariant.Sidebar,
                        style = Style { shape(16f.dp) },
                        modifier = UiModifier().verticalScroll(sidebarScroll, UiScrollConfig.Hidden)
                    ) {
                        drawUiShowcaseSidebar(compact = true)
                    }

                    this@drawUiShowcaseOverlay.column(
                        slot = claimSlot(Dimension.FillMax, Dimension.FillMax),
                        theme = showcaseTheme
                    ) {
                        column(
                            id = "ui-showcase-mobile-content-viewport",
                            width = Dimension.FillMax,
                            height = Dimension.FillMax,
                            modifier = UiModifier().verticalScroll(contentScroll)
                        ) {
                            awakeShadcnSurface(
                                id = "ui-showcase-mobile-content",
                                height = Dimension.WrapContent,
                                style = Style { shape(16f.dp) }
                            ) {
                                drawUiShowcasePageContent(state, showInlineMenu = true)
                            }
                        }
                    }
                }
            } else {
                row(width = Dimension.FillMax, height = Dimension.FillMax, gap = railGap) {
                    this@row.awakeShadcnSurface(
                        id = "ui-showcase-sidebar",
                        width = sidebarWidth,
                        height = Dimension.FillMax,
                        variant = AwakeShadcnSurfaceVariant.Sidebar,
                        style = Style { shape(16f.dp) },
                        modifier = UiModifier().verticalScroll(sidebarScroll, UiScrollConfig.Hidden)
                    ) {
                        drawUiShowcaseSidebar(compact = false)
                    }

                    this@drawUiShowcaseOverlay.column(
                        slot = claimSlot(Dimension.FillMax, Dimension.FillMax),
                        theme = showcaseTheme
                    ) {
                        column(
                            id = "ui-showcase-content-viewport",
                            width = Dimension.FillMax,
                            height = Dimension.FillMax,
                            modifier = UiModifier().verticalScroll(contentScroll)
                        ) {
                            surface(
                                id = "ui-showcase-content",
                                height = Dimension.WrapContent,
                                style = Style { shape(16f.dp) }
                            ) {
                                drawUiShowcasePageContent(state, showInlineMenu = false)
                            }
                        }
                    }
                }
            }
        }
    }
}
