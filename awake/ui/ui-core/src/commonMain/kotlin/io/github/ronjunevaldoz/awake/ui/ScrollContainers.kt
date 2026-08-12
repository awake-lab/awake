// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.graphics.clip
import io.github.ronjunevaldoz.awake.ui.graphics.emitFillAndBorder
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layout.horizontalPx
import io.github.ronjunevaldoz.awake.ui.layout.inset
import io.github.ronjunevaldoz.awake.ui.layout.verticalPx
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.baseSpacingPx
import io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.scope.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.scope.debugScopeLabel
import io.github.ronjunevaldoz.awake.ui.scope.fillHeightOrNull
import io.github.ronjunevaldoz.awake.ui.scope.fillWidthOrNull
import io.github.ronjunevaldoz.awake.ui.scope.hasBoundedFillHeight
import io.github.ronjunevaldoz.awake.ui.scope.hasBoundedFillWidth
import io.github.ronjunevaldoz.awake.ui.scope.measureColumnContent
import io.github.ronjunevaldoz.awake.ui.scope.onOverScrollable
import io.github.ronjunevaldoz.awake.ui.scope.onScrollConsumed
import io.github.ronjunevaldoz.awake.ui.scope.recordSemantic
import io.github.ronjunevaldoz.awake.ui.scope.resolveStyle
import io.github.ronjunevaldoz.awake.ui.style.Style

data class UiScrollPanelResult(
    val slot: UiBounds,
    val viewport: UiBounds,
    val contentWidth: Float,
    val contentHeight: Float,
    val verticalThumb: UiScrollThumb?,
    val horizontalThumb: UiScrollThumb?,
)

/**
 * Enhanced dual-axis scroll container.
 * Sizing, scrolling state, and configuration are extracted from [modifier].
 * Visuals are resolved from [style].
 */
