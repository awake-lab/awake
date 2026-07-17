// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseCounterContract
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseThemeMode
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiAlertDialogAction
import io.github.ronjunevaldoz.awake.ui.UiButtonVariant
import io.github.ronjunevaldoz.awake.ui.UiColumnDslScope
import io.github.ronjunevaldoz.awake.ui.UiDropdownMenuItem
import io.github.ronjunevaldoz.awake.ui.UiDropdownMenuSeparator
import io.github.ronjunevaldoz.awake.ui.UiLinearGradient
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiSlot
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
import io.github.ronjunevaldoz.awake.ui.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.gradientBorder
import io.github.ronjunevaldoz.awake.ui.gradientRect
import io.github.ronjunevaldoz.awake.ui.height
import io.github.ronjunevaldoz.awake.ui.offset
import io.github.ronjunevaldoz.awake.ui.rememberBooleanState
import io.github.ronjunevaldoz.awake.ui.rememberPopupState
import io.github.ronjunevaldoz.awake.ui.rememberStateValue
import io.github.ronjunevaldoz.awake.ui.sp
import io.github.ronjunevaldoz.awake.ui.supportingLines
import io.github.ronjunevaldoz.awake.ui.text
import io.github.ronjunevaldoz.awake.ui.textLines
import io.github.ronjunevaldoz.awake.ui.UiInsets
import io.github.ronjunevaldoz.awake.ui.width
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.font.UiFonts

private val ShowcaseStyleOptions = AwakeShadcnStylePreset.entries.map { it.label }
private val ShowcaseBaseColorOptions = AwakeShadcnBaseColor.entries.map { it.label }
private val ShowcaseAccentOptions = AwakeShadcnAccent.entries.map { it.label }
private val ShowcaseThemeModeOptions = UiShowcaseThemeMode.entries.map { it.label }
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

internal enum class ShowcaseCategory(val title: String) {
    GettingStarted("Getting Started"),
    Foundations("Foundations"),
    Overlays("Overlays"),
    Patterns("Patterns"),
}

internal data class ShowcasePage(
    val id: String,
    val title: String,
    val category: ShowcaseCategory,
    val description: String,
    val usageCode: String,
    val notes: List<String>,
)

