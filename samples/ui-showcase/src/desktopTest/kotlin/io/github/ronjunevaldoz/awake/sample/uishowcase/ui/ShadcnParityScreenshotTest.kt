// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

/**
 * Proof-of-concept answering "can we generate an Awake-rendered screenshot of the same
 * component, laid out to match a real shadcn-compose reference image?" -- yes: this reuses
 * the exact same [AwakeUiPreview]/[renderAnnotatedUiPreview]/[saveAwakeUiPreview] pipeline
 * that already produces every other PNG in this repo's preview reports, just pointed at a
 * layout that mirrors `docs/reference/shadcn-previews/button_variants_light.png` and
 * `text-field_states_light.png`'s arrangement instead of a showcase page.
 *
 * Output lands in `build/ui-previews/` like every other preview -- copy into
 * `docs/reference/awake-previews/` when actually publishing a side-by-side comparison,
 * the same manual step already used for `docs/reference/shadcn-previews/`.
 */
import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreview
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewEntry
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewFrame
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewMetadata
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewSample
import io.github.ronjunevaldoz.awake.testing.ui.headlessComponentStateMatrix
import io.github.ronjunevaldoz.awake.testing.ui.renderAnnotatedUiPreviews
import io.github.ronjunevaldoz.awake.testing.ui.saveAwakeUiPreview
import io.github.ronjunevaldoz.awake.testing.ui.verifyAwakeUiPreview
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnAvatarSize
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnTextStyle
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnAlert
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnAvatar
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBreadcrumb
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCheckbox
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCollapsible
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnDialog
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnDropdownMenu
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnInput
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnKbd
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnMuted
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnPopover
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnProgress
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnRadioGroup
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSelect
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSkeleton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSlider
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSpinner
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSwitch
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnTabs
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnTextarea
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnToggle
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnThemeValues
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnAlertVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.UiTextWrap
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.createUiScope
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.offset
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.size
import io.github.ronjunevaldoz.awake.ui.headless.uiScope
import io.github.ronjunevaldoz.awake.ui.headless.width
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.toUiInputState
import kotlin.test.Test
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnDropdownMenuItem as UiDropdownMenuItem

/** Builds a one-off [UiInputState] for a preview frame -- [Input] is a per-session
 * instance now (no longer a global object), so tests construct their own throwaway one. */
private fun parityTestSnapshot(): UiInputState {
    val input = Input()
    input.setPointer(down = false, x = -100f, y = -100f)
    return input.updateSnapshot().toUiInputState()
}

/**
 * The browser reference is captured after CSS transitions have settled.  Keep the product
 * Progress animation enabled, but advance the isolated parity fixture to the equivalent rest
 * state before rasterizing it; a one-frame immediate-mode capture is otherwise an in-flight
 * frame by definition.
 */
private const val PROGRESS_PARITY_SETTLE_FRAMES = 30
private const val PROGRESS_PARITY_SETTLE_DELTA_SECONDS = 1f / 20f

class ShadcnParityScreenshotTest {

    @Test
    fun verifyParityScreenshots() {
        val record = System.getProperty("AWAKE_RECORD_SNAPSHOTS")?.toBoolean() ?: false
        val failures = mutableListOf<Throwable>()
        listOf(
            AwakeButtonVariantsLightPreview,
            AwakeButtonVariantsDarkPreview,
            AwakeTextFieldStatesLightPreview,
            AwakeTextareaStatesLightPreview,
            AwakeSwitchVariantsLightPreview,
            AwakeToggleButtonVariantsLightPreview,
            AwakeBadgeVariantsLightPreview,
            AwakeAlertVariantsLightPreview,
            AwakeRadioGroupLightPreview,
            AwakeProgressLightPreview,
            AwakeAvatarLightPreview,
            AwakeKbdLightPreview,
            AwakeSkeletonLightPreview,
            AwakeTabsLightPreview,
            AwakeBreadcrumbLightPreview,
            AwakeCollapsibleLightPreview,
            AwakeSpinnerLightPreview,
            AwakeSliderMatrixLightPreview,
            AwakeSliderLightPreview,
            AwakeTextareaMatrixLightPreview,
            AwakeToggleMatrixLightPreview,
            AwakeTextFieldMatrixLightPreview,
            AwakeSwitchMatrixLightPreview,
            AwakeCheckboxStatesLightPreview,
            AwakeSelectClosedLightPreview,
            AwakeTooltipTriggerLightPreview,
            AwakeDialogStatesLightPreview,
            AwakeDropdownMenuStatesLightPreview,
            AwakePopoverStatesLightPreview,
        ).forEach { entry ->
            renderAnnotatedUiPreviews(entry).forEach { scene ->
                // Always save to build/ui-previews for report generation
                saveAwakeUiPreview(scene)
                // Verify against the golden baseline, but keep rendering the remaining
                // matrix when one case drifts. A first-failure test left the crop tool with
                // only one component's evidence, which made a whole-catalog parity audit
                // impossible. We still fail the test after all scenes have been materialized.
                runCatching { verifyAwakeUiPreview(scene, record = record) }
                    .onFailure { failures += it }
            }
        }
        if (failures.isNotEmpty()) {
            val summary = failures.joinToString(separator = "\n") { failure ->
                failure.message ?: failure::class.simpleName.orEmpty()
            }
            error("${failures.size} parity scene(s) differ from their golden baseline(s):\n$summary")
        }
    }
}

