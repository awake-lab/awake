// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.snapshot

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.ui.CoreUiTheme
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiButtonVariant
import io.github.ronjunevaldoz.awake.ui.UiContext
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.button
import io.github.ronjunevaldoz.awake.ui.checkbox
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.dropdown
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.panel
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.text
import kotlin.test.Test

/**
 * Curated tutorial snapshots for the developer docs pipeline. These are intentionally more
 * narrative than [UiSnapshotTest]: stable, representative scenes we can embed in docs while
 * the prose itself lives under `docs/reference/`.
 */
class UiTutorialDocsTest {

    @Test
    fun buttonVariantsTutorial() {
        val font = BitmapFont()
        Input.setPointer(down = false, x = -100f, y = -100f)
        val ui = UiContext()
        ui.beginFrame(520f, 72f)

        ui.absolute(12f, 16f, font = font, theme = CoreUiTheme)
            .button("primary", 150f, 40f, label = "Primary", variant = UiButtonVariant.Filled, radius = UiShape.md)
        ui.absolute(184f, 16f, font = font, theme = CoreUiTheme)
            .button("outline", 150f, 40f, label = "Outline", variant = UiButtonVariant.Outline, radius = UiShape.md)
        ui.absolute(356f, 16f, font = font, theme = CoreUiTheme)
            .button("ghost", 150f, 40f, label = "Ghost", variant = UiButtonVariant.Ghost, radius = UiShape.md)

        saveUiTutorialSnapshot(
            name = "ui-button-variants",
            title = "Button Variants",
            summary = "One button API can express filled, outline, and ghost treatments while keeping layout and shape stable.",
            primitives = ui.endFrame(),
            width = 520,
            height = 72,
            background = CoreUiTheme.tokens.background
        )
    }

    @Test
    fun shapedPanelTutorial() {
        val font = BitmapFont()
        Input.setPointer(down = false, x = -100f, y = -100f)
        val ui = UiContext()
        ui.beginFrame(300f, 180f)

        ui.absolute(20f, 20f, font = font, theme = CoreUiTheme).panel(
            id = "shape-panel",
            width = Dimension.Fixed(260f.px),
            height = Dimension.Fixed(120f.px),
            style = Style {
                shape(UiShapeSpec.CutCorner(12f.dp))
                border(1f.dp, CoreUiTheme.tokens.border)
                contentPadding(12f.dp)
            },
            clipContent = true
        ) { slot ->
            text("Shaped Panel", color = CoreUiTheme.tokens.mutedForeground)
            context.absolute(slot.x + 12f, slot.y + 44f, font, CoreUiTheme)
                .button("launch", 180f, 36f, label = "Launch Scene", radius = UiShape.md)
        }

        saveUiTutorialSnapshot(
            name = "ui-shaped-panel",
            title = "Shaped Panel Composition",
            summary = "Panels can opt into a custom shape and content clipping, which gives the DSL a reusable way to compose containers and controls.",
            primitives = ui.endFrame(),
            width = 300,
            height = 180,
            background = CoreUiTheme.tokens.background
        )
    }

    @Test
    fun panelControlsTutorial() {
        val font = BitmapFont()
        Input.setPointer(down = false, x = -100f, y = -100f)
        val ui = UiContext()
        ui.beginFrame(280f, 210f)

        val scope = ui.column(x = 20f, y = 20f, width = 240f, font = font, theme = CoreUiTheme)
        scope.panel(
            id = "inspector",
            width = Dimension.FillMax,
            height = Dimension.Fixed(150f.px),
            radius = UiShape.md,
            borderWidth = 1f.dp
        ) {
            text("Controls", color = CoreUiTheme.tokens.mutedForeground)
            dropdown("mode", listOf("Orbit", "Free Fly"), 0, 200f, 24f)
            checkbox("debug", checked = true, width = 200f, height = 24f, label = "Debug Frustum")
            checkbox("grid", checked = false, width = 200f, height = 24f, label = "Show Grid")
        }

        saveUiTutorialSnapshot(
            name = "ui-panel-controls",
            title = "Panel Controls",
            summary = "Panels, dropdowns, and checkboxes already compose into a compact tool surface while staying inside the generic widget layer.",
            primitives = ui.endFrame(),
            width = 280,
            height = 210,
            background = CoreUiTheme.tokens.background
        )
    }
}
