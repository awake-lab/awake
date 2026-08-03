// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.engine.application.GameUiRuntime
import io.github.ronjunevaldoz.awake.engine.application.GameUiSpec
import io.github.ronjunevaldoz.awake.engine.application.frame
import io.github.ronjunevaldoz.awake.engine.application.gameUi
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.ui.UiScrollConfig
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebar
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnTheme
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layout.toDimension
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxSize
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.padding
import io.github.ronjunevaldoz.awake.ui.modifier.verticalScroll
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.rememberScrollState
import io.github.ronjunevaldoz.awake.ui.style.Style

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
    frame { constraints ->
        val compact = constraints.isCompact
        val outerPadding = if (compact) 16f.dp else 24f.dp
        val sidebarWidth = 264f.dp.toDimension()
        val railGap = 20f.dp

        if (compact) {
            column(
                id = "ui-showcase-mobile-shell",
                // Pilot cross-frame hasWeightedChild cache (see
                // docs/tasks/2026-08-02-trial-measure-cross-frame-cache.md): this outer shell
                // column's two direct children (shadcnSidebar, the content-viewport column) never
                // call .weight() anywhere in this fixed source -- the weight()-usage answer for
                // THIS column can never change at runtime, so a constant cacheKey is genuinely
                // safe, not a guess. Does not need to vary with `compact`/selected page/scroll
                // offset -- none of those affect whether a direct child of *this* column uses
                // weight().
                cacheKey = "static",
                verticalArrangement = Arrangement.spacedBy(12f.dp),
                modifier = (Modifier.fillMaxSize().padding(outerPadding)).width(Dimension.FillMax).height(Dimension.FillMax)) {
                shadcnSidebar(
                    id = "ui-showcase-mobile-sidebar",
                    style = Style { shape(16f.dp) },
                    modifier = (Modifier.verticalScroll(sidebarScroll, UiScrollConfig.Hidden)).height(Dimension.FillMax)) {
                    drawUiShowcaseSidebar(compact = true)
                }

                column(
                    id = "ui-showcase-mobile-content-viewport",
                    modifier = (Modifier.verticalScroll(contentScroll)).width(Dimension.FillMax).height(Dimension.FillMax)) {
                    shadcnSurface(
                        id = "ui-showcase-mobile-content",
                        style = Style { shape(16f.dp) }
                    , modifier = Modifier.height(Dimension.WrapContent)) {
                        drawUiShowcasePageContent(state, showInlineMenu = true)
                    }
                }
            }
        } else {
            row(
                id = "ui-showcase-shell-row",
                // Same reasoning as the compact column above: this row's two direct children
                // (shadcnSidebar, the content-viewport column) never call .weight() in this fixed
                // source, so the weight()-usage answer can never change -- a constant cacheKey is
                // safe.
                cacheKey = "static",
                horizontalArrangement = Arrangement.spacedBy(railGap),
                modifier = (Modifier.fillMaxSize().padding(outerPadding)).width(Dimension.FillMax).height(Dimension.FillMax)) {
                shadcnSidebar(
                    id = "ui-showcase-sidebar",
                    style = Style { shape(16f.dp) },
                    modifier = (Modifier.verticalScroll(sidebarScroll, UiScrollConfig.Hidden)).width(sidebarWidth).height(Dimension.FillMax)) {
                    drawUiShowcaseSidebar(compact = false)
                }

                column(
                    id = "ui-showcase-content-viewport",
                    modifier = (Modifier.verticalScroll(contentScroll)).width(Dimension.FillMax).height(Dimension.FillMax)) {
                    shadcnSurface(
                        id = "ui-showcase-content",
                        style = Style {
                            shape(16f.dp)
                            borderColor(io.github.ronjunevaldoz.awake.core.colors.Color(1f, 0f, 0f, 1f))
                        },
                        modifier = Modifier.height(Dimension.WrapContent)
                    ) {
                        drawUiShowcasePageContent(state, showInlineMenu = false)
                    }
                }
            }
        }
    }
    uiContext.popTheme()
}
