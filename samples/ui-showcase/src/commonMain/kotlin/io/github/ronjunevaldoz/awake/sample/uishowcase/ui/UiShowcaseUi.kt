// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.engine.application.GameUiRuntime
import io.github.ronjunevaldoz.awake.engine.application.GameUiSpec
import io.github.ronjunevaldoz.awake.engine.application.canvas
import io.github.ronjunevaldoz.awake.engine.application.gameUi
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.ui.UiScrollConfig
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnSurfaceVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ext.column
import io.github.ronjunevaldoz.awake.ui.layouts.ext.row
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxSize
import io.github.ronjunevaldoz.awake.ui.modifier.padding
import io.github.ronjunevaldoz.awake.ui.rememberScrollState
import io.github.ronjunevaldoz.awake.ui.layout.toDimension
import io.github.ronjunevaldoz.awake.ui.modifier.verticalScroll
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

private val ShowcaseChromeTheme = shadcnTheme(dark = false)

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
    val showcaseTheme = state.showcaseTheme()
    val sidebarScroll = uiContext.rememberScrollState("ui-showcase-scroll-side")
    val contentScroll = uiContext.rememberScrollState("ui-showcase-scroll-content")
    uiContext.pushTheme(showcaseTheme)
    canvas { constraints ->
        val compact = constraints.isCompact
        val outerPadding = if (compact) 16f.dp else 24f.dp
        val sidebarWidth = 264f.dp.toDimension()
        val railGap = 20f.dp

        if (compact) {
            column(
                verticalArrangement = Arrangement.spacedBy(12f.dp),
                modifier = (Modifier.fillMaxSize().padding(outerPadding)).copy(width = Dimension.FillMax, height = Dimension.FillMax)) {
                shadcnSurface(
                    id = "ui-showcase-mobile-sidebar",
                    variant = ShadcnSurfaceVariant.Sidebar,
                    style = Style { shape(16f.dp) },
                    modifier = (Modifier.verticalScroll(sidebarScroll, UiScrollConfig.Hidden)).copy(height = Dimension.FillMax)) {
                    drawUiShowcaseSidebar(compact = true)
                }

                column(
                    id = "ui-showcase-mobile-content-viewport",
                    modifier = (Modifier.verticalScroll(contentScroll)).copy(width = Dimension.FillMax, height = Dimension.FillMax)) {
                    shadcnSurface(
                        id = "ui-showcase-mobile-content",
                        style = Style { shape(16f.dp) }
                    , modifier = Modifier.copy(height = Dimension.WrapContent)) {
                        drawUiShowcasePageContent(state, showInlineMenu = true)
                    }
                }
            }
        } else {
            row(
                horizontalArrangement = Arrangement.spacedBy(railGap),
                modifier = (Modifier.fillMaxSize().padding(outerPadding)).copy(width = Dimension.FillMax, height = Dimension.FillMax)) {
                shadcnSurface(
                    id = "ui-showcase-sidebar",
                    variant = ShadcnSurfaceVariant.Sidebar,
                    style = Style { shape(16f.dp) },
                    modifier = (Modifier.verticalScroll(sidebarScroll, UiScrollConfig.Hidden)).copy(width = sidebarWidth, height = Dimension.FillMax)) {
                    drawUiShowcaseSidebar(compact = false)
                }

                column(
                    id = "ui-showcase-content-viewport",
                    modifier = (Modifier.verticalScroll(contentScroll)).copy(width = Dimension.FillMax, height = Dimension.FillMax)) {
                    shadcnSurface(
                        id = "ui-showcase-content",
                        style = Style { shape(16f.dp) }
                    , modifier = Modifier.copy(height = Dimension.WrapContent)) {
                        drawUiShowcasePageContent(state, showInlineMenu = false)
                    }
                }
            }
        }
    }
    uiContext.popTheme()
}
