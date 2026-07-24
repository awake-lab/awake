// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.core.graphics.clip as coreClip
import io.github.ronjunevaldoz.awake.ui.layouts.AbsoluteScope
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.layouts.ext.spacer as extSpacer
import io.github.ronjunevaldoz.awake.ui.layouts.ext.surface as extSurface

fun UiScope.clip(rect: UiSlot, content: UiScope.() -> Unit) = coreClip(rect, content)

fun UiScope.clip(shape: UiShapeSpec, rect: UiSlot, content: UiScope.() -> Unit) =
    coreClip(shape, rect, content)

fun UiScope.surface(
    id: String,
    width: Dimension,
    height: Dimension,
    verticalArrangement: Arrangement = io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement(),
    radius: Dp = UiShape.md,
    borderWidth: Dp = UiShape.none,
    style: Style = Style.Empty,
    modifier: UiModifier = UiModifier(),
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = extSurface(id, width, height, verticalArrangement, radius, borderWidth, style, modifier, clipContent, content)

fun ColumnScope.surface(
    id: String,
    width: Dimension,
    height: Dimension,
    verticalArrangement: Arrangement = io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement(),
    radius: Dp = UiShape.md,
    borderWidth: Dp = UiShape.none,
    style: Style = Style.Empty,
    modifier: UiModifier = UiModifier(),
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = extSurface(id, height, width, verticalArrangement, radius, borderWidth, style, modifier, clipContent, content)

fun RowScope.surface(
    id: String,
    width: Dimension,
    height: Dimension = Dimension.FillMax,
    verticalArrangement: Arrangement = io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement(),
    radius: Dp = UiShape.md,
    borderWidth: Dp = UiShape.none,
    style: Style = Style.Empty,
    modifier: UiModifier = UiModifier(),
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = extSurface(id, width, height, verticalArrangement, radius, borderWidth, style, modifier, clipContent, content)

fun AbsoluteScope.surface(
    id: String,
    width: Dimension,
    height: Dimension,
    verticalArrangement: Arrangement = io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement(),
    radius: Dp = UiShape.md,
    borderWidth: Dp = UiShape.none,
    style: Style = Style.Empty,
    modifier: UiModifier = UiModifier(),
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = extSurface(id, width, height, verticalArrangement, radius, borderWidth, style, modifier, clipContent, content)

fun BoxScope.surface(
    id: String,
    width: Dimension = Dimension.WrapContent,
    height: Dimension = Dimension.WrapContent,
    verticalArrangement: Arrangement = io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement(),
    radius: Dp = UiShape.md,
    borderWidth: Dp = UiShape.none,
    style: Style = Style.Empty,
    modifier: UiModifier = UiModifier(),
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = extSurface(id, width, height, verticalArrangement, radius, borderWidth, style, modifier, clipContent, content)

fun ColumnScope.spacer(modifier: UiModifier) = extSpacer(modifier)

fun RowScope.spacer(modifier: UiModifier) = extSpacer(modifier)
