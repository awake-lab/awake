// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless.internal.controls

import io.github.ronjunevaldoz.awake.ui.headless.internal.layout.interact
import io.github.ronjunevaldoz.awake.ui.headless.internal.layout.UiInteraction
import io.github.ronjunevaldoz.awake.ui.headless.internal.layout.withIntrinsicLabelWidth

import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.font.measureTextWidth
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.headless.internal.text.UiBitmapTextLayout
import io.github.ronjunevaldoz.awake.ui.headless.internal.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.headless.internal.text.UiTextWrap
import io.github.ronjunevaldoz.awake.ui.headless.internal.text.layoutBitmapText

/**
 * The subset of a menu item's shape [intrinsicMenuWidthPx] needs -- deliberately narrower than
 * a real menu-item type (icon content, destructive/enabled flags, etc. are decoration/behavior
 * a caller's own item type still owns) so this stays a pure width calculation, not a second
 * competing menu-item data model.
 */
data class UiMenuItemWidthInput(
    val label: String,
    val trailingLabel: String?,
    val hasIcon: Boolean,
)

/**
 * Pure geometry for a single menu-item row -- label/icon/trailing/supporting-text placement and
 * the row's own height. No color, theme, or decoration: any menu skin draws using these
 * positions, this function only answers "where does everything go."
 */
data class UiMenuItemLayout(
    val labelStart: Float,
    val bodyWidth: Float,
    val computedHeight: Float,
    val labelAlignment: UiAlignment,
    val trailingAlignment: UiAlignment,
    val verticalPadding: Dp,
    val supportingLayout: UiBitmapTextLayout?,
)

/**
 * Moved out of `ui-designsystem`'s `dropdownMenuItem` verbatim -- the math itself never
 * referenced a theme color or a shadcn constant, only [trailingLabel]/[hasIcon]/
 * [supportingText] plus the row's own [width]/[baseHeight]/[font]/[glyphPx]. The label's own
 * text isn't measured here (it draws with ellipsis overflow at [bodyWidth], not a pre-measured
 * width) -- drawing (button background, text color, `Style`) stays with the caller.
 */
fun measureMenuItemLayout(
    trailingLabel: String?,
    hasIcon: Boolean,
    supportingText: String?,
    width: Float,
    baseHeight: Float,
    font: UiFont,
    glyphPx: Float,
): UiMenuItemLayout {
    val trailingWidth = trailingLabel?.let { font.measureTextWidth(it, glyphPx) + 8f } ?: 0f
    // shadcn px-2 = 8dp horizontal item padding.
    val iconSlotWidth = if (hasIcon) glyphPx + 8f else 0f
    val labelStart = 8f.dp.toPx() + iconSlotWidth
    val bodyWidth = (width - 16f.dp.toPx() - trailingWidth - iconSlotWidth).coerceAtLeast(glyphPx)

    val lineGap = glyphPx * 0.25f
    val supportingLayout = supportingText?.takeIf { it.isNotBlank() }?.let {
        layoutBitmapText(
            label = it,
            glyphPx = glyphPx,
            maxWidthPx = (width - 16f.dp.toPx()).coerceAtLeast(glyphPx),
            wrap = UiTextWrap.Word,
            overflow = UiTextOverflow.Ellipsis,
            maxLines = 2,
            advanceOf = { char -> font.advanceFor(char, glyphPx) },
        )
    }

    val supportingHeight = supportingLayout?.blockHeight(glyphPx * font.lineHeightEm, lineGap) ?: 0f
    val computedHeight = if (supportingLayout == null) {
        baseHeight
    } else {
        // Vertical stack: 8dp top + label + 4dp gap + supporting + 8dp bottom
        maxOf(baseHeight, 8f + glyphPx + 4f + supportingHeight + 8f)
    }

    val labelAlignment =
        if (supportingLayout == null) UiAlignment.CenterStart else UiAlignment.TopStart
    val trailingAlignment =
        if (supportingLayout == null) UiAlignment.CenterEnd else UiAlignment.TopEnd
    val verticalPadding = if (supportingLayout == null) 0f.dp else 8f.dp

    return UiMenuItemLayout(
        labelStart = labelStart,
        bodyWidth = bodyWidth,
        computedHeight = computedHeight,
        labelAlignment = labelAlignment,
        trailingAlignment = trailingAlignment,
        verticalPadding = verticalPadding,
        supportingLayout = supportingLayout,
    )
}

/**
 * The width the widest entry needs: 8dp start padding, optional icon slot, the label's measured
 * ink, the trailing shortcut, and 8dp end padding. Floored at [minWidthPx] (shadcn's `min-w-32`
 * = 128dp) so a menu of one-word items still reads as a panel rather than a sliver.
 */
fun intrinsicMenuWidthPx(
    items: List<UiMenuItemWidthInput>,
    font: UiFont,
    glyphPx: Float,
    minWidthPx: Float,
): Float {
    val horizontalPadding = 16f.dp.toPx()
    val widest = items.maxOfOrNull { item ->
        val iconSlotWidth = if (item.hasIcon) glyphPx + 8f else 0f
        val trailingWidth =
            item.trailingLabel?.let { font.measureTextWidth(it, glyphPx) + 8f } ?: 0f
        font.measureTextWidth(item.label, glyphPx) + iconSlotWidth + trailingWidth
    } ?: 0f
    return (widest + horizontalPadding).coerceAtLeast(minWidthPx)
}
