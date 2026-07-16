// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseCounterContract
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiAlertDialogAction
import io.github.ronjunevaldoz.awake.ui.UiButtonVariant
import io.github.ronjunevaldoz.awake.ui.UiColumnDslScope
import io.github.ronjunevaldoz.awake.ui.UiDropdownMenuItem
import io.github.ronjunevaldoz.awake.ui.UiDropdownMenuSeparator
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.UiTextWrap
import io.github.ronjunevaldoz.awake.ui.alertDialog
import io.github.ronjunevaldoz.awake.ui.animateFloat
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnAccent
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnBaseColor
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnStylePreset
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnSurfaceVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnBodyText
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnDropdown
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnHeadline
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnPropertyDropdown
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnPropertySlider
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnPropertyToggle
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnSectionHeader
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnSectionTitle
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnToggle
import io.github.ronjunevaldoz.awake.ui.dropdownMenu
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.offset
import io.github.ronjunevaldoz.awake.ui.rememberPopupState
import io.github.ronjunevaldoz.awake.ui.rememberStateValue
import io.github.ronjunevaldoz.awake.ui.supportingLines
import io.github.ronjunevaldoz.awake.ui.textLines

private val ShowcaseStyleOptions = AwakeShadcnStylePreset.entries.map { it.label }
private val ShowcaseBaseColorOptions = AwakeShadcnBaseColor.entries.map { it.label }
private val ShowcaseAccentOptions = AwakeShadcnAccent.entries.map { it.label }
private val ShowcaseBadgeOptions = listOf("Primary", "Secondary", "Outline", "Danger")

private val ShowcaseActionMenuItems = listOf(
    UiDropdownMenuItem(
        label = "Pinned action",
        enabled = false,
        supportingText = "Disabled actions stay visible without becoming clickable."
    ),
    UiDropdownMenuSeparator,
    UiDropdownMenuItem(
        label = "Duplicate panel",
        trailingLabel = "Cmd+D",
        supportingText = "Example of a richer menu row with trailing metadata."
    ),
    UiDropdownMenuItem(
        label = "Delete scene",
        destructive = true,
        trailingLabel = "Del",
        supportingText = "Routes into the alert dialog flow instead of doing anything immediately."
    )
)

private enum class ShowcaseCategory(val title: String) {
    GettingStarted("Getting Started"),
    Foundations("Foundations"),
    Overlays("Overlays"),
    Patterns("Patterns"),
}

private data class ShowcasePage(
    val id: String,
    val title: String,
    val category: ShowcaseCategory,
    val description: String,
    val usageCode: String,
    val notes: List<String>,
)

