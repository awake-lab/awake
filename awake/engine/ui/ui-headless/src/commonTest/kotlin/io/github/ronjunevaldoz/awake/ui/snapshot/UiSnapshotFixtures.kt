// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.snapshot

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiImageVector
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldDropdown
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldSlider
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldToggle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.shadcnAlertDialog
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.shadcnFieldError
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.components.typography.shadcnSupportingLines
import io.github.ronjunevaldoz.awake.ui.designsystem.components.typography.shadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnSurfaceVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.headless.UiButtonVariant
import io.github.ronjunevaldoz.awake.ui.headless.button
import io.github.ronjunevaldoz.awake.ui.headless.components.icon
import io.github.ronjunevaldoz.awake.ui.headless.input.select
import io.github.ronjunevaldoz.awake.ui.headless.input.selection.checkbox
import io.github.ronjunevaldoz.awake.ui.headless.input.slider
import io.github.ronjunevaldoz.awake.ui.headless.input.text.UiTextWrap
import io.github.ronjunevaldoz.awake.ui.headless.input.text.text
import io.github.ronjunevaldoz.awake.ui.headless.input.text.textField
import io.github.ronjunevaldoz.awake.ui.headless.input.toggle.toggle
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.layout.toDimension
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.layouts.spacer
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.align
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.offset
import io.github.ronjunevaldoz.awake.ui.modifier.size
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.testSnapshot
import io.github.ronjunevaldoz.awake.ui.theme.CoreUiComponentStyles
import io.github.ronjunevaldoz.awake.ui.theme.UiColorTokens
import io.github.ronjunevaldoz.awake.ui.theme.UiDefaultTheme
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme
import io.github.ronjunevaldoz.awake.ui.uiImageVector

data class UiSnapshotScene(
    val name: String,
    val width: Int,
    val height: Int,
    val primitives: List<io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive>,
    val background: Color = Color(0.1f, 0.1f, 0.12f, 1f),
    val font: UiFont? = null,
    val title: String? = null,
    val summary: String? = null,
)

