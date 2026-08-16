// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.headless.column
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Regression: filled variants must keep their own fill family on hover (`primary/90`,
 * `destructive/90`), never the theme defaults' neutral hover. The 2026-08-15 audit found every
 * filled variant repainting muted-gray with an unchanged white label on hover, because
 * `neutralStyle()` shipped default hovered/active rules that outrank a variant's unconditional
 * background under state-conditional resolution.
 */
class ShadcnButtonStateColorTest {

    private fun hoveredButtonBackground(variant: ShadcnButtonVariant) = shadcnTestSession(width = 300f, height = 120f) {
        val id = "state-btn"
        fun renderFrame(x: Float, y: Float) = frame(x = x, y = y) {
            column {
                shadcnButton(id = id, label = "STATE", variant = variant)
            }
        }
        val settled = renderFrame(x = -100f, y = -100f) // settle mount
        val bounds = settled.bounds(id)
        renderFrame(x = bounds.x + bounds.width / 2f, y = bounds.y + bounds.height / 2f)
    }

    @Test
    fun primaryHoverStaysInPrimaryFamily() {
        val node = hoveredButtonBackground(ShadcnButtonVariant.Primary).node("state-btn")
        assertEquals(UiSemanticRole.Button, node.role)
        assertEquals(
            ShadcnTheme.colors.primary.withAlpha(0.9f),
            node.backgroundColor,
            "hovered primary must be primary/90, not the theme defaults' muted hover",
        )
        assertEquals(ShadcnTheme.colors.primaryForeground, node.foregroundColor)
    }

    @Test
    fun dangerHoverStaysDestructive() {
        val node = hoveredButtonBackground(ShadcnButtonVariant.Danger).node("state-btn")
        assertEquals(UiSemanticRole.Button, node.role)
        assertEquals(
            ShadcnTheme.colors.destructive.withAlpha(0.9f),
            node.backgroundColor,
            "hovered destructive must be destructive/90, not the theme defaults' muted hover",
        )
    }
}
