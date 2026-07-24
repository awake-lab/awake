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
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreview
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewEntry
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewFrame
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewMetadata
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewSample
import io.github.ronjunevaldoz.awake.testing.ui.componentStateMatrix
import io.github.ronjunevaldoz.awake.testing.ui.renderAnnotatedUiPreviews
import io.github.ronjunevaldoz.awake.testing.ui.saveAwakeUiPreview
import io.github.ronjunevaldoz.awake.testing.ui.verifyAwakeUiPreview
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.column
import io.github.ronjunevaldoz.awake.ui.createColumn
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnAlert
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnAvatar
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBreadcrumb
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCollapsible
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnKbd
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnProgress
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnRadioGroup
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSkeleton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSpinner
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSwitch
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnTabs
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnTextField
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnTextarea
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnToggle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnAlertVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnTextFieldVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ext.row
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.offset
import io.github.ronjunevaldoz.awake.ui.modifier.size
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.toUiInputState
import io.github.ronjunevaldoz.awake.ui.unstyled.input.slider
import io.github.ronjunevaldoz.awake.ui.unstyled.input.selection.switch
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.textField
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.textarea
import io.github.ronjunevaldoz.awake.ui.unstyled.input.toggle.toggle
import kotlin.test.Test
import io.github.ronjunevaldoz.awake.ui.layout.toDimension
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

/** Builds a one-off [UiInputState] for a preview frame -- [Input] is a per-session
 * instance now (no longer a global object), so tests construct their own throwaway one. */
private fun parityTestSnapshot(): UiInputState {
    val input = Input()
    input.setPointer(down = false, x = -100f, y = -100f)
    return input.updateSnapshot().toUiInputState()
}

class ShadcnParityScreenshotTest {

    @Test
    fun verifyParityScreenshots() {
        val record = System.getProperty("AWAKE_RECORD_SNAPSHOTS")?.toBoolean() ?: false
        listOf(
            AwakeButtonVariantsLightPreview,
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
            AwakeTextareaMatrixLightPreview,
            AwakeToggleMatrixLightPreview,
            AwakeTextFieldMatrixLightPreview,
            AwakeSwitchMatrixLightPreview
        ).forEach { entry ->
            renderAnnotatedUiPreviews(entry).forEach { scene ->
                // Always save to build/ui-previews for report generation
                saveAwakeUiPreview(scene)
                // Verify against golden baseline
                verifyAwakeUiPreview(scene, record = record)
            }
        }
    }
}

@AwakeUiPreview(
    id = "awake-button-variants-light",
    title = "Awake Button Variants (light)",
    group = "Shadcn Parity",
    summary = "Matches docs/reference/shadcn-previews/button_variants_light.png's arrangement for a direct side-by-side.",
    width = 661,
    height = 132
)
internal object AwakeButtonVariantsLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame {
        val theme = shadcnTheme(dark = false)
        val font = UiFonts.default()
        val ui = UiContext()
        ui.beginFrame(metadata.width.toFloat(), metadata.height.toFloat(), parityTestSnapshot())
        ui.pushFont(font)
        ui.pushTheme(theme)
        ui.createColumn(
            modifier = Modifier.offset(30f.dp, 45f.dp).width(600f.dp)
                .height((metadata.height.toFloat() - 45f).dp),
            verticalArrangement = Arrangement.spacedBy(10f.dp)
        ).row( horizontalArrangement = Arrangement.spacedBy(10f.dp), modifier = Modifier.copy(heightDimension = 40f.dp.toDimension())) {
                shadcnButton(
                    "parity-default",
                    "Default",
                    modifier = Modifier.width(90f.px).height(40f.px),
                    variant = ShadcnButtonVariant.Primary
                )
                shadcnButton(
                    "parity-secondary",
                    "Secondary",
                    modifier = Modifier.width(100f.px).height(40f.px),
                    variant = ShadcnButtonVariant.Secondary
                )
                shadcnButton(
                    "parity-outline",
                    "Outline",
                    modifier = Modifier.width(90f.px).height(40f.px),
                    variant = ShadcnButtonVariant.Outline
                )
                shadcnButton(
                    "parity-ghost",
                    "Ghost",
                    modifier = Modifier.width(80f.px).height(40f.px),
                    variant = ShadcnButtonVariant.Ghost
                )
                shadcnButton(
                    "parity-destructive",
                    "Destructive",
                    modifier = Modifier.width(110f.px).height(40f.px),
                    variant = ShadcnButtonVariant.Danger
                )
                shadcnButton(
                    "parity-link",
                    "Link",
                    modifier = Modifier.width(60f.px).height(40f.px),
                    variant = ShadcnButtonVariant.Link
                )
            }
        return AwakeUiPreviewFrame(
            primitives = ui.endFrame(),
            background = theme.tokens.background,
            font = font,
            semantics = ui.semanticNodes()
        )
    }
}

