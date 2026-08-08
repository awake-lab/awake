// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.ui.designsystem.components.controls.shadcnSelect
import io.github.ronjunevaldoz.awake.ui.designsystem.components.controls.shadcnSlider
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldRangeSlider
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldTextField
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldTextarea
import io.github.ronjunevaldoz.awake.ui.designsystem.components.navigation.shadcnTabs
import io.github.ronjunevaldoz.awake.ui.designsystem.components.selection.shadcnCheckbox
import io.github.ronjunevaldoz.awake.ui.designsystem.components.selection.shadcnRadioGroup
import io.github.ronjunevaldoz.awake.ui.designsystem.components.selection.shadcnSwitch
import io.github.ronjunevaldoz.awake.ui.designsystem.components.selection.shadcnToggle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.selection.shadcnToggleGroup
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnAccordion
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnAlert
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnAvatar
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBodyText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBreadcrumb
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCard
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCollapsible
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnKbd
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSectionTitle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSeparator
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebar
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarGroup
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenu
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenuItem
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.status.shadcnProgress
import io.github.ronjunevaldoz.awake.ui.designsystem.components.status.shadcnSkeleton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.status.shadcnSpinner
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnAlertVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.headless.components.icon
import io.github.ronjunevaldoz.awake.ui.headless.input.text.text
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
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.unstyled.UiIcons

internal fun ColumnScope.drawUiShowcaseButtonPreview() {
    shadcnSupportingText("Figma Component Layout: Hero interactive card, 2x2 variant matrix grid, and dynamic token inspector.")
    spacer(Modifier.height(8f.dp))

    // 1. Hero Preview Card
    shadcnCard(
        id = "button-hero-card",
        modifier = Modifier.fillMaxWidth().height(160f.dp),
        header = {
            row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                text("Interactive Hero Button")
                shadcnBadge("FIGMA EXACT", variant = ShadcnBadgeVariant.Primary)
            }
        },
    ) {
        row(
            horizontalArrangement = Arrangement.spacedBy(16f.dp),
            modifier = Modifier.height(50f.dp.toDimension())
        ) {
            shadcnButton(
                "hero-button-primary",
                label = "Primary Action",
                variant = ShadcnButtonVariant.Primary,
                modifier = Modifier.width(150f.dp)
            )
            shadcnButton(
                "hero-button-secondary",
                label = "Secondary Action",
                variant = ShadcnButtonVariant.Secondary,
                modifier = Modifier.width(150f.dp)
            )
            shadcnButton(
                "hero-button-outline",
                label = "Outline Action",
                variant = ShadcnButtonVariant.Outline,
                modifier = Modifier.width(150f.dp)
            )
        }
    }

    spacer(Modifier.height(12f.dp))

    // 2. Variant Matrix Grid
    shadcnSectionTitle("Variant Matrix")
    spacer(Modifier.height(4f.dp))
    row(
        horizontalArrangement = Arrangement.spacedBy(12f.dp),
        modifier = Modifier.height(40f.dp.toDimension())
    ) {
        shadcnButton(
            "showcase-button-primary",
            label = "Primary",
            variant = ShadcnButtonVariant.Primary,
            modifier = Modifier.width(120f.dp)
        )
        shadcnButton(
            "showcase-button-secondary",
            label = "Secondary",
            variant = ShadcnButtonVariant.Secondary,
            modifier = Modifier.width(120f.dp)
        )
        shadcnButton(
            "showcase-button-outline",
            label = "Outline",
            variant = ShadcnButtonVariant.Outline,
            modifier = Modifier.width(112f.dp)
        )
        shadcnButton(
            "showcase-button-ghost",
            label = "Ghost",
            variant = ShadcnButtonVariant.Ghost,
            modifier = Modifier.width(100f.dp)
        )
        shadcnButton(
            "showcase-button-danger",
            label = "Destructive",
            variant = ShadcnButtonVariant.Danger,
            modifier = Modifier.width(118f.dp)
        )
    }

    spacer(Modifier.height(12f.dp))

    // 3. Token & Metric Inspector
    shadcnCard(
        id = "button-token-inspector",
        modifier = Modifier.fillMaxWidth(),
        header = { text("Token & Metric Inspector") },
    ) {
        column(verticalArrangement = Arrangement.spacedBy(4f.dp)) {
            shadcnSupportingText("Tokens: primary | primary-foreground | border | ring")
            shadcnSupportingText("Height: 40dp | Padding: 16dp | Radius: radius-lg (8dp)")
        }
    }
}

