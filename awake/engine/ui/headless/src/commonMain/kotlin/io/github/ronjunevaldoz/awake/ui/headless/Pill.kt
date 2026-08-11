// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layouts.surface as primitiveSurface
import io.github.ronjunevaldoz.awake.ui.unstyled.withIntrinsicLabelWidth

/** Neutral compact surface for badges, labels, and key-cap-like content. */
fun UiScope.pill(
    id: String,
    label: String,
    modifier: Modifier = Modifier,
    style: SurfaceStyle = SurfaceStyle(),
    textColor: Color? = style.foreground,
): UiBounds = surface(
    id = id,
    modifier = modifier,
    style = style,
    clipContent = true,
) {
    text(label = label, color = textColor, centered = true)
}

fun ColumnScope.pill(
    id: String,
    label: String,
    modifier: Modifier = Modifier,
    style: SurfaceStyle = SurfaceStyle(),
    textColor: Color? = style.foreground,
): UiBounds = primitive.primitiveSurface(
    id = id,
    modifier = primitive.withIntrinsicLabelWidth(
        modifier = modifier.asPrimitiveModifier(),
        label = label,
        style = style.asPrimitiveStyle(),
    ),
    style = style.asPrimitiveStyle(),
    clipContent = true,
) { slot ->
    text(label = label, color = textColor, centered = true)
}

fun RowScope.pill(
    id: String,
    label: String,
    modifier: Modifier = Modifier,
    style: SurfaceStyle = SurfaceStyle(),
    textColor: Color? = style.foreground,
): UiBounds = surface(id, modifier, style, clipContent = true) {
    text(label = label, color = textColor, centered = true)
}
