// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreview
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewEntry
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewFrame
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewMetadata
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.UiDensity
import io.github.ronjunevaldoz.awake.ui.column
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.UiDropdownMenuItem
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.UiDropdownMenuSeparator
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiPopupDefaults
import io.github.ronjunevaldoz.awake.ui.UiScrollState
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextWrap
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.alertDialog
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnCheckbox
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnDropdown
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnSectionHeader
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnSlider
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnTextField
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnToggle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnSurfaceVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.dropdownMenu
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.height
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.toUiInputState
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.tooltip
import io.github.ronjunevaldoz.awake.ui.layouts.ext.row
import io.github.ronjunevaldoz.awake.ui.verticalScroll
import io.github.ronjunevaldoz.awake.ui.width
import io.github.ronjunevaldoz.awake.ui.layouts.ext.spacer
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.unstyled.buttonSlot
import io.github.ronjunevaldoz.awake.ui.scrollPanel
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot

internal val UiShowcasePreviewEntries: List<AwakeUiPreviewEntry> = listOf(
    UiShowcaseOverviewPreview,
    UiShowcaseReferencePreview,
    UiShowcaseThemePreview,
    UiShowcaseFontsPreview,
    UiShowcaseLayoutPreview,
    UiShowcaseCanvasPreview,
    UiShowcaseSlotApisPreview,
    UiShowcaseButtonsPreview,
    UiShowcaseTextInputPreview,
    UiShowcasePopupsPreview,
    UiShowcaseStatePreview,
    UiShowcaseButtonMatrixPreview,
    UiShowcaseFieldMatrixPreview,
    UiShowcaseSliderMatrixPreview,
    UiShowcaseDropdownOpenPreview,
    UiShowcaseTooltipOpenPreview,
    UiShowcaseAlertDialogPreview,
    UiShowcaseScrollPanelPreview,
    UiShowcaseShimmerPreview
)

private val PreviewOverlayMenuItems = listOf(
    UiDropdownMenuItem(
        label = "Pinned action",
        enabled = false,
        supportingText = "Disabled rows stay visible in the popover."
    ),
    UiDropdownMenuSeparator,
    UiDropdownMenuItem(
        label = "Duplicate panel",
        trailingLabel = "Cmd+D",
        supportingText = "Secondary action metadata sits on the trailing edge."
    ),
    UiDropdownMenuItem(
        label = "Delete scene",
        destructive = true,
        trailingLabel = "Del",
        supportingText = "Destructive actions use the red foreground treatment."
    )
)

internal expect fun previewMetadataFor(
    entry: AwakeUiPreviewEntry,
    reportScale: Int = 1
): AwakeUiPreviewMetadata

@AwakeUiPreview(
    id = "ui-showcase-overview",
    title = "Overview",
    group = "Getting Started",
    summary = "Docs-style shell overview for the Awake shadcn showcase.",
    width = 900,
    height = 560,
    reportScale = 2
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
    reportScale = 2
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
    reportScale = 2
)
internal object UiShowcaseThemePreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcasePagePreviewFrame(metadata, pageId = "theming")
}

@AwakeUiPreview(
    id = "ui-showcase-fonts",
    title = "Bitmap And True Font",
    group = "Typography",
    summary = "Direct specimen comparison between the bitmap default and the new TTF-derived runtime font.",
    width = 920,
    height = 620,
    reportScale = 2
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
    reportScale = 2
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
    reportScale = 2
)
internal object UiShowcaseCanvasPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcasePagePreviewFrame(metadata, pageId = "canvas")
}

@AwakeUiPreview(
    id = "ui-showcase-slot-apis",
    title = "Slot APIs",
    group = "Patterns",
    summary = "buttonSlot(...)'s content-lambda form composing arbitrary content inside a widget's own claimed slot.",
    width = 900,
    height = 360,
    reportScale = 2
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
    reportScale = 2
)
internal object UiShowcaseButtonsPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcasePagePreviewFrame(metadata, pageId = "buttons")
}

@AwakeUiPreview(
    id = "ui-showcase-text-input",
    title = "Text Input",
    group = "Inputs",
    summary = "A real, typeable single-line field with click-to-position cursor and keyboard editing.",
    width = 900,
    height = 460,
    reportScale = 2
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
    reportScale = 2
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
    reportScale = 2
)
internal object UiShowcaseScrollPanelPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcaseCardPreviewFrame(
            metadata = metadata,
            badge = "LAYOUT",
            title = "Scroll panel state",
            summary = "Scrollable content should clip cleanly, reserve a thumb lane, and keep the card shell measured correctly."
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
    reportScale = 2
)
internal object UiShowcaseShimmerPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcasePagePreviewFrame(metadata, pageId = "shimmer")
}

