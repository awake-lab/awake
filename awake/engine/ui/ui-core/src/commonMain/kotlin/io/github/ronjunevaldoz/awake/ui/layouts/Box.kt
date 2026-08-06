// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.layouts

import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.childBox
import io.github.ronjunevaldoz.awake.ui.scope.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

/**
 * Fixed-rect and alignment container.
 * Sizing and content alignment are handled via [modifier].
 */
fun UiScope.box(
    modifier: UiModifier = Modifier,
    contentAlignment: UiAlignment = UiAlignment.TopStart,
    content: BoxScope.(slot: UiBounds) -> Unit
): UiBounds {
    val slot = claimModifiedSlot(modifier.withSizeFallback(Dimension.FillMax, Dimension.FillMax))
    val styleState = MutableStyleState(
        hovered = modifier.forceHover ?: hitTest(slot),
        active = modifier.forceActive ?: false,
        focused = modifier.forceFocus ?: false
    )
    val textStyle = (modifier.styleable ?: Style.Empty).resolve(styleState, context.currentTextStyle).textStyle

    context.pushTextStyle(textStyle)
    val scope = childBox(slot, contentAlignment = contentAlignment)
    // Matches UiScope.row()/column()/surface()'s own withMeasuredRecordingSuppressed { scope.
    // content(slot) } -- box() is just as much a composite widget as those, so its children's
    // claimSlot() calls must not leak into an ancestor row/column's in-progress measuredSlots/
    // measuredWeights trial when this box sits next to (or, as here, itself IS) a weight()-
    // tagged sibling that forces the ancestor into its trial-measurement path. Without this, a
    // box with real content (e.g. a stats badge sharing a box with the demo's own viewport
    // content) desyncs resolveWeightedMainAxis()'s index pairing and the ancestor's slot
    // consumption order, corrupting sibling layout the same way the pre-fix surface() bug did.
    context.withMeasuredRecordingSuppressed { scope.content(slot) }
    context.popTextStyle()
    return slot
}
