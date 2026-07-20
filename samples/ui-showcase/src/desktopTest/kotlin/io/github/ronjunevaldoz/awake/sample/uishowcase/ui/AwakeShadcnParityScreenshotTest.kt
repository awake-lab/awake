// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.input.InputSnapshot
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreview
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewEntry
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewFrame
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewMetadata
import io.github.ronjunevaldoz.awake.testing.ui.renderAnnotatedUiPreview
import io.github.ronjunevaldoz.awake.testing.ui.saveAwakeUiPreview
import io.github.ronjunevaldoz.awake.ui.UiContext
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnAlert
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnAvatar
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnBreadcrumb
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnCollapsible
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnKbd
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnProgress
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnRadioGroup
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnSkeleton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnSpinner
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnTabs
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnTextField
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnAlertVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnTextFieldVariant
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.height
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.ui
import io.github.ronjunevaldoz.awake.ui.width
import kotlin.test.Test

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
/** Builds a one-off [InputSnapshot] for a preview frame -- [Input] is a per-session
 * instance now (no longer a global object), so tests construct their own throwaway one. */
private fun parityTestSnapshot(): InputSnapshot {
    val input = Input()
    input.setPointer(down = false, x = -100f, y = -100f)
    return input.updateSnapshot()
}

class AwakeShadcnParityScreenshotTest {

