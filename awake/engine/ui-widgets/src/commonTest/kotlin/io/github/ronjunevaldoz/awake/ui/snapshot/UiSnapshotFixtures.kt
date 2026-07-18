// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.snapshot

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.ui.CoreUiComponentStyles
import io.github.ronjunevaldoz.awake.ui.CoreUiTheme
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiAlignment
import io.github.ronjunevaldoz.awake.ui.UiButtonVariant
import io.github.ronjunevaldoz.awake.ui.UiColorTokens
import io.github.ronjunevaldoz.awake.ui.UiContext
import io.github.ronjunevaldoz.awake.ui.UiImageVector
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.UiTextWrap
import io.github.ronjunevaldoz.awake.ui.UiTheme
import io.github.ronjunevaldoz.awake.ui.align
import io.github.ronjunevaldoz.awake.ui.button
import io.github.ronjunevaldoz.awake.ui.checkbox
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.dropdown
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.height
import io.github.ronjunevaldoz.awake.ui.icon
import io.github.ronjunevaldoz.awake.ui.offset
import io.github.ronjunevaldoz.awake.ui.panel
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.size
import io.github.ronjunevaldoz.awake.ui.slider
import io.github.ronjunevaldoz.awake.ui.supportingLines
import io.github.ronjunevaldoz.awake.ui.supportingText
import io.github.ronjunevaldoz.awake.ui.text
import io.github.ronjunevaldoz.awake.ui.textField
import io.github.ronjunevaldoz.awake.ui.toggle
import io.github.ronjunevaldoz.awake.ui.ui
import io.github.ronjunevaldoz.awake.ui.uiImageVector
import io.github.ronjunevaldoz.awake.ui.width
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnSurfaceVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnPropertyDropdown
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnPropertySlider
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnPropertyToggle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnSurface
import io.github.ronjunevaldoz.awake.ui.alertDialog

data class UiSnapshotScene(
    val name: String,
    val width: Int,
    val height: Int,
    val primitives: List<io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive>,
    val background: Color = Color(0.1f, 0.1f, 0.12f, 1f),
    val font: UiFont? = null,
    val title: String? = null,
    val summary: String? = null
)

