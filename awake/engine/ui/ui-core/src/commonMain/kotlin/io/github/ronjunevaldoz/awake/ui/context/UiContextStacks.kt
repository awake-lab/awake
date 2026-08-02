// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.context

import io.github.ronjunevaldoz.awake.ui.CoreUiTheme
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme

internal class UiContextStacks {
    private val themeStack = mutableListOf<UiTheme>(CoreUiTheme)
    private val textStyleStack = mutableListOf(TextStyle.Default)
    private val fontStack = mutableListOf(UiFonts.default())
    private val shapeStack = mutableListOf<UiShapeSpec?>(null)

    // Cumulative, not per-level -- each push stores currentAlpha * the newly pushed alpha
    // directly, so reading the top of the stack is always the fully composed effective alpha
    // with no need to fold the whole stack on every primitive emission (the hot path).
    private val alphaStack = mutableListOf(1f)

    val currentTheme: UiTheme get() = themeStack.last()
    val currentTextStyle: TextStyle get() = textStyleStack.last()
    val currentFont: UiFont get() = fontStack.last()
    val currentShapeSpec: UiShapeSpec? get() = shapeStack.last()
    val currentAlpha: Float get() = alphaStack.last()

    fun pushTheme(theme: UiTheme) { themeStack.add(theme) }
    fun popTheme() { if (themeStack.size > 1) themeStack.removeAt(themeStack.size - 1) }

    fun pushTextStyle(style: TextStyle) { textStyleStack.add(textStyleStack.last() then style) }
    fun popTextStyle() { if (textStyleStack.size > 1) textStyleStack.removeAt(textStyleStack.size - 1) }

    fun pushFont(font: UiFont) { fontStack.add(font) }
    fun popFont() { if (fontStack.size > 1) fontStack.removeAt(fontStack.size - 1) }

    fun pushShapeSpec(spec: UiShapeSpec?) {
        shapeStack.add(spec)
    }

    fun popShapeSpec() {
        if (shapeStack.size > 1) shapeStack.removeAt(shapeStack.size - 1)
    }

    fun pushAlpha(alpha: Float) { alphaStack.add((currentAlpha * alpha).coerceIn(0f, 1f)) }
    fun popAlpha() { if (alphaStack.size > 1) alphaStack.removeAt(alphaStack.size - 1) }

    /**
     * Resets all four stacks back to a single base entry carrying [theme]/[textStyle]/[font] --
     * used by a *reused* trial [UiContext] (see [UiContextMeasureState.createMeasureContext])
     * instead of push/pop, since a trial context that's reused across many trial passes within a
     * frame is never explicitly popped back to empty the way a real widget's push/pop pair is.
     * Clearing to size 1 each time keeps the stacks from growing unbounded across reuses while
     * still handing back exactly the same effective top-of-stack value a fresh
     * `UiContextStacks()` + one `pushTextStyle`/`pushFont`/`pushTheme` call would have produced
     * (`TextStyle.Default then style == style`, so resetting directly to [textStyle] instead of
     * merging onto a fresh `Default` base is behavior-identical -- see `TextStyle.then`).
     */
    fun resetForTrial(theme: UiTheme, textStyle: TextStyle, font: UiFont) {
        themeStack.clear(); themeStack.add(theme)
        textStyleStack.clear(); textStyleStack.add(textStyle)
        fontStack.clear(); fontStack.add(font)
        shapeStack.clear(); shapeStack.add(null)
        alphaStack.clear(); alphaStack.add(1f)
    }
}
