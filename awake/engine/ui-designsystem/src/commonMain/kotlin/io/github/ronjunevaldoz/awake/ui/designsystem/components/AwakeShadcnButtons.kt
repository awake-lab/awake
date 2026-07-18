// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.AbsoluteScope
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiButtonVariant
import io.github.ronjunevaldoz.awake.ui.UiDslScope
import io.github.ronjunevaldoz.awake.ui.UiAbsoluteDslScope
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.UiTheme
import io.github.ronjunevaldoz.awake.ui.button
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnStyles
import io.github.ronjunevaldoz.awake.ui.designsystem.asAwakeShadcnTheme

private fun awakeShadcnButtonStyle(
    theme: UiTheme,
    variant: AwakeShadcnButtonVariant,
    style: Style
): Style = AwakeShadcnStyles.button(theme.asAwakeShadcnTheme(), variant) then style

private inline fun awakeShadcnLabelButton(
    id: String,
    label: String,
    modifier: UiModifier,
    theme: UiTheme,
    variant: AwakeShadcnButtonVariant,
    style: Style,
    centered: Boolean,
    verticallyCentered: Boolean,
    invoke: (id: String, label: String, modifier: UiModifier, style: Style, variant: UiButtonVariant, centered: Boolean, verticallyCentered: Boolean) -> Boolean
): Boolean = invoke(
    id,
    label,
    modifier,
    awakeShadcnButtonStyle(theme, variant, style),
    variant.toUiButtonVariant(),
    centered,
    verticallyCentered
)

private inline fun awakeShadcnContentButton(
    id: String,
    modifier: UiModifier,
    theme: UiTheme,
    variant: AwakeShadcnButtonVariant,
    style: Style,
    invoke: (id: String, modifier: UiModifier, style: Style, variant: UiButtonVariant) -> Boolean
): Boolean = invoke(
    id,
    modifier,
    awakeShadcnButtonStyle(theme, variant, style),
    variant.toUiButtonVariant()
)

fun UiScope.awakeShadcnButton(
    id: String,
    label: String,
    modifier: UiModifier = UiModifier(),
    variant: AwakeShadcnButtonVariant = AwakeShadcnButtonVariant.Primary,
    style: Style = Style.Empty,
    centered: Boolean = true,
    verticallyCentered: Boolean = centered
): Boolean = awakeShadcnLabelButton(id, label, modifier, theme, variant, style, centered, verticallyCentered) { resolvedId, resolvedLabel, resolvedModifier, resolvedStyle, resolvedVariant, resolvedCentered, resolvedVerticallyCentered ->
    button(
        id = resolvedId,
        label = resolvedLabel,
        modifier = resolvedModifier,
        style = resolvedStyle,
        variant = resolvedVariant,
        radius = UiShape.none,
        centered = resolvedCentered,
        verticallyCentered = resolvedVerticallyCentered
    )
}

fun UiDslScope.awakeShadcnButton(
    id: String,
    label: String,
    modifier: UiModifier = UiModifier(),
    variant: AwakeShadcnButtonVariant = AwakeShadcnButtonVariant.Primary,
    style: Style = Style.Empty,
    centered: Boolean = true,
    verticallyCentered: Boolean = centered
): Boolean = awakeShadcnLabelButton(id, label, modifier, theme, variant, style, centered, verticallyCentered) { resolvedId, resolvedLabel, resolvedModifier, resolvedStyle, resolvedVariant, resolvedCentered, resolvedVerticallyCentered ->
    button(
        id = resolvedId,
        label = resolvedLabel,
        modifier = resolvedModifier,
        style = resolvedStyle,
        variant = resolvedVariant,
        radius = UiShape.none,
        centered = resolvedCentered,
        verticallyCentered = resolvedVerticallyCentered
    )
}

fun UiScope.awakeShadcnButton(
    id: String,
    modifier: UiModifier = UiModifier(),
    variant: AwakeShadcnButtonVariant = AwakeShadcnButtonVariant.Primary,
    style: Style = Style.Empty,
    content: AbsoluteScope.(slot: UiSlot) -> Unit
): Boolean = awakeShadcnContentButton(id, modifier, theme, variant, style) { resolvedId, resolvedModifier, resolvedStyle, resolvedVariant ->
    button(
        id = resolvedId,
        modifier = resolvedModifier,
        style = resolvedStyle,
        variant = resolvedVariant,
        radius = UiShape.none,
        content = content
    )
}

fun UiDslScope.awakeShadcnButton(
    id: String,
    modifier: UiModifier = UiModifier(),
    variant: AwakeShadcnButtonVariant = AwakeShadcnButtonVariant.Primary,
    style: Style = Style.Empty,
    content: UiAbsoluteDslScope.(slot: UiSlot) -> Unit
): Boolean = awakeShadcnContentButton(id, modifier, theme, variant, style) { resolvedId, resolvedModifier, resolvedStyle, resolvedVariant ->
    button(
        id = resolvedId,
        modifier = resolvedModifier,
        style = resolvedStyle,
        variant = resolvedVariant,
        radius = UiShape.none,
        content = content
    )
}

private fun AwakeShadcnButtonVariant.toUiButtonVariant(): UiButtonVariant = when (this) {
    AwakeShadcnButtonVariant.Outline -> UiButtonVariant.Outline
    AwakeShadcnButtonVariant.Ghost -> UiButtonVariant.Ghost
    else -> UiButtonVariant.Filled
}
