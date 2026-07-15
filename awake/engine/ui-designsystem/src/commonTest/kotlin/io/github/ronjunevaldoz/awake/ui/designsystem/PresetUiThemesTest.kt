// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertNotEquals

class PresetUiThemesTest {

    @Test
    fun defaultPresetDelegatesToCoreFallbackPalette() {
        assertContentEquals(DefaultUiTheme.tokens.background, DarkUiTheme.tokens.background)
        assertContentEquals(
            DefaultUiTheme.components.button.resolve().background!!,
            DarkUiTheme.components.button.resolve().background!!
        )
    }

    @Test
    fun lightPresetSwapsBackgroundLuminance() {
        assertNotEquals(
            DefaultUiTheme.tokens.background.toList(),
            LightUiTheme.tokens.background.toList()
        )
        assertNotEquals(
            DefaultUiTheme.tokens.foreground.toList(),
            LightUiTheme.tokens.foreground.toList()
        )
    }
}
