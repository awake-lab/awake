package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnFieldDropdown
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnFieldRangeSlider
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnFieldSlider
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnFieldSwitch
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnFieldTextField
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnFieldTextarea
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCheckbox
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnRadioGroup
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnAlert
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnAvatar
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBodyText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBreadcrumb
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCard
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCollapsible
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnDialog
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnKbd
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSeparator
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebar
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnTabs
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnProgress
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSkeleton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSpinner
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.rememberPopupState
import io.github.ronjunevaldoz.awake.ui.headless.rememberStateValue
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.spacer
import io.github.ronjunevaldoz.awake.ui.headless.uiScope
import io.github.ronjunevaldoz.awake.ui.headless.width

private fun ColumnScope.component(
    label: String,
    detail: String = "Headless behavior with a shadcn recipe."
) {
    shadcnBadge(
        id = "showcase-component-${label.lowercase().replace(' ', '-')}",
        label = label,
        variant = ShadcnBadgeVariant.Outline
    )
    spacer(Modifier.height(6f.dp))
    shadcnSupportingText(detail)
    // Keep the catalog header and its live sample as separate vertical groups. Without this
    // explicit spacer, the first control is glued to the supporting copy and looks clipped in
    // the compact preview viewport.
    spacer(Modifier.height(8f.dp))
}

internal fun ColumnScope.drawUiShowcaseButtonPreview() {
    component("BUTTON")
    var clicks by rememberStateValue("ui-showcase-button", "clicks") { 0 }
    shadcnButton(
        id = "showcase-button",
        label = "Continue",
        variant = ShadcnButtonVariant.Primary,
        onClick = { clicks += 1 },
    )
    spacer(Modifier.height(6f.dp))
    shadcnSupportingText("Interaction proof: $clicks clicks")
}

internal fun ColumnScope.drawUiShowcaseBadgePreview() {
    component("BADGE")
    row(horizontalArrangement = Arrangement.spacedBy(8f.dp)) {
        shadcnBadge("showcase-badge-primary", "Primary", ShadcnBadgeVariant.Primary)
        shadcnBadge("showcase-badge-secondary", "Secondary", ShadcnBadgeVariant.Secondary)
        shadcnBadge("showcase-badge-danger", "Danger", ShadcnBadgeVariant.Danger)
        shadcnBadge("showcase-badge-outline", "Outline", ShadcnBadgeVariant.Outline)
    }
}

internal fun ColumnScope.drawUiShowcaseTextFieldPreview() {
    var name by rememberStateValue("ui-showcase-text-field", "name") { "" }
    var email by rememberStateValue("ui-showcase-text-field", "email") { "" }
    var bio by rememberStateValue("ui-showcase-text-field", "bio") { "" }
    shadcnSupportingText("Single-line and multi-line keyboard-driven text input controls with focus ring bounds.")
    spacer(Modifier.height(8f.dp))
    shadcnCard(
        id = "text-field-hero-card",
        modifier = Modifier.height(260f.dp),
        header = { shadcnBodyText("Text Input & Area Interactive Preview") }) {
        name = shadcnFieldTextField(
            id = "showcase-name",
            label = "Full Name",
            value = name,
            placeholder = "Jane Doe"
        )
        email = shadcnFieldTextField(
            id = "showcase-email",
            label = "Email Address",
            value = email,
            placeholder = "jane@example.com"
        )
        bio = shadcnFieldTextarea(
            id = "showcase-bio",
            label = "Biography",
            value = bio,
            placeholder = "Tell us about your background...",
            minLines = 4
        )
    }
}

internal fun ColumnScope.drawUiShowcaseTextareaPreview() {
    var bio by rememberStateValue("ui-showcase-textarea", "bio") { "" }
    shadcnSupportingText("Multi-line expandable text input field for longform content.")
    spacer(Modifier.height(8f.dp))
    shadcnCard(
        id = "textarea-hero-card",
        modifier = Modifier.height(220f.dp),
        header = { shadcnBodyText("Text Area Preview") }) {
        bio = shadcnFieldTextarea(
            id = "showcase-bio",
            label = "Biography",
            value = bio,
            placeholder = "Tell us about your background...",
            minLines = 4
        )
    }
}

