// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseThemeMode
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
    ShowcasePage(
        id = "introduction",
        title = "Introduction",
        category = ShowcaseCategory.GettingStarted,
        description = "A dedicated catalog sample for owned Awake UI components instead of a one-off demo page.",
        usageCode = """
            gameUi {
                theme(shadcnTheme())
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
            shadcnButton(
                id = "primary",
                label = "Primary",
                modifier = Modifier.width(120f.dp).height(36f.dp)
            )
            shadcnSelect(
                id = "style",
                options = options,
                selectedIndex = 0,
                modifier = Modifier.width(128f.dp)
            )
            shadcnCard("card") { ... }
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
            val theme = shadcnTheme(
                preset = ShadcnStylePreset.Vega,
                baseColor = ShadcnBaseColor.Neutral,
                accent = ShadcnAccent.Base,
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
        id = "typography",
        title = "Typography",
        category = ShowcaseCategory.Typography,
        description = "The shadcn text component family: sectionTitle, headline, bodyText, supportingText, generic text, and label.",
        usageCode = """
            shadcnSectionTitle("Section Title")
            shadcnHeadline("Headline text sets the tone for a page or panel.")
            shadcnBodyText("Body text is the default reading size for paragraphs.")
            shadcnSupportingText("Supporting text is the muted caption size.")
            shadcnText("Generic shadcn text.", muted = true)
            shadcnLabel("Email", required = true)
        """.trimIndent(),
        notes = listOf(
            "These are the actual owned typography components, not a font-rendering-path comparison.",
            "shadcnLabel's required/disabled states show up here in isolation -- shadcnFieldLabel (used throughout Text Input and Checkout Form) is sugar over the default-state case of the same function.",
            "See the Font Atlas page for bitmap-vs-TTF glyph quality instead."
        ),
        renderPreview = { drawUiShowcaseTypographySpecimenPreview() }
    ),
    ShowcasePage(
        id = "fonts",
        title = "Font Atlas",
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
        category = ShowcaseCategory.Patterns,
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
        id = "canvas",
        title = "Canvas",
        category = ShowcaseCategory.Patterns,
        description = "A bounded drawing surface for custom shapes, gradients, text, clipping, and nested authored drawing.",
        usageCode = """
            surface(id = "canvas-card", width = Dimension.Fixed(420f.dp), height = Dimension.Fixed(220f.dp)) { slot ->
                canvas(slot) {
                    drawGradientRect(0f, 0f, bounds.width, 56f, headerGradient)
                    drawRoundRect(16f, 76f, 152f, 104f, color = theme.tokens.background, radius = 16f.dp)
                    drawLine(184f, 90f, 332f, 156f, color = theme.tokens.primary)
                    drawText("Canvas", x = 24f, y = 22f)
                    clipShape(UiShapeSpec.Circle, x = 292f, y = 88f, width = 80f, height = 80f) {
                        drawGradientRect(0f, 0f, 80f, 80f, badgeGradient)
                    }
                }
            }
        """.trimIndent(),
        notes = listOf(
            "Canvas is for authored drawing inside a resolved slot, not a replacement for layout.",
            "The API stays local-coordinate based so a nested canvas composes cleanly inside cards, HUDs, and docs previews."
        ),
        renderPreview = { drawUiShowcaseCanvasPreview() }
    ),
    ShowcasePage(
        id = "slot-apis",
        title = "Slot APIs",
        category = ShowcaseCategory.Patterns,
        description = "buttonSlot(...)'s content-lambda form composes arbitrary content inside a widget's own claimed slot instead of a fixed label string.",
        usageCode = """
            buttonSlot(id = "launch", modifier = Modifier.width(180f.dp).height(40f.dp)) {
                val labelSize = Style { textSize(theme.typography.label) }
                text(">", modifier = Modifier.offset(x = 12f.dp).width(16f.dp), style = labelSize)
                text("Launch", modifier = Modifier.offset(x = 32f.dp), style = labelSize)
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
            shadcnButton(
                id = "save",
                label = "Save",
                modifier = Modifier.width(120f.dp).height(36f.dp),
                variant = ShadcnButtonVariant.Primary
            )
            shadcnBadge("LIVE", variant = ShadcnBadgeVariant.Secondary)
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
            name = shadcnInput(
                id = "name",
                value = name,
                placeholder = "Jane Doe",
                modifier = Modifier.width(240f.dp).height(36f.dp)
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
            exposure = shadcnSlider(
                id = "exposure",
                min = 0f,
                max = 100f,
                value = exposure,
                label = "Exposure ${'$'}{exposure.toInt()}%",
                modifier = Modifier.width(360f.dp).height(32f.dp)
            )
        """.trimIndent(),
        notes = listOf(
            "The dedicated preview matrix captures low, mid, and max states side by side.",
            "This page exists so the same widget is also documented as a reusable component, not just tested as a hidden proof surface."
        ),
        renderPreview = { drawUiShowcaseSliderPreview() }
    ),
    ShowcasePage(
        id = "range-slider",
        title = "Range Slider",
        category = ShowcaseCategory.Inputs,
        description = "Dual-thumb variant of Slider -- two independently draggable knobs on one track, with the fill spanning only between them.",
        usageCode = """
            var temperature by rememberStateValue("scene", "temperature") { 0.3f to 0.7f }
            temperature = shadcnFieldRangeSlider(
                id = "temperature",
                label = "Temperature",
                min = 0f,
                max = 1f,
                valueStart = temperature.first,
                valueEnd = temperature.second,
                modifier = Modifier.width(360f.dp)
            )
        """.trimIndent(),
        notes = listOf(
            "Dragging one thumb past the other clamps instead of crossing -- a small minimum gap between start and end is enforced.",
            "shadcnFieldRangeSlider is the label-above/value-above layout shadcn's own range demo uses; the bare shadcnRangeSlider control has no label row of its own since two knobs have no single 'beside the knob' spot."
        ),
        renderPreview = { drawUiShowcaseRangeSliderPreview() }
    ),
    ShowcasePage(
        id = "selection",
        title = "Selection Controls",
        category = ShowcaseCategory.Inputs,
        description = "The Awake-owned selection family: Toggle, Switch, Checkbox, and RadioGroup, all sharing the same shadcn token layer.",
        usageCode = """
            var wireframe by rememberStateValue("scene", "wireframe") { true }
            wireframe = shadcnToggle(id = "wireframe", checked = wireframe, label = "Wireframe overlay")

            var darkMode by rememberStateValue("scene", "darkMode") { false }
            darkMode = shadcnSwitch(id = "dark-mode", checked = darkMode, label = "Dark mode")

            var quality by rememberStateValue("scene", "quality") { 1 }
            quality = shadcnRadioGroup(id = "quality", options = listOf("Low", "Medium", "High"), selectedIndex = quality)

            var alignment by rememberStateValue("scene", "alignment") { 1 }
            alignment = shadcnToggleGroup(id = "alignment", options = listOf("Left", "Center", "Right", "Justify"), selectedIndex = alignment)
        """.trimIndent(),
        notes = listOf(
            "shadcnSwitch is real shadcn's `Switch`; shadcnToggle is our own boolean pressable control -- both exist and both get a row here.",
            "shadcnRadioGroup reuses checkbox() with a Circle shape for single-select semantics.",
            "shadcnToggleGroup delegates entirely to toggleGroup() (ui-unstyled); each option now gets an equal weight(1f) share of the group width (previously the first option's FillMax claimed the whole row, leaving every option after it zero-width -- fixed).",
            "Their checked/unchecked and selected-index states are also covered in the widget preview lanes."
        ),
        renderPreview = { drawUiShowcaseSelectionPreview() }
    ),
    ShowcasePage(
        id = "tabs",
        title = "Tabs",
        category = ShowcaseCategory.Layout,
        description = "A muted track with a raised active tab, composed from shadcnButton the same way shadcnRadioGroup composes from checkbox().",
        usageCode = """
            var section by rememberStateValue("scene", "section") { 0 }
            section = shadcnTabs(
                id = "section",
                tabs = listOf("Account", "Password", "Team"),
                selectedIndex = section
            )
        """.trimIndent(),
        notes = listOf(
            "The active tab uses ShadcnButtonVariant.Primary with its background/foreground overridden to the card color; inactive tabs stay Ghost for the real chromeless-until-hover look.",
            "No variant axis on real shadcn's Tabs either -- single look, single-select state is the whole story."
        ),
        renderPreview = { drawUiShowcaseTabsPreview() }
    ),
    ShowcasePage(
        id = "select",
        title = "Select",
        category = ShowcaseCategory.Inputs,
        description = "A non-searchable dropdown trigger -- matches real shadcn's plain Select, not a searchable Combobox.",
        usageCode = """
            var theme by rememberStateValue("scene", "theme") { 0 }
            theme = shadcnSelect(
                id = "theme",
                options = listOf("Light", "Dark", "Auto"),
                selectedIndex = theme,
                modifier = Modifier.width(200f.dp)
            ) ?: theme
        """.trimIndent(),
        notes = listOf(
            "shadcnSelect already appears composed inside the Checkout Form and the Field State Matrix preview -- this page is its own first-class home in isolation.",
            "The Dropdown Menu And Dialog page's open-state preview demonstrates the same underlying popover anchoring shadcnSelect builds on."
        ),
        renderPreview = { drawUiShowcaseSelectPreview() }
    ),
    ShowcasePage(
        id = "kbd-separator",
        title = "Kbd And Separator",
        category = ShowcaseCategory.Layout,
        description = "Two tiny presentational primitives with no variant or state axis, grouped on one page instead of two near-empty ones.",
        usageCode = """
            shadcnKbd("Cmd")
            shadcnKbd("Shift")
            shadcnKbd("P")

            shadcnSeparator(modifier = Modifier.width(320f.dp))
        """.trimIndent(),
        notes = listOf(
            "shadcnKbd shares its 'measure text, draw a box, draw the label' mechanics with shadcnBadge -- just a different (sm-radius, muted) style.",
            "shadcnSeparator is separator() with shadcn's border token -- no shadcn-specific style axis beyond that."
        ),
        renderPreview = { drawUiShowcaseKbdSeparatorPreview() }
    ),
    ShowcasePage(
        id = "feedback",
        title = "Feedback",
        category = ShowcaseCategory.Layout,
        description = "Progress, Skeleton, and Spinner -- three small loading/status primitives grouped on one page since none carry a variant axis of their own.",
        usageCode = """
            shadcnProgress(id = "load", value = 0.8f, modifier = Modifier.width(320f.dp).height(8f.dp))
            shadcnSkeleton(id = "row", modifier = Modifier.width(160f.dp).height(20f.dp))
            shadcnSpinner(id = "spinner", modifier = Modifier.width(24f.dp).height(24f.dp))
        """.trimIndent(),
        notes = listOf(
            "Skeleton has a real per-widget opacity pulse (not a static box); Spinner is a real orbiting-dots animation approximating shadcn's CSS-rotated Lucide icon.",
            "See the Shimmer page for the other loading-state animation -- a moving highlight sweep rather than a pulse or rotation."
        ),
        renderPreview = { drawUiShowcaseFeedbackPreview() }
    ),
    ShowcasePage(
        id = "avatar",
        title = "Avatar",
        category = ShowcaseCategory.Inputs,
        description = "A circular fallback avatar -- initials text via the convenience overload, or arbitrary caller-supplied content (e.g. an icon) via the slot-based primary overload.",
        usageCode = """
            shadcnAvatar("RV", modifier = Modifier.width(40f.dp).height(40f.dp))
            shadcnAvatar(modifier = Modifier.width(40f.dp).height(40f.dp)) { slot ->
                icon(UiIcons.chevronDown, modifier = Modifier.align(UiAlignment.Center))
            }
        """.trimIndent(),
        notes = listOf(
            "Awake has no image-loading pipeline wired in yet, so this is the fallback-only look -- a real gap, not a faked one.",
            "The initials overload is sugar over the slot-based primary overload; there is no capability gap between them."
        ),
        renderPreview = { drawUiShowcaseAvatarPreview() }
    ),
    ShowcasePage(
        id = "breadcrumb",
        title = "Breadcrumb",
        category = ShowcaseCategory.Layout,
        description = "A trail of muted links separated by a glyph, with the last item rendered as plain current-page text.",
        usageCode = """
            shadcnBreadcrumb(listOf("Scenes", "Lighting", "Exposure"))
        """.trimIndent(),
        notes = listOf(
            "No click/navigation wiring -- that's caller-owned routing, same as every other Awake nav element.",
            "The string-list overload is sugar over a content-lambda overload for custom trail items."
        ),
        renderPreview = { drawUiShowcaseBreadcrumbPreview() }
    ),
    ShowcasePage(
        id = "card",
        title = "Card",
        category = ShowcaseCategory.Layout,
        description = "A header/body/footer surface composition -- both slots are optional and separated from the body by the same divider convention as DropdownMenu's item separator.",
        usageCode = """
            shadcnCard(
                id = "card",
                header = { shadcnSectionTitle("Title") },
                footer = { shadcnButton("save", label = "Save") }
            ) { shadcnBodyText("Body content.") }
        """.trimIndent(),
        notes = listOf(
            "Header and footer are independently optional -- a body-only card is just shadcnCard with neither slot filled.",
            "Uses the base theme surface style directly; it isn't a ShadcnSurfaceVariant flavor like shadcnSurface's Card variant used to be."
        ),
        renderPreview = { drawUiShowcaseCardPreview() }
    ),
    ShowcasePage(
        id = "sidebar",
        title = "Sidebar",
        category = ShowcaseCategory.Layout,
        description = "A fixed-width navigation shell -- optional header/footer slots around a required nav-item content area, sharing the same divider convention as Card.",
        usageCode = """
            shadcnSidebar(
                id = "nav",
                modifier = Modifier.width(220f.dp).height(Dimension.WrapContent),
                header = { shadcnBadge("STARTER", variant = ShadcnBadgeVariant.Primary) },
                footer = { shadcnButton("sign-out", label = "Sign out") }
            ) { _ ->
                shadcnButton("scene-lighting", label = "Lighting", variant = ShadcnButtonVariant.Primary)
                shadcnButton("scene-camera", label = "Camera", variant = ShadcnButtonVariant.Secondary)
            }
        """.trimIndent(),
        notes = listOf(
            "Header and footer are each independently optional, same as shadcnCard -- a content-only sidebar is just shadcnSidebar with neither slot filled.",
            "Real usage (see StarterGameUi.kt, UiShowcaseUi.kt) gives it a fixed width; letting it stretch full-width reads wrong for a nav rail."
        ),
        renderPreview = { drawUiShowcaseSidebarPreview() }
    ),
    ShowcasePage(
        id = "alert",
        title = "Alert",
        category = ShowcaseCategory.Overlays,
        description = "A static inline banner for default and destructive status messages -- not a modal.",
        usageCode = """
            shadcnAlert(
                id = "saved",
                title = "Scene saved",
                description = "Your changes are stored in the current project."
            )
        """.trimIndent(),
        notes = listOf(
            "No dismiss/action slot yet -- shadcn's own Alert doesn't have one either; that's composed alongside it by the caller.",
            "See the Dropdown Menu And Dialog page for overlay/modal surfaces instead."
        ),
        renderPreview = { drawUiShowcaseAlertPreview() }
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
                modifier = Modifier.width(112f.dp).height(36f.dp)
            )
            val result = shadcnDropdownMenu(
                id = "actions.menu",
                anchorSlot = trigger.slot,
                expanded = popupState.expanded,
                items = ShowcaseActionMenuItems
            )
            shadcnAlertDialog("delete", expanded = dialogState.expanded, title = "Delete scene?")
            shadcnDialog(
                id = "info",
                expanded = infoDialogState.expanded,
                header = { text("Scene info") }
            ) { _ -> shadcnBodyText("Freeform content, unlike AlertDialog's fixed title/message shape.") }
        """.trimIndent(),
        notes = listOf(
            "The dropdown now renders inside a popover container with padding and grouped rows.",
            "shadcnDialog is the plain content-based dialog shadcnAlertDialog is itself composed from -- this page now shows both, not just the alert-dialog wrapper.",
            "This page is the easiest way to spot overlay spacing regressions."
        ),
        renderPreview = { drawUiShowcasePopupPreview() }
    ),
    ShowcasePage(
        id = "popover",
        title = "Popover",
        category = ShowcaseCategory.Overlays,
        description = "A trigger-anchored floating panel for freeform content -- not a fixed list of menu rows like the dropdown menu.",
        usageCode = """
            val trigger = buttonSlot(
                id = "share",
                label = "Share",
                modifier = Modifier.width(120f.dp).height(36f.dp)
            )
            val popoverResult = shadcnPopover(
                id = "share.popover",
                anchorSlot = trigger.slot.toBounds(),
                expanded = popoverState.expanded,
                width = Dimension.Fixed(280f.dp)
            ) {
                text("Share scene", wrap = UiTextWrap.Word, maxLines = 1)
                shadcnSupportingText("Anyone with the link can view this scene until you revoke it.")
            }
        """.trimIndent(),
        notes = listOf(
            "The caller owns the trigger widget and expanded state, same split as shadcnDropdownMenu -- shadcnPopover only owns anchored positioning, dismiss, and panel chrome.",
            "Content is freeform (any ColumnScope body), unlike the dropdown menu's fixed list of UiDropdownMenuItem rows."
        ),
        renderPreview = { drawUiShowcasePopoverPreview() }
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
                modifier = Modifier.width(132f.dp).height(36f.dp),
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
            shadcnScrollSurface(
                id = "inspector",
                width = Dimension.Fixed(420f.dp),
                height = Dimension.Fixed(176f.dp),
                state = scrollState.value
            ) { _ ->
                repeat(10) { index ->
                    shadcnButton(
                        id = "row-${'$'}index",
                        label = "Inspector row ${'$'}{index + 1}",
                        modifier = Modifier.width(360f.dp).height(32f.dp)
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
            shadcnCollapsible(
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
    ),
    ShowcasePage(
        id = "shimmer",
        title = "Shimmer",
        category = ShowcaseCategory.Animations,
        description = "A sweeping highlight animation for loading states.",
        usageCode = """
            shadcnText(
                label = "Generating response...",
                muted = true,
                modifier = Modifier.shadcnShimmer()
            )
        """.trimIndent(),
        notes = listOf(
            "Uses a moving clip rect to draw a secondary 'highlight' pass of the same text.",
            "Can be enabled/disabled via the modifier property."
        ),
        renderPreview = { _ -> drawUiShowcaseShimmerPreview() }
    ),
    ShowcasePage(
        id = "easing",
        title = "Easing",
        category = ShowcaseCategory.Animations,
        description = "Fixed-duration tweens shaped by an Easing curve, distinct from the spring-style animateFloat above.",
        usageCode = """
            val fraction = animateFloatTween(
                id = "panel-reveal",
                target = 1f,
                durationMs = 1200f,
                easing = EaseInOut
            )
        """.trimIndent(),
        notes = listOf(
            "LinearEasing, EaseIn, EaseOut, and EaseInOut mirror CSS's transition-timing-function presets.",
            "Retargeting mid-tween restarts the duration from the current animated value instead of snapping back to the start.",
            "Each row's thumbnail samples the same Easing.transform used to drive the moving thumb, so the shape and the motion always agree."
        ),
        renderPreview = { _ -> drawUiShowcaseEasingPreview() }
    ),
    ShowcasePage(
        id = "fade-visibility",
        title = "Fade Visibility",
        category = ShowcaseCategory.Animations,
        description = "Real alpha compositing for graphicsLayer -- fades a subtree in/out instead of instantly unmounting it.",
        usageCode = """
            animatedVisibility(
                id = "panel",
                visible = visible,
                durationMs = 300f,
                easing = EaseInOut
            ) {
                shadcnSurface(id = "panel-surface") { text("Fading in and out") }
            }
        """.trimIndent(),
        notes = listOf(
            "Alpha-only -- no rotation/scale, unlike Compose's full AnimatedVisibility.",
            "Content keeps rendering (dimmed) through the exit fade instead of unmounting the frame visible flips false.",
            "Built on graphicsLayer's UiAlphaEffect, applied once at UiContext's own primitive-emission choke point -- no per-widget changes needed.",
            "popup()'s own show/hide now fades the same way -- see the Dropdown/Popover pages."
        ),
        renderPreview = { _ -> drawUiShowcaseFadeVisibilityPreview() }
    ),
    ShowcasePage(
        id = "field-demo",
        title = "Checkout Form",
        category = ShowcaseCategory.Patterns,
        description = "A real-world composed pattern: shadcn/ui's Payment Method checkout form, built from shadcnFieldSet/shadcnFieldLegend and the rest of the Field family.",
        usageCode = """
            shadcnFieldGroup {
                shadcnFieldSet(id = "checkout-payment") {
                    shadcnFieldLegend("Payment Method")
                    shadcnFieldDescription("All transactions are secure and encrypted")
                    shadcnFieldGroup {
                        shadcnField(id = "checkout-name-on-card") {
                            shadcnFieldLabel("Name on Card")
                            shadcnInput(id = "...", value = nameOnCard, placeholder = "Evil Rabbit")
                        }
                        row(horizontalArrangement = Arrangement.spacedBy(16f.dp)) {
                            column(modifier = Modifier.weight(1f)) {
                                shadcnField(id = "checkout-month") { ... }
                            }
                            // ...Year, CVV
                        }
                    }
                }
                shadcnFieldSeparator()
                shadcnFieldSet(id = "checkout-billing") { ... }
            }
        """.trimIndent(),
        notes = listOf(
            "shadcnFieldSet/shadcnFieldLegend are new -- a titled section wrapper and its title, one step up in weight from shadcnFieldLabel.",
            "The Month/Year/CVV row uses row() + weight(1f) columns, the same pattern CSS's grid-cols-3 gap-4 maps to.",
            "No border/background chrome on shadcnFieldSet, matching real shadcn's CSS reset of the native <fieldset> border."
        ),
        renderPreview = { state -> drawUiShowcaseFieldDemoPreview(state) }
    )
)

internal val ShowcasePagesByCategory = ShowcasePages.groupBy { it.category }

internal fun showcasePageById(pageId: String): ShowcasePage =
    ShowcasePages.firstOrNull { it.id == pageId } ?: ShowcasePages.first()
