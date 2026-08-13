// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("MagicNumber", "LongParameterList")

package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.UiPopupResult
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets
import io.github.ronjunevaldoz.awake.ui.scope.recordSemantic

import io.github.ronjunevaldoz.awake.ui.api.EaseOut
import io.github.ronjunevaldoz.awake.ui.api.UiPopupPositionProvider
import io.github.ronjunevaldoz.awake.ui.api.UiPopupProperties
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.DialogProperties
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.RowScope
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceBorder
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.animateFloatTween
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.contextMenuTrigger
import io.github.ronjunevaldoz.awake.ui.headless.dialog
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxHeight
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.frameBounds
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.icon
import io.github.ronjunevaldoz.awake.ui.headless.overlayScrim
import io.github.ronjunevaldoz.awake.ui.headless.popup
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.surface
import io.github.ronjunevaldoz.awake.ui.toPx

/** Public, skin-level menu entries. They intentionally contain no Core style or layout types. */
sealed interface ShadcnMenuEntry
data class ShadcnMenuItem(val label: String, val enabled: Boolean = true) : ShadcnMenuEntry
data object ShadcnMenuSeparator : ShadcnMenuEntry

fun UiScope.shadcnContextMenu(
    id: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    items: List<ShadcnMenuEntry>,
    target: UiScope.() -> UiBounds,
): UiBounds {
    val bounds = target()
    val trigger = contextMenuTrigger(id, expanded, bounds)
    if (trigger.shouldOpen) onExpandedChange(true)
    if (expanded) {
        val entries = items.map { item ->
            when (item) {
                is ShadcnMenuItem -> ShadcnDropdownMenuItem(item.label, enabled = item.enabled)
                ShadcnMenuSeparator -> ShadcnDropdownMenuSeparator
            }
        }
        val result = shadcnDropdownMenu(
            id = "$id.menu",
            anchorSlot = trigger.anchor,
            expanded = true,
            items = entries,
        )
        if (result.dismissed || result.selectedIndex != null) onExpandedChange(false)
    }
    return bounds
}

enum class ShadcnDrawerPosition { Bottom, Top, Left, Right }
typealias ShadcnSheetSide = ShadcnDrawerPosition

fun UiScope.shadcnSheet(
    id: String,
    expanded: Boolean,
    onDismissRequest: () -> Unit = {},
    side: ShadcnSheetSide = ShadcnSheetSide.Right,
    size: Dp = 320f.dp,
    content: ColumnScope.(UiBounds) -> Unit,
): UiPopupResult {
    if (!expanded) return UiPopupResult(null, false)
    val progress = animateFloatTween("$id.slide", 1f, 0f, 250f, EaseOut)
    overlayScrim(frameBounds(), Color.Black.withAlpha(0.48f))
    val result = popup(
        id = id,
        anchorSlot = UiBounds(0f, 0f, 0f, 0f),
        expanded = true,
        positionProvider = sheetPositionProvider(side, size.toPx(), progress),
        properties = UiPopupProperties(dismissOnClickOutside = true, clippingEnabled = false),
    ) { slot ->
        primitive.recordSemantic(
            role = io.github.ronjunevaldoz.awake.ui.UiSemanticRole.Panel,
            id = id,
            bounds = slot,
        )
        surface(
            id = "$id.surface",
            modifier = if (side == ShadcnSheetSide.Left || side == ShadcnSheetSide.Right) Modifier.fillMaxHeight() else Modifier.height(size),
            style = SurfaceStyle(
                background = themeValues.colors.background,
                foreground = themeValues.colors.foreground,
                border = SurfaceBorder(1f.dp, themeValues.colors.border),
                contentPadding = UiInsets(16f.dp),
            ),
        ) { _ ->
            column {
                row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    icon(ShadcnIcons.xMark)
                }
                content(slot)
            }
        }
    }
    if (result.dismissed) onDismissRequest()
    return result
}

private fun sheetPositionProvider(side: ShadcnSheetSide, size: Float, progress: Float) = UiPopupPositionProvider { _, frame, _ ->
    val offset = (1f - progress) * size
    when (side) {
        ShadcnSheetSide.Left -> UiBounds(-offset, 0f, size, frame.height)
        ShadcnSheetSide.Right -> UiBounds(frame.width - size + offset, 0f, size, frame.height)
        ShadcnSheetSide.Top -> UiBounds(0f, -offset, frame.width, size)
        ShadcnSheetSide.Bottom -> UiBounds(0f, frame.height - size + offset, frame.width, size)
    }
}


fun UiScope.shadcnDrawer(
    id: String,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    position: ShadcnDrawerPosition = ShadcnDrawerPosition.Bottom,
    size: Dp = 320f.dp,
    content: ColumnScope.(UiBounds) -> Unit,
): UiPopupResult = dialog(
    id = id,
    expanded = expanded,
    width = if (position == ShadcnDrawerPosition.Left || position == ShadcnDrawerPosition.Right) Dimension.Fixed(size) else Dimension.FillMax,
    height = if (position == ShadcnDrawerPosition.Top || position == ShadcnDrawerPosition.Bottom) Dimension.Fixed(size) else Dimension.FillMax,
    properties = DialogProperties(
        dismissOnClickOutside = true,
        showScrim = true,
        surface = SurfaceStyle(
            background = themeValues.colors.background,
            foreground = themeValues.colors.foreground,
            border = SurfaceBorder(1f.dp, themeValues.colors.border),
            contentPadding = UiInsets(16f.dp),
        ),
    ),
) { slot ->
    column {
        row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            icon(ShadcnIcons.xMark)
        }
        content(slot)
    }
}.also { if (it.dismissed) onDismissRequest() }

fun UiScope.shadcnDialog(
    id: String,
    expanded: Boolean,
    width: Dimension = Dimension.WrapContent,
    height: Dimension = Dimension.WrapContent,
    header: (ColumnScope.() -> Unit)? = null,
    actions: (RowScope.() -> Unit)? = null,
    content: ColumnScope.(UiBounds) -> Unit,
): UiPopupResult = dialog(
    id = id,
    expanded = expanded,
    width = width,
    height = height,
    properties = DialogProperties(
        dismissOnClickOutside = true,
        showScrim = true,
        surface = SurfaceStyle(
            background = themeValues.colors.card,
            foreground = themeValues.colors.cardForeground,
            border = SurfaceBorder(1f.dp, themeValues.colors.border),
            cornerRadius = themeValues.shapes.lg,
            contentPadding = UiInsets(24f.dp),
        ),
    ),
) { slot ->
    column(verticalArrangement = Arrangement.spacedBy(16f.dp)) {
        header?.invoke(this)
        content(slot)
        if (actions != null) {
            row(horizontalArrangement = Arrangement.End) {
                actions()
            }
        }
    }
}
