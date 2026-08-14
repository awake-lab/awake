// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreview
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewEntry
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewFrame
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewMetadata
import io.github.ronjunevaldoz.awake.ui.UiDensity
import io.github.ronjunevaldoz.awake.ui.headless.UiPopupDefaults
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.context.UiFrameInput
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnInput
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSelect
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSlider
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnDropdownMenuItem
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnDropdownMenuSeparator
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnAlertDialog
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnDropdownMenu
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnTooltip
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnTooltipText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCheckbox
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnToggle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCollapsible
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnPopover
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSectionTitle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnScrollArea
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSeparator
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnMuted
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.headless.buttonSlot
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement as HeadlessArrangement
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.offset
import io.github.ronjunevaldoz.awake.ui.headless.padding
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.spacer
import io.github.ronjunevaldoz.awake.ui.headless.surface
import io.github.ronjunevaldoz.awake.ui.headless.text
import io.github.ronjunevaldoz.awake.ui.headless.verticalScroll
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnIcons
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebar
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarFooterButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarGroup
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarHeaderButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenu
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenuItem
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenuSub
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenuSubItem
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxHeight
import io.github.ronjunevaldoz.awake.ui.headless.icon
import io.github.ronjunevaldoz.awake.ui.headless.width
import io.github.ronjunevaldoz.awake.ui.headless.rememberScrollState
import io.github.ronjunevaldoz.awake.ui.headless.uiScope
import io.github.ronjunevaldoz.awake.ui.headless.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.headless.UiTextWrap
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.toUiInputState

internal val UiShowcasePreviewEntries: List<AwakeUiPreviewEntry> = listOf(
    UiShowcaseOverviewPreview,
    UiShowcaseThemePreview,
    UiShowcaseTypographyPreview,
    UiShowcaseButtonsPreview,
    UiShowcaseAvatarPreview,
    UiShowcaseBreadcrumbPreview,
    UiShowcaseCardPreview,
    UiShowcaseSidebarPreview,
    ShadcnSidebarExamplePreview,
    UiShowcaseSelectionPreview,
    UiShowcaseRangeSliderPreview,
    UiShowcaseTabsPreview,
    UiShowcaseSelectPreview,
    UiShowcaseKbdSeparatorPreview,
    UiShowcaseFeedbackPreview,
    UiShowcaseAlertPreview,
    UiShowcaseTextInputPreview,
    UiShowcaseInputOtpPreview,
    UiShowcasePopupsPreview,
    UiShowcaseStatePreview,
    UiShowcaseButtonMatrixPreview,
    UiShowcaseFieldMatrixPreview,
    UiShowcaseSliderMatrixPreview,
    UiShowcaseDropdownOpenPreview,
    UiShowcasePopoverOpenPreview,
    UiShowcaseTooltipOpenPreview,
    UiShowcaseAlertDialogPreview,
    UiShowcaseScrollPanelPreview,
    UiShowcaseShimmerPreview,
    UiShowcaseCollapsiblePreview,
    UiShowcaseCollapsibleOpenPreview,
    UiShowcaseFieldDemoPreview,
)

private val PreviewOverlayMenuItems = listOf(
    ShadcnDropdownMenuItem(
        label = "Pinned action",
        enabled = false,
    ),
    ShadcnDropdownMenuSeparator,
    ShadcnDropdownMenuItem(
        label = "Duplicate panel",
    ),
    ShadcnDropdownMenuItem(
        label = "Delete scene",
        destructive = true,
    ),
)

internal expect fun previewMetadataFor(
    entry: AwakeUiPreviewEntry,
    reportScale: Int = 1,
): AwakeUiPreviewMetadata

/** iOS and wasmJs have no reflection, so their [previewMetadataFor] actuals return a 1x1
 * dummy -- rendering a real page into that frame is guaranteed to fail every bounds rule.
 * Tests that validate real preview metadata must skip on such targets instead of asserting
 * against the dummy. */
internal fun previewMetadataIsReal(): Boolean =
    UiShowcasePreviewEntries.firstOrNull()
        ?.let { !previewMetadataFor(it).id.endsWith("-dummy") } ?: false

@AwakeUiPreview(
    id = "ui-showcase-overview",
    title = "Overview",
    group = "Getting Started",
    summary = "Docs-style shell overview for the Awake shadcn showcase.",
    width = 900,
    height = 560,
    reportScale = 2,
)
internal object UiShowcaseOverviewPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcasePagePreviewFrame(metadata, pageId = "introduction")
}

@AwakeUiPreview(
    id = "ui-showcase-reference",
    title = "Reference Comparison",
    group = "Getting Started",
    summary = "Side-by-side visual cues we are matching against the shadcn reference.",
    width = 900,
    height = 620,
    reportScale = 2,
)
internal object UiShowcaseReferencePreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcasePagePreviewFrame(metadata, pageId = "reference")
}

@AwakeUiPreview(
    id = "ui-showcase-theming",
    title = "Theme Controls",
    group = "Getting Started",
    summary = "Preset, base color, accent, and dark-mode controls sharing the same Awake theme factory.",
    width = 900,
    height = 660,
    reportScale = 2,
)
internal object UiShowcaseThemePreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcasePagePreviewFrame(metadata, pageId = "theming")
}

@AwakeUiPreview(
    id = "ui-showcase-typography",
    title = "Typography",
    group = "Typography",
    summary = "The shadcn text component family, including shadcnLabel's default/required/disabled states.",
    width = 900,
    height = 620,
    reportScale = 2,
)
internal object UiShowcaseTypographyPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcasePagePreviewFrame(metadata, pageId = "typography")
}

