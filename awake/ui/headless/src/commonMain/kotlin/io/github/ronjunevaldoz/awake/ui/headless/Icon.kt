// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiImageVector
import io.github.ronjunevaldoz.awake.ui.api.UiIcon
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.icon as primitiveIcon

private fun UiIcon.asVector(): UiImageVector = when (this) {
    is UiImageVector -> this
    else -> error(
        "Unsupported UiIcon implementation: ${this::class}. " +
            "Headless only draws UiImageVector icons.",
    )
}

fun UiScope.icon(
    icon: UiIcon,
    modifier: UiModifier = Modifier,
    tint: Color? = null,
): UiBounds = icon(
    icon = icon.asVector(),
    modifier = modifier,
    tint = tint,
)

fun UiScope.icon(
    icon: UiImageVector,
    modifier: UiModifier = Modifier,
    tint: Color? = null,
): UiBounds = if (tint != null) {
    primitive.primitiveIcon(
        imageVector = icon,
        modifier = modifier,
        tint = tint,
    )
} else {
    primitive.primitiveIcon(
        imageVector = icon,
        modifier = modifier,
    )
}
