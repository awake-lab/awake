// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.GameUiRuntime
import io.github.ronjunevaldoz.awake.ui.GameUiSpec
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiAlignment
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.align
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnSurfaceVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnSurface
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.gameUi
import io.github.ronjunevaldoz.awake.ui.overlayBox
import io.github.ronjunevaldoz.awake.ui.padding

internal fun uiShowcaseUiSpec(state: UiShowcaseRuntimeState): GameUiSpec {
    return gameUi {
        theme(AwakeShadcnTheme)
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
    val showcaseTheme = state.showcaseTheme()
    overlayBox(viewportWidth, viewportHeight, theme = showcaseTheme) { constraints ->
        val compact = constraints.isCompact
        val outerPadding = if (compact) 16f else 24f
        val topBarHeight = if (compact) 156f else 108f
        val bodyTop = outerPadding + topBarHeight + 16f
        val sidebarWidth = 264f
        val railGap = 20f
        val availableHeight = (constraints.maxHeightDp - bodyTop - outerPadding).coerceAtLeast(420f)

        if (compact) {
            column(
                width = Dimension.FillMax,
                height = Dimension.WrapContent,
                modifier = UiModifier()
                    .align(UiAlignment.TopStart)
                    .padding(outerPadding.dp)
            ) {
                awakeShadcnSurface(
                    id = "ui-showcase-mobile-topbar",
                    height = Dimension.Fixed(topBarHeight.dp),
                    style = Style {
                        shape(16f.dp)
                    }
                ) { _ ->
                    drawUiShowcaseTopBar(state = state, compact = true)
                }
                spacer(12f)
                awakeShadcnSurface(
                    id = "ui-showcase-mobile-sidebar",
                    height = Dimension.WrapContent,
                    variant = AwakeShadcnSurfaceVariant.Sidebar,
                    style = Style {
                        shape(16f.dp)
                    }
                ) { _ ->
                    drawUiShowcaseSidebar(compact = true)
                }
                spacer(12f)
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
            box(width = Dimension.FillMax, height = Dimension.FillMax) { _ ->
                column(
                    width = Dimension.FillMax,
                    height = Dimension.Fixed(topBarHeight.dp),
                    modifier = UiModifier()
                        .align(UiAlignment.TopStart)
                        .padding(
                            start = outerPadding.dp,
                            top = outerPadding.dp,
                            end = outerPadding.dp,
                            bottom = 0f.dp
                        )
                ) {
                    awakeShadcnSurface(
                        id = "ui-showcase-topbar",
                        height = Dimension.Fixed(topBarHeight.dp),
                        style = Style {
                            shape(16f.dp)
                        }
                    ) { _ ->
                        drawUiShowcaseTopBar(state = state, compact = false)
                    }
                }

                column(
                    width = Dimension.Fixed(sidebarWidth.dp),
                    height = Dimension.Fixed(availableHeight.dp),
                    modifier = UiModifier()
                        .align(UiAlignment.TopStart)
                        .padding(
                            start = outerPadding.dp,
                            top = bodyTop.dp,
                            end = 0f.dp,
                            bottom = outerPadding.dp
                        )
                ) {
                    awakeShadcnSurface(
                        id = "ui-showcase-sidebar",
                        height = Dimension.Fixed(availableHeight.dp),
                        variant = AwakeShadcnSurfaceVariant.Sidebar,
                        style = Style {
                            shape(16f.dp)
                        }
                    ) { _ ->
                        drawUiShowcaseSidebar(compact = false)
                    }
                }

                column(
                    width = Dimension.FillMax,
                    height = Dimension.Fixed(availableHeight.dp),
                    modifier = UiModifier()
                        .align(UiAlignment.TopStart)
                        .padding(
                            start = (outerPadding + sidebarWidth + railGap).dp,
                            top = bodyTop.dp,
                            end = outerPadding.dp,
                            bottom = outerPadding.dp
                        )
                ) {
                    awakeShadcnSurface(
                        id = "ui-showcase-content",
                        height = Dimension.Fixed(availableHeight.dp),
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
}