private val ShowcasePages = listOf(
    ShowcasePage(
        id = "introduction",
        title = "Introduction",
        category = ShowcaseCategory.GettingStarted,
        description = "A dedicated catalog sample for owned Awake UI components instead of a one-off demo page.",
        usageCode = """
            gameUi {
                theme(awakeShadcnTheme())
                overlay { width, height ->
                    drawUiShowcaseOverlay(state, width, height)
                }
            }
        """.trimIndent(),
        notes = listOf(
            "This sample is now a docs-style catalog instead of a single giant component pile.",
            "Theme tokens, sidebar navigation, preview framing, and usage snippets all live together.",
            "It mirrors the shadcn-compose pattern of chrome + sidebar + detail pane."
        )
    ),
    ShowcasePage(
        id = "reference",
        title = "Reference Comparison",
        category = ShowcaseCategory.GettingStarted,
        description = "A side-by-side checkpoint for the official shadcn cues we are trying to match.",
        usageCode = """
            awakeShadcnButton("primary", 120f, 36f, "Primary")
            awakeShadcnDropdown("style", options, selectedIndex = 0, width = 128f)
            awakeShadcnSurface("card", variant = AwakeShadcnSurfaceVariant.Card) { ... }
        """.trimIndent(),
        notes = listOf(
            "The point is visual calibration, not pixel-perfect cloning.",
            "The biggest cues are control height, popover containment, and softer card contrast."
        )
    ),
    ShowcasePage(
        id = "theming",
        title = "Theming",
        category = ShowcaseCategory.GettingStarted,
        description = "Live preset, base color, accent, and dark mode controls feeding the same public Awake theme factory.",
        usageCode = """
            val theme = awakeShadcnTheme(
                preset = AwakeShadcnStylePreset.Vega,
                baseColor = AwakeShadcnBaseColor.Neutral,
                accent = AwakeShadcnAccent.Base,
                dark = true
            )
        """.trimIndent(),
        notes = listOf(
            "This is the Awake-owned equivalent of the shadcn-compose catalog theme pickers.",
            "Chrome and content are reading the same theme state, so regressions show up fast."
        )
    ),
    ShowcasePage(
        id = "buttons",
        title = "Buttons And Badges",
        category = ShowcaseCategory.Foundations,
        description = "Core action and status components with tighter sizing and the current shadcn-style variants.",
        usageCode = """
            awakeShadcnButton(
                id = "save",
                width = 120f,
                height = 36f,
                label = "Save",
                variant = AwakeShadcnButtonVariant.Primary
            )
            awakeShadcnBadge("LIVE", variant = AwakeShadcnBadgeVariant.Secondary)
        """.trimIndent(),
        notes = listOf(
            "These are the first controls that reveal whether spacing and typography feel right.",
            "Their default height now sits closer to the official 36px rhythm."
        )
    ),
    ShowcasePage(
        id = "popups",
        title = "Dropdown Menu And Dialog",
        category = ShowcaseCategory.Overlays,
        description = "Proof that our menus and dialogs behave like real overlay surfaces rather than loose button stacks.",
        usageCode = """
            val trigger = buttonSlot("actions", label = "Actions", width = 112f, height = 36f)
            val result = dropdownMenu(
                id = "actions.menu",
                anchorSlot = trigger.slot,
                expanded = popupState.expanded,
                items = ShowcaseActionMenuItems
            )
            alertDialog("delete", expanded = dialogState.expanded, title = "Delete scene?")
        """.trimIndent(),
        notes = listOf(
            "The dropdown now renders inside a popover container with padding and grouped rows.",
            "This page is the easiest way to spot overlay spacing regressions."
        )
    ),
    ShowcasePage(
        id = "state",
        title = "State Container",
        category = ShowcaseCategory.Patterns,
        description = "A small Awake-native MVI sample showing reducer state, intents, and one-shot effects.",
        usageCode = """
            state.counterStore.dispatch(UiShowcaseCounterContract.Intent.Increment)
            val counterState = state.counterStore.state.value
            val effects = state.counterStore.drainEffects()
        """.trimIndent(),
        notes = listOf(
            "This is the pattern we can keep reusing once samples stop being static showcases.",
            "Effects stay transient instead of leaking into persistent UI state."
        )
    )
)

private val ShowcasePagesByCategory = ShowcasePages.groupBy { it.category }

