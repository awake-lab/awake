// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.headless.button as primitiveButton

/**
 * Generic interactive button with a plain text label.
 *
 * Interaction state, focus, semantic output, and painting remain inside Headless/Core internals.
 * Callers provide neutral state visuals through [SurfaceVisuals].
 */
fun UiScope.button(
    id: String,
    label: String? = null,
    modifier: Modifier = Modifier,
    visuals: SurfaceVisuals = SurfaceVisuals(),
    enabled: Boolean = true,
): Boolean = primitive.primitiveButton(
    id = id,
    label = label,
    modifier = modifier.asPrimitiveModifier(),
    style = visuals.asPrimitiveStyle(),
    radius = 0.dp,
    enabled = enabled,
)

fun ColumnScope.button(
    id: String,
    label: String? = null,
    modifier: Modifier = Modifier,
    visuals: SurfaceVisuals = SurfaceVisuals(),
    enabled: Boolean = true,
): Boolean = primitive.primitiveButton(
    id = id,
    label = label,
    modifier = modifier.asPrimitiveModifier(),
    style = visuals.asPrimitiveStyle(),
    radius = 0.dp,
    enabled = enabled,
)

fun RowScope.button(
    id: String,
    label: String? = null,
    modifier: Modifier = Modifier,
    visuals: SurfaceVisuals = SurfaceVisuals(),
    enabled: Boolean = true,
): Boolean = primitive.primitiveButton(
    id = id,
    label = label,
    modifier = modifier.asPrimitiveModifier(),
    style = visuals.asPrimitiveStyle(),
    radius = 0.dp,
    enabled = enabled,
)
