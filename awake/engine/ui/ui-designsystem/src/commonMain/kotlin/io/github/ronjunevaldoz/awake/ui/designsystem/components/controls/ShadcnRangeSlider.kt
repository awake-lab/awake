// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.controls

import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.headless.input.rangeSlider
import io.github.ronjunevaldoz.awake.ui.style.*

/** Real shadcn's `Slider` in range mode: two draggable thumbs on one track, sharing its
 * track/fill/knob chrome with [shadcnSlider] via the same [shadcnSliderStyle]. Delegates
 * entirely to [rangeSlider]. */
fun UiScope.shadcnRangeSlider(
    id: String,
    min: Float,
    max: Float,
    valueStart: Float,
    valueEnd: Float,
    label: String? = null,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    enabled: Boolean = true
): Pair<Float, Float> = rangeSlider(
    id = id,
    min = min,
    max = max,
    valueStart = valueStart,
    valueEnd = valueEnd,
    label = label,
    modifier = modifier,
    style = shadcnSliderStyle(theme, style),
    enabled = enabled
)
