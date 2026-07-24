// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.unstyled.components

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.modifier.Dimension
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiImageVector
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.fitTo
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier

fun UiScope.icon(
    imageVector: UiImageVector,
    modifier: UiModifier = Modifier,
    width: Dimension = Dimension.Fixed(imageVector.defaultWidth),
    height: Dimension = Dimension.Fixed(imageVector.defaultHeight),
    tint: Color = context.currentTheme.tokens.foreground,
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
