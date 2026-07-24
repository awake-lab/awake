// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnStyles
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnSurfaceVariant
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.ext.surface
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

/** Real shadcn's `Surface`: a contained region (Card, Popover, Dialog) that owns its
 * background, border, and content padding. Composed from the [surface] primitive. */
fun UiScope.shadcnSurface(
    id: String,
    modifier: UiModifier = Modifier,
    variant: ShadcnSurfaceVariant = ShadcnSurfaceVariant.Card,
    style: Style = Style.Empty,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = surface(
    id = id,
    modifier = modifier,
    style = ShadcnStyles.surface(theme.asShadcnTheme(), variant) then style,
    content = content
)

/** [shadcnSurface] override for [ColumnScope]. */
fun ColumnScope.shadcnSurface(
    id: String,
    modifier: UiModifier = Modifier,
    variant: ShadcnSurfaceVariant = ShadcnSurfaceVariant.Card,
    style: Style = Style.Empty,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = surface(
    id = id,
    modifier = modifier,
    style = ShadcnStyles.surface(theme.asShadcnTheme(), variant) then style,
    content = content
)

/** [shadcnSurface] override for [RowScope]. */
fun RowScope.shadcnSurface(
    id: String,
    modifier: UiModifier = Modifier,
    variant: ShadcnSurfaceVariant = ShadcnSurfaceVariant.Card,
    style: Style = Style.Empty,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = surface(
    id = id,
    modifier = modifier,
    style = ShadcnStyles.surface(theme.asShadcnTheme(), variant) then style,
    content = content
)

/** [shadcnSurface] override for [BoxScope]. */
fun BoxScope.shadcnSurface(
    id: String,
    modifier: UiModifier = Modifier,
    variant: ShadcnSurfaceVariant = ShadcnSurfaceVariant.Card,
    style: Style = Style.Empty,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = surface(
    id = id,
    modifier = modifier,
    style = ShadcnStyles.surface(theme.asShadcnTheme(), variant) then style,
    content = content
)
