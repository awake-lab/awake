// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui.pages.inputs

import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcaseCategory
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.ShowcasePage
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnFieldSlider
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.rememberStateValue
import io.github.ronjunevaldoz.awake.ui.headless.spacer

internal val SliderPage = ShowcasePage(
    id = "slider",
    title = "Slider",
    category = ShowcaseCategory.Inputs,
    description = "An input where the user selects a value from within a given range.",
    usageCode = """shadcnSlider(id = "vol", min = 0f, max = 100f, value = 50f)""",
    referenceExample = "registry/new-york-v4/examples/slider-demo.tsx",
    previewHeight = 380,
    notes = listOf("Track fill with thumb drag interaction."),
    hero = {
        var value by rememberStateValue("ui-showcase-slider", "value") { 60f }
        value = shadcnFieldSlider(
            id = "showcase-slider",
            label = "Volume",
            min = 0f,
            max = 100f,
            value = value,
        )
    },
    states = {
        listOf("Low" to 10f, "Mid" to 50f, "High" to 95f).forEach { (label, value) ->
            shadcnFieldSlider(
                id = "slider-state-${label.lowercase()}",
                label = label,
                min = 0f,
                max = 100f,
                value = value,
            )
            spacer(Modifier.height(8f.dp))
        }
    },
)