internal fun ColumnScope.drawUiShowcaseBadgePreview() {
    shadcnSupportingText("Compact pill status badge indicator.")
    spacer(Modifier.height(8f.dp))
    shadcnCard(
        id = "badge-hero-card",
        modifier = Modifier.fillMaxWidth().height(120f.dp),
        header = { text("Badge Variants") },
    ) {
        row(
            horizontalArrangement = Arrangement.spacedBy(12f.dp),
            modifier = Modifier.height(30f.dp.toDimension())
        ) {
            shadcnBadge("Primary", variant = ShadcnBadgeVariant.Primary)
            shadcnBadge("Secondary", variant = ShadcnBadgeVariant.Secondary)
            shadcnBadge("Outline", variant = ShadcnBadgeVariant.Outline)
            shadcnBadge("Destructive", variant = ShadcnBadgeVariant.Danger)
        }
    }
}

internal fun ColumnScope.drawUiShowcaseTextFieldPreview() {
    var name by context.rememberStateValue("ui-showcase-text-field", "name") { "" }
    var email by context.rememberStateValue("ui-showcase-text-field", "email") { "" }
    var bio by context.rememberStateValue("ui-showcase-text-field", "bio") { "" }

    shadcnSupportingText("Single-line and multi-line keyboard-driven text input controls with focus ring bounds.")
    spacer(Modifier.height(8f.dp))
    shadcnCard(
        id = "text-field-hero-card",
        modifier = Modifier.fillMaxWidth(),
        header = { text("Text Input & Area Interactive Preview") },
    ) {
        column(verticalArrangement = Arrangement.spacedBy(10f.dp)) {
            shadcnFieldTextField(
                id = "showcase-name",
                label = "Full Name",
                value = name,
                placeholder = "Jane Doe",
            ).also { name = it }

            shadcnFieldTextField(
                id = "showcase-email",
                label = "Email Address",
                value = email,
                placeholder = "jane@example.com",
            ).also { email = it }

            shadcnFieldTextarea(
                id = "showcase-bio",
                label = "Biography",
                value = bio,
                placeholder = "Tell us about your background...",
                minLines = 4,
            ).also { bio = it }
        }
    }
}

internal fun ColumnScope.drawUiShowcaseTextareaPreview() {
    var bio by context.rememberStateValue("ui-showcase-textarea", "bio") { "" }

    shadcnSupportingText("Multi-line expandable text input field for longform content.")
    spacer(Modifier.height(8f.dp))
    shadcnCard(
        id = "textarea-hero-card",
        modifier = Modifier.fillMaxWidth(),
        header = { text("Text Area Preview") },
    ) {
        shadcnFieldTextarea(
            id = "showcase-bio",
            label = "Biography",
            value = bio,
            placeholder = "Tell us about your background...",
            minLines = 4,
        ).also { bio = it }
    }
}

internal fun ColumnScope.drawUiShowcaseCheckboxPreview() {
    var agree by context.rememberStateValue("ui-showcase-checkbox", "agree") { true }
    var updates by context.rememberStateValue("ui-showcase-checkbox", "updates") { false }

    shadcnSupportingText("Independent toggle box for binary boolean choices.")
    spacer(Modifier.height(8f.dp))
    shadcnCard(
        id = "checkbox-hero-card",
        modifier = Modifier.fillMaxWidth(),
        header = { text("Checkbox Preview") },
    ) {
        column(verticalArrangement = Arrangement.spacedBy(12f.dp)) {
            agree = shadcnCheckbox(
                id = "showcase-checkbox-agree",
                checked = agree,
                label = "Accept terms and conditions",
                modifier = Modifier.width(260f.dp).height(24f.dp),
            )
            updates = shadcnCheckbox(
                id = "showcase-checkbox-updates",
                checked = updates,
                label = "Receive email updates",
                modifier = Modifier.width(260f.dp).height(24f.dp),
            )
        }
    }
}

