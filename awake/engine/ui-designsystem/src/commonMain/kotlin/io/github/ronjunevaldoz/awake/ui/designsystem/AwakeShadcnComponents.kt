// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.ColumnScope
import io.github.ronjunevaldoz.awake.ui.UiButtonVariant
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.button
import io.github.ronjunevaldoz.awake.ui.emitFillAndBorder
import io.github.ronjunevaldoz.awake.ui.panel
import io.github.ronjunevaldoz.awake.ui.resolveStyle
import io.github.ronjunevaldoz.awake.ui.text
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.toDimension
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
            hovered { background(brighten(AwakeShadcnTheme.tokens.primary, 1.05f)) }
        }
        AwakeShadcnButtonVariant.Secondary -> AwakeShadcnTheme.components.button
        AwakeShadcnButtonVariant.Outline -> Style {
            background(TRANSPARENT)
            foreground(AwakeShadcnTheme.tokens.foreground)
            borderWidth(1f.dp)
            borderColor(AwakeShadcnTheme.tokens.border)
            shape(6f.dp)
            hovered { background(AwakeShadcnTheme.tokens.muted) }
        }
        AwakeShadcnButtonVariant.Ghost -> Style {
            background(TRANSPARENT)
            foreground(AwakeShadcnTheme.tokens.foreground)
            shape(6f.dp)
            hovered { background(AwakeShadcnTheme.tokens.muted) }
        }
        AwakeShadcnButtonVariant.Danger -> Style {
            background(AwakeShadcnTheme.tokens.destructive)
            foreground(AwakeShadcnTheme.tokens.destructiveForeground)
            borderWidth(1f.dp)
            borderColor(AwakeShadcnTheme.tokens.destructive)
            shape(6f.dp)
            hovered { background(brighten(AwakeShadcnTheme.tokens.destructive, 1.05f)) }
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

fun UiScope.awakeShadcnBadge(
    label: String,
    width: Float = 96f,
    height: Float = 28f,
    modifier: UiModifier = UiModifier(),
    variant: AwakeShadcnBadgeVariant = AwakeShadcnBadgeVariant.Secondary,
    style: Style = Style.Empty
) {
    val slot = claimSlot(modifier.width ?: width.toDimension(), modifier.height ?: height.toDimension())
    val resolved = resolveStyle(style = style, defaults = AwakeShadcnStyles.badge(variant))
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

private val TRANSPARENT = floatArrayOf(0f, 0f, 0f, 0f)

private fun brighten(color: FloatArray, brightness: Float): FloatArray = floatArrayOf(
    (color[0] * brightness).coerceAtMost(1f),
    (color[1] * brightness).coerceAtMost(1f),
    (color[2] * brightness).coerceAtMost(1f),
    color[3]
)
