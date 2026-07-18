// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.font.measureTextWidth

@AwakeUiDsl
sealed class UiDslScope protected constructor(
    protected val scope: UiScope
) {
    val context: UiContext get() = scope.context
    val font get() = scope.font
    val theme: UiTheme get() = scope.theme
    val textScale: Float get() = scope.textScale
    val emitsToOverlay: Boolean get() = scope.emitsToOverlay

    /** Nested-scope builders for DSL layout functions (row/column/absolute/box) -- delegate to
     * [scope]'s own [childColumn]/[childRow]/[childAbsolute]/[childBox], so every DSL nesting
     * call site automatically inherits [emitsToOverlay] the same way a [UiScope]-level widget
     * would, without repeating `overlayOnly = emitsToOverlay` by hand. Declared here (not as
     * top-level [UiScope] extensions called with an explicit receiver) so they resolve
     * correctly even when the lexical `this` inside a DSL builder lambda is a *different*
     * [UiDslScope] subtype than the one whose function body it's written in -- see
     * [UiPropertyDsl.propertyRow]'s `content` lambda, which runs with a freshly-constructed
     * [UiAbsoluteDslScope] as its receiver. */
    fun childColumn(slot: UiSlot, gap: Float = UiSpacing.sm.toPx(), insets: UiInsets = UiInsets.Zero): ColumnScope =
        scope.childColumn(slot, gap, insets, textScale)

    fun childRow(slot: UiSlot, gap: Float = UiSpacing.sm.toPx(), insets: UiInsets = UiInsets.Zero): RowScope =
        scope.childRow(slot, gap, insets, textScale)

    fun childAbsolute(slot: UiSlot, insets: UiInsets = UiInsets.Zero): AbsoluteScope =
        scope.childAbsolute(slot, insets, textScale)

    fun childBox(slot: UiSlot, insets: UiInsets = UiInsets.Zero, contentAlignment: UiAlignment = UiAlignment.TopStart): BoxScope =
        scope.childBox(slot, insets, contentAlignment, textScale)

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
        val glyphPx = scope.resolveGlyphPx(resolvedFont, resolved.textScale, resolved.textSize)
        val labelWidthPx = resolvedFont.measureTextWidth(label, glyphPx)
        val defaultWidth: Dimension = when {
            modifier.width != null -> requireNotNull(modifier.width)
            wrap != UiTextWrap.None || overflow != UiTextOverflow.Visible || label.contains('\n') -> {
                if (scope.fillWidthOrNull() != null) Dimension.FillMax else Dimension.Fixed((labelWidthPx + resolved.contentPadding.dslHorizontalPx()).px)
            }
            else -> Dimension.Fixed((labelWidthPx + resolved.contentPadding.dslHorizontalPx()).px)
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
            maxLines = maxLines,
            advanceOf = { char -> resolvedFont.advanceFor(char, glyphPx) }
        )
        val lineGap = glyphPx * 0.25f
        val blockHeight = layout.blockHeight(glyphPx, lineGap)
        val slot = scope.claimModifiedSlot(
            defaultWidth,
            Dimension.Fixed((blockHeight + resolved.contentPadding.dslVerticalPx()).px),
            modifier
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
            maxLines = maxLines,
            textScale = resolved.textScale,
            textSize = resolved.textSize
        )
        return slot
    }

    fun buttonSlot(
        id: String,
        label: String,
        modifier: UiModifier = UiModifier(),
        style: Style = Style.Empty,
        variant: UiButtonVariant = UiButtonVariant.Filled,
        radius: Dp = UiShape.none,
        centered: Boolean = true,
        verticallyCentered: Boolean = centered
    ): UiButtonResult = scope.buttonSlot(
        id = id,
        label = label,
        modifier = modifier,
        style = style,
        variant = variant,
        radius = radius,
        centered = centered,
        verticallyCentered = verticallyCentered
    )

    fun buttonSlot(
        id: String,
        modifier: UiModifier = UiModifier(),
        style: Style = Style.Empty,
        variant: UiButtonVariant = UiButtonVariant.Filled,
        radius: Dp = UiShape.none,
        content: UiAbsoluteDslScope.(slot: UiSlot) -> Unit
    ): UiButtonResult = scope.buttonSlot(
        id = id,
        modifier = modifier,
        style = style,
        variant = variant,
        radius = radius
    ) { slot ->
        UiAbsoluteDslScope(this).content(slot)
    }

    /** Draws a select-style trigger's content (label left-aligned, expand chevron right) on
     * an already-claimed slot -- see [io.github.ronjunevaldoz.awake.ui.drawDropdownTriggerContent].
     * Exposed here since [scope] is protected and a custom dropdown built from this DSL scope
     * (e.g. a design-system layer's own popup shape) still needs the same trigger visuals. */
    fun drawDropdownTriggerContent(
        slot: UiSlot,
        label: String,
        expanded: Boolean,
        style: Style,
        semanticId: String? = null
    ) = scope.drawDropdownTriggerContent(slot, label, expanded, style, semanticId)

    fun recordSemantic(
        role: UiSemanticRole,
        bounds: UiSlot,
        id: String? = null,
        label: String? = null,
        contentBounds: UiSlot? = null,
        clippedBounds: UiSlot? = null,
        truncated: Boolean = false,
        lineCount: Int = 0,
        selected: Boolean? = null
    ) = scope.recordSemantic(
        role = role,
        bounds = bounds,
        id = id,
        label = label,
        contentBounds = contentBounds,
        clippedBounds = clippedBounds,
        truncated = truncated,
        lineCount = lineCount,
        selected = selected
    )

    fun button(
        id: String,
        label: String,
        modifier: UiModifier = UiModifier(),
        style: Style = Style.Empty,
        variant: UiButtonVariant = UiButtonVariant.Filled,
        radius: Dp = UiShape.none,
        centered: Boolean = true,
        verticallyCentered: Boolean = centered
    ): Boolean = buttonSlot(
        id = id,
        label = label,
        modifier = modifier,
        style = style,
        variant = variant,
        radius = radius,
        centered = centered,
        verticallyCentered = verticallyCentered
    ).clicked

    fun button(
        id: String,
        modifier: UiModifier = UiModifier(),
        style: Style = Style.Empty,
        variant: UiButtonVariant = UiButtonVariant.Filled,
        radius: Dp = UiShape.none,
        content: UiAbsoluteDslScope.(slot: UiSlot) -> Unit
    ): Boolean = buttonSlot(
        id = id,
        modifier = modifier,
        style = style,
        variant = variant,
        radius = radius,
        content = content
    ).clicked

    fun checkbox(
        id: String,
        checked: Boolean,
        label: String? = null,
        modifier: UiModifier = UiModifier(),
        style: Style = Style.Empty,
        boxSize: Dp = 16f.dp
    ): Boolean = scope.checkbox(id, checked, label, modifier, style, boxSize)

    fun toggle(
        id: String,
        checked: Boolean,
        label: String? = null,
        modifier: UiModifier = UiModifier(),
        style: Style = Style.Empty
    ): Boolean = scope.toggle(id, checked, label, modifier, style)

    fun dropdown(
        id: String,
        options: List<String>,
        selectedIndex: Int,
        modifier: UiModifier = UiModifier(),
        style: Style = Style.Empty
    ): Int? = scope.dropdown(id, options, selectedIndex, modifier, style)

    fun slider(
        id: String,
        min: Float,
        max: Float,
        value: Float,
        label: String? = null,
        modifier: UiModifier = UiModifier(),
        style: Style = Style.Empty
    ): Float = scope.slider(id, min, max, value, label, modifier, style)

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

    fun scrollPanel(
        id: String,
        width: Dimension,
        height: Dimension,
        state: UiScrollState,
        gap: Float = UiSpacing.sm.toPx(),
        radius: Dp = UiShape.md,
        borderWidth: Dp = UiShape.none,
        style: Style = Style.Empty,
        modifier: UiModifier = UiModifier(),
        scrollSpeed: Float = 32f,
        scrollbarWidth: Dp = 6f.dp,
        scrollbarGap: Dp = 8f.dp,
        content: UiColumnDslScope.(slot: UiSlot) -> Unit
    ): UiScrollPanelResult = scope.scrollPanel(
        id = id,
        width = width,
        height = height,
        state = state,
        gap = gap,
        radius = radius,
        borderWidth = borderWidth,
        style = style,
        modifier = modifier,
        scrollSpeed = scrollSpeed,
        scrollbarWidth = scrollbarWidth,
        scrollbarGap = scrollbarGap
    ) { slot ->
        UiColumnDslScope(this).content(slot)
    }
}
