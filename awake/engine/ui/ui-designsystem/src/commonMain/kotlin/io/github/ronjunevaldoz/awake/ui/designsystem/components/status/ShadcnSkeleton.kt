// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.status

import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme
import io.github.ronjunevaldoz.awake.ui.unstyled.skeleton
import io.github.ronjunevaldoz.awake.ui.style.*

private fun shadcnSkeletonStyle(theme: UiTheme, style: Style): Style {
    val shadcnTheme = theme.asShadcnTheme()
    return Style {
        background(shadcnTheme.palette.muted)
        shape(shadcnTheme.radii.md)
    } then style
}

/** Real shadcn's `Skeleton`: a muted placeholder block shown while content is loading.
 * Delegates entirely to [skeleton]. */
fun UiScope.shadcnSkeleton(
    id: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty
): Unit = skeleton(id = id, modifier = modifier, style = shadcnSkeletonStyle(theme, style))