@AwakeUiPreview(
    id = "ui-showcase-fonts",
    title = "Bitmap And True Font",
    group = "Typography",
    summary = "Direct specimen comparison between the bitmap default and the new TTF-derived runtime font.",
    width = 920,
    height = 620,
    reportScale = 2,
)
internal object UiShowcaseFontsPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcasePagePreviewFrame(metadata, pageId = "fonts")
}

@AwakeUiPreview(
    id = "ui-showcase-layout",
    title = "Layout Primitives",
    group = "Layout",
    summary = "row/column/spacer, the modifier-first layout primitives every other page is built from.",
    width = 900,
    height = 460,
    reportScale = 2,
)
internal object UiShowcaseLayoutPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcasePagePreviewFrame(metadata, pageId = "layout")
}

@AwakeUiPreview(
    id = "ui-showcase-canvas",
    title = "Canvas",
    group = "Layout",
    summary = "Immediate-mode drawing through Awake's public canvas DSL: gradients, paths, clipping, and nested local coordinates.",
    width = 900,
    height = 520,
    reportScale = 2,
)
internal object UiShowcaseCanvasPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcaseCardPreviewFrame(
            metadata = metadata,
            surfaceId = "canvas",
            badge = "PATTERNS",
            title = "Canvas",
            summary = "Immediate-mode drawing through Awake's public canvas DSL: gradients, paths, clipping, and nested local coordinates.",
        ) {
            drawUiShowcaseCanvasPreview()
        }
}

@AwakeUiPreview(
    id = "ui-showcase-slot-apis",
    title = "Slot APIs",
    group = "Patterns",
    summary = "buttonSlot(...)'s content-lambda form composing arbitrary content inside a widget's own claimed slot.",
    width = 900,
    height = 360,
    reportScale = 2,
)
internal object UiShowcaseSlotApisPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcasePagePreviewFrame(metadata, pageId = "slot-apis")
}

@AwakeUiPreview(
    id = "ui-showcase-buttons",
    title = "Buttons And Badges",
    group = "Inputs",
    summary = "Core action and status components rendered with the current Awake shadcn recipe.",
    width = 900,
    height = 560,
    reportScale = 2,
)
internal object UiShowcaseButtonsPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcasePagePreviewFrame(metadata, pageId = "buttons")
}

@AwakeUiPreview(
    id = "ui-showcase-avatar",
    title = "Avatar",
    group = "Inputs",
    summary = "Initials-string convenience overload beside the slot-based primary overload rendering custom icon content.",
    width = 900,
    height = 360,
    reportScale = 2,
)
internal object UiShowcaseAvatarPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcasePagePreviewFrame(metadata, pageId = "avatar")
}

@AwakeUiPreview(
    id = "ui-showcase-breadcrumb",
    title = "Breadcrumb",
    group = "Layout",
    summary = "A muted link trail with the last item rendered as plain current-page text.",
    width = 900,
    height = 320,
    reportScale = 2,
)
internal object UiShowcaseBreadcrumbPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcasePagePreviewFrame(metadata, pageId = "breadcrumb")
}

@AwakeUiPreview(
    id = "ui-showcase-card",
    title = "Card",
    group = "Layout",
    summary = "Full header+body+footer, header+body-only, and body-only slot combinations of shadcnCard.",
    width = 900,
    height = 620,
    reportScale = 2,
)
internal object UiShowcaseCardPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcasePagePreviewFrame(metadata, pageId = "card")
}

@AwakeUiPreview(
    id = "ui-showcase-sidebar",
    title = "Sidebar",
    group = "Layout",
    summary = "A fixed-width navigation shell beside a content-only variant, both header/footer optional.",
    width = 900,
    height = 560,
    reportScale = 2,
)
internal object UiShowcaseSidebarPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcasePagePreviewFrame(metadata, pageId = "sidebar")
}

@AwakeUiPreview(
    id = "ui-showcase-selection",
    title = "Selection Controls",
    group = "Inputs",
    summary = "Toggle, Switch, Checkbox, RadioGroup, and ToggleGroup -- the current Awake-owned selection family.",
    width = 900,
    height = 640,
    reportScale = 2,
)
internal object UiShowcaseSelectionPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcasePagePreviewFrame(metadata, pageId = "selection")
}

@AwakeUiPreview(
    id = "ui-showcase-range-slider",
    title = "Range Slider",
    group = "Inputs",
    summary = "Dual-thumb variant of Slider -- two draggable knobs sharing one track, fill spanning only between them.",
    width = 900,
    height = 380,
    reportScale = 2,
)
internal object UiShowcaseRangeSliderPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcasePagePreviewFrame(metadata, pageId = "range-slider")
}

@AwakeUiPreview(
    id = "ui-showcase-tabs",
    title = "Tabs",
    group = "Layout",
    summary = "A muted track with a raised active tab, composed from shadcnButton.",
    width = 900,
    height = 320,
    reportScale = 2,
)
internal object UiShowcaseTabsPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcasePagePreviewFrame(metadata, pageId = "tabs")
}

@AwakeUiPreview(
    id = "ui-showcase-select",
    title = "Select",
    group = "Inputs",
    summary = "Closed-state dropdown triggers -- a non-searchable Select matching real shadcn's plain Select.",
    width = 900,
    height = 320,
    reportScale = 2,
)
internal object UiShowcaseSelectPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcasePagePreviewFrame(metadata, pageId = "select")
}

@AwakeUiPreview(
    id = "ui-showcase-kbd-separator",
    title = "Kbd And Separator",
    group = "Layout",
    summary = "Two tiny presentational primitives with no variant/state axis, grouped on one page.",
    width = 900,
    height = 360,
    reportScale = 2,
)
internal object UiShowcaseKbdSeparatorPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcasePagePreviewFrame(metadata, pageId = "kbd-separator")
}

