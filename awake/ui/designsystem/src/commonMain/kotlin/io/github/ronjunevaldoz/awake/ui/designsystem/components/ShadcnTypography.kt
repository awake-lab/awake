// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.headless.UiTextWrap
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.text

fun UiScope.shadcnText(
    label: String,
    modifier: Modifier = Modifier,
    centered: Boolean = false,
    muted: Boolean = false,
    visuals: SurfaceStyle = SurfaceStyle(),
): UiBounds {
    val effectiveVisuals = if (muted) {
        visuals.copy(foreground = themeValues.colors.mutedForeground)
    } else visuals
    return text(
        label = label,
        modifier = modifier,
        centered = centered,
        visuals = effectiveVisuals,
    )
}

fun UiScope.shadcnHeadline(
    label: String,
    modifier: Modifier = Modifier,
): UiBounds = text(
    label = label,
    modifier = modifier,
    centered = false,
    visuals = SurfaceStyle(
        foreground = themeValues.colors.foreground,
        textSize = themeValues.typography.headline,
    ),
)

fun UiScope.shadcnBodyText(
    label: String,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    wrap: UiTextWrap = UiTextWrap.Word,
): UiBounds = text(
    label = label,
    modifier = modifier,
    centered = false,
    visuals = SurfaceStyle(
        foreground = themeValues.colors.foreground,
        textSize = themeValues.typography.body,
    ),
    maxLines = maxLines,
    wrap = wrap,
)

fun UiScope.shadcnMetaText(
    label: String,
    modifier: Modifier = Modifier,
    muted: Boolean = true,
    wrap: UiTextWrap = UiTextWrap.None,
    overflow: UiTextOverflow = UiTextOverflow.Ellipsis,
    maxLines: Int = 1,
): UiBounds = text(
    label = label,
    modifier = modifier,
    visuals = SurfaceStyle(
        foreground = if (muted) themeValues.colors.mutedForeground else themeValues.colors.foreground,
        textSize = themeValues.typography.caption,
    ),
    wrap = wrap,
    overflow = overflow,
    maxLines = maxLines,
)

fun UiScope.shadcnLabel(
    label: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 1,
): UiBounds = shadcnMetaText(label = label, modifier = modifier, maxLines = maxLines)

fun UiScope.shadcnSectionHeader(
    title: String,
    description: String? = null,
    modifier: Modifier = Modifier,
): UiBounds = shadcnSectionTitle(title = title, description = description, modifier = modifier)

fun UiScope.shadcnSectionTitle(
    title: String,
    description: String? = null,
    modifier: Modifier = Modifier,
    muted: Boolean = false,
): UiBounds = column(modifier = modifier) {
    text(
        label = title,
        visuals = SurfaceStyle(
            foreground = if (muted) themeValues.colors.mutedForeground else themeValues.colors.foreground,
            textSize = themeValues.typography.label,
        ),
    )
    if (description != null) {
        shadcnSupportingText(label = description)
    }
}

fun UiScope.shadcnSupportingText(
    label: String,
    modifier: Modifier = Modifier,
    wrap: UiTextWrap = UiTextWrap.Word,
    overflow: UiTextOverflow = UiTextOverflow.Ellipsis,
    maxLines: Int = Int.MAX_VALUE,
): UiBounds = text(
    label = label,
    modifier = modifier,
    visuals = SurfaceStyle(
        foreground = themeValues.colors.mutedForeground,
        textSize = themeValues.typography.caption,
    ),
    wrap = wrap,
    overflow = overflow,
    maxLines = maxLines,
)

fun UiScope.shadcnTextLines(
    lines: Iterable<String>,
    modifier: Modifier = Modifier,
    wrap: UiTextWrap = UiTextWrap.Word,
    overflow: UiTextOverflow = UiTextOverflow.Ellipsis,
    maxLines: Int = Int.MAX_VALUE,
): UiBounds = column(modifier = modifier) {
    lines.forEach { line ->
        shadcnSupportingText(label = line, wrap = wrap, overflow = overflow, maxLines = maxLines)
    }
}

fun UiScope.shadcnSupportingLines(
    lines: Iterable<String>,
    modifier: Modifier = Modifier,
    wrap: UiTextWrap = UiTextWrap.Word,
    overflow: UiTextOverflow = UiTextOverflow.Ellipsis,
    maxLines: Int = Int.MAX_VALUE,
): UiBounds = shadcnTextLines(lines = lines, modifier = modifier, wrap = wrap, overflow = overflow, maxLines = maxLines)
