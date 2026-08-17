// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.styleable
import io.github.ronjunevaldoz.awake.ui.style.MutableStyleState
import io.github.ronjunevaldoz.awake.ui.style.Style
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Pins the style-merge precedence fixed by 537d13c5 (2026-08-15): every state-conditional rule
 * (hovered/active/focused/disabled/selected) resolves after every unconditional rule in the
 * chain, regardless of `then`/`.styleable()` declaration order -- see [Style.resolve]'s own doc.
 * This is the safety net for that fix: it must fail if the precedence regresses back to strict
 * declaration order.
 */
class StyleResolutionOrderTest {

    private val gray = Color(r = 0.5f, g = 0.5f, b = 0.5f)
    private val red = Color(r = 1f, g = 0f, b = 0f)
    private val blue = Color(r = 0f, g = 0f, b = 1f)
    private val green = Color(r = 0f, g = 1f, b = 0f)
    private val darkGray = Color(r = 0.2f, g = 0.2f, b = 0.2f)

    // The 2026-08-15 P0 shape. Pre-fix, `resolve()` applied every rule in strict declaration
    // order, so `variant`'s unconditional background -- declared later in the `then` chain --
    // clobbered `defaults`' earlier hover rule even while hovered (idle and hovered both read
    // red). Post-fix, the state rule always wins once its predicate matches, no matter where in
    // the chain it was declared.
    @Test
    fun stateRuleOutranksLaterUnconditionalOverrideRegardlessOfThenOrder() {
        val defaults = Style { hovered { background(gray) } }
        val variant = Style { background(red) }
        val composed = defaults then variant

        assertEquals(red, composed.resolve(MutableStyleState(hovered = false)).background, "idle uses the unconditional override")
        assertEquals(gray, composed.resolve(MutableStyleState(hovered = true)).background, "hover must still win despite being declared first")
    }

    // A style's own state rule wins over its own unconditional rule for that state, regardless
    // of which one is written first inside the same Style block.
    @Test
    fun ownStateRuleWinsOverOwnUnconditionalRegardlessOfDeclarationOrder() {
        val unconditionalFirst = Style {
            background(blue)
            hovered { background(green) }
        }
        val conditionalFirst = Style {
            hovered { background(green) }
            background(blue)
        }

        assertEquals(blue, unconditionalFirst.resolve(MutableStyleState(hovered = false)).background)
        assertEquals(green, unconditionalFirst.resolve(MutableStyleState(hovered = true)).background)
        assertEquals(blue, conditionalFirst.resolve(MutableStyleState(hovered = false)).background)
        assertEquals(green, conditionalFirst.resolve(MutableStyleState(hovered = true)).background)
    }

    // Among rules of the same conditionality, `then` position decides the winner: the
    // later-declared style's rule for a field overwrites the earlier one.
    @Test
    fun thenOrderDecidesWinnerBetweenTwoUnconditionalRulesOnTheSameField() {
        val a = Style { background(blue) }
        val b = Style { background(green) }

        assertEquals(green, (a then b).resolve().background)
        assertEquals(blue, (b then a).resolve().background)
    }

    // Each state block only resolves when its own flag is set on the resolved StyleState; an
    // unmatched state block must leave the unconditional value untouched. `pressed` is a direct
    // alias for `active` (StyleScope.pressed = active), verified here via the `active` flag.
    @Test
    fun eachStateBlockResolvesOnlyWhenItsOwnFlagIsSet() {
        val style = Style {
            background(blue)
            hovered { background(green) }
            pressed { background(red) }
            focused { background(gray) }
            disabled { background(darkGray) }
        }

        assertEquals(blue, style.resolve(MutableStyleState()).background, "no flags set: unconditional wins")
        assertEquals(green, style.resolve(MutableStyleState(hovered = true)).background)
        assertEquals(red, style.resolve(MutableStyleState(active = true)).background, "pressed{} fires off the active flag")
        assertEquals(gray, style.resolve(MutableStyleState(focused = true)).background)
        assertEquals(darkGray, style.resolve(MutableStyleState(disabled = true)).background)
    }

    // Modifier.styleable(...) merges through the same `then` Style.then uses directly -- style
    // composed via the modifier API must not change precedence.
    @Test
    fun modifierStyleableChainMatchesDirectThenPrecedence() {
        val defaults = Style { hovered { background(gray) } }
        val variant = Style { background(red) }
        val chained = Modifier.styleable(defaults).styleable(variant).styleable ?: Style.Empty

        assertEquals(red, chained.resolve(MutableStyleState(hovered = false)).background)
        assertEquals(gray, chained.resolve(MutableStyleState(hovered = true)).background)
    }
}
