// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiColumnDslScope
import io.github.ronjunevaldoz.awake.ui.UiInsets
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiPopupDefaults
import io.github.ronjunevaldoz.awake.ui.UiScrollState
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.UiTextWrap
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnPropertyTextField
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnCheckbox
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnSectionHeader
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnSectionTitle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnSlider
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnScrollSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnToggle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnSurfaceVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.height
import io.github.ronjunevaldoz.awake.ui.offset
import io.github.ronjunevaldoz.awake.ui.rememberStateValue
import io.github.ronjunevaldoz.awake.ui.sp
import io.github.ronjunevaldoz.awake.ui.supportingLines
import io.github.ronjunevaldoz.awake.ui.text
import io.github.ronjunevaldoz.awake.ui.tooltip
import io.github.ronjunevaldoz.awake.ui.width
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.font.UiFonts

internal fun UiColumnDslScope.drawUiShowcaseButtonsPreview() {
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

internal fun UiColumnDslScope.drawUiShowcaseTextInputPreview(state: UiShowcaseRuntimeState) {
    val name = context.rememberStateValue("ui-showcase-text-input", "name") { "" }
    val email = context.rememberStateValue("ui-showcase-text-input", "email") { "" }

    awakeShadcnSectionTitle("Text Input")
    awakeShadcnSupportingText("Click a field, type, backspace, and use the arrow keys -- this is a real keyboard-driven widget, not a mockup.")
    spacer(UiModifier().height(8f.dp))
    awakeShadcnPropertyTextField(
        id = "showcase-name",
        label = "Name",
        value = name.value,
        placeholder = "Jane Doe"
    ).also { name.value = it }
    awakeShadcnPropertyTextField(
        id = "showcase-email",
        label = "Email",
        value = email.value,
        placeholder = "jane@example.com"
    ).also { email.value = it }
    spacer(UiModifier().height(8f.dp))
    awakeShadcnSupportingText(
        if (name.value.isEmpty() && email.value.isEmpty()) {
            "Nothing typed yet."
        } else {
            "You typed: ${name.value.ifEmpty { "(name)" }} / ${email.value.ifEmpty { "(email)" }}"
        }
    )
}

internal fun UiColumnDslScope.drawUiShowcaseSliderPreview() {
    val exposure = context.rememberStateValue("ui-showcase-slider", "exposure") { 52f }
    val bloom = context.rememberStateValue("ui-showcase-slider", "bloom") { 18f }

    awakeShadcnSectionTitle("Slider")
    awakeShadcnSupportingText("Use sliders when you need a continuous value with immediate visual feedback and a stable control footprint.")
    spacer(UiModifier().height(8f.dp))
    exposure.value = awakeShadcnSlider(
        id = "showcase-slider-exposure",
        min = 0f,
        max = 100f,
        value = exposure.value,
        label = "Exposure ${exposure.value.toInt()}%",
        modifier = UiModifier().width(360f.dp).height(32f.dp)
    )
    bloom.value = awakeShadcnSlider(
        id = "showcase-slider-bloom",
        min = 0f,
        max = 32f,
        value = bloom.value,
        label = "Bloom ${bloom.value.toInt()} px",
        modifier = UiModifier().width(360f.dp).height(32f.dp)
    )
    spacer(UiModifier().height(8f.dp))
    supportingLines(
        listOf(
            "The label stays inside the control row, so value changes do not resize the surrounding layout.",
            "The preview matrix covers low, mid, and max values; this live page proves the same component interactively."
        )
    )
}

internal fun UiColumnDslScope.drawUiShowcaseSelectionPreview() {
    val wireframe = context.rememberStateValue("ui-showcase-selection", "wireframe") { true }
    val stats = context.rememberStateValue("ui-showcase-selection", "stats") { false }

    awakeShadcnSectionTitle("Toggle And Checkbox")
    awakeShadcnSupportingText("These are the current Awake-owned selection controls. They already share the same shadcn token layer even though radio and tab families are still pending.")
    spacer(UiModifier().height(8f.dp))
    wireframe.value = awakeShadcnToggle(
        id = "showcase-selection-wireframe",
        checked = wireframe.value,
        label = "Wireframe overlay",
        modifier = UiModifier().width(220f.dp).height(24f.dp)
    )
    stats.value = awakeShadcnCheckbox(
        id = "showcase-selection-stats",
        checked = stats.value,
        label = "Scene statistics",
        modifier = UiModifier().width(220f.dp).height(24f.dp)
    )
    spacer(UiModifier().height(8f.dp))
    awakeShadcnSupportingText(
        "Selection state now lives in the same preview lane as fields and overlays, so color inversions and label alignment regressions show up faster."
    )
}

internal fun UiColumnDslScope.drawUiShowcaseFontsPreview() {
    awakeShadcnSectionHeader(
        title = "Font pipeline comparison",
        description = "The same specimen rendered through each Awake UI font path so we can judge edge quality and spacing directly."
    )
    spacer(UiModifier().height(8f.dp))
    row(height = 292f.dp, gap = 12f) {
        panel(
            id = "showcase-font-bitmap",
            width = Dimension.Fixed(264f.dp),
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
            width = Dimension.Fixed(264f.dp),
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

internal fun UiColumnDslScope.drawUiShowcaseScrollPanelPreview() {
    val scrollState = context.rememberStateValue("ui-showcase-scroll-panel", "state") { UiScrollState() }

    awakeShadcnSectionTitle("Scroll Panel")
    awakeShadcnSupportingText("Scrollable containers own clipping, content measurement, and the scrollbar lane so callers do not have to reimplement any of it.")
    spacer(UiModifier().height(8f.dp))
    awakeShadcnScrollSurface(
        id = "showcase-scroll-panel-page",
        width = Dimension.Fixed(420f.dp),
        height = Dimension.Fixed(176f.dp),
        state = scrollState.value,
        variant = AwakeShadcnSurfaceVariant.Card,
        style = Style { shape(14f.dp) }
    ) { _ ->
        repeat(10) { index ->
            awakeShadcnButton(
                id = "showcase-scroll-row-$index",
                label = "Inspector row ${index + 1}",
                modifier = UiModifier().width(360f.dp).height(32f.dp),
                variant = if (index % 2 == 0) AwakeShadcnButtonVariant.Outline else AwakeShadcnButtonVariant.Ghost
            )
        }
    }
    spacer(UiModifier().height(8f.dp))
    supportingLines(
        listOf(
            "The scroll thumb only appears when content actually exceeds the viewport.",
            "The widget-level preview report keeps a static clipped state around so we can catch scrollbar and clipping drift without manual scrolling."
        )
    )
}

internal fun UiColumnDslScope.drawUiShowcaseLayoutPreview() {
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

internal fun UiColumnDslScope.drawUiShowcaseSlotApiPreview() {
    awakeShadcnSectionTitle("buttonSlot(...) content lambda")
    awakeShadcnSupportingText("The label-string overload is sugar over this content-lambda form -- there is no capability gap between them.")
    spacer(UiModifier().height(8f.dp))
    buttonSlot(
        id = "slot-api-launch",
        modifier = UiModifier().width(180f.dp).height(40f.dp),
        style = theme.components.button
    ) {
        val labelSize = Style { textSize(theme.typography.label) }
        text(">", modifier = UiModifier().offset(x = 12f.dp).width(16f.dp), style = labelSize)
        text("Launch", modifier = UiModifier().offset(x = 32f.dp), style = labelSize)
    }
    spacer(UiModifier().height(16f.dp))
    awakeShadcnSectionTitle("Custom widgets, same primitives")
    awakeShadcnSupportingText("samples:hello-cube's Gauge.kt is a fully custom widget built from the same claimSlot()/emit() primitives a built-in widget uses -- no library-only capability gap.")
}

internal fun UiColumnDslScope.drawUiShowcaseTooltipPreview() {
    awakeShadcnSectionTitle("Tooltip")
    awakeShadcnSupportingText("Tooltips stay small and contextual: anchored to a trigger, wrapped inside a surfaced popup, and dismissible without changing the surrounding layout.")
    spacer(UiModifier().height(8f.dp))
    row(height = 36f.dp, gap = 12f) {
        val trigger = buttonSlot(
            id = "showcase-tooltip-trigger",
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
                label = "Frame pacing, draw calls, and scene counters can live in a tooltip without forcing a dedicated panel.",
                wrap = UiTextWrap.Word,
                overflow = UiTextOverflow.Ellipsis,
                maxLines = 3
            )
        }
        awakeShadcnButton(
            id = "showcase-tooltip-reference",
            label = "Reference",
            modifier = UiModifier().width(120f.dp).height(36f.dp),
            variant = AwakeShadcnButtonVariant.Secondary
        )
    }
    spacer(UiModifier().height(8f.dp))
    awakeShadcnSupportingText("The preview suite keeps an open-state proof so tooltip width, wrap, and anchoring stay reviewable without hover automation.")
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
        insets = UiInsets(16f.dp),
        overlayOnly = emitsToOverlay
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
        slot = specimenScope.claimSlot(Dimension.FillMax, Dimension.Fixed(44f.dp)),
        color = theme.tokens.mutedForeground,
        wrap = UiTextWrap.Word,
        overflow = UiTextOverflow.Ellipsis,
        maxLines = 3,
        textSize = 11f.sp
    )
}
