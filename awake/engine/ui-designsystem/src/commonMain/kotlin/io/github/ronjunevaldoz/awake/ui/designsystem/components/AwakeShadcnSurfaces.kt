// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.AbsoluteScope
import io.github.ronjunevaldoz.awake.ui.BoxScope
import io.github.ronjunevaldoz.awake.ui.ColumnScope
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScrollPanelResult
import io.github.ronjunevaldoz.awake.ui.UiScrollState
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.designsystem.asAwakeShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnStyles
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnSurfaceVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.panel
import io.github.ronjunevaldoz.awake.ui.scrollPanel

/** Real shadcn's `Surface`: a contained region (Card, Popover, Dialog) that owns its
 * background, border, and content padding. Composed from the [panel] primitive. */
fun UiScope.awakeShadcnSurface(
    id: String,
    width: Dimension,
    height: Dimension,
    variant: AwakeShadcnSurfaceVariant = AwakeShadcnSurfaceVariant.Card,
    style: Style = Style.Empty,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = panel(
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
): UiSlot = panel(
    id = id,
    width = width,
    height = height,
    modifier = modifier,
    style = AwakeShadcnStyles.surface(theme.asAwakeShadcnTheme(), variant) then style,
    content = content
)

/** Real shadcn's `ScrollArea`: a surface that scrolls its vertical overflow and renders
 * a custom scrollbar. Composed from the [scrollPanel] primitive. */
fun ColumnScope.awakeShadcnScrollSurface(
    id: String,
    height: Dimension,
    state: UiScrollState,
    width: Dimension = Dimension.FillMax,
    modifier: UiModifier = UiModifier(),
    variant: AwakeShadcnSurfaceVariant = AwakeShadcnSurfaceVariant.Card,
    style: Style = Style.Empty,
    scrollSpeed: Float = 32f,
    scrollbarWidth: Dp = 6f.dp,
    scrollbarGap: Dp = 8f.dp,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiScrollPanelResult = scrollPanel(
    id = id,
    width = width,
    height = height,
    state = state,
    modifier = modifier,
    style = AwakeShadcnStyles.surface(theme.asAwakeShadcnTheme(), variant) then style,
    scrollSpeed = scrollSpeed,
    scrollbarWidth = scrollbarWidth,
    scrollbarGap = scrollbarGap,
    content = content
)

/** [awakeShadcnScrollSurface] override for [AbsoluteScope]. */
fun AbsoluteScope.awakeShadcnScrollSurface(
    id: String,
    width: Dimension,
    height: Dimension,
    state: UiScrollState,
    modifier: UiModifier = UiModifier(),
    variant: AwakeShadcnSurfaceVariant = AwakeShadcnSurfaceVariant.Card,
    style: Style = Style.Empty,
    scrollSpeed: Float = 32f,
    scrollbarWidth: Dp = 6f.dp,
    scrollbarGap: Dp = 8f.dp,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiScrollPanelResult = scrollPanel(
    id = id,
    width = width,
    height = height,
    state = state,
    modifier = modifier,
    style = AwakeShadcnStyles.surface(theme.asAwakeShadcnTheme(), variant) then style,
    scrollSpeed = scrollSpeed,
    scrollbarWidth = scrollbarWidth,
    scrollbarGap = scrollbarGap,
    content = content
)

/** [awakeShadcnScrollSurface] override for [BoxScope]. */
fun BoxScope.awakeShadcnScrollSurface(
    id: String,
    width: Dimension,
    height: Dimension,
    state: UiScrollState,
    modifier: UiModifier = UiModifier(),
    variant: AwakeShadcnSurfaceVariant = AwakeShadcnSurfaceVariant.Card,
    style: Style = Style.Empty,
    scrollSpeed: Float = 32f,
    scrollbarWidth: Dp = 6f.dp,
    scrollbarGap: Dp = 8f.dp,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiScrollPanelResult = scrollPanel(
    id = id,
    width = width,
    height = height,
    state = state,
    modifier = modifier,
    style = AwakeShadcnStyles.surface(theme.asAwakeShadcnTheme(), variant) then style,
    scrollSpeed = scrollSpeed,
    scrollbarWidth = scrollbarWidth,
    scrollbarGap = scrollbarGap,
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
): UiSlot = panel(
    id = id,
    width = width,
    height = height,
    modifier = modifier,
    style = AwakeShadcnStyles.surface(theme.asAwakeShadcnTheme(), variant) then style,
    content = content
)
