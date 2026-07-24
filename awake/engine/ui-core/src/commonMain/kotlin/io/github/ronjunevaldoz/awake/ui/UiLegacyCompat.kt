// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.core.graphics.clip as coreClip
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.layouts.AbsoluteScope
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.layouts.ext.spacer as extSpacer
import io.github.ronjunevaldoz.awake.ui.layouts.ext.surface as extSurface
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

fun UiScope.clip(rect: UiSlot, content: UiScope.() -> Unit) = coreClip(rect, content)

fun UiScope.clip(shape: UiShapeSpec, rect: UiSlot, content: UiScope.() -> Unit) =
    coreClip(shape, rect, content)

fun UiScope.surface(
    id: String,
    verticalArrangement: Arrangement = io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement(),
    style: Style = Style.Empty,
    modifier: UiModifier = Modifier,
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = extSurface(id, verticalArrangement, style, modifier, clipContent, content)

fun ColumnScope.surface(
    id: String,
    verticalArrangement: Arrangement = io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement(),
    style: Style = Style.Empty,
    modifier: UiModifier = Modifier,
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = extSurface(id, verticalArrangement, style, modifier, clipContent, content)

fun RowScope.surface(
    id: String,
    verticalArrangement: Arrangement = io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement(),
    style: Style = Style.Empty,
    modifier: UiModifier = Modifier,
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = extSurface(id, verticalArrangement, style, modifier, clipContent, content)

fun AbsoluteScope.surface(
    id: String,
    verticalArrangement: Arrangement = io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement(),
    style: Style = Style.Empty,
    modifier: UiModifier = Modifier,
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = extSurface(id, verticalArrangement, style, modifier, clipContent, content)

fun BoxScope.surface(
    id: String,
    verticalArrangement: Arrangement = io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement(),
    style: Style = Style.Empty,
    modifier: UiModifier = Modifier,
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = extSurface(id, verticalArrangement, style, modifier, clipContent, content)

fun ColumnScope.spacer(modifier: UiModifier) = extSpacer(modifier)

fun RowScope.spacer(modifier: UiModifier) = extSpacer(modifier)
