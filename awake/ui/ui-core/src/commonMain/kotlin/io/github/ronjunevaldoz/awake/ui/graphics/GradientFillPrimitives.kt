// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.graphics

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiLinearGradient
import io.github.ronjunevaldoz.awake.ui.UiPrimitiveScope
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds

fun UiPrimitiveScope.gradientRect(
    slot: UiBounds,
    gradient: UiLinearGradient,
    overlay: Boolean = false,
) {
    dispatchPrimitive(
        UiDrawPrimitive.GradientQuad(
            x = slot.x,
            y = slot.y,
            w = slot.width,
            h = slot.height,
            gradient = gradient,
        ),
        overlay,
    )
}
