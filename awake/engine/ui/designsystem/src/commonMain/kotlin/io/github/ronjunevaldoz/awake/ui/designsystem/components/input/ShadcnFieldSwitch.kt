// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.input

import io.github.ronjunevaldoz.awake.ui.Dp
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

/** The `shadcnField*` control family: each is a horizontal [shadcnField]
 * (label beside control -- matches this family's pre-existing property-row look) whose
 * [labelContent] composes a [shadcnFieldLabel] (or a plain string label via the convenience
 * overload) followed by the themed control itself. Sibling families:
 * [io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldToggle],
 * [io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldDropdown],
 * [io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldTextField],
 * [io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldTextarea],
 * [io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldSlider]. */

fun ColumnScope.shadcnFieldSwitch(
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

fun ColumnScope.shadcnFieldSwitch(
    id: String,
    checked: Boolean,
    height: Dp,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    labelContent: UiScope.() -> Unit,
): Boolean = shadcnFieldSwitch(
    id = id,
    checked = checked,
    modifier = modifier.height(height),
    style = style,
    labelContent = labelContent,
)

fun ColumnScope.shadcnFieldSwitch(
    id: String,
    label: String,
    checked: Boolean,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
): Boolean = shadcnFieldSwitch(
    id = id,
    checked = checked,
    modifier = modifier,
    style = style,
) {
    shadcnFieldLabel(label)
}
