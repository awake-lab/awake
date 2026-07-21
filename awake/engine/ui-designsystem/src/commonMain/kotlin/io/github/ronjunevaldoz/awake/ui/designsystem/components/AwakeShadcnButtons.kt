// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.layouts.AbsoluteScope
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.unstyled.UiButtonVariant
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.UiTheme
import io.github.ronjunevaldoz.awake.ui.unstyled.buttonSlot
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnButtonSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnStyles
import io.github.ronjunevaldoz.awake.ui.designsystem.asAwakeShadcnTheme
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.height

private fun UiModifier.withShadcnSize(size: AwakeShadcnButtonSize): UiModifier =
    if (height == null) height(size.heightDp.dp) else this

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
    size: AwakeShadcnButtonSize = AwakeShadcnButtonSize.Md,
    style: Style = Style.Empty,
    centered: Boolean = true,
    verticallyCentered: Boolean = centered
): Boolean = awakeShadcnLabelButton(id, label, modifier.withShadcnSize(size), theme, variant, style, centered, verticallyCentered) { resolvedId, resolvedLabel, resolvedModifier, resolvedStyle, resolvedVariant, resolvedCentered, resolvedVerticallyCentered ->
    buttonSlot(
        id = resolvedId,
        label = resolvedLabel,
        modifier = resolvedModifier,
        style = resolvedStyle,
        variant = resolvedVariant,
        radius = UiShape.none,
        centered = resolvedCentered,
        verticallyCentered = resolvedVerticallyCentered
    ).clicked
}

fun UiScope.awakeShadcnButton(
    id: String,
    modifier: UiModifier = UiModifier(),
    variant: AwakeShadcnButtonVariant = AwakeShadcnButtonVariant.Primary,
    size: AwakeShadcnButtonSize = AwakeShadcnButtonSize.Md,
    style: Style = Style.Empty,
    content: AbsoluteScope.(slot: UiSlot) -> Unit
): Boolean = awakeShadcnContentButton(id, modifier.withShadcnSize(size), theme, variant, style) { resolvedId, resolvedModifier, resolvedStyle, resolvedVariant ->
    buttonSlot(
        id = resolvedId,
        modifier = resolvedModifier,
        style = resolvedStyle,
        variant = resolvedVariant,
        radius = UiShape.none,
        content = content
    ).clicked
}

private fun AwakeShadcnButtonVariant.toUiButtonVariant(): UiButtonVariant = when (this) {
    AwakeShadcnButtonVariant.Outline -> UiButtonVariant.Outline
    AwakeShadcnButtonVariant.Ghost -> UiButtonVariant.Ghost
    else -> UiButtonVariant.Filled
}