@AwakeUiPreview(
    id = "awake-badge-variants-light",
    title = "Awake Badge Variants (light)",
    group = "Shadcn Parity",
    summary = "Matches docs/reference/shadcn-previews/badge_variants_light.png's arrangement for a direct side-by-side.",
    width = 479,
    height = 116
)
internal object AwakeBadgeVariantsLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame {
        val theme = shadcnTheme(dark = false)
        val font = UiFonts.default()
        val ui = UiContext()
        ui.beginFrame(metadata.width.toFloat(), metadata.height.toFloat(), parityTestSnapshot())
        ui.pushFont(font)
        ui.pushTheme(theme)
        ui.createColumn(
            modifier = Modifier.offset(30f.dp, 42f.dp).width(420f.dp)
                .height((metadata.height.toFloat() - 42f).dp),
            verticalArrangement = Arrangement.spacedBy(10f.dp)
        ).row( horizontalArrangement = Arrangement.spacedBy(10f.dp), modifier = Modifier.copy(heightDimension = 30f.dp.toDimension())) {
                shadcnBadge(
                    "Default",
                    modifier = Modifier.width(72f.px).height(30f.px),
                    variant = ShadcnBadgeVariant.Primary
                )
                shadcnBadge(
                    "Secondary",
                    modifier = Modifier.width(90f.px).height(30f.px),
                    variant = ShadcnBadgeVariant.Secondary
                )
                shadcnBadge(
                    "Destructive",
                    modifier = Modifier.width(100f.px).height(30f.px),
                    variant = ShadcnBadgeVariant.Danger
                )
                shadcnBadge(
                    "Outline",
                    modifier = Modifier.width(80f.px).height(30f.px),
                    variant = ShadcnBadgeVariant.Outline
                )
                shadcnBadge(
                    "Ghost",
                    modifier = Modifier.width(70f.px).height(30f.px),
                    variant = ShadcnBadgeVariant.Ghost
                )
            }
        return AwakeUiPreviewFrame(
            primitives = ui.endFrame(),
            background = theme.tokens.background,
            font = font,
            semantics = ui.semanticNodes()
        )
    }
}

@AwakeUiPreview(
    id = "awake-textfield-states-light",
    title = "Awake TextField States (light)",
    group = "Shadcn Parity",
    summary = "Matches docs/reference/shadcn-previews/text-field_states_light.png's arrangement -- all 5 real shadcn states now exist.",
    width = 296,
    height = 400
)
internal object AwakeTextFieldStatesLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame {
        val theme = shadcnTheme(dark = false)
        val font = UiFonts.default()
        val ui = UiContext()
        ui.beginFrame(metadata.width.toFloat(), metadata.height.toFloat(), parityTestSnapshot())
        ui.pushFont(font)
        ui.pushTheme(theme)
        ui.column(
            modifier = Modifier.offset(24f.dp, 24f.dp).width(248f.dp)
                .height((metadata.height.toFloat() - 48f).dp),
            verticalArrangement = Arrangement.spacedBy(16f.dp)
        ) {
            shadcnTextField("parity-field-1", value = "", placeholder = "Default", modifier = Modifier.width(248f.px).height(40f.px))
            shadcnTextField("parity-field-2", value = "", placeholder = "Filled", variant = ShadcnTextFieldVariant.Filled, modifier = Modifier.width(248f.px).height(40f.px))
            shadcnTextField("parity-field-3", value = "", placeholder = "Ghost", variant = ShadcnTextFieldVariant.Ghost, modifier = Modifier.width(248f.px).height(40f.px))
            shadcnTextField("parity-field-4", value = "Invalid value", modifier = Modifier.width(248f.px).height(40f.px), isError = true)
            shadcnTextField("parity-field-5", value = "", placeholder = "Disabled", modifier = Modifier.width(248f.px).height(40f.px), enabled = false)
        }
        return AwakeUiPreviewFrame(
            primitives = ui.endFrame(),
            background = theme.tokens.background,
            font = font,
            semantics = ui.semanticNodes()
        )
    }
}

