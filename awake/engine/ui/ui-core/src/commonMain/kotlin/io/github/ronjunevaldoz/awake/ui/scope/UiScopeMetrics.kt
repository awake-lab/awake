// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.scope

import io.github.ronjunevaldoz.awake.ui.UiDensity
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layout.place
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.FillAwareScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.resolveAgainst
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle
import io.github.ronjunevaldoz.awake.ui.toPx
import kotlin.math.roundToInt
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.Dp

fun pixelPerfectTextScale(requestedScale: Float, step: Float = 0.25f): Float {
    val safeStep = step.takeIf { it.isFinite() && it > 0f } ?: 0.25f
    val snapped = (requestedScale / safeStep).roundToInt()
        .coerceAtLeast((1f / safeStep).roundToInt()) * safeStep
    return snapped.coerceAtLeast(1f)
}

fun pixelPerfectPixel(value: Float): Float = value.roundToInt().toFloat()

fun UiScope.resolvedTextScale(): Float =
    pixelPerfectTextScale(context.currentTextStyle.scale, context.currentFont.textScaleStep)

fun UiScope.resolveGlyphPx(
    font: UiFont = context.currentFont,
    textStyle: TextStyle = context.currentTextStyle,
): Float {
    val baseSize = textStyle.size ?: context.currentTheme.typography.body
    val scale = pixelPerfectTextScale(textStyle.scale, font.textScaleStep)
    return pixelPerfectPixel(baseSize.value * UiDensity.scale * UiDensity.fontScale * scale)
        .coerceAtLeast(1f)
}

fun UiScope.fillWidthOrNull(): Float? = (this as? FillAwareScope)?.fillWidth

fun UiScope.fillHeightOrNull(): Float? = (this as? FillAwareScope)?.fillHeight

fun UiScope.hasBoundedFillWidth(): Boolean = (this as? FillAwareScope)?.hasBoundedFillWidth == true

fun UiScope.hasBoundedFillHeight(): Boolean = (this as? FillAwareScope)?.hasBoundedFillHeight == true

fun UiScope.debugScopeLabel(): String {
    val typeName = this::class.simpleName ?: "UiScope"
    val name = (this as? FillAwareScope)?.testTag
    return if (name.isNullOrBlank()) typeName else "'$name' ($typeName)"
}

fun UiScope.claimModifiedSlot(modifier: UiModifier = Modifier): UiBounds {
    // A weighted child's own width/height defaults to WrapContent below (no Dimension set by
    // weight() itself, see UiModifier), which Dimension.resolveAgainst() can't handle -- weight
    // only sizes the row/column's main axis, so on that axis "unset" must resolve like FillMax
    // instead (the row/column then works out the child's real weighted share once every
    // sibling's width/height is known -- see resolveWeightedMainAxis()).
    val requestedWidth =
        modifier.widthDimension ?: (if (modifier.layoutWeight != null) Dimension.FillMax else Dimension.WrapContent)
    val requestedHeight =
        modifier.heightDimension ?: (if (modifier.layoutWeight != null) Dimension.FillMax else Dimension.WrapContent)
    // Clamp the request before claiming, not the result after: claimSlot() reserves the space,
    // and place() cannot hand back more than was reserved -- so a minimum applied afterwards
    // would silently do nothing while a maximum appeared to work.
    val boundedWidth = requestedWidth.clampToBounds(modifier.minWidth, modifier.maxWidth)
    val boundedHeight = requestedHeight.clampToBounds(modifier.minHeight, modifier.maxHeight)
    val containerSlot = claimSlot(boundedWidth, boundedHeight, modifier.layoutWeight)
    val width = boundedWidth.resolveAgainst(containerSlot.width)
        .clampToBounds(modifier.minWidth?.toPx(), modifier.maxWidth?.toPx())
    val height = boundedHeight.resolveAgainst(containerSlot.height)
        .clampToBounds(modifier.minHeight?.toPx(), modifier.maxHeight?.toPx())
    // claimSlot() above already recorded containerSlot for measurement purposes -- recording
    // the aligned/inset/offset placement again here would double-count this single widget claim
    // in measured.slots (corrupting row/column child-count-sized aggregates like arrangement
    // distribution and weight division), so this placed rect is deliberately not re-recorded.
    return crossAxisAlignmentContainer(containerSlot).place(
        width = width,
        height = height,
        alignment = modifier.alignment ?: defaultAlignment(),
        insets = modifier.insets,
        offsetX = modifier.offsetX.toPx(),
        offsetY = modifier.offsetY.toPx(),
    )
}

private fun UiScope.defaultAlignment(): UiAlignment = when (this) {
    is BoxScope -> contentAlignment
    // A row's own horizontal position on the main axis is already fully determined by
    // arrangement/cursor placement (see RowScope.claimSlot) -- Horizontal.Start here is a
    // no-op, only the Vertical component ever has room to act.
    is RowScope -> UiAlignment.of(verticalAlignment, UiAlignment.Horizontal.Start)
    is ColumnScope -> UiAlignment.of(UiAlignment.Vertical.Top, horizontalAlignment)
    else -> UiAlignment.TopStart
}

/** Widens [containerSlot]'s cross-axis size (only, and only ever upward -- see below) to the
 * row/column's own full cross-axis extent, so [defaultAlignment]'s verticalAlignment/
 * horizontalAlignment (and an explicit per-child `.align(...)`) has real slack to center/end a
 * shorter child into. Deliberately not done inside [RowScope.claimSlot]/[ColumnScope.claimSlot]
 * themselves -- those keep returning a request-shaped rect because plenty of existing call
 * sites read that returned rect directly for non-alignment purposes.
 *
 * `coerceAtLeast(containerSlot.*)` rather than an unconditional swap: [RowScope.height]/
 * [ColumnScope.width] is a snapshot taken at scope-construction time, and a handful of existing
 * call sites (test fixtures in particular) build/reuse a scope before a frame has ever begun,
 * where that snapshot is a stale 0 -- widening down to 0 would make [place] clamp every child's
 * real, already-resolved size down to nothing instead of just leaving it un-centered. Only ever
 * growing the container guarantees this can't regress a single existing call site; it just
 * silently gives up centering for that (already degenerate) one.
 */
private fun UiScope.crossAxisAlignmentContainer(containerSlot: UiBounds): UiBounds = when (this) {
    is RowScope -> containerSlot.copy(height = height.coerceAtLeast(containerSlot.height))
    is ColumnScope -> containerSlot.copy(width = width.coerceAtLeast(containerSlot.width))
    else -> containerSlot
}


/** Applies whichever of [min]/[max] were supplied. A max below min loses to min, matching
 * Compose, where the minimum constraint wins. */
private fun Float.clampToBounds(min: Float?, max: Float?): Float {
    var value = this
    if (max != null) value = value.coerceAtMost(max)
    if (min != null) value = value.coerceAtLeast(min)
    return value
}

/** Applies min/max to a sizing request. Only a [Dimension.Fixed] request can be bounded up
 * front; [Dimension.FillMax] already means "whatever the parent offers", and clamping it here
 * would change what gets reserved rather than what gets used. */
private fun Dimension.clampToBounds(min: Dp?, max: Dp?): Dimension {
    if (this !is Dimension.Fixed || (min == null && max == null)) return this
    return Dimension.Fixed(dp.value.clampToBounds(min?.value, max?.value).dp)
}