internal fun reviewSnapshotScenes(): List<UiSnapshotScene> {
    val font = UiFonts.default()
    parkPointerOffCanvas()

    val uncheckedUi = UiContext()
    uncheckedUi.beginFrame(160f, 40f)
    uncheckedUi.absolute(0f, 0f, font = font, theme = CoreUiTheme)
        .toggle("toggle-unchecked", checked = false, label = "ENABLED", modifier = UiModifier().width(160f.px).height(40f.px))

    val checkedUi = UiContext()
    checkedUi.beginFrame(160f, 40f)
    checkedUi.absolute(0f, 0f, font = font, theme = CoreUiTheme)
        .toggle("toggle-checked", checked = true, label = "ENABLED", modifier = UiModifier().width(160f.px).height(40f.px))

    val buttonVariants = UiButtonVariant.entries.map { variant ->
        val variantId = buttonVariantId(variant)
        val ui = UiContext()
        ui.beginFrame(160f, 40f)
        ui.absolute(0f, 0f, font = font, theme = CoreUiTheme)
            .button("button-$variantId", label = "BUTTON", modifier = UiModifier().width(160f.px).height(40f.px), variant = variant, radius = UiShape.md)
        UiSnapshotScene(
            name = "button-$variantId",
            width = 160,
            height = 40,
            primitives = ui.endFrame(),
            background = CoreUiTheme.tokens.background,
            font = font
        )
    }

    val lightThemeUi = UiContext()
    lightThemeUi.beginFrame(160f, 40f)
    lightThemeUi.absolute(0f, 0f, font = font, theme = SnapshotLightUiTheme)
        .button("theme-light", label = "BUTTON", modifier = UiModifier().width(160f.px).height(40f.px))

    val darkThemeUi = UiContext()
    darkThemeUi.beginFrame(160f, 40f)
    darkThemeUi.absolute(0f, 0f, font = font, theme = CoreUiTheme)
        .button("theme-dark", label = "BUTTON", modifier = UiModifier().width(160f.px).height(40f.px))

    val panelUi = UiContext()
    panelUi.beginFrame(240f, 200f)
    val panelColumn = panelUi.column(x = 20f, y = 20f, width = 200f, font = font, theme = CoreUiTheme)
    panelColumn.panel(
        "inspector",
        Dimension.FillMax,
        Dimension.Fixed(140f.px),
        radius = UiShape.md,
        borderWidth = 1f.dp
    ) {
        text("CAMERA", color = CoreUiTheme.tokens.mutedForeground)
        dropdown("mode", listOf("ORBIT", "FREE_FLY"), 0, modifier = UiModifier().width(180f.px).height(24f.px))
        checkbox("debug", checked = true, label = "DEBUG", modifier = UiModifier().width(180f.px).height(24f.px))
    }

    return buildList {
        add(
            UiSnapshotScene(
                name = "toggle-unchecked",
                width = 160,
                height = 40,
                primitives = uncheckedUi.endFrame(),
                font = font
            )
        )
        add(
            UiSnapshotScene(
                name = "toggle-checked",
                width = 160,
                height = 40,
                primitives = checkedUi.endFrame(),
                font = font
            )
        )
        addAll(buttonVariants)
        add(
            UiSnapshotScene(
                name = "theme-dark",
                width = 160,
                height = 40,
                primitives = darkThemeUi.endFrame(),
                background = CoreUiTheme.tokens.background,
                font = font
            )
        )
        add(
            UiSnapshotScene(
                name = "theme-light",
                width = 160,
                height = 40,
                primitives = lightThemeUi.endFrame(),
                background = SnapshotLightUiTheme.tokens.background,
                font = font
            )
        )
        add(
            UiSnapshotScene(
                name = "panel-with-children",
                width = 240,
                height = 200,
                primitives = panelUi.endFrame(),
                font = font
            )
        )
    }
}