@AwakeUiPreview(
    id = "ui-showcase-feedback",
    title = "Feedback",
    group = "Layout",
    summary = "Progress, Skeleton, and Spinner -- three small loading/status primitives grouped on one page.",
    width = 900,
    height = 480,
    reportScale = 2,
)
internal object UiShowcaseFeedbackPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcasePagePreviewFrame(metadata, pageId = "feedback")
}

@AwakeUiPreview(
    id = "ui-showcase-alert",
    title = "Alert",
    group = "Overlays",
    summary = "Default and destructive inline banner variants.",
    width = 900,
    height = 400,
    reportScale = 2,
)
internal object UiShowcaseAlertPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcasePagePreviewFrame(metadata, pageId = "alert")
}

@AwakeUiPreview(
    id = "ui-showcase-text-input",
    title = "Text Input",
    group = "Inputs",
    summary = "A real, typeable single-line field with click-to-position cursor and keyboard editing.",
    width = 900,
    height = 460,
    reportScale = 2,
)
internal object UiShowcaseTextInputPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcasePagePreviewFrame(metadata, pageId = "text-input")
}

@AwakeUiPreview(
    id = "ui-showcase-popups",
    title = "Popup Components",
    group = "Overlays",
    summary = "Dropdown and dialog proofs rendered through the shared DSL surface.",
    width = 900,
    height = 620,
    reportScale = 2,
)
internal object UiShowcasePopupsPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcasePagePreviewFrame(metadata, pageId = "popups")
}

@AwakeUiPreview(
    id = "ui-showcase-scroll-panel",
    title = "Scroll Panel State",
    group = "Layout",
    summary = "Clipped scroll content and scrollbar proof in one static validation surface.",
    width = 920,
    height = 440,
    reportScale = 2,
)
internal object UiShowcaseScrollPanelPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcaseCardPreviewFrame(
            metadata = metadata,
            badge = "LAYOUT",
            title = "Scroll panel state",
            summary = "Scrollable content should clip cleanly, reserve a thumb lane, and keep the card shell measured correctly.",
        ) {
            drawUiShowcaseScrollPanelContent()
        }
}

@AwakeUiPreview(
    id = "ui-showcase-shimmer",
    title = "Shimmer Effect",
    group = "Animations",
    summary = "A sweeping highlight animation for loading states and attention cues.",
    width = 900,
    height = 320,
    reportScale = 2,
)
internal object UiShowcaseShimmerPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcasePagePreviewFrame(metadata, pageId = "shimmer")
}

// The easing page's tween thumbs animate continuously, so per docs/reference/ui-validation.md's
// "Animated Components" rule a single rest-frame snapshot isn't enough proof. Each of the three
// previews below drives the *same* page content through UiShowcaseEasingDurationMs (1200ms) worth
// of elapsed time by feeding a single beginFrame() call a specific deltaSeconds -- animateFloatTween
// advances its stored elapsed time by exactly that delta on the one frame it's called, so this
// reaches an exact fraction without needing to loop multiple frames.
@AwakeUiPreview(
    id = "ui-showcase-easing-rest",
    title = "Easing (Rest)",
    group = "Animations",
    summary = "Easing tween thumbs at their rest state (fraction 0) before the animation starts.",
    width = 900,
    height = 620,
    reportScale = 2,
)
internal object UiShowcaseEasingRestPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcaseEasingPreviewFrame(metadata, deltaSeconds = 0f)
}

@AwakeUiPreview(
    id = "ui-showcase-easing-in-flight",
    title = "Easing (In Flight)",
    group = "Animations",
    summary = "Easing tween thumbs mid-animation (fraction 0.5), where the four curves visibly diverge in position.",
    width = 900,
    height = 620,
    reportScale = 2,
)
internal object UiShowcaseEasingInFlightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcaseEasingPreviewFrame(metadata, deltaSeconds = 0.6f)
}

@AwakeUiPreview(
    id = "ui-showcase-easing-settled",
    title = "Easing (Settled)",
    group = "Animations",
    summary = "Easing tween thumbs at their settled state (fraction 1) once the tween duration elapses.",
    width = 900,
    height = 620,
    reportScale = 2,
)
internal object UiShowcaseEasingSettledPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcaseEasingPreviewFrame(metadata, deltaSeconds = 1.2f)
}

@AwakeUiPreview(
    id = "ui-showcase-state",
    title = "State Container",
    group = "Patterns",
    summary = "Reducer-backed counter preview showing Awake's small MVI path in the sample catalog.",
    width = 900,
    height = 560,
    reportScale = 2,
)
internal object UiShowcaseStatePreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcasePagePreviewFrame(metadata, pageId = "state")
}

@AwakeUiPreview(
    id = "ui-showcase-button-matrix",
    title = "Button State Matrix",
    group = "Inputs",
    summary = "Variant and long-label button coverage side by side so spacing and fit regressions show up without a click-through.",
    width = 920,
    height = 420,
    reportScale = 2,
)
internal object UiShowcaseButtonMatrixPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcaseCardPreviewFrame(
            metadata = metadata,
            badge = "INPUTS",
            title = "Button state matrix",
            summary = "Primary, secondary, outline, ghost, danger, and constrained-label cases captured as one validation surface.",
        ) {
            drawUiShowcaseButtonMatrixContent()
        }
}

