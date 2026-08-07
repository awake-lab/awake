// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseThemeMode
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.demos.inputs.drawShadcnInputOtpDemoPreview
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.demos.layout.drawShadcnAccordionDemoPreview
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.demos.overlay.drawShadcnContextMenuDemoPreview
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.demos.overlay.drawShadcnDrawerDemoPreview
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnAccent
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnBaseColor
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnStylePreset
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope

internal typealias ShowcasePreviewRenderer = ColumnScope.(UiShowcaseRuntimeState) -> Unit

internal val ShowcaseStyleOptions = ShadcnStylePreset.entries.map { it.label }
internal val ShowcaseBaseColorOptions = ShadcnBaseColor.entries.map { it.label }
internal val ShowcaseAccentOptions = ShadcnAccent.entries.map { it.label }
internal val ShowcaseThemeModeOptions = UiShowcaseThemeMode.entries.map { it.label }
internal val ShowcaseBadgeOptions = listOf("Primary", "Secondary", "Outline", "Danger")

internal enum class ShowcaseCategory(val title: String) {
    GettingStarted("Getting Started"),
    Inputs("Inputs"),
    Overlays("Overlays"),
    Layout("Layout"),
    Typography("Typography"),
    Animations("Animations"),
    Patterns("Patterns"),
}

internal data class ShowcasePage(
    val id: String,
    val title: String,
    val category: ShowcaseCategory,
    val description: String,
    val usageCode: String,
    val notes: List<String>,
    val renderPreview: ShowcasePreviewRenderer,
)

