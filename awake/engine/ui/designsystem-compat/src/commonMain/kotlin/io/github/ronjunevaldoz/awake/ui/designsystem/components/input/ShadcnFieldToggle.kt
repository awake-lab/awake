// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.input

import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.ShadcnFieldOrientation
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.shadcnField
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.shadcnFieldLabel
import io.github.ronjunevaldoz.awake.ui.designsystem.components.selection.shadcnSwitch
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.style.Style

/** Deprecated Core bridge for `shadcnFieldToggle`: see the `shadcnField*` control family doc in
 * [io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldSwitch] --
 * a horizontal [shadcnField] whose [labelContent] composes a [shadcnFieldLabel] (or a plain
 * string label via the convenience overload) followed by the themed switch control. The public
 * Headless recipe remains a true toggle; this bridge preserves the Core-era form-control output.
 */

fun ColumnScope.shadcnFieldToggle(
    id: String,
    checked: Boolean,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    labelContent: UiScope.() -> Unit,
): Boolean {
    var resolved = checked
    shadcnField(modifier = modifier, orientation = ShadcnFieldOrientation.Horizontal) {
        labelContent()
        resolved = shadcnSwitch(id = id, checked = checked, style = style)
    }
    return resolved
}

fun ColumnScope.shadcnFieldToggle(
    id: String,
    checked: Boolean,
    height: Dp,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    labelContent: UiScope.() -> Unit,
): Boolean = shadcnFieldToggle(
    id = id,
    checked = checked,
    modifier = modifier.height(height),
    style = style,
    labelContent = labelContent,
)

fun ColumnScope.shadcnFieldToggle(
    id: String,
    label: String,
    checked: Boolean,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
): Boolean = shadcnFieldToggle(
    id = id,
    checked = checked,
    modifier = modifier,
    style = style,
) {
    shadcnFieldLabel(label)
}