@AwakeUiPreview(
    id = "awake-alert-variants-light",
    title = "Awake Alert Variants (light)",
    group = "Shadcn Parity",
    summary = "New component -- Awake had no inline banner before, only alertDialog (a modal). Matches real shadcn's Default/Destructive Alert look.",
    width = 320,
    height = 220
)
internal object AwakeAlertVariantsLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame {
        val theme = shadcnTheme(dark = false)
        val font = UiFonts.default()
        val ui = UiContext()
        ui.beginFrame(metadata.width.toFloat(), metadata.height.toFloat(), parityTestSnapshot())
        ui.pushFont(font)
        ui.pushTheme(theme)
        ui.column(
            modifier = Modifier.offset(24f.dp, 24f.dp).width(272f.dp)
                .height((metadata.height.toFloat() - 48f).dp),
            verticalArrangement = Arrangement.spacedBy(16f.dp)
        ) {
            shadcnAlert(
                id = "parity-alert-default",
                title = "You can add components",
                description = "Use the CLI to add components to your project.",
                modifier = Modifier.width(Dimension.Fixed(272f.px))
            )
            shadcnAlert(
                id = "parity-alert-destructive",
                title = "Unable to process your payment.",
                description = "Please verify your billing information and try again.",
                modifier = Modifier.width(Dimension.Fixed(272f.px)),
                variant = ShadcnAlertVariant.Destructive
            )
        }
        return AwakeUiPreviewFrame(
            primitives = ui.endFrame(),
            background = theme.tokens.background,
            font = font,
            semantics = ui.semanticNodes()
        )
    }
}

@AwakeUiPreview(
    id = "awake-radiogroup-light",
    title = "Awake RadioGroup (light)",
    group = "Shadcn Parity",
    summary = "New component -- circular checkbox() reused via a Circle shapeSpec, single-select logic composed on top, no new ui-unstyled primitive.",
    width = 200,
    height = 108
)
internal object AwakeRadioGroupLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame {
        val theme = shadcnTheme(dark = false)
        val font = UiFonts.default()
        val ui = UiContext()
        ui.beginFrame(metadata.width.toFloat(), metadata.height.toFloat(), parityTestSnapshot())
        ui.pushFont(font)
        ui.pushTheme(theme)
        ui.column(
            modifier = Modifier.offset(24f.dp, 24f.dp).width(160f.dp)
                .height((metadata.height.toFloat() - 48f).dp),
            verticalArrangement = Arrangement.spacedBy(8f.dp)
        ) {
            shadcnRadioGroup(
                id = "parity-radio",
                options = listOf("Default", "Comfortable", "Compact"),
                selectedIndex = 1
            )
        }
        return AwakeUiPreviewFrame(
            primitives = ui.endFrame(),
            background = theme.tokens.background,
            font = font,
            semantics = ui.semanticNodes()
        )
    }
}

@AwakeUiPreview(
    id = "awake-progress-light",
    title = "Awake Progress (light)",
    group = "Shadcn Parity",
    summary = "New component -- non-interactive track+fill, ProgressWidget.kt reuses slider()'s painting logic minus the knob/drag handling.",
    width = 260,
    height = 96
)
internal object AwakeProgressLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame {
        val theme = shadcnTheme(dark = false)
        val font = UiFonts.default()
        val ui = UiContext()
        ui.beginFrame(metadata.width.toFloat(), metadata.height.toFloat(), parityTestSnapshot())
        ui.pushFont(font)
        ui.pushTheme(theme)
        ui.column(
            modifier = Modifier.offset(24f.dp, 24f.dp).width(212f.dp)
                .height((metadata.height.toFloat() - 48f).dp),
            verticalArrangement = Arrangement.spacedBy(16f.dp)
        ) {
            shadcnProgress("parity-progress-1", value = 0.25f, modifier = Modifier.width(212f.px))
            shadcnProgress("parity-progress-2", value = 0.65f, modifier = Modifier.width(212f.px))
        }
        return AwakeUiPreviewFrame(
            primitives = ui.endFrame(),
            background = theme.tokens.background,
            font = font,
            semantics = ui.semanticNodes()
        )
    }
}

