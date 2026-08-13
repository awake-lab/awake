// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.childBox
import io.github.ronjunevaldoz.awake.ui.headless.button as primitiveButton
import io.github.ronjunevaldoz.awake.ui.headless.buttonSlot as primitiveButtonSlot

/** Headless button slot result containing click state and resolved bounds. */
data class HeadlessButtonResult(val clicked: Boolean, val slot: UiBounds)

/**
 * Generic unstyled interactive button.
 *
 * Interaction state, focus, semantic output, and layout bounds are managed internally.
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

fun UiScope.buttonSlot(
    id: String,
    label: String? = null,
    modifier: Modifier = Modifier,
    visuals: SurfaceVisuals = SurfaceVisuals(),
    enabled: Boolean = true,
): HeadlessButtonResult {
    val result = primitive.primitiveButtonSlot(
        id = id,
        label = label,
        modifier = modifier.asPrimitiveModifier(),
        style = visuals.asPrimitiveStyle(),
        enabled = enabled,
    )
    return HeadlessButtonResult(result.clicked, result.slot)
}

fun UiScope.buttonSlot(
    id: String,
    modifier: Modifier = Modifier,
    visuals: SurfaceVisuals = SurfaceVisuals(),
    enabled: Boolean = true,
    content: BoxScope.(slot: UiBounds) -> Unit,
): HeadlessButtonResult {
    val result = primitive.primitiveButtonSlot(
        id = id,
        modifier = modifier.asPrimitiveModifier(),
        style = visuals.asPrimitiveStyle(),
        enabled = enabled,
    ) { slot ->
        BoxScope(primitive.childBox(slot)).content(slot)
    }
    return HeadlessButtonResult(result.clicked, result.slot)
}
