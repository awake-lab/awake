// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.layouts.ext

import io.github.ronjunevaldoz.awake.ui.modifier.Dimension
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope

/** 
 * A simple empty layout element that reserves space.
 * By default, it has 0 size on the layout axis and fills the cross axis.
 */
fun ColumnScope.spacer(modifier: UiModifier) {
    val width = modifier.width ?: Dimension.FillMax
    val height = modifier.height ?: Dimension.Fixed(0f.dp)
    claimSlot(width, height)
}

fun RowScope.spacer(modifier: UiModifier) {
    val width = modifier.width ?: Dimension.Fixed(0f.dp)
    val height = modifier.height ?: Dimension.FillMax
    claimSlot(width, height)
}
