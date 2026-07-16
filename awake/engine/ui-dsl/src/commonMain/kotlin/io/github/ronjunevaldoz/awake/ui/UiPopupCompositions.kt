// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color

data class UiDropdownMenuItem(
    val label: String,
    val destructive: Boolean = false
)

data class UiDropdownMenuResult(
    val slot: UiSlot?,
    val selectedIndex: Int?,
    val dismissed: Boolean
)

data class UiDialogProperties(
    val dismissOnClickOutside: Boolean = true,
    val showScrim: Boolean = true,
    val scrimColor: Color? = null,
    val popupProperties: UiPopupProperties = UiPopupProperties(),
    val surfaceStyle: Style = Style.Empty
)

private val DetachedPopupAnchor = UiSlot(-1f, -1f, 0f, 0f)

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
    items: List<UiDropdownMenuItem>,
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
            radius = UiShape.sm,
            clipContent = true,
            style = theme.components.panel then style
        ) {
            items.forEachIndexed { index, item ->
                val menuItemStyle = when {
                    index == selectedIndex -> Style {
                        background(theme.tokens.accent)
                        foreground(theme.tokens.accentForeground)
                    }
                    item.destructive -> Style {
                        foreground(theme.tokens.destructive)
                    }
                    else -> Style.Empty
                }
                if (
                    button(
                        id = "$id.item.$index",
                        label = item.label,
                        width = popupSlot.width,
                        height = itemHeight,
                        style = theme.components.dropdown then itemStyle then menuItemStyle,
                        variant = UiButtonVariant.Ghost
                    )
                ) {
                    picked = index
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
                properties.scrimColor ?: theme.tokens.background.withAlpha(0.74f)
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
                row(height = 36f, width = Dimension.FillMax) { actionSlot ->
                    actions(actionSlot)
                }
            }
        }
    }
    return popupResult
}