/**
 * Frame boilerplate every parity preview repeats. Extracted so a light and a dark preview of
 * one component share a single body -- duplicating the whole entry per theme is what kept dark
 * mode uncaptured.
 */
private fun parityFrame(
    metadata: AwakeUiPreviewMetadata,
    dark: Boolean = false,
    body: UiScope.() -> Unit,
): AwakeUiPreviewFrame {
    val theme = shadcnThemeValues(dark = dark)
    val font = UiFonts.default()
    val ui = UiContext()
    ui.beginFrame(metadata.width.toFloat(), metadata.height.toFloat(), parityTestSnapshot())
    ui.pushFont(font)
    ui.pushTheme(theme)
    ui.createUiScope(UiBounds(0f, 0f, metadata.width.toFloat(), metadata.height.toFloat())).body()
    val output = ui.finishFrame()
    return AwakeUiPreviewFrame(
        primitives = output.primitives,
        background = theme.colors.background,
        font = font,
        semantics = output.semantics,
    )
}

/** Shared body for the light and dark button-variant parity previews. */
private fun UiScope.drawParityButtonVariants(metadata: AwakeUiPreviewMetadata) {
    row(
        horizontalArrangement = Arrangement.spacedBy(8f.dp),
        // No height override: the whole point of this capture is to measure the button's
        // own size against the reference, and pinning it here made that impossible.
        modifier = Modifier.height(ShadcnButtonSize.Md.heightDp),
    ) {
        shadcnButton(
            "parity-default",
            "Default",
            variant = ShadcnButtonVariant.Primary,
        )
        shadcnButton(
            "parity-secondary",
            "Secondary",
            variant = ShadcnButtonVariant.Secondary,
        )
        shadcnButton(
            "parity-outline",
            "Outline",
            variant = ShadcnButtonVariant.Outline,
        )
        shadcnButton(
            "parity-ghost",
            "Ghost",
            variant = ShadcnButtonVariant.Ghost,
        )
        shadcnButton(
            "parity-destructive",
            "Destructive",
            variant = ShadcnButtonVariant.Danger,
        )
        shadcnButton(
            "parity-link",
            "Link",
            variant = ShadcnButtonVariant.Link,
        )
    }
}

@AwakeUiPreview(
    id = "awake-button-variants-light",
    title = "Awake Button Variants (light)",
    group = "Shadcn Parity",
    summary = "Matches docs/reference/shadcn-previews/button_variants_light.png's arrangement for a direct side-by-side. " +
        "Canvas is hugged to the row's own bounding box (see docs/reference/ui-validation.md's parity-harness note) -- " +
        "the old 661x132 canvas left ~70% of its area as unused background, which crop_quality (comparedSize/awakeSize) " +
        "read as an unmeasurable framing sliver rather than a fidelity signal. Canvas is exactly the row's own bounding " +
        "box (no margin) -- checked this doesn't clip anything: the same flat-cap-glyph look on bold white-on-dark " +
        "labels ('Default', 'Destructive') is already present in the previous 661x132 golden with 44px of surrounding " +
        "margin, so it's a font-rasterization characteristic at this size, not frame-edge clipping introduced here.",
    width = 543,
    height = 36,
)
internal object AwakeButtonVariantsLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        parityFrame(metadata) { drawParityButtonVariants(metadata) }
}

@AwakeUiPreview(
    id = "awake-button-variants-dark",
    title = "Awake Button Variants (dark)",
    group = "Shadcn Parity",
    summary = "Dark twin of awake-button-variants-light, sharing its body through " +
        "drawParityButtonVariants so the two cannot drift apart. Pairs against the local " +
        "reference app's button-variants case rather than a scraped docs page.",
    width = 543,
    height = 36,
)
internal object AwakeButtonVariantsDarkPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        parityFrame(metadata, dark = true) { drawParityButtonVariants(metadata) }
}

@AwakeUiPreview(
    id = "awake-badge-variants-light",
    title = "Awake Badge Variants (light)",
    group = "Shadcn Parity",
    summary = "Matches docs/reference/shadcn-previews/badge_variants_light.png's arrangement for a direct side-by-side. " +
        "Badges are no longer forced to arbitrary preview-chosen pixel widths -- they self-size from their own " +
        "contentPadding like real shadcn badges do, and the fifth 'Ghost' swatch was dropped to keep this crop " +
        "aligned with the four-state reference capture. The upstream Badge also defines Ghost and Link variants; " +
        "Ghost is supported by Awake, while Link remains a documented API gap because the current pill " +
        "primitive has no text-decoration slot. Same flat-cap-glyph check as the button " +
        "entry above -- the look is pre-existing font rasterization, not clipping from this tight canvas.",
    width = 308,
    height = 22,
)
internal object AwakeBadgeVariantsLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        parityFrame(metadata) { drawParityBadgeVariants(metadata) }
}

