// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.snapshot

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.ui.DefaultUiTheme
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiButtonVariant
import io.github.ronjunevaldoz.awake.ui.UiContext
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.button
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.dropdown
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.panel
import io.github.ronjunevaldoz.awake.ui.propertyCheckbox
import io.github.ronjunevaldoz.awake.ui.propertyRow
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

        ui.absolute(12f, 16f, font = font, theme = DefaultUiTheme)
            .button("primary", 150f, 40f, label = "Primary", variant = UiButtonVariant.Filled, radius = UiShape.md)
        ui.absolute(184f, 16f, font = font, theme = DefaultUiTheme)
            .button("outline", 150f, 40f, label = "Outline", variant = UiButtonVariant.Outline, radius = UiShape.md)
        ui.absolute(356f, 16f, font = font, theme = DefaultUiTheme)
            .button("ghost", 150f, 40f, label = "Ghost", variant = UiButtonVariant.Ghost, radius = UiShape.md)

        saveUiTutorialSnapshot(
            name = "ui-button-variants",
            title = "Button Variants",
            summary = "One button API can express filled, outline, and ghost treatments while keeping layout and shape stable.",
            primitives = ui.endFrame(),
            width = 520,
            height = 72,
            background = DefaultUiTheme.tokens.background
        )
    }

    @Test
    fun shapedPanelTutorial() {
        val font = BitmapFont()
        Input.setPointer(down = false, x = -100f, y = -100f)
        val ui = UiContext()
        ui.beginFrame(300f, 180f)

        ui.absolute(20f, 20f, font = font, theme = DefaultUiTheme).panel(
            id = "shape-panel",
            width = Dimension.Fixed(260f.px),
            height = Dimension.Fixed(120f.px),
            style = Style {
                shape(UiShapeSpec.CutCorner(12f.dp))
                border(1f.dp, DefaultUiTheme.tokens.border)
                contentPadding(12f.dp)
            },
            clipContent = true
        ) { slot ->
            text("Shaped Panel", color = DefaultUiTheme.tokens.mutedForeground)
            context.absolute(slot.x + 12f, slot.y + 44f, font, DefaultUiTheme)
                .button("launch", 180f, 36f, label = "Launch Scene", radius = UiShape.md)
        }

        saveUiTutorialSnapshot(
            name = "ui-shaped-panel",
            title = "Shaped Panel Composition",
            summary = "Panels can opt into a custom shape and content clipping, which gives the DSL a reusable way to compose containers and controls.",
            primitives = ui.endFrame(),
            width = 300,
            height = 180,
            background = DefaultUiTheme.tokens.background
        )
    }

    @Test
    fun inspectorControlsTutorial() {
        val font = BitmapFont()
        Input.setPointer(down = false, x = -100f, y = -100f)
        val ui = UiContext()
        ui.beginFrame(280f, 210f)

        val scope = ui.column(x = 20f, y = 20f, width = 240f, font = font, theme = DefaultUiTheme)
        scope.panel(
            id = "inspector",
            width = Dimension.FillMax,
            height = Dimension.Fixed(150f.px),
            radius = UiShape.md,
            borderWidth = 1f.dp
        ) {
            text("Inspector", color = DefaultUiTheme.tokens.mutedForeground)
            val modeSlot = propertyRow("Mode", 24f)
            context.absolute(modeSlot.x, modeSlot.y, font, DefaultUiTheme)
                .dropdown("mode", listOf("Orbit", "Free Fly"), 0, modeSlot.width, modeSlot.height)
            propertyCheckbox("debug", checked = true, label = "Debug Frustum", height = 24f)
            propertyCheckbox("grid", checked = false, label = "Show Grid", height = 24f)
        }

        saveUiTutorialSnapshot(
            name = "ui-inspector-controls",
            title = "Inspector Controls",
            summary = "Property rows, dropdowns, and checkboxes already compose into a tooling-style inspector, which is the right proving ground for the first DSL slice.",
            primitives = ui.endFrame(),
            width = 280,
            height = 210,
            background = DefaultUiTheme.tokens.background
        )
    }
}
