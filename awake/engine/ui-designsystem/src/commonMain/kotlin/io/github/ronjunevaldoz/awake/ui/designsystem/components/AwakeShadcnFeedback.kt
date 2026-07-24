// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.modifier.Dimension
import io.github.ronjunevaldoz.awake.ui.styling.Style
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.designsystem.asAwakeShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnAlertVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnStyles
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.ext.surface
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.theme

/**
 * Real shadcn's `Alert`: a static inline banner, not a modal like [awakeShadcnSurface]'s
 * `Popover`/`Dialog` uses -- title required, description optional, no dismiss/action slot
 * yet (a real gap, not a silently-cut corner: shadcn's own Alert doesn't have one either,
 * that's ButtonGroup/actions composed alongside it by the caller).
 */
fun ColumnScope.awakeShadcnAlert(
    id: String,
    title: String,
    description: String? = null,
    width: Dimension = Dimension.FillMax,
    height: Dimension = Dimension.WrapContent,
    modifier: UiModifier = Modifier,
    variant: AwakeShadcnAlertVariant = AwakeShadcnAlertVariant.Default,
    style: Style = Style.Empty
): UiSlot = surface(
    id = id,
    modifier = (modifier).copy(width = width, height = height),
    style = AwakeShadcnStyles.alert(theme.asAwakeShadcnTheme(), variant) then style
) {
    val titleColor = when (variant) {
        AwakeShadcnAlertVariant.Default -> theme.asAwakeShadcnTheme().tokens.foreground
        AwakeShadcnAlertVariant.Destructive -> theme.asAwakeShadcnTheme().palette.destructive
    }
    awakeShadcnBodyText(title, style = Style { foreground(titleColor) })
    if (description != null) {
        awakeShadcnSupportingText(description, style = Style { foreground(titleColor) })
    }
}