internal val ShowcasePages = listOf(
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
            awakeShadcnButton(
                id = "primary",
                label = "Primary",
                modifier = UiModifier().width(120f.dp).height(36f.dp)
            )
            awakeShadcnDropdown(
                id = "style",
                options = options,
                selectedIndex = 0,
                modifier = UiModifier().width(128f.dp)
            )
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
            "The app chrome stays on a stable shell theme while the content pane is re-themed live."
        )
    ),
    ShowcasePage(
        id = "fonts",
        title = "Bitmap And True Font",
        category = ShowcaseCategory.Foundations,
        description = "A direct specimen view of the current bitmap default beside a real TTF-derived atlas font.",
        usageCode = """
            val bitmap = UiFonts.bitmap()
            val trueSans = UiFonts.trueSans()

            // Render the same specimen strings with each font path.
        """.trimIndent(),
        notes = listOf(
            "Bitmap remains available as the explicit pixel fallback and debug path.",
            "The default runtime font now uses the true-font atlas so spacing and letterforms stop fighting the showcase."
        )
    ),
    ShowcasePage(
        id = "layout",
        title = "Layout Primitives",
        category = ShowcaseCategory.Foundations,
        description = "row(...) and column(...) -- the modifier-first layout primitives every other page in this catalog is built from.",
        usageCode = """
            row(height = 48f.dp, gap = 8f) {
                panel(id = "a", width = Dimension.Fixed(80f.dp), height = Dimension.FillMax) { }
                panel(id = "b", width = Dimension.Fixed(120f.dp), height = Dimension.FillMax) { }
            }
            column(height = Dimension.Fixed(112f.dp), width = Dimension.Fixed(200f.dp), gap = 6f) {
                panel(id = "a", width = Dimension.FillMax, height = Dimension.Fixed(28f.dp)) { }
            }
        """.trimIndent(),
        notes = listOf(
            "row/column advance a cursor along one axis -- each child claims the next slot in call order.",
            "Neither has a flex-grow concept: FillMax only resolves against the enclosing scope's own fixed axis (a column's configured width, not a row's advancing width)."
        )
    ),
    ShowcasePage(
        id = "slot-apis",
        title = "Slot APIs",
        category = ShowcaseCategory.Patterns,
        description = "buttonSlot(...)'s content-lambda form composes arbitrary content inside a widget's own claimed slot instead of a fixed label string.",
        usageCode = """
            buttonSlot(id = "launch", modifier = UiModifier().width(180f.dp).height(40f.dp)) {
                text(">", modifier = UiModifier().offset(x = 12f.dp).width(16f.dp))
                text("Launch", modifier = UiModifier().offset(x = 32f.dp))
            }
        """.trimIndent(),
        notes = listOf(
            "The label-string overload is sugar over this content-lambda form -- there is no capability gap between them.",
            "A custom widget (like this catalog's own recipes) uses this exact same slot-content pattern, not a special library-only path."
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
                label = "Save",
                modifier = UiModifier().width(120f.dp).height(36f.dp),
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
            val trigger = buttonSlot(
                id = "actions",
                label = "Actions",
                modifier = UiModifier().width(112f.dp).height(36f.dp)
            )
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
    spacer(UiModifier().height(12f.dp))
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
        spacer(UiModifier().height(12f.dp))
    }

    awakeShadcnBadge(page.category.title.uppercase(), variant = AwakeShadcnBadgeVariant.Outline)
    awakeShadcnSectionHeader(
        title = page.title,
        description = page.description
    )
    spacer(UiModifier().height(8f.dp))
    drawUiShowcasePreviewCodeSection(page, state)
    spacer(UiModifier().height(12f.dp))
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
        spacer(UiModifier().height(12f.dp))
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
            spacer(UiModifier().height(4f.dp))
        }
        pages.forEach { page ->
            if (
                awakeShadcnButton(
                    id = "ui-showcase-page-${page.id}",
                    label = page.title,
                    modifier = UiModifier()
                        .fillMaxWidth()
                        .height(36f.dp),
                    style = Style {
                        contentPadding(start = 14f.dp, top = 0f.dp, end = 14f.dp, bottom = 0f.dp)
                    },
                    variant = if (page.id == selectedPageId) AwakeShadcnButtonVariant.Primary else AwakeShadcnButtonVariant.Ghost,
                    centered = false,
                    verticallyCentered = true
                )
            ) {
                onSelect(page)
            }
        }
        spacer(UiModifier().height(if (compact) 8f.dp else 12f.dp))
    }
}

