// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color

@AwakeUiDsl
sealed class UiDslScope protected constructor(
    protected val scope: UiScope
) {
    val context: UiContext get() = scope.context
    val font get() = scope.font
    val theme: UiTheme get() = scope.theme
    val textScale: Float get() = scope.textScale

    fun text(
        label: String,
        modifier: UiModifier = UiModifier(),
        style: Style = Style.Empty,
        centered: Boolean = false,
        wrap: UiTextWrap = UiTextWrap.None,
        overflow: UiTextOverflow = UiTextOverflow.Visible,
        maxLines: Int = if (wrap == UiTextWrap.None) 1 else Int.MAX_VALUE
    ): UiSlot {
        val resolvedFont = checkNotNull(scope.font) { "DSL text() requires a font on the enclosing UiScope." }
        val resolved = scope.resolveStyle(
            style = style,
            defaults = Style {
                foreground(scope.theme.tokens.foreground)
            }
        )
        val glyphPx = resolvedFont.cellSize * scope.resolvedTextScale()
        val defaultWidth: Dimension = when {
            modifier.width != null -> requireNotNull(modifier.width)
            wrap != UiTextWrap.None || overflow != UiTextOverflow.Visible || label.contains('\n') -> {
                if (scope.fillWidthOrNull() != null) Dimension.FillMax else Dimension.Fixed((label.length * glyphPx + resolved.contentPadding.dslHorizontalPx()).px)
            }
            else -> Dimension.Fixed((label.length * glyphPx + resolved.contentPadding.dslHorizontalPx()).px)
        }
        val availableTextWidth = when (defaultWidth) {
            is Dimension.Fixed -> (defaultWidth.dp.toPx() - resolved.contentPadding.dslHorizontalPx()).coerceAtLeast(glyphPx)
            Dimension.FillMax -> (scope.fillWidthOrNull()?.minus(resolved.contentPadding.dslHorizontalPx()))?.coerceAtLeast(glyphPx) ?: glyphPx
            Dimension.WrapContent -> glyphPx
        }
        val layout = layoutBitmapText(
            label = label,
            glyphPx = glyphPx,
            maxWidthPx = availableTextWidth,
            wrap = wrap,
            overflow = overflow,
            maxLines = maxLines
        )
        val lineGap = glyphPx * 0.25f
        val blockHeight = layout.blockHeight(glyphPx, lineGap)
        val slot = scope.claimSlot(
            defaultWidth,
            modifier.height ?: Dimension.Fixed((blockHeight + resolved.contentPadding.dslVerticalPx()).px)
        )
        if (resolved.background != null || resolved.borderWidth.toPx() > 0f || resolved.shapeSpec != null || resolved.shape.toPx() > 0f) {
            scope.emitFillAndBorder(
                slot = slot,
                fillColor = resolved.background ?: DslTransparentColor,
                radiusPx = resolved.shape.toPx(),
                borderWidth = resolved.borderWidth,
                borderColor = resolved.borderColor ?: scope.theme.tokens.border,
                shapeSpec = resolved.shapeSpec
            )
        }
        scope.text(
            label = label,
            slot = slot.inset(resolved.contentPadding),
            font = resolvedFont,
            color = resolved.foreground ?: scope.theme.tokens.foreground,
            centered = centered,
            wrap = wrap,
            overflow = overflow,
            maxLines = maxLines
        )
        return slot
    }

    fun buttonSlot(
        id: String,
        label: String,
        width: Float = 0f,
        height: Float = 36f,
        modifier: UiModifier = UiModifier(),
        style: Style = Style.Empty,
        variant: UiButtonVariant = UiButtonVariant.Filled,
        radius: Dp = UiShape.none
    ): UiButtonResult = scope.buttonSlot(id, width, height, label, modifier, style, variant, radius)

    fun button(
        id: String,
        label: String,
        width: Float = 0f,
        height: Float = 36f,
        modifier: UiModifier = UiModifier(),
        style: Style = Style.Empty,
        variant: UiButtonVariant = UiButtonVariant.Filled,
        radius: Dp = UiShape.none
    ): Boolean = buttonSlot(id, label, width, height, modifier, style, variant, radius).clicked

    fun checkbox(
        id: String,
        checked: Boolean,
        label: String? = null,
        width: Float = 0f,
        height: Float = 24f,
        modifier: UiModifier = UiModifier(),
        style: Style = Style.Empty,
        boxSize: Dp = 16f.dp
    ): Boolean = scope.checkbox(id, checked, width, height, label, modifier, style, boxSize)

    fun toggle(
        id: String,
        checked: Boolean,
        width: Float = 0f,
        height: Float = 32f,
        label: String? = null,
        modifier: UiModifier = UiModifier(),
        style: Style = Style.Empty
    ): Boolean = scope.toggle(id, checked, width, height, label, modifier, style)

    fun dropdown(
        id: String,
        options: List<String>,
        selectedIndex: Int,
        width: Float = 0f,
        height: Float = 28f,
        modifier: UiModifier = UiModifier(),
        style: Style = Style.Empty
    ): Int? = scope.dropdown(id, options, selectedIndex, width, height, modifier, style)

    fun slider(
        id: String,
        min: Float,
        max: Float,
        value: Float,
        width: Float,
        height: Float,
        label: String? = null,
        modifier: UiModifier = UiModifier(),
        style: Style = Style.Empty
    ): Float = scope.slider(id, min, max, value, width, height, label, modifier, style)

    fun separator(
        width: Dimension = Dimension.FillMax,
        thickness: Dp = 1f.dp,
        modifier: UiModifier = UiModifier(),
        color: Color = theme.tokens.border
    ): UiSlot = scope.separator(width, thickness, modifier, color)

    fun popup(
        anchorSlot: UiSlot,
        expanded: Boolean,
        width: Dimension = Dimension.WrapContent,
        height: Dimension = Dimension.WrapContent,
        gap: Float = UiSpacing.sm.toPx(),
        insets: UiInsets = UiInsets.Zero,
        positionProvider: UiPopupPositionProvider = UiPopupDefaults.dropdown(),
        properties: UiPopupProperties = UiPopupProperties(),
        content: UiColumnDslScope.(slot: UiSlot) -> Unit
    ): UiPopupResult = scope.popup(
        anchorSlot = anchorSlot,
        expanded = expanded,
        width = width,
        height = height,
        gap = gap,
        insets = insets,
        positionProvider = positionProvider,
        properties = properties
    ) { slot ->
        UiColumnDslScope(this).content(slot)
    }
}