internal fun tutorialSnapshotScenes(): List<UiSnapshotScene> {
    val font = UiFonts.default()
    parkPointerOffCanvas()

    fun scene(
        name: String,
        width: Int,
        height: Int,
        background: Color,
        title: String,
        summary: String,
        build: UiContext.(UiFont) -> Unit
    ): UiSnapshotScene {
        val ui = UiContext()
        ui.beginFrame(width.toFloat(), height.toFloat())
        ui.build(font)
        return UiSnapshotScene(
            name = name,
            width = width,
            height = height,
            primitives = ui.endFrame(),
            background = background,
            font = font,
            title = title,
            summary = summary
        )
    }

    return listOf(
        scene(
            name = "ui-button-variants",
            width = 620,
            height = 200,
            background = AwakeShadcnTheme.tokens.background,
            title = "Button Variants",
            summary = "The Awake shadcn layer keeps the same shared widget runtime while giving buttons a sharper, darker design language."
        ) { snapshotFont ->
            ui(x = 16f, y = 18f, width = 588f, font = snapshotFont, theme = AwakeShadcnTheme, gap = 10f, textScale = 2f) {
                awakeShadcnSurface(
                    id = "button-variants",
                    width = Dimension.Fixed(588f.px),
                    height = Dimension.WrapContent
                ) {
                    text("Awake Shadcn Buttons")
                    supportingText("Primary, secondary, outline, ghost, and danger all ride the same owned design tokens.")
                    spacer(UiModifier().height(8f.dp))
                    row(height = 40f.dp, gap = 8f) {
                        awakeShadcnButton("primary", "Primary", modifier = UiModifier().width(138f.px).height(40f.px), variant = AwakeShadcnButtonVariant.Primary)
                        awakeShadcnButton("secondary", "Secondary", modifier = UiModifier().width(172f.px).height(40f.px), variant = AwakeShadcnButtonVariant.Secondary)
                        awakeShadcnButton("outline", "Outline", modifier = UiModifier().width(138f.px).height(40f.px), variant = AwakeShadcnButtonVariant.Outline)
                    }
                    row(height = 40f.dp, gap = 8f) {
                        awakeShadcnButton("ghost", "Ghost", modifier = UiModifier().width(122f.px).height(40f.px), variant = AwakeShadcnButtonVariant.Ghost)
                        awakeShadcnButton("danger", "Danger", modifier = UiModifier().width(138f.px).height(40f.px), variant = AwakeShadcnButtonVariant.Danger)
                    }
                }
            }
        },
        scene(
            name = "ui-shaped-panel",
            width = 300,
            height = 180,
            background = CoreUiTheme.tokens.background,
            title = "Shaped Panel Composition",
            summary = "Panels can opt into a custom shape and content clipping, which gives the DSL a reusable way to compose containers and controls."
        ) { snapshotFont ->
            absolute(20f, 20f, font = snapshotFont, theme = CoreUiTheme).panel(
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
                context.absolute(slot.x + 12f, slot.y + 44f, snapshotFont, CoreUiTheme)
                    .button("launch", label = "Launch Scene", modifier = UiModifier().width(180f.px).height(36f.px), radius = UiShape.md)
            }
        },
        scene(
            name = "ui-panel-controls",
            width = 430,
            height = 360,
            background = AwakeShadcnTheme.tokens.background,
            title = "Panel Controls",
            summary = "The same property-form scaffolds can be skinned by the shared shadcn layer, so tool surfaces look authored without moving logic into the sample."
        ) { snapshotFont ->
            ui(x = 20f, y = 20f, width = 390f, font = snapshotFont, theme = AwakeShadcnTheme, gap = 10f, textScale = 2f) {
                awakeShadcnSurface(
                    id = "inspector",
                    width = Dimension.Fixed(390f.px),
                    height = Dimension.WrapContent
                ) {
                    text("Controls")
                    supportingText("Shared DSL rows with branded field recipes that stay readable even when labels and helper copy run long.")
                    spacer(UiModifier().height(4f.dp))
                    awakeShadcnPropertyDropdown("mode", "Camera Mode", listOf("Orbit", "Free Fly", "Follow"), selectedIndex = 0, labelWidth = 96f.dp)
                    awakeShadcnPropertyToggle("debug", "Debug Frustum Overlay", checked = true)
                    awakeShadcnPropertyToggle("grid", "Show Reference Grid", checked = false)
                    awakeShadcnPropertySlider("exposure", "Exposure Compensation", min = 0f, max = 100f, value = 68f, labelWidth = 96f.dp)
                    checkbox("wireframe", checked = true, label = "Wireframe Overlay")
                }
            }
        },
        scene(
            name = "ui-alert-dialog",
            width = 360,
            height = 260,
            background = AwakeShadcnTheme.tokens.background,
            title = "Alert Dialog",
            summary = "A long title must wrap and stay clipped inside the dialog panel instead of overflowing past its bounds."
        ) { snapshotFont ->
            ui(x = 0f, y = 0f, width = 360f, font = snapshotFont, theme = AwakeShadcnTheme, textScale = 2f) {
                alertDialog(
                    id = "snapshot-alert",
                    expanded = true,
                    title = "Delete this very long showcase card title that must wrap?",
                    message = "This sample does not really delete anything."
                )
            }
        },
        scene(
            name = "ui-component-state-matrix",
            width = 460,
            height = 360,
            background = AwakeShadcnTheme.tokens.background,
            title = "Component State Matrix",
            summary = "Every state a component can be in, side by side under the shadcn theme -- not just its default rest look. This is the gallery page that would have shown the toggle/slider/checkbox color-inversion bug and the dropdown-row styling bug at a glance instead of requiring a live click-through."
        ) { snapshotFont ->
            requestFocus("state-matrix-focused-field")
            ui(x = 20f, y = 20f, width = 420f, font = snapshotFont, theme = AwakeShadcnTheme, gap = 14f, textScale = 2f) {
                row(height = 40f.dp, gap = 16f) {
                    toggle("state-matrix-toggle-off", checked = false, modifier = UiModifier().width(120f.px).height(24f.px))
                    toggle("state-matrix-toggle-on", checked = true, modifier = UiModifier().width(120f.px).height(24f.px))
                }
                row(height = 32f.dp, gap = 16f) {
                    checkbox("state-matrix-checkbox-off", checked = false, modifier = UiModifier().width(180f.px).height(24f.px))
                    checkbox("state-matrix-checkbox-on", checked = true, modifier = UiModifier().width(180f.px).height(24f.px))
                }
                row(height = 36f.dp, gap = 16f) {
                    slider("state-matrix-slider-empty", min = 0f, max = 100f, value = 0f, modifier = UiModifier().width(120f.px).height(36f.px))
                    slider("state-matrix-slider-half", min = 0f, max = 100f, value = 50f, modifier = UiModifier().width(120f.px).height(36f.px))
                    slider("state-matrix-slider-full", min = 0f, max = 100f, value = 100f, modifier = UiModifier().width(120f.px).height(36f.px))
                }
                row(height = 36f.dp, gap = 16f) {
                    textField("state-matrix-empty-field", value = "", placeholder = "Placeholder", modifier = UiModifier().width(190f.px).height(36f.px))
                    textField("state-matrix-focused-field", value = "Typed", modifier = UiModifier().width(190f.px).height(36f.px))
                }
            }
        },
        scene(
            name = "ui-rounded-clip-vector",
            width = 340,
            height = 220,
            background = CoreUiTheme.tokens.background,
            title = "Rounded Clip And Vector",
            summary = "Rounded surfaces, colored borders, Box-style alignment, and vector-path icons all compose through the same widget surface, with shape clipping trimming intentional overflow."
        ) { snapshotFont ->
            val panelScope = absolute(24f, 24f, font = snapshotFont, theme = CoreUiTheme)
            panelScope.panel(
                id = "vector-showcase",
                width = Dimension.Fixed(292f.px),
                height = Dimension.Fixed(164f.px),
                style = Style {
                    shape(UiShapeSpec.CutCorner(18f.dp))
                    background(Color(0.13f, 0.16f, 0.24f, 1f))
                    border(2f.dp, Color(0.38f, 0.58f, 0.94f, 1f))
                    contentPadding(14f.dp)
                },
                clipContent = true
            ) { slot ->
                text("Rounded + Clip + Vector", color = Color(0.94f, 0.96f, 1f, 1f))
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
                    font = snapshotFont,
                    theme = CoreUiTheme,
                    contentAlignment = UiAlignment.Center
                ).apply {
                    panel(
                        id = "chip",
                        width = Dimension.Fixed(180f.px),
                        height = Dimension.Fixed(56f.px),
                        style = Style {
                            shape(28f.dp)
                            background(Color(0.2f, 0.24f, 0.36f, 1f))
                            border(1f.dp, Color(0.56f, 0.72f, 1f, 1f))
                        },
                        modifier = UiModifier().align(UiAlignment.Center)
                    ) {
                        text("ICON CHIP", color = Color(0.95f, 0.97f, 1f, 1f))
                    }
                    icon(
                        imageVector = tutorialSparkleIcon,
                        modifier = UiModifier()
                            .align(UiAlignment.CenterEnd)
                            .offset(x = 18f.dp)
                            .size(88f.dp, 88f.dp),
                        tint = Color(0.68f, 0.84f, 1f, 0.95f)
                    )
                }
            }
        },
        scene(
            name = "ui-awake-shadcn-showcase",
            width = 560,
            height = 360,
            background = AwakeShadcnTheme.tokens.background,
            title = "Awake Shadcn Showcase",
            summary = "The starter design-system layer can already express a recognizable shadcn-style component set while staying fully inside Awake's owned widget stack."
        ) { snapshotFont ->
            ui(x = 20f, y = 20f, width = 520f, font = snapshotFont, theme = AwakeShadcnTheme, gap = 12f, textScale = 2f) {
                awakeShadcnSurface(
                    id = "shadcn-showcase",
                    width = Dimension.Fixed(520f.px),
                    height = Dimension.WrapContent
                ) {
                    text("Awake Shadcn")
                    supportingText("Owned components layered over Awake widgets, with the same shared layout/runtime handling long copy and wrapped panel content.")
                    spacer(UiModifier().height(8f.dp))
                    row(height = 40f.dp, gap = 8f) {
                        awakeShadcnButton("showcase-doc-primary", "Primary", modifier = UiModifier().width(138f.px).height(40f.px), variant = AwakeShadcnButtonVariant.Primary)
                        awakeShadcnButton("showcase-doc-secondary", "Secondary", modifier = UiModifier().width(172f.px).height(40f.px), variant = AwakeShadcnButtonVariant.Secondary)
                        awakeShadcnButton("showcase-doc-outline", "Outline", modifier = UiModifier().width(138f.px).height(40f.px), variant = AwakeShadcnButtonVariant.Outline)
                    }
                    row(height = 30f.dp, gap = 8f) {
                        awakeShadcnBadge("LIVE", variant = AwakeShadcnBadgeVariant.Primary)
                        awakeShadcnBadge("NEUTRAL", variant = AwakeShadcnBadgeVariant.Secondary)
                        awakeShadcnBadge("BETA", variant = AwakeShadcnBadgeVariant.Outline)
                        awakeShadcnBadge("RISK", variant = AwakeShadcnBadgeVariant.Danger)
                    }
                    spacer(UiModifier().height(8f.dp))
                    awakeShadcnSurface(
                        id = "shadcn-subcard",
                        height = Dimension.WrapContent,
                        variant = AwakeShadcnSurfaceVariant.Popover
                    ) {
                        text("Preview Card")
                        supportingText("A nested card keeps the same tokens and border language while inheriting the same wrap and overflow rules.")
                        spacer(UiModifier().height(6f.dp))
                        row(height = 36f.dp, gap = 8f) {
                            awakeShadcnButton("showcase-doc-ghost", "Ghost", modifier = UiModifier().width(112f.px).height(36f.px), variant = AwakeShadcnButtonVariant.Ghost)
                            awakeShadcnButton("showcase-doc-danger", "Danger", modifier = UiModifier().width(112f.px).height(36f.px), variant = AwakeShadcnButtonVariant.Danger)
                        }
                    }
                    spacer(UiModifier().height(8f.dp))
                    supportingLines(
                        listOf(
                            "Sample overlays now rely on shared supporting/meta text helpers.",
                            "Property rows stretch labels before starving the control column."
                        )
                    )
                }
            }
        }
    )
}