internal fun ColumnScope.drawUiShowcaseCheckboxPreview() {
    component("CHECKBOX")
    var checked by rememberStateValue("ui-showcase-checkbox", "checked") { true }
    checked = shadcnCheckbox(id = "showcase-checkbox", checked = checked, label = "Accept terms")
}

internal fun ColumnScope.drawUiShowcaseRadioGroupPreview() {
    component("RADIO GROUP")
    var selected by rememberStateValue("ui-showcase-radio-group", "selected") { 0 }
    selected = shadcnRadioGroup(
        id = "showcase-radio-group",
        options = listOf("System", "Light", "Dark"),
        selectedIndex = selected,
        onIndexChange = { selected = it },
    )
}

internal fun ColumnScope.drawUiShowcaseSwitchPreview() {
    component("SWITCH")
    var checked by rememberStateValue("ui-showcase-switch", "checked") { true }
    checked = shadcnFieldSwitch(id = "showcase-switch", label = "Airplane mode", checked = checked)
}

internal fun ColumnScope.drawUiShowcaseCollapsiblePreview() {
    component("COLLAPSIBLE")
    var expanded by rememberStateValue("ui-showcase-collapsible", "expanded") { true }
    expanded = shadcnCollapsible(
        id = "showcase-collapsible",
        title = "Show details",
        expanded = expanded,
        onExpandedChange = { expanded = it }) {
        shadcnSupportingText("Additional content is visible when expanded.")
    }
}

internal fun ColumnScope.drawUiShowcaseSliderPreview() {
    component("SLIDER")
    var value by rememberStateValue("ui-showcase-slider", "value") { 60f }
    value = shadcnFieldSlider(
        id = "showcase-slider",
        label = "Volume",
        min = 0f,
        max = 100f,
        value = value
    )
}

internal fun ColumnScope.drawUiShowcaseRangeSliderPreview() {
    component("RANGE SLIDER")
    var range by rememberStateValue("ui-showcase-range-slider", "range") { 20f to 80f }
    range = shadcnFieldRangeSlider(
        id = "showcase-range-slider",
        label = "Price",
        min = 0f,
        max = 100f,
        valueStart = range.first,
        valueEnd = range.second,
    )
}

internal fun ColumnScope.drawUiShowcaseSelectionPreview() {
    component("SELECTION")
    var notifications by rememberStateValue("ui-showcase-selection", "notifications") { false }
    var compact by rememberStateValue("ui-showcase-selection", "compact") { false }
    notifications = shadcnCheckbox(
        id = "showcase-selection-checkbox",
        checked = notifications,
        label = "Enable notifications"
    )
    spacer(Modifier.height(8f.dp))
    compact = shadcnFieldSwitch(
        id = "showcase-selection-switch",
        label = "Use compact mode",
        checked = compact
    )
}

internal fun ColumnScope.drawUiShowcaseTabsPreview() {
    component("TABS")
    var selected by rememberStateValue("ui-showcase-tabs", "selected") { 0 }
    selected = shadcnTabs(
        id = "showcase-tabs",
        tabs = listOf("Account", "Password"),
        selectedIndex = selected
    )
}

internal fun ColumnScope.drawUiShowcaseFeedbackPreview() {
    component("FEEDBACK")
    shadcnAlert(
        id = "showcase-feedback",
        title = "Saved",
        description = "Your changes were saved successfully."
    )
}

internal fun ColumnScope.drawUiShowcaseSelectPreview() {
    component("SELECT")
    var selected by rememberStateValue("ui-showcase-select", "selected") { 0 }
    selected = shadcnFieldDropdown(
        id = "showcase-select",
        label = "Framework",
        options = listOf("Compose", "SwiftUI", "Flutter"),
        selectedIndex = selected,
    ) ?: selected
}

