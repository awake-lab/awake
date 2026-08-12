// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.layouts.AbsoluteScope as PrimitiveAbsoluteScope
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope as PrimitiveBoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope as PrimitiveColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope as PrimitiveRowScope

/** Public Headless receiver for vertical layout content. */
class ColumnScope internal constructor(
    override val primitive: PrimitiveColumnScope,
) : UiScope

/** Public Headless receiver for horizontal layout content. */
class RowScope internal constructor(
    override val primitive: PrimitiveRowScope,
) : UiScope

/** Public Headless receiver for overlapping layout content. */
class BoxScope internal constructor(
    override val primitive: PrimitiveBoxScope,
) : UiScope

/** Public Headless receiver for explicitly positioned layout content. */
class AbsoluteScope internal constructor(
    override val primitive: PrimitiveAbsoluteScope,
) : UiScope

/**
 * Returns the public UI scope backed by this layout scope.
 *
 * This is useful for anchored overlays composed inside a layout lambda: popup behavior is still
 * Headless-owned, while the caller keeps the same Compose-style column/row receiver.
 */
fun ColumnScope.uiScope(): UiScope = UiScope(primitive)

fun RowScope.uiScope(): UiScope = UiScope(primitive)

fun BoxScope.uiScope(): UiScope = UiScope(primitive)

fun AbsoluteScope.uiScope(): UiScope = UiScope(primitive)

internal fun PrimitiveColumnScope.asHeadlessScope(): ColumnScope = ColumnScope(this)

internal fun PrimitiveRowScope.asHeadlessScope(): RowScope = RowScope(this)

internal fun PrimitiveBoxScope.asHeadlessScope(): BoxScope = BoxScope(this)

internal fun PrimitiveAbsoluteScope.asHeadlessScope(): AbsoluteScope = AbsoluteScope(this)
