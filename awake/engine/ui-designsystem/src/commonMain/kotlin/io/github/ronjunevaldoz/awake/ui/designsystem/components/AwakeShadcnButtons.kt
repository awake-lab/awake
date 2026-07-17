// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.AbsoluteScope
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiButtonVariant
import io.github.ronjunevaldoz.awake.ui.UiDslScope
import io.github.ronjunevaldoz.awake.ui.UiAbsoluteDslScope
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.button
import io.github.ronjunevaldoz.awake.ui.height
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.width

fun UiScope.awakeShadcnButton(
    id: String,
    label: String,
    modifier: UiModifier = UiModifier(),
    variant: AwakeShadcnButtonVariant = AwakeShadcnButtonVariant.Primary,
    style: Style = Style.Empty,
    centered: Boolean = true,
    verticallyCentered: Boolean = centered
): Boolean = button(
    id = id,
    label = label,
    modifier = modifier,
    style = AwakeShadcnStyles.button(theme.asAwakeShadcnTheme(), variant) then style,
    variant = variant.toUiButtonVariant(),
    radius = UiShape.none,
    centered = centered,
    verticallyCentered = verticallyCentered
)

fun UiScope.awakeShadcnButton(
    id: String,
    width: Float,
    height: Float,
    label: String,
    modifier: UiModifier = UiModifier(),
    variant: AwakeShadcnButtonVariant = AwakeShadcnButtonVariant.Primary,
    style: Style = Style.Empty,
    centered: Boolean = true,
    verticallyCentered: Boolean = centered
): Boolean = button(
    id = id,
    label = label,
    modifier = modifier.width(width.px).height(height.px),
    style = AwakeShadcnStyles.button(theme.asAwakeShadcnTheme(), variant) then style,
    variant = variant.toUiButtonVariant(),
    radius = UiShape.none,
    centered = centered,
    verticallyCentered = verticallyCentered
)

fun UiDslScope.awakeShadcnButton(
    id: String,
    label: String,
    modifier: UiModifier = UiModifier(),
    variant: AwakeShadcnButtonVariant = AwakeShadcnButtonVariant.Primary,
    style: Style = Style.Empty,
    centered: Boolean = true,
    verticallyCentered: Boolean = centered
): Boolean = button(
    id = id,
    label = label,
    modifier = modifier,
    style = AwakeShadcnStyles.button(theme.asAwakeShadcnTheme(), variant) then style,
    variant = variant.toUiButtonVariant(),
    radius = UiShape.none,
    centered = centered,
    verticallyCentered = verticallyCentered
)

fun UiDslScope.awakeShadcnButton(
    id: String,
    width: Float,
    height: Float,
    label: String,
    modifier: UiModifier = UiModifier(),
    variant: AwakeShadcnButtonVariant = AwakeShadcnButtonVariant.Primary,
    style: Style = Style.Empty,
    centered: Boolean = true,
    verticallyCentered: Boolean = centered
): Boolean = button(
    id = id,
    label = label,
    modifier = modifier.width(width.px).height(height.px),
    style = AwakeShadcnStyles.button(theme.asAwakeShadcnTheme(), variant) then style,
    variant = variant.toUiButtonVariant(),
    radius = UiShape.none,
    centered = centered,
    verticallyCentered = verticallyCentered
)

fun UiScope.awakeShadcnButton(
    id: String,
    modifier: UiModifier = UiModifier(),
    variant: AwakeShadcnButtonVariant = AwakeShadcnButtonVariant.Primary,
    style: Style = Style.Empty,
    content: AbsoluteScope.(slot: UiSlot) -> Unit
): Boolean = button(
    id = id,
    modifier = modifier,
    style = AwakeShadcnStyles.button(theme.asAwakeShadcnTheme(), variant) then style,
    variant = variant.toUiButtonVariant(),
    radius = UiShape.none,
    content = content
)

fun UiScope.awakeShadcnButton(
    id: String,
    width: Float,
    height: Float,
    modifier: UiModifier = UiModifier(),
    variant: AwakeShadcnButtonVariant = AwakeShadcnButtonVariant.Primary,
    style: Style = Style.Empty,
    content: AbsoluteScope.(slot: UiSlot) -> Unit
): Boolean = button(
    id = id,
    modifier = modifier.width(width.px).height(height.px),
    style = AwakeShadcnStyles.button(theme.asAwakeShadcnTheme(), variant) then style,
    variant = variant.toUiButtonVariant(),
    radius = UiShape.none,
    content = content
)

fun UiDslScope.awakeShadcnButton(
    id: String,
    modifier: UiModifier = UiModifier(),
    variant: AwakeShadcnButtonVariant = AwakeShadcnButtonVariant.Primary,
    style: Style = Style.Empty,
    content: UiAbsoluteDslScope.(slot: UiSlot) -> Unit
): Boolean = button(
    id = id,
    modifier = modifier,
    style = AwakeShadcnStyles.button(theme.asAwakeShadcnTheme(), variant) then style,
    variant = variant.toUiButtonVariant(),
    radius = UiShape.none,
    content = content
)

fun UiDslScope.awakeShadcnButton(
    id: String,
    width: Float,
    height: Float,
    modifier: UiModifier = UiModifier(),
    variant: AwakeShadcnButtonVariant = AwakeShadcnButtonVariant.Primary,
    style: Style = Style.Empty,
    content: UiAbsoluteDslScope.(slot: UiSlot) -> Unit
): Boolean = button(
    id = id,
    modifier = modifier.width(width.px).height(height.px),
    style = AwakeShadcnStyles.button(theme.asAwakeShadcnTheme(), variant) then style,
    variant = variant.toUiButtonVariant(),
    radius = UiShape.none,
    content = content
)

private fun AwakeShadcnButtonVariant.toUiButtonVariant(): UiButtonVariant = when (this) {
    AwakeShadcnButtonVariant.Outline -> UiButtonVariant.Outline
    AwakeShadcnButtonVariant.Ghost -> UiButtonVariant.Ghost
    else -> UiButtonVariant.Filled
}