internal fun UiColumnDslScope.drawUiShowcaseTopBar(
    state: UiShowcaseRuntimeState,
    compact: Boolean,
) {
    if (compact) {
        awakeShadcnBadge("CATALOG", variant = AwakeShadcnBadgeVariant.Primary)
        awakeShadcnHeadline("Awake UI Showcase")
        awakeShadcnSupportingText("A docs-style shell for our owned shadcn-inspired components.")
        spacer(10f)
        row(height = 72f, gap = 8f) {
            panel(
                id = "ui-showcase-topbar-preset-compact",
                width = Dimension.Fixed(150f.dp),
                style = theme.components.panel then Style { shape(12f.dp) }
            ) {
                awakeShadcnSectionTitle("Preset")
                awakeShadcnDropdown(
                    id = "ui-showcase-topbar-style",
                    options = ShowcaseStyleOptions,
                    selectedIndex = state.showcaseStylePresetIndex,
                    width = 118f
                )?.let { state.showcaseStylePresetIndex = it }
            }
            panel(
                id = "ui-showcase-topbar-dark-compact",
                width = Dimension.Fixed(138f.dp),
                style = theme.components.panel then Style { shape(12f.dp) }
            ) {
                awakeShadcnSectionTitle("Dark")
                state.showcaseDarkMode = awakeShadcnToggle(
                    id = "ui-showcase-topbar-dark-toggle",
                    checked = state.showcaseDarkMode,
                    width = 108f,
                    height = 36f,
                    label = if (state.showcaseDarkMode) "Enabled" else "Disabled"
                )
            }
        }
        return
    }

    row(height = 76f, gap = 12f) {
        panel(
            id = "ui-showcase-topbar-title",
            width = Dimension.Fixed(324f.dp),
            style = theme.components.panel then Style { shape(12f.dp) }
        ) {
            awakeShadcnBadge("CATALOG", variant = AwakeShadcnBadgeVariant.Primary)
            awakeShadcnHeadline("Awake UI Showcase")
            awakeShadcnSupportingText("Structured like the shadcn-compose catalog: stable chrome, grouped sidebar, one detail page at a time.")
        }
        panel(
            id = "ui-showcase-topbar-controls",
            width = Dimension.Fixed(584f.dp),
            style = theme.components.panel then Style { shape(12f.dp) }
        ) {
            row(height = 58f, gap = 8f) {
                panel(
                    id = "ui-showcase-topbar-style-card",
                    width = Dimension.Fixed(128f.dp),
                    style = theme.components.panel then Style { shape(10f.dp) }
                ) {
                    awakeShadcnSectionTitle("Preset")
                    awakeShadcnDropdown(
                        id = "ui-showcase-desktop-style",
                        options = ShowcaseStyleOptions,
                        selectedIndex = state.showcaseStylePresetIndex,
                        width = 104f
                    )?.let { state.showcaseStylePresetIndex = it }
                }
                panel(
                    id = "ui-showcase-topbar-base-card",
                    width = Dimension.Fixed(128f.dp),
                    style = theme.components.panel then Style { shape(10f.dp) }
                ) {
                    awakeShadcnSectionTitle("Base")
                    awakeShadcnDropdown(
                        id = "ui-showcase-desktop-base",
                        options = ShowcaseBaseColorOptions,
                        selectedIndex = state.showcaseBaseColorIndex,
                        width = 104f
                    )?.let { state.showcaseBaseColorIndex = it }
                }
                panel(
                    id = "ui-showcase-topbar-accent-card",
                    width = Dimension.Fixed(128f.dp),
                    style = theme.components.panel then Style { shape(10f.dp) }
                ) {
                    awakeShadcnSectionTitle("Accent")
                    awakeShadcnDropdown(
                        id = "ui-showcase-desktop-accent",
                        options = ShowcaseAccentOptions,
                        selectedIndex = state.showcaseAccentIndex,
                        width = 104f
                    )?.let { state.showcaseAccentIndex = it }
                }
                panel(
                    id = "ui-showcase-topbar-dark-card",
                    width = Dimension.Fixed(128f.dp),
                    style = theme.components.panel then Style { shape(10f.dp) }
                ) {
                    awakeShadcnSectionTitle("Dark")
                    state.showcaseDarkMode = awakeShadcnToggle(
                        id = "ui-showcase-desktop-dark",
                        checked = state.showcaseDarkMode,
                        width = 104f,
                        height = 36f,
                        label = if (state.showcaseDarkMode) "Enabled" else "Disabled"
                    )
                }
            }
        }
    }
}

internal fun UiColumnDslScope.drawUiShowcaseSidebar(compact: Boolean) {
    val selectedPage = context.rememberStateValue("ui-showcase-page", "entry") {
        ShowcasePages.first().id
    }
    awakeShadcnBadge("SHADCN", variant = AwakeShadcnBadgeVariant.Primary)
    awakeShadcnHeadline("Catalog")
    awakeShadcnSupportingText(
        if (compact) {
            "Choose one page at a time."
        } else {
            "Grouped component and pattern pages, following the shadcn-compose catalog layout."
        }
    )
    spacer(12f)
    drawUiShowcaseSidebarMenu(
        compact = compact,
        selectedPageId = selectedPage.value,
        onSelect = { selectedPage.value = it.id }
    )
}

