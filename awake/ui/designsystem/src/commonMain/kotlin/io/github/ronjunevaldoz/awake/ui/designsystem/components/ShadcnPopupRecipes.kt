// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.UiPopupPositionProvider
import io.github.ronjunevaldoz.awake.ui.api.UiPopupProperties
import io.github.ronjunevaldoz.awake.ui.api.UiPopupResult
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets
import io.github.ronjunevaldoz.awake.ui.api.sp
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.DialogProperties
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceBorder
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceVisuals
import io.github.ronjunevaldoz.awake.ui.headless.UiAlertDialogAction
import io.github.ronjunevaldoz.awake.ui.headless.UiAlertDialogResult
import io.github.ronjunevaldoz.awake.ui.headless.UiMenuItem
import io.github.ronjunevaldoz.awake.ui.headless.UiMenuResult
import io.github.ronjunevaldoz.awake.ui.headless.UiMenuSeparator
import io.github.ronjunevaldoz.awake.ui.headless.UiPopupDefaults
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.headless.UiTextWrap
import io.github.ronjunevaldoz.awake.ui.font.measureTextWidth
import io.github.ronjunevaldoz.awake.ui.headless.button
import io.github.ronjunevaldoz.awake.ui.headless.dialog
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.menu
import io.github.ronjunevaldoz.awake.ui.headless.menuItem
import io.github.ronjunevaldoz.awake.ui.headless.popup
import io.github.ronjunevaldoz.awake.ui.headless.separator
import io.github.ronjunevaldoz.awake.ui.headless.surface
import io.github.ronjunevaldoz.awake.ui.headless.text
import io.github.ronjunevaldoz.awake.ui.headless.width
import io.github.ronjunevaldoz.awake.ui.scope.resolveGlyphPx
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle
import io.github.ronjunevaldoz.awake.ui.toPx

/** Public Shadcn menu entries. Popup mechanics remain Headless-owned. */
sealed interface ShadcnDropdownMenuEntry

data class ShadcnDropdownMenuItem(
    val label: String,
    val destructive: Boolean = false,
    val enabled: Boolean = true,
) : ShadcnDropdownMenuEntry

data object ShadcnDropdownMenuSeparator : ShadcnDropdownMenuEntry

fun UiScope.shadcnDropdownMenu(
    id: String,
    anchorSlot: UiBounds,
    expanded: Boolean,
    items: List<ShadcnDropdownMenuEntry>,
    selectedIndex: Int? = null,
    width: Dimension = Dimension.WrapContent,
    height: Dimension = Dimension.WrapContent,
    positionProvider: UiPopupPositionProvider = UiPopupDefaults.dropdown(),
    properties: UiPopupProperties = UiPopupProperties(),
): UiMenuResult {
    var actionIndex = 0
    val itemsByActionIndex = mutableMapOf<Int, ShadcnDropdownMenuItem>()
    val entries = items.map { entry ->
        when (entry) {
            ShadcnDropdownMenuSeparator -> UiMenuSeparator
            is ShadcnDropdownMenuItem -> UiMenuItem(
                id = "$id.item.$actionIndex",
                index = actionIndex,
                enabled = entry.enabled,
            ).also { itemsByActionIndex[actionIndex++] = entry }
        }
    }
    var selectedItemIndex: Int? = null
    val resolvedWidth = if (width == Dimension.WrapContent) {
        val font = primitive.context.currentFont
        val glyphPx = primitive.resolveGlyphPx(textStyle = TextStyle(size = themeValues.typography.label))
        val maxLabelWidthPx = items.filterIsInstance<ShadcnDropdownMenuItem>().maxOfOrNull { item ->
            font.measureTextWidth(item.label, glyphPx)
        } ?: 80f
        val calcWidthPx = kotlin.math.ceil(maxLabelWidthPx + 40f.dp.toPx()).coerceAtLeast(128f.dp.toPx())
        Dimension.Fixed(calcWidthPx.dp)
    } else {
        width
    }
    val result = popup(
        id = id,
        anchorSlot = anchorSlot,
        expanded = expanded,
        width = resolvedWidth,
        height = height,
        positionProvider = positionProvider,
        properties = properties,
    ) {
        surface(
            id = "$id.surface",
            modifier = Modifier.fillMaxWidth(),
            style = SurfaceStyle(
                background = themeValues.colors.popover,
                foreground = themeValues.colors.popoverForeground,
                border = SurfaceBorder(1f.dp, themeValues.colors.border),
                cornerRadius = themeValues.shapes.md,
                contentPadding = UiInsets(4f.dp),
            ),
        ) {
            entries.forEach { entry ->
                when (entry) {
                    UiMenuSeparator -> separator(color = themeValues.colors.border)
                    is UiMenuItem -> {
                        val source = itemsByActionIndex[entry.index]
                        if (source != null && entry.enabled) {
                            val activated = menuItem(
                                item = entry,
                                label = source.label,
                                modifier = Modifier.fillMaxWidth().height(32f.dp),
                                visuals = SurfaceVisuals(
                                    rest = SurfaceStyle(
                                        background = if (entry.index == selectedIndex) themeValues.colors.accent else themeValues.colors.popover,
                                        foreground = when {
                                            entry.index == selectedIndex -> themeValues.colors.accentForeground
                                            source.destructive -> themeValues.colors.destructive
                                            else -> themeValues.colors.popoverForeground
                                        },
                                        cornerRadius = themeValues.shapes.sm,
                                        contentPadding = UiInsets(horizontal = 8f.dp, vertical = 6f.dp),
                                        textSize = 14f.sp,
                                    ),
                                    hovered = SurfaceStyle(
                                        background = if (source.destructive) themeValues.colors.destructive.withAlpha(0.1f) else themeValues.colors.accent,
                                        foreground = if (source.destructive) themeValues.colors.destructive else themeValues.colors.accentForeground,
                                        cornerRadius = themeValues.shapes.sm,
                                        contentPadding = UiInsets(horizontal = 8f.dp, vertical = 6f.dp),
                                        textSize = 14f.sp,
                                    ),
                                ),
                            )
                            if (activated) selectedItemIndex = entry.index
                        }
                    }
                }
            }
        }
    }
    return UiMenuResult(result.slot, selectedItemIndex, result.dismissed)
}

