// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.components.typography.sectionTitle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.typography.supportingText
import io.github.ronjunevaldoz.awake.ui.font
import io.github.ronjunevaldoz.awake.ui.font.measureTextWidth
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.resolveGlyphPx
import io.github.ronjunevaldoz.awake.ui.textStyle
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

/** [sectionTitle] with Shadcn tokens. */
fun ColumnScope.shadcnSectionTitle(
    title: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty
): UiBounds = sectionTitle(
    title = title,
    modifier = modifier,
    style = Style {
        val shadcnTheme = theme.asShadcnTheme()
        if (context.currentTextStyle.color == null) {
            foreground(shadcnTheme.colors.foreground)
        }
        textSize(shadcnTheme.typography.title)
    } then style
)

/** Larger headline text using Shadcn tokens. */
fun ColumnScope.shadcnHeadline(
    label: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty
): UiBounds = text(
    label = label,
    modifier = modifier,
    style = Style {
        val shadcnTheme = theme.asShadcnTheme()
        if (context.currentTextStyle.color == null) {
            foreground(shadcnTheme.colors.foreground)
        }
        textSize(shadcnTheme.typography.headline)
    } then style
)

/**Standard body text using Shadcn tokens. */
fun ColumnScope.shadcnBodyText(
    label: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    maxLines: Int = Int.MAX_VALUE
): UiBounds = text(
    label = label,
    modifier = modifier,
    style = Style {
        val shadcnTheme = theme.asShadcnTheme()
        if (context.currentTextStyle.color == null) {
            foreground(shadcnTheme.colors.foreground)
        }
        textSize(shadcnTheme.typography.body)
    } then style,
    maxLines = maxLines
)

/** Muted caption/supporting text using Shadcn tokens. */
fun ColumnScope.shadcnSupportingText(
    label: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    maxLines: Int = Int.MAX_VALUE
): UiBounds = supportingText(
    label = label,
    modifier = modifier,
    style = Style {
        val shadcnTheme = theme.asShadcnTheme()
        if (context.currentTextStyle.color == null) {
            foreground(shadcnTheme.colors.mutedForeground)
        }
        textSize(shadcnTheme.typography.caption)
    } then style,
    maxLines = maxLines
)

/** Generic shadcn text component with support for muted state and shimmer modifier. */
fun UiScope.shadcnText(
    label: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    muted: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    overflow: UiTextOverflow = UiTextOverflow.Visible
): UiBounds = text(
    label = label,
    modifier = modifier,
    style = Style {
        val shadcnTheme = theme.asShadcnTheme()
        if (muted) {
            foreground(shadcnTheme.colors.mutedForeground)
        } else if (context.currentTextStyle.color == null) {
            foreground(shadcnTheme.colors.foreground)
        }
        textSize(shadcnTheme.typography.body)
    } then style,
    maxLines = maxLines,
    overflow = overflow
)

/** Real shadcn's `Label` -- a purely presentational field label. Compose has no HTML `for`
 * attribute to wire up, so association with a field is just visual (place it directly above or
 * beside the field it describes, matching [io.github.ronjunevaldoz.awake.ui.designsystem.components.property.shadcnField]). */
fun UiScope.shadcnLabel(
    text: String,
    modifier: UiModifier = Modifier,
    required: Boolean = false,
    disabled: Boolean = false
): UiBounds {
    val shadcnTheme = theme.asShadcnTheme()
    // Pin both dimensions from a synchronous glyph measurement instead of leaning on row()'s
    // WrapContent/FillMax fallbacks: (1) row()'s plain UiScope overload claims its slot
    // directly with no pre-measuring trial pass, so a WrapContent height crashes when nested
    // inside another container's own measuring pass; (2) a FillMax-width label sitting next to
    // a weight()ed control (every real shadcnField* call site) reports its huge FillMax trial
    // width as this row's "non-weighted occupied space", starving the weighted control of the
    // width it should get. A label's own text is already knowable up front, so measure it
    // instead of asking the layout system to guess.
    val labelTextStyle = textStyle then TextStyle(size = shadcnTheme.typography.label)
    val glyphPx = resolveGlyphPx(font, labelTextStyle)
    val fullText = if (required) "$text *" else text
    val labelWidthPx = font.measureTextWidth(fullText, glyphPx)
    // Respect an explicit caller-supplied width (e.g. a shared label column across sibling
    // rows) -- only fall back to the intrinsic glyph measurement when the caller didn't ask
    // for a specific width.
    val resolvedWidth = modifier.widthDimension ?: Dimension.Fixed(labelWidthPx.px)
    return row(
        modifier = modifier.width(resolvedWidth).height(glyphPx.px),
        horizontalArrangement = Arrangement.spacedBy(0f.dp)
    ) {
        shadcnText(text, muted = disabled, style = Style { textSize(shadcnTheme.typography.label) })
        if (required) {
            shadcnText(
                " *",
                style = Style {
                    foreground(shadcnTheme.colors.destructive)
                    textSize(shadcnTheme.typography.label)
                }
            )
        }
    }.toBounds()
}

/** Common section header layout (title + optional description). */
fun ColumnScope.shadcnSectionHeader(
    title: ColumnScope.() -> Unit,
    description: (ColumnScope.() -> Unit)? = null
) {
    title()
    description?.invoke(this)
}

/** Convenience [shadcnSectionHeader] for plain string labels. */
fun ColumnScope.shadcnSectionHeader(
    title: String,
    description: String? = null,
    modifier: UiModifier = Modifier
): Unit = shadcnSectionHeader(
    title = { shadcnSectionTitle(title, modifier = modifier) },
    description = description?.takeIf { it.isNotBlank() }?.let { text ->
        { shadcnSupportingText(text) }
    }
)
