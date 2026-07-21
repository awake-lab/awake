// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.unstyled.separator

/** [separator] with Shadcn tokens. */
fun UiScope.awakeShadcnSeparator(
    modifier: UiModifier = UiModifier(),
    width: Dimension = Dimension.FillMax,
    thickness: Dp = 1f.dp
): UiSlot = separator(
    width = width,
    thickness = thickness,
    modifier = modifier,
    color = theme.tokens.border
)