private fun UiColumnDslScope.drawUiShowcasePreviewCodeSection(
    page: ShowcasePage,
    state: UiShowcaseRuntimeState,
) {
    val showCode = context.rememberStateValue("ui-showcase-page", "${page.id}.show-code") { false }
    row(height = 36f.dp, gap = 8f) {
        awakeShadcnButton(
            id = "ui-showcase-preview-tab-${page.id}",
            label = "Preview",
            modifier = UiModifier().width(96f.dp).height(36f.dp),
            variant = if (!showCode.value) AwakeShadcnButtonVariant.Primary else AwakeShadcnButtonVariant.Ghost
        ).also { clicked ->
            if (clicked) showCode.value = false
        }
        awakeShadcnButton(
            id = "ui-showcase-code-tab-${page.id}",
            label = "Code",
            modifier = UiModifier().width(88f.dp).height(36f.dp),
            variant = if (showCode.value) AwakeShadcnButtonVariant.Primary else AwakeShadcnButtonVariant.Ghost
        ).also { clicked ->
            if (clicked) showCode.value = true
        }
    }
    spacer(UiModifier().height(8f.dp))
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

internal fun UiColumnDslScope.renderUiShowcasePagePreview(
    page: ShowcasePage,
    state: UiShowcaseRuntimeState,
) {
    when (page.id) {
        "introduction" -> drawUiShowcaseOverviewPreview()
        "reference" -> drawUiShowcaseReferenceComparisonPreview()
        "theming" -> drawUiShowcaseControlsPreview(state)
        "fonts" -> drawUiShowcaseFontsPreview()
        "layout" -> drawUiShowcaseLayoutPreview()
        "slot-apis" -> drawUiShowcaseSlotApiPreview()
        "buttons" -> drawUiShowcaseButtonsPreview()
        "popups" -> drawUiShowcasePopupPreview()
        "state" -> drawUiShowcaseCounterPreview(state)
    }
}

private fun UiColumnDslScope.drawUiShowcaseOverviewPreview() {
    awakeShadcnBadge("SHOWCASE", variant = AwakeShadcnBadgeVariant.Secondary)
    awakeShadcnBodyText("Dedicated sample route")
    awakeShadcnSupportingText("This page shell exists so the design system is judged as a product surface, not just as loose demo widgets.")
    spacer(UiModifier().height(8f.dp))
    supportingLines(
        listOf(
            "Stable chrome on top, grouped navigation on the left, one detail page in the content pane.",
            "The starter sample stays a starter sample; docs and polish move here.",
            "This is now the right home for future design-system tutorials and regression proofs."
        )
    )
}

private fun UiColumnDslScope.drawUiShowcaseReferenceComparisonPreview() {
    val cardWidth = 220f
    row(height = 284f.dp, gap = 12f) {
        panel(
            id = "ui-showcase-reference-spec",
            width = Dimension.Fixed(cardWidth.dp),
            height = Dimension.Fixed(284f.dp),
            style = theme.components.panel then Style { shape(14f.dp) }
        ) {
            awakeShadcnSectionTitle("Official cues")
            awakeShadcnSupportingText("The reference we keep checking against.")
            spacer(UiModifier().height(8f.dp))
            supportingLines(
                listOf(
                    "Controls feel closer to 36px than 44px.",
                    "Dropdown content is a popover, not a bare button stack.",
                    "Cards sit close to the page background with restrained contrast."
                )
            )
            spacer(UiModifier().height(8f.dp))
            awakeShadcnBadge("TARGET", variant = AwakeShadcnBadgeVariant.Outline)
        }
        panel(
            id = "ui-showcase-reference-awake",
            width = Dimension.Fixed(cardWidth.dp),
            height = Dimension.Fixed(284f.dp),
            style = theme.components.panel then Style { shape(14f.dp) }
        ) {
            awakeShadcnSectionTitle("Awake now")
            awakeShadcnSupportingText("Our current implementation after the sizing and popover pass.")
            spacer(UiModifier().height(8f.dp))
            awakeShadcnSupportingText("Typography is tighter, menu surfaces are contained, and the gray slab effect is reduced.", maxLines = 4)
            spacer(UiModifier().height(8f.dp))
            row(height = 36f.dp, gap = 8f) {
                awakeShadcnButton("reference-primary", "Primary", modifier = UiModifier().width(100f.dp).height(36f.dp), variant = AwakeShadcnButtonVariant.Primary)
                awakeShadcnButton("reference-outline", "Outline", modifier = UiModifier().width(96f.dp).height(36f.dp), variant = AwakeShadcnButtonVariant.Outline)
            }
            spacer(UiModifier().height(8f.dp))
            awakeShadcnBadge("AWAKE", variant = AwakeShadcnBadgeVariant.Primary)
        }
    }
}

private fun UiColumnDslScope.drawUiShowcaseButtonsPreview() {
    awakeShadcnSectionTitle("Buttons")
    row(height = 36f.dp, gap = 10f) {
        awakeShadcnButton("showcase-primary", "Primary", modifier = UiModifier().width(120f.dp).height(36f.dp), variant = AwakeShadcnButtonVariant.Primary)
        awakeShadcnButton("showcase-secondary", "Secondary", modifier = UiModifier().width(120f.dp).height(36f.dp), variant = AwakeShadcnButtonVariant.Secondary)
    }
    row(height = 36f.dp, gap = 10f) {
        awakeShadcnButton("showcase-outline", "Outline", modifier = UiModifier().width(112f.dp).height(36f.dp), variant = AwakeShadcnButtonVariant.Outline)
        awakeShadcnButton("showcase-ghost", "Ghost", modifier = UiModifier().width(100f.dp).height(36f.dp), variant = AwakeShadcnButtonVariant.Ghost)
        awakeShadcnButton("showcase-danger", "Danger", modifier = UiModifier().width(104f.dp).height(36f.dp), variant = AwakeShadcnButtonVariant.Danger)
    }
    spacer(UiModifier().height(10f.dp))
    awakeShadcnSectionTitle("Badges")
    row(height = 34f.dp, gap = 8f) {
        awakeShadcnBadge("LIVE", variant = AwakeShadcnBadgeVariant.Primary)
        awakeShadcnBadge("SCENE", variant = AwakeShadcnBadgeVariant.Secondary)
        awakeShadcnBadge("BETA", variant = AwakeShadcnBadgeVariant.Outline)
        awakeShadcnBadge("RISK", variant = AwakeShadcnBadgeVariant.Danger)
    }
}

private fun UiColumnDslScope.drawUiShowcaseFontsPreview() {
    awakeShadcnSectionHeader(
        title = "Font pipeline comparison",
        description = "The same specimen rendered through each Awake UI font path so we can judge edge quality and spacing directly."
    )
    spacer(UiModifier().height(8f.dp))
    row(height = 292f.dp, gap = 12f) {
        panel(
            id = "showcase-font-bitmap",
            width = Dimension.Fixed(240f.dp),
            height = Dimension.Fixed(292f.dp),
            style = Style { shape(14f.dp) }
        ) { slot ->
            drawUiShowcaseFontSpecimen(
                slot = slot,
                label = "Bitmap",
                detail = "Coverage-alpha atlas from the original grid source.",
                previewFont = BitmapFont()
            )
        }
        panel(
            id = "showcase-font-truesans",
            width = Dimension.Fixed(240f.dp),
            height = Dimension.Fixed(292f.dp),
            style = Style { shape(14f.dp) }
        ) { slot ->
            drawUiShowcaseFontSpecimen(
                slot = slot,
                label = "True Font",
                detail = "Real Roboto glyph atlas baked from a TTF source with proportional quad metrics.",
                previewFont = UiFonts.trueSans()
            )
        }
    }
    spacer(UiModifier().height(8f.dp))
    supportingLines(
        listOf(
            "Bitmap stays closer to the authored pixel grid and remains useful for low-fi or debug surfaces.",
            "True Font uses real outline-derived glyphs, so spacing and letterforms stop fighting the renderer."
        )
    )
}

private fun UiColumnDslScope.drawUiShowcaseLayoutPreview() {
    awakeShadcnSectionTitle("Row")
    awakeShadcnSupportingText("row(...) advances a cursor along the horizontal axis; each child claims the next slot in call order.")
    spacer(UiModifier().height(8f.dp))
    row(height = 48f.dp, gap = 8f) {
        panel(id = "layout-row-a", width = Dimension.Fixed(80f.dp), height = Dimension.FillMax, style = Style { background(theme.tokens.primary) }) { }
        panel(id = "layout-row-b", width = Dimension.Fixed(120f.dp), height = Dimension.FillMax, style = Style { background(theme.tokens.secondary) }) { }
        panel(id = "layout-row-c", width = Dimension.Fixed(160f.dp), height = Dimension.FillMax, style = Style { background(theme.tokens.muted) }) { }
    }
    spacer(UiModifier().height(16f.dp))
    awakeShadcnSectionTitle("Column")
    awakeShadcnSupportingText("column(...) advances a cursor along the vertical axis -- the default layout for every page in this catalog.")
    spacer(UiModifier().height(8f.dp))
    column(height = Dimension.Fixed(112f.dp), width = Dimension.Fixed(200f.dp), gap = 6f) {
        panel(id = "layout-col-a", width = Dimension.FillMax, height = Dimension.Fixed(28f.dp), style = Style { background(theme.tokens.primary) }) { }
        panel(id = "layout-col-b", width = Dimension.FillMax, height = Dimension.Fixed(28f.dp), style = Style { background(theme.tokens.secondary) }) { }
        panel(id = "layout-col-c", width = Dimension.FillMax, height = Dimension.Fixed(28f.dp), style = Style { background(theme.tokens.muted) }) { }
    }
}

private fun UiColumnDslScope.drawUiShowcaseSlotApiPreview() {
    awakeShadcnSectionTitle("buttonSlot(...) content lambda")
    awakeShadcnSupportingText("The label-string overload is sugar over this content-lambda form -- there is no capability gap between them.")
    spacer(UiModifier().height(8f.dp))
    buttonSlot(
        id = "slot-api-launch",
        modifier = UiModifier().width(180f.dp).height(40f.dp),
        style = theme.components.button
    ) {
        text(">", modifier = UiModifier().offset(x = 12f.dp).width(16f.dp))
        text("Launch", modifier = UiModifier().offset(x = 32f.dp))
    }
    spacer(UiModifier().height(16f.dp))
    awakeShadcnSectionTitle("Custom widgets, same primitives")
    awakeShadcnSupportingText("samples:hello-cube's Gauge.kt is a fully custom widget built from the same claimSlot()/emit() primitives a built-in widget uses -- no library-only capability gap.")
}

private fun UiColumnDslScope.drawUiShowcaseFontSpecimen(
    slot: UiSlot,
    label: String,
    detail: String,
    previewFont: UiFont,
) {
    val specimenScope = context.column(
        slot = slot,
        font = previewFont,
        theme = theme,
        gap = 8f,
        insets = UiInsets(16f.dp)
    )
    specimenScope.awakeShadcnBadge(
        label.uppercase(),
        modifier = UiModifier().width(84f.dp).height(28f.dp),
        variant = AwakeShadcnBadgeVariant.Outline
    )
    specimenScope.text(
        label = "Awake UI",
        color = theme.tokens.foreground,
        textSize = 18f.sp
    )
    specimenScope.text(
        label = "Sphinx 123",
        color = theme.tokens.foreground,
        textSize = 16f.sp
    )
    specimenScope.text(
        label = "THE QUICK BROWN FOX",
        color = theme.tokens.foreground,
        textSize = 12f.sp
    )
    specimenScope.text(
        label = detail,
        color = theme.tokens.mutedForeground,
        wrap = UiTextWrap.Word,
        overflow = UiTextOverflow.Ellipsis,
        maxLines = 3,
        textSize = 11f.sp
    )
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
        id = "showcase-theme-mode",
        label = "Theme",
        options = ShowcaseThemeModeOptions,
        selectedIndex = state.showcaseThemeModeIndex,
        labelWidth = 72f.dp
    )?.let { state.showcaseThemeModeIndex = it }
    awakeShadcnPropertyDropdown(
        id = "showcase-accent",
        label = "Accent",
        options = ShowcaseAccentOptions,
        selectedIndex = state.showcaseAccentIndex,
        labelWidth = 72f.dp
    )?.let { state.showcaseAccentIndex = it }
    awakeShadcnSupportingText(
        "Auto resolves to ${if (state.showcaseResolvedDarkMode()) "dark" else "light"} on this platform.",
        maxLines = 2
    )

    spacer(UiModifier().height(10f.dp))
    awakeShadcnSectionTitle("Live preview")
    val nextLive = awakeShadcnPropertyToggle(
        id = "showcase-live",
        label = "Live badge",
        checked = state.showcaseLiveBadge
    )
    if (nextLive != state.showcaseLiveBadge) state.showcaseLiveBadge = nextLive

    val nextDanger = awakeShadcnPropertyToggle(
        id = "showcase-danger-mode",
        label = "Danger mode",
        checked = state.showcaseDangerMode
    )
    if (nextDanger != state.showcaseDangerMode) state.showcaseDangerMode = nextDanger

    awakeShadcnPropertyDropdown(
        id = "showcase-badge-variant",
        label = "Badge",
        options = ShowcaseBadgeOptions,
        selectedIndex = state.showcaseBadgeVariantIndex
    )?.let { state.showcaseBadgeVariantIndex = it }

    state.showcaseSurfaceRadius = awakeShadcnPropertySlider(
        id = "showcase-radius",
        label = "Radius",
        min = 8f,
        max = 24f,
        value = state.showcaseSurfaceRadius
    )

    spacer(UiModifier().height(10f.dp))
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
    ) { previewSlot ->
        val shimmerForward = context.rememberBooleanState("showcase-preview-shimmer-direction", initial = true)
        val shimmerTarget = when {
            !state.showcaseLiveBadge -> 0f
            shimmerForward.value -> 1f
            else -> 0f
        }
        val shimmerPhase = context.animateFloat(
            id = "showcase-preview-shimmer",
            target = shimmerTarget,
            initial = 0f,
            responsiveness = 2.5f,
            snapDistance = 0.015f
        )
        if (state.showcaseLiveBadge) {
            if (shimmerForward.value && shimmerPhase >= 0.98f) shimmerForward.value = false
            if (!shimmerForward.value && shimmerPhase <= 0.02f) shimmerForward.value = true
        } else {
            shimmerForward.value = true
        }
        drawShowcaseGradientChrome(
            slot = previewSlot,
            shimmerPhase = shimmerPhase,
            dangerMode = state.showcaseDangerMode
        )
        val badgeVariant = state.showcaseBadgeVariant()
        awakeShadcnBadge(if (state.showcaseLiveBadge) "LIVE" else "PAUSED", variant = badgeVariant)
        row(height = 28f.dp, gap = 8f) {
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
        awakeShadcnSupportingText("Light is the default mood now, Auto follows the platform, and the sample chrome can carry gradients and shimmer without hardcoding per-demo paint.")
        spacer(UiModifier().height(6f.dp))
        row(height = 36f.dp, gap = 10f) {
            if (
                awakeShadcnButton(
                    id = "preview-primary-action",
                    label = "Inspect",
                    modifier = UiModifier().width(112f.dp).height(36f.dp),
                    variant = AwakeShadcnButtonVariant.Primary
                )
            ) {
                state.showcasePrimaryClicks += 1
            }
            awakeShadcnButton(
                id = "preview-secondary-action",
                label = if (state.showcaseDangerMode) "Rollback" else "Publish",
                modifier = UiModifier().width(120f.dp).height(36f.dp),
                variant = if (state.showcaseDangerMode) AwakeShadcnButtonVariant.Danger else AwakeShadcnButtonVariant.Outline
            )
        }
        awakeShadcnBodyText("Primary clicks: ${state.showcasePrimaryClicks}")
    }
}

