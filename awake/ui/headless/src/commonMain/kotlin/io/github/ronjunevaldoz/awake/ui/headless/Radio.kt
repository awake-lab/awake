// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.radio as primitiveRadio
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.style.Style

fun UiScope.radio(
    id: String,
    selected: Boolean,
    modifier: UiModifier = Modifier,
    enabled: Boolean = true,
    // No implicit fallback -- the primitive already paints radio's indicator circular
    // unconditionally (UiShapeSpec.Circle), so this shape rule was dead weight, not a real
    // default; omission resolves to Style.Empty like every other widget.
    style: Style = Style.Empty,
    onClick: () -> Unit = {},
): Boolean {
    val next = primitive.primitiveRadio(
        id = id,
        selected = selected,
        modifier = modifier,
        style = style,
        boxSize = 16f.dp,
        enabled = enabled,
    )
    if (next != selected) onClick()
    return next
}
