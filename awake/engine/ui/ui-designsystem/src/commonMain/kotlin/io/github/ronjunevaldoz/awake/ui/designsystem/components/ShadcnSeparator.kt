// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.headless.SeparatorOrientation
import io.github.ronjunevaldoz.awake.ui.headless.separator
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.theme

/** [separator] with Shadcn tokens. Real shadcn's `Separator` has a horizontal/vertical axis
 * (used e.g. between toolbar buttons or sidebar items); [orientation] threads straight through
 * to [io.github.ronjunevaldoz.awake.ui.headless.separator]. */
fun UiScope.shadcnSeparator(
    modifier: UiModifier = Modifier,
    thickness: Dp = 1f.dp,
    orientation: SeparatorOrientation = SeparatorOrientation.Horizontal,
): UiBounds = separator(
    thickness = thickness,
    modifier = modifier,
    color = theme.colors.border,
    orientation = orientation,
)
