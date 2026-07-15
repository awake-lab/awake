// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.snapshot

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.ui.CoreUiTheme
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiAlignment
import io.github.ronjunevaldoz.awake.ui.UiButtonVariant
import io.github.ronjunevaldoz.awake.ui.UiContext
import io.github.ronjunevaldoz.awake.ui.UiImageVector
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.UiTextWrap
import io.github.ronjunevaldoz.awake.ui.align
import io.github.ronjunevaldoz.awake.ui.button
import io.github.ronjunevaldoz.awake.ui.checkbox
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.dropdown
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.icon
import io.github.ronjunevaldoz.awake.ui.offset
import io.github.ronjunevaldoz.awake.ui.panel
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.size
import io.github.ronjunevaldoz.awake.ui.text
import io.github.ronjunevaldoz.awake.ui.ui
import io.github.ronjunevaldoz.awake.ui.uiImageVector
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnSurface
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
            background = CoreUiTheme.tokens.background,
            font = font
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
            background = CoreUiTheme.tokens.background,
            font = font
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
            background = CoreUiTheme.tokens.background,
            font = font
        )
    }

    @Test
    fun roundedClipAndVectorTutorial() {
        val font = BitmapFont()
        Input.setPointer(down = false, x = -100f, y = -100f)
        val ui = UiContext()
        ui.beginFrame(340f, 220f)

        val panelScope = ui.absolute(24f, 24f, font = font, theme = CoreUiTheme)
        panelScope.panel(
            id = "vector-showcase",
            width = Dimension.Fixed(292f.px),
            height = Dimension.Fixed(164f.px),
            style = Style {
                shape(UiShapeSpec.CutCorner(18f.dp))
                background(floatArrayOf(0.13f, 0.16f, 0.24f, 1f))
                border(2f.dp, floatArrayOf(0.38f, 0.58f, 0.94f, 1f))
                contentPadding(14f.dp)
            },
            clipContent = true
        ) { slot ->
            text("Rounded + Clip + Vector", color = floatArrayOf(0.94f, 0.96f, 1f, 1f))
            text(
                "The icon intentionally overflows and gets clipped by the cut-corner shell.",
                color = CoreUiTheme.tokens.mutedForeground,
                wrap = UiTextWrap.Word
            )

            context.box(
                x = slot.x + 16f,
                y = slot.y + 56f,
                width = slot.width - 32f,
                height = 78f,
                font = font,
                theme = CoreUiTheme,
                contentAlignment = UiAlignment.Center
            ).apply {
                panel(
                    id = "chip",
                    width = Dimension.Fixed(180f.px),
                    height = Dimension.Fixed(56f.px),
                    style = Style {
                        shape(28f.dp)
                        background(floatArrayOf(0.2f, 0.24f, 0.36f, 1f))
                        border(1f.dp, floatArrayOf(0.56f, 0.72f, 1f, 1f))
                    },
                    modifier = UiModifier().align(UiAlignment.Center)
                ) {
                    text("ICON CHIP", color = floatArrayOf(0.95f, 0.97f, 1f, 1f))
                }
                icon(
                    imageVector = tutorialSparkleIcon,
                    modifier = UiModifier()
                        .align(UiAlignment.CenterEnd)
                        .offset(x = 18f.dp)
                        .size(88f.dp, 88f.dp),
                    tint = floatArrayOf(0.68f, 0.84f, 1f, 0.95f)
                )
            }
        }

        saveUiTutorialSnapshot(
            name = "ui-rounded-clip-vector",
            title = "Rounded Clip And Vector",
            summary = "Rounded surfaces, colored borders, Box-style alignment, and vector-path icons all compose through the same widget surface, with shape clipping trimming intentional overflow.",
            primitives = ui.endFrame(),
            width = 340,
            height = 220,
            background = CoreUiTheme.tokens.background,
            font = font
        )
    }

    @Test
    fun awakeShadcnShowcaseTutorial() {
        val font = BitmapFont()
        Input.setPointer(down = false, x = -100f, y = -100f)
        val ui = UiContext()
        ui.beginFrame(420f, 250f)

        ui.ui(x = 18f, y = 18f, width = 384f, font = font, theme = AwakeShadcnTheme, gap = 10f) {
            awakeShadcnSurface(
                id = "shadcn-showcase",
                width = Dimension.Fixed(384f.px),
                height = Dimension.Fixed(214f.px)
            ) {
                text("Awake Shadcn")
                text(
                    "Owned components layered over Awake widgets.",
                    style = Style { foreground(AwakeShadcnTheme.tokens.mutedForeground) },
                    wrap = UiTextWrap.Word
                )
                spacer(6f)
                row(height = 36f, gap = 8f) {
                    awakeShadcnButton("showcase-doc-primary", 112f, 36f, "Primary", variant = AwakeShadcnButtonVariant.Primary)
                    awakeShadcnButton("showcase-doc-secondary", 112f, 36f, "Secondary", variant = AwakeShadcnButtonVariant.Secondary)
                    awakeShadcnButton("showcase-doc-outline", 112f, 36f, "Outline", variant = AwakeShadcnButtonVariant.Outline)
                }
                row(height = 28f, gap = 8f) {
                    awakeShadcnBadge("LIVE", variant = AwakeShadcnBadgeVariant.Primary)
                    awakeShadcnBadge("NEUTRAL", variant = AwakeShadcnBadgeVariant.Secondary)
                    awakeShadcnBadge("BETA", variant = AwakeShadcnBadgeVariant.Outline)
                    awakeShadcnBadge("RISK", variant = AwakeShadcnBadgeVariant.Danger)
                }
                spacer(6f)
                awakeShadcnSurface(
                    id = "shadcn-subcard",
                    height = Dimension.WrapContent,
                    style = Style {
                        background(AwakeShadcnTheme.tokens.background)
                    }
                ) {
                    text("Preview Card")
                    text(
                        "A nested card keeps the same tokens and border language.",
                        style = Style { foreground(AwakeShadcnTheme.tokens.mutedForeground) },
                        wrap = UiTextWrap.Word
                    )
                    spacer(4f)
                    row(height = 32f, gap = 8f) {
                        awakeShadcnButton("showcase-doc-ghost", 96f, 32f, "Ghost", variant = AwakeShadcnButtonVariant.Ghost)
                        awakeShadcnButton("showcase-doc-danger", 96f, 32f, "Danger", variant = AwakeShadcnButtonVariant.Danger)
                    }
                }
            }
        }

        saveUiTutorialSnapshot(
            name = "ui-awake-shadcn-showcase",
            title = "Awake Shadcn Showcase",
            summary = "The starter design-system layer can already express a recognizable shadcn-style component set while staying fully inside Awake's owned widget stack.",
            primitives = ui.endFrame(),
            width = 420,
            height = 250,
            background = AwakeShadcnTheme.tokens.background,
            font = font
        )
    }

    private companion object {
        val tutorialSparkleIcon: UiImageVector = uiImageVector(
            defaultWidth = 24f.dp,
            defaultHeight = 24f.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ) {
            path {
                moveTo(12f, 1f)
                lineTo(15f, 8.5f)
                lineTo(23f, 12f)
                lineTo(15f, 15.5f)
                lineTo(12f, 23f)
                lineTo(9f, 15.5f)
                lineTo(1f, 12f)
                lineTo(9f, 8.5f)
                close()
            }
            path {
                moveTo(17f, 2f)
                lineTo(18f, 4.5f)
                lineTo(20.5f, 5.5f)
                lineTo(18f, 6.5f)
                lineTo(17f, 9f)
                lineTo(16f, 6.5f)
                lineTo(13.5f, 5.5f)
                lineTo(16f, 4.5f)
                close()
            }
        }
    }
}
