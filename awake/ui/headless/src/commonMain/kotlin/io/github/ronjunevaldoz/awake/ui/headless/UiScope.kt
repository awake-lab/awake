// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.UiPrimitiveScope
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.context.UiContext

@DslMarker
annotation class AwakeUiDsl

/**
 * Public receiver for ordinary Headless widgets and design-system recipes.
 *
 * The runtime primitive scope is intentionally internal: callers compose behavior through this
 * type and cannot reach Core's frame, draw, or input escape hatches from a widget recipe.
 */
@AwakeUiDsl
interface UiScope {
    val primitive: UiPrimitiveScope

    /** Immutable values from the currently installed theme, without Core runtime access. */
    val themeValues: UiThemeValues
        get() = primitive.context.currentTheme
}

internal class DefaultUiScope internal constructor(
    override val primitive: UiPrimitiveScope,
) : UiScope

fun UiScope(primitive: UiPrimitiveScope): UiScope = DefaultUiScope(primitive)

/** Requests keyboard focus for a Headless-owned input or composite widget. */
fun UiScope.requestFocus(id: String) = primitive.context.requestFocus(id)

/**
 * Creates the public Headless receiver for a root UI region.
 *
 * App integration owns [UiContext]; ordinary widgets receive only [UiScope]. The explicit
 * [slot] keeps root sizing at the app/runtime boundary rather than inventing a Headless size.
 */
fun UiContext.createUiScope(slot: UiBounds): UiScope =
    UiScope(createBox(slot = slot))

/** Internal bridge used while Headless behavior is migrated from Core's raw receiver. */
internal fun UiPrimitiveScope.asUiScope(): UiScope = UiScope(this)