internal fun ColumnScope.drawUiShowcaseRadioGroupPreview() {
    var quality by context.rememberStateValue("ui-showcase-radiogroup", "quality") { 1 }

    shadcnSupportingText("Single-selection radio button group.")
    spacer(Modifier.height(8f.dp))
    shadcnCard(
        id = "radio-hero-card",
        modifier = Modifier.fillMaxWidth(),
        header = { text("Radio Group Preview") },
    ) {
        quality = shadcnRadioGroup(
            id = "showcase-radio-quality",
            options = listOf(
                "Low Quality (Fast)",
                "Medium Quality (Balanced)",
                "High Quality (Fidelity)"
            ),
            selectedIndex = quality,
            modifier = Modifier.width(320f.dp),
        )
    }
}

internal fun ColumnScope.drawUiShowcaseSwitchPreview() {
    var notifications by context.rememberStateValue("ui-showcase-switch", "notif") { true }

    shadcnSupportingText("Sliding toggle switch for binary setting states.")
    spacer(Modifier.height(8f.dp))
    shadcnCard(
        id = "switch-hero-card",
        modifier = Modifier.fillMaxWidth(),
        header = { text("Switch Preview") },
    ) {
        notifications = shadcnSwitch(
            id = "showcase-switch-notif",
            checked = notifications,
            label = "Enable push notifications",
            modifier = Modifier.width(260f.dp).height(24f.dp),
        )
    }
}

internal fun ColumnScope.drawUiShowcaseCollapsiblePreview() {
    var expanded by context.rememberStateValue("ui-showcase-collapsible", "expanded") { false }

    shadcnSupportingText("An interactive panel that expands to show more content, with a smooth height transition.")
    spacer(Modifier.height(8f.dp))

    shadcnCollapsible(
        id = "showcase-collapsible",
        title = "...",
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        column(
            verticalArrangement = Arrangement.spacedBy(8f.dp),
            modifier = Modifier.width(Dimension.FillMax).height(Dimension.WrapContent),
        ) {
            shadcnSeparator(modifier = Modifier.padding(0f.dp, 4f.dp, 0f.dp, 4f.dp))
            row(modifier = Modifier.fillMaxWidth().height(32f.dp)) {
                text(
                    "@radix-ui/primitives",
                    style = Style { contentPadding(12f.dp, 0f.dp, 0f.dp, 0f.dp) })
            }
            row(modifier = Modifier.fillMaxWidth().height(32f.dp)) {
                text(
                    "@radix-ui/colors",
                    style = Style { contentPadding(12f.dp, 0f.dp, 0f.dp, 0f.dp) })
            }
            row(modifier = Modifier.fillMaxWidth().height(32f.dp)) {
                text(
                    "@stitches/react",
                    style = Style { contentPadding(12f.dp, 0f.dp, 0f.dp, 0f.dp) })
            }
        }
    }

    spacer(Modifier.height(12f.dp))
    shadcnSectionTitle("Accordion Group")
    var accordionSelectedId: String? by context.rememberStateValue(
        "showcase-accordion",
        "selected"
    ) { "acc-1" }
    shadcnAccordion(
        items = listOf("acc-1", "acc-2", "acc-3"),
        selectedId = accordionSelectedId,
        onSelectId = { accordionSelectedId = it },
        idProvider = { it },
        titleProvider = {
            when (it) {
                "acc-1" -> "Is it accessible?"
                "acc-2" -> "Is it styled?"
                else -> "Is it animated?"
            }
        },
    ) { item ->
        shadcnBodyText("Yes. It adheres to the WAI-ARIA design pattern for accordion components.")
    }
}

