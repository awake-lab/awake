// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.typography

import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.input.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.headless.input.text.text
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme

fun UiScope.shadcnMetaText(
    label: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Companion {
        foreground(theme.colors.mutedForeground)
        textSize(theme.typography.caption)
    },
    maxLines: Int = 1,
): UiBounds = text(
    label = label,
    modifier = modifier,
    style = style,
    overflow = UiTextOverflow.Ellipsis,
    maxLines = maxLines,
)