@AwakeUiPreview(
    id = "ui-showcase-field-matrix",
    title = "Field State Matrix",
    group = "Inputs",
    summary = "Text fields, dropdowns, toggles, and checkboxes rendered in their key states under the shared shadcn recipe.",
    width = 920,
    height = 520,
    reportScale = 2,
)
internal object UiShowcaseFieldMatrixPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcaseCardPreviewFrame(
            metadata = metadata,
            badge = "INPUTS",
            title = "Field state matrix",
            summary = "Focused text, placeholder text, closed selects, and binary controls all share the same automated review surface.",
            focusedNodeId = "showcase-matrix-field-focused",
        ) {
            drawUiShowcaseFieldMatrixContent()
        }
}

@AwakeUiPreview(
    id = "ui-showcase-slider-matrix",
    title = "Slider State Matrix",
    group = "Inputs",
    summary = "Low, mid, and max slider positions rendered together so track fill, thumb sizing, and label rhythm stay stable.",
    width = 920,
    height = 420,
    reportScale = 2,
)
internal object UiShowcaseSliderMatrixPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcaseCardPreviewFrame(
            metadata = metadata,
            badge = "INPUTS",
            title = "Slider state matrix",
            summary = "Three values expose track fill, knob placement, and label spacing without interactive setup.",
        ) {
            drawUiShowcaseSliderMatrixContent()
        }
}

@AwakeUiPreview(
    id = "ui-showcase-dropdown-open",
    title = "Dropdown Open State",
    group = "Overlays",
    summary = "Open popover-state proof for menu spacing, grouping, and disabled/destructive rows.",
    width = 920,
    height = 420,
    reportScale = 2,
)
internal object UiShowcaseDropdownOpenPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcaseCardPreviewFrame(
            metadata = metadata,
            badge = "OVERLAYS",
            title = "Dropdown open state",
            summary = "The menu stays inside a real popover container, not a loose stack of buttons.",
        ) {
            drawUiShowcaseDropdownOpenContent()
        }
}

@AwakeUiPreview(
    id = "ui-showcase-popover-open",
    title = "Popover Open State",
    group = "Overlays",
    summary = "Open popover proof for freeform content, anchored placement, and panel chrome.",
    width = 920,
    height = 380,
    reportScale = 2,
)
internal object UiShowcasePopoverOpenPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcaseCardPreviewFrame(
            metadata = metadata,
            badge = "OVERLAYS",
            title = "Popover open state",
            summary = "The panel stays inside a real popover container anchored to its trigger, with freeform content instead of a fixed menu row list.",
        ) {
            drawUiShowcasePopoverOpenContent()
        }
}

@AwakeUiPreview(
    id = "ui-showcase-tooltip-open",
    title = "Tooltip Open State",
    group = "Overlays",
    summary = "Open tooltip proof for anchored placement, wrap, and popover chrome.",
    width = 920,
    height = 460,
    reportScale = 2,
)
internal object UiShowcaseTooltipOpenPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcaseCardPreviewFrame(
            metadata = metadata,
            badge = "OVERLAYS",
            title = "Tooltip open state",
            summary = "The helper text should sit in a real surfaced popup, aligned to the trigger with tidy spacing.",
        ) {
            drawUiShowcaseTooltipOpenContent()
        }
}

@AwakeUiPreview(
    id = "ui-showcase-alert-dialog",
    title = "Alert Dialog Open State",
    group = "Overlays",
    summary = "Centered dialog-state proof for wrapping, action-row layout, and scrim rendering.",
    width = 920,
    height = 520,
    reportScale = 2,
)
internal object UiShowcaseAlertDialogPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcaseCardPreviewFrame(
            metadata = metadata,
            badge = "OVERLAYS",
            title = "Alert dialog open state",
            summary = "Long titles and dialog actions stay clipped and aligned inside the centered modal shell.",
        ) {
            drawUiShowcaseAlertDialogContent()
        }
}

@AwakeUiPreview(
    id = "ui-showcase-collapsible",
    title = "Collapsible",
    group = "Layout",
    summary = "Ghost-button header disclosure panel in its default collapsed state.",
    width = 900,
    height = 360,
    reportScale = 2,
)
internal object UiShowcaseCollapsiblePreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcasePagePreviewFrame(metadata, pageId = "collapsible")
}

@AwakeUiPreview(
    id = "ui-showcase-collapsible-open",
    title = "Collapsible Open State",
    group = "Layout",
    summary = "Expanded-state proof for the collapsible's animated height transition and revealed content.",
    width = 900,
    height = 440,
    reportScale = 2,
)
internal object UiShowcaseCollapsibleOpenPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcaseCardPreviewFrame(
            metadata = metadata,
            badge = "LAYOUT",
            title = "Collapsible open state",
            summary = "Expanded content, separators, and row spacing stay stable while the panel is open.",
        ) {
            drawUiShowcaseCollapsibleOpenContent()
        }
}

@AwakeUiPreview(
    id = "ui-showcase-field-demo",
    title = "Checkout Form",
    group = "Patterns",
    summary = "shadcn/ui's Payment Method checkout form rebuilt from shadcnFieldSet/shadcnFieldLegend and the rest of the Field family.",
    width = 900,
    height = 1000,
    reportScale = 2,
)
internal object UiShowcaseFieldDemoPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcasePagePreviewFrame(metadata, pageId = "field-demo")
}

private fun renderUiShowcasePagePreviewFrame(
    metadata: AwakeUiPreviewMetadata,
    pageId: String,
): AwakeUiPreviewFrame {
    val state = UiShowcaseRuntimeState()
    val page = showcasePageById(pageId)
    return renderUiShowcaseCardPreviewFrame(
        metadata = metadata,
        surfaceId = page.id,
        badge = page.category.title.uppercase(),
        title = page.title,
        summary = page.description,
    ) {
        renderUiShowcasePagePreview(page, state)
    }
}