internal fun UiColumnDslScope.drawUiShowcasePageContent(
    state: UiShowcaseRuntimeState,
    showInlineMenu: Boolean,
) {
    val selectedPage = context.rememberStateValue("ui-showcase-page", "entry") {
        ShowcasePages.first().id
    }
    val page = ShowcasePages.firstOrNull { it.id == selectedPage.value } ?: ShowcasePages.first()

    if (showInlineMenu) {
        drawUiShowcaseSidebarMenu(
            compact = true,
            selectedPageId = page.id,
            onSelect = { selectedPage.value = it.id }
        )
        spacer(12f)
    }

    awakeShadcnBadge(page.category.title.uppercase(), variant = AwakeShadcnBadgeVariant.Outline)
    awakeShadcnSectionHeader(
        title = page.title,
        description = page.description
    )
    spacer(8f)
    drawUiShowcasePreviewCodeSection(page, state)
    spacer(12f)
    awakeShadcnSurface(
        id = "ui-showcase-usage-${page.id}",
        height = Dimension.WrapContent,
        variant = AwakeShadcnSurfaceVariant.Card,
        style = Style { shape(14f.dp) }
    ) {
        awakeShadcnSectionTitle("Usage")
        drawUiShowcaseCodeBlock(page.usageCode)
    }
    if (page.notes.isNotEmpty()) {
        spacer(12f)
        awakeShadcnSurface(
            id = "ui-showcase-notes-${page.id}",
            height = Dimension.WrapContent,
            variant = AwakeShadcnSurfaceVariant.Card,
            style = Style { shape(14f.dp) }
        ) {
            awakeShadcnSectionTitle("Notes")
            supportingLines(page.notes)
        }
    }
}

private fun UiColumnDslScope.drawUiShowcaseSidebarMenu(
    compact: Boolean,
    selectedPageId: String,
    onSelect: (ShowcasePage) -> Unit,
) {
    ShowcasePagesByCategory.forEach { (category, pages) ->
        if (!compact) {
            awakeShadcnSectionTitle(category.title)
            spacer(4f)
        }
        pages.forEach { page ->
            if (
                awakeShadcnButton(
                    id = "ui-showcase-page-${page.id}",
                    width = 0f,
                    height = 36f,
                    label = page.title,
                    modifier = UiModifier(width = Dimension.FillMax),
                    variant = if (page.id == selectedPageId) AwakeShadcnButtonVariant.Primary else AwakeShadcnButtonVariant.Ghost
                )
            ) {
                onSelect(page)
            }
        }
        spacer(if (compact) 8f else 12f)
    }
}

private fun UiColumnDslScope.drawUiShowcasePreviewCodeSection(
    page: ShowcasePage,
    state: UiShowcaseRuntimeState,
) {
    val showCode = context.rememberStateValue("ui-showcase-page", "${page.id}.show-code") { false }
    row(height = 36f, gap = 8f) {
        awakeShadcnButton(
            id = "ui-showcase-preview-tab-${page.id}",
            width = 96f,
            height = 36f,
            label = "Preview",
            variant = if (!showCode.value) AwakeShadcnButtonVariant.Primary else AwakeShadcnButtonVariant.Ghost
        ).also { clicked ->
            if (clicked) showCode.value = false
        }
        awakeShadcnButton(
            id = "ui-showcase-code-tab-${page.id}",
            width = 88f,
            height = 36f,
            label = "Code",
            variant = if (showCode.value) AwakeShadcnButtonVariant.Primary else AwakeShadcnButtonVariant.Ghost
        ).also { clicked ->
            if (clicked) showCode.value = true
        }
    }
    spacer(8f)
    awakeShadcnSurface(
        id = "ui-showcase-preview-code-${page.id}",
        height = Dimension.WrapContent,
        variant = AwakeShadcnSurfaceVariant.Card,
        style = Style { shape(14f.dp) }
    ) {
        if (showCode.value) {
            drawUiShowcaseCodeBlock(page.usageCode)
        } else {
            renderUiShowcasePagePreview(page, state)
        }
    }
}

