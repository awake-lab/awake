// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.font.measureTextWidth

sealed interface UiDropdownMenuEntry

data class UiDropdownMenuItem(
    val label: String,
    val destructive: Boolean = false,
    val enabled: Boolean = true,
    val supportingText: String? = null,
    val trailingLabel: String? = null
) : UiDropdownMenuEntry

data object UiDropdownMenuSeparator : UiDropdownMenuEntry

data class UiDropdownMenuResult(
    val slot: UiSlot?,
    val selectedIndex: Int?,
    val dismissed: Boolean
)

enum class UiAlertDialogAction {
    Confirm,
    Dismiss
}

data class UiAlertDialogResult(
    val popup: UiPopupResult,
    val action: UiAlertDialogAction?
)

data class UiDialogProperties(
    val dismissOnClickOutside: Boolean = true,
    val showScrim: Boolean = true,
    val scrimColor: Color? = null,
    val popupProperties: UiPopupProperties = UiPopupProperties(),
    val surfaceStyle: Style = Style.Empty
)

private val DetachedPopupAnchor = UiSlot(-1f, -1f, 0f, 0f)
private val DefaultDialogScrimColor = Color.Black.withAlpha(0.48f)

fun UiDslScope.tooltip(
    anchorSlot: UiSlot,
    visible: Boolean,
    width: Dimension = Dimension.WrapContent,
    height: Dimension = Dimension.WrapContent,
    positionProvider: UiPopupPositionProvider = UiPopupDefaults.aligned(
        anchorAlignment = UiAlignment.BottomCenter,
        popupAlignment = UiAlignment.TopCenter,
        offsetY = UiSpacing.xs
    ),
    properties: UiPopupProperties = UiPopupProperties(),
    style: Style = Style.Empty,
    content: UiColumnDslScope.(slot: UiSlot) -> Unit
): UiPopupResult = popup(
    anchorSlot = anchorSlot,
    expanded = visible,
    width = width,
    height = height,
    gap = 0f,
    positionProvider = positionProvider,
    properties = properties
) { popupSlot ->
    panel(
        id = "tooltip",
        width = width,
        height = height,
        radius = UiShape.sm,
        style = theme.components.panel then style
    ) { slot ->
        content(slot)
    }
}

fun UiDslScope.tooltipText(
    anchorSlot: UiSlot,
    visible: Boolean,
    text: String,
    positionProvider: UiPopupPositionProvider = UiPopupDefaults.aligned(
        anchorAlignment = UiAlignment.BottomCenter,
        popupAlignment = UiAlignment.TopCenter,
        offsetY = UiSpacing.xs
    ),
    properties: UiPopupProperties = UiPopupProperties(),
    style: Style = Style.Empty
): UiPopupResult = tooltip(
    anchorSlot = anchorSlot,
    visible = visible,
    positionProvider = positionProvider,
    properties = properties,
    style = style
) {
    text(
        label = text,
        wrap = UiTextWrap.Word,
        overflow = UiTextOverflow.Ellipsis
    )
}

fun UiDslScope.dropdownMenu(
    id: String,
    anchorSlot: UiSlot,
    expanded: Boolean,
    items: List<UiDropdownMenuEntry>,
    selectedIndex: Int? = null,
    width: Dimension = Dimension.Fixed(anchorSlot.width.px),
    height: Dimension = Dimension.WrapContent,
    itemHeight: Float = 32f,
    positionProvider: UiPopupPositionProvider = UiPopupDefaults.dropdown(),
    properties: UiPopupProperties = UiPopupProperties(),
    style: Style = Style.Empty,
    itemStyle: Style = Style.Empty
): UiDropdownMenuResult {
    var picked: Int? = null
    val popupResult = popup(
        anchorSlot = anchorSlot,
        expanded = expanded,
        width = width,
        height = height,
        gap = 0f,
        positionProvider = positionProvider,
        properties = properties
    ) { popupSlot ->
        panel(
            id = "$id.menu",
            width = Dimension.Fixed(popupSlot.width.px),
            height = height,
            gap = 0f,
            radius = UiShape.sm,
            clipContent = true,
            style = theme.components.panel then style
        ) {
            var actionIndex = 0
            items.forEach { entry ->
                when (entry) {
                    UiDropdownMenuSeparator -> {
                        spacer(UiModifier().height(4f.dp))
                        separator(
                            width = Dimension.FillMax,
                            thickness = 1f.dp,
                            color = theme.tokens.border.withAlpha(0.72f)
                        )
                        spacer(UiModifier().height(4f.dp))
                    }
                    is UiDropdownMenuItem -> {
                        val menuItemStyle = when {
                            !entry.enabled -> Style {
                                foreground(theme.tokens.mutedForeground)
                                background(theme.tokens.background.withAlpha(0.86f))
                            }
                            actionIndex == selectedIndex -> Style {
                                background(theme.tokens.accent)
                                foreground(theme.tokens.accentForeground)
                            }
                            entry.destructive -> Style {
                                foreground(theme.tokens.destructive)
                            }
                            else -> Style.Empty
                        }
                        val clicked = dropdownMenuItem(
                            id = "$id.item.$actionIndex",
                            item = entry,
                            width = popupSlot.width,
                            baseHeight = itemHeight,
                            style = itemStyle then menuItemStyle,
                            selected = actionIndex == selectedIndex
                        )
                        if (clicked && entry.enabled) {
                            picked = actionIndex
                        }
                        actionIndex += 1
                    }
                }
            }
        }
    }
    return UiDropdownMenuResult(
        slot = popupResult.slot,
        selectedIndex = picked,
        dismissed = popupResult.dismissed
    )
}