internal val ShowcasePages = listOf(
    // --- Getting Started ---
    ShowcasePage(
        id = "introduction",
        title = "Introduction",
        category = ShowcaseCategory.GettingStarted,
        description = "A dedicated catalog sample for owned Awake UI components.",
        usageCode = "gameUi { theme(shadcnTheme()) }",
        notes = listOf("Mirrors shadcn catalog chrome + sidebar + detail pane."),
        renderPreview = { drawUiShowcaseOverviewPreview() },
    ),
    ShowcasePage(
        id = "theming",
        title = "Theming",
        category = ShowcaseCategory.GettingStarted,
        description = "Live preset, base color, accent, and dark mode theme controls.",
        usageCode = "val theme = shadcnTheme(preset = ShadcnStylePreset.Vega, dark = true)",
        notes = listOf("Re-themes content pane live while maintaining shell chrome."),
        renderPreview = { state -> drawUiShowcaseControlsPreview(state) },
    ),

    // --- Inputs & Controls ---
    ShowcasePage(
        id = "button",
        title = "Button",
        category = ShowcaseCategory.Inputs,
        description = "Interactive trigger button supporting Primary, Secondary, Outline, Ghost, and Destructive variants.",
        usageCode = "shadcnButton(id = \"btn\", label = \"Click Me\", variant = ShadcnButtonVariant.Primary)",
        notes = listOf("Supports text labels, icons, and custom slot API blocks."),
        renderPreview = { drawUiShowcaseButtonPreview() },
    ),
    ShowcasePage(
        id = "badge",
        title = "Badge",
        category = ShowcaseCategory.Inputs,
        description = "Compact pill badge indicator for status labels and counts.",
        usageCode = "shadcnBadge(\"NEW\", variant = ShadcnBadgeVariant.Primary)",
        notes = listOf("Pill shape with primary, secondary, outline, and destructive tokens."),
        renderPreview = { drawUiShowcaseBadgePreview() },
    ),
    ShowcasePage(
        id = "text-input",
        title = "Text Field",
        category = ShowcaseCategory.Inputs,
        description = "Single-line keyboard-driven text input control with label and focus ring.",
        usageCode = "shadcnFieldTextField(id = \"email\", label = \"Email\", value = email)",
        notes = listOf("Supports live keyboard typing, arrow navigation, and clear actions."),
        renderPreview = { drawUiShowcaseTextFieldPreview() },
    ),
    ShowcasePage(
        id = "text-area",
        title = "Text Area",
        category = ShowcaseCategory.Inputs,
        description = "Multi-line expandable text input field for longform text entry.",
        usageCode = "shadcnFieldTextarea(id = \"bio\", label = \"Bio\", value = bio, minLines = 4)",
        notes = listOf("Scrollable multiline input with focus ring boundary."),
        renderPreview = { drawUiShowcaseTextareaPreview() },
    ),
    ShowcasePage(
        id = "input-otp",
        title = "Input OTP",
        category = ShowcaseCategory.Inputs,
        description = "Segmented One-Time Password / PIN code digit entry row.",
        usageCode = "shadcnInputOTP(id = \"otp\", value = otpCode, length = 6)",
        notes = listOf("Segmented digits with length mask and space separation."),
        renderPreview = { drawShadcnInputOtpDemoPreview() },
    ),
    ShowcasePage(
        id = "checkbox",
        title = "Checkbox",
        category = ShowcaseCategory.Inputs,
        description = "Independent toggle box for binary boolean options.",
        usageCode = "shadcnCheckbox(id = \"agree\", checked = true, label = \"I agree\")",
        notes = listOf("Supports checked, unchecked, and focus ring state tokens."),
        renderPreview = { drawUiShowcaseCheckboxPreview() },
    ),
    ShowcasePage(
        id = "radio-group",
        title = "Radio Group",
        category = ShowcaseCategory.Inputs,
        description = "Single-selection group of radio options.",
        usageCode = "shadcnRadioGroup(id = \"rad\", options = listOf(\"A\", \"B\"), selectedIndex = 0)",
        notes = listOf("Exclusive single-choice selection with keyboard navigation."),
        renderPreview = { drawUiShowcaseRadioGroupPreview() },
    ),
    ShowcasePage(
        id = "switch",
        title = "Switch",
        category = ShowcaseCategory.Inputs,
        description = "Sliding toggle switch for binary setting states.",
        usageCode = "shadcnSwitch(id = \"sw\", checked = enabled, label = \"Enable notifications\")",
        notes = listOf("Smooth thumb sliding animation across track."),
        renderPreview = { drawUiShowcaseSwitchPreview() },
    ),
    ShowcasePage(
        id = "slider",
        title = "Slider",
        category = ShowcaseCategory.Inputs,
        description = "Continuous range thumb slider for numeric inputs.",
        usageCode = "shadcnSlider(id = \"vol\", min = 0f, max = 100f, value = 50f)",
        notes = listOf("Track fill with thumb drag interaction."),
        renderPreview = { drawUiShowcaseSliderPreview() },
    ),
    ShowcasePage(
        id = "select",
        title = "Select",
        category = ShowcaseCategory.Inputs,
        description = "Dropdown picker menu trigger with item selection popover.",
        usageCode = "shadcnSelect(id = \"sel\", options = options, selectedIndex = 0)",
        notes = listOf("Floating item popover with hover highlight."),
        renderPreview = { drawUiShowcaseSelectPreview() },
    ),

    // --- Layout & Surfaces ---
    ShowcasePage(
        id = "card",
        title = "Card",
        category = ShowcaseCategory.Layout,
        description = "Surface container with header, content, and footer slots.",
        usageCode = "shadcnCard(id = \"card\", header = { text(\"Title\") }) { ... }",
        notes = listOf("Encapsulated content surface with card and border tokens."),
        renderPreview = { drawUiShowcaseCardPreview() },
    ),
    ShowcasePage(
        id = "tabs",
        title = "Tabs",
        category = ShowcaseCategory.Layout,
        description = "Segmented tab track for switching between content panes.",
        usageCode = "shadcnTabs(id = \"tabs\", tabs = tabs, activeTab = active) { ... }",
        notes = listOf("Muted track with active tab surface elevation."),
        renderPreview = { drawUiShowcaseTabsPreview() },
    ),
    ShowcasePage(
        id = "accordion",
        title = "Accordion",
        category = ShowcaseCategory.Layout,
        description = "Single-select interactive collapsible section group.",
        usageCode = "shadcnAccordion(items = items, selectedId = sel) { ... }",
        notes = listOf("Collapsible group supporting WAI-ARIA single selection."),
        renderPreview = { drawShadcnAccordionDemoPreview() },
    ),
    ShowcasePage(
        id = "collapsible",
        title = "Collapsible",
        category = ShowcaseCategory.Layout,
        description = "Expandable disclosure container with height transition.",
        usageCode = "shadcnCollapsible(id = \"col\", title = \"Header\", expanded = exp) { ... }",
        notes = listOf("Smooth expand/collapse animation for hidden content."),
        renderPreview = { drawUiShowcaseCollapsiblePreview() },
    ),
    ShowcasePage(
        id = "sidebar",
        title = "Sidebar",
        category = ShowcaseCategory.Layout,
        description = "Navigation sidebar panel with collapsible groups and menus.",
        usageCode = "shadcnSidebar(id = \"sb\") { drawUiShowcaseSidebarMenu() }",
        notes = listOf("App navigation rail with sidebar tokens."),
        renderPreview = { drawUiShowcaseSidebarPreview() },
    ),
    ShowcasePage(
        id = "breadcrumb",
        title = "Breadcrumb",
        category = ShowcaseCategory.Layout,
        description = "Hierarchy navigation link trail with chevron dividers.",
        usageCode = "shadcnBreadcrumb(items = listOf(\"Home\", \"Components\", \"Button\"))",
        notes = listOf("Compact path trail for deep page hierarchies."),
        renderPreview = { drawUiShowcaseBreadcrumbPreview() },
    ),

    // --- Overlays & Popups ---
    ShowcasePage(
        id = "dialog",
        title = "Dialog",
        category = ShowcaseCategory.Overlays,
        description = "Centered modal dialog window with backdrop scrim.",
        usageCode = "shadcnDialog(id = \"dlg\", expanded = open, onDismissRequest = { ... }) { ... }",
        notes = listOf("Centered popup window with dismiss actions."),
        renderPreview = { drawUiShowcaseDialogPreview() },
    ),
    ShowcasePage(
        id = "drawer",
        title = "Drawer",
        category = ShowcaseCategory.Overlays,
        description = "Slide-over viewport overlay panel (Bottom, Top, Left, Right).",
        usageCode = "shadcnDrawer(id = \"drw\", expanded = open, position = ShadcnDrawerPosition.Bottom) { ... }",
        notes = listOf("Anchors to any viewport edge with overlay scrim."),
        renderPreview = { drawShadcnDrawerDemoPreview() },
    ),
    ShowcasePage(
        id = "context-menu",
        title = "Context Menu",
        category = ShowcaseCategory.Overlays,
        description = "Right-click trigger opening a floating action dropdown popup.",
        usageCode = "shadcnContextMenu(id = \"ctx\", expanded = open, items = items) { ... }",
        notes = listOf("Triggers on secondary pointer click over target bounds."),
        renderPreview = { drawShadcnContextMenuDemoPreview() },
    ),
    ShowcasePage(
        id = "tooltip",
        title = "Tooltip",
        category = ShowcaseCategory.Overlays,
        description = "Contextual hover popover surfacing helpful hint text.",
        usageCode = "shadcnTooltip(anchorSlot = slot, visible = true) { text(\"Hint\") }",
        notes = listOf("Floating popover positioned relative to anchor bounds."),
        renderPreview = { drawUiShowcaseTooltipPreview() },
    ),

    // --- Status & Feedback ---
    ShowcasePage(
        id = "alert",
        title = "Alert",
        category = ShowcaseCategory.Typography,
        description = "Callout message box for warnings, errors, and announcements.",
        usageCode = "shadcnAlert(title = \"Warning\", description = \"Action required\")",
        notes = listOf("Default and Destructive alert callout boxes."),
        renderPreview = { drawUiShowcaseAlertPreview() },
    ),
    ShowcasePage(
        id = "avatar",
        title = "Avatar",
        category = ShowcaseCategory.Typography,
        description = "Circular profile picture or fallback initial badge.",
        usageCode = "shadcnAvatar(initials = \"JD\")",
        notes = listOf("Rounded avatar with initial text fallback."),
        renderPreview = { drawUiShowcaseAvatarPreview() },
    ),
    ShowcasePage(
        id = "progress",
        title = "Progress",
        category = ShowcaseCategory.Typography,
        description = "Linear progress indicator track.",
        usageCode = "shadcnProgress(progress = 0.7f)",
        notes = listOf("Animated progress fill bar."),
        renderPreview = { drawUiShowcaseProgressPreview() },
    ),
    ShowcasePage(
        id = "skeleton",
        title = "Skeleton",
        category = ShowcaseCategory.Typography,
        description = "Placeholder wireframe pulse shape during data loading.",
        usageCode = "shadcnSkeleton(modifier = Modifier.width(120f.dp).height(20f.dp))",
        notes = listOf("Muted background box indicating content loading."),
        renderPreview = { drawUiShowcaseSkeletonPreview() },
    ),
    ShowcasePage(
        id = "spinner",
        title = "Spinner",
        category = ShowcaseCategory.Typography,
        description = "Circular activity loading spinner indicator.",
        usageCode = "shadcnSpinner(modifier = Modifier.width(24f.dp))",
        notes = listOf("Continuous rotation loading indicator."),
        renderPreview = { drawUiShowcaseSpinnerPreview() },
    ),
    ShowcasePage(
        id = "kbd",
        title = "Kbd Key",
        category = ShowcaseCategory.Typography,
        description = "Keyboard shortcut key pill display.",
        usageCode = "shadcnKbd(\"Ctrl+K\")",
        notes = listOf("Subtle border box representing physical keys."),
        renderPreview = { drawUiShowcaseKbdPreview() },
    ),

    // --- Typography & Patterns ---
    ShowcasePage(
        id = "typography",
        title = "Typography",
        category = ShowcaseCategory.Typography,
        description = "The shadcn text component family: shadcnSectionTitle, headline, bodyText, shadcnSupportingText, and label.",
        usageCode = "shadcnHeadline(\"Headline text\")",
        notes = listOf("Official typography components."),
        renderPreview = { drawUiShowcaseTypographySpecimenPreview() },
    ),
)

internal val ShowcasePagesByCategory = ShowcasePages.groupBy { it.category }

internal fun showcasePageById(pageId: String): ShowcasePage =
    ShowcasePages.firstOrNull { it.id == pageId }
        ?: when (pageId) {
            "buttons" -> showcasePageById("button")
            "text-input" -> showcasePageById("text-field")
            "selection" -> showcasePageById("checkbox")
            "feedback" -> showcasePageById("progress")
            "popups" -> showcasePageById("dialog")
            "kbd-separator" -> showcasePageById("kbd")
            else -> ShowcasePages.first()
        }
