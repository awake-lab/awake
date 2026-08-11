// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.api.dp
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The checkbox corner is shadcn v4's literal `rounded-[4px]`, deliberately off the radius scale.
 * It used to take `radii.md`, which is half the 16dp box at the default base radius -- a circle --
 * and grew rounder with every larger preset.
 */
class ShadcnCheckboxRadiusTest {

    @Test
    fun checkboxCornerStaysFourDpAcrossEveryPreset() {
        ShadcnStylePreset.entries.forEach { preset ->
            val theme = shadcnTheme(preset = preset)
            assertEquals(
                4f.dp,
                theme.components.checkbox.resolve().shape,
                "checkbox radius must not track ${preset.label}'s base radius",
            )
        }
    }
}
