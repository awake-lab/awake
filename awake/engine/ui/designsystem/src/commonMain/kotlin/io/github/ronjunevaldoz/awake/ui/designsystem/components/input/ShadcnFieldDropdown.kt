// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.input

import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.designsystem.components.controls.shadcnSelect
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.ShadcnFieldOrientation
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.shadcnField
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.shadcnFieldLabel
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonSize
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.weight
import io.github.ronjunevaldoz.awake.ui.style.Style

/** `shadcnFieldDropdown`: see the `shadcnField*` control family doc in
 * [io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldSwitch] --
 * a horizontal [shadcnField] whose [labelContent] composes a [shadcnFieldLabel] (or a plain
 * string label via the convenience overload) followed by the themed [shadcnSelect] control. */

fun ColumnScope.shadcnFieldDropdown(
    id: String,
    options: List<String>,
    selectedIndex: Int,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    labelContent: UiScope.() -> Unit,
): Int? {
    var resolved: Int? = null
    shadcnField(modifier = modifier, orientation = ShadcnFieldOrientation.Horizontal) {
        labelContent()
        resolved = shadcnSelect(
            id = id,
            options = options,
            selectedIndex = selectedIndex,
            // Matches shadcnSelect's own trigger height -- this wrapper previously forced a
            // hand-typed 40dp (h-10), the only place doing so.
            modifier = Modifier.weight(1f).height(ShadcnButtonSize.Md.heightDp),
            style = style,
        )
    }
    return resolved
}

fun ColumnScope.shadcnFieldDropdown(
    id: String,
    options: List<String>,
    selectedIndex: Int,
    height: Dp,
    style: Style = Style.Empty,
    labelContent: UiScope.() -> Unit,
): Int? {
    var resolved: Int? = null
    shadcnField(modifier = Modifier.height(height), orientation = ShadcnFieldOrientation.Horizontal) {
        labelContent()
        resolved = shadcnSelect(
            id = id,
            options = options,
            selectedIndex = selectedIndex,
            modifier = Modifier.weight(1f).height(height),
            style = style,
        )
    }
    return resolved
}

fun ColumnScope.shadcnFieldDropdown(
    id: String,
    label: String,
    options: List<String>,
    selectedIndex: Int,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
): Int? = shadcnFieldDropdown(
    id = id,
    options = options,
    selectedIndex = selectedIndex,
    modifier = modifier,
    style = style,
) {
    shadcnFieldLabel(label)
}
