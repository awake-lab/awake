// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.input

import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.designsystem.components.controls.shadcnSlider
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.ShadcnFieldOrientation
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.shadcnField
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.shadcnFieldLabel
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.weight
import io.github.ronjunevaldoz.awake.ui.style.*

/** `shadcnFieldSlider`: see the `shadcnField*` control family doc in
 * [io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldSwitch] --
 * a horizontal [shadcnField] whose [labelContent] composes a [shadcnFieldLabel] (or a plain
 * string label via the convenience overload) followed by the themed [shadcnSlider] control. */

fun ColumnScope.shadcnFieldSlider(
    id: String,
    min: Float,
    max: Float,
    value: Float,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    labelContent: UiScope.() -> Unit
): Float {
    var resolved = value
    shadcnField(modifier = modifier, orientation = ShadcnFieldOrientation.Horizontal) {
        labelContent()
        resolved = shadcnSlider(
            id = id,
            min = min,
            max = max,
            value = value,
            modifier = Modifier.weight(1f).height(40f.dp),
            style = style
        )
    }
    return resolved
}

fun ColumnScope.shadcnFieldSlider(
    id: String,
    min: Float,
    max: Float,
    value: Float,
    height: Dp,
    style: Style = Style.Empty,
    labelContent: UiScope.() -> Unit
): Float {
    var resolved = value
    shadcnField(modifier = Modifier.height(height), orientation = ShadcnFieldOrientation.Horizontal) {
        labelContent()
        resolved = shadcnSlider(
            id = id,
            min = min,
            max = max,
            value = value,
            modifier = Modifier.weight(1f).height(height),
            style = style
        )
    }
    return resolved
}

fun ColumnScope.shadcnFieldSlider(
    id: String,
    label: String,
    min: Float,
    max: Float,
    value: Float,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty
): Float = shadcnFieldSlider(
    id = id,
    min = min,
    max = max,
    value = value,
    modifier = modifier,
    style = style
) {
    shadcnFieldLabel(label)
}
