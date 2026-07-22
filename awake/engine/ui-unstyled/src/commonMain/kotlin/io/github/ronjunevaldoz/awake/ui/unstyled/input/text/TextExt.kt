package io.github.ronjunevaldoz.awake.ui.unstyled.input.text

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.MutableStyleState
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.core.graphics.emitFillAndBorder
import io.github.ronjunevaldoz.awake.ui.fillWidthOrNull
import io.github.ronjunevaldoz.awake.ui.font
import io.github.ronjunevaldoz.awake.ui.font.measureTextWidth
import io.github.ronjunevaldoz.awake.ui.horizontalPx
import io.github.ronjunevaldoz.awake.ui.inset
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.resolveGlyphPx
import io.github.ronjunevaldoz.awake.ui.resolveStyle
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.verticalPx

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
    val resolvedFont = font

    // We need to know whether the widget is hovered to resolve hover-dependent style state,
    // but performing a hitTest requires a measured slot. To avoid claiming a WrapContent slot
    // prematurely (which crashes in non-measuring scopes), perform a two-pass approach:
    // 1) Assume no hover (or use forced hover) and resolve style + measure;
    // 2) claim the slot and run hitTest(slot); if the actual hover differs from the assumed one,
    //    recompute style+measurement and re-claim the slot.
    val assumedHover = modifier.forceHover ?: false
    var styleState = MutableStyleState(
        hovered = assumedHover,
        active = modifier.forceActive ?: false,
        focused = modifier.forceFocus ?: false
    )

    fun resolveAndMeasure(state: MutableStyleState): Pair<io.github.ronjunevaldoz.awake.ui.ResolvedStyle, Float> {
        val resolved = resolveStyle(
            style = style,
            defaults = Style {
                foreground(theme.tokens.foreground)
            },
            state = state
        )
        val textStyle = resolved.textStyle
        val glyphPx = resolveGlyphPx(resolvedFont, textStyle)
        val labelWidthPx = resolvedFont.measureTextWidth(label, glyphPx)
        return resolved to labelWidthPx
    }

    var resolved = resolveAndMeasure(styleState).first
    var labelWidthPx = resolveAndMeasure(styleState).second
    var textStyle = resolved.textStyle
    var glyphPx = resolveGlyphPx(resolvedFont, textStyle)
    val defaultWidth: Dimension = when {
        modifier.width != null -> requireNotNull(modifier.width)
        wrap != UiTextWrap.None || overflow != UiTextOverflow.Visible || label.contains('\n') -> {
            if (fillWidthOrNull() != null) Dimension.FillMax else Dimension.Fixed((labelWidthPx + resolved.contentPadding.horizontalPx()).px)
        }
        else -> Dimension.Fixed((labelWidthPx + resolved.contentPadding.horizontalPx()).px)
    }
    val availableTextWidth = when (defaultWidth) {
        is Dimension.Fixed -> (defaultWidth.dp.toPx() - resolved.contentPadding.horizontalPx()).coerceAtLeast(glyphPx)
        Dimension.FillMax -> (fillWidthOrNull()?.minus(resolved.contentPadding.horizontalPx()))?.coerceAtLeast(glyphPx) ?: 4096f
        Dimension.WrapContent -> glyphPx
    }
    var layout = layoutBitmapText(
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
    var slot = claimModifiedSlot(
        defaultWidth,
        Dimension.Fixed((blockHeight + resolveStyle(state = styleState, style = style, defaults = Style {}).contentPadding.verticalPx()).px),
        modifier
    )

    // If hover isn't forced, check actual hover and recompute if it changed.
    // Important: do not re-claim the slot in cursor-based layouts like ColumnScope/RowScope.
    // A second claim would advance the parent cursor again and make the text visibly jump when
    // hovered. Hover may restyle the existing slot, but it must not move the widget.
    if (modifier.forceHover == null) {
        val actualHover = hitTest(slot)
        if (actualHover != styleState.hovered) {
            styleState = MutableStyleState(
                hovered = actualHover,
                active = modifier.forceActive ?: false,
                focused = modifier.forceFocus ?: false
            )
            val (newResolved, newLabelWidthPx) = resolveAndMeasure(styleState)
            resolved = newResolved
            labelWidthPx = newLabelWidthPx
            textStyle = resolved.textStyle
            glyphPx = resolveGlyphPx(resolvedFont, textStyle)
            // re-measure layout with updated text metrics
            val newAvailableTextWidth = when (defaultWidth) {
                is Dimension.Fixed -> (defaultWidth.dp.toPx() - resolved.contentPadding.horizontalPx()).coerceAtLeast(glyphPx)
                Dimension.FillMax -> (fillWidthOrNull()?.minus(resolved.contentPadding.horizontalPx()))?.coerceAtLeast(glyphPx) ?: 4096f
                Dimension.WrapContent -> glyphPx
            }
            layout = layoutBitmapText(
                label = label,
                glyphPx = glyphPx,
                maxWidthPx = newAvailableTextWidth,
                wrap = wrap,
                overflow = overflow,
                maxLines = maxLines,
                advanceOf = { char -> resolvedFont.advanceFor(char, glyphPx) }
            )
        }
    }
    if (resolved.background != null || resolved.borderWidth.toPx() > 0f || resolved.shapeSpec != null || resolved.shape.toPx() > 0f) {
        emitFillAndBorder(
            slot = slot,
            fillColor = resolved.background ?: Color.Transparent,
            radiusPx = resolved.shape.toPx(),
            borderWidth = resolved.borderWidth,
            borderColor = resolved.borderColor ?: theme.tokens.border,
            shapeSpec = resolved.shapeSpec
        )
    }
    this.basicText(
        label = label,
        slot = slot.inset(resolved.contentPadding),
        font = resolvedFont,
        color = resolved.foreground ?: theme.tokens.foreground,
        centered = centered,
        wrap = wrap,
        overflow = overflow,
        maxLines = maxLines,
        textStyle = textStyle
    )
    return slot
}