fun UiPrimitiveScope.scrollPanel(
    id: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    verticalArrangement: Arrangement = defaultArrangement(),
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiScrollPanelResult {
    val state =
        requireNotNull(modifier.scrollState) { "scrollPanel requires a scrollState on the modifier" }
    val config = modifier.scrollConfig
    val requestedWidth = modifier.widthDimension ?: Dimension.FillMax
    val requestedHeight = modifier.heightDimension ?: Dimension.WrapContent
    val containerLabel = modifier.testTag ?: id

    val currentTheme = theme
    val resolved = resolveStyle(
        style = style then (modifier.styleable ?: Style.Empty),
        // Neutral, not currentTheme.components.surface -- unlike [resolveVisualSurface]'s
        // non-scroll path (which only paints a background/border when [style] itself resolves
        // one, via smartColumn's hasResolvedVisuals gate), this ran unconditionally: any plain
        // `column(...).verticalScroll(...)` with no [style] at all silently rendered as a
        // themed card (border + card background), a real bug -- a bare scroll container should
        // stay invisible by default, exactly like a bare non-scroll column, and only paint
        // when the caller's own [style] asks for it.
        defaults = Style {
            shape(UiShape.md)
            borderWidth(UiShape.none)
        },
    )
    val paddingWidth = resolved.contentPadding.horizontalPx()
    val paddingHeight = resolved.contentPadding.verticalPx()
    // Overlay scrollbar (shadcn/ui's own scroll-area.tsx convention, see the reference's
    // ShadcnScrollArea.kt: content fills the full box, the thumb is drawn on top via
    // Modifier.align, never narrowing the content) -- the scrollbar is painted directly over
    // the last few content pixels instead of reserving a permanent width/height slice, so it
    // never shrinks usable content area the way a space-reserving scrollbar would.
    val scrollbarWidthPx = config.width.toPx().coerceAtLeast(0f)
    // Repurposed for the overlay thumb: the inset between the thumb and the container's own
    // edge, instead of the old space-reserving gap between the scrollbar and narrowed content.
    val scrollbarEdgeInsetPx = config.gap.toPx().coerceAtLeast(0f)
    val gap = verticalArrangement.baseSpacingPx()

    fun requireBoundedAxis(axis: String): Float {
        val isBounded = when (axis) {
            "width" -> hasBoundedFillWidth()
            "height" -> hasBoundedFillHeight()
            else -> false
        }
        val value = when (axis) {
            "width" -> fillWidthOrNull()
            "height" -> fillHeightOrNull()
            else -> null
        }
        return if (isBounded && value != null) {
            value
        } else {
            error(
                "Scrollable container '$containerLabel' requested $axis=FillMax under unbounded parent ${debugScopeLabel()}. " +
                    "FillMax scroll viewports require a bounded parent $axis. " +
                    "A WrapContent ancestor usually means the parent chain never established one.",
            )
        }
    }

    fun availableOuterWidth(): Float = when (requestedWidth) {
        is Dimension.Fixed -> requestedWidth.dp.toPx()
        Dimension.FillMax -> requireBoundedAxis(axis = "width")
        Dimension.WrapContent -> (fillWidthOrNull() ?: 4096f)
    }

    val maxInnerWidth = (availableOuterWidth() - paddingWidth).coerceAtLeast(0f)

    // Overlay scrollbar: content is always measured/laid out at the container's full width --
    // no narrowed re-measure pass, since the scrollbar never claims its own content-shrinking
    // width/height slice (it paints on top of the content's own last few pixels instead).
    val measured = measureColumnContent(
        width = maxInnerWidth,
        gap = gap,
        content = content,
    )

    val containerHeight = when (requestedHeight) {
        is Dimension.Fixed -> (requestedHeight.dp.toPx() - paddingHeight).coerceAtLeast(0f)
        Dimension.FillMax -> (requireBoundedAxis(axis = "height") - paddingHeight).coerceAtLeast(0f)
        else -> measured.height // WrapContent
    }
    val verticalNeeded = when (config.verticalVisibility) {
        UiScrollbarVisibility.Always -> true
        UiScrollbarVisibility.Never -> false
        UiScrollbarVisibility.Auto -> measured.height > containerHeight
    }

    val containerWidth = when (requestedWidth) {
        is Dimension.Fixed -> (requestedWidth.dp.toPx() - paddingWidth).coerceAtLeast(0f)
        Dimension.FillMax -> (requireBoundedAxis(axis = "width") - paddingWidth).coerceAtLeast(0f)
        else -> measured.width // WrapContent
    }
    val horizontalNeeded = when (config.horizontalVisibility) {
        UiScrollbarVisibility.Always -> true
        UiScrollbarVisibility.Never -> false
        UiScrollbarVisibility.Auto -> measured.width > containerWidth
    }

    val resolvedWidth = when (requestedWidth) {
        Dimension.WrapContent -> Dimension.Fixed((measured.width + paddingWidth).px)
        else -> requestedWidth
    }
    val resolvedHeight = when (requestedHeight) {
        Dimension.WrapContent -> Dimension.Fixed((measured.height + paddingHeight).px)
        else -> requestedHeight
    }

    val slot = claimModifiedSlot(modifier.withSizeFallback(resolvedWidth, resolvedHeight))
    emitFillAndBorder(
        slot = slot,
        fillColor = resolved.background ?: Color.Transparent,
        radiusPx = resolved.shape.toPx(),
        borderWidth = resolved.borderWidth,
        borderColor = resolved.borderColor ?: currentTheme.colors.border,
        shapeSpec = resolved.shapeSpec,
    )

    val innerSlot = slot.inset(resolved.contentPadding)
    // Overlay scrollbar: the viewport is the full inner slot -- the scrollbar paints on top of
    // its last few pixels (see the thumb slots below), it does not carve out its own space.
    val viewport = UiBounds(
        x = innerSlot.x,
        y = innerSlot.y,
        width = innerSlot.width,
        height = innerSlot.height,
    )
    recordSemantic(
        role = UiSemanticRole.ScrollPanel,
        id = id,
        bounds = slot,
        contentBounds = viewport,
        clippedBounds = viewport,
    )

    state.update(
        viewportWidth = viewport.width,
        viewportHeight = viewport.height,
        contentWidth = measured.width,
        contentHeight = measured.height,
    )

    if (hitTest(slot)) {
        onOverScrollable()

        val scrollDeltaY = context.inputState.scrollDeltaY
        val scrollDeltaX = context.inputState.scrollDeltaX

        if (state.canScrollY && scrollDeltaY != 0f) {
            state.scrollBy(deltaY = -scrollDeltaY * config.scrollSpeed)
            onScrollConsumed()
        }
        if (state.canScrollX && scrollDeltaX != 0f) {
            state.scrollBy(deltaX = -scrollDeltaX * config.scrollSpeed)
            onScrollConsumed()
        }
    }

    val contentScope = childColumn(
        slot = UiBounds(
            viewport.x - state.offsetX,
            viewport.y - state.offsetY,
            viewport.width,
            viewport.height,
        ),
        verticalArrangement = verticalArrangement,
        modifier = UiModifier(testTag = containerLabel),
    )
    // The viewport's own already-resolved [slot] (recorded above via claimModifiedSlot) is the
    // only thing that should ever count toward an ancestor's WrapContent hugging -- these
    // children are deliberately positioned beyond the viewport using the current scroll offset
    // (that's the whole point of scrolling), so during a WrapContent ancestor's own trial-
    // measurement pass their claims must not leak into that ancestor's size, or the ancestor's
    // resolved height would swing with wherever the scroll thumb currently sits. See
    // UiContext.withMeasuredSubtreeIsolated's doc comment for the live report this fixes.
    context.withMeasuredSubtreeIsolated {
        clip(viewport) {
            contentScope.content(viewport)
        }
    }

    // Vertical Scrollbar -- overlay thumb only, no separate track fill (matches the shadcn-
    // compose reference's ScrollThumb: a bare thumb aligned to the container's edge, not a
    // painted track). Uses theme.colors.border, the same token the reference's own
    // `shadcnTheme.colors.border` thumb color resolves to -- not a hardcoded gray, and not
    // `primary` (too strong an accent for a passive scroll indicator).
    val vThumb = if (verticalNeeded && config.verticalVisibility != UiScrollbarVisibility.Never) {
        val vTrackSlot = UiBounds(
            x = innerSlot.x + innerSlot.width - scrollbarWidthPx - scrollbarEdgeInsetPx,
            y = innerSlot.y,
            width = scrollbarWidthPx,
            height = viewport.height,
        )
        verticalScrollThumb(vTrackSlot, state)?.also { thumb ->
            val custom = config.verticalScrollbar
            if (custom != null) {
                childAbsolute(thumb.track).custom(thumb)
            } else {
                emitFillAndBorder(
                    slot = thumb.thumb,
                    fillColor = currentTheme.colors.border,
                    radiusPx = scrollbarWidthPx / 2f,
                    borderWidth = UiShape.none,
                    borderColor = Color.Transparent,
                )
            }
        }
    } else {
        null
    }

    // Horizontal Scrollbar -- same overlay-thumb-only treatment as vertical, above.
    val hThumb =
        if (horizontalNeeded && config.horizontalVisibility != UiScrollbarVisibility.Never) {
            val hTrackSlot = UiBounds(
                x = innerSlot.x,
                y = innerSlot.y + innerSlot.height - scrollbarWidthPx - scrollbarEdgeInsetPx,
                width = viewport.width,
                height = scrollbarWidthPx,
            )
            horizontalScrollThumb(hTrackSlot, state)?.also { thumb ->
                val custom = config.horizontalScrollbar
                if (custom != null) {
                    childAbsolute(thumb.track).custom(thumb)
                } else {
                    emitFillAndBorder(
                        slot = thumb.thumb,
                        fillColor = currentTheme.colors.border,
                        radiusPx = scrollbarWidthPx / 2f,
                        borderWidth = UiShape.none,
                        borderColor = Color.Transparent,
                    )
                }
            }
        } else {
            null
        }

    return UiScrollPanelResult(
        slot = slot,
        viewport = viewport,
        contentWidth = measured.width,
        contentHeight = measured.height,
        verticalThumb = vThumb,
        horizontalThumb = hThumb,
    )
}
