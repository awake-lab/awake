// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless.internal.controls

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiImageVector
import io.github.ronjunevaldoz.awake.ui.UiPrimitiveScope
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.fitTo
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.scope.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.strokeToFillPath

fun UiPrimitiveScope.icon(
    imageVector: UiImageVector,
    modifier: UiModifier = Modifier,
    tint: Color = theme.colors.foreground,
    overlay: Boolean = false,
): UiBounds {
    val slot = claimModifiedSlot(
        modifier.withSizeFallback(
            Dimension.Fixed(imageVector.defaultWidth),
            Dimension.Fixed(imageVector.defaultHeight),
        ),
    )
    imageVector.fitTo(slot).forEach { vectorPath ->
        val fillColor = vectorPath.fill ?: tint
        if (fillColor.isTransparent()) return@forEach
        // A stroked glyph (Heroicons' outline tier) has no fill of its own -- convert its
        // centerline+stroke into an equivalent filled outline and draw that through the same
        // FilledPath primitive, so it gets the same AA fringe every other icon path gets.
        val stroke = vectorPath.stroke
        val path = if (stroke != null) vectorPath.path.strokeToFillPath(stroke) else vectorPath.path
        if (overlay) {
            emitOverlay(UiDrawPrimitive.FilledPath(path, fillColor))
        } else {
            emit(UiDrawPrimitive.FilledPath(path, fillColor))
        }
    }
    return slot
}
