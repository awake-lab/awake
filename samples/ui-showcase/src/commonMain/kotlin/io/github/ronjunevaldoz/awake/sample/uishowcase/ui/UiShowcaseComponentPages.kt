// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCheckbox
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCollapsible
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSeparator
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSlider
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnToggle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnPropertyTextField
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnPropertyTextarea
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.layouts.spacer
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.padding
import io.github.ronjunevaldoz.awake.ui.rememberStateValue
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.layout.toDimension
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

internal fun ColumnScope.drawUiShowcaseButtonsPreview() {
    shadcnSupportingText("Matches docs/reference/shadcn-previews/button_variants_light.png for a direct side-by-side.")
    spacer(Modifier.height(8f.dp))
    row( horizontalArrangement = Arrangement.spacedBy(10f.dp), modifier = Modifier.copy(heightDimension = 40f.dp.toDimension())) {
        shadcnButton("showcase-button-primary", label = "Primary", variant = ShadcnButtonVariant.Primary, modifier = Modifier.width(120f.dp))
        shadcnButton("showcase-button-secondary", label = "Secondary", variant = ShadcnButtonVariant.Secondary, modifier = Modifier.width(120f.dp))
        shadcnButton("showcase-button-outline", label = "Outline", variant = ShadcnButtonVariant.Outline, modifier = Modifier.width(112f.dp))
    }
    row( horizontalArrangement = Arrangement.spacedBy(10f.dp), modifier = Modifier.copy(heightDimension = 40f.dp.toDimension())) {
        shadcnButton("showcase-button-ghost", label = "Ghost", variant = ShadcnButtonVariant.Ghost, modifier = Modifier.width(100f.dp))
        shadcnButton("showcase-button-danger", label = "Danger", variant = ShadcnButtonVariant.Danger, modifier = Modifier.width(108f.dp))
    }
}

internal fun ColumnScope.drawUiShowcaseTextInputPreview(state: UiShowcaseRuntimeState) {
    val name = context.rememberStateValue("ui-showcase-text-input", "name") { "" }
    val email = context.rememberStateValue("ui-showcase-text-input", "email") { "" }

    shadcnSupportingText("Click a field, type, backspace, and use the arrow keys -- this is a real keyboard-driven widget, not a mockup.")
    spacer(Modifier.height(8f.dp))
    shadcnPropertyTextField(
        id = "showcase-name",
        label = "Name",
        value = name.value,
        placeholder = "Jane Doe"
    ).also { name.value = it }
    shadcnPropertyTextField(
        id = "showcase-email",
        label = "Email",
        value = email.value,
        placeholder = "jane@example.com"
    ).also { email.value = it }

    val bio = context.rememberStateValue("ui-showcase-text-input", "bio") { "" }
    shadcnPropertyTextarea(
        id = "showcase-bio",
        label = "Bio",
        value = bio.value,
        placeholder = "Tell us about yourself...",
        minLines = 4
    ).also { bio.value = it }

    spacer(Modifier.height(8f.dp))
    shadcnSupportingText(
        if (name.value.isEmpty() && email.value.isEmpty()) {
            "Nothing typed yet."
        } else {
            "You typed: ${name.value.ifEmpty { "(name)" }} / ${email.value.ifEmpty { "(email)" }}"
        }
    )
}

internal fun ColumnScope.drawUiShowcaseCollapsiblePreview(state: UiShowcaseRuntimeState) {
    val expanded = context.rememberStateValue("ui-showcase-collapsible", "expanded") { false }

    shadcnSupportingText("An interactive panel that expands to show more content, with a smooth height transition.")
    spacer(Modifier.height(8f.dp))

    shadcnCollapsible(
        id = "showcase-collapsible",
        title = "...",
        expanded = expanded.value,
        onExpandedChange = { expanded.value = it }
    ) {
        column(
            verticalArrangement = Arrangement.spacedBy(8f.dp)
        , modifier = Modifier.copy(widthDimension = Dimension.FillMax, heightDimension = Dimension.WrapContent)) {
            shadcnSeparator(modifier = Modifier.padding(0f.dp, 4f.dp, 0f.dp, 4f.dp))
            row(modifier = Modifier.fillMaxWidth().height(32f.dp)) {
                text("@radix-ui/primitives", modifier = Modifier.padding(12f.dp, 0f.dp, 0f.dp, 0f.dp))
            }
            row(modifier = Modifier.fillMaxWidth().height(32f.dp)) {
                text("@radix-ui/colors", modifier = Modifier.padding(12f.dp, 0f.dp, 0f.dp, 0f.dp))
            }
            row(modifier = Modifier.fillMaxWidth().height(32f.dp)) {
                text("@stitches/react", modifier = Modifier.padding(12f.dp, 0f.dp, 0f.dp, 0f.dp))
            }
        }
    }
}

internal fun ColumnScope.drawUiShowcaseSliderPreview() {
    val exposure = context.rememberStateValue("ui-showcase-slider", "exposure") { 52f }
    val bloom = context.rememberStateValue("ui-showcase-slider", "bloom") { 18f }

    shadcnSupportingText("Use sliders when you need a continuous value with immediate visual feedback and a stable control footprint.")
    spacer(Modifier.height(8f.dp))
    exposure.value = shadcnSlider(
        id = "showcase-slider-exposure",
        min = 0f,
        max = 100f,
        value = exposure.value,
        label = "Exposure ${exposure.value.toInt()}%",
        modifier = Modifier.width(360f.dp)
    )
    bloom.value = shadcnSlider(
        id = "showcase-slider-bloom",
        min = 0f,
        max = 32f,
        value = bloom.value,
        label = "Bloom ${bloom.value.toInt()} px",
        modifier = Modifier.width(360f.dp)
    )
    spacer(Modifier.height(8f.dp))
}

internal fun ColumnScope.drawUiShowcaseSelectionPreview() {
    val wireframe = context.rememberStateValue("ui-showcase-selection", "wireframe") { true }
    val stats = context.rememberStateValue("ui-showcase-selection", "stats") { false }

    shadcnSupportingText("These are the current Awake-owned selection controls. They already share the same shadcn token layer even though radio and tab families are still pending.")
    spacer(Modifier.height(8f.dp))
    wireframe.value = shadcnToggle(
        id = "showcase-selection-wireframe",
        checked = wireframe.value,
        label = "Wireframe overlay",
        modifier = Modifier.width(220f.dp).height(24f.dp)
    )
    stats.value = shadcnCheckbox(
        id = "showcase-selection-stats",
        checked = stats.value,
        label = "Scene statistics",
        modifier = Modifier.width(220f.dp).height(24f.dp)
    )
    spacer(Modifier.height(8f.dp))
    shadcnSupportingText(
        "Selection state now lives in the same preview lane as fields and overlays, so color inversions and label alignment regressions show up faster."
    )
}
