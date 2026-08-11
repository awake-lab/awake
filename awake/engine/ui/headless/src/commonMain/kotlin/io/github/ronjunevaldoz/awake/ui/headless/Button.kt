// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.headless.UiButtonVariant as PrimitiveButtonVariant
import io.github.ronjunevaldoz.awake.ui.headless.button as primitiveButton

/** Generic button paint behavior, independent of a design-system vocabulary. */
enum class ButtonVariant {
    Filled,
    Outline,
    Ghost,
}

private fun ButtonVariant.asPrimitiveVariant(): PrimitiveButtonVariant = when (this) {
    ButtonVariant.Filled -> PrimitiveButtonVariant.Filled
    ButtonVariant.Outline -> PrimitiveButtonVariant.Outline
    ButtonVariant.Ghost -> PrimitiveButtonVariant.Ghost
}

/**
 * Generic interactive button with a plain text label.
 *
 * Interaction state, focus, semantic output, and painting remain inside Headless/Core internals.
 * Callers provide neutral visual values through [SurfaceStyle].
 */
fun UiScope.button(
    id: String,
    label: String? = null,
    modifier: Modifier = Modifier,
    style: SurfaceStyle = SurfaceStyle(),
    variant: ButtonVariant = ButtonVariant.Filled,
    enabled: Boolean = true,
): Boolean = primitive.primitiveButton(
    id = id,
    label = label,
    modifier = modifier.asPrimitiveModifier(),
    style = style.asPrimitiveStyle(),
    variant = variant.asPrimitiveVariant(),
    radius = 0.dp,
    enabled = enabled,
)

fun ColumnScope.button(
    id: String,
    label: String? = null,
    modifier: Modifier = Modifier,
    style: SurfaceStyle = SurfaceStyle(),
    variant: ButtonVariant = ButtonVariant.Filled,
    enabled: Boolean = true,
): Boolean = primitive.primitiveButton(
    id = id,
    label = label,
    modifier = modifier.asPrimitiveModifier(),
    style = style.asPrimitiveStyle(),
    variant = variant.asPrimitiveVariant(),
    radius = 0.dp,
    enabled = enabled,
)
