// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.Sp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets
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
    cornerRadius?.let(::shape)
    contentPadding(contentPadding.start, contentPadding.top, contentPadding.end, contentPadding.bottom)
    textSize?.let(::textSize)
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

fun ColumnScope.surface(
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

fun RowScope.surface(
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

fun BoxScope.surface(
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
