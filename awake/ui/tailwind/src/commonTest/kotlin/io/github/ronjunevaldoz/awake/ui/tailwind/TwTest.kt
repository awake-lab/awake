// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.tailwind

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TwTest {

    @Test
    fun tailwindSpacingScaleMatchesExpectedValues() {
        assertEquals(0f.dp, Tw.Spacing.s0)
        assertEquals(1f.dp, Tw.Spacing.px)
        assertEquals(2f.dp, Tw.Spacing.s0_5)
        assertEquals(4f.dp, Tw.Spacing.s1)
        assertEquals(8f.dp, Tw.Spacing.s2)
        assertEquals(10f.dp, Tw.Spacing.s2_5)
        assertEquals(16f.dp, Tw.Spacing.s4)
        assertEquals(32f.dp, Tw.Spacing.s8)
        assertEquals(64f.dp, Tw.Spacing.s16)
        assertEquals(384f.dp, Tw.Spacing.s96)
    }

    @Test
    fun tailwindRadiusScaleMatchesExpectedValues() {
        assertEquals(0f.dp, Tw.Radius.none)
        assertEquals(2f.dp, Tw.Radius.sm)
        assertEquals(4f.dp, Tw.Radius.md)
        assertEquals(6f.dp, Tw.Radius.lg)
        assertEquals(8f.dp, Tw.Radius.xl)
        assertEquals(9999f.dp, Tw.Radius.full)
    }

    @Test
    fun tailwindColorsMatchHexValues() {
        assertEquals(Color.fromHex("#0f172a"), TwColors.slate900)
        assertEquals(Color.fromHex("#3b82f6"), TwColors.blue500)
        assertEquals(Color.fromHex("#ef4444"), TwColors.red500)
        assertEquals(Color.fromHex("#22c55e"), TwColors.green500)
    }

    @Test
    fun gridInsetsConstructCorrectDpValues() {
        val insets = UiInsets.grid(horizontal = 2.5, vertical = 0.5)
        assertEquals(10f.dp, insets.start)
        assertEquals(2f.dp, insets.top)
        assertEquals(10f.dp, insets.end)
        assertEquals(2f.dp, insets.bottom)
    }

    @Test
    fun modifierExtensionsReturnChainedModifier() {
        val modifier = Modifier.p(Tw.Spacing.s4).px(Tw.Spacing.s2).roundedMd().wFull()
        assertNotNull(modifier)
    }
}
