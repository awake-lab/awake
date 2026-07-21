// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.layouts.ext

import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope

fun ColumnScope.spacer(modifier: UiModifier) {
    claimSlot(modifier.width ?: Dimension.FillMax, modifier.height ?: Dimension.FillMax)
}

fun RowScope.spacer(modifier: UiModifier) {
    claimSlot(modifier.width ?: Dimension.FillMax, modifier.height ?: Dimension.FillMax)
}
