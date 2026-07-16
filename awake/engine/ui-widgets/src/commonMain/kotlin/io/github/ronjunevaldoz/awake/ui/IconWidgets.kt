// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color

fun UiScope.icon(
    imageVector: UiImageVector,
    modifier: UiModifier = UiModifier(),
    width: Dimension = Dimension.Fixed(imageVector.defaultWidth),
    height: Dimension = Dimension.Fixed(imageVector.defaultHeight),
    tint: Color = theme.tokens.foreground,
    overlay: Boolean = false
): UiSlot {
    val slot = claimModifiedSlot(width, height, modifier)
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
