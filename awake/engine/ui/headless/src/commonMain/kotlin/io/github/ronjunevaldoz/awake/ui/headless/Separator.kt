// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.unstyled.SeparatorOrientation as PrimitiveOrientation
import io.github.ronjunevaldoz.awake.ui.unstyled.separator as primitiveSeparator

enum class UiSeparatorOrientation { Horizontal, Vertical }

/** Neutral separator primitive; skins provide the token color. */
fun UiScope.separator(
    modifier: Modifier = Modifier,
    thickness: Dp = 1f.dp,
    orientation: UiSeparatorOrientation = UiSeparatorOrientation.Horizontal,
    color: Color? = null,
): UiBounds = primitive.primitiveSeparator(
    thickness = thickness,
    modifier = modifier.asPrimitiveModifier(),
    color = color ?: themeValues.colors.border,
    orientation = when (orientation) {
        UiSeparatorOrientation.Horizontal -> PrimitiveOrientation.Horizontal
        UiSeparatorOrientation.Vertical -> PrimitiveOrientation.Vertical
    },
)

fun ColumnScope.separator(
    modifier: Modifier = Modifier,
    thickness: Dp = 1f.dp,
    orientation: UiSeparatorOrientation = UiSeparatorOrientation.Horizontal,
    color: Color? = null,
): UiBounds = primitive.primitiveSeparator(
    thickness = thickness,
    modifier = modifier.asPrimitiveModifier(),
    color = color ?: themeValues.colors.border,
    orientation = when (orientation) {
        UiSeparatorOrientation.Horizontal -> PrimitiveOrientation.Horizontal
        UiSeparatorOrientation.Vertical -> PrimitiveOrientation.Vertical
    },
)

fun RowScope.separator(
    modifier: Modifier = Modifier,
    thickness: Dp = 1f.dp,
    orientation: UiSeparatorOrientation = UiSeparatorOrientation.Horizontal,
    color: Color? = null,
): UiBounds = primitive.primitiveSeparator(
    thickness = thickness,
    modifier = modifier.asPrimitiveModifier(),
    color = color ?: themeValues.colors.border,
    orientation = when (orientation) {
        UiSeparatorOrientation.Horizontal -> PrimitiveOrientation.Horizontal
        UiSeparatorOrientation.Vertical -> PrimitiveOrientation.Vertical
    },
)