internal fun ColumnScope.drawUiShowcaseSliderPreview() {
    var exposure by context.rememberStateValue("ui-showcase-slider", "exposure") { 52f }
    var bloom by context.rememberStateValue("ui-showcase-slider", "bloom") { 18f }

    shadcnSupportingText("Use sliders when you need a continuous value with immediate visual feedback and a stable control footprint.")
    spacer(Modifier.height(8f.dp))
    row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = UiAlignment.Vertical.Center,
        modifier = Modifier.width(360f.dp),
    ) {
        shadcnText("Exposure")
        shadcnText("${exposure.toInt()}%", muted = true)
    }
    spacer(Modifier.height(4f.dp))
    exposure = shadcnSlider(
        id = "showcase-slider-exposure",
        min = 0f,
        max = 100f,
        value = exposure,
        modifier = Modifier.width(360f.dp),
    )
    spacer(Modifier.height(8f.dp))
    row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = UiAlignment.Vertical.Center,
        modifier = Modifier.width(360f.dp),
    ) {
        shadcnText("Bloom")
        shadcnText("${bloom.toInt()} px", muted = true)
    }
    spacer(Modifier.height(4f.dp))
    bloom = shadcnSlider(
        id = "showcase-slider-bloom",
        min = 0f,
        max = 32f,
        value = bloom,
        modifier = Modifier.width(360f.dp),
    )
    spacer(Modifier.height(12f.dp))

    var volume by context.rememberStateValue("ui-showcase-slider", "volume") { 65f }
    shadcnSupportingText("A label-beside-track layout for a compact row (e.g. a settings list row).")
    spacer(Modifier.height(8f.dp))
    row(
        horizontalArrangement = Arrangement.spacedBy(10f.dp),
        verticalAlignment = UiAlignment.Vertical.Center,
        modifier = Modifier.height(32f.dp.toDimension()),
    ) {
        shadcnText("Volume", modifier = Modifier.width(64f.dp))
        volume = shadcnSlider(
            id = "showcase-slider-volume",
            min = 0f,
            max = 100f,
            value = volume,
            modifier = Modifier.width(220f.dp),
        )
        shadcnText("${volume.toInt()}%", muted = true, modifier = Modifier.width(40f.dp))
    }
    spacer(Modifier.height(8f.dp))

    shadcnSupportingText("Min/max range labels flanking the track.")
    spacer(Modifier.height(8f.dp))
    var speed by context.rememberStateValue("ui-showcase-slider", "speed") { 1f }
    row(
        horizontalArrangement = Arrangement.spacedBy(10f.dp),
        verticalAlignment = UiAlignment.Vertical.Center,
        modifier = Modifier.height(32f.dp.toDimension()),
    ) {
        shadcnText("0.5x", modifier = Modifier.width(36f.dp))
        speed = shadcnSlider(
            id = "showcase-slider-speed",
            min = 0.5f,
            max = 2f,
            value = speed,
            modifier = Modifier.width(220f.dp),
        )
        shadcnText("2x", modifier = Modifier.width(28f.dp))
    }
    spacer(Modifier.height(8f.dp))
}

internal fun ColumnScope.drawUiShowcaseRangeSliderPreview() {
    var temperature by context.rememberStateValue(
        "ui-showcase-range-slider",
        "temperature"
    ) { 0.3f to 0.7f }

    shadcnSupportingText("Two independent thumbs share one track, with the fill spanning only between them -- shadcn's range-mode Slider.")
    spacer(Modifier.height(8f.dp))
    temperature = shadcnFieldRangeSlider(
        id = "showcase-range-slider-temperature",
        label = "Temperature",
        min = 0f,
        max = 1f,
        valueStart = temperature.first,
        valueEnd = temperature.second,
        modifier = Modifier.width(360f.dp),
    )
    spacer(Modifier.height(8f.dp))
}

