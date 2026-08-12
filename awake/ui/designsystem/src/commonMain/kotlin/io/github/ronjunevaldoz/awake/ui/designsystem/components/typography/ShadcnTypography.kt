// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.typography

import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.headless.UiTextWrap
import io.github.ronjunevaldoz.awake.ui.headless.text

fun UiScope.shadcnMetaText(
    label: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 1,
): UiBounds = text(
    label = label,
    modifier = modifier,
    visuals = SurfaceStyle(foreground = themeValues.colors.mutedForeground, textSize = themeValues.typography.caption),
    overflow = UiTextOverflow.Ellipsis,
    maxLines = maxLines,
)

fun UiScope.shadcnSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
): UiBounds = text(
    label = title,
    modifier = modifier,
    visuals = SurfaceStyle(foreground = themeValues.colors.mutedForeground, textSize = themeValues.typography.label),
)

fun UiScope.shadcnSupportingText(
    label: String,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
): UiBounds = text(
    label = label,
    modifier = modifier,
    visuals = SurfaceStyle(foreground = themeValues.colors.mutedForeground, textSize = themeValues.typography.caption),
    wrap = UiTextWrap.Word,
    overflow = UiTextOverflow.Ellipsis,
    maxLines = maxLines,
)

fun UiScope.shadcnTextLines(
    lines: Iterable<String>,
    modifier: Modifier = Modifier,
    wrap: UiTextWrap = UiTextWrap.None,
    overflow: UiTextOverflow = if (wrap == UiTextWrap.None) UiTextOverflow.Ellipsis else UiTextOverflow.Clip,
    maxLines: Int = if (wrap == UiTextWrap.None) 1 else Int.MAX_VALUE,
) {
    lines.forEach { line ->
        text(label = line, modifier = modifier, wrap = wrap, overflow = overflow, maxLines = maxLines)
    }
}

fun UiScope.shadcnSupportingLines(
    lines: Iterable<String>,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
) {
    shadcnTextLines(
        lines = lines,
        modifier = modifier,
        wrap = UiTextWrap.Word,
        overflow = UiTextOverflow.Ellipsis,
        maxLines = maxLines,
    )
}
