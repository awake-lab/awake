// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.Sp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxSize
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.style.Style

enum class AvatarStatus { Idle, Loading, Loaded, Error }

/** Unstyled avatar container with content slot for image / fallback initials. */
fun UiScope.avatar(
    id: String,
    size: Dp,
    modifier: UiModifier = Modifier,
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
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
): UiBounds = surface(
    id = id,
    modifier = modifier.width(size).height(size),
    style = style then Style { textSize(textSize) },
) {
    // Typography only. Passing the container [style] down re-applied its background and full
    // corner radius to the label, painting a second filled circle behind the initials inside
    // the avatar's own circle. The colour still arrives: surface() propagates its resolved
    // foreground to children as the ambient text style.
    text(
        label = initials,
        modifier = Modifier.fillMaxSize(),
        style = Style { textSize(textSize) },
        centered = true,
    )
}
