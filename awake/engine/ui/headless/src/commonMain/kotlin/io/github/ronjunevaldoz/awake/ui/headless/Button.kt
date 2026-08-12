// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.childBox
import io.github.ronjunevaldoz.awake.ui.headless.button as primitiveButton
import io.github.ronjunevaldoz.awake.ui.headless.buttonSlot as primitiveButtonSlot

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
    centered: Boolean = true,
    enabled: Boolean = true,
    semanticRole: UiSemanticRole = UiSemanticRole.Button,
): Boolean = primitive.primitiveButton(
    id = id,
    label = label,
    modifier = modifier.asPrimitiveModifier(),
    style = visuals.asPrimitiveStyle(),
    radius = 0.dp,
    centered = centered,
    enabled = enabled,
    semanticRole = semanticRole,
)

fun ColumnScope.button(
    id: String,
    label: String? = null,
    modifier: Modifier = Modifier,
    visuals: SurfaceVisuals = SurfaceVisuals(),
    centered: Boolean = true,
    enabled: Boolean = true,
    semanticRole: UiSemanticRole = UiSemanticRole.Button,
): Boolean = primitive.primitiveButton(
    id = id,
    label = label,
    modifier = modifier.asPrimitiveModifier(),
    style = visuals.asPrimitiveStyle(),
    radius = 0.dp,
    centered = centered,
    enabled = enabled,
    semanticRole = semanticRole,
)

fun RowScope.button(
    id: String,
    label: String? = null,
    modifier: Modifier = Modifier,
    visuals: SurfaceVisuals = SurfaceVisuals(),
    centered: Boolean = true,
    enabled: Boolean = true,
    semanticRole: UiSemanticRole = UiSemanticRole.Button,
): Boolean = primitive.primitiveButton(
    id = id,
    label = label,
    modifier = modifier.asPrimitiveModifier(),
    style = visuals.asPrimitiveStyle(),
    radius = 0.dp,
    centered = centered,
    enabled = enabled,
    semanticRole = semanticRole,
)

fun BoxScope.button(
    id: String,
    label: String? = null,
    modifier: Modifier = Modifier,
    visuals: SurfaceVisuals = SurfaceVisuals(),
    centered: Boolean = true,
    enabled: Boolean = true,
    semanticRole: UiSemanticRole = UiSemanticRole.Button,
): Boolean = primitive.primitiveButton(
    id = id,
    label = label,
    modifier = modifier.asPrimitiveModifier(),
    style = visuals.asPrimitiveStyle(),
    radius = 0.dp,
    centered = centered,
    enabled = enabled,
    semanticRole = semanticRole,
)

fun AbsoluteScope.button(
    id: String,
    label: String? = null,
    modifier: Modifier = Modifier,
    visuals: SurfaceVisuals = SurfaceVisuals(),
    centered: Boolean = true,
    enabled: Boolean = true,
    semanticRole: UiSemanticRole = UiSemanticRole.Button,
): Boolean = primitive.primitiveButton(
    id = id,
    label = label,
    modifier = modifier.asPrimitiveModifier(),
    style = visuals.asPrimitiveStyle(),
    radius = 0.dp,
    centered = centered,
    enabled = enabled,
    semanticRole = semanticRole,
)

fun UiScope.button(
    id: String,
    modifier: Modifier = Modifier,
    visuals: SurfaceVisuals = SurfaceVisuals(),
    enabled: Boolean = true,
    semanticRole: UiSemanticRole = UiSemanticRole.Button,
    content: BoxScope.(slot: UiBounds) -> Unit,
): Boolean = primitive.primitiveButtonSlot(
    id = id,
    modifier = modifier.asPrimitiveModifier(),
    style = visuals.asPrimitiveStyle(),
    enabled = enabled,
    semanticRole = semanticRole,
) { slot ->
    BoxScope(primitive.childBox(slot)).content(slot)
}.clicked
