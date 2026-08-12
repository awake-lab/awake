// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiImageVector
import io.github.ronjunevaldoz.awake.ui.api.UiIcon
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.headless.internal.components.icon as primitiveIcon

private fun UiIcon.asVector(): UiImageVector = this as UiImageVector

fun UiScope.icon(
    icon: UiIcon,
    modifier: Modifier = Modifier,
    tint: Color? = null,
): UiBounds = icon(
    icon = icon.asVector(),
    modifier = modifier,
    tint = tint
)

fun UiScope.icon(
    icon: UiImageVector,
    modifier: Modifier = Modifier,
    tint: Color? = null,
): UiBounds = primitive.primitiveIcon(
    imageVector = icon,
    modifier = modifier.asPrimitiveModifier(),
    tint = tint ?: themeValues.colors.foreground,
)