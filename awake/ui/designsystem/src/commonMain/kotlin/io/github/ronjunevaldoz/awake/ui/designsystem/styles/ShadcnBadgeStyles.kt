// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.styles

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets
import io.github.ronjunevaldoz.awake.ui.api.sp
import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.font.FontWeight
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceBorder
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle
import io.github.ronjunevaldoz.awake.ui.tailwind.Tw
import io.github.ronjunevaldoz.awake.ui.tailwind.grid

/**
 * Resolves [SurfaceStyle] for a [ShadcnBadgeVariant].
 */
fun ShadcnBadgeVariant.style(values: UiThemeValues): SurfaceStyle {
    val colors = values.colors
    val base = when (this) {
        ShadcnBadgeVariant.Primary -> SurfaceStyle(
            background = colors.primary,
            foreground = colors.primaryForeground,
            border = SurfaceBorder(1f.dp, Color.Transparent),
        )

        ShadcnBadgeVariant.Secondary -> SurfaceStyle(
            background = colors.secondary,
            foreground = colors.secondaryForeground,
            border = SurfaceBorder(1f.dp, Color.Transparent),
        )

        ShadcnBadgeVariant.Outline -> SurfaceStyle(
            background = Color.Transparent,
            foreground = colors.foreground,
            border = SurfaceBorder(1f.dp, colors.border),
        )

        ShadcnBadgeVariant.Danger -> SurfaceStyle(
            background = colors.destructive,
            foreground = Color.White,
            border = SurfaceBorder(1f.dp, Color.Transparent),
        )

        ShadcnBadgeVariant.Ghost -> SurfaceStyle(
            background = Color.Transparent,
            foreground = colors.foreground,
            border = SurfaceBorder(1f.dp, Color.Transparent),
        )
    }
    return base.copy(
        cornerRadius = values.shapes.full,
        // Upstream badge is `px-2 py-0.5 text-xs font-medium`, but matching it literally moves
        // badge-local-light from 27.3% to 34.8% against the real reference: the extra padding and
        // weight are compensating for glyph advance/stem fidelity, not styling drift. Revisit
        // together with the atlas, not on its own.
        contentPadding = UiInsets.grid(horizontal = 2.0, vertical = 0.5),
        textSize = Tw.Text.xs,
        lineHeight = 16f.sp,
        fontWeight = FontWeight.SemiBold,
    )
}
