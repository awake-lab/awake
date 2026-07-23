// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.FillAwareScope
import io.github.ronjunevaldoz.awake.ui.layouts.resolveAgainst
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle
import kotlin.math.roundToInt

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
    textStyle: TextStyle = context.currentTextStyle
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

fun UiScope.claimModifiedSlot(
    defaultWidth: Dimension = Dimension.WrapContent,
    defaultHeight: Dimension = Dimension.WrapContent,
    modifier: UiModifier = UiModifier()
): UiSlot {
    val requestedWidth = modifier.width ?: defaultWidth
    val requestedHeight = modifier.height ?: defaultHeight
    val containerSlot = claimSlot(requestedWidth, requestedHeight)
    val width = requestedWidth.resolveAgainst(containerSlot.width)
    val height = requestedHeight.resolveAgainst(containerSlot.height)
    return containerSlot.place(
        width = width,
        height = height,
        alignment = modifier.alignment ?: defaultAlignment(),
        insets = modifier.insets,
        offsetX = modifier.offsetX.toPx(),
        offsetY = modifier.offsetY.toPx()
    ).also(context::recordMeasuredSlot)
}

private fun UiScope.defaultAlignment(): UiAlignment = when (this) {
    is BoxScope -> contentAlignment
    else -> UiAlignment.TopStart
}
