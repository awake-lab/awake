package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBodyText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCard
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldTextField
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldTextarea
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.spacer
import io.github.ronjunevaldoz.awake.ui.headless.rememberStateValue

private fun ColumnScope.component(label: String, detail: String = "Headless behavior with a shadcn recipe.") {
    shadcnBadge(id = "showcase-component-${label.lowercase().replace(' ', '-')}", label = label, variant = ShadcnBadgeVariant.Outline)
    spacer(Modifier.height(6f.dp))
    shadcnSupportingText(detail)
}

internal fun ColumnScope.drawUiShowcaseButtonPreview() { component("BUTTON"); shadcnButton(id = "showcase-button", label = "Continue", variant = ShadcnButtonVariant.Primary) }
internal fun ColumnScope.drawUiShowcaseBadgePreview() = component("BADGE")
internal fun ColumnScope.drawUiShowcaseTextFieldPreview() {
    var name by rememberStateValue("ui-showcase-text-field", "name") { "" }
    var email by rememberStateValue("ui-showcase-text-field", "email") { "" }
    var bio by rememberStateValue("ui-showcase-text-field", "bio") { "" }
    shadcnSupportingText("Single-line and multi-line keyboard-driven text input controls with focus ring bounds.")
    spacer(Modifier.height(8f.dp))
    shadcnCard(id = "text-field-hero-card", modifier = Modifier.height(260f.dp), header = { shadcnBodyText("Text Input & Area Interactive Preview") }) {
        name = shadcnFieldTextField(id = "showcase-name", label = "Full Name", value = name, placeholder = "Jane Doe")
        email = shadcnFieldTextField(id = "showcase-email", label = "Email Address", value = email, placeholder = "jane@example.com")
        bio = shadcnFieldTextarea(id = "showcase-bio", label = "Biography", value = bio, placeholder = "Tell us about your background...", minLines = 4)
    }
}
internal fun ColumnScope.drawUiShowcaseTextareaPreview() {
    var bio by rememberStateValue("ui-showcase-textarea", "bio") { "" }
    shadcnSupportingText("Multi-line expandable text input field for longform content.")
    spacer(Modifier.height(8f.dp))
    shadcnCard(id = "textarea-hero-card", modifier = Modifier.height(220f.dp), header = { shadcnBodyText("Text Area Preview") }) {
        bio = shadcnFieldTextarea(id = "showcase-bio", label = "Biography", value = bio, placeholder = "Tell us about your background...", minLines = 4)
    }
}
internal fun ColumnScope.drawUiShowcaseCheckboxPreview() = component("CHECKBOX")
internal fun ColumnScope.drawUiShowcaseRadioGroupPreview() = component("RADIO GROUP")
internal fun ColumnScope.drawUiShowcaseSwitchPreview() = component("SWITCH")
internal fun ColumnScope.drawUiShowcaseCollapsiblePreview() = component("COLLAPSIBLE")
internal fun ColumnScope.drawUiShowcaseSliderPreview() = component("SLIDER")
internal fun ColumnScope.drawUiShowcaseRangeSliderPreview() = component("RANGE SLIDER")
internal fun ColumnScope.drawUiShowcaseSelectionPreview() = component("SELECTION")
internal fun ColumnScope.drawUiShowcaseTabsPreview() = component("TABS")
internal fun ColumnScope.drawUiShowcaseFeedbackPreview() = component("FEEDBACK")
internal fun ColumnScope.drawUiShowcaseSelectPreview() = component("SELECT")
internal fun ColumnScope.drawUiShowcaseKbdSeparatorPreview() = component("SEPARATOR")
internal fun ColumnScope.drawUiShowcaseAvatarPreview() = component("AVATAR")
internal fun ColumnScope.drawUiShowcaseBreadcrumbPreview() = component("BREADCRUMB")
internal fun ColumnScope.drawUiShowcaseCardPreview() {
    shadcnCard(id = "showcase-card", modifier = Modifier.height(100f.dp)) { shadcnBodyText("Card content") }
}
internal fun ColumnScope.drawUiShowcaseSidebarPreview() = component("SIDEBAR")
internal fun ColumnScope.drawUiShowcaseAlertPreview() = component("ALERT")
internal fun ColumnScope.drawUiShowcaseDialogPreview() = component("DIALOG")
internal fun ColumnScope.drawUiShowcaseProgressPreview() = component("PROGRESS")
internal fun ColumnScope.drawUiShowcaseSkeletonPreview() = component("SKELETON")
internal fun ColumnScope.drawUiShowcaseSpinnerPreview() = component("SPINNER")
internal fun ColumnScope.drawUiShowcaseKbdPreview() = component("KBD")
