// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds

/**
 * Shared shape behind [menubar] and [toolbar]: a [Panel]-rooted bounded row. Both Base UI
 * components reduce to the same thing at this layer -- a horizontal strip of interactive
 * children with no visual opinion. Neither adds keyboard roving-focus/ARIA semantics; add that
 * to this one function if a real consumer needs it, rather than forking it into two.
 */
private fun UiScope.actionRow(
    id: String,
    modifier: Modifier,
    style: SurfaceStyle,
    horizontalArrangement: Arrangement,
    content: RowScope.() -> Unit,
): UiBounds = surface(id = id, modifier = modifier, style = style) {
    row(horizontalArrangement = horizontalArrangement, content = { _ -> content() })
}

/** Horizontal strip of menu triggers. See [Menu.kt] for the trigger/popup behavior itself. */
fun UiScope.menubar(
    id: String,
    modifier: Modifier = Modifier,
    style: SurfaceStyle = SurfaceStyle(),
    horizontalArrangement: Arrangement = Arrangement.Start,
    content: RowScope.() -> Unit,
): UiBounds = actionRow(id, modifier, style, horizontalArrangement, content)

/** Horizontal strip of action buttons/toggles. */
fun UiScope.toolbar(
    id: String,
    modifier: Modifier = Modifier,
    style: SurfaceStyle = SurfaceStyle(),
    horizontalArrangement: Arrangement = Arrangement.Start,
    content: RowScope.() -> Unit,
): UiBounds = actionRow(id, modifier, style, horizontalArrangement, content)
