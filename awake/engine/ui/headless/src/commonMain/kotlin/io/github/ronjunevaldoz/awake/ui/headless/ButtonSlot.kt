// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.headless.buttonSlot as primitiveButtonSlot

/** Headless button slot result for anchoring menus and popups. */
data class HeadlessButtonResult(val clicked: Boolean, val slot: UiBounds)

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

fun ColumnScope.buttonSlot(
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

fun RowScope.buttonSlot(
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
