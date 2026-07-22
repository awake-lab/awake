// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.designsystem.asAwakeShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnStyles
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnSurfaceVariant
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.ext.surface
import io.github.ronjunevaldoz.awake.ui.theme

/** Real shadcn's `Surface`: a contained region (Card, Popover, Dialog) that owns its
 * background, border, and content padding. Composed from the [surface] primitive. */
fun UiScope.awakeShadcnSurface(
    id: String,
    width: Dimension,
    height: Dimension,
    variant: AwakeShadcnSurfaceVariant = AwakeShadcnSurfaceVariant.Card,
    style: Style = Style.Empty,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = surface(
    id = id,
    width = width,
    height = height,
    style = AwakeShadcnStyles.surface(theme.asAwakeShadcnTheme(), variant) then style,
    content = content
)

/** [awakeShadcnSurface] override for [ColumnScope] that defaults width to [Dimension.FillMax]. */
fun ColumnScope.awakeShadcnSurface(
    id: String,
    height: Dimension,
    width: Dimension = Dimension.FillMax,
    modifier: UiModifier = UiModifier(),
    variant: AwakeShadcnSurfaceVariant = AwakeShadcnSurfaceVariant.Card,
    style: Style = Style.Empty,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = surface(
    id = id,
    width = width,
    height = height,
    modifier = modifier,
    style = AwakeShadcnStyles.surface(theme.asAwakeShadcnTheme(), variant) then style,
    content = content
)

/** [awakeShadcnSurface] override for [RowScope] that defaults height to [Dimension.FillMax]. */
fun RowScope.awakeShadcnSurface(
    id: String,
    width: Dimension,
    height: Dimension = Dimension.FillMax,
    modifier: UiModifier = UiModifier(),
    variant: AwakeShadcnSurfaceVariant = AwakeShadcnSurfaceVariant.Card,
    style: Style = Style.Empty,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = surface(
    id = id,
    width = width,
    height = height,
    modifier = modifier,
    style = AwakeShadcnStyles.surface(theme.asAwakeShadcnTheme(), variant) then style,
    content = content
)

/** [awakeShadcnSurface] override for [BoxScope]. */
fun BoxScope.awakeShadcnSurface(
    id: String,
    width: Dimension,
    height: Dimension,
    modifier: UiModifier = UiModifier(),
    variant: AwakeShadcnSurfaceVariant = AwakeShadcnSurfaceVariant.Card,
    style: Style = Style.Empty,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = surface(
    id = id,
    width = width,
    height = height,
    modifier = modifier,
    style = AwakeShadcnStyles.surface(theme.asAwakeShadcnTheme(), variant) then style,
    content = content
)
