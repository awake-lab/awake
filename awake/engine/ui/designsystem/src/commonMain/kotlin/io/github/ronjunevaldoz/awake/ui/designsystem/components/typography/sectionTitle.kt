// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.typography

import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text

fun UiScope.shadcnSectionTitle(
    title: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Companion {
        foreground(theme.colors.mutedForeground)
        textSize(theme.typography.label)
    },
): UiBounds = text(title, modifier = modifier, style = style)