fun UiDslScope.dialog(
    id: String,
    expanded: Boolean,
    width: Dimension = Dimension.WrapContent,
    height: Dimension = Dimension.WrapContent,
    properties: UiDialogProperties = UiDialogProperties(),
    header: (UiColumnDslScope.(slot: UiSlot) -> Unit)? = null,
    actions: (UiRowDslScope.(slot: UiSlot) -> Unit)? = null,
    content: UiColumnDslScope.(slot: UiSlot) -> Unit
): UiPopupResult {
    if (!expanded) return UiPopupResult(slot = null, dismissed = false)

    val frameBounds = context.frameBounds()
    if (properties.showScrim) {
        context.absolute(
            x = frameBounds.x,
            y = frameBounds.y,
            font = font,
            theme = theme,
            textScale = textScale,
            overlayOnly = true
        ).emit(
            UiDrawPrimitive.Quad(
                frameBounds.x,
                frameBounds.y,
                frameBounds.width,
                frameBounds.height,
                properties.scrimColor ?: DefaultDialogScrimColor
            )
        )
    }

    val popupResult = popup(
        anchorSlot = DetachedPopupAnchor,
        expanded = true,
        width = width,
        height = height,
        gap = 0f,
        positionProvider = UiPopupDefaults.centered(),
        properties = properties.popupProperties.copy(
            dismissOnClickOutside = properties.dismissOnClickOutside && properties.popupProperties.dismissOnClickOutside
        )
    ) { popupSlot ->
        panel(
            id = id,
            width = width,
            height = height,
            radius = UiShape.md,
            style = theme.components.panel then properties.surfaceStyle
        ) { slot ->
            header?.invoke(this, slot)
            content(slot)
            if (actions != null) {
                row(height = 36f.dp, width = Dimension.FillMax) { actionSlot ->
                    actions(actionSlot)
                }
            }
        }
    }
    return popupResult
}

fun UiDslScope.alertDialog(
    id: String,
    expanded: Boolean,
    title: String,
    message: String,
    width: Dimension = Dimension.Fixed(320f.px),
    confirmLabel: String = "Confirm",
    dismissLabel: String? = "Cancel",
    confirmVariant: UiButtonVariant = UiButtonVariant.Filled,
    dismissVariant: UiButtonVariant = UiButtonVariant.Ghost,
    properties: UiDialogProperties = UiDialogProperties(),
    style: Style = Style.Empty
): UiAlertDialogResult {
    var action: UiAlertDialogAction? = null
    val popup = dialog(
        id = id,
        expanded = expanded,
        width = width,
        properties = properties.copy(surfaceStyle = properties.surfaceStyle then style),
        header = {
            text(title, style = Style { textSize(theme.typography.title) })
        },
        actions = {
            dismissLabel?.let { label ->
                if (
                    button(
                        id = "$id.dismiss",
                        label = label,
                        modifier = UiModifier().width(96f.px).height(32f.px),
                        variant = dismissVariant
                    )
                ) {
                    action = UiAlertDialogAction.Dismiss
                }
            }
            spacer(UiModifier().width(8f.dp))
            if (
                button(
                    id = "$id.confirm",
                    label = confirmLabel,
                    modifier = UiModifier().width(96f.px). height(32f.px),
                    variant = confirmVariant
                )
            ) {
                action = UiAlertDialogAction.Confirm
            }
        }
    ) {
        supportingText(message)
    }
    return UiAlertDialogResult(popup = popup, action = action)
}