internal fun reviewSnapshotScenes(): List<UiSnapshotScene> {
    val font = UiFonts.default()
    parkPointerOffCanvas()

    val uncheckedUi = UiContext()
    uncheckedUi.beginFrame(160f, 40f, testSnapshot())
    uncheckedUi.pushFont(font)
    uncheckedUi.pushTheme(UiDefaultTheme)
    uncheckedUi.createAbsolute(x = 0f, y = 0f)
        .toggle("toggle-unchecked", checked = false, label = "ENABLED", modifier = Modifier.width(160f.px).height(40f.px))

    val checkedUi = UiContext()
    checkedUi.beginFrame(160f, 40f, testSnapshot())
    checkedUi.pushFont(font)
    checkedUi.pushTheme(UiDefaultTheme)
    checkedUi.createAbsolute(x = 0f, y = 0f)
        .toggle("toggle-checked", checked = true, label = "ENABLED", modifier = Modifier.width(160f.px).height(40f.px))

    val buttonVariants = UiButtonVariant.entries.map { variant ->
        val variantId = buttonVariantId(variant)
        val ui = UiContext()
        ui.beginFrame(160f, 40f, testSnapshot())
        ui.pushFont(font)
        ui.pushTheme(UiDefaultTheme)
        ui.createAbsolute(x = 0f, y = 0f)
            .button("button-$variantId", label = "BUTTON", modifier = Modifier.width(160f.px).height(40f.px), variant = variant, radius = UiShape.md)
        UiSnapshotScene(
            name = "button-$variantId",
            width = 160,
            height = 40,
            primitives = ui.endFrame(),
            background = UiDefaultTheme.colors.background,
            font = font,
        )
    }

    val lightThemeUi = UiContext()
    lightThemeUi.beginFrame(160f, 40f, testSnapshot())
    lightThemeUi.pushFont(font)
    lightThemeUi.pushTheme(SnapshotLightUiTheme)
    lightThemeUi.createAbsolute(x = 0f, y = 0f)
        .button("theme-light", label = "BUTTON", modifier = Modifier.width(160f.px).height(40f.px))

    val darkThemeUi = UiContext()
    darkThemeUi.beginFrame(160f, 40f, testSnapshot())
    darkThemeUi.pushFont(font)
    darkThemeUi.pushTheme(UiDefaultTheme)
    darkThemeUi.createAbsolute(x = 0f, y = 0f)
        .button("theme-dark", label = "BUTTON", modifier = Modifier.width(160f.px).height(40f.px))

    val panelUi = UiContext()
    panelUi.beginFrame(240f, 200f, testSnapshot())
    panelUi.pushFont(font)
    panelUi.pushTheme(UiDefaultTheme)
    val panelColumn = panelUi.createColumn(x = 20f, y = 20f, width = 200f)
    panelColumn.surface(
        "inspector",
        modifier = Modifier.width(Dimension.FillMax).height(Dimension.Fixed(140f.px)),
        style = Style { borderWidth(1f.dp) },
    ) {
        text("CAMERA", color = UiDefaultTheme.colors.mutedForeground)
        select("mode", listOf("ORBIT", "FREE_FLY"), 0, modifier = Modifier.width(180f.px).height(24f.px))
        checkbox("debug", checked = true, label = "DEBUG", modifier = Modifier.width(180f.px).height(24f.px))
    }

    // Gap found by the unified UI component lookup audit (2026-08-02): shadcnFieldError had
    // no showcase page or snapshot demonstrating it standalone -- cheap enough to add here
    // rather than punt to a follow-up.
    val fieldErrorUi = UiContext()
    fieldErrorUi.beginFrame(240f, 40f, testSnapshot())
    fieldErrorUi.pushFont(font)
    fieldErrorUi.pushTheme(ShadcnTheme)
    fieldErrorUi.createAbsolute(x = 0f, y = 0f)
        .shadcnFieldError("This field is required.", modifier = Modifier.width(240f.px).height(24f.px))

    return buildList {
        add(
            UiSnapshotScene(
                name = "toggle-unchecked",
                width = 160,
                height = 40,
                primitives = uncheckedUi.endFrame(),
                font = font,
            ),
        )
        add(
            UiSnapshotScene(
                name = "toggle-checked",
                width = 160,
                height = 40,
                primitives = checkedUi.endFrame(),
                font = font,
            ),
        )
        addAll(buttonVariants)
        add(
            UiSnapshotScene(
                name = "theme-dark",
                width = 160,
                height = 40,
                primitives = darkThemeUi.endFrame(),
                background = UiDefaultTheme.colors.background,
                font = font,
            ),
        )
        add(
            UiSnapshotScene(
                name = "theme-light",
                width = 160,
                height = 40,
                primitives = lightThemeUi.endFrame(),
                background = SnapshotLightUiTheme.colors.background,
                font = font,
            ),
        )
        add(
            UiSnapshotScene(
                name = "panel-with-children",
                width = 240,
                height = 200,
                primitives = panelUi.endFrame(),
                font = font,
            ),
        )
        add(
            UiSnapshotScene(
                name = "shadcn-field-error",
                width = 240,
                height = 40,
                primitives = fieldErrorUi.endFrame(),
                background = ShadcnTheme.colors.background,
                font = font,
            ),
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
        build: UiContext.(UiFont) -> Unit,
    ): UiSnapshotScene {
        val ui = UiContext()
        ui.beginFrame(width.toFloat(), height.toFloat(), testSnapshot())
        ui.build(font)
        return UiSnapshotScene(
            name = name,
            width = width,
            height = height,
            primitives = ui.endFrame(),
            background = background,
            font = font,
            title = title,
            summary = summary,
        )
    }

    return listOf(
        scene(
            name = "ui-button-variants",
            width = 620,
            height = 200,
            background = ShadcnTheme.colors.background,
            title = "Button Variants",
            summary = "The Awake shadcn layer keeps the same shared widget runtime while giving buttons a sharper, darker design language.",
        ) { snapshotFont ->
            pushFont(snapshotFont)
            pushTheme(ShadcnTheme)
            pushTextStyle(io.github.ronjunevaldoz.awake.ui.theme.TextStyle(scale = 2f))
            column(
                modifier = Modifier.offset(16f.dp, 18f.dp).width(588f.dp),
                verticalArrangement = Arrangement.spacedBy(10f.dp),
            ) {
                shadcnSurface(
                    id = "button-variants",
                    modifier = Modifier.width(Dimension.Fixed(588f.px)).height(Dimension.WrapContent),
                ) {
                    text("Awake Shadcn Buttons")
                    shadcnSupportingText("Primary, secondary, outline, ghost, and danger all ride the same owned design tokens.")
                    spacer(Modifier.height(8f.dp))
                    row(horizontalArrangement = Arrangement.spacedBy(8f.dp), modifier = Modifier.height(40f.dp.toDimension())) {
                        shadcnButton("primary", "Primary", modifier = Modifier.width(138f.px).height(40f.px), variant = ShadcnButtonVariant.Primary)
                        shadcnButton("secondary", "Secondary", modifier = Modifier.width(172f.px).height(40f.px), variant = ShadcnButtonVariant.Secondary)
                        shadcnButton("outline", "Outline", modifier = Modifier.width(138f.px).height(40f.px), variant = ShadcnButtonVariant.Outline)
                    }
                    row(horizontalArrangement = Arrangement.spacedBy(8f.dp), modifier = Modifier.height(40f.dp.toDimension())) {
                        shadcnButton("ghost", "Ghost", modifier = Modifier.width(122f.px).height(40f.px), variant = ShadcnButtonVariant.Ghost)
                        shadcnButton("danger", "Danger", modifier = Modifier.width(138f.px).height(40f.px), variant = ShadcnButtonVariant.Danger)
                    }
                }
            }
        },
        scene(
            name = "ui-shaped-panel",
            width = 300,
            height = 180,
            background = UiDefaultTheme.colors.background,
            title = "Shaped Panel Composition",
            summary = "Panels can opt into a custom shape and content clipping, which gives the DSL a reusable way to compose containers and controls.",
        ) { snapshotFont ->
            pushFont(snapshotFont)
            pushTheme(UiDefaultTheme)
            createAbsolute(x = 20f, y = 20f).surface(
                id = "shape-panel",
                style = Style {
                    shape(UiShapeSpec.CutCorner(12f.dp))
                    border(1f.dp, UiDefaultTheme.colors.border)
                    contentPadding(12f.dp)
                },
                clipContent = true,
                modifier = Modifier.width(Dimension.Fixed(260f.px)).height(Dimension.Fixed(120f.px)),
            ) { slot ->
                text("Shaped Panel", color = UiDefaultTheme.colors.mutedForeground)
                context.pushFont(snapshotFont)
                context.pushTheme(UiDefaultTheme)
                context.createAbsolute(x = slot.x + 12f, y = slot.y + 44f)
                    .button("launch", label = "Launch Scene", modifier = Modifier.width(180f.px).height(36f.dp), radius = UiShape.md)
            }
        },
        scene(
            name = "ui-panel-controls",
            width = 430,
            height = 360,
            background = ShadcnTheme.colors.background,
            title = "Panel Controls",
            summary = "The same property-form scaffolds can be skinned by the shared shadcn layer, so tool surfaces look authored without moving logic into the sample.",
        ) { snapshotFont ->
            pushFont(snapshotFont)
            pushTheme(ShadcnTheme)
            pushTextStyle(io.github.ronjunevaldoz.awake.ui.theme.TextStyle(scale = 2f))
            column(
                modifier = Modifier.offset(20f.dp, 20f.dp).width(390f.dp),
                verticalArrangement = Arrangement.spacedBy(10f.dp),
            ) {
                shadcnSurface(
                    id = "inspector",
                    modifier = Modifier.width(Dimension.Fixed(390f.px)).height(Dimension.WrapContent),
                ) {
                    text("Controls")
                    shadcnSupportingText("Shared DSL rows with branded field recipes that stay readable even when labels and helper copy run long.")
                    spacer(Modifier.height(4f.dp))
                    shadcnFieldDropdown("mode", "Camera Mode", listOf("Orbit", "Free Fly", "Follow"), selectedIndex = 0)
                    shadcnFieldToggle("debug", "Debug Frustum Overlay", checked = true)
                    shadcnFieldToggle("grid", "Show Reference Grid", checked = false)
                    shadcnFieldSlider("exposure", "Exposure Compensation", min = 0f, max = 100f, value = 68f)
                    checkbox("wireframe", checked = true, label = "Wireframe Overlay")
                }
            }
        },
        scene(
            name = "ui-alert-dialog",
            width = 360,
            height = 260,
            background = ShadcnTheme.colors.background,
            title = "Alert Dialog",
            summary = "A long title must wrap and stay clipped inside the dialog panel instead of overflowing past its bounds.",
        ) { snapshotFont ->
            pushFont(snapshotFont)
            pushTheme(ShadcnTheme)
            pushTextStyle(io.github.ronjunevaldoz.awake.ui.theme.TextStyle(scale = 2f))
            column(modifier = Modifier.width(360f.dp)) {
                shadcnAlertDialog(
                    id = "snapshot-alert",
                    expanded = true,
                    title = "Delete this very long showcase card title that must wrap?",
                    message = "This sample does not really delete anything.",
                )
            }
        },
        scene(
            name = "ui-component-state-matrix",
            width = 460,
            height = 360,
            background = ShadcnTheme.colors.background,
            title = "Component State Matrix",
            summary = "Every state a component can be in, side by side under the shadcn theme -- not just its default rest look. This is the gallery page that would have shown the toggle/slider/checkbox color-inversion bug and the dropdown-row styling bug at a glance instead of requiring a live click-through.",
        ) { snapshotFont ->
            requestFocus("state-matrix-focused-field")
            pushFont(snapshotFont)
            pushTheme(ShadcnTheme)
            pushTextStyle(io.github.ronjunevaldoz.awake.ui.theme.TextStyle(scale = 2f))
            column(
                modifier = Modifier.offset(20f.dp, 20f.dp).width(420f.dp),
                verticalArrangement = Arrangement.spacedBy(14f.dp),
            ) {
                row(horizontalArrangement = Arrangement.spacedBy(16f.dp), modifier = Modifier.height(40f.dp.toDimension())) {
                    toggle("state-matrix-toggle-off", checked = false, modifier = Modifier.width(120f.px).height(24f.px))
                    toggle("state-matrix-toggle-on", checked = true, modifier = Modifier.width(120f.px).height(24f.px))
                }
                row(horizontalArrangement = Arrangement.spacedBy(16f.dp), modifier = Modifier.height(32f.dp.toDimension())) {
                    checkbox("state-matrix-checkbox-off", checked = false, modifier = Modifier.width(180f.px).height(24f.px))
                    checkbox("state-matrix-checkbox-on", checked = true, modifier = Modifier.width(180f.px).height(24f.px))
                }
                row(horizontalArrangement = Arrangement.spacedBy(16f.dp), modifier = Modifier.height(36f.dp.toDimension())) {
                    slider("state-matrix-slider-empty", min = 0f, max = 100f, value = 0f, modifier = Modifier.width(120f.px).height(36f.dp))
                    slider("state-matrix-slider-half", min = 0f, max = 100f, value = 50f, modifier = Modifier.width(120f.px).height(36f.dp))
                    slider("state-matrix-slider-full", min = 0f, max = 100f, value = 100f, modifier = Modifier.width(120f.px).height(36f.dp))
                }
                row(horizontalArrangement = Arrangement.spacedBy(16f.dp), modifier = Modifier.height(36f.dp.toDimension())) {
                    textField("state-matrix-empty-field", value = "", placeholder = "Placeholder", modifier = Modifier.width(190f.px).height(36f.dp))
                    textField("state-matrix-focused-field", value = "Typed", modifier = Modifier.width(190f.px).height(36f.dp))
                }
            }
        },
        scene(
            name = "ui-rounded-clip-vector",
            width = 340,
            height = 220,
            background = UiDefaultTheme.colors.background,
            title = "Rounded Clip And Vector",
            summary = "Rounded surfaces, colored borders, Box-style alignment, and vector-path icons all compose through the same widget surface, with shape clipping trimming intentional overflow.",
        ) { snapshotFont ->
            pushFont(snapshotFont)
            pushTheme(UiDefaultTheme)
            val panelScope = createAbsolute(x = 24f, y = 24f)
            panelScope.surface(
                id = "vector-showcase",
                style = Style {
                    shape(UiShapeSpec.CutCorner(18f.dp))
                    background(Color(0.13f, 0.16f, 0.24f, 1f))
                    border(2f.dp, Color(0.38f, 0.58f, 0.94f, 1f))
                    contentPadding(14f.dp)
                },
                clipContent = true,
                modifier = Modifier.width(Dimension.Fixed(292f.px)).height(Dimension.Fixed(164f.px)),
            ) { slot ->
                text("Rounded + Clip + Vector", color = Color(0.94f, 0.96f, 1f, 1f))
                text(
                    "The icon intentionally overflows and gets clipped by the cut-corner shell.",
                    color = UiDefaultTheme.colors.mutedForeground,
                    wrap = UiTextWrap.Word,
                )

                context.pushFont(snapshotFont)
                context.pushTheme(UiDefaultTheme)
                context.createBox(
                    x = slot.x + 16f,
                    y = slot.y + 56f,
                    width = slot.width - 32f,
                    height = 78f,
                    contentAlignment = UiAlignment.Center,
                ).apply {
                    surface(
                        id = "chip",
                        style = Style {
                            shape(28f.dp)
                            background(Color(0.2f, 0.24f, 0.36f, 1f))
                            border(1f.dp, Color(0.56f, 0.72f, 1f, 1f))
                        },
                        modifier = (Modifier.align(UiAlignment.Center)).width(Dimension.Fixed(180f.px)).height(Dimension.Fixed(56f.px)),
                    ) {
                        text("ICON CHIP", color = Color(0.95f, 0.97f, 1f, 1f))
                    }
                    icon(
                        imageVector = tutorialSparkleIcon,
                        modifier = Modifier
                            .align(UiAlignment.CenterEnd)
                            .offset(x = 18f.dp)
                            .size(88f.dp, 88f.dp),
                        tint = Color(0.68f, 0.84f, 1f, 0.95f),
                    )
                }
            }
        },
        scene(
            name = "ui-awake-shadcn-showcase",
            width = 560,
            height = 360,
            background = ShadcnTheme.colors.background,
            title = "Awake Shadcn Showcase",
            summary = "The starter design-system layer can already express a recognizable shadcn-style component set while staying fully inside Awake's owned widget stack.",
        ) { snapshotFont ->
            pushFont(snapshotFont)
            pushTheme(ShadcnTheme)
            pushTextStyle(io.github.ronjunevaldoz.awake.ui.theme.TextStyle(scale = 2f))
            column(
                modifier = Modifier.offset(20f.dp, 20f.dp).width(520f.dp),
                verticalArrangement = Arrangement.spacedBy(12f.dp),
            ) {
                shadcnSurface(
                    id = "shadcn-showcase",
                    modifier = Modifier.width(Dimension.Fixed(520f.px)).height(Dimension.WrapContent),
                ) {
                    text("Awake Shadcn")
                    shadcnSupportingText("Owned components layered over Awake widgets, with the same shared layout/runtime handling long copy and wrapped panel content.")
                    spacer(Modifier.height(8f.dp))
                    row(horizontalArrangement = Arrangement.spacedBy(8f.dp), modifier = Modifier.height(40f.dp.toDimension())) {
                        shadcnButton("showcase-doc-primary", "Primary", modifier = Modifier.width(138f.px).height(40f.px), variant = ShadcnButtonVariant.Primary)
                        shadcnButton("showcase-doc-secondary", "Secondary", modifier = Modifier.width(172f.px).height(40f.px), variant = ShadcnButtonVariant.Secondary)
                        shadcnButton("showcase-doc-outline", "Outline", modifier = Modifier.width(138f.px).height(40f.px), variant = ShadcnButtonVariant.Outline)
                    }
                    row(horizontalArrangement = Arrangement.spacedBy(8f.dp), modifier = Modifier.height(30f.dp.toDimension())) {
                        shadcnBadge("LIVE", variant = ShadcnBadgeVariant.Primary)
                        shadcnBadge("NEUTRAL", variant = ShadcnBadgeVariant.Secondary)
                        shadcnBadge("BETA", variant = ShadcnBadgeVariant.Outline)
                        shadcnBadge("RISK", variant = ShadcnBadgeVariant.Danger)
                    }
                    spacer(Modifier.height(8f.dp))
                    shadcnSurface(
                        id = "shadcn-subcard",
                        variant = ShadcnSurfaceVariant.Muted,
                        modifier = Modifier.height(Dimension.WrapContent),
                    ) {
                        text("Preview Card")
                        shadcnSupportingText("A nested card keeps the same tokens and border language while inheriting the same wrap and overflow rules.")
                        spacer(Modifier.height(6f.dp))
                        row(horizontalArrangement = Arrangement.spacedBy(8f.dp), modifier = Modifier.height(36f.dp.toDimension())) {
                            shadcnButton("showcase-doc-ghost", "Ghost", modifier = Modifier.width(112f.px).height(36f.dp), variant = ShadcnButtonVariant.Ghost)
                            shadcnButton("showcase-doc-danger", "Danger", modifier = Modifier.width(112f.px).height(36f.dp), variant = ShadcnButtonVariant.Danger)
                        }
                    }
                    spacer(Modifier.height(8f.dp))
                    shadcnSupportingLines(
                        listOf(
                            "Sample overlays now rely on shared supporting/meta text helpers.",
                            "Property rows stretch labels before starving the control column.",
                        ),
                    )
                }
            }
        },
    )
}

private fun parkPointerOffCanvas() {
}

private fun buttonVariantId(variant: UiButtonVariant): String = when (variant) {
    UiButtonVariant.Filled -> "filled"
    UiButtonVariant.Outline -> "outline"
    UiButtonVariant.Ghost -> "ghost"
}

private object SnapshotLightUiTheme : UiTheme {
    override val colors: UiColorTokens = object : UiColorTokens {
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

    override val components = CoreUiComponentStyles(colors)
}

private val tutorialSparkleIcon: UiImageVector = uiImageVector(
    defaultWidth = 24f.dp,
    defaultHeight = 24f.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
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
