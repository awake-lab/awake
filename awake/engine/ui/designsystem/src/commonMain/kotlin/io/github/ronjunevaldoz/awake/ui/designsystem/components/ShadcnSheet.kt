// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.EaseOut
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiPopupPositionProvider
import io.github.ronjunevaldoz.awake.ui.UiPopupProperties
import io.github.ronjunevaldoz.awake.ui.UiPopupResult
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.animateFloatTween
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layout.UiAlignment
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
import io.github.ronjunevaldoz.awake.ui.scope.frameBounds
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.unstyled.components.UiEdge
import io.github.ronjunevaldoz.awake.ui.unstyled.components.icon
import io.github.ronjunevaldoz.awake.ui.unstyled.overlayScrim
import io.github.ronjunevaldoz.awake.ui.unstyled.components.slideInPanelBounds

private const val SHEET_SCRIM_ALPHA = 0.5f
private val DefaultSheetScrimColor = Color.Black.withAlpha(SHEET_SCRIM_ALPHA)
private const val SHEET_SLIDE_DURATION_MS = 250f

/** Thickness of the single inner-edge border real shadcn's `SheetContent` draws (`border-l`/
 * `border-r`/`border-t`/`border-b` depending on [ShadcnSheetSide]) -- the edge facing the page
 * content, not the screen edge the panel is flush against. */
private val SheetBorderWidth = 1f.dp

/** Draws only the edge of [slot] that faces the page content -- the opposite edge from the one
 * [side] is anchored against. A plain [io.github.ronjunevaldoz.awake.ui.graphics.border] draws
 * all four sides, which is wrong here: a Left sheet is flush against the screen's own left edge,
 * so only its right edge is a real visual seam. */
private fun UiScope.emitSheetInnerEdgeBorder(slot: UiBounds, side: ShadcnSheetSide, color: Color) {
    val strokePx = SheetBorderWidth.toPx()
    val quad = when (side) {
        ShadcnSheetSide.Left -> UiDrawPrimitive.Quad(
            slot.x + slot.width - strokePx,
            slot.y,
            strokePx,
            slot.height,
            color,
        )

        ShadcnSheetSide.Right -> UiDrawPrimitive.Quad(slot.x, slot.y, strokePx, slot.height, color)
        ShadcnSheetSide.Top -> UiDrawPrimitive.Quad(
            slot.x,
            slot.y + slot.height - strokePx,
            slot.width,
            strokePx,
            color,
        )

        ShadcnSheetSide.Bottom -> UiDrawPrimitive.Quad(slot.x, slot.y, slot.width, strokePx, color)
    }
    emit(quad)
}

/** The panel's own header row: optional caller [header] slot on the start side, close button
 * (real shadcn's `SheetClose`, always top-right regardless of [ShadcnSheetSide]) on the end. */
private fun ColumnScope.shadcnSheetHeaderRow(
    id: String,
    slot: UiBounds,
    showCloseButton: Boolean,
    onDismissRequest: () -> Unit,
    header: (ColumnScope.(slot: UiBounds) -> Unit)?,
) {
    row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = UiAlignment.Vertical.Center,
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (header != null) {
            column { header(slot) }
        } else {
            spacer(Modifier.width(1f.dp))
        }
        if (showCloseButton) {
            // onClick already calls onDismissRequest directly -- no separate "closeClicked"
            // bookkeeping needed (see popupResult.dismissed in shadcnSheet, which only ever
            // reflects click-outside, same split shadcnDrawer uses).
            shadcnButton(
                id = "$id.close",
                variant = ShadcnButtonVariant.Ghost,
                // Icon size, not the default: real shadcn's `icon` variant is `size-9` with no
                // px-* at all. Leaving it Md gave this 28dp box a px-4 (16dp/side) inset, which
                // exceeds the box outright and left no room for the glyph to draw.
                size = ShadcnButtonSize.Icon,
                modifier = Modifier.width(28f.dp).height(28f.dp),
                onClick = onDismissRequest,
            ) { icon(ShadcnIcons.xMark) }
        }
    }
}

/** Fixed per-call chrome config, bundled so [shadcnSheetPanel] stays under the parameter-count
 * threshold instead of taking each of these as its own top-level parameter. */
private class ShadcnSheetChrome(
    val id: String,
    val side: ShadcnSheetSide,
    val showCloseButton: Boolean,
    val onDismissRequest: () -> Unit,
)

/** The panel's visual chrome: a full-bleed (no radius) surface at [slot] plus the single
 * inner-edge border, wrapping the header row / [content] / [actions] stack. */
