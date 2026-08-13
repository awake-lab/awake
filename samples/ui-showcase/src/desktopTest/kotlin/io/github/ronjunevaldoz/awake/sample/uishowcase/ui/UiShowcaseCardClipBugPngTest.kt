// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewMetadata
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewScene
import io.github.ronjunevaldoz.awake.testing.ui.saveAwakeUiPreview
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCard
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.createUiScope
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.surface
import io.github.ronjunevaldoz.awake.ui.headless.width
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.toUiInputState
import kotlin.test.Test

/** Isolated repro of the reported "Showcase Preview Card" corner-radius clipping bug: a
 * [shadcnSurface] at the slider's max radius (32dp), with a header row of square-cornered
 * children (badge + buttons) pushed right up against the card's edges -- exactly the
 * "LIVE" badge / "Inspect"/"Publish" button layout from the real Theming-page demo, just
 * inlined here so the render is cropped-friendly and isolated from the rest of the page. */
class UiShowcaseCardClipBugPngTest {

    @Test
    fun dumpCornerRadiusClipRepro() {
        saveRepro("debug-card-corner-radius-clip-bug")
    }

    private fun saveRepro(id: String) {
        val state = UiShowcaseRuntimeState()
        val theme = state.showcaseTheme()
        val font = UiFonts.default(cellSize = 12)
        val ui = UiContext()
        val input = Input()
        input.setPointer(down = false, x = -100f, y = -100f)

        ui.beginFrame(420f, 220f, input.updateSnapshot().toUiInputState())
        ui.pushFont(font)
        ui.pushTheme(theme)
        ui.createUiScope(UiBounds(0f, 0f, 420f, 220f)).column {
            surface(
                id = "clip-repro-card",
                style = SurfaceStyle(
                    background = themeValues.colors.muted,
                    foreground = themeValues.colors.foreground,
                    cornerRadius = 32f.dp, // slider's max corner radius
                    contentPadding = io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets(16f.dp),
                ),
                modifier = Modifier.width(420f.dp),
            ) { _ ->
                row(horizontalArrangement = Arrangement.SpaceBetween) {
                    shadcnBadge("badge.live", "LIVE", variant = ShadcnBadgeVariant.Primary)
                    shadcnBadge("badge.danger", "DANGER", variant = ShadcnBadgeVariant.Danger)
                }
                shadcnText("Showcase Preview Card")
                row(
                    horizontalArrangement = Arrangement.spacedBy(10f.dp),
                    modifier = Modifier.height(36f.dp),
                ) {
                    shadcnButton(
                        id = "clip-repro-primary",
                        label = "Inspect",
                        modifier = Modifier.width(100f.dp).height(36f.dp),
                        variant = ShadcnButtonVariant.Primary,
                    )
                    shadcnButton(
                        id = "clip-repro-secondary",
                        label = "Publish",
                        modifier = Modifier.width(100f.dp).height(36f.dp),
                        variant = ShadcnButtonVariant.Outline,
                    )
                }
            }
        }

        val output = ui.finishFrame()
        val scene = AwakeUiPreviewScene(
            metadata = AwakeUiPreviewMetadata(
                id = id,
                title = id,
                group = "Debug",
                summary = "Corner-radius content clip repro at the slider's max radius.",
                width = 420,
                height = 220,
                reportScale = 2,
            ),
            primitives = output.primitives,
            background = theme.colors.background,
            font = font,
            semantics = output.semantics,
        )
        saveAwakeUiPreview(scene)
    }
}
