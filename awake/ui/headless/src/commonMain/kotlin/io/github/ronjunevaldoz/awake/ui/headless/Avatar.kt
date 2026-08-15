// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.Sp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.style.Style

enum class AvatarStatus { Idle, Loading, Loaded, Error }

/** Unstyled avatar container with content slot for image / fallback initials. */
fun UiScope.avatar(
    id: String,
    size: Dp,
    modifier: Modifier = Modifier,
    style: Style = Style.Empty,
    content: ColumnScope.(UiBounds) -> Unit,
): UiBounds = surface(
    id = id,
    modifier = modifier.width(size).height(size),
    style = style,
    content = content,
)

fun UiScope.avatar(
    id: String,
    initials: String,
    size: Dp,
    textSize: Sp,
    modifier: Modifier = Modifier,
    style: Style = Style.Empty,
): UiBounds = surface(
    id = id,
    modifier = modifier.width(size).height(size),
    style = style then Style { textSize(textSize) },
) {
    text(label = initials, modifier = Modifier.fillMaxSize(), style = style then Style { textSize(textSize) }, centered = true)
}
