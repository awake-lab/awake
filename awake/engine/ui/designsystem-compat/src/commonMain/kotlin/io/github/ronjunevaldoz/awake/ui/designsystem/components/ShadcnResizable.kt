// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.graphics.emitFillAndBorder
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.scope.pixelPerfectPixel
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.unstyled.ResizableDirection
import io.github.ronjunevaldoz.awake.ui.unstyled.ResizablePanelGroupScope
import io.github.ronjunevaldoz.awake.ui.unstyled.resizablePanelGroup

// Real shadcn's ResizableHandle: a `w-px` (1dp) border line, with an optional `h-4 w-3
// rounded-xs` (16x12dp) grip pill centered on it.
private val HANDLE_LINE_THICKNESS = 1f.dp
private val GRIP_WIDTH = 12f.dp
private val GRIP_HEIGHT = 16f.dp

/** Real shadcn's `ResizablePanelGroup`: styled only in that it reads the active theme's border
 * color/radii for its handles below -- the group itself has no chrome of its own, matching real
 * shadcn's `flex h-full w-full` (no border/background on the group wrapper). */
fun UiScope.shadcnResizablePanelGroup(
    id: String,
    direction: ResizableDirection = ResizableDirection.Horizontal,
    modifier: UiModifier = Modifier,
    content: ResizablePanelGroupScope.() -> Unit,
): UiBounds = resizablePanelGroup(id = id, direction = direction, modifier = modifier, content = content)

/** Real shadcn's `ResizablePanel` -- a thin passthrough, shadcn's own reference adds no styling
 * beyond what [resizablePanelGroup]'s headless `panel` already does. */
fun ResizablePanelGroupScope.shadcnResizablePanel(
    id: String,
    defaultSize: Float,
    minSize: Float = 0.1f,
    maxSize: Float = 1f,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds = panel(id = id, defaultSize = defaultSize, minSize = minSize, maxSize = maxSize, content = content)

/** Real shadcn's `ResizableHandle`: a thin border-colored divider between two panels, with an
 * optional centered grip pill ([withHandle]). Drag/layout mechanics are the headless `handle`'s
 * job -- this only paints, styled entirely from [theme]'s border color and radii. */
fun ResizablePanelGroupScope.shadcnResizableHandle(
    id: String,
    withHandle: Boolean = false,
): UiBounds {
    val slot = handle(id)
    val shadcnTheme = theme.asShadcnTheme()
    val lineThicknessPx = pixelPerfectPixel(HANDLE_LINE_THICKNESS.toPx()).coerceAtLeast(1f)
    val lineSlot = if (direction == ResizableDirection.Horizontal) {
        UiBounds(
            pixelPerfectPixel(slot.x + (slot.width - lineThicknessPx) / 2f),
            slot.y,
            lineThicknessPx,
            slot.height,
        )
    } else {
        UiBounds(
            slot.x,
            pixelPerfectPixel(slot.y + (slot.height - lineThicknessPx) / 2f),
            slot.width,
            lineThicknessPx,
        )
    }
    emit(UiDrawPrimitive.Quad(lineSlot.x, lineSlot.y, lineSlot.width, lineSlot.height, shadcnTheme.colors.border))
    if (withHandle) {
        val gripWidthPx = GRIP_WIDTH.toPx()
        val gripHeightPx = GRIP_HEIGHT.toPx()
        val gripSlot = UiBounds(
            slot.x + (slot.width - gripWidthPx) / 2f,
            slot.y + (slot.height - gripHeightPx) / 2f,
            gripWidthPx,
            gripHeightPx,
        )
        emitFillAndBorder(
            slot = gripSlot,
            fillColor = shadcnTheme.colors.border,
            radiusPx = shadcnTheme.radii.xs.toPx(),
            borderWidth = 1f.dp,
            borderColor = shadcnTheme.colors.border,
        )
    }
    return slot
}
