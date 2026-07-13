// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.snapshot

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.ui.DefaultUiTheme
import io.github.ronjunevaldoz.awake.ui.UiContext
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.toggle
import kotlin.test.Test

/**
 * Not a pass/fail regression check -- a visual review aid (see [saveUiSnapshot]'s doc
 * comment). Motivating case: `toggle()`'s checked-state fill quad (`Widgets.kt`) is emitted
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
        Input.setPointer(down = false, x = 0f, y = 0f)

        val uncheckedUi = UiContext()
        uncheckedUi.beginFrame(160f, 40f)
        uncheckedUi.absolute(0f, 0f, font = font, theme = DefaultUiTheme).toggle("t", checked = false, width = 160f, height = 40f, label = "ENABLED")
        saveUiSnapshot("toggle-unchecked", uncheckedUi.endFrame(), 160, 40)

        val checkedUi = UiContext()
        checkedUi.beginFrame(160f, 40f)
        checkedUi.absolute(0f, 0f, font = font, theme = DefaultUiTheme).toggle("t", checked = true, width = 160f, height = 40f, label = "ENABLED")
        saveUiSnapshot("toggle-checked", checkedUi.endFrame(), 160, 40)
    }
}
