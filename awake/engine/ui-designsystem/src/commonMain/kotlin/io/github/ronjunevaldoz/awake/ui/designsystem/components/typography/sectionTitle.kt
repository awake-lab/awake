package io.github.ronjunevaldoz.awake.ui.designsystem.components.typography

import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

fun UiScope.sectionTitle(
    title: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Companion {
        foreground(theme.tokens.mutedForeground)
        textSize(theme.typography.label)
    }
): UiSlot = text(title, modifier = modifier, style = style)
