// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.input

import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.components.controls.shadcnRangeSlider
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.shadcnFieldLabel
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme
import kotlin.math.round

private fun defaultRangeSliderValueLabel(start: Float, end: Float): String {
    fun round1(value: Float) = round(value * 10f) / 10f
    return "${round1(start)}, ${round1(end)}"
}

/**
 * `shadcnFieldRangeSlider`: unlike the rest of the `shadcnField*` family (label beside control,
 * see [io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldSwitch]'s family
 * doc), a two-knob track has no single "beside the control" spot for its label. Matches real
 * shadcn's own range-mode `Slider` demo instead: a row with the label on the left and the
 * current `"start, end"` pair right-aligned in muted text, with the full-width [shadcnRangeSlider]
 * track below.
 */
fun ColumnScope.shadcnFieldRangeSlider(
    id: String,
    label: String,
    min: Float,
    max: Float,
    valueStart: Float,
    valueEnd: Float,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    valueLabel: (Float, Float) -> String = ::defaultRangeSliderValueLabel,
): Pair<Float, Float> {
    var resolved = valueStart to valueEnd
    column(
        verticalArrangement = Arrangement.spacedBy(theme.asShadcnTheme().spacing.xs),
        modifier = modifier,
    ) {
        row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            shadcnFieldLabel(label)
            shadcnText(valueLabel(valueStart, valueEnd), muted = true)
        }
        // 32dp (rangeSlider's own withSizeFallback height, not shadcnFieldSlider's 40dp) --
        // that single-slider height is generous because its label sits inline beside the knob
        // in the same row (needs headroom for the label's own line height); this component's
        // label is a separate row above, so the track itself only needs room for its 26px knob.
        resolved = shadcnRangeSlider(
            id = id,
            min = min,
            max = max,
            valueStart = valueStart,
            valueEnd = valueEnd,
            label = label,
            modifier = Modifier.fillMaxWidth().height(20f.dp),
            style = style,
        )
    }
    return resolved
}
