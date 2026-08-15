// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("UnusedParameter")

package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.UiPopupPositionProvider
import io.github.ronjunevaldoz.awake.ui.api.UiPopupProperties
import io.github.ronjunevaldoz.awake.ui.api.UiPopupResult
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets
import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnCardSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnCardVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnSurfaceVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.style
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceBorder
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle
import io.github.ronjunevaldoz.awake.ui.headless.UiPopupDefaults
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.popup
import io.github.ronjunevaldoz.awake.ui.headless.spacer
import io.github.ronjunevaldoz.awake.ui.headless.surface

private fun surfaceStyle(values: UiThemeValues, variant: ShadcnSurfaceVariant?): SurfaceStyle {
    val colors = values.colors
    val shapes = values.shapes
    return when (variant) {
        ShadcnSurfaceVariant.Muted -> SurfaceStyle(
            background = colors.muted,
            foreground = colors.foreground,
            cornerRadius = shapes.lg,
            contentPadding = UiInsets(16f.dp),
        )

        else -> SurfaceStyle(
            background = colors.card,
            foreground = colors.cardForeground,
            border = SurfaceBorder(1f.dp, colors.border),
            cornerRadius = shapes.lg,
            contentPadding = UiInsets(24f.dp),
        )
    }
}

fun UiScope.shadcnSurface(
    id: String,
    modifier: Modifier = Modifier,
    variant: ShadcnSurfaceVariant? = null,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds = surface(
    id = id,
    modifier = modifier,
    style = surfaceStyle(themeValues, variant),
    content = content,
)

fun UiScope.shadcnCard(
    id: String,
    modifier: Modifier = Modifier,
    variant: ShadcnCardVariant = ShadcnCardVariant.Default,
    size: ShadcnCardSize = ShadcnCardSize.Default,
    header: (ColumnScope.() -> Unit)? = null,
    footer: (ColumnScope.() -> Unit)? = null,
    body: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds = surface(
    id = id,
    modifier = modifier,
    style = variant.style(themeValues),
    verticalArrangement = Arrangement.spacedBy(0f.dp),
) {
    if (header != null) {
        header()
        // CardHeader/CardContent are independently padded in shadcn. The compatibility size
        // axis contributes only a small slot gap; it never draws a divider.
        spacer(Modifier.height((24f + size.dividerGapDp).dp))
    }
    body(it)
    if (footer != null) {
        spacer(Modifier.height(size.dividerGapDp.dp))
        footer()
    }
}

fun UiScope.shadcnPopover(
    id: String,
    anchorSlot: UiBounds,
    expanded: Boolean,
    width: Dimension = Dimension.WrapContent,
    height: Dimension = Dimension.WrapContent,
    positionProvider: UiPopupPositionProvider = UiPopupDefaults.popover(),
    properties: UiPopupProperties = UiPopupProperties(),
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiPopupResult = popup(
    anchorSlot = anchorSlot,
    expanded = expanded,
    width = width,
    height = height,
    positionProvider = positionProvider,
    properties = properties,
    id = id,
) {
    surface(
        id = "$id.content",
        modifier = Modifier,
        style = SurfaceStyle(
            background = themeValues.colors.popover,
            foreground = themeValues.colors.popoverForeground,
            border = SurfaceBorder(1f.dp, themeValues.colors.border),
            cornerRadius = themeValues.shapes.md,
            contentPadding = UiInsets(16f.dp),
        ),
        content = content,
    )
}
