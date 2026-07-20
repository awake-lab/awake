// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.GameUiRuntime
import io.github.ronjunevaldoz.awake.ui.GameUiSpec
import io.github.ronjunevaldoz.awake.ui.UiScrollState
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiAlignment
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.align
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnSurfaceVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnScrollSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnTheme
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.gameUi
import io.github.ronjunevaldoz.awake.ui.overlayBox
import io.github.ronjunevaldoz.awake.ui.padding
import io.github.ronjunevaldoz.awake.ui.rememberStateValue
import io.github.ronjunevaldoz.awake.ui.column
import io.github.ronjunevaldoz.awake.ui.box

private val ShowcaseChromeTheme = awakeShadcnTheme(dark = false)

internal fun uiShowcaseUiSpec(state: UiShowcaseRuntimeState): GameUiSpec {
    return gameUi {
        theme(ShowcaseChromeTheme)
        overlay { viewportWidth, viewportHeight ->
            drawUiShowcaseOverlay(
                state = state,
                viewportWidth = viewportWidth,
                viewportHeight = viewportHeight
            )
        }
    }
}

internal fun GameUiRuntime.drawUiShowcaseOverlay(
    state: UiShowcaseRuntimeState,
    viewportWidth: Float,
    viewportHeight: Float
) {
    val chromeTheme = ShowcaseChromeTheme
    val showcaseTheme = state.showcaseTheme()
    val sidebarScroll = uiContext.rememberStateValue("ui-showcase-scroll", "sidebar") { UiScrollState() }
    val contentScroll = uiContext.rememberStateValue("ui-showcase-scroll", "content") { UiScrollState() }

    overlayBox(viewportWidth, viewportHeight, theme = chromeTheme) { constraints ->
        val compact = constraints.isCompact
        val outerPadding = if (compact) 16f else 24f
        val sidebarWidth = 264f
        val availableHeight = (constraints.maxHeightDp - outerPadding * 2f).coerceAtLeast(420f)

        if (compact) {
            val sidebarHeight = minOf(220f, availableHeight * 0.34f).coerceAtLeast(160f)
            column(
                width = Dimension.FillMax,
                height = Dimension.WrapContent,
                modifier = UiModifier()
                    .align(UiAlignment.TopStart)
                    .padding(outerPadding.dp)
            ) {
                awakeShadcnScrollSurface(
                    id = "ui-showcase-mobile-sidebar",
                    height = Dimension.Fixed(sidebarHeight.dp),
                    state = sidebarScroll.value,
                    variant = AwakeShadcnSurfaceVariant.Sidebar,
                    style = Style {
                        shape(16f.dp)
                    }
                ) { _ ->
                    drawUiShowcaseSidebar(compact = true)
                }
            }
        } else {
            box(width = Dimension.FillMax, height = Dimension.FillMax) { _ ->
                column(
                    width = Dimension.Fixed(sidebarWidth.dp),
                    height = Dimension.Fixed(availableHeight.dp),
                    modifier = UiModifier()
                        .align(UiAlignment.TopStart)
                        .padding(outerPadding.dp)
                ) {
                    awakeShadcnScrollSurface(
                        id = "ui-showcase-sidebar",
                        height = Dimension.Fixed(availableHeight.dp),
                        state = sidebarScroll.value,
                        variant = AwakeShadcnSurfaceVariant.Sidebar,
                        style = Style {
                            shape(16f.dp)
                        }
                    ) { _ ->
                        drawUiShowcaseSidebar(compact = false)
                    }
                }
            }
        }
    }

    overlayBox(viewportWidth, viewportHeight, theme = showcaseTheme) { constraints ->
        val compact = constraints.isCompact
        val outerPadding = if (compact) 16f else 24f
        val sidebarWidth = 264f
        val railGap = 20f
        val availableHeight = (constraints.maxHeightDp - outerPadding * 2f).coerceAtLeast(420f)

        if (compact) {
            val sidebarHeight = minOf(220f, availableHeight * 0.34f).coerceAtLeast(160f)
            val contentHeight = (availableHeight - sidebarHeight - 12f).coerceAtLeast(220f)
            column(
                width = Dimension.FillMax,
                height = Dimension.WrapContent,
                modifier = UiModifier()
                    .align(UiAlignment.TopStart)
                    .padding(
                        start = outerPadding.dp,
                        top = (outerPadding + sidebarHeight + 12f).dp,
                        end = outerPadding.dp,
                        bottom = outerPadding.dp
                    )
            ) {
                awakeShadcnScrollSurface(
                    id = "ui-showcase-mobile-content",
                    height = Dimension.Fixed(contentHeight.dp),
                    state = contentScroll.value,
                    style = Style {
                        shape(16f.dp)
                    }
                ) { _ ->
                    drawUiShowcasePageContent(state, showInlineMenu = true)
                }
            }
        } else {
            column(
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
            ) {
                awakeShadcnScrollSurface(
                    id = "ui-showcase-content",
                    height = Dimension.Fixed(availableHeight.dp),
                    state = contentScroll.value,
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