fun UiScope.shadcnTooltip(
    anchorSlot: UiBounds,
    visible: Boolean,
    width: Dimension = Dimension.WrapContent,
    height: Dimension = Dimension.WrapContent,
    positionProvider: UiPopupPositionProvider = UiPopupDefaults.aligned(
        anchorAlignment = UiAlignment.BottomCenter,
        popupAlignment = UiAlignment.TopCenter,
        offsetY = 4f.dp,
    ),
    properties: UiPopupProperties = UiPopupProperties(),
    id: String = "tooltip",
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiPopupResult = popup(
    id = id,
    anchorSlot = anchorSlot,
    expanded = visible,
    width = width,
    height = height,
    positionProvider = positionProvider,
    properties = properties,
) {
    surface(
        id = "$id.content",
        style = SurfaceStyle(
            background = themeValues.colors.foreground,
            foreground = themeValues.colors.background,
            cornerRadius = themeValues.shapes.md,
            contentPadding = io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets(12f.dp, 6f.dp),
            // tooltip.tsx uses Tailwind `text-xs` (12px), not the preset's caption tier (11px
            // in Vega). Keep this component tied to the source token so its intrinsic width is
            // stable across theme presets.
            textSize = 12f.sp,
            // shadcn's text-xs token uses a 1rem line-height (Tailwind's default leading),
            // while the bundled font's intrinsic line box is slightly shorter.
            lineHeight = 16f.sp,
        ),
        content = content,
    )
}

fun UiScope.shadcnTooltipText(
    anchorSlot: UiBounds,
    visible: Boolean,
    text: String,
    positionProvider: UiPopupPositionProvider = UiPopupDefaults.aligned(
        anchorAlignment = UiAlignment.BottomCenter,
        popupAlignment = UiAlignment.TopCenter,
        offsetY = 4f.dp,
    ),
    properties: UiPopupProperties = UiPopupProperties(),
    id: String = "tooltip",
): UiPopupResult = shadcnTooltip(
    anchorSlot = anchorSlot,
    visible = visible,
    positionProvider = positionProvider,
    properties = properties,
    id = id,
) {
    text(label = text, wrap = UiTextWrap.Word, overflow = UiTextOverflow.Ellipsis)
}

fun UiScope.shadcnAlertDialog(
    id: String,
    expanded: Boolean,
    title: String,
    width: Dimension = Dimension.Fixed(320f.dp),
    properties: DialogProperties = DialogProperties(),
    actions: ColumnScope.() -> Unit,
    body: ColumnScope.() -> Unit,
): UiAlertDialogResult {
    var action: UiAlertDialogAction? = null
    val popup = dialog(
        id = id,
        expanded = expanded,
        width = width,
        properties = properties.copy(
            surface = properties.surface.copy(
                background = themeValues.colors.card,
                foreground = themeValues.colors.cardForeground,
                border = SurfaceBorder(1f.dp, themeValues.colors.border),
                cornerRadius = themeValues.shapes.lg,
                contentPadding = io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets(24f.dp),
            ),
        ),
    ) {
        text(label = title, visuals = SurfaceStyle(textSize = themeValues.typography.title), wrap = UiTextWrap.Word)
        body()
        actions()
    }
    return UiAlertDialogResult(popup = popup, action = action)
}

fun UiScope.shadcnAlertDialog(
    id: String,
    expanded: Boolean,
    title: String,
    message: String,
    width: Dimension = Dimension.Fixed(320f.dp),
    confirmLabel: String = "Confirm",
    dismissLabel: String? = "Cancel",
    properties: DialogProperties = DialogProperties(),
): UiAlertDialogResult = shadcnAlertDialog(
    id = id,
    expanded = expanded,
    title = title,
    width = width,
    properties = properties,
    actions = {
        dismissLabel?.let { label ->
            button(id = "$id.dismiss", label = label)
        }
        button(id = "$id.confirm", label = confirmLabel)
    },
    body = { text(label = message, visuals = SurfaceStyle(textSize = themeValues.typography.body), wrap = UiTextWrap.Word) },
)
