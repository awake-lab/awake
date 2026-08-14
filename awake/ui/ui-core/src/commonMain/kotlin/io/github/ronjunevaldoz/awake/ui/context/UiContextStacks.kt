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

/**
 * Every value that scopes to a subtree, each a [UiScopedValue] carrying its own combine rule.
 *
 * The public push/pop/current surface is unchanged -- callers still say `pushTheme`. What changed
 * is that the seven stacks are no longer seven hand-written copies of the same two lines, and that
 * the three rules which are NOT "replace the parent" now sit next to the value they govern rather
 * than being spelled out in a push function someone has to notice.
 */
internal class UiContextStacks {
    private val theme = UiScopedValue<UiTheme>(UiDefaultTheme)

    /** Merges with the style it nests inside, so a child inherits what it does not override. */
    private val textStyle = UiScopedValue(TextStyle.Default) { parent, incoming -> parent then incoming }

    /** Inherits the enclosing token when a push does not name one. */
    private val textStyleToken = UiScopedValue<String?>(null) { parent, incoming -> incoming ?: parent }

    private val font = UiScopedValue(UiFonts.default())
    private val shapeSpec = UiScopedValue<UiShapeSpec?>(null)

    /**
     * Cumulative rather than per-level: each push stores parent * incoming, so reading the top is
     * always the fully composed effective alpha, with no fold over the stack on the hot path.
     */
    private val alpha = UiScopedValue(1f) { parent, incoming -> (parent * incoming).coerceIn(0f, 1f) }

    /**
     * Innermost wins, `null` meaning no active scale effect.
     *
     * Unlike [alpha], nested scale effects with DIFFERENT pivots do not compose by multiplication
     * -- that needs real affine-matrix composition, out of scope for the scale-only pass (see
     * docs/tasks/2026-08-02-graphicslayer-rotation-scale.md). ponytail: nested graphicsLayer scale
     * blocks with different pivots are not composed correctly (only the innermost applies) --
     * upgrade to matrix stacking if nested scale becomes a real use case.
     */
    private val transform = UiScopedValue<UiPrimitiveTransform?>(null)

    val currentTheme: UiTheme get() = theme.current
    val currentTextStyle: TextStyle get() = textStyle.current
    val currentTextStyleToken: String? get() = textStyleToken.current
    val currentFont: UiFont get() = font.current
    val currentShapeSpec: UiShapeSpec? get() = shapeSpec.current
    val currentAlpha: Float get() = alpha.current
    val currentTransform: UiPrimitiveTransform? get() = transform.current

    fun pushTheme(theme: UiTheme) = this.theme.push(theme)
    fun popTheme() = theme.pop()

    // Style and token move together or the token misattributes a style it never described.
    fun pushTextStyle(style: TextStyle, tokenId: String? = null) {
        textStyle.push(style)
        textStyleToken.push(tokenId)
    }

    fun popTextStyle() {
        textStyle.pop()
        textStyleToken.pop()
    }

    fun pushFont(font: UiFont) = this.font.push(font)
    fun popFont() = font.pop()

    fun pushShapeSpec(spec: UiShapeSpec?) = shapeSpec.push(spec)
    fun popShapeSpec() = shapeSpec.pop()

    fun pushAlpha(alpha: Float) = this.alpha.push(alpha)
    fun popAlpha() = alpha.pop()

    fun pushTransform(transform: UiPrimitiveTransform) = this.transform.push(transform)
    fun popTransform() = transform.pop()

    /**
     * Collapses every scoped value to a single base entry carrying [theme]/[textStyle]/[font].
     *
     * A reused trial context (see [UiContextMeasureState.createMeasureContext]) is never popped
     * back the way a real widget's push/pop pair is, so it resets. Resetting directly to
     * [textStyle] rather than merging onto a fresh Default is behavior-identical, since
     * `TextStyle.Default then style == style`.
     *
     * Now covers the token as well. The hand-written version reset six stacks and missed
     * `textStyleTokenStack`, so it grew on every reuse and `currentTextStyleToken` could report a
     * token left over from a previous trial.
     */
    fun resetForTrial(theme: UiTheme, textStyle: TextStyle, font: UiFont) {
        this.theme.reset(theme)
        this.textStyle.reset(textStyle)
        textStyleToken.reset()
        this.font.reset(font)
        shapeSpec.reset()
        alpha.reset()
        transform.reset()
    }
}