private fun UiColumnDslScope.drawShowcaseGradientChrome(
    slot: UiSlot,
    shimmerPhase: Float,
    dangerMode: Boolean,
) {
    val themeGradient = UiLinearGradient.horizontal(
        start = lerpColor(theme.tokens.primary.withAlpha(0.12f), theme.tokens.accent.withAlpha(0.18f), shimmerPhase),
        end = lerpColor(theme.tokens.accent.withAlpha(0.22f), theme.tokens.secondary.withAlpha(0.12f), shimmerPhase)
    )
    val borderGradient = UiLinearGradient.horizontal(
        start = if (dangerMode) theme.tokens.destructive.withAlpha(0.92f) else theme.tokens.primary.withAlpha(0.64f),
        end = if (dangerMode) theme.tokens.accent.withAlpha(0.82f) else theme.tokens.accent.withAlpha(0.84f)
    )
    val shimmerWidth = (slot.width * 0.28f).coerceAtLeast(52f)
    val shimmerX = slot.x + (slot.width - shimmerWidth) * shimmerPhase.coerceIn(0f, 1f)
    context.absolute(slot.x, slot.y, font = font, theme = theme, overlayOnly = true).apply {
        gradientBorder(slot, width = 1f.dp, gradient = borderGradient, overlay = true)
        gradientRect(
            UiSlot(slot.x, slot.y, slot.width, 44f.coerceAtMost(slot.height)),
            gradient = themeGradient,
            overlay = true
        )
        gradientRect(
            UiSlot(shimmerX, slot.y + 1f, shimmerWidth, (slot.height - 2f).coerceAtLeast(0f)),
            gradient = UiLinearGradient.horizontal(
                start = Color.Transparent,
                end = theme.tokens.foreground.withAlpha(if (dangerMode) 0.08f else 0.12f)
            ),
            overlay = true
        )
    }
}

