// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnPropertyTextField
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnPropertyTextarea
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnAlert
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnAvatar
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBreadcrumb
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCheckbox
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCollapsible
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSeparator
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSlider
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnToggle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnAlertVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.layout.toDimension
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.layouts.spacer
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.align
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.padding
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.rememberStateValue
import io.github.ronjunevaldoz.awake.ui.unstyled.UiIcons
import io.github.ronjunevaldoz.awake.ui.unstyled.components.icon
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text

internal fun ColumnScope.drawUiShowcaseButtonsPreview() {
    shadcnSupportingText("Matches docs/reference/shadcn-previews/button_variants_light.png for a direct side-by-side.")
    spacer(Modifier.height(8f.dp))
    row( horizontalArrangement = Arrangement.spacedBy(10f.dp), modifier = Modifier.height(40f.dp.toDimension())) {
        shadcnButton("showcase-button-primary", label = "Primary", variant = ShadcnButtonVariant.Primary, modifier = Modifier.width(120f.dp))
        shadcnButton("showcase-button-secondary", label = "Secondary", variant = ShadcnButtonVariant.Secondary, modifier = Modifier.width(120f.dp))
        shadcnButton("showcase-button-outline", label = "Outline", variant = ShadcnButtonVariant.Outline, modifier = Modifier.width(112f.dp))
    }
    row( horizontalArrangement = Arrangement.spacedBy(10f.dp), modifier = Modifier.height(40f.dp.toDimension())) {
        shadcnButton("showcase-button-ghost", label = "Ghost", variant = ShadcnButtonVariant.Ghost, modifier = Modifier.width(100f.dp))
        shadcnButton("showcase-button-danger", label = "Danger", variant = ShadcnButtonVariant.Danger, modifier = Modifier.width(108f.dp))
    }
    spacer(Modifier.height(8f.dp))
    shadcnSupportingText("The Slot API overload composes an icon and label inside the same button instead of a fixed label string.")
    spacer(Modifier.height(8f.dp))
    row( horizontalArrangement = Arrangement.spacedBy(10f.dp), modifier = Modifier.height(40f.dp.toDimension())) {
        shadcnButton("showcase-button-icon", modifier = Modifier.width(140f.dp)) {
            row(horizontalArrangement = Arrangement.spacedBy(6f.dp)) {
                // row()'s children default to top alignment on the cross axis (matches
                // Compose's Row default) -- an icon much shorter than the text's line height
                // needs an explicit center to read as a real icon+label combo instead of
                // "riding high" above the label's visual center.
                icon(UiIcons.chevronDown, modifier = Modifier.align(UiAlignment.CenterStart))
                text("Expand", modifier = Modifier.align(UiAlignment.CenterStart))
            }
        }
    }
}

internal fun ColumnScope.drawUiShowcaseTextInputPreview(state: UiShowcaseRuntimeState) {
    var name by context.rememberStateValue("ui-showcase-text-input", "name") { "" }
    var email by context.rememberStateValue("ui-showcase-text-input", "email") { "" }

    shadcnSupportingText("Click a field, type, backspace, and use the arrow keys -- this is a real keyboard-driven widget, not a mockup.")
    spacer(Modifier.height(8f.dp))
    shadcnPropertyTextField(
        id = "showcase-name",
        label = "Name",
        value = name,
        placeholder = "Jane Doe"
    ).also { name = it }
    shadcnPropertyTextField(
        id = "showcase-email",
        label = "Email",
        value = email,
        placeholder = "jane@example.com"
    ).also { email = it }

    var bio by context.rememberStateValue("ui-showcase-text-input", "bio") { "" }
    shadcnPropertyTextarea(
        id = "showcase-bio",
        label = "Bio",
        value = bio,
        placeholder = "Tell us about yourself...",
        minLines = 4
    ).also { bio = it }

    spacer(Modifier.height(8f.dp))
    shadcnSupportingText(
        if (name.isEmpty() && email.isEmpty()) {
            "Nothing typed yet."
        } else {
            "You typed: ${name.ifEmpty { "(name)" }} / ${email.ifEmpty { "(email)" }}"
        }
    )
}

