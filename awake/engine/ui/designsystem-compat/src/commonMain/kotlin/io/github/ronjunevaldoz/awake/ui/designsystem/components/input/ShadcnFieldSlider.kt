// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.input

import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.components.controls.shadcnSlider
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.ShadcnFieldOrientation
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.shadcnField
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.shadcnFieldLabel
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.weight
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme
import kotlin.math.round

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
    labelContent: UiScope.() -> Unit,
): Float {
    var resolved = value
    shadcnField(modifier = modifier, orientation = ShadcnFieldOrientation.Horizontal) {
        labelContent()
        resolved = shadcnSlider(
            id = id,
            min = min,
            max = max,
            value = value,
            modifier = Modifier.weight(1f).height(20f.dp),
            style = style,
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
    labelContent: UiScope.() -> Unit,
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
            style = style,
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
    style: Style = Style.Empty,
): Float = shadcnFieldSlider(
    id = id,
    min = min,
    max = max,
    value = value,
    modifier = modifier,
    style = style,
) {
    shadcnFieldLabel(label)
}

private fun defaultSliderValueLabel(value: Float, min: Float, max: Float): String {
    fun round1(number: Float) = round(number * 10f) / 10f
    return "${round1(value)} (${round1(min)}-${round1(max)})"
}

/**
 * `shadcnFieldSliderWithValue`: unlike the rest of the `shadcnFieldSlider` family (label beside
 * the knob, no value shown), this shows the live value and [min]/[max] range as its own
 * right-aligned muted text next to the label -- a label row above, the full-width [shadcnSlider]
 * track below -- the same layout [shadcnFieldRangeSlider] uses, so a single-value slider and a
 * range slider read consistently side by side in a controls panel with many sliders.
 */
fun ColumnScope.shadcnFieldSliderWithValue(
    id: String,
    label: String,
    min: Float,
    max: Float,
    value: Float,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    enabled: Boolean = true,
    valueLabel: (Float, Float, Float) -> String = ::defaultSliderValueLabel,
): Float {
    var resolved = value
    column(
        verticalArrangement = Arrangement.spacedBy(theme.asShadcnTheme().spacing.xs),
        modifier = modifier,
    ) {
        row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            shadcnFieldLabel(label)
            shadcnText(valueLabel(value, min, max), muted = true)
        }
        resolved = shadcnSlider(
            id = id,
            min = min,
            max = max,
            value = value,
            modifier = Modifier.fillMaxWidth().height(20f.dp),
            style = style,
            enabled = enabled,
        )
    }
    return resolved
}
