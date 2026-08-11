// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.Sp
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.unstyled.avatarFallback as primitiveAvatarFallback

/** Neutral initials avatar. Image loading and branded decoration remain outside Headless. */
fun UiScope.avatar(
    id: String,
    initials: String,
    size: Dp,
    textSize: Sp,
    style: SurfaceStyle = SurfaceStyle(),
): Unit = primitive.primitiveAvatarFallback(
    initials = initials,
    modifier = Modifier.width(size).height(size).asPrimitiveModifier(),
    style = style.copy(textSize = textSize).asPrimitiveStyle(),
)

fun RowScope.avatar(
    id: String,
    initials: String,
    size: Dp,
    textSize: Sp,
    modifier: Modifier = Modifier,
    style: SurfaceStyle = SurfaceStyle(),
): Unit = primitive.primitiveAvatarFallback(
    initials = initials,
    modifier = modifier.width(size).height(size).asPrimitiveModifier(),
    style = style.copy(textSize = textSize).asPrimitiveStyle(),
)

fun ColumnScope.avatar(
    id: String,
    initials: String,
    size: Dp,
    textSize: Sp,
    modifier: Modifier = Modifier,
    style: SurfaceStyle = SurfaceStyle(),
): Unit = primitive.primitiveAvatarFallback(
    initials = initials,
    modifier = modifier.width(size).height(size).asPrimitiveModifier(),
    style = style.copy(textSize = textSize).asPrimitiveStyle(),
)

fun BoxScope.avatar(
    id: String,
    initials: String,
    size: Dp,
    textSize: Sp,
    modifier: Modifier = Modifier,
    style: SurfaceStyle = SurfaceStyle(),
): Unit = primitive.primitiveAvatarFallback(
    initials = initials,
    modifier = modifier.width(size).height(size).asPrimitiveModifier(),
    style = style.copy(textSize = textSize).asPrimitiveStyle(),
)