/**
 * Renders the easing page's content with a single [deltaSeconds] fed into one [UiContext.beginFrame]
 * call, so [io.github.ronjunevaldoz.awake.ui.animateFloatTween]'s stored elapsed time advances by
 * exactly that amount (see [UiShowcaseEasingRestPreview] and friends above).
 */
private fun renderUiShowcaseEasingPreviewFrame(
    metadata: AwakeUiPreviewMetadata,
    deltaSeconds: Float,
): AwakeUiPreviewFrame {
    val previewScale = metadata.reportScale.coerceAtLeast(1)
    val state = UiShowcaseRuntimeState()
    val theme = state.showcaseTheme()
    val font = UiFonts.default(cellSize = 12 * previewScale)
    val ui = UiContext()
    val page = ShowcasePages.firstOrNull { it.id == "animation" } ?: ShowcasePages.first()

    return withPreviewDensity(previewScale) {
        val insetPx = 24f * previewScale
        val contentGapPx = 10f * previewScale
        val previewInput = Input()
        previewInput.setPointer(down = false, x = -100f, y = -100f)
        ui.beginFrame(
            UiFrameInput(
                viewportWidth = metadata.rasterWidth.toFloat(),
                viewportHeight = metadata.rasterHeight.toFloat(),
                input = previewInput.updateSnapshot().toUiInputState(),
                deltaSeconds = deltaSeconds,
            ),
        )
        ui.pushFont(font)
        ui.pushTheme(theme)
        ui.headlessRoot().column(
            modifier = Modifier
                .offset(insetPx.px, insetPx.px)
                .width((metadata.rasterWidth.toFloat() - insetPx * 2f).dp)
                .height((metadata.rasterHeight.toFloat() - insetPx * 2f).dp),
            verticalArrangement = HeadlessArrangement.spacedBy((contentGapPx / previewScale).dp),
        ) {
            surface(
                id = "ui-showcase-preview-${metadata.id}",
                style = SurfaceStyle(cornerRadius = 16f.dp),
                modifier = Modifier,
            ) {
                shadcnBadge(
                    id = "${metadata.id}.category",
                    label = page.category.title.uppercase(),
                    variant = ShadcnBadgeVariant.Outline,
                )
                shadcnSectionTitle(title = metadata.title, description = metadata.summary)
                spacer(Modifier.height(10f.dp))
                renderUiShowcasePagePreview(page, state)
            }
        }

        AwakeUiPreviewFrame(
            primitives = ui.endFrame(),
            background = theme.colors.background,
            font = font,
            semantics = ui.semanticNodes(),
        )
    }
}

private fun renderUiShowcaseCardPreviewFrame(
    metadata: AwakeUiPreviewMetadata,
    surfaceId: String = metadata.id,
    badge: String,
    title: String,
    summary: String,
    focusedNodeId: String? = null,
    content: ColumnScope.() -> Unit,
): AwakeUiPreviewFrame {
    val previewScale = metadata.reportScale.coerceAtLeast(1)
    val state = UiShowcaseRuntimeState()
    val theme = state.showcaseTheme()
    val font = UiFonts.default(cellSize = 12 * previewScale)
    val ui = UiContext()

    return withPreviewDensity(previewScale) {
        val insetPx = 24f * previewScale
        val contentGapPx = 10f * previewScale
        val previewInput = Input()
        previewInput.setPointer(down = false, x = -100f, y = -100f)
        ui.beginFrame(
            metadata.rasterWidth.toFloat(),
            metadata.rasterHeight.toFloat(),
            previewInput.updateSnapshot().toUiInputState(),
        )
        if (focusedNodeId != null) {
            ui.requestFocus(focusedNodeId)
        }
        ui.pushFont(font)
        ui.pushTheme(theme)
        ui.headlessRoot().column(
            modifier = Modifier
                .offset(insetPx.px, insetPx.px)
                .width((metadata.rasterWidth.toFloat() - insetPx * 2f).dp)
                .height((metadata.rasterHeight.toFloat() - insetPx * 2f).dp),
            verticalArrangement = HeadlessArrangement.spacedBy((contentGapPx / previewScale).dp),
        ) {
            shadcnSurface(
                id = "ui-showcase-preview-$surfaceId",
                modifier = Modifier.fillMaxWidth(),
            ) {
                shadcnBadge(id = "${surfaceId}.badge", label = badge, variant = ShadcnBadgeVariant.Outline)
                shadcnSectionTitle(
                    title = title,
                    description = summary,
                )
                spacer(Modifier.height(10f.dp))
                content()
            }
        }

        AwakeUiPreviewFrame(
            primitives = ui.endFrame(),
            background = theme.colors.background,
            font = font,
            semantics = ui.semanticNodes(),
        )
    }
}

private fun ColumnScope.drawUiShowcaseButtonMatrixContent() {
    row(
        horizontalArrangement = HeadlessArrangement.spacedBy(10f.dp),
        modifier = Modifier.height(36f.dp),
    ) {
        shadcnButton(
            id = "showcase-matrix-button-primary",
            label = "Primary",
            modifier = Modifier.width(120f.dp).height(36f.dp),
            variant = ShadcnButtonVariant.Primary,
        )
        shadcnButton(
            id = "showcase-matrix-button-secondary",
            label = "Secondary",
            modifier = Modifier.width(136f.dp).height(36f.dp),
            variant = ShadcnButtonVariant.Secondary,
        )
        shadcnButton(
            id = "showcase-matrix-button-outline",
            label = "Outline",
            modifier = Modifier.width(112f.dp).height(36f.dp),
            variant = ShadcnButtonVariant.Outline,
        )
    }
    row(
        horizontalArrangement = HeadlessArrangement.spacedBy(10f.dp),
        modifier = Modifier.height(36f.dp),
    ) {
        shadcnButton(
            id = "showcase-matrix-button-ghost",
            label = "Ghost",
            modifier = Modifier.width(100f.dp).height(36f.dp),
            variant = ShadcnButtonVariant.Ghost,
        )
        shadcnButton(
            id = "showcase-matrix-button-danger",
            label = "Danger",
            modifier = Modifier.width(108f.dp).height(36f.dp),
            variant = ShadcnButtonVariant.Danger,
        )
        shadcnButton(
            id = "showcase-matrix-button-long",
            label = "Primary action with a long label",
            modifier = Modifier.width(248f.dp).height(36f.dp),
            variant = ShadcnButtonVariant.Primary,
        )
    }
    shadcnMuted("This matrix is the quick read for control height, horizontal padding, and long-label fit.")
}

