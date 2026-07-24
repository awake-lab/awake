// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.layouts.ext

import io.github.ronjunevaldoz.awake.ui.modifier.Dimension
import io.github.ronjunevaldoz.awake.ui.styling.MutableStyleState
import io.github.ronjunevaldoz.awake.ui.styling.Style
import io.github.ronjunevaldoz.awake.ui.UiAlignment
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.childBox
import io.github.ronjunevaldoz.awake.ui.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope

/**
 * Fixed-rect and alignment container.
 * Sizing and content alignment are handled via [modifier].
 */
@Deprecated("use rawBox instead", )
fun BoxScope.box(
    modifier: UiModifier = Modifier,
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
    modifier: UiModifier = Modifier,
    contentAlignment: UiAlignment = UiAlignment.TopStart,
    content: BoxScope.(slot: UiSlot) -> Unit
): UiSlot {
    val slot = claimModifiedSlot(
        defaultWidth = Dimension.FillMax,
        defaultHeight = Dimension.FillMax,
        modifier = modifier
    )
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
