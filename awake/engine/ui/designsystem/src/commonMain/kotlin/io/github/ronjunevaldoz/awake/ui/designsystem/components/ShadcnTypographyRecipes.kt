// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.headless.BoxScope
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.RowScope
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.headless.UiTextWrap
import io.github.ronjunevaldoz.awake.ui.headless.text

fun ColumnScope.shadcnSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
): UiBounds = text(
    label = title,
    modifier = modifier,
    visuals = SurfaceStyle(
        foreground = themeValues.colors.mutedForeground,
        textSize = themeValues.typography.label,
    ),
)

fun ColumnScope.shadcnHeadline(
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

fun ColumnScope.shadcnBodyText(
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

fun ColumnScope.shadcnSupportingText(
    label: String,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
): UiBounds = text(
    label = label,
    modifier = modifier,
    centered = false,
    visuals = SurfaceStyle(
        foreground = themeValues.colors.mutedForeground,
        textSize = themeValues.typography.caption,
    ),
    maxLines = maxLines,
    wrap = UiTextWrap.Word,
)

fun UiScope.shadcnText(
    label: String,
    modifier: Modifier = Modifier,
    muted: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    overflow: UiTextOverflow = UiTextOverflow.Visible,
    wrap: UiTextWrap = UiTextWrap.None,
): UiBounds = text(
    label = label,
    modifier = modifier,
    visuals = SurfaceStyle(
        foreground = if (muted) themeValues.colors.mutedForeground else themeValues.colors.foreground,
        textSize = themeValues.typography.body,
    ),
    maxLines = maxLines,
    overflow = overflow,
    wrap = wrap,
)

fun ColumnScope.shadcnText(
    label: String,
    modifier: Modifier = Modifier,
    muted: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    overflow: UiTextOverflow = UiTextOverflow.Visible,
    wrap: UiTextWrap = UiTextWrap.None,
): UiBounds = text(
    label = label,
    modifier = modifier,
    visuals = SurfaceStyle(
        foreground = if (muted) themeValues.colors.mutedForeground else themeValues.colors.foreground,
        textSize = themeValues.typography.body,
    ),
    maxLines = maxLines,
    overflow = overflow,
    wrap = wrap,
)

fun RowScope.shadcnText(
    label: String,
    modifier: Modifier = Modifier,
    muted: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    overflow: UiTextOverflow = UiTextOverflow.Visible,
    wrap: UiTextWrap = UiTextWrap.None,
): UiBounds = text(
    label = label,
    modifier = modifier,
    visuals = SurfaceStyle(
        foreground = if (muted) themeValues.colors.mutedForeground else themeValues.colors.foreground,
        textSize = themeValues.typography.body,
    ),
    maxLines = maxLines,
    overflow = overflow,
    wrap = wrap,
)

fun BoxScope.shadcnText(
    label: String,
    modifier: Modifier = Modifier,
    muted: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    overflow: UiTextOverflow = UiTextOverflow.Visible,
    wrap: UiTextWrap = UiTextWrap.None,
): UiBounds = text(
    label = label,
    modifier = modifier,
    visuals = SurfaceStyle(
        foreground = if (muted) themeValues.colors.mutedForeground else themeValues.colors.foreground,
        textSize = themeValues.typography.body,
    ),
    maxLines = maxLines,
    overflow = overflow,
    wrap = wrap,
)

fun UiScope.shadcnLabel(
    text: String,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    disabled: Boolean = false,
): UiBounds = this.text(
    label = if (required) "$text *" else text,
    modifier = modifier,
    visuals = SurfaceStyle(
        foreground = if (disabled) themeValues.colors.mutedForeground else themeValues.colors.foreground,
        textSize = themeValues.typography.label,
    ),
)

fun ColumnScope.shadcnSectionHeader(
    title: String,
    description: String? = null,
    modifier: Modifier = Modifier,
) {
    shadcnSectionTitle(title, modifier)
    description?.takeIf(String::isNotBlank)?.let(::shadcnSupportingText)
}
