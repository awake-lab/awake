// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiButtonVariant
import io.github.ronjunevaldoz.awake.ui.UiDslScope
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.button

fun UiScope.awakeShadcnButton(
    id: String,
    width: Float,
    height: Float,
    label: String,
    modifier: UiModifier = UiModifier(),
    variant: AwakeShadcnButtonVariant = AwakeShadcnButtonVariant.Primary,
    style: Style = Style.Empty
): Boolean = button(
    id = id,
    width = width,
    height = height,
    label = label,
    modifier = modifier,
    style = AwakeShadcnStyles.button(theme.asAwakeShadcnTheme(), variant) then style,
    variant = variant.toUiButtonVariant(),
    radius = UiShape.none
)

fun UiDslScope.awakeShadcnButton(
    id: String,
    width: Float,
    height: Float,
    label: String,
    modifier: UiModifier = UiModifier(),
    variant: AwakeShadcnButtonVariant = AwakeShadcnButtonVariant.Primary,
    style: Style = Style.Empty
): Boolean = button(
    id = id,
    label = label,
    width = width,
    height = height,
    modifier = modifier,
    style = AwakeShadcnStyles.button(theme.asAwakeShadcnTheme(), variant) then style,
    variant = variant.toUiButtonVariant(),
    radius = UiShape.none
)

private fun AwakeShadcnButtonVariant.toUiButtonVariant(): UiButtonVariant = when (this) {
    AwakeShadcnButtonVariant.Outline -> UiButtonVariant.Outline
    AwakeShadcnButtonVariant.Ghost -> UiButtonVariant.Ghost
    else -> UiButtonVariant.Filled
}