private fun ColumnScope.drawUiShowcaseFieldMatrixContent() {
    row(
        horizontalArrangement = HeadlessArrangement.spacedBy(12f.dp),
        modifier = Modifier.height(36f.dp),
    ) {
        shadcnInput(
            id = "showcase-matrix-field-empty",
            value = "",
            placeholder = "Placeholder",
            modifier = Modifier.width(200f.dp).height(36f.dp),
        )
        shadcnInput(
            id = "showcase-matrix-field-focused",
            value = "Typed text",
            modifier = Modifier.width(200f.dp).height(36f.dp),
        )
    }
    row(
        horizontalArrangement = HeadlessArrangement.spacedBy(12f.dp),
        modifier = Modifier.height(36f.dp),
    ) {
        shadcnSelect(
            id = "showcase-matrix-dropdown-theme",
            options = listOf("Light", "Dark", "Auto"),
            selectedIndex = 0,
            modifier = Modifier.width(200f.dp),
        )
        shadcnSelect(
            id = "showcase-matrix-dropdown-accent",
            options = listOf("Base", "Blue", "Emerald"),
            selectedIndex = 1,
            modifier = Modifier.width(200f.dp),
        )
    }
    row(
        horizontalArrangement = HeadlessArrangement.spacedBy(16f.dp),
        modifier = Modifier.height(24f.dp),
    ) {
        shadcnToggle(
            id = "showcase-matrix-toggle-off",
            checked = false,
            label = "Off",
            modifier = Modifier.width(120f.dp).height(24f.dp),
        )
        shadcnToggle(
            id = "showcase-matrix-toggle-on",
            checked = true,
            label = "On",
            modifier = Modifier.width(120f.dp).height(24f.dp),
        )
    }
    row(
        horizontalArrangement = HeadlessArrangement.spacedBy(16f.dp),
        modifier = Modifier.height(24f.dp),
    ) {
        shadcnCheckbox(
            id = "showcase-matrix-checkbox-off",
            checked = false,
            label = "Unchecked",
            modifier = Modifier.width(180f.dp).height(24f.dp),
        )
        shadcnCheckbox(
            id = "showcase-matrix-checkbox-on",
            checked = true,
            label = "Checked",
            modifier = Modifier.width(180f.dp).height(24f.dp),
        )
    }
}

private fun ColumnScope.drawUiShowcaseSliderMatrixContent() {
    shadcnMuted("Sliders catch subtle spacing bugs quickly because thumb, fill, and label alignment drift together.")
    spacer(Modifier.height(8f.dp))
    shadcnSlider(
        id = "showcase-matrix-slider-low",
        min = 0f,
        max = 100f,
        value = 12f,
        label = "Exposure 12%",
        modifier = Modifier.width(360f.dp).height(32f.dp),
    )
    shadcnSlider(
        id = "showcase-matrix-slider-mid",
        min = 0f,
        max = 100f,
        value = 52f,
        label = "Exposure 52%",
        modifier = Modifier.width(360f.dp).height(32f.dp),
    )
    shadcnSlider(
        id = "showcase-matrix-slider-high",
        min = 0f,
        max = 100f,
        value = 100f,
        label = "Exposure 100%",
        modifier = Modifier.width(360f.dp).height(32f.dp),
    )
}

private fun ColumnScope.drawUiShowcaseDropdownOpenContent() {
    shadcnMuted(
        "This preview intentionally renders the menu in its expanded state so row spacing and popover chrome are reviewable in docs.",
    )
    spacer(Modifier.height(8f.dp))
    row(
        horizontalArrangement = HeadlessArrangement.spacedBy(12f.dp),
        modifier = Modifier.height(36f.dp),
    ) {
        val trigger = buttonSlot(
            id = "showcase-matrix-dropdown-trigger",
            label = "Actions",
            modifier = Modifier.width(124f.dp).height(36f.dp),
        )
        uiScope().shadcnDropdownMenu(
            id = "showcase-matrix-dropdown-menu",
            anchorSlot = trigger.slot,
            expanded = true,
            items = PreviewOverlayMenuItems,
            selectedIndex = 1,
            width = Dimension.Fixed(340f.dp),
            positionProvider = UiPopupDefaults.dropdown(offsetY = 4f.dp),
        )
        shadcnButton(
            id = "showcase-matrix-dropdown-secondary",
            label = "Secondary",
            modifier = Modifier.width(132f.dp).height(36f.dp),
            variant = ShadcnButtonVariant.Outline,
        )
    }
}

