// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.engine.application.GameUiRuntime
import io.github.ronjunevaldoz.awake.engine.application.GameUiSpec
import io.github.ronjunevaldoz.awake.engine.application.canvas
import io.github.ronjunevaldoz.awake.engine.application.gameUi
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiAlignment
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.align
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnSurfaceVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.fillMaxSize
import io.github.ronjunevaldoz.awake.ui.layouts.ext.box
import io.github.ronjunevaldoz.awake.ui.layouts.ext.column
import io.github.ronjunevaldoz.awake.ui.layouts.ext.surface
import io.github.ronjunevaldoz.awake.ui.padding
import io.github.ronjunevaldoz.awake.ui.rememberScrollState
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
        val outerPadding = if (compact) 16f else 24f
        val sidebarWidth = 264f
        val availableHeight = (constraints.maxHeightDp - outerPadding * 2f).coerceAtLeast(420f)

        if (compact) {
            val sidebarHeight = minOf(220f, availableHeight * 0.34f).coerceAtLeast(160f)
            awakeShadcnSurface(
                id = "ui-showcase-mobile-sidebar",
                width = Dimension.Fixed(sidebarWidth.dp),
                height = Dimension.Fixed(sidebarHeight.dp),
                variant = AwakeShadcnSurfaceVariant.Sidebar,
                style = Style {
                    shape(16f.dp)
                },
                modifier = UiModifier().verticalScroll(sidebarScroll)
            ) { _ ->
                drawUiShowcaseSidebar(compact = true)
            }
        } else {
            box(modifier = UiModifier().fillMaxSize()) { _ ->
                awakeShadcnSurface(
                    id = "ui-showcase-sidebar",
                    width = Dimension.Fixed(sidebarWidth.dp),
                    height = Dimension.Fixed(availableHeight.dp),
                    variant = AwakeShadcnSurfaceVariant.Sidebar,
                    style = Style {
                        shape(16f.dp)
                    },
                    modifier = UiModifier().verticalScroll(sidebarScroll)
                ) { _ ->
                    drawUiShowcaseSidebar(compact = false)
                }
            }
        }
    }

    canvas(theme = showcaseTheme) { constraints ->
        val compact = constraints.isCompact
        val outerPadding = if (compact) 16f else 24f
        val sidebarWidth = 264f
        val railGap = 20f
        val availableHeight = (constraints.maxHeightDp - outerPadding * 2f).coerceAtLeast(420f)

        if (compact) {
            val sidebarHeight = minOf(220f, availableHeight * 0.34f).coerceAtLeast(160f)
            column(
                id = "ui-showcase-page-content",
                width = Dimension.FillMax,
                height = Dimension.Fixed(availableHeight.dp),
                modifier = UiModifier()
                    .align(UiAlignment.TopStart)
                    .padding(
                        start = outerPadding.dp,
                        top = (outerPadding + sidebarHeight + 12f).dp,
                        end = outerPadding.dp,
                        bottom = outerPadding.dp
                    )
                    .verticalScroll(contentScroll),
                style = Style {
                    background(Color.Transparent)
                    borderWidth(0f.dp)
                    contentPadding(0f.dp)
                }
            ) {
                awakeShadcnSurface(
                    id = "ui-showcase-mobile-content",
                    height = Dimension.WrapContent,
                    style = Style {
                        shape(16f.dp)
                    }
                ) { _ ->
                    drawUiShowcasePageContent(state, showInlineMenu = true)
                }
            }
        } else {
            column(
                id = "ui-showcase-page-content",
                width = Dimension.FillMax,
                height = Dimension.Fixed(availableHeight.dp),
                modifier = UiModifier()
                    .align(UiAlignment.TopStart)
                    .padding(
                        start = (outerPadding + sidebarWidth + railGap).dp,
                        top = outerPadding.dp,
                        end = outerPadding.dp,
                        bottom = outerPadding.dp
                    )
                    .verticalScroll(contentScroll),
                style = Style {
                    background(Color.Transparent)
                    borderWidth(0f.dp)
                    contentPadding(0f.dp)
                }
            ) {
                surface(
                    id = "ui-showcase-content",
                    height = Dimension.WrapContent,
                    style = Style {
                        shape(16f.dp)
                    }
                ) { _ ->
                    drawUiShowcasePageContent(state, showInlineMenu = false)
                }
            }
        }
    }
}
