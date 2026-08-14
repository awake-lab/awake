// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds

enum class UiSeparatorOrientation { Horizontal, Vertical }

/** Neutral separator primitive using unstyled surface composition with 0 corner radius. */
fun UiScope.separator(
    modifier: Modifier = Modifier,
    thickness: Dp = 1f.dp,
    orientation: UiSeparatorOrientation = UiSeparatorOrientation.Horizontal,
    color: Color? = null,
    // Defaults to an orientation-derived id, which collides as soon as one frame draws two
    // horizontal separators. Callers that repeat separators must pass their own.
    id: String? = null,
): UiBounds {
    val sepColor = color ?: themeValues.colors.border
    val sepModifier = when (orientation) {
        UiSeparatorOrientation.Horizontal -> modifier.fillMaxWidth().height(thickness)
        UiSeparatorOrientation.Vertical -> modifier.fillMaxHeight().width(thickness)
    }
    return surface(
        id = id ?: "separator.${orientation.name}",
        modifier = sepModifier,
        style = SurfaceStyle(background = sepColor, cornerRadius = 0.dp),
    ) { _ -> }
}