@AwakeUiPreview(
    id = "awake-badge-variants-dark",
    title = "Awake Badge Variants (dark)",
    group = "Shadcn Parity",
    summary = "Dark twin of awake-badge-variants-light, sharing its body through " +
        "drawParityBadgeVariants so the two cannot drift apart. Geometry must be identical to the " +
        "light case -- a theme changes colour, not layout -- which makes this pair a check on the " +
        "geometry oracle as much as on the badge.",
    width = 308,
    height = 22,
)
internal object AwakeBadgeVariantsDarkPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        parityFrame(metadata, dark = true) { drawParityBadgeVariants(metadata) }
}

// Use the Headless Row receiver so each pill owns a semantic surface node and its caption is
// composed inside that surface (the legacy Core badge bridge only exposes a bare text node, which
// cannot produce a reliable component crop).
private fun UiScope.drawParityBadgeVariants(metadata: AwakeUiPreviewMetadata) {
    row(
        horizontalArrangement = Arrangement.spacedBy(8f.dp),
        modifier = Modifier.height(metadata.height.toFloat().dp),
    ) {
        shadcnBadge("badge.Default", "Default", variant = ShadcnBadgeVariant.Primary)
        shadcnBadge("badge.Secondary", "Secondary", variant = ShadcnBadgeVariant.Secondary)
        shadcnBadge("badge.Destructive", "Destructive", variant = ShadcnBadgeVariant.Danger)
        shadcnBadge("badge.Outline", "Outline", variant = ShadcnBadgeVariant.Outline)
    }
}

@AwakeUiPreview(
    id = "awake-textfield-states-light",
    title = "Awake TextField States (light)",
    group = "Shadcn Parity",
    summary = "Matches docs/reference/shadcn-previews/text-field_states_light.png's arrangement -- the reference only " +
        "shows three vertically stacked states (placeholder, typed, disabled), so this parity entry mirrors the " +
        "pinned local shadcn-reference-app capture exactly instead of the older two-state migration fixture.",
    width = 256,
    height = 132,
)
internal object AwakeTextFieldStatesLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        parityFrame(metadata) {
            column(
                modifier = Modifier.width(256f.dp).height(132f.dp),
                verticalArrangement = Arrangement.spacedBy(12f.dp),
            ) {
                shadcnInput(
                    "parity-field-1",
                    value = "",
                    placeholder = "Placeholder",
                    modifier = Modifier.width(256f.px).height(36f.px),
                )
                shadcnInput(
                    "parity-field-2",
                    value = "Typed text",
                    modifier = Modifier.width(256f.px).height(36f.px),
                )
                shadcnInput(
                    "parity-field-3",
                    value = "",
                    placeholder = "Disabled",
                    modifier = Modifier.width(256f.px).height(36f.px),
                    enabled = false,
                )
            }
        }
}

@AwakeUiPreview(
    id = "awake-alert-variants-light",
    title = "Awake Alert Variants (light)",
    group = "Shadcn Parity",
    summary = "New component -- Awake had no inline banner before, only alertDialog (a modal). Matches real shadcn's Default/Destructive Alert look.",
    width = 320,
    height = 220,
)
internal object AwakeAlertVariantsLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        parityFrame(metadata) {
            column(
                modifier = Modifier.offset(24f.dp, 24f.dp).width(272f.dp)
                    .height((metadata.height.toFloat() - 48f).dp),
                verticalArrangement = Arrangement.spacedBy(16f.dp),
            ) {
                shadcnAlert(
                    id = "parity-alert-default",
                    title = "You can add components",
                    description = "Use the CLI to add components to your project.",
                    modifier = Modifier.width(272f.px),
                )
                shadcnAlert(
                    id = "parity-alert-destructive",
                    title = "Unable to process your payment.",
                    description = "Please verify your billing information and try again.",
                    modifier = Modifier.width(272f.px),
                    variant = ShadcnAlertVariant.Destructive,
                )
            }
        }
}

@AwakeUiPreview(
    id = "awake-radiogroup-light",
    title = "Awake RadioGroup (light)",
    group = "Shadcn Parity",
    summary = "Public Headless radio recipe: 16dp circular indicators with 8dp inline labels and " +
        "12dp group gaps, matching the pinned shadcn radio-group case. Canvas is the reference's " +
        "own 107x72 content box -- at 160 wide the extra 53px of background was counted as " +
        "uncompared area and dropped parity coverage to 60%.",
    width = 107,
    height = 72,
)
internal object AwakeRadioGroupLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        parityFrame(metadata) {
            column(
                modifier = Modifier.width(metadata.width.toFloat().dp)
                    .height(metadata.height.toFloat().dp),
            ) {
                shadcnRadioGroup(
                    id = "parity-radio",
                    options = listOf("Default", "Comfortable", "Compact"),
                    selectedIndex = 1,
                )
            }
        }
}

