package io.github.ronjunevaldoz.awake.ui.designsystem.components.typography

import io.github.ronjunevaldoz.awake.ui.styling.Style
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text

fun UiScope.metaText(
    label: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Companion {
        foreground(theme.tokens.mutedForeground)
        textSize(theme.typography.caption)
    },
    maxLines: Int = 1
): UiSlot = text(
    label = label,
    modifier = modifier,
    style = style,
    overflow = UiTextOverflow.Ellipsis,
    maxLines = maxLines
)