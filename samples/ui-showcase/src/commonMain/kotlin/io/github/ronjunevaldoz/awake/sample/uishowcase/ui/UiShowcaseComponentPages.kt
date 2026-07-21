// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnCheckbox
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnCollapsible
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnSectionTitle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnSeparator
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnSlider
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnToggle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.awakeShadcnPropertyTextField
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.awakeShadcnPropertyTextarea
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.height
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.ext.column
import io.github.ronjunevaldoz.awake.ui.layouts.ext.rawRow
import io.github.ronjunevaldoz.awake.ui.layouts.ext.row
import io.github.ronjunevaldoz.awake.ui.layouts.ext.spacer
import io.github.ronjunevaldoz.awake.ui.padding
import io.github.ronjunevaldoz.awake.ui.rememberStateValue
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.width

internal fun ColumnScope.drawUiShowcaseButtonsPreview() {
    awakeShadcnSectionTitle("Buttons")
    awakeShadcnSupportingText("Matches docs/reference/shadcn-previews/button_variants_light.png for a direct side-by-side.")
    spacer(UiModifier().height(8f.dp))
    row(height = 40f.dp, gap = 10f) {
        awakeShadcnButton("showcase-button-primary", label = "Primary", variant = AwakeShadcnButtonVariant.Primary, modifier = UiModifier().width(120f.dp))
        awakeShadcnButton("showcase-button-secondary", label = "Secondary", variant = AwakeShadcnButtonVariant.Secondary, modifier = UiModifier().width(120f.dp))
        awakeShadcnButton("showcase-button-outline", label = "Outline", variant = AwakeShadcnButtonVariant.Outline, modifier = UiModifier().width(112f.dp))
    }
    row(height = 40f.dp, gap = 10f) {
        awakeShadcnButton("showcase-button-ghost", label = "Ghost", variant = AwakeShadcnButtonVariant.Ghost, modifier = UiModifier().width(100f.dp))
        awakeShadcnButton("showcase-button-danger", label = "Danger", variant = AwakeShadcnButtonVariant.Danger, modifier = UiModifier().width(108f.dp))
    }
}

internal fun ColumnScope.drawUiShowcaseTextInputPreview(state: UiShowcaseRuntimeState) {
    val name = context.rememberStateValue("ui-showcase-text-input", "name") { "" }
    val email = context.rememberStateValue("ui-showcase-text-input", "email") { "" }

    awakeShadcnSectionTitle("Text Input")
    awakeShadcnSupportingText("Click a field, type, backspace, and use the arrow keys -- this is a real keyboard-driven widget, not a mockup.")
    spacer(UiModifier().height(8f.dp))
    awakeShadcnPropertyTextField(
        id = "showcase-name",
        label = "Name",
        value = name.value,
        placeholder = "Jane Doe"
    ).also { name.value = it }
    awakeShadcnPropertyTextField(
        id = "showcase-email",
        label = "Email",
        value = email.value,
        placeholder = "jane@example.com"
    ).also { email.value = it }

    val bio = context.rememberStateValue("ui-showcase-text-input", "bio") { "" }
    awakeShadcnPropertyTextarea(
        id = "showcase-bio",
        label = "Bio",
        value = bio.value,
        placeholder = "Tell us about yourself...",
        minLines = 4
    ).also { bio.value = it }

    spacer(UiModifier().height(8f.dp))
    awakeShadcnSupportingText(
        if (name.value.isEmpty() && email.value.isEmpty()) {
            "Nothing typed yet."
        } else {
            "You typed: ${name.value.ifEmpty { "(name)" }} / ${email.value.ifEmpty { "(email)" }}"
        }
    )
}

internal fun ColumnScope.drawUiShowcaseCollapsiblePreview(state: UiShowcaseRuntimeState) {
    val expanded = context.rememberStateValue("ui-showcase-collapsible", "expanded") { false }

    awakeShadcnSectionTitle("Collapsible")
    awakeShadcnSupportingText("An interactive panel that expands to show more content, with a smooth height transition.")
    spacer(UiModifier().height(8f.dp))

    awakeShadcnCollapsible(
        id = "showcase-collapsible",
        title = "...",
        expanded = expanded.value,
        onExpandedChange = { expanded.value = it }
    ) {
        column(width = Dimension.FillMax, height = Dimension.WrapContent, gap = 8f) {
            awakeShadcnSeparator(modifier = UiModifier().padding(0f.dp, 4f.dp, 0f.dp, 4f.dp))
            rawRow(modifier = UiModifier().fillMaxWidth().height(32f.dp)) {
                text("@radix-ui/primitives", modifier = UiModifier().padding(12f.dp, 0f.dp, 0f.dp, 0f.dp))
            }
            rawRow(modifier = UiModifier().fillMaxWidth().height(32f.dp)) {
                text("@radix-ui/colors", modifier = UiModifier().padding(12f.dp, 0f.dp, 0f.dp, 0f.dp))
            }
            rawRow(modifier = UiModifier().fillMaxWidth().height(32f.dp)) {
                text("@stitches/react", modifier = UiModifier().padding(12f.dp, 0f.dp, 0f.dp, 0f.dp))
            }
        }
    }
}

internal fun ColumnScope.drawUiShowcaseSliderPreview() {
    val exposure = context.rememberStateValue("ui-showcase-slider", "exposure") { 52f }
    val bloom = context.rememberStateValue("ui-showcase-slider", "bloom") { 18f }

    awakeShadcnSectionTitle("Slider")
    awakeShadcnSupportingText("Use sliders when you need a continuous value with immediate visual feedback and a stable control footprint.")
    spacer(UiModifier().height(8f.dp))
    exposure.value = awakeShadcnSlider(
        id = "showcase-slider-exposure",
        min = 0f,
        max = 100f,
        value = exposure.value,
        label = "Exposure ${exposure.value.toInt()}%",
        modifier = UiModifier().width(360f.dp)
    )
    bloom.value = awakeShadcnSlider(
        id = "showcase-slider-bloom",
        min = 0f,
        max = 32f,
        value = bloom.value,
        label = "Bloom ${bloom.value.toInt()} px",
        modifier = UiModifier().width(360f.dp)
    )
    spacer(UiModifier().height(8f.dp))
}

internal fun ColumnScope.drawUiShowcaseSelectionPreview() {
    val wireframe = context.rememberStateValue("ui-showcase-selection", "wireframe") { true }
    val stats = context.rememberStateValue("ui-showcase-selection", "stats") { false }

    awakeShadcnSectionTitle("Toggle And Checkbox")
    awakeShadcnSupportingText("These are the current Awake-owned selection controls. They already share the same shadcn token layer even though radio and tab families are still pending.")
    spacer(UiModifier().height(8f.dp))
    wireframe.value = awakeShadcnToggle(
        id = "showcase-selection-wireframe",
        checked = wireframe.value,
        label = "Wireframe overlay",
        modifier = UiModifier().width(220f.dp).height(24f.dp)
    )
    stats.value = awakeShadcnCheckbox(
        id = "showcase-selection-stats",
        checked = stats.value,
        label = "Scene statistics",
        modifier = UiModifier().width(220f.dp).height(24f.dp)
    )
    spacer(UiModifier().height(8f.dp))
    awakeShadcnSupportingText(
        "Selection state now lives in the same preview lane as fields and overlays, so color inversions and label alignment regressions show up faster."
    )
}