internal fun ColumnScope.drawUiShowcaseSelectionPreview() {
    var wireframe by context.rememberStateValue("ui-showcase-selection", "wireframe") { true }
    var stats by context.rememberStateValue("ui-showcase-selection", "stats") { false }
    var darkMode by context.rememberStateValue("ui-showcase-selection", "darkMode") { false }
    var quality by context.rememberStateValue("ui-showcase-selection", "quality") { 1 }
    var alignment by context.rememberStateValue("ui-showcase-selection", "alignment") { 1 }

    shadcnSupportingText("The current Awake-owned selection controls: two independent booleans (Toggle/Switch), an exclusive checkbox group (Checkbox), and single-select (RadioGroup/ToggleGroup).")
    spacer(Modifier.height(8f.dp))
    wireframe = shadcnToggle(
        id = "showcase-selection-wireframe",
        checked = wireframe,
        label = "Wireframe overlay",
        modifier = Modifier.width(220f.dp).height(24f.dp),
    )
    darkMode = shadcnSwitch(
        id = "showcase-selection-dark-mode",
        checked = darkMode,
        label = "Dark mode",
        modifier = Modifier.width(220f.dp).height(24f.dp),
    )
    stats = shadcnCheckbox(
        id = "showcase-selection-stats",
        checked = stats,
        label = "Scene statistics",
        modifier = Modifier.width(220f.dp).height(24f.dp),
    )
    spacer(Modifier.height(10f.dp))
    shadcnSupportingText("RadioGroup: single-select, clicking the already-selected item is a no-op.")
    spacer(Modifier.height(4f.dp))
    quality = shadcnRadioGroup(
        id = "showcase-selection-quality",
        options = listOf("Low", "Medium", "High"),
        selectedIndex = quality,
        modifier = Modifier.width(220f.dp),
    )
    spacer(Modifier.height(10f.dp))
    shadcnSupportingText("ToggleGroup: row of mutually exclusive toggle buttons, each option gets an equal share of the group width.")
    spacer(Modifier.height(4f.dp))
    shadcnToggleGroup(
        id = "showcase-selection-alignment",
        options = listOf("Left", "Center", "Right", "Justify"),
        selectedIndex = alignment,
        modifier = Modifier.width(280f.dp).height(32f.dp),
        onIndexChange = { alignment = it },
    )
    spacer(Modifier.height(8f.dp))
    shadcnSupportingText(
        "Selection state now lives in the same preview lane as fields and overlays, so color inversions and label alignment regressions show up faster.",
    )
}

internal fun ColumnScope.drawUiShowcaseTabsPreview() {
    var section by context.rememberStateValue("ui-showcase-tabs", "section") { 0 }
    val sections = listOf("Account", "Password", "Team")

    shadcnSupportingText("Composed from shadcnButton, same reuse-existing-variant approach as RadioGroup -- the active tab uses Primary with an overridden background/foreground, inactive tabs stay Ghost.")
    spacer(Modifier.height(8f.dp))
    section = shadcnTabs(
        id = "showcase-tabs-section",
        tabs = sections,
        selectedIndex = section,
    )
    spacer(Modifier.height(8f.dp))
    shadcnSupportingText("Selected: ${sections[section]}")
}

internal fun ColumnScope.drawUiShowcaseFeedbackPreview() {
    shadcnSupportingText("Progress -- a static fraction bar.")
    spacer(Modifier.height(4f.dp))
    shadcnProgress(
        id = "showcase-feedback-progress-low",
        value = 0.25f,
        modifier = Modifier.width(320f.dp).height(8f.dp),
    )
    spacer(Modifier.height(6f.dp))
    shadcnProgress(
        id = "showcase-feedback-progress-high",
        value = 0.8f,
        modifier = Modifier.width(320f.dp).height(8f.dp),
    )
    spacer(Modifier.height(12f.dp))
    shadcnSupportingText("Skeleton -- a real per-widget opacity pulse standing in for unloaded content.")
    spacer(Modifier.height(4f.dp))
    row(
        horizontalArrangement = Arrangement.spacedBy(10f.dp),
        modifier = Modifier.height(20f.dp.toDimension())
    ) {
        shadcnSkeleton(
            id = "showcase-feedback-skeleton-a",
            modifier = Modifier.width(160f.dp).height(20f.dp)
        )
        shadcnSkeleton(
            id = "showcase-feedback-skeleton-b",
            modifier = Modifier.width(96f.dp).height(20f.dp)
        )
    }
    spacer(Modifier.height(12f.dp))
    shadcnSupportingText("Spinner -- an orbiting-dots loader.")
    spacer(Modifier.height(4f.dp))
    row(
        horizontalArrangement = Arrangement.spacedBy(10f.dp),
        modifier = Modifier.height(24f.dp.toDimension())
    ) {
        shadcnSpinner(
            id = "showcase-feedback-spinner",
            modifier = Modifier.width(24f.dp).height(24f.dp)
        )
        text("Loading scene...", modifier = Modifier.align(UiAlignment.CenterStart))
    }
}

