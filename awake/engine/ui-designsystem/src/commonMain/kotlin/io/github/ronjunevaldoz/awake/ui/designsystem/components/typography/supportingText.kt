package io.github.ronjunevaldoz.awake.ui.designsystem.components.typography

import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextWrap
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text

fun UiScope.supportingText(
    label: String,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Companion {
        foreground(theme.tokens.mutedForeground)
        textSize(theme.typography.caption)
    },
    maxLines: Int = Int.MAX_VALUE
): UiSlot = text(
    label = label,
    modifier = modifier,
    style = style,
    wrap = UiTextWrap.Word,
    overflow = UiTextOverflow.Ellipsis,
    maxLines = maxLines
)