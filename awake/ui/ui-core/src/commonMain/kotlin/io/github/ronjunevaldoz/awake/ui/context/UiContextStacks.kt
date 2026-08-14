// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.context

import io.github.ronjunevaldoz.awake.ui.UiPrimitiveTransform
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle
import io.github.ronjunevaldoz.awake.ui.theme.UiDefaultTheme
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme

/** Merges with the style it nests inside, so a child inherits what it does not override. */
val LocalTextStyle: UiLocal<TextStyle> =
    uiLocalOf(TextStyle.Default) { parent, incoming -> parent then incoming }

val LocalTheme: UiLocal<UiTheme> = uiLocalOf(UiDefaultTheme)
val LocalFont: UiLocal<UiFont> = uiLocalOf(UiFonts.default())
val LocalShapeSpec: UiLocal<UiShapeSpec?> = uiLocalOf(null)

/**
 * Cumulative rather than per-level: each provide stores parent * incoming, so reading it is always
 * the fully composed effective alpha, with no fold on the hot path.
 */
val LocalAlpha: UiLocal<Float> = uiLocalOf(1f) { parent, incoming -> (parent * incoming).coerceIn(0f, 1f) }

/**
 * Innermost wins, `null` meaning no active scale effect.
 *
 * Unlike [LocalAlpha], nested scale effects with DIFFERENT pivots do not compose by multiplication
 * -- that needs real affine-matrix composition, out of scope for the scale-only pass (see
 * docs/tasks/2026-08-02-graphicslayer-rotation-scale.md). ponytail: nested graphicsLayer scale
 * blocks with different pivots are not composed correctly (only the innermost applies) -- upgrade
 * to matrix stacking if nested scale becomes a real use case.
 */
val LocalTransform: UiLocal<UiPrimitiveTransform?> = uiLocalOf(null)

/**
 * The engine's own locals, held per context.
 *
 * These seven are declared here rather than being special: they go through the same [UiLocalValues]
 * an app-declared [uiLocalOf] does. Keeping two mechanisms -- one privileged for engine values, one
 * for everyone else -- is how the two drift apart, so there is only the one.
 *
 * The push/pop surface below stays because ~19 call sites use it and its balance is checked by
 * hand at each one. It is now a thin naming layer over [UiLocalValues].
 */
internal class UiContextStacks {
    private val locals = UiLocalValues()

    val currentTheme: UiTheme get() = locals.current(LocalTheme)
    val currentTextStyle: TextStyle get() = locals.current(LocalTextStyle)
    val currentFont: UiFont get() = locals.current(LocalFont)
    val currentShapeSpec: UiShapeSpec? get() = locals.current(LocalShapeSpec)
    val currentAlpha: Float get() = locals.current(LocalAlpha)
    val currentTransform: UiPrimitiveTransform? get() = locals.current(LocalTransform)

    fun <T> current(local: UiLocal<T>): T = locals.current(local)
    fun <T> push(local: UiLocal<T>, value: T) = locals.push(local, value)
    fun <T> pop(local: UiLocal<T>) = locals.pop(local)

    fun pushTheme(theme: UiTheme) = locals.push(LocalTheme, theme)
    fun popTheme() = locals.pop(LocalTheme)

    fun pushTextStyle(style: TextStyle) = locals.push(LocalTextStyle, style)
    fun popTextStyle() = locals.pop(LocalTextStyle)

    fun pushFont(font: UiFont) = locals.push(LocalFont, font)
    fun popFont() = locals.pop(LocalFont)

    fun pushShapeSpec(spec: UiShapeSpec?) = locals.push(LocalShapeSpec, spec)
    fun popShapeSpec() = locals.pop(LocalShapeSpec)

    fun pushAlpha(alpha: Float) = locals.push(LocalAlpha, alpha)
    fun popAlpha() = locals.pop(LocalAlpha)

    fun pushTransform(transform: UiPrimitiveTransform) = locals.push(LocalTransform, transform)
    fun popTransform() = locals.pop(LocalTransform)

    /**
     * Collapses every local to its default, then seeds the three a trial needs.
     *
     * A reused trial context (see [UiContextMeasureState.createMeasureContext]) is never popped
     * back the way a real widget's push/pop pair is, so it resets. [UiLocalValues.resetAll] covers
     * app-declared locals too -- the previous version reset a hand-written list, which is how
     * `textStyleTokenStack` was once missed and grew across every reuse.
     */
    fun resetForTrial(theme: UiTheme, textStyle: TextStyle, font: UiFont) {
        locals.resetAll()
        locals.reset(LocalTheme, theme)
        // Resetting directly to [textStyle] rather than merging onto a fresh Default is
        // behavior-identical, since `TextStyle.Default then style == style`.
        locals.reset(LocalTextStyle, textStyle)
        locals.reset(LocalFont, font)
    }
}