private fun ColumnScope.drawUiShowcasePopoverOpenContent() {
    shadcnMuted(
        "This preview intentionally renders the popover in its expanded state so anchored placement and panel chrome are reviewable in docs.",
    )
    spacer(Modifier.height(8f.dp))
    row(
        horizontalArrangement = HeadlessArrangement.spacedBy(12f.dp),
        modifier = Modifier.height(36f.dp),
    ) {
        spacer(Modifier.width(100f.dp))
        val trigger = buttonSlot(
            id = "showcase-matrix-popover-trigger",
            label = "Share",
            modifier = Modifier.width(120f.dp).height(36f.dp),
        )
        uiScope().shadcnPopover(
            id = "showcase-matrix-popover",
            anchorSlot = trigger.slot,
            expanded = true,
            width = Dimension.Fixed(280f.dp),
        ) {
            text(
                label = "Share scene",
                wrap = UiTextWrap.Word,
                overflow = UiTextOverflow.Ellipsis,
                maxLines = 1,
            )
            shadcnMuted("Anyone with the link can view this scene until you revoke it.")
        }
        shadcnButton(
            id = "showcase-matrix-popover-secondary",
            label = "Reference",
            modifier = Modifier.width(120f.dp).height(36f.dp),
            variant = ShadcnButtonVariant.Secondary,
        )
    }
}

private fun ColumnScope.drawUiShowcaseTooltipOpenContent() {
    shadcnMuted("A tooltip is a tiny overlay, but it still needs proper container chrome, spacing, and wrap behavior.")
    spacer(Modifier.height(8f.dp))
    row(
        horizontalArrangement = HeadlessArrangement.spacedBy(12f.dp),
        modifier = Modifier.height(36f.dp),
    ) {
        val trigger = buttonSlot(
            id = "showcase-matrix-tooltip-trigger",
            label = "Hover target",
            modifier = Modifier.width(156f.dp).height(36f.dp),
        )
        uiScope().shadcnTooltip(
            anchorSlot = trigger.slot,
            visible = true,
            width = Dimension.Fixed(260f.dp),
            positionProvider = UiPopupDefaults.dropdown(offsetY = 4f.dp),
        ) {
            text(
                label = "Scene stats stay live here when the cursor rests on the trigger.",
                wrap = UiTextWrap.Word,
                overflow = UiTextOverflow.Ellipsis,
                maxLines = 3,
            )
        }
        shadcnButton(
            id = "showcase-matrix-tooltip-secondary",
            label = "Reference",
            modifier = Modifier.width(120f.dp).height(36f.dp),
            variant = ShadcnButtonVariant.Secondary,
        )
    }
    spacer(Modifier.height(12f.dp))
    shadcnMuted("shadcnTooltipText is the text-only convenience wrapper: same anchor/popup composition, no custom content lambda.")
    spacer(Modifier.height(8f.dp))
    row(
        horizontalArrangement = HeadlessArrangement.spacedBy(12f.dp),
        modifier = Modifier.height(36f.dp),
    ) {
        val textTrigger = buttonSlot(
            id = "showcase-matrix-tooltip-text-trigger",
            label = "Text-only tooltip",
            modifier = Modifier.width(200f.dp).height(36f.dp),
        )
        uiScope().shadcnTooltipText(
            id = "tooltip-text",
            anchorSlot = textTrigger.slot,
            visible = true,
            text = "shadcnTooltipText skips the custom content lambda for a plain wrapped label.",
            positionProvider = UiPopupDefaults.dropdown(offsetY = 4f.dp),
        )
    }
}

private fun ColumnScope.drawUiShowcaseAlertDialogContent() {
    shadcnMuted(
        "The dialog is rendered open on purpose so title wrapping, message rhythm, scrim color, and action widths can be checked without live interaction.",
    )
    spacer(Modifier.height(8f.dp))
    shadcnButton(
        id = "showcase-matrix-dialog-trigger",
        label = "Open Dialog",
        modifier = Modifier.width(156f.dp).height(36f.dp),
        variant = ShadcnButtonVariant.Outline,
    )
    uiScope().shadcnAlertDialog(
        id = "showcase-matrix-alert-dialog",
        expanded = true,
        title = "Delete this long showcase card title before publishing the updated catalog?",
        message = "This static preview exists only to validate the dialog treatment. No real deletion happens here.",
        confirmLabel = "Delete",
        dismissLabel = "Cancel",
    )
}

