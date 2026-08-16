// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.style.Style

/**
 * Shared shape behind [menubar] and [toolbar]: a [Panel]-rooted bounded row. Both Base UI
 * components reduce to the same thing at this layer -- a horizontal strip of interactive
 * children with no visual opinion. Neither adds keyboard roving-focus/ARIA semantics; add that
 * to this one function if a real consumer needs it, rather than forking it into two.
 */
private fun UiScope.actionRow(
    id: String,
    modifier: Modifier,
    style: Style,
    horizontalArrangement: Arrangement,
    content: RowScope.() -> Unit,
): UiBounds = surface(id = id, modifier = modifier, style = style) {
    row(horizontalArrangement = horizontalArrangement, content = { _ -> content() })
}

/**
 * Horizontal strip of menu triggers.
 *
 * NOT a Radix/shadcn Menubar, despite the name: that primitive owns roving focus, typeahead,
 * submenu traversal and menu-level ARIA, and this is [toolbar] under another name -- a bounded
 * row with no behavior of its own. The name promises a contract the code does not keep, which is
 * exactly what the Radix-canonical naming rule exists to prevent, so it is flagged rather than
 * left to be discovered by whoever first relies on it. No production caller exists today.
 */
@Deprecated(
    "Not a Radix Menubar -- no roving focus, typeahead, or menu semantics. Use toolbar() for an " +
        "action strip; a real menubar needs the trigger/popup behavior in Menu.kt.",
    ReplaceWith("toolbar(id, modifier, style, horizontalArrangement, content)"),
)
fun UiScope.menubar(
    id: String,
    modifier: Modifier = Modifier,
    style: Style = Style.Empty,
    horizontalArrangement: Arrangement = Arrangement.Start,
    content: RowScope.() -> Unit,
): UiBounds = actionRow(id, modifier, style, horizontalArrangement, content)

/** Horizontal strip of action buttons/toggles. */
fun UiScope.toolbar(
    id: String,
    modifier: Modifier = Modifier,
    style: Style = Style.Empty,
    horizontalArrangement: Arrangement = Arrangement.Start,
    content: RowScope.() -> Unit,
): UiBounds = actionRow(id, modifier, style, horizontalArrangement, content)
