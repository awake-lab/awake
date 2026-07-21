// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.layouts.ext

import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.UiAlignment
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.childBox
import io.github.ronjunevaldoz.awake.ui.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope

/**
 * Fixed-rect and alignment container.
 * Sizing and content alignment are handled via [modifier].
 */
fun BoxScope.box(
    modifier: UiModifier = UiModifier(),
    contentAlignment: UiAlignment = UiAlignment.TopStart,
    content: BoxScope.(slot: UiSlot) -> Unit
): UiSlot {
    return rawBox(
        modifier = modifier,
        contentAlignment = contentAlignment,
        content = content
    )
}

/** [UiScope] version of [box] for top-level usage. */
fun UiScope.rawBox(
    modifier: UiModifier = UiModifier(),
    contentAlignment: UiAlignment = UiAlignment.TopStart,
    content: BoxScope.(slot: UiSlot) -> Unit
): UiSlot {
    val slot = claimModifiedSlot(
        defaultWidth = Dimension.FillMax,
        defaultHeight = Dimension.FillMax,
        modifier = modifier
    )
    childBox(slot, contentAlignment = contentAlignment).content(slot)
    return slot
}