private fun UiColumnDslScope.drawUiShowcaseCodeBlock(code: String) {
    textLines(
        lines = code.trimIndent().lines(),
        style = Style {
            foreground(theme.tokens.foreground)
        },
        wrap = UiTextWrap.Word,
        overflow = UiTextOverflow.Clip,
        maxLines = Int.MAX_VALUE
    )
}

private fun UiColumnDslScope.renderUiShowcasePagePreview(
    page: ShowcasePage,
    state: UiShowcaseRuntimeState,
) {
    when (page.id) {
        "introduction" -> drawUiShowcaseOverviewPreview()
        "reference" -> drawUiShowcaseReferenceComparisonPreview()
        "theming" -> drawUiShowcaseControlsPreview(state)
        "buttons" -> drawUiShowcaseButtonsPreview()
        "popups" -> drawUiShowcasePopupPreview()
        "state" -> drawUiShowcaseCounterPreview(state)
    }
}

private fun UiColumnDslScope.drawUiShowcaseOverviewPreview() {
    awakeShadcnBadge("SHOWCASE", variant = AwakeShadcnBadgeVariant.Secondary)
    awakeShadcnBodyText("Dedicated sample route")
    awakeShadcnSupportingText("This page shell exists so the design system is judged as a product surface, not just as loose demo widgets.")
    spacer(8f)
    supportingLines(
        listOf(
            "Stable chrome on top, grouped navigation on the left, one detail page in the content pane.",
            "The starter sample stays a starter sample; docs and polish move here.",
            "This is now the right home for future design-system tutorials and regression proofs."
        )
    )
}

private fun UiColumnDslScope.drawUiShowcaseReferenceComparisonPreview() {
    val cardWidth = 180f
    row(height = 236f, gap = 12f) {
        panel(
            id = "ui-showcase-reference-spec",
            width = Dimension.Fixed(cardWidth.dp),
            height = Dimension.Fixed(236f.dp),
            style = theme.components.panel then Style { shape(14f.dp) }
        ) {
            awakeShadcnSectionTitle("Official cues")
            awakeShadcnSupportingText("The reference we keep checking against.")
            spacer(8f)
            supportingLines(
                listOf(
                    "Controls feel closer to 36px than 44px.",
                    "Dropdown content is a popover, not a bare button stack.",
                    "Cards sit close to the page background with restrained contrast."
                )
            )
            spacer(8f)
            awakeShadcnBadge("TARGET", variant = AwakeShadcnBadgeVariant.Outline)
        }
        panel(
            id = "ui-showcase-reference-awake",
            width = Dimension.Fixed(cardWidth.dp),
            height = Dimension.Fixed(236f.dp),
            style = theme.components.panel then Style { shape(14f.dp) }
        ) {
            awakeShadcnSectionTitle("Awake now")
            awakeShadcnSupportingText("Our current implementation after the sizing and popover pass.")
            spacer(8f)
            awakeShadcnBodyText("Typography is tighter, menu surfaces are contained, and the gray slab effect is reduced.")
            spacer(8f)
            row(height = 36f, gap = 8f) {
                awakeShadcnButton("reference-primary", 100f, 36f, "Primary", variant = AwakeShadcnButtonVariant.Primary)
                awakeShadcnButton("reference-outline", 96f, 36f, "Outline", variant = AwakeShadcnButtonVariant.Outline)
            }
            spacer(8f)
            awakeShadcnBadge("AWAKE", variant = AwakeShadcnBadgeVariant.Primary)
        }
    }
}