internal fun ColumnScope.drawUiShowcaseSelectPreview() {
    var themeOption by context.rememberStateValue("ui-showcase-select", "theme") { 0 }
    var accentOption by context.rememberStateValue("ui-showcase-select", "accent") { 1 }

    shadcnSupportingText("A closed-state trigger + popover dropdown -- see the Dropdown Menu And Dialog page for the open-state proof of the same underlying popover mechanics.")
    spacer(Modifier.height(8f.dp))
    row(
        horizontalArrangement = Arrangement.spacedBy(16f.dp),
        modifier = Modifier.height(40f.dp.toDimension())
    ) {
        themeOption = shadcnSelect(
            id = "showcase-select-theme",
            options = listOf("Light", "Dark", "Auto"),
            selectedIndex = themeOption,
            modifier = Modifier.width(200f.dp),
        ) ?: themeOption
        accentOption = shadcnSelect(
            id = "showcase-select-accent",
            options = listOf("Base", "Blue", "Emerald", "Rose"),
            selectedIndex = accentOption,
            modifier = Modifier.width(200f.dp),
        ) ?: accentOption
    }
}

internal fun ColumnScope.drawUiShowcaseKbdSeparatorPreview() {
    row(
        horizontalArrangement = Arrangement.spacedBy(8f.dp),
        modifier = Modifier.height(28f.dp.toDimension())
    ) {
        shadcnKbd("Cmd", modifier = Modifier.width(48f.dp).height(28f.dp))
        shadcnKbd("Shift", modifier = Modifier.width(56f.dp).height(28f.dp))
        shadcnKbd("P", modifier = Modifier.width(32f.dp).height(28f.dp))
    }
    spacer(Modifier.height(16f.dp))
    shadcnBodyText("Above the fold.")
    spacer(Modifier.height(8f.dp))
    shadcnSeparator(modifier = Modifier.width(320f.dp))
    spacer(Modifier.height(8f.dp))
    shadcnBodyText("Below the fold.")
}

