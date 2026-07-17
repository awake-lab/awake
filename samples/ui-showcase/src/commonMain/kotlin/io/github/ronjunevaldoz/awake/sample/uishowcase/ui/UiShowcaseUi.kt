// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.GameUiRuntime
import io.github.ronjunevaldoz.awake.ui.GameUiSpec
import io.github.ronjunevaldoz.awake.ui.UiContext
import io.github.ronjunevaldoz.awake.ui.UiDensity
import io.github.ronjunevaldoz.awake.ui.UiScrollState
import io.github.ronjunevaldoz.awake.ui.UiSpacing
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiAlignment
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiTheme
import io.github.ronjunevaldoz.awake.ui.align
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnSurfaceVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnScrollSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnSurface
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.gameUi
import io.github.ronjunevaldoz.awake.ui.horizontalPx
import io.github.ronjunevaldoz.awake.ui.measureDslColumnContent
import io.github.ronjunevaldoz.awake.ui.overlayBox
import io.github.ronjunevaldoz.awake.ui.padding
import io.github.ronjunevaldoz.awake.ui.rememberStateValue
import io.github.ronjunevaldoz.awake.ui.resolveStyle
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.verticalPx

private const val UiShowcaseOuterTopBarGap = 16f

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
    val chromeTheme = AwakeShadcnTheme
    val showcaseTheme = state.showcaseTheme()
    val sidebarScroll = uiContext.rememberStateValue("ui-showcase-scroll", "sidebar") { UiScrollState() }
    val contentScroll = uiContext.rememberStateValue("ui-showcase-scroll", "content") { UiScrollState() }

    overlayBox(viewportWidth, viewportHeight, theme = chromeTheme) { constraints ->
        val compact = constraints.isCompact
        val outerPadding = if (compact) 16f else 24f
        val topBarWidth = constraints.maxWidthDp - (outerPadding * 2f)
        val topBarHeight = measureUiShowcaseTopBarHeight(
            context = uiContext,
            font = font,
            state = state,
            compact = compact,
            width = topBarWidth,
            theme = chromeTheme
        )
        val bodyTop = outerPadding + topBarHeight + UiShowcaseOuterTopBarGap
        val sidebarWidth = 264f
        val availableHeight = (constraints.maxHeightDp - bodyTop - outerPadding).coerceAtLeast(420f)

        if (compact) {
            val sidebarHeight = minOf(220f, availableHeight * 0.34f).coerceAtLeast(160f)
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
        val topBarWidth = constraints.maxWidthDp - (outerPadding * 2f)
        val topBarHeight = measureUiShowcaseTopBarHeight(
            context = uiContext,
            font = font,
            state = state,
            compact = compact,
            width = topBarWidth,
            theme = chromeTheme
        )
        val bodyTop = outerPadding + topBarHeight + UiShowcaseOuterTopBarGap
        val sidebarWidth = 264f
        val railGap = 20f
        val availableHeight = (constraints.maxHeightDp - bodyTop - outerPadding).coerceAtLeast(420f)

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
                        top = (outerPadding + topBarHeight + 12f + sidebarHeight + 12f).dp,
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
                        top = bodyTop.dp,
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

internal fun measureUiShowcaseTopBarHeight(
    context: UiContext,
    font: UiFont?,
    state: UiShowcaseRuntimeState,
    compact: Boolean,
    width: Float,
    theme: UiTheme = AwakeShadcnTheme
): Float {
    // `width` arrives in dp space (callers pass `UiBoxConstraints.maxWidthDp`), but
    // measureDslColumnContent/UiSlot layout math is raw-pixel space (see Dp.kt's contract).
    // Convert at this boundary and convert the measured px height back to dp before
    // returning, since callers re-wrap the result with `.dp` (Dimension.Fixed(...)) --
    // skipping this double-converts through UiDensity.scale on any non-1x display.
    val widthPx = width.dp.toPx()
    val resolved = context.absolute(0f, 0f, font = font, theme = theme).resolveStyle(
        style = theme.components.panel then Style { shape(16f.dp) }
    )
    val availableWidth = (widthPx - resolved.contentPadding.horizontalPx()).coerceAtLeast(0f)
    val measured = context.measureDslColumnContent(
        width = availableWidth,
        font = font,
        theme = theme,
        gap = UiSpacing.sm.toPx(),
        textScale = resolved.textScale
    ) { slot ->
        drawUiShowcaseTopBar(state = state, compact = compact)
    }
    val heightPx = (measured.height + resolved.contentPadding.verticalPx()).coerceAtLeast(0f)
    return UiDensity.pxToDp(heightPx)
}
