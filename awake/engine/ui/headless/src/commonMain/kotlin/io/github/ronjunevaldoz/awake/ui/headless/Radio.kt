// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.unstyled.input.selection.checkbox as primitiveCheckbox

fun UiScope.radio(
    id: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    visuals: SurfaceStyle = SurfaceStyle(cornerRadius = 9999f.dp),
    onClick: () -> Unit = {},
): Boolean {
    val next = primitive.primitiveCheckbox(
        id = id,
        checked = selected,
        modifier = modifier.asPrimitiveModifier(),
        style = visuals.asPrimitiveStyle(),
        boxSize = 16f.dp,
        enabled = enabled,
    )
    if (next != selected) onClick()
    return next
}

fun ColumnScope.radio(
    id: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    visuals: SurfaceStyle = SurfaceStyle(cornerRadius = 9999f.dp),
    onClick: () -> Unit = {},
): Boolean {
    val next = primitive.primitiveCheckbox(
        id = id,
        checked = selected,
        modifier = modifier.asPrimitiveModifier(),
        style = visuals.asPrimitiveStyle(),
        boxSize = 16f.dp,
        enabled = enabled,
    )
    if (next != selected) onClick()
    return next
}

fun RowScope.radio(
    id: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    visuals: SurfaceStyle = SurfaceStyle(cornerRadius = 9999f.dp),
    onClick: () -> Unit = {},
): Boolean {
    val next = primitive.primitiveCheckbox(
        id = id,
        checked = selected,
        modifier = modifier.asPrimitiveModifier(),
        style = visuals.asPrimitiveStyle(),
        boxSize = 16f.dp,
        enabled = enabled,
    )
    if (next != selected) onClick()
    return next
}