private fun UiColumnDslScope.drawUiShowcaseButtonsPreview() {
    awakeShadcnSectionTitle("Buttons")
    row(height = 36f, gap = 10f) {
        awakeShadcnButton("showcase-primary", 120f, 36f, "Primary", variant = AwakeShadcnButtonVariant.Primary)
        awakeShadcnButton("showcase-secondary", 120f, 36f, "Secondary", variant = AwakeShadcnButtonVariant.Secondary)
    }
    row(height = 36f, gap = 10f) {
        awakeShadcnButton("showcase-outline", 112f, 36f, "Outline", variant = AwakeShadcnButtonVariant.Outline)
        awakeShadcnButton("showcase-ghost", 100f, 36f, "Ghost", variant = AwakeShadcnButtonVariant.Ghost)
        awakeShadcnButton("showcase-danger", 104f, 36f, "Danger", variant = AwakeShadcnButtonVariant.Danger)
    }
    spacer(10f)
    awakeShadcnSectionTitle("Badges")
    row(height = 34f, gap = 8f) {
        awakeShadcnBadge("LIVE", variant = AwakeShadcnBadgeVariant.Primary)
        awakeShadcnBadge("SCENE", variant = AwakeShadcnBadgeVariant.Secondary)
        awakeShadcnBadge("BETA", variant = AwakeShadcnBadgeVariant.Outline)
        awakeShadcnBadge("RISK", variant = AwakeShadcnBadgeVariant.Danger)
    }
}

private fun UiColumnDslScope.drawUiShowcaseControlsPreview(state: UiShowcaseRuntimeState) {
    awakeShadcnSectionTitle("Theme controls")
    awakeShadcnPropertyDropdown(
        id = "showcase-style-preset",
        label = "Style",
        options = ShowcaseStyleOptions,
        selectedIndex = state.showcaseStylePresetIndex,
        labelWidth = 72f.dp
    )?.let { state.showcaseStylePresetIndex = it }
    awakeShadcnPropertyDropdown(
        id = "showcase-base-color",
        label = "Base",
        options = ShowcaseBaseColorOptions,
        selectedIndex = state.showcaseBaseColorIndex,
        labelWidth = 72f.dp
    )?.let { state.showcaseBaseColorIndex = it }
    awakeShadcnPropertyDropdown(
        id = "showcase-accent",
        label = "Accent",
        options = ShowcaseAccentOptions,
        selectedIndex = state.showcaseAccentIndex,
        labelWidth = 72f.dp
    )?.let { state.showcaseAccentIndex = it }
    val nextDark = awakeShadcnPropertyToggle(
        id = "showcase-dark-mode",
        label = "Dark mode",
        checked = state.showcaseDarkMode,
        height = 36f
    )
    if (nextDark != state.showcaseDarkMode) state.showcaseDarkMode = nextDark

    spacer(10f)
    awakeShadcnSectionTitle("Live preview")
    val nextLive = awakeShadcnPropertyToggle(
        id = "showcase-live",
        label = "Live badge",
        checked = state.showcaseLiveBadge,
        height = 36f
    )
    if (nextLive != state.showcaseLiveBadge) state.showcaseLiveBadge = nextLive

    val nextDanger = awakeShadcnPropertyToggle(
        id = "showcase-danger-mode",
        label = "Danger mode",
        checked = state.showcaseDangerMode,
        height = 36f
    )
    if (nextDanger != state.showcaseDangerMode) state.showcaseDangerMode = nextDanger

    awakeShadcnPropertyDropdown(
        id = "showcase-badge-variant",
        label = "Badge",
        options = ShowcaseBadgeOptions,
        selectedIndex = state.showcaseBadgeVariantIndex,
        height = 36f
    )?.let { state.showcaseBadgeVariantIndex = it }

    state.showcaseSurfaceRadius = awakeShadcnPropertySlider(
        id = "showcase-radius",
        label = "Radius",
        min = 8f,
        max = 24f,
        value = state.showcaseSurfaceRadius,
        height = 36f
    )

    spacer(10f)
    val previewLift = context.animateFloat(
        id = "showcase-preview-lift",
        target = if (state.showcaseDangerMode) 10f else 0f,
        responsiveness = 10f
    )
    awakeShadcnSurface(
        id = "showcase-preview",
        height = Dimension.WrapContent,
        modifier = UiModifier().offset(y = (-previewLift).dp),
        variant = AwakeShadcnSurfaceVariant.Muted,
        style = Style { shape(state.showcaseSurfaceRadius.dp) }
    ) {
        val badgeVariant = state.showcaseBadgeVariant()
        awakeShadcnBadge(if (state.showcaseLiveBadge) "LIVE" else "PAUSED", variant = badgeVariant)
        row(height = 28f, gap = 8f) {
            awakeShadcnBadge(
                label = state.showcaseStylePreset().label.uppercase(),
                variant = AwakeShadcnBadgeVariant.Outline
            )
            awakeShadcnBadge(
                label = state.showcaseBaseColor().label.uppercase(),
                variant = AwakeShadcnBadgeVariant.Secondary
            )
            awakeShadcnBadge(
                label = state.showcaseAccent().label.uppercase(),
                variant = if (state.showcaseAccent() == AwakeShadcnAccent.Base) {
                    AwakeShadcnBadgeVariant.Outline
                } else {
                    AwakeShadcnBadgeVariant.Primary
                }
            )
        }
        awakeShadcnBodyText("Showcase preview card")
        awakeShadcnSupportingText("The same Awake theme factory feeds the shell chrome and the inner components.")
        spacer(6f)
        row(height = 36f, gap = 10f) {
            if (
                awakeShadcnButton(
                    id = "preview-primary-action",
                    width = 112f,
                    height = 36f,
                    label = "Inspect",
                    variant = AwakeShadcnButtonVariant.Primary
                )
            ) {
                state.showcasePrimaryClicks += 1
            }
            awakeShadcnButton(
                id = "preview-secondary-action",
                width = 120f,
                height = 36f,
                label = if (state.showcaseDangerMode) "Rollback" else "Publish",
                variant = if (state.showcaseDangerMode) AwakeShadcnButtonVariant.Danger else AwakeShadcnButtonVariant.Outline
            )
        }
        awakeShadcnBodyText("Primary clicks: ${state.showcasePrimaryClicks}")
    }
}