    @Test
    fun writeParityScreenshots() {
        listOf(AwakeButtonVariantsLightPreview, AwakeTextFieldStatesLightPreview, AwakeBadgeVariantsLightPreview, AwakeAlertVariantsLightPreview, AwakeRadioGroupLightPreview, AwakeProgressLightPreview, AwakeAvatarLightPreview, AwakeKbdLightPreview, AwakeSkeletonLightPreview, AwakeTabsLightPreview, AwakeBreadcrumbLightPreview, AwakeCollapsibleLightPreview, AwakeSpinnerLightPreview).forEach { entry ->
            saveAwakeUiPreview(renderAnnotatedUiPreview(entry))
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
        val theme = awakeShadcnTheme(dark = false)
        val font = UiFonts.default()
        val ui = UiContext()
        ui.beginFrame(metadata.width.toFloat(), metadata.height.toFloat(), parityTestSnapshot())
        ui.ui(x = 30f, y = 45f, width = 600f, font = font, theme = theme, gap = 10f) {
            row(height = 40f.dp, gap = 10f) {
                awakeShadcnButton("parity-default", "Default", modifier = UiModifier().width(90f.px).height(40f.px), variant = AwakeShadcnButtonVariant.Primary)
                awakeShadcnButton("parity-secondary", "Secondary", modifier = UiModifier().width(100f.px).height(40f.px), variant = AwakeShadcnButtonVariant.Secondary)
                awakeShadcnButton("parity-outline", "Outline", modifier = UiModifier().width(90f.px).height(40f.px), variant = AwakeShadcnButtonVariant.Outline)
                awakeShadcnButton("parity-ghost", "Ghost", modifier = UiModifier().width(80f.px).height(40f.px), variant = AwakeShadcnButtonVariant.Ghost)
                awakeShadcnButton("parity-destructive", "Destructive", modifier = UiModifier().width(110f.px).height(40f.px), variant = AwakeShadcnButtonVariant.Danger)
                awakeShadcnButton("parity-link", "Link", modifier = UiModifier().width(60f.px).height(40f.px), variant = AwakeShadcnButtonVariant.Link)
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
    id = "awake-badge-variants-light",
    title = "Awake Badge Variants (light)",
    group = "Shadcn Parity",
    summary = "Matches docs/reference/shadcn-previews/badge_variants_light.png's arrangement for a direct side-by-side.",
    width = 479,
    height = 116
)
internal object AwakeBadgeVariantsLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame {
        val theme = awakeShadcnTheme(dark = false)
        val font = UiFonts.default()
        val ui = UiContext()
        ui.beginFrame(metadata.width.toFloat(), metadata.height.toFloat(), parityTestSnapshot())
        ui.ui(x = 30f, y = 42f, width = 420f, font = font, theme = theme, gap = 10f) {
            row(height = 30f.dp, gap = 10f) {
                awakeShadcnBadge("Default", modifier = UiModifier().width(72f.px).height(30f.px), variant = AwakeShadcnBadgeVariant.Primary)
                awakeShadcnBadge("Secondary", modifier = UiModifier().width(90f.px).height(30f.px), variant = AwakeShadcnBadgeVariant.Secondary)
                awakeShadcnBadge("Destructive", modifier = UiModifier().width(100f.px).height(30f.px), variant = AwakeShadcnBadgeVariant.Danger)
                awakeShadcnBadge("Outline", modifier = UiModifier().width(80f.px).height(30f.px), variant = AwakeShadcnBadgeVariant.Outline)
                awakeShadcnBadge("Ghost", modifier = UiModifier().width(70f.px).height(30f.px), variant = AwakeShadcnBadgeVariant.Ghost)
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
    id = "awake-textfield-states-light",
    title = "Awake TextField States (light)",
    group = "Shadcn Parity",
    summary = "Matches docs/reference/shadcn-previews/text-field_states_light.png's arrangement -- all 5 real shadcn states now exist.",
    width = 296,
    height = 400
)
internal object AwakeTextFieldStatesLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame {
        val theme = awakeShadcnTheme(dark = false)
        val font = UiFonts.default()
        val ui = UiContext()
        ui.beginFrame(metadata.width.toFloat(), metadata.height.toFloat(), parityTestSnapshot())
        ui.ui(x = 24f, y = 24f, width = 248f, font = font, theme = theme, gap = 16f) {
            awakeShadcnTextField("parity-field-1", value = "", placeholder = "Default", modifier = UiModifier().width(248f.px).height(40f.px))
            awakeShadcnTextField("parity-field-2", value = "", placeholder = "Filled", variant = AwakeShadcnTextFieldVariant.Filled, modifier = UiModifier().width(248f.px).height(40f.px))
            awakeShadcnTextField("parity-field-3", value = "", placeholder = "Ghost", variant = AwakeShadcnTextFieldVariant.Ghost, modifier = UiModifier().width(248f.px).height(40f.px))
            awakeShadcnTextField("parity-field-4", value = "Invalid value", modifier = UiModifier().width(248f.px).height(40f.px), isError = true)
            awakeShadcnTextField("parity-field-5", value = "", placeholder = "Disabled", modifier = UiModifier().width(248f.px).height(40f.px), enabled = false)
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
        val theme = awakeShadcnTheme(dark = false)
        val font = UiFonts.default()
        val ui = UiContext()
        ui.beginFrame(metadata.width.toFloat(), metadata.height.toFloat(), parityTestSnapshot())
        ui.ui(x = 24f, y = 24f, width = 272f, font = font, theme = theme, gap = 16f) {
            awakeShadcnAlert(
                id = "parity-alert-default",
                title = "You can add components",
                description = "Use the CLI to add components to your project.",
                width = Dimension.Fixed(272f.px)
            )
            awakeShadcnAlert(
                id = "parity-alert-destructive",
                title = "Unable to process your payment.",
                description = "Please verify your billing information and try again.",
                width = Dimension.Fixed(272f.px),
                variant = AwakeShadcnAlertVariant.Destructive
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
    summary = "New component -- circular checkbox() reused via a Circle shapeSpec, single-select logic composed on top, no new ui-widgets primitive.",
    width = 200,
    height = 108
)
internal object AwakeRadioGroupLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame {
        val theme = awakeShadcnTheme(dark = false)
        val font = UiFonts.default()
        val ui = UiContext()
        ui.beginFrame(metadata.width.toFloat(), metadata.height.toFloat(), parityTestSnapshot())
        ui.ui(x = 24f, y = 24f, width = 160f, font = font, theme = theme, gap = 8f) {
            awakeShadcnRadioGroup(
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
        val theme = awakeShadcnTheme(dark = false)
        val font = UiFonts.default()
        val ui = UiContext()
        ui.beginFrame(metadata.width.toFloat(), metadata.height.toFloat(), parityTestSnapshot())
        ui.ui(x = 24f, y = 24f, width = 212f, font = font, theme = theme, gap = 16f) {
            awakeShadcnProgress("parity-progress-1", value = 0.25f, modifier = UiModifier().width(212f.px))
            awakeShadcnProgress("parity-progress-2", value = 0.65f, modifier = UiModifier().width(212f.px))
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
        val theme = awakeShadcnTheme(dark = false)
        val font = UiFonts.default()
        val ui = UiContext()
        ui.beginFrame(metadata.width.toFloat(), metadata.height.toFloat(), parityTestSnapshot())
        ui.ui(x = 24f, y = 24f, width = 180f, font = font, theme = theme, gap = 10f) {
            row(height = 48f.dp, gap = 12f) {
                awakeShadcnAvatar("CN")
                awakeShadcnAvatar("RV", diameter = 48f.dp)
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
    summary = "New component -- inline key-cap label, same measure-and-draw mechanics as awakeShadcnBadge with a Kbd-specific style.",
    width = 200,
    height = 72
)
internal object AwakeKbdLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame {
        val theme = awakeShadcnTheme(dark = false)
        val font = UiFonts.default()
        val ui = UiContext()
        ui.beginFrame(metadata.width.toFloat(), metadata.height.toFloat(), parityTestSnapshot())
        ui.ui(x = 24f, y = 28f, width = 160f, font = font, theme = theme, gap = 10f) {
            row(height = 24f.dp, gap = 6f) {
                awakeShadcnKbd("Ctrl")
                awakeShadcnKbd("K")
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
        val theme = awakeShadcnTheme(dark = false)
        val font = UiFonts.default()
        val ui = UiContext()
        ui.beginFrame(metadata.width.toFloat(), metadata.height.toFloat(), parityTestSnapshot())
        ui.ui(x = 24f, y = 24f, width = 192f, font = font, theme = theme, gap = 10f) {
            awakeShadcnSkeleton("parity-skeleton-1", modifier = UiModifier().width(192f.px).height(16f.px))
            awakeShadcnSkeleton("parity-skeleton-2", modifier = UiModifier().width(140f.px).height(16f.px))
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
    summary = "New component -- awakeShadcnTabs composes awakeShadcnButton (Ghost variant) per tab inside a muted track, same reuse-existing-variant approach as awakeShadcnRadioGroup.",
    width = 320,
    height = 80
)
internal object AwakeTabsLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame {
        val theme = awakeShadcnTheme(dark = false)
        val font = UiFonts.default()
        val ui = UiContext()
        ui.beginFrame(metadata.width.toFloat(), metadata.height.toFloat(), parityTestSnapshot())
        ui.ui(x = 24f, y = 24f, width = 280f, font = font, theme = theme, gap = 10f) {
            awakeShadcnTabs(
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
        val theme = awakeShadcnTheme(dark = false)
        val font = UiFonts.default()
        val ui = UiContext()
        ui.beginFrame(metadata.width.toFloat(), metadata.height.toFloat(), parityTestSnapshot())
        ui.ui(x = 24f, y = 24f, width = 220f, font = font, theme = theme, gap = 10f) {
            awakeShadcnBreadcrumb(listOf("Home", "Components", "Breadcrumb"))
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
        val theme = awakeShadcnTheme(dark = false)
        val font = UiFonts.default()
        val ui = UiContext()
        ui.beginFrame(metadata.width.toFloat(), metadata.height.toFloat(), parityTestSnapshot())
        ui.ui(x = 24f, y = 24f, width = 220f, font = font, theme = theme, gap = 10f) {
            awakeShadcnCollapsible(id = "parity-collapsible", title = "Can I use this in my project?", expanded = true) {
                awakeShadcnSupportingText("Yes. Free to use for personal and commercial projects.")
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
        val theme = awakeShadcnTheme(dark = false)
        val font = UiFonts.default()
        val ui = UiContext()
        ui.beginFrame(metadata.width.toFloat(), metadata.height.toFloat(), parityTestSnapshot())
        ui.ui(x = 24f, y = 24f, width = 72f, font = font, theme = theme, gap = 10f) {
            awakeShadcnSpinner("parity-spinner")
        }
        return AwakeUiPreviewFrame(
            primitives = ui.endFrame(),
            background = theme.tokens.background,
            font = font,
            semantics = ui.semanticNodes()
        )
    }
}
