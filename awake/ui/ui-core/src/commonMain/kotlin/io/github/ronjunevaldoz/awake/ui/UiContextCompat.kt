// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.layout.place
import io.github.ronjunevaldoz.awake.ui.layouts.resolveAgainst
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier

/** [resolveRootSlot] backs the root layout entry points in `layouts/RootLayouts.kt`
 * (`UiContext.column`/`row`/`box`/`absolute`) -- resolves a [UiModifier]'s requested
 * width/height/alignment/offset against the frame into a concrete root [UiBounds]. */
internal fun UiContext.resolveRootSlot(
    modifier: UiModifier,
    defaultWidth: Dimension = Dimension.FillMax,
    defaultHeight: Dimension = Dimension.FillMax,
): UiBounds {
    val frame = frameBoundsInternal()
    val requestedWidth = modifier.widthDimension ?: defaultWidth
    val requestedHeight = modifier.heightDimension ?: defaultHeight
    val width = requestedWidth.resolveAgainst(frame.width)
    val height = requestedHeight.resolveAgainst(frame.height)
    return frame.place(
        width = width,
        height = height,
        alignment = modifier.alignment ?: UiAlignment.TopStart,
        insets = modifier.insets,
        offsetX = modifier.offsetX.toPx(),
        offsetY = modifier.offsetY.toPx(),
    )
}
