// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.radio as primitiveRadio
import io.github.ronjunevaldoz.awake.ui.style.Style

fun UiScope.radio(
    id: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: Style = Style { shape(9999f.dp) },
    onClick: () -> Unit = {},
): Boolean {
    val next = primitive.primitiveRadio(
        id = id,
        selected = selected,
        modifier = modifier.asPrimitiveModifier(),
        style = style,
        boxSize = 16f.dp,
        enabled = enabled,
    )
    if (next != selected) onClick()
    return next
}
