// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.unstyled.SeparatorOrientation
import io.github.ronjunevaldoz.awake.ui.unstyled.separator
import io.github.ronjunevaldoz.awake.ui.headless.UiScope as HeadlessUiScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier as HeadlessModifier
import io.github.ronjunevaldoz.awake.ui.headless.UiSeparatorOrientation
import io.github.ronjunevaldoz.awake.ui.headless.separator as headlessSeparator

/** [separator] with Shadcn tokens. Real shadcn's `Separator` has a horizontal/vertical axis
 * (used e.g. between toolbar buttons or sidebar items); [orientation] threads straight through
 * to [separator]. */
@Deprecated(
    message = "Use the Headless UiScope overload. This Core receiver is retained only for migration.",
)
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

/** Headless-facade separator recipe; border tokens resolve from the active named theme. */
fun HeadlessUiScope.shadcnSeparator(
    modifier: HeadlessModifier = HeadlessModifier,
    thickness: Dp = 1f.dp,
    orientation: UiSeparatorOrientation = UiSeparatorOrientation.Horizontal,
): UiBounds = headlessSeparator(
    modifier = modifier,
    thickness = thickness,
    orientation = orientation,
    color = themeValues.colors.border,
)