@AwakeUiPreview(
    id = "awake-avatar-light",
    title = "Awake Avatar (light)",
    group = "Shadcn Parity",
    summary = "New component -- AvatarFallback-only (initials on a muted circle); no image-loading pipeline wired into this rasterizer yet, that's the real component's image slot.",
    width = 220,
    height = 96
)
internal object AwakeAvatarLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame {
        val theme = shadcnTheme(dark = false)
        val font = UiFonts.default()
        val ui = UiContext()
        ui.beginFrame(metadata.width.toFloat(), metadata.height.toFloat(), parityTestSnapshot())
        ui.pushFont(font)
        ui.pushTheme(theme)
        ui.column(
            modifier = Modifier.offset(24f.dp, 24f.dp).width(180f.dp)
                .height((metadata.height.toFloat() - 48f).dp),
            verticalArrangement = Arrangement.spacedBy(10f.dp)
        ) {
            row( horizontalArrangement = Arrangement.spacedBy(12f.dp), modifier = Modifier.copy(heightDimension = 48f.dp.toDimension())) {
                shadcnAvatar("CN")
                shadcnAvatar("RV", modifier = Modifier.size(48f.dp, 48f.dp))
            }
        }
        return AwakeUiPreviewFrame(
            primitives = ui.endFrame(),
            background = theme.tokens.background,
            font = font,
            semantics = ui.semanticNodes()
        )
    }
}

@AwakeUiPreview(
    id = "awake-kbd-light",
    title = "Awake Kbd (light)",
    group = "Shadcn Parity",
    summary = "New component -- inline key-cap label, same measure-and-draw mechanics as shadcnBadge with a Kbd-specific style.",
    width = 200,
    height = 72
)
internal object AwakeKbdLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame {
        val theme = shadcnTheme(dark = false)
        val font = UiFonts.default()
        val ui = UiContext()
        ui.beginFrame(metadata.width.toFloat(), metadata.height.toFloat(), parityTestSnapshot())
        ui.pushFont(font)
        ui.pushTheme(theme)
        ui.column(
            modifier = Modifier.offset(24f.dp, 28f.dp).width(160f.dp)
                .height((metadata.height.toFloat() - 52f).dp),
            verticalArrangement = Arrangement.spacedBy(10f.dp)
        ) {
            row( horizontalArrangement = Arrangement.spacedBy(6f.dp), modifier = Modifier.copy(heightDimension = 24f.dp.toDimension())) {
                shadcnKbd("Ctrl")
                shadcnKbd("K")
            }
        }
        return AwakeUiPreviewFrame(
            primitives = ui.endFrame(),
            background = theme.tokens.background,
            font = font,
            semantics = ui.semanticNodes()
        )
    }
}

@AwakeUiPreview(
    id = "awake-skeleton-light",
    title = "Awake Skeleton (light)",
    group = "Shadcn Parity",
    summary = "New component -- muted placeholder block with a real per-widget opacity pulse (sine wave over elapsed time), not a static gray box.",
    width = 240,
    height = 96
)
internal object AwakeSkeletonLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame {
        val theme = shadcnTheme(dark = false)
        val font = UiFonts.default()
        val ui = UiContext()
        ui.beginFrame(metadata.width.toFloat(), metadata.height.toFloat(), parityTestSnapshot())
        ui.pushFont(font)
        ui.pushTheme(theme)
        ui.column(
            modifier = Modifier.offset(24f.dp, 24f.dp).width(192f.dp)
                .height((metadata.height.toFloat() - 48f).dp),
            verticalArrangement = Arrangement.spacedBy(10f.dp)
        ) {
            shadcnSkeleton("parity-skeleton-1", modifier = Modifier.width(192f.px).height(16f.px))
            shadcnSkeleton("parity-skeleton-2", modifier = Modifier.width(140f.px).height(16f.px))
        }
        return AwakeUiPreviewFrame(
            primitives = ui.endFrame(),
            background = theme.tokens.background,
            font = font,
            semantics = ui.semanticNodes()
        )
    }
}

