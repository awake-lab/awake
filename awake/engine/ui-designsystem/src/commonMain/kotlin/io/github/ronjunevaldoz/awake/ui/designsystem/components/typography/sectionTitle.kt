package io.github.ronjunevaldoz.awake.ui.designsystem.components.typography

import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text

fun UiScope.sectionTitle(
    title: String,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Companion {
        foreground(theme.tokens.mutedForeground)
        textSize(theme.typography.label)
    }
): UiSlot = text(title, modifier = modifier, style = style)
