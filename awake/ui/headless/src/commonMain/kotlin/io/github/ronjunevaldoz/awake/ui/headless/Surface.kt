// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.Sp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets
import io.github.ronjunevaldoz.awake.ui.font.FontWeight
import io.github.ronjunevaldoz.awake.ui.layouts.surface as primitiveSurface
import io.github.ronjunevaldoz.awake.ui.style.Style as PrimitiveStyle

/** Neutral visual values for a generic Headless surface. */
data class SurfaceStyle(
    val background: Color? = null,
    val foreground: Color? = null,
    val border: SurfaceBorder? = null,
    val cornerRadius: Dp? = null,
    val contentPadding: UiInsets = UiInsets.Zero,
    val textSize: Sp? = null,
    val lineHeight: Sp? = null,
    val fontWeight: FontWeight? = null,
    val shadow: SurfaceShadow? = null,
    val scrollThumbColor: Color? = null,
)

/** Neutral elevation decoration exposed by Headless without leaking Core's [UiShadow]. */
data class SurfaceShadow(
    val color: Color,
    val offsetX: Dp = Dp(0f),
    val offsetY: Dp = Dp(0f),
    val blurRadius: Dp = Dp(0f),
    val spread: Dp = Dp(0f),
)

/** Optional border decoration for [SurfaceStyle]. */
data class SurfaceBorder(
    val width: Dp,
    val color: Color,
)

internal fun SurfaceStyle.asPrimitiveStyle(): PrimitiveStyle = PrimitiveStyle {
    background?.let(::background)
    foreground?.let(::foreground)
    border?.let { border(it.width, it.color) }
    cornerRadius?.let { radius -> shape(radius) }
    contentPadding(
        contentPadding.start,
        contentPadding.top,
        contentPadding.end,
        contentPadding.bottom
    )
    textSize?.let(::textSize)
    lineHeight?.let(::lineHeight)
    fontWeight?.let(::fontWeight)
    shadow?.let { value ->
        shadow(
            color = value.color,
            offsetX = value.offsetX,
            offsetY = value.offsetY,
            blurRadius = value.blurRadius,
            spread = value.spread,
        )
    }
}

/**
 * Neutral visual values for the interaction states of a Headless surface.
 *
 * The names describe runtime state only. Design-system variants such as primary, outline, and
 * ghost are mapped to these values above Headless rather than becoming part of a widget API.
 */
data class SurfaceVisuals(
    val rest: SurfaceStyle = SurfaceStyle(),
    val hovered: SurfaceStyle? = null,
    val pressed: SurfaceStyle? = null,
    val disabled: SurfaceStyle? = null,
)

internal fun SurfaceVisuals.asPrimitiveStyle(): PrimitiveStyle =
    rest.asPrimitiveStyle() then PrimitiveStyle {
        hovered?.let { visual -> hovered { apply(visual) } }
        pressed?.let { visual -> active { apply(visual) } }
        disabled?.let { visual -> disabled { apply(visual) } }
    }

private fun io.github.ronjunevaldoz.awake.ui.style.StyleScope.apply(surface: SurfaceStyle) {
    surface.background?.let(::background)
    surface.foreground?.let(::foreground)
    surface.border?.let { border(it.width, it.color) }
    surface.cornerRadius?.let(::shape)
    contentPadding(
        surface.contentPadding.start,
        surface.contentPadding.top,
        surface.contentPadding.end,
        surface.contentPadding.bottom,
    )
    surface.textSize?.let(::textSize)
    surface.lineHeight?.let(::lineHeight)
    surface.fontWeight?.let(::fontWeight)
    surface.shadow?.let { value ->
        shadow(
            color = value.color,
            offsetX = value.offsetX,
            offsetY = value.offsetY,
            blurRadius = value.blurRadius,
            spread = value.spread,
        )
    }
}

/**
 * Generic painted container with Headless layout content.
 *
 * The public surface is limited to neutral visual values. Theme resolution, drawing, clipping,
 * semantic recording, and measurement remain inside Core's runtime implementation.
 */
fun UiScope.surface(
    id: String,
    modifier: Modifier = Modifier,
    style: SurfaceStyle = SurfaceStyle(),
    verticalArrangement: Arrangement = Arrangement.Start,
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds = primitive.primitiveSurface(
    id = id,
    modifier = modifier.asPrimitiveModifier(),
    style = style.asPrimitiveStyle(),
    verticalArrangement = verticalArrangement.asPrimitiveArrangement(),
    clipContent = clipContent,
) { slot -> content(asHeadlessScope(), slot) }