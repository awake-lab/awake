package io.github.ronjunevaldoz.awake.ui.designsystem.components.typography

import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextWrap
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

fun UiScope.shadcnSupportingText(
    label: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Companion {
        foreground(theme.colors.mutedForeground)
        textSize(theme.typography.caption)
    },
    maxLines: Int = Int.MAX_VALUE
): UiBounds = text(
    label = label,
    modifier = modifier,
    style = style,
    wrap = UiTextWrap.Word,
    overflow = UiTextOverflow.Ellipsis,
    maxLines = maxLines
)

@Deprecated(
    message = "Use shadcnSupportingText for UI design system typography functions",
    replaceWith = ReplaceWith("shadcnSupportingText(label, modifier, style, maxLines)")
)
fun UiScope.supportingText(
    label: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Companion {
        foreground(theme.colors.mutedForeground)
        textSize(theme.typography.caption)
    },
    maxLines: Int = Int.MAX_VALUE
): UiBounds = shadcnSupportingText(
    label = label,
    modifier = modifier,
    style = style,
    maxLines = maxLines
)