internal fun ColumnScope.drawUiShowcaseKbdSeparatorPreview() {
    component("SEPARATOR")
    row(horizontalArrangement = Arrangement.spacedBy(8f.dp)) {
        shadcnKbd(id = "showcase-kbd-shortcut", label = "⌘K")
        shadcnSeparator(
            modifier = Modifier.width(1f.dp),
            orientation = io.github.ronjunevaldoz.awake.ui.headless.UiSeparatorOrientation.Vertical
        )
        shadcnSupportingText("Open command menu")
    }
}

internal fun ColumnScope.drawUiShowcaseAvatarPreview() {
    component("AVATAR")
    row(horizontalArrangement = Arrangement.spacedBy(8f.dp)) {
        shadcnAvatar(id = "showcase-avatar-rj", initials = "RJ")
        shadcnAvatar(id = "showcase-avatar-ak", initials = "AK")
        shadcnAvatar(id = "showcase-avatar-ms", initials = "MS")
    }
}

internal fun ColumnScope.drawUiShowcaseBreadcrumbPreview() {
    component("BREADCRUMB")
    shadcnBreadcrumb(
        id = "showcase-breadcrumb",
        items = listOf("Docs", "Components", "Button")
    )
}

internal fun ColumnScope.drawUiShowcaseCardPreview() {
    shadcnCard(
        id = "showcase-card",
        modifier = Modifier.height(100f.dp)
    ) { shadcnBodyText("Card content") }
}

internal fun ColumnScope.drawUiShowcaseSidebarPreview() {
    component("SIDEBAR")
    shadcnSidebar(
        id = "showcase-sidebar",
        modifier = Modifier.width(220f.dp),
        header = { shadcnBodyText("Workspace") }) {
        shadcnSupportingText("Overview")
        spacer(Modifier.height(4f.dp))
        shadcnSupportingText("Settings")
    }
}

internal fun ColumnScope.drawUiShowcaseAlertPreview() {
    component("ALERT")
    shadcnAlert(
        id = "showcase-alert",
        title = "Heads up",
        description = "This is a visible alert preview."
    )
}

internal fun ColumnScope.drawUiShowcaseDialogPreview() {
    component("DIALOG")
    val popup = rememberPopupState("ui-showcase-dialog", initial = false)
    shadcnButton(
        id = "showcase-dialog-trigger",
        label = "Open dialog",
        variant = ShadcnButtonVariant.Outline,
        onClick = popup::open,
    )
    val result = uiScope().shadcnDialog(id = "showcase-dialog", expanded = popup.expanded) {
        shadcnBodyText("This dialog is interactive.")
        spacer(Modifier.height(8f.dp))
        shadcnButton(id = "showcase-dialog-close", label = "Close", onClick = popup::close)
    }
    if (result.dismissed) popup.close()
}

internal fun ColumnScope.drawUiShowcaseProgressPreview() {
    component("PROGRESS")
    shadcnProgress(
        id = "showcase-progress",
        value = 0.65f,
        modifier = Modifier.width(260f.dp).height(8f.dp)
    )
}

internal fun ColumnScope.drawUiShowcaseSkeletonPreview() {
    component("SKELETON")
    shadcnSkeleton(id = "showcase-skeleton", modifier = Modifier.width(260f.dp).height(20f.dp))
}

internal fun ColumnScope.drawUiShowcaseSpinnerPreview() {
    component("SPINNER")
    shadcnSpinner(id = "showcase-spinner", modifier = Modifier.width(24f.dp).height(24f.dp))
}

internal fun ColumnScope.drawUiShowcaseKbdPreview() {
    component("KBD")
    shadcnKbd(id = "showcase-kbd", label = "⌘K")
}
