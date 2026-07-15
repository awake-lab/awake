// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.snapshot

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.ui.CoreUiComponentStyles
import io.github.ronjunevaldoz.awake.ui.CoreUiTheme
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.UiColorTokens
import io.github.ronjunevaldoz.awake.ui.UiButtonVariant
import io.github.ronjunevaldoz.awake.ui.UiContext
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.UiTheme
import io.github.ronjunevaldoz.awake.ui.border
import io.github.ronjunevaldoz.awake.ui.button
import io.github.ronjunevaldoz.awake.ui.checkbox
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.dropdown
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.panel
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.text
import io.github.ronjunevaldoz.awake.ui.toggle
import kotlin.test.Test

/**
 * Not a pass/fail regression check -- a visual review aid (see [saveUiSnapshot]'s doc
 * comment). Motivating case: `toggle()`'s checked-state fill quad is emitted
 * AFTER the button's own label glyph, inset around the same centered region the label
 * occupies -- once cross-type paint order was fixed (this session's dropdown draw-order fix),
 * that later quad now correctly paints ON TOP of the label, which means a checked toggle's
 * own label can end up genuinely obscured by its own check-fill, not just low-contrast. No
 * numeric assertion here catches that; looking at the rasterized PNG does. Snapshots land in
 * `build/ui-snapshots/`, always regenerated (`uiSnapshotReport`'s Gradle task turns them into
 * one HTML gallery), regardless of whether any assertion in this file passes.
 */
class UiSnapshotTest {

    @Test
    fun toggleLabelVisibilityUncheckedVsChecked() {
        val font = BitmapFont()
        Input.setPointer(down = false, x = -100f, y = -100f)

        val uncheckedUi = UiContext()
        uncheckedUi.beginFrame(160f, 40f)
        uncheckedUi.absolute(0f, 0f, font = font, theme = CoreUiTheme).toggle("t", checked = false, width = 160f, height = 40f, label = "ENABLED")
        saveUiSnapshot("toggle-unchecked", uncheckedUi.endFrame(), 160, 40)

        val checkedUi = UiContext()
        checkedUi.beginFrame(160f, 40f)
        checkedUi.absolute(0f, 0f, font = font, theme = CoreUiTheme).toggle("t", checked = true, width = 160f, height = 40f, label = "ENABLED")
        saveUiSnapshot("toggle-checked", checkedUi.endFrame(), 160, 40)
    }

    /** Idle state only (pointer parked off-canvas) -- [UiButtonVariant.Outline]/[Ghost] have
     * no fill at rest by design (see `UiButtonVariant.resolveFill`'s doc comment), so this is
     * exactly the case that needs a visual check: is a borderless/fill-less button's label
     * still legible against the surrounding theme background, not just against its own quad. */
    @Test
    fun buttonVariantsFilledOutlineGhost() {
        val font = BitmapFont()
        Input.setPointer(down = false, x = -100f, y = -100f)

        UiButtonVariant.entries.forEach { variant ->
            val ui = UiContext()
            ui.beginFrame(160f, 40f)
            ui.absolute(0f, 0f, font = font, theme = CoreUiTheme)
                .button("b-$variant", 160f, 40f, label = "BUTTON", variant = variant, radius = UiShape.md)
            saveUiSnapshot("button-${variant.name.lowercase()}", ui.endFrame(), 160, 40, background = CoreUiTheme.tokens.background)
        }
    }

    /** Same button, both themes -- the actual case this whole tool exists for (per the
     * session's motivating request): confirm a real light/dark palette swap doesn't leave a
     * near-invisible-contrast label in either mode. */
    @Test
    fun themeDarkVsLight() {
        val font = BitmapFont()
        Input.setPointer(down = false, x = -100f, y = -100f)

        listOf("dark" to CoreUiTheme, "light" to SnapshotLightUiTheme).forEach { (name, theme: UiTheme) ->
            val ui = UiContext()
            ui.beginFrame(160f, 40f)
            ui.absolute(0f, 0f, font = font, theme = theme).button("b-$name", 160f, 40f, label = "BUTTON")
            saveUiSnapshot("theme-$name", ui.endFrame(), 160, 40, background = theme.tokens.background)
        }
    }

    /** [panel]'s own tests (`PanelTest.kt`) only check layout/geometry with empty content
     * lambdas -- never rendered with real child widgets inside it. Motivating case: a
     * compact tools panel (bordered/rounded box) holding a section label, a dropdown, and a
     * checkbox row -- enough to prove the generic widget layer can host nested controls
     * without relying on higher-level DSL composition. */
    @Test
    fun panelWithNestedChildren() {
        val font = BitmapFont()
        Input.setPointer(down = false, x = -100f, y = -100f)

        val ui = UiContext()
        ui.beginFrame(240f, 200f)
        val column = ui.column(x = 20f, y = 20f, width = 200f, font = font, theme = CoreUiTheme)
        column.panel(
            "inspector",
            Dimension.FillMax,
            Dimension.Fixed(140f.px),
            radius = UiShape.md,
            borderWidth = 1f.dp
        ) {
            text("CAMERA", color = CoreUiTheme.tokens.mutedForeground)
            dropdown("mode", listOf("ORBIT", "FREE_FLY"), 0, 180f, 24f)
            checkbox("debug", checked = true, width = 180f, height = 24f, label = "DEBUG")
        }
        saveUiSnapshot("panel-with-children", ui.endFrame(), 240, 200)
    }
}

private object SnapshotLightUiTheme : UiTheme {
    override val tokens: UiColorTokens = object : UiColorTokens {
        override val background = floatArrayOf(0.98f, 0.98f, 0.99f, 1f)
        override val foreground = floatArrayOf(0.1f, 0.1f, 0.12f, 1f)
        override val primary = floatArrayOf(0.2f, 0.2f, 0.24f, 1f)
        override val primaryForeground = floatArrayOf(0.98f, 0.98f, 0.99f, 1f)
        override val secondary = floatArrayOf(0.9f, 0.9f, 0.92f, 1f)
        override val secondaryForeground = floatArrayOf(0.1f, 0.1f, 0.12f, 1f)
        override val muted = floatArrayOf(0.9f, 0.9f, 0.92f, 1f)
        override val mutedForeground = floatArrayOf(0.4f, 0.4f, 0.45f, 1f)
        override val accent = floatArrayOf(0.85f, 0.85f, 0.88f, 1f)
        override val accentForeground = floatArrayOf(0.1f, 0.1f, 0.12f, 1f)
        override val destructive = floatArrayOf(0.8f, 0.2f, 0.2f, 1f)
        override val destructiveForeground = floatArrayOf(0.98f, 0.98f, 0.99f, 1f)
        override val border = floatArrayOf(0.8f, 0.8f, 0.83f, 1f)
    }

    override val components = CoreUiComponentStyles(tokens)
}
