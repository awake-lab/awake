// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.layouts

import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.childBox
import io.github.ronjunevaldoz.awake.ui.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

/**
 * Fixed-rect and alignment container.
 * Sizing and content alignment are handled via [modifier].
 */
fun UiScope.box(
    modifier: UiModifier = Modifier,
    contentAlignment: UiAlignment = UiAlignment.TopStart,
    content: BoxScope.(slot: UiSlot) -> Unit
): UiSlot {
    val slot = claimModifiedSlot(modifier.withSizeFallback(Dimension.FillMax, Dimension.FillMax))
    val styleState = MutableStyleState(
        hovered = modifier.forceHover ?: hitTest(slot),
        active = modifier.forceActive ?: false,
        focused = modifier.forceFocus ?: false
    )
    val textStyle = (modifier.styleable ?: Style.Empty).resolve(styleState, context.currentTextStyle).textStyle

    context.pushTextStyle(textStyle)
    val scope = childBox(slot, contentAlignment = contentAlignment)
    scope.content(slot)
    context.popTextStyle()
    return slot
}
