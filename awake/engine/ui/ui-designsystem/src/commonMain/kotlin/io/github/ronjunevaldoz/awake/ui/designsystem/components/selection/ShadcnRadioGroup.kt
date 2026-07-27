// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

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
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme
import io.github.ronjunevaldoz.awake.ui.unstyled.input.selection.checkbox
import io.github.ronjunevaldoz.awake.ui.style.*

// Real shadcn's RadioGroup item is a circular checkbox.checkbox() -- same box/inset-dot
// mechanics, just a Circle shapeSpec instead of a rounded square. No separate ui-unstyled
// primitive needed for that alone.
private fun shadcnRadioStyle(theme: UiTheme, style: Style): Style =
    ShadcnStyles.checkbox(theme.asShadcnTheme()) then Style { shape(UiShapeSpec.Circle) } then style

/** Real shadcn's `RadioGroup`: single-select among [options] -- clicking an unselected item
 * selects it; clicking the already-selected item is a no-op (checkbox()'s own toggle-off
 * return is discarded), since a real radio group has no way to end up with nothing selected
 * once one item is chosen. */
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
    options.forEachIndexed { index, label ->
        val clicked = checkbox(
            id = "$id.$index",
            checked = index == selectedIndex,
            label = label,
            modifier = modifier.height(24f.dp),
            style = radioStyle,
            boxSize = 16f.dp
        )
        if (clicked) resolved = index
        if (index != options.lastIndex) spacer(Modifier.height(gap))
    }
    return resolved
}
