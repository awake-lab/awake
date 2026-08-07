// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless.components

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiImageVector
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.scope.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.fitTo
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.layout.*

fun UiScope.icon(
    imageVector: UiImageVector,
    modifier: UiModifier = Modifier,
    tint: Color = context.currentTheme.colors.foreground,
    overlay: Boolean = false
): UiBounds {
    val slot = claimModifiedSlot(
        modifier.withSizeFallback(
            Dimension.Fixed(imageVector.defaultWidth),
            Dimension.Fixed(imageVector.defaultHeight)
        )
    )
    imageVector.fitTo(slot).forEach { vectorPath ->
        val fillColor = vectorPath.fill ?: tint
        if (fillColor.isTransparent()) return@forEach
        if (overlay) {
            emitOverlay(UiDrawPrimitive.FilledPath(vectorPath.path, fillColor))
        } else {
            emit(UiDrawPrimitive.FilledPath(vectorPath.path, fillColor))
        }
    }
    return slot
}