@AwakeUiPreview(
    id = "awake-progress-light",
    title = "Awake Progress (light)",
    group = "Shadcn Parity",
    summary = "New component -- non-interactive track+fill, ProgressWidget.kt reuses slider()'s " +
        "painting logic minus the knob/drag handling. Canvas matches the reference's 212x32 box: " +
        "the same two bars (0.25, 0.65) at the same 16dp gap, but the old 260x96 canvas with a " +
        "24px inset left 73% of the image as background the comparison never looked at.",
    width = 212,
    height = 32,
)
internal object AwakeProgressLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame {
        val theme = shadcnThemeValues(dark = false)
        val font = UiFonts.default()
        val ui = UiContext()
        fun composeFrame() {
            ui.pushFont(font)
            ui.pushTheme(theme)
            ui.createUiScope(UiBounds(0f, 0f, 212f, metadata.height.toFloat())).column(
                modifier = Modifier.width(212f.dp)
                    .height(metadata.height.toFloat().dp),
                verticalArrangement = Arrangement.spacedBy(16f.dp),
            ) {
                shadcnProgress(
                    "parity-progress-1",
                    value = 0.25f,
                    modifier = Modifier.width(212f.dp),
                )
                shadcnProgress(
                    "parity-progress-2",
                    value = 0.65f,
                    modifier = Modifier.width(212f.dp),
                )
            }
        }

        repeat(PROGRESS_PARITY_SETTLE_FRAMES) {
            ui.beginFrame(
                metadata.width.toFloat(),
                metadata.height.toFloat(),
                parityTestSnapshot(),
                deltaSeconds = PROGRESS_PARITY_SETTLE_DELTA_SECONDS,
            )
            composeFrame()
            ui.endFrame()
        }
        ui.beginFrame(
            metadata.width.toFloat(),
            metadata.height.toFloat(),
            parityTestSnapshot(),
            deltaSeconds = PROGRESS_PARITY_SETTLE_DELTA_SECONDS,
        )
        composeFrame()
        val output = ui.finishFrame()
        return AwakeUiPreviewFrame(
            primitives = output.primitives,
            background = theme.colors.background,
            font = font,
            semantics = output.semantics,
        )
    }
}

@AwakeUiPreview(
    id = "awake-avatar-light",
    title = "Awake Avatar (light)",
    group = "Shadcn Parity",
    summary = "New component -- AvatarFallback-only (initials on a muted circle); no image-loading pipeline wired into this rasterizer yet, that's the real component's image slot.",
    width = 220,
    height = 96,
)
internal object AwakeAvatarLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        parityFrame(metadata) {
            column(
                modifier = Modifier.offset(24f.dp, 24f.dp).width(180f.dp)
                    .height((metadata.height.toFloat() - 48f).dp),
                verticalArrangement = Arrangement.spacedBy(10f.dp),
            ) {
                row(
                    horizontalArrangement = Arrangement.spacedBy(12f.dp),
                    modifier = Modifier.height(48f.dp),
                ) {
                    shadcnAvatar("avatar.1", "CN")
                    shadcnAvatar("avatar.2", "RV", size = ShadcnAvatarSize.Lg)
                }
            }
        }
}

@AwakeUiPreview(
    id = "awake-kbd-light",
    title = "Awake Kbd (light)",
    group = "Shadcn Parity",
    summary = "New component -- inline key-cap label, same measure-and-draw mechanics as shadcnBadge with a Kbd-specific style.",
    width = 200,
    height = 72,
)
internal object AwakeKbdLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        parityFrame(metadata) {
            column(
                modifier = Modifier.offset(24f.dp, 28f.dp).width(160f.dp)
                    .height((metadata.height.toFloat() - 52f).dp),
                verticalArrangement = Arrangement.spacedBy(10f.dp),
            ) {
                row(
                    horizontalArrangement = Arrangement.spacedBy(6f.dp),
                    modifier = Modifier.height(24f.dp),
                ) {
                    shadcnKbd("kbd.ctrl", "Ctrl")
                    shadcnKbd("kbd.k", "K")
                }
            }
        }
}

@AwakeUiPreview(
    id = "awake-skeleton-light",
    title = "Awake Skeleton (light)",
    group = "Shadcn Parity",
    summary = "New component -- muted placeholder block with a real per-widget opacity pulse (sine wave over elapsed time), not a static gray box.",
    width = 240,
    height = 96,
)
internal object AwakeSkeletonLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        parityFrame(metadata) {
            column(
                modifier = Modifier.offset(24f.dp, 24f.dp).width(192f.dp)
                    .height((metadata.height.toFloat() - 48f).dp),
                verticalArrangement = Arrangement.spacedBy(10f.dp),
            ) {
                shadcnSkeleton(
                    "parity-skeleton-1",
                    modifier = Modifier.width(192f.px).height(16f.px),
                )
                shadcnSkeleton(
                    "parity-skeleton-2",
                    modifier = Modifier.width(140f.px).height(16f.px),
                )
            }
        }
}

