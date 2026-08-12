// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless.internal.controls

import io.github.ronjunevaldoz.awake.ui.headless.internal.layout.interact
import io.github.ronjunevaldoz.awake.ui.headless.internal.layout.UiInteraction
import io.github.ronjunevaldoz.awake.ui.headless.internal.layout.withIntrinsicLabelWidth

import io.github.ronjunevaldoz.awake.ui.graphics.animation.animatedHeight
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier

/**
 * A clickable trigger toggles [expanded] content with an animated height transition -- what a
 * Collapsible IS, independent of skin. [trigger] renders the *entire* trigger (not just a
 * header label plugged into a fixed internal button), so a caller can use any interactive
 * widget as the trigger by calling `toggle()` from its own click handler.
 */
fun ColumnScope.collapsible(
    id: String,
    expanded: Boolean,
    modifier: UiModifier = Modifier,
    onExpandedChange: (Boolean) -> Unit = {},
    trigger: ColumnScope.(isOpen: Boolean, toggle: () -> Unit) -> Unit,
    content: ColumnScope.() -> Unit,
): Boolean {
    trigger(expanded) { onExpandedChange(!expanded) }

    animatedHeight(id = "$id.content", expanded = expanded) {
        content()
    }

    return expanded
}
