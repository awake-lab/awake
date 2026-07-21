package io.github.ronjunevaldoz.awake.ui.designsystem.components.typography

import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextWrap
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text

fun UiScope.textLines(
    lines: Iterable<String>,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty,
    wrap: UiTextWrap = UiTextWrap.None,
    overflow: UiTextOverflow = if (wrap == UiTextWrap.None) UiTextOverflow.Ellipsis else UiTextOverflow.Clip,
    maxLines: Int = if (wrap == UiTextWrap.None) 1 else Int.MAX_VALUE
) {
    lines.forEach { line ->
        text(
            label = line,
            modifier = modifier,
            style = style,
            wrap = wrap,
            overflow = overflow,
            maxLines = maxLines
        )
    }
}