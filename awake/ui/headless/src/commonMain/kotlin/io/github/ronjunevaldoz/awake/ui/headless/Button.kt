// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.childBox
import io.github.ronjunevaldoz.awake.ui.headless.button as primitiveButton
import io.github.ronjunevaldoz.awake.ui.headless.buttonSlot as primitiveButtonSlot
import io.github.ronjunevaldoz.awake.ui.style.Style

/** @deprecated Design-system recipes must migrate to callback/content composition. */
@Deprecated("Internal layout detail; use button(..., onClick = ...) instead")
data class HeadlessButtonResult(val clicked: Boolean, val slot: UiBounds)

/** Style-native button API. State rules belong in [style], not in a parallel visual DTO. */
fun UiScope.button(
    id: String,
    label: String? = null,
    modifier: Modifier = Modifier,
    style: Style,
    centered: Boolean = true,
    enabled: Boolean = true,
    semanticRole: UiSemanticRole = UiSemanticRole.Button,
): Boolean = primitive.primitiveButton(
    id = id,
    label = label,
    modifier = modifier.asPrimitiveModifier(),
    style = style,
    radius = 0.dp,
    centered = centered,
    enabled = enabled,
    semanticRole = semanticRole,
)

/** Callback-oriented button API for application composition. */
fun UiScope.button(
    id: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: Style = Style.Empty,
    centered: Boolean = true,
    enabled: Boolean = true,
    semanticRole: UiSemanticRole = UiSemanticRole.Button,
) {
    if (
        primitive.primitiveButton(
            id = id,
            label = label,
            modifier = modifier.asPrimitiveModifier(),
            style = style,
            radius = 0.dp,
            centered = centered,
            enabled = enabled,
            semanticRole = semanticRole,
        )
    ) onClick()
}

/** Slot variant of the Style-native button API. */
fun UiScope.button(
    id: String,
    modifier: Modifier = Modifier,
    style: Style,
    enabled: Boolean = true,
    semanticRole: UiSemanticRole = UiSemanticRole.Button,
    content: BoxScope.(slot: UiBounds) -> Unit,
): Boolean = primitive.primitiveButtonSlot(
    id = id,
    modifier = modifier.asPrimitiveModifier(),
    style = style,
    enabled = enabled,
    semanticRole = semanticRole,
) { slot ->
    BoxScope(primitive.childBox(slot)).content(slot)
}.clicked

/** Compatibility bridge for recipes that still require a button's resolved bounds. */
@Deprecated("Internal layout detail; use button(..., onClick = ...) instead")
fun UiScope.buttonSlot(
    id: String,
    label: String? = null,
    modifier: Modifier = Modifier,
    style: Style,
    enabled: Boolean = true,
): HeadlessButtonResult {
    val result = primitive.primitiveButtonSlot(id, label, modifier.asPrimitiveModifier(), style, enabled = enabled)
    return HeadlessButtonResult(result.clicked, result.slot)
}

/** Compatibility bridge for recipes that still require a button's resolved bounds. */
@Deprecated("Internal layout detail; use button(..., onClick = ...) instead")
fun UiScope.buttonSlot(
    id: String,
    modifier: Modifier = Modifier,
    style: Style,
    enabled: Boolean = true,
    content: BoxScope.(slot: UiBounds) -> Unit,
): HeadlessButtonResult {
    val result = primitive.primitiveButtonSlot(id, modifier.asPrimitiveModifier(), style, enabled = enabled) { slot ->
        BoxScope(primitive.childBox(slot)).content(slot)
    }
    return HeadlessButtonResult(result.clicked, result.slot)
}

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

/** Compatibility bridge for legacy visual DTO callers. */
@Deprecated("Internal layout detail; use button(..., onClick = ...) instead")
fun UiScope.buttonSlot(
    id: String,
    label: String? = null,
    modifier: Modifier = Modifier,
    visuals: SurfaceVisuals = SurfaceVisuals(),
    enabled: Boolean = true,
): HeadlessButtonResult {
    val result = primitive.primitiveButtonSlot(
        id, label, modifier.asPrimitiveModifier(), visuals.asPrimitiveStyle(), enabled = enabled,
    )
    return HeadlessButtonResult(result.clicked, result.slot)
}

/** Compatibility bridge for legacy visual DTO callers. */
@Deprecated("Internal layout detail; use button(..., onClick = ...) instead")
fun UiScope.buttonSlot(
    id: String,
    modifier: Modifier = Modifier,
    visuals: SurfaceVisuals = SurfaceVisuals(),
    enabled: Boolean = true,
    content: BoxScope.(slot: UiBounds) -> Unit,
): HeadlessButtonResult {
    val result = primitive.primitiveButtonSlot(
        id, modifier.asPrimitiveModifier(), visuals.asPrimitiveStyle(), enabled = enabled,
    ) { slot -> BoxScope(primitive.childBox(slot)).content(slot) }
    return HeadlessButtonResult(result.clicked, result.slot)
}
