// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.ColumnScope
import io.github.ronjunevaldoz.awake.ui.UiAbsoluteDslScope
import io.github.ronjunevaldoz.awake.ui.UiButtonVariant
import io.github.ronjunevaldoz.awake.ui.UiColumnDslScope
import io.github.ronjunevaldoz.awake.ui.UiDslScope
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.button
import io.github.ronjunevaldoz.awake.ui.checkbox
import io.github.ronjunevaldoz.awake.ui.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.dropdown
import io.github.ronjunevaldoz.awake.ui.emitFillAndBorder
import io.github.ronjunevaldoz.awake.ui.horizontalPx
import io.github.ronjunevaldoz.awake.ui.panel
import io.github.ronjunevaldoz.awake.ui.propertyCheckbox
import io.github.ronjunevaldoz.awake.ui.propertyRow
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.resolveStyle
import io.github.ronjunevaldoz.awake.ui.sectionTitle
import io.github.ronjunevaldoz.awake.ui.slider
import io.github.ronjunevaldoz.awake.ui.supportingText
import io.github.ronjunevaldoz.awake.ui.text
import io.github.ronjunevaldoz.awake.ui.toDimension
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.toggle
import io.github.ronjunevaldoz.awake.ui.verticalPx
import io.github.ronjunevaldoz.awake.ui.dp

enum class AwakeShadcnButtonVariant {
    Primary,
    Secondary,
    Outline,
    Ghost,
    Danger
}

enum class AwakeShadcnBadgeVariant {
    Primary,
    Secondary,
    Outline,
    Danger
}

object AwakeShadcnStyles {
    fun button(variant: AwakeShadcnButtonVariant): Style = when (variant) {
        AwakeShadcnButtonVariant.Primary -> Style {
            background(AwakeShadcnTheme.tokens.primary)
            foreground(AwakeShadcnTheme.tokens.primaryForeground)
            borderWidth(1f.dp)
            borderColor(AwakeShadcnTheme.tokens.primary)
            shape(6f.dp)
            hovered { background(dim(AwakeShadcnTheme.tokens.primary, 0.96f)) }
            active { background(dim(AwakeShadcnTheme.tokens.primary, 0.92f)) }
        }
        AwakeShadcnButtonVariant.Secondary -> AwakeShadcnTheme.components.button
        AwakeShadcnButtonVariant.Outline -> Style {
            background(TRANSPARENT)
            foreground(AwakeShadcnTheme.tokens.foreground)
            borderWidth(1f.dp)
            borderColor(AwakeShadcnTheme.tokens.border)
            shape(6f.dp)
            hovered { background(AwakeShadcnTheme.tokens.accent) }
            active { background(dim(AwakeShadcnTheme.tokens.accent, 0.92f)) }
        }
        AwakeShadcnButtonVariant.Ghost -> Style {
            background(TRANSPARENT)
            foreground(AwakeShadcnTheme.tokens.foreground)
            shape(6f.dp)
            hovered { background(AwakeShadcnTheme.tokens.accent) }
            active { background(dim(AwakeShadcnTheme.tokens.accent, 0.92f)) }
        }
        AwakeShadcnButtonVariant.Danger -> Style {
            background(AwakeShadcnTheme.tokens.destructive)
            foreground(AwakeShadcnTheme.tokens.destructiveForeground)
            borderWidth(1f.dp)
            borderColor(AwakeShadcnTheme.tokens.destructive)
            shape(6f.dp)
            hovered { background(dim(AwakeShadcnTheme.tokens.destructive, 0.94f)) }
            active { background(dim(AwakeShadcnTheme.tokens.destructive, 0.88f)) }
        }
    }

