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
        // Upstream badge is `px-2 py-0.5 text-xs font-medium`, and this now says the same thing.
        //
        // An older note here claimed the wider padding was compensating for glyph fidelity and
        // that matching upstream made parity worse. That was measured against a reference
        // rendering in a different typeface entirely (the app never set a font -- see 7a82b9a8).
        // With the same Roboto on both sides the padding is simply wrong: 2.5 grid units is 10dp
        // a side against upstream's 8px, which is the whole ~4px per pill that geometry parity
        // reported.
        //
        // fontWeight has no effect on rendering today and is set for intent only. UiFonts.default()
        // packs a single Roboto-Regular face, so UiFont.glyphFor/advanceFor discard the weight
        // (see UiFont.kt) -- Medium, SemiBold and Bold all produce a byte-identical raster, checked
        // by hashing all three. Upstream draws this at 500 and Awake draws 400, which is most of
        // the pixel gap that remains. Closing it needs a second packed face, not a value here.
        contentPadding = UiInsets.grid(horizontal = 2.0, vertical = 0.5),
        textSize = Tw.Text.xs,
        lineHeight = 16f.sp,
        fontWeight = FontWeight.Medium,
    )
}
