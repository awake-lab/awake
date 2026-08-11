// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiImageVector
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.unstyled.components.icon as primitiveIcon

fun UiScope.icon(
    imageVector: UiImageVector,
    modifier: Modifier = Modifier,
    tint: Color? = null,
): UiBounds = primitive.primitiveIcon(
    imageVector = imageVector,
    modifier = modifier.asPrimitiveModifier(),
    tint = tint ?: themeValues.colors.foreground,
)

fun BoxScope.icon(
    imageVector: UiImageVector,
    modifier: Modifier = Modifier,
    tint: Color? = null,
): UiBounds {
    val primitiveModifier = modifier.asPrimitiveModifier()
    return if (tint == null) {
        primitive.primitiveIcon(imageVector = imageVector, modifier = primitiveModifier)
    } else {
        primitive.primitiveIcon(imageVector = imageVector, modifier = primitiveModifier, tint = tint)
    }
}
