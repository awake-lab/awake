// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PresetUiThemesTest {

    @Test
    fun defaultPresetDelegatesToShadcnPalette() {
        assertEquals(ShadcnDefaultTheme.colors.background, DarkUiTheme.colors.background)
    }

    @Test
    fun lightPresetSwapsBackgroundLuminance() {
        assertNotEquals(
            ShadcnDefaultTheme.colors.background,
            LightUiTheme.colors.background,
        )
        assertNotEquals(
            ShadcnDefaultTheme.colors.foreground,
            LightUiTheme.colors.foreground,
        )
    }
}
