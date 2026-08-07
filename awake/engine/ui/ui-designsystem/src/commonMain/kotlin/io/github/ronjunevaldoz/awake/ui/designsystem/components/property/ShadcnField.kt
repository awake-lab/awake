// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.property

import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnLabel
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSectionTitle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.weight
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.unstyled.separator

/**
 * Matches real shadcn-compose's `Field` orientation switch -- [Vertical] stacks label above
 * control above description/error (a plain column); [Horizontal] places them side by side (a
 * plain row). Neither branch does any width negotiation; that's the whole point of this
 * rebuild -- see [shadcnField].
 */
enum class ShadcnFieldOrientation { Vertical, Horizontal }

/**
 * A single form field. Real shadcn's `Field` is nothing more than a `Column`/`Row` with fixed
 * spacing -- [content] composes whatever combination of [shadcnFieldLabel], a control, and
 * [shadcnFieldDescription]/[shadcnFieldError] it needs, in order. This replaces Awake's previous
 * box-slot label-width-negotiation algorithm entirely.
 */
fun ColumnScope.shadcnField(
    id: String? = null,
    modifier: UiModifier = Modifier,
    orientation: ShadcnFieldOrientation = ShadcnFieldOrientation.Vertical,
    content: UiScope.() -> Unit
): UiBounds = when (orientation) {
    ShadcnFieldOrientation.Vertical -> column(
        id = id,
        verticalArrangement = Arrangement.spacedBy(theme.asShadcnTheme().spacing.sm),
        modifier = modifier
    ) { content() }

    ShadcnFieldOrientation.Horizontal -> row(
        horizontalArrangement = Arrangement.spacedBy(theme.asShadcnTheme().spacing.md),
        verticalAlignment = UiAlignment.Vertical.Center,
        modifier = modifier
    ) { content() }
}

/** Real shadcn's `FieldLabel` -- delegates to [shadcnLabel]. */
fun UiScope.shadcnFieldLabel(
    text: String,
    modifier: UiModifier = Modifier,
    required: Boolean = false,
    disabled: Boolean = false
): UiBounds = shadcnLabel(text, modifier, required, disabled)

/** Real shadcn's `FieldDescription` -- muted small text below a field's control. */
fun UiScope.shadcnFieldDescription(text: String, modifier: UiModifier = Modifier): UiBounds {
    val shadcnTheme = theme.asShadcnTheme()
    return shadcnText(
        text,
        modifier = modifier,
        muted = true,
        style = Style { textSize(shadcnTheme.typography.caption) }
    )
}

/** Real shadcn's `FieldError` -- destructive-colored small text below a field's control. */
fun UiScope.shadcnFieldError(text: String, modifier: UiModifier = Modifier): UiBounds {
    val shadcnTheme = theme.asShadcnTheme()
    return shadcnText(
        text,
        modifier = modifier,
        style = Style {
            foreground(shadcnTheme.colors.destructive)
            textSize(shadcnTheme.typography.caption)
        }
    )
}

/**
 * Real shadcn's `FieldSet` (HTML `<fieldset>` equivalent) -- a titled section wrapper, typically
 * containing a [shadcnFieldLegend], an optional [shadcnFieldDescription], and a nested
 * [shadcnFieldGroup]. Real shadcn resets the browser's default fieldset border/padding to
 * nothing, so like [shadcnField] this is just structural grouping -- a plain column, no
 * border/background.
 */
fun ColumnScope.shadcnFieldSet(
    id: String? = null,
    modifier: UiModifier = Modifier,
    content: ColumnScope.() -> Unit
): UiBounds = column(id = id, verticalArrangement = Arrangement.spacedBy(theme.asShadcnTheme().spacing.sm), modifier = modifier) { content() }

/** Real shadcn's `FieldLegend` (HTML `<legend>` equivalent) -- the title of a [shadcnFieldSet],
 * one step up in visual weight from [shadcnFieldLabel]. Delegates to [shadcnSectionTitle] for the
 * same size/weight used by every other section-level heading in this module. */
fun ColumnScope.shadcnFieldLegend(
    text: String,
    modifier: UiModifier = Modifier
): UiBounds = shadcnSectionTitle(text, modifier = modifier)

/** Real shadcn's `FieldGroup` -- groups several [shadcnField]s into one column. */
fun ColumnScope.shadcnFieldGroup(
    id: String? = null,
    modifier: UiModifier = Modifier,
    content: ColumnScope.() -> Unit
): UiBounds = column(id = id, verticalArrangement = Arrangement.spacedBy(theme.asShadcnTheme().spacing.xxl), modifier = modifier) { content() }

/**
 * Real shadcn's `FieldSeparator` -- a plain hairline between fields, or (with [label]) a
 * centered label flanked by two hairlines that share the remaining width via `weight(1f)`.
 */
fun ColumnScope.shadcnFieldSeparator(modifier: UiModifier = Modifier, label: String? = null) {
    val borderColor = theme.asShadcnTheme().colors.border
    if (label == null) {
        separator(modifier = modifier, color = borderColor)
    } else {
        row(
            horizontalArrangement = Arrangement.spacedBy(theme.asShadcnTheme().spacing.sm),
            verticalAlignment = UiAlignment.Vertical.Center,
            modifier = modifier
        ) {
            separator(modifier = Modifier.weight(1f), color = borderColor)
            shadcnFieldDescription(label)
            separator(modifier = Modifier.weight(1f), color = borderColor)
        }
    }
}
