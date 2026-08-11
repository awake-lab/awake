// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.UiPopupPositionProvider
import io.github.ronjunevaldoz.awake.ui.api.UiPopupProperties
import io.github.ronjunevaldoz.awake.ui.api.UiPopupResult
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
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
import io.github.ronjunevaldoz.awake.ui.unstyled.components.icon

enum class ShadcnDrawerPosition {
    Bottom,
    Top,
    Left,
    Right,
}

/**
 * Real shadcn's `Drawer` (built on vaul/sheet primitives): a slide-over modal overlay panel
 * anchored to the bottom, top, left, or right viewport edge.
 *
 * [sizeDp] note: one default across all four positions, which real shadcn does NOT have --
 * left/right drawers are `w-3/4 sm:max-w-sm` (the same 384dp cap [shadcnSheet] uses), while
 * top/bottom are `max-h-[80vh]`, viewport-relative with no fixed-dp equivalent this API can
 * express. Left at 320dp because Bottom is the default position here, so pinning it to the
 * left/right cap would change the common case to match a spec that doesn't govern it. Pass
 * `sizeDp = 384f.dp` explicitly for a left/right drawer to match shadcn exactly. */
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
    content: ColumnScope.(slot: UiBounds) -> Unit,
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
        properties = UiPopupProperties(dismissOnClickOutside = true),
    ) { slot ->
        val resolvedTheme = theme.asShadcnTheme()
        val bounds = slot
        surface(
            id = id,
            modifier = modifier.width(Dimension.Fixed(bounds.width.dp)).height(Dimension.Fixed(bounds.height.dp)),
            style = resolvedTheme.components.surface then Style {
                shape(
                    when (position) {
                        ShadcnDrawerPosition.Bottom, ShadcnDrawerPosition.Top -> resolvedTheme.radii.xl
                        else -> UiShape.none
                    },
                )
                contentPadding(16f.dp)
            },
        ) {
            // surface() already reserves contentPadding(16dp) on all sides for this content
            // lambda's coordinate space -- forcing this column to bounds.height (the panel's
            // full, unpadded outer height) double-counts that padding and overflows the
            // padded/rounded content area by 2x the inset. WrapContent lets the header/content/
            // actions stack size to what it actually needs.
            column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Drag handle pill for bottom drawer
                if (position == ShadcnDrawerPosition.Bottom) {
                    row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth().height(12f.dp),
                    ) {
                        surface(
                            id = "$id.handle",
                            // Real shadcn/vaul drag handle is h-2 w-[100px] (8x100dp).
                            modifier = Modifier.width(100f.dp).height(8f.dp),
                            style = Style {
                                background(resolvedTheme.palette.muted)
                                shape(resolvedTheme.radii.full)
                            },
                        ) {}
                    }
                    spacer(Modifier.height(8f.dp))
                }

                // Header slot with optional close button inside top-right of panel
                if (header != null || showCloseButton) {
                    row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = UiAlignment.Vertical.Center,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (header != null) {
                            column { header(bounds) }
                        } else {
                            spacer(Modifier.width(1f.dp))
                        }
                        if (showCloseButton) {
                            // "✕" (U+2715) isn't in the bitmap font atlas (ASCII 32-126 only),
                            // so a text label silently drew nothing -- use the real icon glyph.
                            shadcnButton(
                                id = "$id.close",
                                variant = ShadcnButtonVariant.Ghost,
                                // Icon size: real shadcn's `icon` variant is `size-9` with no
                                // px-* at all. The default Md size would inject px-4 (16dp/side)
                                // into this 28dp box, leaving no room for the glyph.
                                size = ShadcnButtonSize.Icon,
                                modifier = Modifier.width(28f.dp).height(28f.dp),
                                onClick = onDismissRequest,
                            ) { icon(ShadcnIcons.xMark) }
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
                        modifier = Modifier.fillMaxWidth(),
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

    return UiPopupResult(slot = popupResult.slot, dismissed = popupResult.dismissed)
}