@AwakeUiPreview(
    id = "ui-showcase-state",
    title = "State Container",
    group = "Patterns",
    summary = "Reducer-backed counter preview showing Awake's small MVI path in the sample catalog.",
    width = 900,
    height = 560,
    reportScale = 2
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
    reportScale = 2
)
internal object UiShowcaseButtonMatrixPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcaseCardPreviewFrame(
            metadata = metadata,
            badge = "INPUTS",
            title = "Button state matrix",
            summary = "Primary, secondary, outline, ghost, danger, and constrained-label cases captured as one validation surface."
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
    reportScale = 2
)
internal object UiShowcaseFieldMatrixPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcaseCardPreviewFrame(
            metadata = metadata,
            badge = "INPUTS",
            title = "Field state matrix",
            summary = "Focused text, placeholder text, closed selects, and binary controls all share the same automated review surface.",
            focusedNodeId = "showcase-matrix-field-focused"
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
    reportScale = 2
)
internal object UiShowcaseSliderMatrixPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcaseCardPreviewFrame(
            metadata = metadata,
            badge = "INPUTS",
            title = "Slider state matrix",
            summary = "Three values expose track fill, knob placement, and label spacing without interactive setup."
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
    reportScale = 2
)
internal object UiShowcaseDropdownOpenPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcaseCardPreviewFrame(
            metadata = metadata,
            badge = "OVERLAYS",
            title = "Dropdown open state",
            summary = "The menu stays inside a real popover container, not a loose stack of buttons."
        ) {
            drawUiShowcaseDropdownOpenContent()
        }
}

@AwakeUiPreview(
    id = "ui-showcase-tooltip-open",
    title = "Tooltip Open State",
    group = "Overlays",
    summary = "Open tooltip proof for anchored placement, wrap, and popover chrome.",
    width = 920,
    height = 360,
    reportScale = 2
)
internal object UiShowcaseTooltipOpenPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcaseCardPreviewFrame(
            metadata = metadata,
            badge = "OVERLAYS",
            title = "Tooltip open state",
            summary = "The helper text should sit in a real surfaced popup, aligned to the trigger with tidy spacing."
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
    reportScale = 2
)
internal object UiShowcaseAlertDialogPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        renderUiShowcaseCardPreviewFrame(
            metadata = metadata,
            badge = "OVERLAYS",
            title = "Alert dialog open state",
            summary = "Long titles and dialog actions stay clipped and aligned inside the centered modal shell."
        ) {
            drawUiShowcaseAlertDialogContent()
        }
}
private fun renderUiShowcasePagePreviewFrame(
    metadata: AwakeUiPreviewMetadata,
    pageId: String,
): AwakeUiPreviewFrame {
    val state = UiShowcaseRuntimeState()
    val page = ShowcasePages.firstOrNull { it.id == pageId } ?: error("Unknown showcase page: $pageId")
    return renderUiShowcaseCardPreviewFrame(
        metadata = metadata,
        surfaceId = page.id,
        badge = page.category.title.uppercase(),
        title = page.title,
        summary = page.description
    ) {
        renderUiShowcasePagePreview(page, state)
    }
}

private fun renderUiShowcaseCardPreviewFrame(
    metadata: AwakeUiPreviewMetadata,
    surfaceId: String = metadata.id,
    badge: String,
    title: String,
    summary: String,
    focusedNodeId: String? = null,
    content: ColumnScope.() -> Unit
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
        ui.beginFrame(metadata.rasterWidth.toFloat(), metadata.rasterHeight.toFloat(), previewInput.updateSnapshot().toUiInputState())
        if (focusedNodeId != null) {
            ui.requestFocus(focusedNodeId)
        }
        ui.column(
            slot = UiSlot(
                insetPx,
                insetPx,
                metadata.rasterWidth.toFloat() - insetPx * 2f,
                metadata.rasterHeight.toFloat() - insetPx * 2f
            ),
            font = font,
            theme = theme,
            verticalArrangement = Arrangement.spacedBy((contentGapPx / previewScale).dp)
        ) {
            awakeShadcnSurface(
                id = "ui-showcase-preview-$surfaceId",
                width = Dimension.FillMax,
                height = Dimension.WrapContent,
                variant = AwakeShadcnSurfaceVariant.Card,
                style = Style { shape(16f.dp) }
            ) {
                awakeShadcnBadge(badge, variant = AwakeShadcnBadgeVariant.Outline)
                awakeShadcnSectionHeader(
                    title = title,
                    description = summary
                )
                spacer(UiModifier().height(10f.dp))
                content()
            }
        }

        AwakeUiPreviewFrame(
            primitives = ui.endFrame(),
            background = theme.tokens.background,
            font = font,
            semantics = ui.semanticNodes()
        )
    }
}

