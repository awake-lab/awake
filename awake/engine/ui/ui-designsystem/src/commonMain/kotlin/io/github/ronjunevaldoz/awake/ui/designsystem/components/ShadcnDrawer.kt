// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.UiPopupPositionProvider
import io.github.ronjunevaldoz.awake.ui.UiPopupProperties
import io.github.ronjunevaldoz.awake.ui.UiPopupResult
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layout.toBounds
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.layouts.spacer
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.popup
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.toPx

enum class ShadcnDrawerPosition {
    Bottom, Top, Left, Right
}

/**
 * Real shadcn's `Drawer` (built on vaul/sheet primitives): a slide-over modal overlay panel
 * anchored to the bottom, top, left, or right viewport edge.
 */
fun UiScope.shadcnDrawer(
    id: String,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: UiModifier = Modifier,
    position: ShadcnDrawerPosition = ShadcnDrawerPosition.Bottom,
    sizeDp: Dp = 320f.dp,
    showCloseButton: Boolean = true,
    header: (ColumnScope.(slot: UiBounds) -> Unit)? = null,
    actions: (RowScope.(slot: UiBounds) -> Unit)? = null,
    content: ColumnScope.(slot: UiBounds) -> Unit
): UiPopupResult {
    if (!expanded) return UiPopupResult(slot = null, dismissed = false)

    val positionProvider = UiPopupPositionProvider { _, windowBounds, _ ->
        val sizePx = sizeDp.toPx()
        val viewportWidth = windowBounds.width
        val viewportHeight = windowBounds.height
        when (position) {
            ShadcnDrawerPosition.Bottom -> UiBounds(0f, viewportHeight - sizePx, viewportWidth, sizePx)
            ShadcnDrawerPosition.Top -> UiBounds(0f, 0f, viewportWidth, sizePx)
            ShadcnDrawerPosition.Left -> UiBounds(0f, 0f, sizePx, viewportHeight)
            ShadcnDrawerPosition.Right -> UiBounds(viewportWidth - sizePx, 0f, sizePx, viewportHeight)
        }
    }

    val popupResult = popup(
        id = id,
        expanded = true,
        anchorSlot = UiBounds(0f, 0f, 0f, 0f),
        positionProvider = positionProvider,
        properties = UiPopupProperties(dismissOnClickOutside = true)
    ) { slot ->
        val resolvedTheme = theme.asShadcnTheme()
        val bounds = slot.toBounds()
        surface(
            id = id,
            modifier = modifier.width(Dimension.Fixed(bounds.width.dp)).height(Dimension.Fixed(bounds.height.dp)),
            style = resolvedTheme.components.surface then Style {
                shape(
                    when (position) {
                        ShadcnDrawerPosition.Bottom, ShadcnDrawerPosition.Top -> resolvedTheme.radii.xl
                        else -> UiShape.none
                    }
                )
                contentPadding(16f.dp)
            }
        ) {
            column(
                modifier = Modifier.fillMaxWidth().height(Dimension.Fixed(bounds.height.dp))
            ) {
            // Drag handle pill for bottom drawer
            if (position == ShadcnDrawerPosition.Bottom) {
                row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth().height(12f.dp)
                ) {
                    surface(
                        id = "$id.handle",
                        modifier = Modifier.width(48f.dp).height(6f.dp),
                        style = Style {
                            background(resolvedTheme.palette.muted)
                            shape(resolvedTheme.radii.full)
                        }
                    ) {}
                }
                spacer(Modifier.height(8f.dp))
            }

            // Header slot with optional close button inside top-right of panel
            if (header != null || showCloseButton) {
                row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = UiAlignment.Vertical.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (header != null) {
                        column { header(bounds) }
                    } else {
                        spacer(Modifier.width(1f.dp))
                    }
                    if (showCloseButton) {
                        if (shadcnButton(
                            id = "$id.close",
                            label = "✕",
                            variant = ShadcnButtonVariant.Ghost,
                            modifier = Modifier.width(28f.dp).height(28f.dp),
                            onClick = onDismissRequest
                        )) { }
                    }
                }
                spacer(Modifier.height(12f.dp))
            }

            content(bounds)

            if (actions != null) {
                spacer(Modifier.height(12f.dp))
                row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = UiAlignment.Vertical.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    actions(bounds)
                }
            }
        }
    }
    }

    if (popupResult.dismissed) {
        onDismissRequest()
    }

    return UiPopupResult(slot = popupResult.slot?.toBounds(), dismissed = popupResult.dismissed)
}