internal fun ColumnScope.drawUiShowcaseCollapsiblePreview(state: UiShowcaseRuntimeState) {
    var expanded by context.rememberStateValue("ui-showcase-collapsible", "expanded") { false }

    shadcnSupportingText("An interactive panel that expands to show more content, with a smooth height transition.")
    spacer(Modifier.height(8f.dp))

    shadcnCollapsible(
        id = "showcase-collapsible",
        title = "...",
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        column(
            verticalArrangement = Arrangement.spacedBy(8f.dp)
        , modifier = Modifier.width(Dimension.FillMax).height(Dimension.WrapContent)) {
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
    var exposure by context.rememberStateValue("ui-showcase-slider", "exposure") { 52f }
    var bloom by context.rememberStateValue("ui-showcase-slider", "bloom") { 18f }

    shadcnSupportingText("Use sliders when you need a continuous value with immediate visual feedback and a stable control footprint.")
    spacer(Modifier.height(8f.dp))
    exposure = shadcnSlider(
        id = "showcase-slider-exposure",
        min = 0f,
        max = 100f,
        value = exposure,
        label = "Exposure ${exposure.toInt()}%",
        modifier = Modifier.width(360f.dp)
    )
    bloom = shadcnSlider(
        id = "showcase-slider-bloom",
        min = 0f,
        max = 32f,
        value = bloom,
        label = "Bloom ${bloom.toInt()} px",
        modifier = Modifier.width(360f.dp)
    )
    spacer(Modifier.height(8f.dp))

    var volume by context.rememberStateValue("ui-showcase-slider", "volume") { 65f }
    shadcnSupportingText("A label-beside-track layout for a compact row (e.g. a settings list row).")
    spacer(Modifier.height(8f.dp))
    row(horizontalArrangement = Arrangement.spacedBy(10f.dp), modifier = Modifier.height(32f.dp.toDimension())) {
        text("Volume", modifier = Modifier.width(64f.dp))
        volume = shadcnSlider(
            id = "showcase-slider-volume",
            min = 0f,
            max = 100f,
            value = volume,
            modifier = Modifier.width(220f.dp)
        )
        text("${volume.toInt()}%", modifier = Modifier.width(40f.dp))
    }
    spacer(Modifier.height(8f.dp))

    shadcnSupportingText("Min/max range labels flanking the track.")
    spacer(Modifier.height(8f.dp))
    var speed by context.rememberStateValue("ui-showcase-slider", "speed") { 1f }
    row(horizontalArrangement = Arrangement.spacedBy(10f.dp), modifier = Modifier.height(32f.dp.toDimension())) {
        text("0.5x", modifier = Modifier.width(36f.dp))
        speed = shadcnSlider(
            id = "showcase-slider-speed",
            min = 0.5f,
            max = 2f,
            value = speed,
            modifier = Modifier.width(220f.dp)
        )
        text("2x", modifier = Modifier.width(28f.dp))
    }
    spacer(Modifier.height(8f.dp))
}

internal fun ColumnScope.drawUiShowcaseSelectionPreview() {
    var wireframe by context.rememberStateValue("ui-showcase-selection", "wireframe") { true }
    var stats by context.rememberStateValue("ui-showcase-selection", "stats") { false }

    shadcnSupportingText("These are the current Awake-owned selection controls. They already share the same shadcn token layer even though radio and tab families are still pending.")
    spacer(Modifier.height(8f.dp))
    wireframe = shadcnToggle(
        id = "showcase-selection-wireframe",
        checked = wireframe,
        label = "Wireframe overlay",
        modifier = Modifier.width(220f.dp).height(24f.dp)
    )
    stats = shadcnCheckbox(
        id = "showcase-selection-stats",
        checked = stats,
        label = "Scene statistics",
        modifier = Modifier.width(220f.dp).height(24f.dp)
    )
    spacer(Modifier.height(8f.dp))
    shadcnSupportingText(
        "Selection state now lives in the same preview lane as fields and overlays, so color inversions and label alignment regressions show up faster."
    )
}

internal fun ColumnScope.drawUiShowcaseAvatarPreview() {
    shadcnSupportingText("The initials-string overload is sugar over the slot-based primary overload -- both share the same circular structure.")
    spacer(Modifier.height(8f.dp))
    row(horizontalArrangement = Arrangement.spacedBy(12f.dp), modifier = Modifier.height(40f.dp.toDimension())) {
        shadcnAvatar("RV", modifier = Modifier.width(40f.dp).height(40f.dp))
        shadcnAvatar("AK", modifier = Modifier.width(40f.dp).height(40f.dp))
        shadcnAvatar(modifier = Modifier.width(40f.dp).height(40f.dp)) { _ ->
            icon(UiIcons.chevronDown, modifier = Modifier.align(UiAlignment.Center))
        }
    }
    spacer(Modifier.height(8f.dp))
    shadcnSupportingText("Awake has no image-loading pipeline yet, so this is the fallback-only look (initials or caller-supplied content) -- see ShadcnAvatars.kt.")
}

internal fun ColumnScope.drawUiShowcaseBreadcrumbPreview() {
    shadcnSupportingText("A trail of muted links with the last item rendered as plain current-page text.")
    spacer(Modifier.height(8f.dp))
    shadcnBreadcrumb(listOf("Scenes", "Lighting", "Exposure"))
    spacer(Modifier.height(8f.dp))
    shadcnSupportingText("No click/navigation wiring -- that routing stays caller-owned, same as every other Awake nav element.")
}

internal fun ColumnScope.drawUiShowcaseAlertPreview() {
    shadcnSupportingText("A static inline banner, not a modal -- see the Dropdown Menu And Dialog page for overlay surfaces.")
    spacer(Modifier.height(8f.dp))
    shadcnAlert(
        id = "showcase-alert-default",
        title = "Scene saved",
        description = "Your changes are stored in the current project.",
        modifier = Modifier.fillMaxWidth()
    )
    spacer(Modifier.height(8f.dp))
    shadcnAlert(
        id = "showcase-alert-destructive",
        title = "Unable to load scene",
        description = "The scene file is missing or corrupted. Choose another scene to continue.",
        variant = ShadcnAlertVariant.Destructive,
        modifier = Modifier.fillMaxWidth()
    )
}
