// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("MagicNumber", "MatchingDeclarationName")

package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.UiPopupResult
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.PanelEdge
import io.github.ronjunevaldoz.awake.ui.headless.SlidePanelProperties
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceBorder
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.slidePanel

/** Which viewport edge a Shadcn Sheet enters from. */
enum class ShadcnSheetSide { Top, Right, Bottom, Left }

/** Shadcn's edge-to-edge Sheet recipe backed by Headless slide-panel behavior. */
fun UiScope.shadcnSheet(
    id: String,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    side: ShadcnSheetSide = ShadcnSheetSide.Right,
    size: Dp = 384f.dp,
    content: ColumnScope.(UiBounds) -> Unit,
): UiPopupResult {
    val result = slidePanel(
        id = id,
        expanded = expanded,
        edge = side.toPanelEdge(),
        size = size,
        properties = SlidePanelProperties(
            scrimColor = Color.Black.withAlpha(0.48f),
            surface = SurfaceStyle(
                background = themeValues.colors.background,
                foreground = themeValues.colors.foreground,
                border = SurfaceBorder(1f.dp, themeValues.colors.border),
                contentPadding = io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets(16f.dp),
                cornerRadius = 0f.dp,
            ),
        ),
        content = content,
    )
    if (result.dismissed) onDismissRequest()
    return result
}

private fun ShadcnSheetSide.toPanelEdge(): PanelEdge = when (this) {
    ShadcnSheetSide.Top -> PanelEdge.Top
    ShadcnSheetSide.Right -> PanelEdge.Right
    ShadcnSheetSide.Bottom -> PanelEdge.Bottom
    ShadcnSheetSide.Left -> PanelEdge.Left
}
