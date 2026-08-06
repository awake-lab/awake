// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.selection

import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnStyles
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.spacer
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.unstyled.input.selection.checkbox
import io.github.ronjunevaldoz.awake.ui.style.*

// Real shadcn's RadioGroup item is a circular checkbox.checkbox() -- same box/inset-dot
// mechanics, just a Circle shapeSpec instead of a rounded square. No separate ui-unstyled
// primitive needed for that alone.
internal fun shadcnRadioStyle(theme: UiTheme, style: Style): Style =
    ShadcnStyles.checkbox(theme.asShadcnTheme()) then Style { shape(UiShapeSpec.Circle) } then style

/** Real shadcn's `ShadcnRadioButton`: a bare circular indicator, standalone and reusable
 * inside any caller-composed row (icon, label, description...) -- see [shadcnRadioGroup]'s
 * primary Slot API form. Delegates to [checkbox] with a Circle shape and no label, exactly
 * like [shadcnRadioGroup]'s `List<String>` convenience overload used to build inline. */
fun UiScope.shadcnRadioButton(
    id: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty
) {
    val boxSize = 16f.dp
    val newChecked = checkbox(
        id = id,
        checked = selected,
        label = null,
        modifier = modifier.width(boxSize).height(boxSize),
        style = shadcnRadioStyle(theme, style),
        boxSize = boxSize
    )
    if (newChecked != selected) onClick()
}

/**
 * Real shadcn's `ShadcnRadioGroup`: caller composes each item's whole row (icon, label,
 * description, anything) via [content], placing a [shadcnRadioButton] wherever the
 * indicator belongs -- mirrors real shadcn-compose's `RadioGroup + RadioGroupItem` split
 * instead of a monolithic "options list" API. See the `List<String>` overload below for the
 * previous fixed-row behavior, now expressed as a convenience wrapper over this primary form.
 */
fun ColumnScope.shadcnRadioGroup(
    id: String,
    modifier: UiModifier = Modifier,
    content: ColumnScope.() -> Unit
) {
    content()
}

/** [shadcnRadioGroup] convenience: a fixed label-only row per option, single-select among
 * [options] -- clicking an unselected item selects it; clicking the already-selected item is
 * a no-op, since a real radio group has no way to end up with nothing selected once one item
 * is chosen. */
fun ColumnScope.shadcnRadioGroup(
    id: String,
    options: List<String>,
    selectedIndex: Int,
    modifier: UiModifier = Modifier,
    gap: Dp = 8f.dp,
    style: Style = Style.Empty
): Int {
    var resolved = selectedIndex
    val radioStyle = shadcnRadioStyle(theme, style)
    shadcnRadioGroup(id = id, modifier = modifier) {
        options.forEachIndexed { index, label ->
            val wasSelected = index == selectedIndex
            // checkbox() returns the post-click checked value:
            //   unselected item clicked → returns true  → select it
            //   selected item clicked   → returns false → no-op (radio groups don't deselect)
            val newChecked = checkbox(
                id = "$id.$index",
                checked = wasSelected,
                label = label,
                // Do NOT forward the group modifier to each item: the group modifier carries a
                // width constraint for the whole group container, not for individual rows.
                modifier = Modifier.height(24f.dp),
                style = radioStyle,
                boxSize = 16f.dp
            )
            // Only update when clicking an unselected item (newChecked=true means it just
            // transitioned from unchecked→checked, i.e. a new selection was made).
            if (newChecked && !wasSelected) resolved = index
            if (index != options.lastIndex) spacer(Modifier.height(gap))
        }
    }
    return resolved
}
