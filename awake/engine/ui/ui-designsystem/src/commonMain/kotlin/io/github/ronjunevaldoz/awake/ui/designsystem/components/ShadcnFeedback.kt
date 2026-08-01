// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnAlertVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnStyles
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

/**
 * Real shadcn's `Alert`: a static inline banner, not a modal like [shadcnSurface]'s
 * `Popover`/`Dialog` uses -- title required, description optional, no dismiss/action slot
 * yet (a real gap, not a silently-cut corner: shadcn's own Alert doesn't have one either,
 * that's ButtonGroup/actions composed alongside it by the caller).
 */
fun ColumnScope.shadcnAlert(
    id: String,
    modifier: UiModifier = Modifier,
    variant: ShadcnAlertVariant = ShadcnAlertVariant.Default,
    style: Style = Style.Empty,
    content: ColumnScope.() -> Unit
): UiBounds = surface(
    id = id,
    modifier = modifier.withSizeFallback(Dimension.FillMax, Dimension.WrapContent),
    style = ShadcnStyles.alert(theme.asShadcnTheme(), variant) then style
) {
    content()
}.toBounds()

/** [shadcnAlert] convenience with plain string title/description. */
fun ColumnScope.shadcnAlert(
    id: String,
    title: String,
    description: String? = null,
    modifier: UiModifier = Modifier,
    variant: ShadcnAlertVariant = ShadcnAlertVariant.Default,
    style: Style = Style.Empty
): UiBounds = shadcnAlert(
    id = id,
    modifier = modifier,
    variant = variant,
    style = style
) {
    val titleColor = when (variant) {
        ShadcnAlertVariant.Default -> theme.asShadcnTheme().tokens.foreground
        ShadcnAlertVariant.Destructive -> theme.asShadcnTheme().palette.destructive
    }
    shadcnBodyText(title, style = Style { foreground(titleColor) })
    if (description != null) {
        shadcnSupportingText(description, style = Style { foreground(titleColor) })
    }
}