private fun ColumnScope.drawUiShowcaseCollapsibleOpenContent() {
    shadcnMuted(
        "The panel is rendered expanded on purpose so revealed-content spacing and separators are reviewable without live interaction.",
    )
    spacer(Modifier.height(8f.dp))
    shadcnCollapsible(
        id = "showcase-matrix-collapsible",
        title = "@radix-ui/primitives",
        expanded = true,
        onExpandedChange = {},
    ) {
        column(
            verticalArrangement = HeadlessArrangement.spacedBy(8f.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            shadcnSeparator(modifier = Modifier.padding(horizontal = 0f.dp, vertical = 4f.dp))
            row(modifier = Modifier.fillMaxWidth().height(32f.dp)) {
                text(
                    "@radix-ui/colors",
                    modifier = Modifier.padding(12f.dp, 0f.dp),
                )
            }
            row(modifier = Modifier.fillMaxWidth().height(32f.dp)) {
                text(
                    "@stitches/react",
                    modifier = Modifier.padding(12f.dp, 0f.dp),
                )
            }
        }
    }
}

private fun ColumnScope.drawUiShowcaseScrollPanelContent() {
    val scrollState = uiScope().rememberScrollState(
        id = "showcase-matrix-scroll-state",
        initialOffsetY = 34f,
    )
    shadcnMuted("This static proof starts partially scrolled so viewport clipping and the scrollbar thumb are visible immediately.")
    spacer(Modifier.height(8f.dp))
    uiScope().shadcnScrollArea(
        id = "showcase-matrix-scroll-panel",
        modifier = Modifier
            .width(420f.dp)
            .height(168f.dp)
            .verticalScroll(scrollState),
    ) {
        repeat(8) { index ->
            shadcnButton(
                id = "showcase-matrix-scroll-item-$index",
                label = "Scene action row ${index + 1}",
                modifier = Modifier.width(360f.dp).height(32f.dp),
                variant = if (index % 2 == 0) ShadcnButtonVariant.Outline else ShadcnButtonVariant.Ghost,
            )
        }
    }
}

private inline fun <T> withPreviewDensity(scale: Int, block: () -> T): T {
    val previousScale = UiDensity.scale
    val previousFontScale = UiDensity.fontScale
    UiDensity.scale = scale.toFloat()
    UiDensity.fontScale = 1f
    return try {
        block()
    } finally {
        UiDensity.scale = previousScale
        UiDensity.fontScale = previousFontScale
    }
}

/**
 * The shadcn sidebar-07 example, rebuilt on Awake's own sidebar recipes.
 *
 * Deliberately bare -- no preview card, badge or section title -- so the raster can be laid next
 * to a screenshot of the real example and compared directly. Every other layout preview wraps its
 * subject in showcase chrome, which is fine for a catalogue and useless for parity.
 */
@AwakeUiPreview(
    id = "shadcn-sidebar-example",
    title = "Sidebar (shadcn example)",
    group = "Layout",
    summary = "Header switcher, one labelled group with an expanded submenu, and a pinned account footer.",
    width = 680,
    height = 440,
    reportScale = 2,
)
internal object ShadcnSidebarExamplePreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderShadcnSidebarExamplePreviewFrame(metadata)
}

private fun renderShadcnSidebarExamplePreviewFrame(
    metadata: AwakeUiPreviewMetadata,
): AwakeUiPreviewFrame {
    val previewScale = metadata.reportScale.coerceAtLeast(1)
    val state = UiShowcaseRuntimeState()
    val theme = state.showcaseTheme()
    val font = UiFonts.default(cellSize = 12 * previewScale)
    val ui = UiContext()

    return withPreviewDensity(previewScale) {
        // Authored in dp, not raster px. `rasterWidth/rasterHeight` are already width x
        // reportScale, so feeding them to `.dp` scales them a second time and the root box comes
        // out twice the frame -- a fillMaxHeight child then resolves against a height no one can
        // see, and a weighted child divides it into nonsense.
        val insetDp = 16f
        val previewInput = Input()
        previewInput.setPointer(down = false, x = -100f, y = -100f)
        ui.beginFrame(
            metadata.rasterWidth.toFloat(),
            metadata.rasterHeight.toFloat(),
            previewInput.updateSnapshot().toUiInputState(),
        )
        ui.pushFont(font)
        ui.pushTheme(theme)
        ui.headlessRoot().row(
            modifier = Modifier
                .offset((insetDp * previewScale).px, (insetDp * previewScale).px)
                .width((metadata.width.toFloat() - insetDp * 2f).dp)
                .height((metadata.height.toFloat() - insetDp * 2f).dp),
        ) {
            shadcnSidebar(
                id = "shadcn-sidebar-example",
                modifier = Modifier.width(256f.dp).fillMaxHeight(),
                header = {
                    shadcnSidebarHeaderButton(
                        id = "shadcn-sidebar-example.team",
                        title = "Acme Inc",
                        subtitle = "Enterprise",
                    )
                },
                footer = {
                    shadcnSidebarFooterButton(
                        id = "shadcn-sidebar-example.account",
                        name = "shadcn",
                        email = "m@example.com",
                    )
                },
            ) {
                shadcnSidebarGroup(label = "Platform") {
                    shadcnSidebarMenu {
                        shadcnSidebarMenuItem(
                            id = "shadcn-sidebar-example.playground",
                            label = "Playground",
                            icon = ShadcnIcons.sparkles,
                            active = true,
                        )
                        shadcnSidebarMenuSub {
                            shadcnSidebarMenuSubItem(
                                id = "shadcn-sidebar-example.history",
                                label = "History",
                            )
                            shadcnSidebarMenuSubItem(
                                id = "shadcn-sidebar-example.starred",
                                label = "Starred",
                            )
                            shadcnSidebarMenuSubItem(
                                id = "shadcn-sidebar-example.sub-settings",
                                label = "Settings",
                            )
                        }
                        shadcnSidebarMenuItem(
                            id = "shadcn-sidebar-example.models",
                            label = "Models",
                            icon = ShadcnIcons.cube,
                        )
                        shadcnSidebarMenuItem(
                            id = "shadcn-sidebar-example.documentation",
                            label = "Documentation",
                            icon = ShadcnIcons.documentText,
                        )
                        shadcnSidebarMenuItem(
                            id = "shadcn-sidebar-example.settings",
                            label = "Settings",
                            icon = ShadcnIcons.cog6Tooth,
                        )
                    }
                }
            }
            shadcnSurface(
                id = "shadcn-sidebar-example.canvas",
                modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            ) {
                icon(ShadcnIcons.squares2x2)
            }
        }

        AwakeUiPreviewFrame(
            primitives = ui.endFrame(),
            background = theme.colors.background,
            font = font,
            semantics = ui.semanticNodes(),
        )
    }
}

@AwakeUiPreview(
    id = "ui-showcase-input-otp",
    title = "Input OTP",
    group = "Inputs",
    summary = "Segmented one-time-password entry -- six slots over one hidden backing field.",
    width = 640,
    height = 300,
    reportScale = 2,
)
internal object UiShowcaseInputOtpPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcasePagePreviewFrame(metadata, pageId = "input-otp")
}
