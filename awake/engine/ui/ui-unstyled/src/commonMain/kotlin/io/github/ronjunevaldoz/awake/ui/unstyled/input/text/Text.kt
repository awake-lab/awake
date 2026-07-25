package io.github.ronjunevaldoz.awake.ui.unstyled.input.text

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.graphics.emitFillAndBorder
import io.github.ronjunevaldoz.awake.ui.fillWidthOrNull
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.font.measureTextWidth
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.shimmer
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.resolveGlyphPx
import io.github.ronjunevaldoz.awake.ui.resolveStyle
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

internal fun UiScope.drawResolvedText(
    label: String,
    slot: UiSlot,
    resolvedFont: UiFont,
    resolvedStyle: ResolvedStyle,
    color: Color? = null,
    textStyle: TextStyle = resolvedStyle.textStyle,
    centered: Boolean = false,
    verticallyCentered: Boolean = centered,
    wrap: UiTextWrap = UiTextWrap.None,
    overflow: UiTextOverflow = UiTextOverflow.Visible,
    maxLines: Int = if (wrap == UiTextWrap.None) 1 else Int.MAX_VALUE,
    semanticId: String? = null,
    semanticRole: UiSemanticRole = UiSemanticRole.Text,
    shimmer: Boolean = false
): UiSlot {
    val theme = context.currentTheme
    if (
        resolvedStyle.background != null ||
        resolvedStyle.borderWidth.toPx() > 0f ||
        resolvedStyle.shapeSpec != null ||
        resolvedStyle.shape.toPx() > 0f
    ) {
        emitFillAndBorder(
            slot = slot,
            fillColor = resolvedStyle.background ?: Color.Transparent,
            radiusPx = resolvedStyle.shape.toPx(),
            borderWidth = resolvedStyle.borderWidth,
            borderColor = resolvedStyle.borderColor ?: theme.tokens.border,
            shapeSpec = resolvedStyle.shapeSpec
        )
    }
    renderTextBlock(
        label = label,
        slot = slot.inset(resolvedStyle.contentPadding),
        font = resolvedFont,
        color = color ?: resolvedStyle.foreground ?: textStyle.color ?: context.currentTextStyle.color ?: theme.tokens.foreground,
        centered = centered,
        verticallyCentered = verticallyCentered,
        wrap = wrap,
        overflow = overflow,
        maxLines = maxLines,
        textStyle = textStyle,
        semanticId = semanticId,
        semanticRole = semanticRole,
        shimmer = shimmer
    )
    return slot
}

fun UiScope.text(
    label: String,
    slot: UiSlot,
    style: Style = Style.Empty,
    font: UiFont = context.currentFont,
    color: Color? = null,
    centered: Boolean = false,
    verticallyCentered: Boolean = centered,
    wrap: UiTextWrap = UiTextWrap.None,
    overflow: UiTextOverflow = UiTextOverflow.Visible,
    maxLines: Int = if (wrap == UiTextWrap.None) 1 else Int.MAX_VALUE,
    textStyle: TextStyle? = null,
    semanticId: String? = null,
    semanticRole: UiSemanticRole = UiSemanticRole.Text,
    shimmer: Boolean = false
): UiSlot {
    val theme = context.currentTheme
    val resolved = resolveStyle(
        style = style,
        defaults = Style {
            if (context.currentTextStyle.color == null) {
                foreground(theme.tokens.foreground)
            }
        },
        state = MutableStyleState(
            hovered = hitTest(slot)
        )
    )
    return drawResolvedText(
        label = label,
        slot = slot,
        resolvedFont = font,
        resolvedStyle = resolved,
        color = color,
        textStyle = textStyle ?: resolved.textStyle,
        centered = centered,
        verticallyCentered = verticallyCentered,
        wrap = wrap,
        overflow = overflow,
        maxLines = maxLines,
        semanticId = semanticId,
        semanticRole = semanticRole,
        shimmer = shimmer
    )
}

/**
 * DSL version of [text] that supports [UiModifier] and [Style] resolution.
 * It claims a slot and draws the text within it.
 */
fun UiScope.text(
    label: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    font: UiFont = context.currentFont,
    color: Color? = null,
    centered: Boolean = false,
    verticallyCentered: Boolean = centered,
    wrap: UiTextWrap = UiTextWrap.None,
    overflow: UiTextOverflow = UiTextOverflow.Visible,
    maxLines: Int = if (wrap == UiTextWrap.None) 1 else Int.MAX_VALUE,
    textStyle: TextStyle? = context.currentTextStyle,
    semanticId: String? = null,
    semanticRole: UiSemanticRole = UiSemanticRole.Text
): UiSlot {
    val resolvedFont = font
    val theme = context.currentTheme
    val resolvedSemanticId = semanticId ?: if (modifier.shimmer) modifier.testTag ?: "shimmer-${label.hashCode()}" else null

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

    fun resolveAndMeasure(state: MutableStyleState): Pair<ResolvedStyle, Float> {
        val resolved = resolveStyle(
            style = style,
            defaults = Style {
                if (context.currentTextStyle.color == null) {
                    foreground(theme.tokens.foreground)
                }
            },
            state = state
        )
        val textStyle = resolved.textStyle
        val glyphPx = resolveGlyphPx(resolvedFont, textStyle)
        val labelWidthPx = resolvedFont.measureTextWidth(label, glyphPx)
        return resolved to labelWidthPx
    }

    val (initialResolved, initialLabelWidthPx) = resolveAndMeasure(styleState)
    var resolved = initialResolved
    var labelWidthPx = initialLabelWidthPx
    var textStyle = resolved.textStyle
    var glyphPx = resolveGlyphPx(resolvedFont, textStyle)
    val defaultWidth: Dimension = when {
        modifier.widthDimension != null -> requireNotNull(modifier.widthDimension)
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
        modifier.withSizeFallback(
            defaultWidth,
            Dimension.Fixed((blockHeight + resolved.contentPadding.verticalPx()).px)
        )
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
    return drawResolvedText(
        label = label,
        slot = slot,
        resolvedFont = resolvedFont,
        resolvedStyle = resolved,
        color = color,
        centered = centered,
        verticallyCentered = verticallyCentered,
        textStyle = textStyle,
        wrap = wrap,
        overflow = overflow,
        maxLines = maxLines,
        semanticId = resolvedSemanticId,
        semanticRole = semanticRole,
        shimmer = modifier.shimmer
    )
}
