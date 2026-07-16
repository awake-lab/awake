// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PresetUiThemesTest {

    @Test
    fun defaultPresetDelegatesToCoreFallbackPalette() {
        assertEquals(DefaultUiTheme.tokens.background, DarkUiTheme.tokens.background)
        assertEquals(
            DefaultUiTheme.components.button.resolve().background!!,
            DarkUiTheme.components.button.resolve().background!!
        )
    }

    @Test
    fun lightPresetSwapsBackgroundLuminance() {
        assertNotEquals(
            DefaultUiTheme.tokens.background,
            LightUiTheme.tokens.background
        )
        assertNotEquals(
            DefaultUiTheme.tokens.foreground,
            LightUiTheme.tokens.foreground
        )
    }
}
