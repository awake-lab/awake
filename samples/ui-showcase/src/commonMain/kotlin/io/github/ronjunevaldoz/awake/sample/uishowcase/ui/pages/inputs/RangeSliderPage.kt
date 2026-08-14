// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.inputs

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnFieldRangeSlider
import io.github.ronjunevaldoz.awake.ui.headless.rememberStateValue

internal val RangeSliderPage = ShowcasePage(
    id = "range-slider",
    title = "Range Slider",
    category = ShowcaseCategory.Inputs,
    description = "A slider with two thumbs that selects a bounded range instead of a single value.",
    usageCode = """shadcnRangeSlider(id = "price", min = 0f, max = 100f, valueStart = 20f, valueEnd = 80f)""",
    previewHeight = 320,
    notes = listOf("Both thumbs share one track; the fill spans only the selected interval."),
    hero = {
        var range by rememberStateValue("ui-showcase-range-slider", "range") { 20f to 80f }
        range = shadcnFieldRangeSlider(
            id = "showcase-range-slider",
            label = "Price",
            min = 0f,
            max = 100f,
            valueStart = range.first,
            valueEnd = range.second,
        )
    },
)
