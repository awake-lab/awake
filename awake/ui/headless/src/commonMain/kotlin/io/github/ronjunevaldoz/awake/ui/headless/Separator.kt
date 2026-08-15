// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme

enum class UiSeparatorOrientation { Horizontal, Vertical }

/** Neutral separator primitive. Theme component defaults provide its fallback [Style]. */
fun UiScope.separator(
    modifier: Modifier = Modifier,
    thickness: Dp = 1f.dp,
    orientation: UiSeparatorOrientation = UiSeparatorOrientation.Horizontal,
    style: Style = primitive.theme.components.separator,
    // Defaults to an orientation-derived id, which collides as soon as one frame draws two
    // horizontal separators. Callers that repeat separators must pass their own.
    id: String? = null,
): UiBounds {
    val sepModifier = when (orientation) {
        UiSeparatorOrientation.Horizontal -> modifier.fillMaxWidth().height(thickness)
        UiSeparatorOrientation.Vertical -> modifier.fillMaxHeight().width(thickness)
    }
    return surface(
        id = id ?: "separator.${orientation.name}",
        modifier = sepModifier,
        style = style,
    ) { _ -> }
}

/** Compatibility bridge for callers that have not moved their separator colour into [Style]. */
@Deprecated("Use style = Style { background(color) }")
fun UiScope.separator(
    modifier: Modifier = Modifier,
    thickness: Dp = 1f.dp,
    orientation: UiSeparatorOrientation = UiSeparatorOrientation.Horizontal,
    color: Color,
    id: String? = null,
): UiBounds = separator(
    modifier = modifier,
    thickness = thickness,
    orientation = orientation,
    style = Style { background(color); shape(UiShape.none) },
    id = id,
)