    fun badge(variant: AwakeShadcnBadgeVariant): Style = when (variant) {
        AwakeShadcnBadgeVariant.Primary -> Style {
            background(AwakeShadcnTheme.tokens.primary)
            foreground(AwakeShadcnTheme.tokens.primaryForeground)
            shape(999f.dp)
            borderWidth(1f.dp)
            borderColor(AwakeShadcnTheme.tokens.primary)
        }
        AwakeShadcnBadgeVariant.Secondary -> Style {
            background(AwakeShadcnTheme.tokens.secondary)
            foreground(AwakeShadcnTheme.tokens.secondaryForeground)
            shape(999f.dp)
            borderWidth(1f.dp)
            borderColor(AwakeShadcnTheme.tokens.border)
        }
        AwakeShadcnBadgeVariant.Outline -> Style {
            background(TRANSPARENT)
            foreground(AwakeShadcnTheme.tokens.foreground)
            shape(999f.dp)
            borderWidth(1f.dp)
            borderColor(AwakeShadcnTheme.tokens.border)
        }
        AwakeShadcnBadgeVariant.Danger -> Style {
            background(AwakeShadcnTheme.tokens.destructive)
            foreground(AwakeShadcnTheme.tokens.destructiveForeground)
            shape(999f.dp)
            borderWidth(1f.dp)
            borderColor(AwakeShadcnTheme.tokens.destructive)
        }
    }

    val field: Style = Style {
        background(AwakeShadcnTheme.card)
        foreground(AwakeShadcnTheme.tokens.foreground)
        borderWidth(1f.dp)
        borderColor(AwakeShadcnTheme.input)
        shape(8f.dp)
        hovered { background(AwakeShadcnTheme.tokens.accent) }
        active { background(dim(AwakeShadcnTheme.tokens.accent, 0.92f)) }
    }

    val checkbox: Style = Style {
        background(AwakeShadcnTheme.card)
        foreground(AwakeShadcnTheme.tokens.foreground)
        borderWidth(1f.dp)
        borderColor(AwakeShadcnTheme.input)
        shape(6f.dp)
        hovered { background(AwakeShadcnTheme.tokens.accent) }
        active { background(dim(AwakeShadcnTheme.tokens.accent, 0.92f)) }
    }

    val slider: Style = Style {
        background(AwakeShadcnTheme.card)
        foreground(AwakeShadcnTheme.tokens.foreground)
        borderWidth(1f.dp)
        borderColor(AwakeShadcnTheme.input)
        shape(999f.dp)
    }
}

fun UiScope.awakeShadcnButton(
    id: String,
    width: Float,
    height: Float,
    label: String,
    modifier: UiModifier = UiModifier(),
    variant: AwakeShadcnButtonVariant = AwakeShadcnButtonVariant.Primary,
    style: Style = Style.Empty
): Boolean = button(
    id = id,
    width = width,
    height = height,
    label = label,
    modifier = modifier,
    style = AwakeShadcnStyles.button(variant) then style,
    variant = when (variant) {
        AwakeShadcnButtonVariant.Outline -> UiButtonVariant.Outline
        AwakeShadcnButtonVariant.Ghost -> UiButtonVariant.Ghost
        else -> UiButtonVariant.Filled
    },
    radius = UiShape.none
)

fun UiDslScope.awakeShadcnButton(
    id: String,
    width: Float,
    height: Float,
    label: String,
    modifier: UiModifier = UiModifier(),
    variant: AwakeShadcnButtonVariant = AwakeShadcnButtonVariant.Primary,
    style: Style = Style.Empty
): Boolean = button(
    id = id,
    label = label,
    width = width,
    height = height,
    modifier = modifier,
    style = AwakeShadcnStyles.button(variant) then style,
    variant = variant.toUiButtonVariant(),
    radius = UiShape.none
)

fun UiScope.awakeShadcnBadge(
    label: String,
    width: Dimension,
    height: Dimension,
    modifier: UiModifier = UiModifier(),
    variant: AwakeShadcnBadgeVariant = AwakeShadcnBadgeVariant.Secondary,
    style: Style = Style.Empty
) {
    val resolved = resolveStyle(style = style, defaults = AwakeShadcnStyles.badge(variant))
    val glyphPx = font?.cellSize?.times(resolved.textScale) ?: 0f
    val resolvedWidth = when (width) {
        Dimension.WrapContent -> Dimension.Fixed((label.length * glyphPx + resolved.contentPadding.horizontalPx()).px)
        else -> width
    }
    val resolvedHeight = when (height) {
        Dimension.WrapContent -> Dimension.Fixed((glyphPx + resolved.contentPadding.verticalPx()).px)
        else -> height
    }
    val slot = claimModifiedSlot(resolvedWidth, resolvedHeight, modifier)
    emitFillAndBorder(
        slot = slot,
        fillColor = resolved.background ?: TRANSPARENT,
        radiusPx = resolved.shape.toPx(),
        borderWidth = resolved.borderWidth,
        borderColor = resolved.borderColor ?: AwakeShadcnTheme.tokens.border
    )
    if (font != null) {
        text(label, slot = slot, font = font, color = resolved.foreground ?: AwakeShadcnTheme.tokens.foreground, centered = true)
    }
}