private fun parkPointerOffCanvas() {
    Input.setPointer(down = false, x = -100f, y = -100f)
}

private fun buttonVariantId(variant: UiButtonVariant): String = when (variant) {
    UiButtonVariant.Filled -> "filled"
    UiButtonVariant.Outline -> "outline"
    UiButtonVariant.Ghost -> "ghost"
}

private object SnapshotLightUiTheme : UiTheme {
    override val tokens: UiColorTokens = object : UiColorTokens {
        override val background = Color(0.98f, 0.98f, 0.99f, 1f)
        override val foreground = Color(0.1f, 0.1f, 0.12f, 1f)
        override val primary = Color(0.2f, 0.2f, 0.24f, 1f)
        override val primaryForeground = Color(0.98f, 0.98f, 0.99f, 1f)
        override val secondary = Color(0.9f, 0.9f, 0.92f, 1f)
        override val secondaryForeground = Color(0.1f, 0.1f, 0.12f, 1f)
        override val muted = Color(0.9f, 0.9f, 0.92f, 1f)
        override val mutedForeground = Color(0.4f, 0.4f, 0.45f, 1f)
        override val accent = Color(0.85f, 0.85f, 0.88f, 1f)
        override val accentForeground = Color(0.1f, 0.1f, 0.12f, 1f)
        override val destructive = Color(0.8f, 0.2f, 0.2f, 1f)
        override val destructiveForeground = Color(0.98f, 0.98f, 0.99f, 1f)
        override val border = Color(0.8f, 0.8f, 0.83f, 1f)
    }

    override val components = CoreUiComponentStyles(tokens)
}

private val tutorialSparkleIcon: UiImageVector = uiImageVector(
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
