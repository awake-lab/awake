// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.font.measureTextWidth

/**
 * DSL version of [text] that supports [UiModifier] and [Style] resolution.
 * It claims a slot and draws the text within it.
 */
fun UiScope.text(
    label: String,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty,
    centered: Boolean = false,
    wrap: UiTextWrap = UiTextWrap.None,
    overflow: UiTextOverflow = UiTextOverflow.Visible,
    maxLines: Int = if (wrap == UiTextWrap.None) 1 else Int.MAX_VALUE
): UiSlot {
    val resolvedFont = checkNotNull(font) { "DSL text() requires a font on the enclosing UiScope." }
    val resolved = resolveStyle(
        style = style,
        defaults = Style {
            foreground(theme.tokens.foreground)
        }
    )
    val glyphPx = resolveGlyphPx(resolvedFont, resolved.textScale, resolved.textSize)
    val labelWidthPx = resolvedFont.measureTextWidth(label, glyphPx)
    val defaultWidth: Dimension = when {
        modifier.width != null -> requireNotNull(modifier.width)
        wrap != UiTextWrap.None || overflow != UiTextOverflow.Visible || label.contains('\n') -> {
            if (fillWidthOrNull() != null) Dimension.FillMax else Dimension.Fixed((labelWidthPx + resolved.contentPadding.dslHorizontalPx()).px)
        }
        else -> Dimension.Fixed((labelWidthPx + resolved.contentPadding.dslHorizontalPx()).px)
    }
    val availableTextWidth = when (defaultWidth) {
        is Dimension.Fixed -> (defaultWidth.dp.toPx() - resolved.contentPadding.dslHorizontalPx()).coerceAtLeast(glyphPx)
        Dimension.FillMax -> (fillWidthOrNull()?.minus(resolved.contentPadding.dslHorizontalPx()))?.coerceAtLeast(glyphPx) ?: 4096f
        Dimension.WrapContent -> glyphPx
    }
    val layout = layoutBitmapText(
        label = label,
        glyphPx = glyphPx,
        maxWidthPx = availableTextWidth,
        wrap = wrap,
        overflow = overflow,
        maxLines = maxLines,
        advanceOf = { char -> resolvedFont.advanceFor(char, glyphPx) }
    )
    val lineGap = glyphPx * 0.25f
    val blockHeight = layout.blockHeight(glyphPx, lineGap)
    val slot = claimModifiedSlot(
        defaultWidth,
        Dimension.Fixed((blockHeight + resolved.contentPadding.dslVerticalPx()).px),
        modifier
    )
    if (resolved.background != null || resolved.borderWidth.toPx() > 0f || resolved.shapeSpec != null || resolved.shape.toPx() > 0f) {
        emitFillAndBorder(
            slot = slot,
            fillColor = resolved.background ?: DslTransparentColor,
            radiusPx = resolved.shape.toPx(),
            borderWidth = resolved.borderWidth,
            borderColor = resolved.borderColor ?: theme.tokens.border,
            shapeSpec = resolved.shapeSpec
        )
    }
    this.text(
        label = label,
        slot = slot.inset(resolved.contentPadding),
        font = resolvedFont,
        color = resolved.foreground ?: theme.tokens.foreground,
        centered = centered,
        wrap = wrap,
        overflow = overflow,
        maxLines = maxLines,
        textScale = resolved.textScale,
        textSize = resolved.textSize
    )
    return slot
}

/** Convenience sugar for [switchWidget] since `switch` is a keyword in many languages
 * (though not Kotlin) and often preferred in UI DSLs. */
fun UiScope.switch(
    id: String,
    checked: Boolean,
    label: String? = null,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Boolean = switchWidget(id, checked, label, modifier, style)