private fun ColumnScope.drawUiShowcaseButtonMatrixContent() {
    row(height = 36f.dp, horizontalArrangement = Arrangement.spacedBy(10f.dp)) {
        awakeShadcnButton(
            id = "showcase-matrix-button-primary",
            label = "Primary",
            modifier = UiModifier().width(120f.px).height(36f.dp),
            variant = AwakeShadcnButtonVariant.Primary
        )
        awakeShadcnButton(
            id = "showcase-matrix-button-secondary",
            label = "Secondary",
            modifier = UiModifier().width(120f.px).height(36f.dp),
            variant = AwakeShadcnButtonVariant.Secondary
        )
        awakeShadcnButton(
            id = "showcase-matrix-button-outline",
            label = "Outline",
            modifier = UiModifier().width(112f.px).height(36f.dp),
            variant = AwakeShadcnButtonVariant.Outline
        )
    }
    row(height = 36f.dp, horizontalArrangement = Arrangement.spacedBy(10f.dp)) {
        awakeShadcnButton(
            id = "showcase-matrix-button-ghost",
            label = "Ghost",
            modifier = UiModifier().width(100f.px).height(36f.dp),
            variant = AwakeShadcnButtonVariant.Ghost
        )
        awakeShadcnButton(
            id = "showcase-matrix-button-danger",
            label = "Danger",
            modifier = UiModifier().width(108f.px).height(36f.dp),
            variant = AwakeShadcnButtonVariant.Danger
        )
        awakeShadcnButton(
            id = "showcase-matrix-button-long",
            label = "Primary action with a long label",
            modifier = UiModifier().width(248f.px).height(36f.dp),
            variant = AwakeShadcnButtonVariant.Primary
        )
    }
    awakeShadcnSupportingText("This matrix is the quick read for control height, horizontal padding, and long-label fit.")
}

private fun ColumnScope.drawUiShowcaseFieldMatrixContent() {
    row(height = 36f.dp, horizontalArrangement = Arrangement.spacedBy(12f.dp)) {
        awakeShadcnTextField(
            id = "showcase-matrix-field-empty",
            value = "",
            placeholder = "Placeholder",
            modifier = UiModifier().width(200f.px).height(36f.dp)
        )
        awakeShadcnTextField(
            id = "showcase-matrix-field-focused",
            value = "Typed text",
            modifier = UiModifier().width(200f.px).height(36f.dp)
        )
    }
    row(height = 36f.dp, horizontalArrangement = Arrangement.spacedBy(12f.dp)) {
        awakeShadcnDropdown(
            id = "showcase-matrix-dropdown-theme",
            options = listOf("Light", "Dark", "Auto"),
            selectedIndex = 0,
            modifier = UiModifier().width(200f.px)
        )
        awakeShadcnDropdown(
            id = "showcase-matrix-dropdown-accent",
            options = listOf("Base", "Blue", "Emerald"),
            selectedIndex = 1,
            modifier = UiModifier().width(200f.px)
        )
    }
    row(height = 24f.dp, horizontalArrangement = Arrangement.spacedBy(16f.dp)) {
        awakeShadcnToggle(
            id = "showcase-matrix-toggle-off",
            checked = false,
            label = "Off",
            modifier = UiModifier().width(120f.px).height(24f.px)
        )
        awakeShadcnToggle(
            id = "showcase-matrix-toggle-on",
            checked = true,
            label = "On",
            modifier = UiModifier().width(120f.px).height(24f.px)
        )
    }
    row(height = 24f.dp, horizontalArrangement = Arrangement.spacedBy(16f.dp)) {
        awakeShadcnCheckbox(
            id = "showcase-matrix-checkbox-off",
            checked = false,
            label = "Unchecked",
            modifier = UiModifier().width(180f.px).height(24f.px)
        )
        awakeShadcnCheckbox(
            id = "showcase-matrix-checkbox-on",
            checked = true,
            label = "Checked",
            modifier = UiModifier().width(180f.px).height(24f.px)
        )
    }
}

private fun ColumnScope.drawUiShowcaseSliderMatrixContent() {
    awakeShadcnSupportingText("Sliders catch subtle spacing bugs quickly because thumb, fill, and label alignment drift together.")
    spacer(UiModifier().height(8f.dp))
    awakeShadcnSlider(
        id = "showcase-matrix-slider-low",
        min = 0f,
        max = 100f,
        value = 12f,
        label = "Exposure 12%",
        modifier = UiModifier().width(360f.px).height(32f.px)
    )
    awakeShadcnSlider(
        id = "showcase-matrix-slider-mid",
        min = 0f,
        max = 100f,
        value = 52f,
        label = "Exposure 52%",
        modifier = UiModifier().width(360f.px).height(32f.px)
    )
    awakeShadcnSlider(
        id = "showcase-matrix-slider-high",
        min = 0f,
        max = 100f,
        value = 100f,
        label = "Exposure 100%",
        modifier = UiModifier().width(360f.px).height(32f.px)
    )
}