@AwakeUiPreview(
    id = "awake-tabs-light",
    title = "Awake Tabs (light)",
    group = "Shadcn Parity",
    summary = "New component -- shadcnTabs composes shadcnButton (Ghost variant) per tab inside a muted track, same reuse-existing-variant approach as shadcnRadioGroup. " +
        "Canvas hugs the tab list's own bounding box instead of the old 320x80 canvas (content was only 164x32).",
    width = 161,
    height = 36,
)
internal object AwakeTabsLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        parityFrame(metadata) {
            column(
                modifier = Modifier.width(161f.dp).height(36f.dp),
            ) {
                shadcnTabs(
                    id = "parity-tabs",
                    tabs = listOf("Account", "Password"),
                    selectedIndex = 0,
                    modifier = Modifier.width(161f.dp),
                )
            }
        }
}

@AwakeUiPreview(
    id = "awake-breadcrumb-light",
    title = "Awake Breadcrumb (light)",
    group = "Shadcn Parity",
    summary = "New component -- muted link trail, last item plain current-page text, separator glyph between.",
    width = 260,
    height = 60,
)
internal object AwakeBreadcrumbLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        parityFrame(metadata) {
            column(
                modifier = Modifier.offset(24f.dp, 24f.dp).width(220f.dp)
                    .height((metadata.height.toFloat() - 48f).dp),
                verticalArrangement = Arrangement.spacedBy(10f.dp),
            ) {
                shadcnBreadcrumb(id = "breadcrumb-parity-test", items = listOf("Home", "Components", "Breadcrumb"))
            }
        }
}

@AwakeUiPreview(
    id = "awake-collapsible-light",
    title = "Awake Collapsible (light)",
    group = "Shadcn Parity",
    summary = "New component -- expand/collapse header with +/- indicator; content only laid out while expanded.",
    width = 328,
    height = 120,
)
internal object AwakeCollapsibleLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        parityFrame(metadata) {
            column(
                modifier = Modifier.offset(24f.dp, 24f.dp).width(280f.dp)
                    .height((metadata.height.toFloat() - 48f).dp),
                verticalArrangement = Arrangement.spacedBy(10f.dp),
            ) {
                shadcnCollapsible(
                    id = "parity-collapsible",
                    title = "Can I use this in my project?",
                    expanded = true,
                ) {
                    // Supporting text is word-wrapped prose. Give it the same content width as
                    // the collapsible so its measured bounds cannot spill past the preview frame.
                    shadcnMuted(
                        "Yes. Free to use for personal and commercial projects.",
                        modifier = Modifier.width(280f.dp),
                    )
                }
            }
        }
}

@AwakeUiPreview(
    id = "awake-spinner-light",
    title = "Awake Spinner (light)",
    group = "Shadcn Parity",
    summary = "New component -- orbiting-dots loader, a real distinct animation (not a static substitute) approximating shadcn's CSS-rotated loader icon, which this engine has no SVG-rotation pipeline to reproduce exactly.",
    width = 120,
    height = 80,
)
internal object AwakeSpinnerLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        parityFrame(metadata) {
            column(
                modifier = Modifier.offset(24f.dp, 24f.dp).width(72f.dp)
                    .height((metadata.height.toFloat() - 48f).dp),
                verticalArrangement = Arrangement.spacedBy(10f.dp),
            ) {
                shadcnSpinner("parity-spinner")
            }
        }
}

@AwakeUiPreview(
    id = "awake-textarea-states-light",
    title = "Awake Textarea States (light)",
    group = "Shadcn Parity",
    summary = "Multi-line text input with manual newline support.",
    width = 320,
    height = 360,
)
internal object AwakeTextareaStatesLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        parityFrame(metadata) {
            column(
                modifier = Modifier.offset(24f.dp, 24f.dp).width(272f.dp)
                    .height((metadata.height.toFloat() - 48f).dp),
                verticalArrangement = Arrangement.spacedBy(16f.dp),
            ) {
                shadcnTextarea(
                    "parity-textarea-1",
                    value = "",
                    placeholder = "Default textarea",
                    modifier = Modifier.width(272f.px),
                )
                shadcnTextarea(
                    "parity-textarea-2",
                    value = "Line 1\nLine 2\nLine 3",
                    modifier = Modifier.width(272f.px),
                )
                shadcnTextarea(
                    "parity-textarea-3",
                    value = "",
                    placeholder = "Disabled textarea",
                    modifier = Modifier.width(272f.px),
                    enabled = false,
                )
            }
        }
}

@AwakeUiPreview(
    id = "awake-switch-variants-light",
    title = "Awake Switch Variants (light)",
    group = "Shadcn Parity",
    summary = "Pill-shaped boolean switch, matches real shadcn Switch. " +
        "docs/reference/shadcn-previews/switch_states_light.png's capture (tools/capture_shadcn_reference.py's " +
        "capture_switch) grabs the bare [role=\"switch\"] element with no label -- this preview used to attach an " +
        "'Airplane Mode' label to each switch, which is real shadcn switch demo content but not what the reference " +
        "screenshot shows, so the two were never comparable content. Dropped the label and laid the two switches " +
        "out side by side (off, on, disabled) to match the reference's own row exactly.",
    width = 128,
    height = 19,
)
internal object AwakeSwitchVariantsLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        parityFrame(metadata) {
            column(
                modifier = Modifier.width(metadata.width.toFloat().dp)
                    .height(metadata.height.toFloat().dp),
            ) {
                row(
                    horizontalArrangement = Arrangement.spacedBy(16f.dp),
                    modifier = Modifier.height(metadata.height.toFloat().dp),
                ) {
                    shadcnSwitch("parity-switch-off", checked = false)
                    shadcnSwitch("parity-switch-on", checked = true)
                    shadcnSwitch("parity-switch-disabled", checked = false, enabled = false)
                }
            }
        }
}