private fun UiScope.shadcnSheetPanel(
    chrome: ShadcnSheetChrome,
    slot: UiBounds,
    content: ColumnScope.(slot: UiBounds) -> Unit,
    modifier: UiModifier = Modifier,
    header: (ColumnScope.(slot: UiBounds) -> Unit)? = null,
    actions: (RowScope.(slot: UiBounds) -> Unit)? = null,
) {
    val resolvedTheme = theme.asShadcnTheme()
    surface(
        id = chrome.id,
        modifier = modifier.width(Dimension.Fixed(slot.width.dp))
            .height(Dimension.Fixed(slot.height.dp)),
        style = resolvedTheme.components.surface then Style {
            shape(UiShape.none)
            borderWidth(UiShape.none)
            contentPadding(16f.dp)
        },
    ) {
        column(modifier = Modifier.fillMaxWidth()) {
            if (header != null || chrome.showCloseButton) {
                shadcnSheetHeaderRow(chrome.id, slot, chrome.showCloseButton, chrome.onDismissRequest, header)
                spacer(Modifier.height(12f.dp))
            }

            content(slot)

            if (actions != null) {
                spacer(Modifier.height(12f.dp))
                row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = UiAlignment.Vertical.Center,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    actions(slot)
                }
            }
        }
    }
    emitSheetInnerEdgeBorder(slot, chrome.side, resolvedTheme.colors.border)
}

/** [side]-anchored slide offset: 0 at rest (flush against the anchored edge), positive pushes
 * the panel past that edge and off-screen -- see [shadcnSheet]'s own [animateFloatTween] call. */
private fun ShadcnSheetSide.toUiEdge(): UiEdge = when (this) {
    ShadcnSheetSide.Top -> UiEdge.Top
    ShadcnSheetSide.Right -> UiEdge.Right
    ShadcnSheetSide.Bottom -> UiEdge.Bottom
    ShadcnSheetSide.Left -> UiEdge.Left
}

private fun sheetPositionProvider(side: ShadcnSheetSide, sizeDp: Dp, slideProgress: Float) =
    UiPopupPositionProvider { _, windowBounds, _ ->
        slideInPanelBounds(
            edge = side.toUiEdge(),
            sizePx = sizeDp.toPx(),
            slideProgress = slideProgress,
            viewportWidth = windowBounds.width,
            viewportHeight = windowBounds.height,
        )
    }

/**
 * Real shadcn's `Sheet` (built on Radix's `Dialog` primitive): a full-height (left/right) or
 * full-width (top/bottom) side-anchored modal panel, distinct from [shadcnDrawer] (bottom-only,
 * vaul-flavored) even though both are popup-backed slide-over panels. Unlike [shadcnDrawer], the
 * panel is edge-to-edge (no corner radius -- real `SheetContent` has none) and only ever draws a
 * border on the single edge facing the page content.
 */
fun UiScope.shadcnSheet(
    id: String,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: UiModifier = Modifier,
    side: ShadcnSheetSide = ShadcnSheetSide.Right,
    // Real shadcn's SheetContent left/right variants cap at sm:max-w-sm = 384dp (on top of a
    // w-3/4 viewport-relative floor this fixed-dp API doesn't model).
    sizeDp: Dp = 384f.dp,
    showCloseButton: Boolean = true,
    header: (ColumnScope.(slot: UiBounds) -> Unit)? = null,
    actions: (RowScope.(slot: UiBounds) -> Unit)? = null,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiPopupResult {
    if (!expanded) return UiPopupResult(slot = null, dismissed = false)

    // Real slide (not just popup()'s own alpha fade): 0 = fully off-screen past the anchored
    // edge, 1 = resting flush against it. initial=0 so a freshly-mounted, already-expanded sheet
    // animates in instead of popping in at rest.
    val slideProgress = animateFloatTween(
        id = "$id.slide",
        target = 1f,
        initial = 0f,
        durationMs = SHEET_SLIDE_DURATION_MS,
        easing = EaseOut,
    )

    val frame = frameBounds()
    overlayScrim(frame, DefaultSheetScrimColor)

    val popupResult = popup(
        id = id,
        expanded = true,
        anchorSlot = UiBounds(0f, 0f, 0f, 0f),
        positionProvider = sheetPositionProvider(side, sizeDp, slideProgress),
        // clippingEnabled=false -- the whole point of [slideProgress] is to place this panel
        // partially or fully OFF the window while animating in; popup()'s default clamp would
        // pin it back on-screen immediately and defeat the slide.
        properties = UiPopupProperties(dismissOnClickOutside = true, clippingEnabled = false),
    ) { slot ->
        shadcnSheetPanel(
            chrome = ShadcnSheetChrome(id, side, showCloseButton, onDismissRequest),
            slot = slot,
            content = content,
            modifier = modifier,
            header = header,
            actions = actions,
        )
    }

    if (popupResult.dismissed) {
        onDismissRequest()
    }
    return popupResult
}
