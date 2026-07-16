// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.ColumnScope
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiAbsoluteDslScope
import io.github.ronjunevaldoz.awake.ui.UiColumnDslScope
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.panel

fun UiScope.awakeShadcnSurface(
    id: String,
    width: Dimension,
    height: Dimension,
    variant: AwakeShadcnSurfaceVariant = AwakeShadcnSurfaceVariant.Card,
    style: Style = Style.Empty,
    content: ColumnScope.(slot: UiSlot) -> Unit
) {
    panel(
        id = id,
        width = width,
        height = height,
        style = AwakeShadcnStyles.surface(theme.asAwakeShadcnTheme(), variant) then style,
        content = content
    )
}

fun UiColumnDslScope.awakeShadcnSurface(
    id: String,
    width: Dimension = Dimension.FillMax,
    height: Dimension,
    modifier: UiModifier = UiModifier(),
    variant: AwakeShadcnSurfaceVariant = AwakeShadcnSurfaceVariant.Card,
    style: Style = Style.Empty,
    content: UiColumnDslScope.(slot: UiSlot) -> Unit
): UiSlot = panel(
    id = id,
    width = width,
    height = height,
    modifier = modifier,
    style = AwakeShadcnStyles.surface(theme.asAwakeShadcnTheme(), variant) then style,
    content = content
)

fun UiAbsoluteDslScope.awakeShadcnSurface(
    id: String,
    width: Dimension,
    height: Dimension,
    modifier: UiModifier = UiModifier(),
    variant: AwakeShadcnSurfaceVariant = AwakeShadcnSurfaceVariant.Card,
    style: Style = Style.Empty,
    content: UiColumnDslScope.(slot: UiSlot) -> Unit
): UiSlot = panel(
    id = id,
    width = width,
    height = height,
    modifier = modifier,
    style = AwakeShadcnStyles.surface(theme.asAwakeShadcnTheme(), variant) then style,
    content = content
)