@AwakeUiPreview(
    id = "awake-toggle-button-variants-light",
    title = "Awake Toggle Button Variants (light)",
    group = "Shadcn Parity",
    summary = "Pressable two-state button, matches real shadcn Toggle.",
    width = 240,
    height = 120,
)
internal object AwakeToggleButtonVariantsLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        parityFrame(metadata) {
            column(
                modifier = Modifier.offset(24f.dp, 24f.dp).width(192f.dp)
                    .height((metadata.height.toFloat() - 48f).dp),
                verticalArrangement = Arrangement.spacedBy(10f.dp),
            ) {
                row(
                    horizontalArrangement = Arrangement.spacedBy(10f.dp),
                    modifier = Modifier.height(40f.dp),
                ) {
                    shadcnToggle(
                        "parity-toggle-off",
                        checked = false,
                        label = "B",
                        modifier = Modifier.width(40f.px).height(40f.px),
                    )
                    shadcnToggle(
                        "parity-toggle-on",
                        checked = true,
                        label = "B",
                        modifier = Modifier.width(40f.px).height(40f.px),
                    )
                    shadcnToggle(
                        "parity-toggle-disabled",
                        checked = false,
                        label = "B",
                        modifier = Modifier.width(40f.px).height(40f.px),
                        enabled = false,
                    )
                }
            }
        }
}

@AwakeUiPreview(
    id = "awake-slider-matrix-light",
    title = "Awake Slider Matrix (light)",
    group = "Component Matrix",
    summary = "Shows Slider in all interaction states.",
    width = 320,
    height = 240,
)
internal object AwakeSliderMatrixLightPreview : AwakeUiPreviewEntry {
    override fun renderSamples(metadata: AwakeUiPreviewMetadata): List<AwakeUiPreviewSample> {
        val theme = shadcnThemeValues(dark = false)
        return metadata.headlessComponentStateMatrix(theme = theme) { forcedModifier ->
            shadcnSlider("slider", 0f, 100f, 50f, label = "Slider", modifier = forcedModifier)
        }
    }

    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        error("Use renderSamples")
}

@AwakeUiPreview(
    id = "awake-slider-light",
    title = "Awake Slider (light)",
    group = "Shadcn Parity",
    summary = "Matches docs/reference/shadcn-previews/slider_states_light.png's arrangement for a direct side-by-side. " +
        "Dedicated tight preview, not a crop of the Component Matrix's 'default' sample (that sample lives in a " +
        "320x240 canvas sized for the full 4-state matrix, which left the slider's real 300x20 content as a sliver " +
        "of unused space) -- canvas here hugs the track+knob exactly. Ceiling on how tight this can read is real, " +
        "not a framing bug: SLIDER_KNOB_DIAMETER (ui-headless Slider.kt) is 20dp, real shadcn's thumb is visibly " +
        "smaller (~12px after trim), so even a pixel-perfect crop compares a 20px-tall Awake knob against a 12px " +
        "reference -- report this size delta rather than shrinking the knob to make the number better.",
    width = 300,
    height = 20,
)
internal object AwakeSliderLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        parityFrame(metadata) {
            column(modifier = Modifier.width(300f.dp).height(20f.dp)) {
                shadcnSlider("parity-slider", 0f, 100f, 50f, modifier = Modifier.width(300f.px))
            }
        }
}

@AwakeUiPreview(
    id = "awake-textarea-matrix-light",
    title = "Awake Textarea Matrix (light)",
    group = "Component Matrix",
    summary = "Shows Textarea in all interaction states.",
    width = 320,
    height = 240,
)
internal object AwakeTextareaMatrixLightPreview : AwakeUiPreviewEntry {
    override fun renderSamples(metadata: AwakeUiPreviewMetadata): List<AwakeUiPreviewSample> {
        val theme = shadcnThemeValues(dark = false)
        return metadata.headlessComponentStateMatrix(theme = theme) { forcedModifier ->
            shadcnTextarea(
                "textarea",
                value = "Line 1\nLine 2",
                placeholder = "Type here...",
                modifier = forcedModifier.width(272f.px),
            )
        }
    }

    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        error("Use renderSamples")
}

