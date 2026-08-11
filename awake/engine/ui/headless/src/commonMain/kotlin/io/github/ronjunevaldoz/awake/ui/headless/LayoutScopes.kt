// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.layouts.AbsoluteScope as PrimitiveAbsoluteScope
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope as PrimitiveBoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope as PrimitiveColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope as PrimitiveRowScope

/** Public Headless receiver for vertical layout content. */
class ColumnScope internal constructor(
    internal val primitive: PrimitiveColumnScope,
)

/** Public Headless receiver for horizontal layout content. */
class RowScope internal constructor(
    internal val primitive: PrimitiveRowScope,
)

/** Public Headless receiver for overlapping layout content. */
class BoxScope internal constructor(
    internal val primitive: PrimitiveBoxScope,
)

/** Public Headless receiver for explicitly positioned layout content. */
class AbsoluteScope internal constructor(
    internal val primitive: PrimitiveAbsoluteScope,
)

internal fun PrimitiveColumnScope.asHeadlessScope(): ColumnScope = ColumnScope(this)

internal fun PrimitiveRowScope.asHeadlessScope(): RowScope = RowScope(this)

internal fun PrimitiveBoxScope.asHeadlessScope(): BoxScope = BoxScope(this)

internal fun PrimitiveAbsoluteScope.asHeadlessScope(): AbsoluteScope = AbsoluteScope(this)