@AwakeUiPreview(
    id = "awake-tabs-light",
    title = "Awake Tabs (light)",
    group = "Shadcn Parity",
    summary = "New component -- shadcnTabs composes shadcnButton (Ghost variant) per tab inside a muted track, same reuse-existing-variant approach as shadcnRadioGroup.",
    width = 320,
    height = 80
)
internal object AwakeTabsLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame {
        val theme = shadcnTheme(dark = false)
        val font = UiFonts.default()
        val ui = UiContext()
        ui.beginFrame(metadata.width.toFloat(), metadata.height.toFloat(), parityTestSnapshot())
        ui.pushFont(font)
        ui.pushTheme(theme)
        ui.column(
            modifier = Modifier.offset(24f.dp, 24f.dp).width(280f.dp)
                .height((metadata.height.toFloat() - 48f).dp),
            verticalArrangement = Arrangement.spacedBy(10f.dp)
        ) {
            shadcnTabs(
                id = "parity-tabs",
                tabs = listOf("Account", "Password"),
                selectedIndex = 0
            )
        }
        return AwakeUiPreviewFrame(
            primitives = ui.endFrame(),
            background = theme.tokens.background,
            font = font,
            semantics = ui.semanticNodes()
        )
    }
}

@AwakeUiPreview(
    id = "awake-breadcrumb-light",
    title = "Awake Breadcrumb (light)",
    group = "Shadcn Parity",
    summary = "New component -- muted link trail, last item plain current-page text, separator glyph between.",
    width = 260,
    height = 60
)
internal object AwakeBreadcrumbLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame {
        val theme = shadcnTheme(dark = false)
        val font = UiFonts.default()
        val ui = UiContext()
        ui.beginFrame(metadata.width.toFloat(), metadata.height.toFloat(), parityTestSnapshot())
        ui.pushFont(font)
        ui.pushTheme(theme)
        ui.column(
            modifier = Modifier.offset(24f.dp, 24f.dp).width(220f.dp)
                .height((metadata.height.toFloat() - 48f).dp),
            verticalArrangement = Arrangement.spacedBy(10f.dp)
        ) {
            shadcnBreadcrumb(listOf("Home", "Components", "Breadcrumb"))
        }
        return AwakeUiPreviewFrame(
            primitives = ui.endFrame(),
            background = theme.tokens.background,
            font = font,
            semantics = ui.semanticNodes()
        )
    }
}

@AwakeUiPreview(
    id = "awake-collapsible-light",
    title = "Awake Collapsible (light)",
    group = "Shadcn Parity",
    summary = "New component -- expand/collapse header with +/- indicator; content only laid out while expanded.",
    width = 260,
    height = 120
)
internal object AwakeCollapsibleLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame {
        val theme = shadcnTheme(dark = false)
        val font = UiFonts.default()
        val ui = UiContext()
        ui.beginFrame(metadata.width.toFloat(), metadata.height.toFloat(), parityTestSnapshot())
        ui.pushFont(font)
        ui.pushTheme(theme)
        ui.column(
            modifier = Modifier.offset(24f.dp, 24f.dp).width(220f.dp)
                .height((metadata.height.toFloat() - 48f).dp),
            verticalArrangement = Arrangement.spacedBy(10f.dp)
        ) {
            shadcnCollapsible(id = "parity-collapsible", title = "Can I use this in my project?", expanded = true) {
                shadcnSupportingText("Yes. Free to use for personal and commercial projects.")
            }
        }
        return AwakeUiPreviewFrame(
            primitives = ui.endFrame(),
            background = theme.tokens.background,
            font = font,
            semantics = ui.semanticNodes()
        )
    }
}

@AwakeUiPreview(
    id = "awake-spinner-light",
    title = "Awake Spinner (light)",
    group = "Shadcn Parity",
    summary = "New component -- orbiting-dots loader, a real distinct animation (not a static substitute) approximating shadcn's CSS-rotated loader icon, which this engine has no SVG-rotation pipeline to reproduce exactly.",
    width = 120,
    height = 80
)
internal object AwakeSpinnerLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame {
        val theme = shadcnTheme(dark = false)
        val font = UiFonts.default()
        val ui = UiContext()
        ui.beginFrame(metadata.width.toFloat(), metadata.height.toFloat(), parityTestSnapshot())
        ui.pushFont(font)
        ui.pushTheme(theme)
        ui.column(
            modifier = Modifier.offset(24f.dp, 24f.dp).width(72f.dp)
                .height((metadata.height.toFloat() - 48f).dp),
            verticalArrangement = Arrangement.spacedBy(10f.dp)
        ) {
            shadcnSpinner("parity-spinner")
        }
        return AwakeUiPreviewFrame(
            primitives = ui.endFrame(),
            background = theme.tokens.background,
            font = font,
            semantics = ui.semanticNodes()
        )
    }
}