@AwakeUiPreview(
    id = "awake-toggle-matrix-light",
    title = "Awake Toggle Matrix (light)",
    group = "Component Matrix",
    summary = "Shows Toggle (button style) in all interaction states.",
    width = 240,
    height = 120,
)
internal object AwakeToggleMatrixLightPreview : AwakeUiPreviewEntry {
    override fun renderSamples(metadata: AwakeUiPreviewMetadata): List<AwakeUiPreviewSample> {
        val theme = shadcnThemeValues(dark = false)
        return metadata.headlessComponentStateMatrix(theme = theme) { forcedModifier ->
            row(
                horizontalArrangement = Arrangement.spacedBy(10f.dp),
                modifier = Modifier.height(40f.dp),
            ) {
                shadcnToggle(
                    "toggle-off",
                    checked = false,
                    label = "Off",
                    modifier = forcedModifier.width(60f.px),
                )
                shadcnToggle(
                    "toggle-on",
                    checked = true,
                    label = "On",
                    modifier = forcedModifier.width(60f.px),
                )
            }
        }
    }

    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        error("Use renderSamples")
}

@AwakeUiPreview(
    id = "awake-textfield-matrix-light",
    title = "Awake TextField Matrix (light)",
    group = "Component Matrix",
    summary = "Shows TextField in all interaction states.",
    width = 320,
    height = 160,
)
internal object AwakeTextFieldMatrixLightPreview : AwakeUiPreviewEntry {
    override fun renderSamples(metadata: AwakeUiPreviewMetadata): List<AwakeUiPreviewSample> {
        val theme = shadcnThemeValues(dark = false)
        return metadata.headlessComponentStateMatrix(theme = theme) { forcedModifier ->
            shadcnInput("textfield", value = "", placeholder = "Default", modifier = forcedModifier)
        }
    }

    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        error("Use renderSamples")
}

@AwakeUiPreview(
    id = "awake-switch-matrix-light",
    title = "Awake Switch Matrix (light)",
    group = "Component Matrix",
    summary = "Shows Switch in all interaction states.",
    width = 240,
    height = 120,
)
internal object AwakeSwitchMatrixLightPreview : AwakeUiPreviewEntry {
    override fun renderSamples(metadata: AwakeUiPreviewMetadata): List<AwakeUiPreviewSample> {
        val theme = shadcnThemeValues(dark = false)
        return metadata.headlessComponentStateMatrix(theme = theme) { forcedModifier ->
            row(
                horizontalArrangement = Arrangement.spacedBy(10f.dp),
                modifier = Modifier.height(40f.dp),
            ) {
                shadcnSwitch(
                    "switch-off",
                    checked = false,
                    label = "Off",
                    modifier = forcedModifier,
                )
                shadcnSwitch("switch-on", checked = true, label = "On", modifier = forcedModifier)
            }
        }
    }

    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        error("Use renderSamples")
}

// -- Below: entries closing the "no awake-previews counterpart yet" gap this audit found for
// checkbox/select/tooltip/dialog/dropdown-menu/popover -- these design-system components
// already exist (shadcnCheckbox, shadcnSelect, shadcnTooltip, shadcnDialog,
// shadcnDropdownMenu, shadcnPopover), they just hadn't been wired into this parity test yet.
// Arrangement mirrors each real shadcn reference screenshot in docs/reference/shadcn-previews/.

@AwakeUiPreview(
    id = "awake-checkbox-states-light",
    title = "Awake Checkbox States (light)",
    group = "Shadcn Parity",
    summary = "Matches docs/reference/shadcn-previews/checkbox_states_light.png's arrangement for a direct side-by-side. " +
        "Canvas hugs the three-checkbox row's own bounding box instead of the old 160x70 canvas.",
    width = 80,
    height = 16,
)
internal object AwakeCheckboxStatesLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        parityFrame(metadata) {
            row(
                horizontalArrangement = Arrangement.spacedBy(16f.dp),
                modifier = Modifier.height(metadata.height.toFloat().dp),
            ) {
                shadcnCheckbox(
                    "parity-checkbox-unchecked",
                    checked = false,
                    modifier = Modifier.width(16f.dp).height(16f.dp),
                )
                shadcnCheckbox(
                    "parity-checkbox-checked",
                    checked = true,
                    modifier = Modifier.width(16f.dp).height(16f.dp),
                )
                shadcnCheckbox(
                    "parity-checkbox-disabled",
                    checked = false,
                    enabled = false,
                    modifier = Modifier.width(16f.dp).height(16f.dp),
                )
            }
        }
}

@AwakeUiPreview(
    id = "awake-select-closed-light",
    title = "Awake Select Closed (light)",
    group = "Shadcn Parity",
    summary = "Matches docs/reference/shadcn-previews/select_closed_light.png's arrangement for a direct side-by-side. " +
        "Canvas hugs the trigger's own bounding box instead of the old 220x70 canvas (content was only 172x36). " +
        "Also fixed a real content mismatch found via this pair's diff heatmap: this preview was showing a " +
        "committed value \"Vega\" -- shadcn-compose's own preset name, the exact stale artifact " +
        "docs/reference/ui-validation.md's harness section warns about -- while the real shadcn Select demo (and " +
        "this reference capture) shows no selection yet, just its placeholder text. Options are now real fruit " +
        "names (matching ui.shadcn.com's own demo) with nothing selected, so the trigger renders its placeholder.",
    width = 172,
    height = 36,
)
internal object AwakeSelectClosedLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        parityFrame(metadata) {
            column(
                modifier = Modifier.width(172f.dp).height(36f.dp),
            ) {
                shadcnSelect(
                    id = "parity-select",
                    options = listOf("Apple", "Banana", "Blueberry", "Grapes", "Pineapple"),
                    selectedIndex = null,
                    placeholder = "Select a fruit",
                    modifier = Modifier.width(172f.dp),
                )
            }
        }
}

