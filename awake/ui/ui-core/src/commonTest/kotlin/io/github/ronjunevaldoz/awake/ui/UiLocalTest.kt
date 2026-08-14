// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.sp
import io.github.ronjunevaldoz.awake.ui.context.UiContextStacks
import io.github.ronjunevaldoz.awake.ui.context.uiLocalOf
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The three scoped values whose combine rule is NOT "replace the parent".
 *
 * A generic provider is only safe if the rule travels with the value. Replace-everything would
 * compile, pass anything that pushes one level, and silently stop text inheriting and alpha
 * compounding the moment something nested -- which reads as a rendering bug nowhere near the
 * cause. These pin the rules themselves.
 */
class UiLocalTest {

    private companion object {
        val LocalUser = uiLocalOf("anonymous")
        val LocalDepth = uiLocalOf(0) { parent, incoming -> parent + incoming }
    }

    @Test
    fun aNestedTextStyleInheritsWhatItDoesNotOverride() {
        val ui = UiContextStacks()
        ui.pushTextStyle(TextStyle(color = Color.White, size = 20f.sp))
        ui.pushTextStyle(TextStyle(size = 12f.sp))

        assertEquals(12f.sp, ui.currentTextStyle.size, "the inner push wins on what it sets")
        assertEquals(
            Color.White,
            ui.currentTextStyle.color,
            "and inherits what it left alone -- replace-the-parent would drop this colour",
        )
    }

    @Test
    fun nestedAlphaCompounds() {
        val ui = UiContextStacks()
        ui.pushAlpha(0.5f)
        ui.pushAlpha(0.5f)

        assertEquals(
            0.25f,
            ui.currentAlpha,
            "two half-transparent layers read 0.25 composed, not 0.5",
        )
    }

    @Test
    fun anAppDeclaredLocalScopesLikeTheEngineOwnOnes() {
        // The point of the whole mechanism: a local the engine never heard of behaves identically.
        val ui = UiContextStacks()

        assertEquals("anonymous", ui.current(LocalUser), "an unprovided local reads its default")

        ui.push(LocalUser, "ada")
        assertEquals("ada", ui.current(LocalUser))
        ui.push(LocalUser, "grace")
        assertEquals("grace", ui.current(LocalUser), "the innermost provide wins")
        ui.pop(LocalUser)
        assertEquals("ada", ui.current(LocalUser), "and unwinds to the outer one")
    }

    @Test
    fun anAppDeclaredLocalCanCombineWithItsParent() {
        val ui = UiContextStacks()
        ui.push(LocalDepth, 2)
        ui.push(LocalDepth, 3)

        assertEquals(5, ui.current(LocalDepth), "combine is not restricted to the engine's locals")
    }

    @Test
    fun aTrialResetClearsAppDeclaredLocalsToo() {
        // resetForTrial used to walk a hand-written list of engine stacks. Anything not on that
        // list survived into the next trial -- which is exactly how a stale text-style token once
        // leaked. An app local must not be able to reintroduce that.
        val ui = UiContextStacks()
        ui.push(LocalUser, "ada")
        ui.resetForTrial(ui.currentTheme, ui.currentTextStyle, ui.currentFont)

        assertEquals("anonymous", ui.current(LocalUser), "a trial reset must clear every local")
    }

    @Test
    fun poppingPastTheBaseKeepsTheBase() {
        val ui = UiContextStacks()
        val base = ui.currentTextStyle
        repeat(3) { ui.popTextStyle() }

        assertEquals(base, ui.currentTextStyle, "an unbalanced pop must not drain the stack")
    }
}