@AwakeUiPreview(
    id = "awake-textarea-states-light",
    title = "Awake Textarea States (light)",
    group = "Shadcn Parity",
    summary = "Multi-line text input with manual newline support.",
    width = 320,
    height = 360
)
internal object AwakeTextareaStatesLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame {
        val theme = shadcnTheme(dark = false)
        val font = UiFonts.default()
        val ui = UiContext()
        ui.beginFrame(metadata.width.toFloat(), metadata.height.toFloat(), parityTestSnapshot())
        ui.pushFont(font)
        ui.pushTheme(theme)
        ui.column(
            modifier = Modifier.offset(24f.dp, 24f.dp).width(272f.dp)
                .height((metadata.height.toFloat() - 48f).dp),
            verticalArrangement = Arrangement.spacedBy(16f.dp)
        ) {
            shadcnTextarea("parity-textarea-1", value = "", placeholder = "Default textarea", modifier = Modifier.width(272f.px))
            shadcnTextarea("parity-textarea-2", value = "Line 1\nLine 2\nLine 3", modifier = Modifier.width(272f.px))
            shadcnTextarea("parity-textarea-3", value = "", placeholder = "Disabled textarea", modifier = Modifier.width(272f.px), enabled = false)
        }
        return AwakeUiPreviewFrame(
            primitives = ui.endFrame(),
            background = theme.tokens.background,
            font = font,
            semantics = ui.semanticNodes()
        )
    }
}

@AwakeUiPreview(
    id = "awake-switch-variants-light",
    title = "Awake Switch Variants (light)",
    group = "Shadcn Parity",
    summary = "Pill-shaped boolean switch, matches real shadcn Switch.",
    width = 200,
    height = 100
)
internal object AwakeSwitchVariantsLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame {
        val theme = shadcnTheme(dark = false)
        val font = UiFonts.default()
        val ui = UiContext()
        ui.beginFrame(metadata.width.toFloat(), metadata.height.toFloat(), parityTestSnapshot())
        ui.pushFont(font)
        ui.pushTheme(theme)
        ui.column(
            modifier = Modifier.offset(24f.dp, 24f.dp).width(152f.dp)
                .height((metadata.height.toFloat() - 48f).dp),
            verticalArrangement = Arrangement.spacedBy(12f.dp)
        ) {
            shadcnSwitch("parity-switch-off", checked = false, label = "Airplane Mode")
            shadcnSwitch("parity-switch-on", checked = true, label = "Airplane Mode")
        }
        return AwakeUiPreviewFrame(
            primitives = ui.endFrame(),
            background = theme.tokens.background,
            font = font,
            semantics = ui.semanticNodes()
        )
    }
}

@AwakeUiPreview(
    id = "awake-toggle-button-variants-light",
    title = "Awake Toggle Button Variants (light)",
    group = "Shadcn Parity",
    summary = "Pressable two-state button, matches real shadcn Toggle.",
    width = 240,
    height = 120
)
internal object AwakeToggleButtonVariantsLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame {
        val theme = shadcnTheme(dark = false)
        val font = UiFonts.default()
        val ui = UiContext()
        ui.beginFrame(metadata.width.toFloat(), metadata.height.toFloat(), parityTestSnapshot())
        ui.pushFont(font)
        ui.pushTheme(theme)
        ui.column(
            modifier = Modifier.offset(24f.dp, 24f.dp).width(192f.dp)
                .height((metadata.height.toFloat() - 48f).dp),
            verticalArrangement = Arrangement.spacedBy(10f.dp)
        ) {
            row( horizontalArrangement = Arrangement.spacedBy(10f.dp), modifier = Modifier.copy(heightDimension = 40f.dp.toDimension())) {
                shadcnToggle("parity-toggle-off", checked = false, label = "B", modifier = Modifier.width(40f.px).height(40f.px))
                shadcnToggle("parity-toggle-on", checked = true, label = "B", modifier = Modifier.width(40f.px).height(40f.px))
                shadcnToggle("parity-toggle-disabled", checked = false, label = "B", modifier = Modifier.width(40f.px).height(40f.px), enabled = false)
            }
        }
        return AwakeUiPreviewFrame(
            primitives = ui.endFrame(),
            background = theme.tokens.background,
            font = font,
            semantics = ui.semanticNodes()
        )
    }
}

