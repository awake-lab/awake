// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.UiImageVector
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnEmptyDescriptionStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnEmptyTitleStyle
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.icon
import io.github.ronjunevaldoz.awake.ui.headless.padding
import io.github.ronjunevaldoz.awake.ui.headless.text

/**
 * Empty state container component.
 *
 * Matches real shadcn/ui's `empty.tsx` pattern for empty data states.
 */
fun UiScope.shadcnEmpty(
    title: String,
    description: String? = null,
    iconVector: UiImageVector? = null,
    modifier: Modifier = Modifier,
    action: (UiScope.() -> Unit)? = null,
): UiBounds = column(
    modifier = modifier.fillMaxWidth().padding(32f.dp),
    horizontalAlignment = UiAlignment.Horizontal.Center,
    verticalArrangement = Arrangement.spacedBy(16f.dp),
) {
    iconVector?.let { vector ->
        icon(icon = vector, tint = themeValues.colors.mutedForeground)
    }
    text(
        label = title,
        style = shadcnEmptyTitleStyle(themeValues),
    )
    description?.let { desc ->
        text(
            label = desc,
            style = shadcnEmptyDescriptionStyle(themeValues),
        )
    }
    action?.invoke(this)
}