private fun ColumnScope.drawUiShowcaseDropdownOpenContent() {
    awakeShadcnSupportingText(
        "This preview intentionally renders the menu in its expanded state so row spacing and popover chrome are reviewable in docs."
    )
    spacer(UiModifier().height(8f.dp))
    row(height = 36f.dp, horizontalArrangement = Arrangement.spacedBy(12f.dp)) {
        val trigger = buttonSlot(
            id = "showcase-matrix-dropdown-trigger",
            label = "Actions",
            modifier = UiModifier().width(124f.px).height(36f.dp),
            style = theme.components.button
        )
        dropdownMenu(
            id = "showcase-matrix-dropdown-menu",
            anchorSlot = trigger.slot,
            expanded = true,
            items = PreviewOverlayMenuItems,
            selectedIndex = 1,
            width = Dimension.Fixed(280f.px),
            itemHeight = 32f,
            positionProvider = UiPopupDefaults.dropdown(offsetY = 4f.dp),
            style = Style { contentPadding(4f.dp) }
        )
        awakeShadcnButton(
            id = "showcase-matrix-dropdown-secondary",
            label = "Secondary",
            modifier = UiModifier().width(132f.px).height(36f.dp),
            variant = AwakeShadcnButtonVariant.Outline
        )
    }
}

private fun ColumnScope.drawUiShowcaseTooltipOpenContent() {
    awakeShadcnSupportingText("A tooltip is a tiny overlay, but it still needs proper container chrome, spacing, and wrap behavior.")
    spacer(UiModifier().height(8f.dp))
    row(height = 36f.dp, horizontalArrangement = Arrangement.spacedBy(12f.dp)) {
        val trigger = buttonSlot(
            id = "showcase-matrix-tooltip-trigger",
            label = "Hover target",
            modifier = UiModifier().width(132f.px).height(36f.dp),
            style = theme.components.button
        )
        tooltip(
            anchorSlot = trigger.slot,
            visible = true,
            width = Dimension.Fixed(260f.dp),
            positionProvider = UiPopupDefaults.dropdown(offsetY = 4f.dp)
        ) {
            text(
                label = "Scene stats stay live here when the cursor rests on the trigger.",
                wrap = UiTextWrap.Word,
                overflow = UiTextOverflow.Ellipsis,
                maxLines = 3
            )
        }
        awakeShadcnButton(
            id = "showcase-matrix-tooltip-secondary",
            label = "Reference",
            modifier = UiModifier().width(120f.dp).height(36f.dp),
            variant = AwakeShadcnButtonVariant.Secondary
        )
    }
}

private fun ColumnScope.drawUiShowcaseAlertDialogContent() {
    awakeShadcnSupportingText(
        "The dialog is rendered open on purpose so title wrapping, message rhythm, scrim color, and action widths can be checked without live interaction."
    )
    spacer(UiModifier().height(8f.dp))
    awakeShadcnButton(
        id = "showcase-matrix-dialog-trigger",
        label = "Open Dialog",
        modifier = UiModifier().width(140f.px).height(36f.dp),
        variant = AwakeShadcnButtonVariant.Outline
    )
    alertDialog(
        id = "showcase-matrix-alert-dialog",
        expanded = true,
        title = "Delete this long showcase card title before publishing the updated catalog?",
        message = "This static preview exists only to validate the dialog treatment. No real deletion happens here.",
        confirmLabel = "Delete",
        dismissLabel = "Cancel"
    )
}

private fun ColumnScope.drawUiShowcaseScrollPanelContent() {
    val scrollState = UiScrollState(initialOffsetY = 34f)
    awakeShadcnSupportingText("This static proof starts partially scrolled so viewport clipping and the scrollbar thumb are visible immediately.")
    spacer(UiModifier().height(8f.dp))
    scrollPanel(
        id = "showcase-matrix-scroll-panel",
        modifier = UiModifier()
            .width(Dimension.Fixed(420f.px))
            .height(Dimension.Fixed(168f.px))
            .verticalScroll(scrollState),
        style = Style { shape(14f.dp) }
    ) {
        repeat(8) { index ->
            awakeShadcnButton(
                id = "showcase-matrix-scroll-item-$index",
                label = "Scene action row ${index + 1}",
                modifier = UiModifier().width(360f.px).height(32f.px),
                variant = if (index % 2 == 0) AwakeShadcnButtonVariant.Outline else AwakeShadcnButtonVariant.Ghost
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