@AwakeUiPreview(
    id = "awake-tooltip-trigger-light",
    title = "Awake Tooltip Trigger (light)",
    group = "Shadcn Parity",
    summary = "Matches docs/reference/shadcn-previews/tooltip_trigger_light.png's arrangement -- the reference screenshot only shows the trigger, not the (hover-only) tooltip content. " +
        "Canvas hugs the trigger button's own bounding box instead of the old 160x70 canvas (content was only 110x36).",
    width = 114,
    height = 40,
)
internal object AwakeTooltipTriggerLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        parityFrame(metadata) {
            column(
                modifier = Modifier.offset(2f.dp, 2f.dp).width(110f.dp)
                    .height((metadata.height.toFloat() - 2f).dp),
            ) {
                shadcnButton(
                    id = "parity-tooltip-trigger",
                    label = "Hover me",
                    variant = io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant.Outline,
                    modifier = Modifier.width(110f.px).height(36f.px),
                )
            }
        }
}

@AwakeUiPreview(
    id = "awake-dialog-states-light",
    title = "Awake Dialog States (light)",
    group = "Shadcn Parity",
    summary = "Matches docs/reference/shadcn-previews/dialog_states_light.png's arrangement (title/description/action) for a direct side-by-side. " +
        "Canvas hugs the dialog panel itself instead of the old 420x260 canvas (content was only 320x150) -- the " +
        "reference is a full 1280x800 viewport capture, so it never limits this pair's comparedSize; only Awake's " +
        "own oversized canvas did.",
    width = 320,
    height = 174,
)
internal object AwakeDialogStatesLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        parityFrame(metadata) {
            column(modifier = Modifier.width(metadata.width.dp).height(metadata.height.dp)) {
                uiScope().shadcnDialog(
                    id = "parity-dialog",
                    expanded = true,
                    width = Dimension.Fixed(320f.dp),
                    header = {
                        shadcnText(
                            "Edit profile",
                            style = ShadcnTextStyle.Title,
                        )
                    },
                    actions = {
                        shadcnButton(
                            "parity-dialog-save",
                            "Save changes",
                        )
                    },
                ) { _ ->
                    shadcnText(
                        "Make changes to your profile here. Click save when you're done.",
                        style = ShadcnTextStyle.Body,
                        wrap = UiTextWrap.Word,
                    )
                }
            }
        }
}

@AwakeUiPreview(
    id = "awake-dropdown-menu-states-light",
    title = "Awake Dropdown Menu States (light)",
    group = "Shadcn Parity",
    summary = "Matches docs/reference/shadcn-previews/dropdown-menu_states_light.png's arrangement (trigger + open menu) for a direct side-by-side.",
    width = 220,
    height = 220,
)
internal object AwakeDropdownMenuStatesLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        parityFrame(metadata) {
            column(
                modifier = Modifier.offset(24f.dp, 16f.dp).width(160f.dp)
                    .height((metadata.height.toFloat() - 16f).dp),
            ) {
                val triggerSlot = UiBounds(24f, 16f, 80f, 36f)
                uiScope().shadcnDropdownMenu(
                    id = "parity-dropdown",
                    anchorSlot = triggerSlot,
                    expanded = true,
                    items = listOf(
                        UiDropdownMenuItem(label = "My Account"),
                        UiDropdownMenuItem(label = "Edit"),
                        UiDropdownMenuItem(label = "Duplicate"),
                        UiDropdownMenuItem(label = "Delete", destructive = true),
                    ),
                    width = Dimension.Fixed(160f.px),
                )
            }
        }
}

@AwakeUiPreview(
    id = "awake-popover-states-light",
    title = "Awake Popover States (light)",
    group = "Shadcn Parity",
    summary = "Matches docs/reference/shadcn-previews/popover_states_light.png's arrangement (trigger + open panel) for a direct side-by-side.",
    width = 300,
    height = 140,
)
internal object AwakePopoverStatesLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame =
        parityFrame(metadata) {
            column(
                modifier = Modifier.offset(24f.dp, 16f.dp).width(260f.dp)
                    .height((metadata.height.toFloat() - 16f).dp),
            ) {
                val triggerSlot = UiBounds(24f, 16f, 130f, 36f)
                uiScope().shadcnPopover(
                    id = "parity-popover",
                    anchorSlot = triggerSlot,
                    expanded = true,
                    width = Dimension.Fixed(260f.dp),
                ) {
                    shadcnText(
                        "Place content for the popover here.",
                        style = ShadcnTextStyle.Body,
                    )
                }
            }
        }
}
