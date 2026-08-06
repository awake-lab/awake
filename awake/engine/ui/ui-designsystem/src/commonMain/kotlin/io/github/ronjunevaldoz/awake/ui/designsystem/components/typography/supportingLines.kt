package io.github.ronjunevaldoz.awake.ui.designsystem.components.typography

import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextWrap
import io.github.ronjunevaldoz.awake.ui.style.*

fun UiScope.shadcnSupportingLines(
    lines: Iterable<String>,
    modifier: UiModifier = Modifier,
    style: Style = Style.Companion {
        foreground(theme.colors.mutedForeground)
    },
    maxLines: Int = Int.MAX_VALUE
) {
    shadcnTextLines(
        lines = lines,
        modifier = modifier,
        style = style,
        wrap = UiTextWrap.Word,
        overflow = UiTextOverflow.Ellipsis,
        maxLines = maxLines
    )
}

@Deprecated(
    message = "Use shadcnSupportingLines for UI design system typography functions",
    replaceWith = ReplaceWith("shadcnSupportingLines(lines, modifier, style, maxLines)")
)
fun UiScope.supportingLines(
    lines: Iterable<String>,
    modifier: UiModifier = Modifier,
    style: Style = Style.Companion {
        foreground(theme.colors.mutedForeground)
    },
    maxLines: Int = Int.MAX_VALUE
) {
    shadcnSupportingLines(
        lines = lines,
        modifier = modifier,
        style = style,
        maxLines = maxLines
    )
}
