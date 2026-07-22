// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseThemeMode
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnAccent
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnBaseColor
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnStylePreset

internal typealias ShowcasePreviewRenderer = ColumnScope.(UiShowcaseRuntimeState) -> Unit

internal val ShowcaseStyleOptions = AwakeShadcnStylePreset.entries.map { it.label }
internal val ShowcaseBaseColorOptions = AwakeShadcnBaseColor.entries.map { it.label }
internal val ShowcaseAccentOptions = AwakeShadcnAccent.entries.map { it.label }
internal val ShowcaseThemeModeOptions = UiShowcaseThemeMode.entries.map { it.label }
internal val ShowcaseBadgeOptions = listOf("Primary", "Secondary", "Outline", "Danger")

internal enum class ShowcaseCategory(val title: String) {
    GettingStarted("Getting Started"),
    Inputs("Inputs"),
    Overlays("Overlays"),
    Layout("Layout"),
    Typography("Typography"),
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
        ),
        renderPreview = { drawUiShowcaseOverviewPreview() }
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
        ),
        renderPreview = { drawUiShowcaseReferenceComparisonPreview() }
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
        ),
        renderPreview = { state -> drawUiShowcaseControlsPreview(state) }
    ),
    ShowcasePage(
        id = "fonts",
        title = "Bitmap And True Font",
        category = ShowcaseCategory.Typography,
        description = "A direct specimen view of the current bitmap default beside a real TTF-derived atlas font.",
        usageCode = """
            val bitmap = UiFonts.bitmap()
            val trueSans = UiFonts.trueSans()

            // Render the same specimen strings with each font path.
        """.trimIndent(),
        notes = listOf(
            "Bitmap remains available as the explicit pixel fallback and debug path.",
            "The default runtime font now uses the true-font atlas so spacing and letterforms stop fighting the showcase."
        ),
        renderPreview = { drawUiShowcaseFontsPreview() }
    ),
    ShowcasePage(
        id = "layout",
        title = "Layout Primitives",
        category = ShowcaseCategory.Layout,
        description = "row(...) and column(...) -- the modifier-first layout primitives every other page in this catalog is built from.",
        usageCode = """
            row(height = 48f.dp, horizontalArrangement = Arrangement.spacedBy(8f.dp)) {
                panel(id = "a", width = Dimension.Fixed(80f.dp), height = Dimension.FillMax) { }
                panel(id = "b", width = Dimension.Fixed(120f.dp), height = Dimension.FillMax) { }
            }
            column(
                height = Dimension.Fixed(112f.dp),
                width = Dimension.Fixed(200f.dp),
                verticalArrangement = Arrangement.spacedBy(6f.dp)
            ) {
                panel(id = "a", width = Dimension.FillMax, height = Dimension.Fixed(28f.dp)) { }
            }
        """.trimIndent(),
        notes = listOf(
            "row/column advance a cursor along one axis -- each child claims the next slot in call order.",
            "Neither has a flex-grow concept: FillMax only resolves against the enclosing scope's own fixed axis (a column's configured width, not a row's advancing width)."
        ),
        renderPreview = { drawUiShowcaseLayoutPreview() }
    ),
    ShowcasePage(
        id = "slot-apis",
        title = "Slot APIs",
        category = ShowcaseCategory.Patterns,
        description = "buttonSlot(...)'s content-lambda form composes arbitrary content inside a widget's own claimed slot instead of a fixed label string.",
        usageCode = """
            buttonSlot(id = "launch", modifier = UiModifier().width(180f.dp).height(40f.dp)) {
                val labelSize = Style { textSize(theme.typography.label) }
                text(">", modifier = UiModifier().offset(x = 12f.dp).width(16f.dp), style = labelSize)
                text("Launch", modifier = UiModifier().offset(x = 32f.dp), style = labelSize)
            }
        """.trimIndent(),
        notes = listOf(
            "The label-string overload is sugar over this content-lambda form -- there is no capability gap between them.",
            "A custom widget (like this catalog's own recipes) uses this exact same slot-content pattern, not a special library-only path."
        ),
        renderPreview = { drawUiShowcaseSlotApiPreview() }
    ),
    ShowcasePage(
        id = "buttons",
        title = "Buttons And Badges",
        category = ShowcaseCategory.Inputs,
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
        ),
        renderPreview = { drawUiShowcaseButtonsPreview() }
    ),
    ShowcasePage(
        id = "text-input",
        title = "Text Input",
        category = ShowcaseCategory.Inputs,
        description = "A real, typeable single-line field -- click to focus, type, backspace, and use the arrow keys to move the cursor.",
        usageCode = """
            var name by remember { "" }
            name = awakeShadcnTextField(
                id = "name",
                value = name,
                placeholder = "Jane Doe",
                modifier = UiModifier().width(240f.dp).height(36f.dp)
            )
        """.trimIndent(),
        notes = listOf(
            "Works with a real keyboard on every platform: GLFW key polling on desktop, the IME on Android, UIKeyInput on iOS.",
            "No selection or clipboard yet -- click-to-position, type, backspace/delete, arrow keys, home/end."
        ),
        renderPreview = { state -> drawUiShowcaseTextInputPreview(state) }
    ),
    ShowcasePage(
        id = "slider",
        title = "Slider",
        category = ShowcaseCategory.Inputs,
        description = "A continuous-value control that keeps value labels, track fill, and thumb placement aligned under the same shadcn recipe.",
        usageCode = """
            var exposure by rememberStateValue("scene", "exposure") { 52f }
            exposure = awakeShadcnSlider(
                id = "exposure",
                min = 0f,
                max = 100f,
                value = exposure,
                label = "Exposure ${'$'}{exposure.toInt()}%",
                modifier = UiModifier().width(360f.dp).height(32f.dp)
            )
        """.trimIndent(),
        notes = listOf(
            "The dedicated preview matrix captures low, mid, and max states side by side.",
            "This page exists so the same widget is also documented as a reusable component, not just tested as a hidden proof surface."
        ),
        renderPreview = { drawUiShowcaseSliderPreview() }
    ),
    ShowcasePage(
        id = "selection",
        title = "Toggle And Checkbox",
        category = ShowcaseCategory.Inputs,
        description = "The current Awake-owned binary selection controls, shown as reusable pieces while radio and tabs remain future work.",
        usageCode = """
            var wireframe by rememberStateValue("scene", "wireframe") { true }
            wireframe = awakeShadcnToggle(
                id = "wireframe",
                checked = wireframe,
                label = "Wireframe overlay",
                modifier = UiModifier().width(220f.dp).height(24f.dp)
            )

            var stats by rememberStateValue("scene", "stats") { false }
            stats = awakeShadcnCheckbox(
                id = "stats",
                checked = stats,
                label = "Scene statistics",
                modifier = UiModifier().width(220f.dp).height(24f.dp)
            )
        """.trimIndent(),
        notes = listOf(
            "Selection-family parity in the task doc is broader than these two controls, but this page gives the existing components a first-class home.",
            "Their checked and unchecked states are also covered in the widget preview lanes."
        ),
        renderPreview = { drawUiShowcaseSelectionPreview() }
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
        ),
        renderPreview = { drawUiShowcasePopupPreview() }
    ),
    ShowcasePage(
        id = "tooltip",
        title = "Tooltip",
        category = ShowcaseCategory.Overlays,
        description = "A compact anchored overlay for helper copy that should wrap cleanly and stay visually attached to its trigger.",
        usageCode = """
            val trigger = buttonSlot(
                id = "scene-info",
                label = "Scene info",
                modifier = UiModifier().width(132f.dp).height(36f.dp),
                style = theme.components.button
            )
            tooltip(
                anchorSlot = trigger.slot,
                visible = true,
                width = Dimension.Fixed(260f.dp),
                positionProvider = UiPopupDefaults.dropdown(offsetY = 4f.dp)
            ) {
                text(
                    label = "Frame pacing, draw calls, and scene counters can live in a tooltip.",
                    wrap = UiTextWrap.Word,
                    overflow = UiTextOverflow.Ellipsis,
                    maxLines = 3
                )
            }
        """.trimIndent(),
        notes = listOf(
            "We keep an open-state widget preview for tooltip sizing and anchoring so this does not depend on hover automation.",
            "The page version makes the same component discoverable as part of the public sample catalog."
        ),
        renderPreview = { drawUiShowcaseTooltipPreview() }
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
        ),
        renderPreview = { state -> drawUiShowcaseCounterPreview(state) }
    ),
    ShowcasePage(
        id = "scroll-panel",
        title = "Scroll Panel",
        category = ShowcaseCategory.Layout,
        description = "A reusable clipped container that owns scrolling, thumb placement, and viewport measurement instead of pushing that work to each sample.",
        usageCode = """
            val scrollState = rememberStateValue("inspector", "scroll") { UiScrollState() }
            awakeShadcnScrollSurface(
                id = "inspector",
                width = Dimension.Fixed(420f.dp),
                height = Dimension.Fixed(176f.dp),
                state = scrollState.value
            ) { _ ->
                repeat(10) { index ->
                    awakeShadcnButton(
                        id = "row-${'$'}index",
                        label = "Inspector row ${'$'}{index + 1}",
                        modifier = UiModifier().width(360f.dp).height(32f.dp)
                    )
                }
            }
        """.trimIndent(),
        notes = listOf(
            "The widget preview report keeps a partially scrolled proof around so clipping and thumb math are validated without manual interaction.",
            "This page is the reusable API story: callers bring content, the scroll surface owns the rest."
        ),
        renderPreview = { drawUiShowcaseScrollPanelPreview() }
    ),
    ShowcasePage(
        id = "collapsible",
        title = "Collapsible",
        category = ShowcaseCategory.Layout,
        description = "An interactive disclosure panel that toggles visibility of its content with an animated height transition.",
        usageCode = """
            var expanded by remember { false }
            awakeShadcnCollapsible(
                id = "my-panel",
                title = "Show more",
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                text("The hidden content goes here.")
            }
        """.trimIndent(),
        notes = listOf(
            "Uses a ghost-variant button as the header.",
            "Lays out and animates height changes using a per-widget state-based measure pass.",
            "Hidden content costs nothing to lay out while fully collapsed."
        ),
        renderPreview = { state -> drawUiShowcaseCollapsiblePreview(state) }
    )
)

internal val ShowcasePagesByCategory = ShowcasePages.groupBy { it.category }

internal fun showcasePageById(pageId: String): ShowcasePage =
    ShowcasePages.firstOrNull { it.id == pageId } ?: ShowcasePages.first()
