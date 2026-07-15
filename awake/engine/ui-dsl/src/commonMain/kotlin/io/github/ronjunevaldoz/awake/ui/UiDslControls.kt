// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

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
        centered: Boolean = false
    ): UiSlot {
        val resolvedFont = checkNotNull(scope.font) { "DSL text() requires a font on the enclosing UiScope." }
        val resolved = scope.resolveStyle(
            style = style,
            defaults = Style {
                foreground(scope.theme.tokens.foreground)
            }
        )
        val glyphPx = resolvedFont.cellSize * scope.resolvedTextScale()
        val slot = scope.claimSlot(
            modifier.width ?: Dimension.Fixed((label.length * glyphPx + resolved.contentPadding.dslHorizontalPx()).px),
            modifier.height ?: Dimension.Fixed((glyphPx + resolved.contentPadding.dslVerticalPx()).px)
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
            centered = centered
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
}
