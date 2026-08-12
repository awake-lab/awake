// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.unstyled

import io.github.ronjunevaldoz.awake.ui.UiPrimitiveScope
import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.font.measureTextWidth
import io.github.ronjunevaldoz.awake.ui.layout.horizontalPx
import io.github.ronjunevaldoz.awake.ui.layout.verticalPx
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.scope.resolveGlyphPx
import io.github.ronjunevaldoz.awake.ui.scope.resolveStyle
import io.github.ronjunevaldoz.awake.ui.style.MutableStyleState
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.resolveTextLineMetrics
import io.github.ronjunevaldoz.awake.ui.toPx
import kotlin.math.ceil

/**
 * Applies a content-derived width when the caller did not provide one.
 *
 * Primitive widgets claim their slot immediately, so they cannot pass [Dimension.WrapContent]
 * through to the core layout scopes. Measuring the label here gives them the same natural-size
 * behavior as a Compose control while still preserving explicit `width(...)` and
 * `fillMaxWidth()` modifiers.
 */
internal fun UiPrimitiveScope.withIntrinsicLabelWidth(
    modifier: UiModifier,
    label: String,
    style: Style = Style.Empty,
    defaults: Style = Style.Empty,
    extraWidth: Dp = 0f.dp,
): UiModifier {
    if (modifier.widthDimension != null || modifier.layoutWeight != null) return modifier

    val resolved = resolveStyle(
        style = style,
        defaults = defaults,
        state = MutableStyleState(),
    )
    val glyphPx = resolveGlyphPx(textStyle = resolved.textStyle)
    val labelWidthPx = context.currentFont.measureTextWidth(label, glyphPx, resolved.textStyle.weight)
    // CSS borders participate in the element's intrinsic border-box even when their color is
    // transparent (`border border-transparent` is part of shadcn Button and Badge). Resolve the
    // width from the style instead of compensating in individual recipes.
    val borderPx = resolved.borderWidth.toPx() * 2f
    val widthPx = ceil(labelWidthPx + resolved.contentPadding.horizontalPx() + borderPx + extraWidth.toPx())
    return modifier.width(widthPx.px)
}

/**
 * Applies both natural label width and the font-metric-derived one-line height.
 *
 * Composite compact controls (badges/pills) cannot rely on a WrapContent child measurement when
 * their parent is already running a measurement pass. Reserving the same line box that text()
 * uses keeps the surface large enough for its content without introducing a component-specific
 * magic height.
 */
internal fun UiPrimitiveScope.withIntrinsicLabelSize(
    modifier: UiModifier,
    label: String,
    style: Style = Style.Empty,
    defaults: Style = Style.Empty,
    extraWidth: Dp = 0f.dp,
): UiModifier {
    if (modifier.layoutWeight != null) return modifier
    val resolved = resolveStyle(
        style = style,
        defaults = defaults,
        state = MutableStyleState(),
    )
    val glyphPx = resolveGlyphPx(textStyle = resolved.textStyle)
    val result = if (modifier.widthDimension == null) {
        val labelWidthPx = context.currentFont.measureTextWidth(label, glyphPx, resolved.textStyle.weight)
        val borderPx = resolved.borderWidth.toPx() * 2f
        modifier.width(ceil(labelWidthPx + resolved.contentPadding.horizontalPx() + borderPx + extraWidth.toPx()).px)
    } else {
        modifier
    }
    if (result.heightDimension != null) return result
    val lineMetrics = resolveTextLineMetrics(context.currentFont, glyphPx, resolved.textStyle)
    val borderPx = resolved.borderWidth.toPx() * 2f
    return result.height(
        (lineMetrics.lineHeightPx + lineMetrics.lineGapPx + resolved.contentPadding.verticalPx() + borderPx).px,
    )
}
