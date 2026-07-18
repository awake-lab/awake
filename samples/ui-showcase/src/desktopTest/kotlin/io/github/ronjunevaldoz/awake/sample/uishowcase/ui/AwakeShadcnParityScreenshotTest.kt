// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreview
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewEntry
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewFrame
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewMetadata
import io.github.ronjunevaldoz.awake.testing.ui.renderAnnotatedUiPreview
import io.github.ronjunevaldoz.awake.testing.ui.saveAwakeUiPreview
import io.github.ronjunevaldoz.awake.ui.UiContext
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnTextField
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
class AwakeShadcnParityScreenshotTest {

    @Test
    fun writeParityScreenshots() {
        Input.setPointer(down = false, x = -100f, y = -100f)
        listOf(AwakeButtonVariantsLightPreview, AwakeTextFieldStatesLightPreview).forEach { entry ->
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
        ui.beginFrame(metadata.width.toFloat(), metadata.height.toFloat())
        ui.ui(x = 30f, y = 45f, width = 600f, font = font, theme = theme, gap = 10f) {
            row(height = 40f.dp, gap = 10f) {
                awakeShadcnButton("parity-default", "Default", modifier = UiModifier().width(90f.px).height(40f.px), variant = AwakeShadcnButtonVariant.Primary)
                awakeShadcnButton("parity-secondary", "Secondary", modifier = UiModifier().width(100f.px).height(40f.px), variant = AwakeShadcnButtonVariant.Secondary)
                awakeShadcnButton("parity-outline", "Outline", modifier = UiModifier().width(90f.px).height(40f.px), variant = AwakeShadcnButtonVariant.Outline)
                awakeShadcnButton("parity-ghost", "Ghost", modifier = UiModifier().width(80f.px).height(40f.px), variant = AwakeShadcnButtonVariant.Ghost)
                awakeShadcnButton("parity-destructive", "Destructive", modifier = UiModifier().width(110f.px).height(40f.px), variant = AwakeShadcnButtonVariant.Danger)
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
    summary = "Matches docs/reference/shadcn-previews/text-field_states_light.png's arrangement. Still no Filled/Ghost variant, but error and disabled states now exist.",
    width = 296,
    height = 322
)
internal object AwakeTextFieldStatesLightPreview : AwakeUiPreviewEntry {
    override fun render(metadata: AwakeUiPreviewMetadata): AwakeUiPreviewFrame {
        val theme = awakeShadcnTheme(dark = false)
        val font = UiFonts.default()
        val ui = UiContext()
        ui.beginFrame(metadata.width.toFloat(), metadata.height.toFloat())
        ui.ui(x = 24f, y = 24f, width = 248f, font = font, theme = theme, gap = 16f) {
            awakeShadcnTextField("parity-field-1", value = "", placeholder = "Default", modifier = UiModifier().width(248f.px).height(40f.px))
            awakeShadcnTextField("parity-field-2", value = "", placeholder = "No Filled/Ghost variant yet", modifier = UiModifier().width(248f.px).height(40f.px))
            awakeShadcnTextField("parity-field-3", value = "Invalid value", modifier = UiModifier().width(248f.px).height(40f.px), isError = true)
            awakeShadcnTextField("parity-field-4", value = "", placeholder = "Disabled", modifier = UiModifier().width(248f.px).height(40f.px), enabled = false)
        }
        return AwakeUiPreviewFrame(
            primitives = ui.endFrame(),
            background = theme.tokens.background,
            font = font,
            semantics = ui.semanticNodes()
        )
    }
}
