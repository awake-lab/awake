// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.modifier

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.style.Style

/** Marker effect for the shadcn-compose-style shimmer sweep, drawn by consuming widgets. */
data object UiShimmerEffect : UiGraphicsEffect

/** True when this modifier's graphics layer carries the shadcn shimmer effect. */
val UiModifier.shimmer: Boolean
    get() = graphicsLayer?.has<UiShimmerEffect>() == true

fun UiModifier.styleable(style: Style): UiModifier =
    copy(styleable = (styleable ?: Style.Empty) then style)

fun UiModifier.background(color: Color): UiModifier =
    styleable(Style.Companion { background(color) })

fun UiModifier.border(width: Dp, color: Color? = null): UiModifier = styleable(
    Style.Companion {
        borderWidth(width)
        color?.let { borderColor(it) }
    },
)

fun UiModifier.shape(radius: Dp): UiModifier = styleable(Style.Companion { shape(radius) })