internal fun ColumnScope.drawUiShowcaseAvatarPreview() {
    shadcnSupportingText("The initials-string overload is sugar over the slot-based primary overload -- both share the same circular structure.")
    spacer(Modifier.height(8f.dp))
    row(
        horizontalArrangement = Arrangement.spacedBy(12f.dp),
        modifier = Modifier.height(40f.dp.toDimension())
    ) {
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

internal fun ColumnScope.drawUiShowcaseCardPreview() {
    shadcnSupportingText("Header and footer are each independently optional -- three real slot combinations, not hover/pressed states.")
    spacer(Modifier.height(8f.dp))
    shadcnCard(
        id = "showcase-card-full",
        modifier = Modifier.fillMaxWidth(),
        header = { shadcnSectionTitle("Full card") },
        footer = {
            shadcnButton(
                "showcase-card-full-action",
                label = "Save",
                modifier = Modifier.width(96f.dp).height(32f.dp)
            )
        },
    ) { shadcnBodyText("Header, body, and footer all present.") }
    spacer(Modifier.height(8f.dp))
    shadcnCard(
        id = "showcase-card-header-only",
        modifier = Modifier.fillMaxWidth(),
        header = { shadcnSectionTitle("Header, no footer") },
    ) { shadcnBodyText("Only header and body -- no divider or footer below the body.") }
    spacer(Modifier.height(8f.dp))
    shadcnCard(
        id = "showcase-card-body-only",
        modifier = Modifier.fillMaxWidth(),
    ) { shadcnBodyText("Body only -- neither header nor footer, so no dividers at all.") }
}

internal fun ColumnScope.drawUiShowcaseSidebarPreview() {
    shadcnSupportingText("Header and footer are each independently optional, same as Card -- a fixed nav-rail width keeps it from stretching full-width. Groups/menu items use the dedicated sidebarAccent tokens for the active highlight, not the generic primary/accent tokens.")
    spacer(Modifier.height(8f.dp))
    var activeItem by rememberStateValue(
        "showcase-sidebar-active",
        "showcase-sidebar"
    ) { "lighting" }
    row(
        horizontalArrangement = Arrangement.spacedBy(16f.dp),
        modifier = Modifier.height(Dimension.WrapContent)
    ) {
        shadcnSidebar(
            id = "showcase-sidebar-full",
            modifier = Modifier.width(220f.dp).height(Dimension.WrapContent),
            header = { shadcnBadge("STARTER", variant = ShadcnBadgeVariant.Primary) },
            footer = {
                shadcnButton(
                    "showcase-sidebar-full-signout",
                    label = "Sign out",
                    modifier = Modifier.fillMaxWidth().height(32f.dp),
                    variant = ShadcnButtonVariant.Outline
                )
            },
        ) { _ ->
            shadcnSidebarGroup(label = "Scene") {
                shadcnSidebarMenu {
                    shadcnSidebarMenuItem(
                        id = "showcase-sidebar-full-lighting",
                        label = "Lighting",
                        active = activeItem == "lighting",
                        badge = "New",
                        onClick = { activeItem = "lighting" },
                    )
                    shadcnSidebarMenuItem(
                        id = "showcase-sidebar-full-camera",
                        label = "Camera",
                        active = activeItem == "camera",
                        onClick = { activeItem = "camera" },
                    )
                    shadcnSidebarMenuItem(
                        id = "showcase-sidebar-full-exposure",
                        label = "Exposure",
                        active = activeItem == "exposure",
                        onClick = { activeItem = "exposure" },
                    )
                }
            }
        }
        shadcnSidebar(
            id = "showcase-sidebar-content-only",
            modifier = Modifier.width(220f.dp).height(Dimension.WrapContent),
        ) { _ ->
            shadcnButton(
                "showcase-sidebar-content-only-lighting",
                label = "Lighting",
                modifier = Modifier.fillMaxWidth().height(32f.dp),
                variant = ShadcnButtonVariant.Primary
            )
            shadcnButton(
                "showcase-sidebar-content-only-camera",
                label = "Camera",
                modifier = Modifier.fillMaxWidth().height(32f.dp),
                variant = ShadcnButtonVariant.Secondary
            )
        }
    }
    spacer(Modifier.height(8f.dp))
    shadcnSupportingText("No header/footer here -- neither divider renders, matching shadcnCard's body-only case.")
    spacer(Modifier.height(16f.dp))
    shadcnSupportingText("expanded animates width toward 0 on collapse (real shadcn's SidebarProvider expand/collapse), caller-hoisted like every other stateful recipe in this module -- toggle from any ordinary button, there's no dedicated trigger/provider type.")
    spacer(Modifier.height(8f.dp))
    var sidebarExpanded by rememberStateValue(
        "showcase-sidebar-expanded",
        "showcase-sidebar-collapsible"
    ) { true }
    row(
        horizontalArrangement = Arrangement.spacedBy(16f.dp),
        verticalAlignment = UiAlignment.Vertical.Top,
        modifier = Modifier.height(Dimension.WrapContent)
    ) {
        shadcnSidebar(
            id = "showcase-sidebar-collapsible",
            modifier = Modifier.width(220f.dp).height(Dimension.WrapContent),
            expanded = sidebarExpanded,
        ) { _ ->
            shadcnSidebarMenu {
                shadcnSidebarMenuItem(
                    id = "showcase-sidebar-collapsible-lighting",
                    label = "Lighting",
                    active = true
                )
                shadcnSidebarMenuItem(
                    id = "showcase-sidebar-collapsible-camera",
                    label = "Camera",
                    active = false
                )
            }
        }
        shadcnButton(
            id = "showcase-sidebar-collapsible-toggle",
            label = if (sidebarExpanded) "Collapse" else "Expand",
            variant = ShadcnButtonVariant.Outline,
            modifier = Modifier.width(120f.dp).height(32f.dp),
        ) { sidebarExpanded = !sidebarExpanded }
    }
}

internal fun ColumnScope.drawUiShowcaseAlertPreview() {
    shadcnSupportingText("A static inline banner, not a modal.")
    spacer(Modifier.height(8f.dp))
    shadcnAlert(
        id = "showcase-alert-default",
        title = "Scene saved",
        description = "Your changes are stored in the current project.",
        modifier = Modifier.fillMaxWidth(),
    )
    spacer(Modifier.height(8f.dp))
    shadcnAlert(
        id = "showcase-alert-destructive",
        title = "Unable to load scene",
        description = "The scene file is missing or corrupted. Choose another scene to continue.",
        variant = ShadcnAlertVariant.Destructive,
        modifier = Modifier.fillMaxWidth(),
    )
}

internal fun ColumnScope.drawUiShowcaseDialogPreview() {
    var open by context.rememberStateValue("ui-showcase-dialog", "open") { false }
    shadcnSupportingText("Centered modal dialog window with backdrop scrim.")
    spacer(Modifier.height(8f.dp))
    shadcnButton(
        "showcase-dialog-trigger",
        label = "Open Dialog",
        modifier = Modifier.width(140f.dp)
    ) {
        open = true
    }
}

internal fun ColumnScope.drawUiShowcaseProgressPreview() {
    row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = UiAlignment.Vertical.Center,
        modifier = Modifier.fillMaxWidth(),
    ) {
        shadcnText("Upload progress")
        shadcnText("56%", muted = true)
    }
    spacer(Modifier.height(8f.dp))
    shadcnProgress(
        id = "showcase-progress-bar",
        value = 0.56f,
        modifier = Modifier.fillMaxWidth().height(4f.dp),
    )
}

internal fun ColumnScope.drawUiShowcaseSkeletonPreview() {
    shadcnSupportingText("Placeholder wireframe pulse shape during data loading.")
    spacer(Modifier.height(8f.dp))
    row(
        horizontalArrangement = Arrangement.spacedBy(12f.dp),
        modifier = Modifier.height(24f.dp.toDimension())
    ) {
        shadcnSkeleton(id = "skel-a", modifier = Modifier.width(120f.dp).height(24f.dp))
        shadcnSkeleton(id = "skel-b", modifier = Modifier.width(180f.dp).height(24f.dp))
    }
}

internal fun ColumnScope.drawUiShowcaseSpinnerPreview() {
    shadcnSupportingText("Circular activity loading spinner indicator.")
    spacer(Modifier.height(8f.dp))
    row(
        horizontalArrangement = Arrangement.spacedBy(10f.dp),
        modifier = Modifier.height(24f.dp.toDimension())
    ) {
        shadcnSpinner(id = "spin", modifier = Modifier.width(24f.dp).height(24f.dp))
        text("Loading component assets...", modifier = Modifier.align(UiAlignment.CenterStart))
    }
}

internal fun ColumnScope.drawUiShowcaseKbdPreview() {
    shadcnSupportingText("Keyboard shortcut key pill display.")
    spacer(Modifier.height(8f.dp))
    row(
        horizontalArrangement = Arrangement.spacedBy(8f.dp),
        modifier = Modifier.height(28f.dp.toDimension())
    ) {
        shadcnKbd("Cmd", modifier = Modifier.width(48f.dp).height(28f.dp))
        shadcnKbd("Shift", modifier = Modifier.width(56f.dp).height(28f.dp))
        shadcnKbd("P", modifier = Modifier.width(32f.dp).height(28f.dp))
    }
}