private fun UiColumnDslScope.drawUiShowcaseCounterPreview(state: UiShowcaseRuntimeState) {
    state.counterStore.drainEffects()
        .lastOrNull()
        ?.let { effect -> state.showcaseCounterEffectMessage = effect.toDebugLabel() }

    val counterState = state.counterStore.state.value
    awakeShadcnSectionHeader(
        title = "MVI Counter",
        description = "A tiny reducer-backed example with effects kept off persistent state."
    )
    awakeShadcnBadge("MVI", variant = AwakeShadcnBadgeVariant.Primary)
    awakeShadcnBodyText("Count: ${counterState.count}")
    awakeShadcnSupportingText("Last effect: ${state.showcaseCounterEffectMessage ?: "None"}")
    spacer(6f)
    row(height = 36f, gap = 10f) {
        if (
            awakeShadcnButton(
                id = "counter-decrement",
                width = 112f,
                height = 36f,
                label = "Decrement",
                variant = AwakeShadcnButtonVariant.Outline
            )
        ) {
            state.counterStore.dispatch(UiShowcaseCounterContract.Intent.Decrement)
        }
        if (
            awakeShadcnButton(
                id = "counter-increment",
                width = 112f,
                height = 36f,
                label = "Increment",
                variant = AwakeShadcnButtonVariant.Primary
            )
        ) {
            state.counterStore.dispatch(UiShowcaseCounterContract.Intent.Increment)
        }
    }
    row(height = 36f, gap = 10f) {
        if (
            awakeShadcnButton(
                id = "counter-reset",
                width = 112f,
                height = 36f,
                label = "Reset",
                variant = AwakeShadcnButtonVariant.Ghost
            )
        ) {
            state.counterStore.dispatch(UiShowcaseCounterContract.Intent.Reset)
        }
        awakeShadcnBadge(
            label = if (counterState.count >= 0) "FLOW" else "NEGATIVE",
            variant = if (counterState.count >= 0) AwakeShadcnBadgeVariant.Secondary else AwakeShadcnBadgeVariant.Danger
        )
    }
}

