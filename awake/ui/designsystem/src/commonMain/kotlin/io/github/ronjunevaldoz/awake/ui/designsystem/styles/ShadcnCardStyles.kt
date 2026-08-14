// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.styles

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets
import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceBorder
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceShadow
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle

/**
 * Resolves [SurfaceStyle] for a [ShadcnCardVariant].
 */
fun ShadcnCardVariant.style(values: UiThemeValues): SurfaceStyle {
    val colors = values.colors
    val shapes = values.shapes
    val base = SurfaceStyle(
        background = colors.card,
        foreground = colors.cardForeground,
        border = SurfaceBorder(1f.dp, colors.border),
        cornerRadius = shapes.lg,
        contentPadding = UiInsets(24f.dp),
    )
    return base.copy(
        shadow = if (this == ShadcnCardVariant.Elevated) {
            SurfaceShadow(color = Color.Black.withAlpha(0.16f), offsetY = 2f.dp, blurRadius = 4f.dp)
        } else {
            null
        },
    )
}
