package io.github.ronjunevaldoz.awake.ui.graphics.internal

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiLinearGradient
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.core.graphics.emitPrimitive

internal fun UiScope.gradientRect(
    slot: UiSlot,
    gradient: UiLinearGradient,
    overlay: Boolean = false
) {
    emitPrimitive(
        UiDrawPrimitive.GradientQuad(
            x = slot.x,
            y = slot.y,
            w = slot.width,
            h = slot.height,
            gradient = gradient
        ),
        overlay
    )
}