private fun lerpColor(start: Color, end: Color, fraction: Float): Color = Color(
    r = start.r + (end.r - start.r) * fraction,
    g = start.g + (end.g - start.g) * fraction,
    b = start.b + (end.b - start.b) * fraction,
    a = start.a + (end.a - start.a) * fraction
)

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
    spacer(UiModifier().height(6f.dp))
    row(height = 36f.dp, gap = 10f) {
        if (
            awakeShadcnButton(
                id = "counter-decrement",
                label = "Decrement",
                modifier = UiModifier().width(112f.dp).height(36f.dp),
                variant = AwakeShadcnButtonVariant.Outline
            )
        ) {
            state.counterStore.dispatch(UiShowcaseCounterContract.Intent.Decrement)
        }
        if (
            awakeShadcnButton(
                id = "counter-increment",
                label = "Increment",
                modifier = UiModifier().width(112f.dp).height(36f.dp),
                variant = AwakeShadcnButtonVariant.Primary
            )
        ) {
            state.counterStore.dispatch(UiShowcaseCounterContract.Intent.Increment)
        }
    }
    row(height = 36f.dp, gap = 10f) {
        if (
            awakeShadcnButton(
                id = "counter-reset",
                label = "Reset",
                modifier = UiModifier().width(112f.dp).height(36f.dp),
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
    spacer(UiModifier().height(6f.dp))
    row(height = 36f.dp, gap = 10f) {
        val menuTrigger = buttonSlot(
            id = "ui-showcase-menu-trigger",
            label = "Actions",
            modifier = UiModifier().width(112f.dp).height(36f.dp),
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
                label = "Open Dialog",
                modifier = UiModifier().width(128f.dp).height(36f.dp),
                variant = AwakeShadcnButtonVariant.Outline
            )
        ) {
            deleteDialogState.open()
        }
    }
    spacer(UiModifier().height(4f.dp))
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