private fun UiColumnDslScope.dropdownMenuItem(
    id: String,
    item: UiDropdownMenuItem,
    width: Float,
    baseHeight: Float,
    style: Style,
    selected: Boolean
): Boolean {
    val resolvedFont = font
    val labelSize = theme.typography.label
    val glyphPx = pixelPerfectPixel(labelSize.toPx().coerceAtLeast(1f)).coerceAtLeast(1f)
    val trailingWidth = item.trailingLabel?.let { label ->
        (resolvedFont?.measureTextWidth(label, glyphPx) ?: label.length * glyphPx) + 8f
    } ?: 0f
    val bodyWidth = (width - 24f - trailingWidth).coerceAtLeast(glyphPx)
    val supportingLayout = item.supportingText?.takeIf { it.isNotBlank() }?.let {
        layoutBitmapText(
            label = it,
            glyphPx = glyphPx,
            maxWidthPx = (width - 24f).coerceAtLeast(glyphPx),
            wrap = UiTextWrap.Word,
            overflow = UiTextOverflow.Ellipsis,
            maxLines = 2,
            advanceOf = { char -> resolvedFont?.advanceFor(char, glyphPx) ?: glyphPx }
        )
    }
    val lineGap = glyphPx * 0.25f
    val supportingHeight = supportingLayout?.blockHeight(glyphPx, lineGap) ?: 0f
    val computedHeight = if (supportingLayout == null) {
        baseHeight
    } else {
        maxOf(baseHeight, 8f + glyphPx + 4f + supportingHeight + 8f)
    }
    val height = computedHeight
    val slot = buttonSlot(
        id = id,
        label = "",
        modifier = UiModifier().width(width.px).height(height.px),
        style = style,
        variant = if (selected) UiButtonVariant.Filled else UiButtonVariant.Ghost
    )
    val contentScope = context.absolute(
        slot = slot.slot,
        font = resolvedFont,
        theme = theme,
        textScale = textScale,
        insets = UiInsets(12f.dp, 8f.dp),
        overlayOnly = true
    )
    val trailingColor = when {
        !item.enabled -> theme.tokens.mutedForeground
        selected -> theme.tokens.accentForeground.withAlpha(0.82f)
        else -> theme.tokens.mutedForeground
    }
    val textColor = when {
        !item.enabled -> theme.tokens.mutedForeground
        selected -> theme.tokens.accentForeground
        item.destructive -> theme.tokens.destructive
        else -> theme.tokens.foreground
    }
        contentScope.text(
            label = item.label,
            slot = UiSlot(
                x = slot.slot.x + 12f,
                y = slot.slot.y + 8f,
                width = bodyWidth,
                height = glyphPx
            ),
            font = resolvedFont,
            color = textColor,
            overflow = UiTextOverflow.Ellipsis,
            textSize = labelSize
        )
    item.trailingLabel?.let { label ->
        contentScope.text(
            label = label,
            slot = UiSlot(
                x = slot.slot.x + slot.slot.width - trailingWidth - 12f,
                y = slot.slot.y + 8f,
                width = trailingWidth,
                height = glyphPx
            ),
            font = resolvedFont,
            color = trailingColor,
            centered = true,
            overflow = UiTextOverflow.Ellipsis,
            textSize = labelSize
        )
    }
    supportingLayout?.let { layout ->
        contentScope.text(
            label = item.supportingText.orEmpty(),
            slot = UiSlot(
                x = slot.slot.x + 12f,
                y = slot.slot.y + 8f + glyphPx + 4f,
                width = (slot.slot.width - 24f).coerceAtLeast(0f),
                height = layout.blockHeight(glyphPx, lineGap)
            ),
            font = resolvedFont,
            color = if (selected) theme.tokens.accentForeground.withAlpha(0.82f) else theme.tokens.mutedForeground,
            wrap = UiTextWrap.Word,
            overflow = UiTextOverflow.Ellipsis,
            maxLines = 2,
            textSize = labelSize
        )
    }
    return slot.clicked && item.enabled
}