private fun UiColumnDslScope.drawUiShowcasePopupPreview() {
    val actionMenuState = context.rememberPopupState("ui-showcase-action-menu")
    val deleteDialogState = context.rememberPopupState("ui-showcase-delete-dialog")
    val feedbackMessage = context.rememberStateValue("ui-showcase-popup-feedback") {
        "Try the action menu and dialog to inspect the popup layer."
    }

    awakeShadcnSectionHeader(
        title = "Popup Components",
        description = "Menu and dialog proofs running through the shared DSL surface."
    )
    awakeShadcnBadge("OVERLAY", variant = AwakeShadcnBadgeVariant.Outline)
    awakeShadcnSupportingText("The action menu anchors to the trigger and opens inside a contained popover surface.")
    spacer(6f)
    row(height = 36f, gap = 10f) {
        val menuTrigger = buttonSlot(
            id = "ui-showcase-menu-trigger",
            label = "Actions",
            width = 112f,
            height = 36f,
            style = theme.components.button,
            variant = UiButtonVariant.Filled
        )
        if (menuTrigger.clicked) {
            actionMenuState.toggle()
        }
        val menuResult = dropdownMenu(
            id = "ui-showcase-action-menu",
            anchorSlot = menuTrigger.slot,
            expanded = actionMenuState.expanded,
            items = ShowcaseActionMenuItems,
            style = Style { contentPadding(4f.dp) }
        )
        when (menuResult.selectedIndex) {
            1 -> {
                feedbackMessage.value = "Duplicate panel queued from the dropdown menu."
                actionMenuState.close()
            }
            2 -> {
                feedbackMessage.value = "Delete requested from the dropdown menu."
                actionMenuState.close()
                deleteDialogState.open()
            }
        }
        if (menuResult.dismissed) {
            actionMenuState.close()
        }
        if (
            awakeShadcnButton(
                id = "ui-showcase-delete-trigger",
                width = 128f,
                height = 36f,
                label = "Open Dialog",
                variant = AwakeShadcnButtonVariant.Outline
            )
        ) {
            deleteDialogState.open()
        }
    }
    spacer(4f)
    awakeShadcnSupportingText(feedbackMessage.value)

    val dialogResult = alertDialog(
        id = "ui-showcase-delete-dialog",
        expanded = deleteDialogState.expanded,
        title = "Delete showcase card?",
        message = "This sample does not really delete anything. It exists to prove the alert dialog composition and confirm or dismiss flow."
    )
    when (dialogResult.action) {
        UiAlertDialogAction.Confirm -> {
            feedbackMessage.value = "Confirmed from the alert dialog."
            deleteDialogState.close()
        }
        UiAlertDialogAction.Dismiss -> {
            feedbackMessage.value = "Dismissed from the alert dialog."
            deleteDialogState.close()
        }
        null -> {
            if (dialogResult.popup.dismissed) {
                feedbackMessage.value = "Dismissed by clicking outside the alert dialog."
                deleteDialogState.close()
            }
        }
    }
}

private fun UiShowcaseRuntimeState.showcaseBadgeVariant(): AwakeShadcnBadgeVariant = when (showcaseBadgeVariantIndex) {
    0 -> AwakeShadcnBadgeVariant.Primary
    1 -> AwakeShadcnBadgeVariant.Secondary
    2 -> AwakeShadcnBadgeVariant.Outline
    else -> AwakeShadcnBadgeVariant.Danger
}

private fun UiShowcaseCounterContract.Effect.toDebugLabel(): String = when (this) {
    is UiShowcaseCounterContract.Effect.MilestoneReached -> "Milestone reached at $count"
    UiShowcaseCounterContract.Effect.ResetCompleted -> "Counter reset"
}