@AwakeUiPreview(
    id = "awake-slider-matrix-light",
    title = "Awake Slider Matrix (light)",
    group = "Component Matrix",
    summary = "Shows Slider in all interaction states.",
    width = 320,
    height = 240
)
internal object AwakeSliderMatrixLightPreview : AwakeUiPreviewEntry {
    override fun renderSamples(metadata: AwakeUiPreviewMetadata): List<AwakeUiPreviewSample> {
        val theme = shadcnTheme(dark = false)
        return metadata.componentStateMatrix(theme = theme) { forcedModifier ->
            slider("slider", 0f, 100f, 50f, label = "Slider", modifier = forcedModifier)
        }
    }

    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame = error("Use renderSamples")
}

@AwakeUiPreview(
    id = "awake-textarea-matrix-light",
    title = "Awake Textarea Matrix (light)",
    group = "Component Matrix",
    summary = "Shows Textarea in all interaction states.",
    width = 320,
    height = 240
)
internal object AwakeTextareaMatrixLightPreview : AwakeUiPreviewEntry {
    override fun renderSamples(metadata: AwakeUiPreviewMetadata): List<AwakeUiPreviewSample> {
        val theme = shadcnTheme(dark = false)
        return metadata.componentStateMatrix(theme = theme) { forcedModifier ->
            textarea("textarea", value = "Line 1\nLine 2", placeholder = "Type here...", modifier = forcedModifier.width(272f.px))
        }
    }
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame = error("Use renderSamples")
}

@AwakeUiPreview(
    id = "awake-toggle-matrix-light",
    title = "Awake Toggle Matrix (light)",
    group = "Component Matrix",
    summary = "Shows Toggle (button style) in all interaction states.",
    width = 240,
    height = 120
)
internal object AwakeToggleMatrixLightPreview : AwakeUiPreviewEntry {
    override fun renderSamples(metadata: AwakeUiPreviewMetadata): List<AwakeUiPreviewSample> {
        val theme = shadcnTheme(dark = false)
        return metadata.componentStateMatrix(theme = theme) { forcedModifier ->
            row( horizontalArrangement = Arrangement.spacedBy(10f.dp), modifier = Modifier.copy(heightDimension = 40f.dp.toDimension())) {
                toggle("toggle-off", checked = false, label = "Off", modifier = forcedModifier.width(Dimension.Fixed(60f.px)))
                toggle("toggle-on", checked = true, label = "On", modifier = forcedModifier.width(Dimension.Fixed(60f.px)))
            }
        }
    }
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame = error("Use renderSamples")
}

@AwakeUiPreview(
    id = "awake-textfield-matrix-light",
    title = "Awake TextField Matrix (light)",
    group = "Component Matrix",
    summary = "Shows TextField in all interaction states.",
    width = 320,
    height = 160
)
internal object AwakeTextFieldMatrixLightPreview : AwakeUiPreviewEntry {
    override fun renderSamples(metadata: AwakeUiPreviewMetadata): List<AwakeUiPreviewSample> {
        val theme = shadcnTheme(dark = false)
        return metadata.componentStateMatrix(theme = theme) { forcedModifier ->
            textField("textfield", value = "", placeholder = "Default", modifier = forcedModifier)
        }
    }
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame = error("Use renderSamples")
}

@AwakeUiPreview(
    id = "awake-switch-matrix-light",
    title = "Awake Switch Matrix (light)",
    group = "Component Matrix",
    summary = "Shows Switch in all interaction states.",
    width = 240,
    height = 120
)
internal object AwakeSwitchMatrixLightPreview : AwakeUiPreviewEntry {
    override fun renderSamples(metadata: AwakeUiPreviewMetadata): List<AwakeUiPreviewSample> {
        val theme = shadcnTheme(dark = false)
        return metadata.componentStateMatrix(theme = theme) { forcedModifier ->
            row( horizontalArrangement = Arrangement.spacedBy(10f.dp), modifier = Modifier.copy(heightDimension = 40f.dp.toDimension())) {
                switch("switch-off", checked = false, label = "Off", modifier = forcedModifier)
                switch("switch-on", checked = true, label = "On", modifier = forcedModifier)
            }
        }
    }
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame = error("Use renderSamples")
}