fun UiScope.awakeShadcnBadge(
    label: String,
    width: Float = 96f,
    height: Float = 28f,
    modifier: UiModifier = UiModifier(),
    variant: AwakeShadcnBadgeVariant = AwakeShadcnBadgeVariant.Secondary,
    style: Style = Style.Empty
) {
    awakeShadcnBadge(
        label = label,
        width = width.toDimension(),
        height = height.toDimension(),
        modifier = modifier,
        variant = variant,
        style = style
    )
}

fun UiDslScope.awakeShadcnBadge(
    label: String,
    modifier: UiModifier = UiModifier(),
    variant: AwakeShadcnBadgeVariant = AwakeShadcnBadgeVariant.Secondary,
    style: Style = Style.Empty
): UiSlot = text(
    label = label,
    modifier = modifier,
    style = AwakeShadcnStyles.badge(variant) then BadgeContentStyle then style,
    centered = true
)

fun UiScope.awakeShadcnSurface(
    id: String,
    width: Dimension,
    height: Dimension,
    style: Style = Style.Empty,
    content: ColumnScope.(slot: UiSlot) -> Unit
) {
    panel(
        id = id,
        width = width,
        height = height,
        style = AwakeShadcnTheme.components.panel then style,
        content = content
    )
}

fun UiScope.awakeShadcnToggle(
    id: String,
    checked: Boolean,
    width: Float,
    height: Float = 32f,
    label: String? = null,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Boolean = toggle(
    id = id,
    checked = checked,
    width = width,
    height = height,
    label = label,
    modifier = modifier,
    style = AwakeShadcnStyles.field then style
)

fun UiScope.awakeShadcnCheckbox(
    id: String,
    checked: Boolean,
    width: Float,
    height: Float = 24f,
    label: String? = null,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Boolean = checkbox(
    id = id,
    checked = checked,
    width = width,
    height = height,
    label = label,
    modifier = modifier,
    style = AwakeShadcnStyles.checkbox then style
)

fun UiScope.awakeShadcnDropdown(
    id: String,
    options: List<String>,
    selectedIndex: Int,
    width: Float,
    height: Float = 28f,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Int? = dropdown(
    id = id,
    options = options,
    selectedIndex = selectedIndex,
    width = width,
    height = height,
    modifier = modifier,
    style = AwakeShadcnStyles.field then style
)

fun UiScope.awakeShadcnSlider(
    id: String,
    min: Float,
    max: Float,
    value: Float,
    width: Float,
    height: Float = 28f,
    label: String? = null,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Float = slider(
    id = id,
    min = min,
    max = max,
    value = value,
    width = width,
    height = height,
    label = label,
    modifier = modifier,
    style = AwakeShadcnStyles.slider then style
)

fun UiDslScope.awakeShadcnToggle(
    id: String,
    checked: Boolean,
    width: Float,
    height: Float = 32f,
    label: String? = null,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Boolean = toggle(
    id = id,
    checked = checked,
    width = width,
    height = height,
    label = label,
    modifier = modifier,
    style = AwakeShadcnStyles.field then style
)

fun UiDslScope.awakeShadcnCheckbox(
    id: String,
    checked: Boolean,
    width: Float,
    height: Float = 24f,
    label: String? = null,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Boolean = checkbox(
    id = id,
    checked = checked,
    width = width,
    height = height,
    label = label,
    modifier = modifier,
    style = AwakeShadcnStyles.checkbox then style
)

fun UiDslScope.awakeShadcnDropdown(
    id: String,
    options: List<String>,
    selectedIndex: Int,
    width: Float,
    height: Float = 28f,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Int? = dropdown(
    id = id,
    options = options,
    selectedIndex = selectedIndex,
    width = width,
    height = height,
    modifier = modifier,
    style = AwakeShadcnStyles.field then style
)

fun UiDslScope.awakeShadcnSlider(
    id: String,
    min: Float,
    max: Float,
    value: Float,
    width: Float,
    height: Float = 28f,
    label: String? = null,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Float = slider(
    id = id,
    min = min,
    max = max,
    value = value,
    width = width,
    height = height,
    label = label,
    modifier = modifier,
    style = AwakeShadcnStyles.slider then style
)

fun UiColumnDslScope.awakeShadcnSurface(
    id: String,
    width: Dimension = Dimension.FillMax,
    height: Dimension,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty,
    content: UiColumnDslScope.(slot: UiSlot) -> Unit
): UiSlot = panel(
    id = id,
    width = width,
    height = height,
    modifier = modifier,
    style = AwakeShadcnTheme.components.panel then style,
    content = content
)

fun UiColumnDslScope.awakeShadcnSectionTitle(
    title: String,
    style: Style = Style {
        foreground(AwakeShadcnTheme.tokens.foreground)
    }
): UiSlot = sectionTitle(title = title, style = style)

fun UiColumnDslScope.awakeShadcnSupportingText(
    label: String,
    modifier: UiModifier = UiModifier(),
    style: Style = Style {
        foreground(AwakeShadcnTheme.tokens.mutedForeground)
    },
    maxLines: Int = Int.MAX_VALUE
): UiSlot = supportingText(
    label = label,
    modifier = modifier,
    style = style,
    maxLines = maxLines
)

fun UiColumnDslScope.awakeShadcnSectionHeader(
    title: String,
    description: String? = null
) {
    awakeShadcnSectionTitle(title)
    if (!description.isNullOrBlank()) {
        awakeShadcnSupportingText(description)
    }
}

fun UiColumnDslScope.awakeShadcnPropertyToggle(
    id: String,
    label: String,
    checked: Boolean,
    height: Float = 28f,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Boolean = propertyCheckbox(
    id = id,
    checked = checked,
    label = label,
    height = height,
    modifier = modifier,
    style = AwakeShadcnStyles.checkbox then style
)

fun UiColumnDslScope.awakeShadcnPropertyDropdown(
    id: String,
    label: String,
    options: List<String>,
    selectedIndex: Int,
    height: Float = 28f,
    labelWidth: Dp = 64f.dp,
    style: Style = Style.Empty
): Int? {
    var resolved: Int? = null
    propertyRow(label = label, height = height, labelWidth = labelWidth) { slot ->
        resolved = awakeShadcnDropdown(
            id = id,
            options = options,
            selectedIndex = selectedIndex,
            width = slot.width,
            height = slot.height,
            style = style
        )
    }
    return resolved
}

fun UiColumnDslScope.awakeShadcnPropertySlider(
    id: String,
    label: String,
    min: Float,
    max: Float,
    value: Float,
    height: Float = 28f,
    labelWidth: Dp = 64f.dp,
    style: Style = Style.Empty
): Float {
    var resolved = value
    propertyRow(label = label, height = height, labelWidth = labelWidth) { slot ->
        resolved = awakeShadcnSlider(
            id = id,
            min = min,
            max = max,
            value = value,
            width = slot.width,
            height = slot.height,
            style = style
        )
    }
    return resolved
}

fun UiAbsoluteDslScope.awakeShadcnSurface(
    id: String,
    width: Dimension,
    height: Dimension,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty,
    content: UiColumnDslScope.(slot: UiSlot) -> Unit
): UiSlot = panel(
    id = id,
    width = width,
    height = height,
    modifier = modifier,
    style = AwakeShadcnTheme.components.panel then style,
    content = content
)

private val TRANSPARENT = floatArrayOf(0f, 0f, 0f, 0f)
private val BadgeContentStyle = Style { contentPadding(10f.dp, 4f.dp) }

private fun AwakeShadcnButtonVariant.toUiButtonVariant(): UiButtonVariant = when (this) {
    AwakeShadcnButtonVariant.Outline -> UiButtonVariant.Outline
    AwakeShadcnButtonVariant.Ghost -> UiButtonVariant.Ghost
    else -> UiButtonVariant.Filled
}

private fun dim(color: FloatArray, brightness: Float): FloatArray = floatArrayOf(
    (color[0] * brightness).coerceIn(0f, 1f),
    (color[1] * brightness).coerceIn(0f, 1f),
    (color[2] * brightness).coerceIn(0f, 1f),
    color[3]
)
