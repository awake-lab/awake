// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.context

import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import kotlin.math.max
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

internal class UiContextMeasureState {
    internal var measuredMaxRight = 0f
    // Same running max as [measuredMaxRight], but skipping slots recorded with
    // contributesToWrapWidth = false (a Dimension.FillMax-width child, e.g. a divider --
    // see [record]). Kept separate rather than folded into [measuredMaxRight] directly so a
    // WrapContent container with *only* FillMax children (e.g. a lone word-wrapped Text whose
    // width is FillMax purely to know its own wrap boundary, not because it wants to dictate
    // the container's size) still falls back to the old, correct "hug whatever's there" answer
    // instead of collapsing to zero -- see snapshot().
    internal var measuredMaxRightExcludingFill = 0f
    internal var measuredMaxBottom = 0f
    internal val measuredSlots = ArrayList<UiSlot>()
    internal val measuredWeights = ArrayList<LayoutWeight?>()

    fun beginFrame() {
        measuredMaxRight = 0f
        measuredMaxRightExcludingFill = 0f
        measuredMaxBottom = 0f
        measuredSlots.clear()
        measuredWeights.clear()
    }

    fun record(slot: UiSlot, contributesToWrapWidth: Boolean = true, contributesToChildList: Boolean = true) {
        // A child that explicitly requested Dimension.FillMax on the cross axis (e.g. a
        // ColumnScope child's width) fills whatever width its container ends up with -- it
        // has no real "intrinsic" width of its own, so as long as some *other* sibling has a
        // real (non-fill) width to hug, this FillMax child must not be allowed to dictate the
        // WrapContent container's own measured width just because it happened to be present in
        // the content (e.g. `shadcnCard`'s divider -- `separator()` defaults to fillMaxWidth()
        // -- must not force the whole card full-width when the body content is short).
        val right = slot.x + slot.width
        measuredMaxRight = max(measuredMaxRight, right)
        if (contributesToWrapWidth) {
            measuredMaxRightExcludingFill = max(measuredMaxRightExcludingFill, right)
        }
        measuredMaxBottom = max(measuredMaxBottom, slot.y + slot.height)
        // measuredMaxRight/measuredMaxRightExcludingFill/measuredMaxBottom deliberately keep
        // tracking *every* descendant claim (not just direct children) -- WrapContent width/
        // height resolution (see snapshot()) has always relied on hugging the deepest actual
        // content extent, e.g. a WrapContent shadcnCard sizing itself around a nested row's own
        // buttons. measuredSlots is different: it's an index-paired-with-measuredWeights list
        // that resolveWeightedMainAxis() reads as "one entry per direct child" -- a composite
        // direct child's *own* grandchildren claims (recorded while contributesToChildList is
        // false, see UiContext.withMeasuredRecordingSuppressed) must not leak into this list or
        // they corrupt that pairing (measuredSlots[i] no longer lines up with measuredWeights[i]
        // for the row/column's actual i-th direct child).
        if (contributesToChildList) {
            measuredSlots += slot
        }
    }

    fun recordWeight(weight: LayoutWeight?, contributesToChildList: Boolean = true) {
        if (contributesToChildList) {
            measuredWeights += weight
        }
    }

    fun measureColumnContent(
        width: Float,
        gap: Float,
        insets: UiInsets,
        sourceContext: UiContext,
        height: Float = 100_000f,
        content: ColumnScope.(slot: UiSlot) -> Unit
    ): UiMeasuredContent {
        val measureContext = createMeasureContext(sourceContext)
        val outerSlot = UiSlot(0f, 0f, width.coerceAtLeast(0f), height.coerceAtLeast(0f))
        val measureScope = measureContext.createColumn(
            slot = outerSlot,
            // This trial column's own cursor advance must match the real column's actual
            // [gap] (already resolved by the caller from its real arrangement), not this trial
            // scope's own default arrangement -- reconstruct an equivalent SpacedBy arrangement
            // via .px (not .dp) so verticalArrangement.baseSpacingPx() round-trips back to the
            // exact same pixel value regardless of UiDensity.scale.
            verticalArrangement = Arrangement.spacedBy(gap.px),
            insets = insets
        )
        measureScope.content(outerSlot)
        return measureContext.measuredContentSnapshot()
    }

    fun measureRowContent(
        height: Float,
        gap: Float,
        insets: UiInsets,
        sourceContext: UiContext,
        width: Float = 100_000f,
        content: RowScope.(slot: UiSlot) -> Unit
    ): UiMeasuredContent {
        val measureContext = createMeasureContext(sourceContext)
        val outerSlot = UiSlot(0f, 0f, width.coerceAtLeast(0f), height.coerceAtLeast(0f))
        val measureScope = measureContext.createRow(
            slot = outerSlot,
            // See the matching comment in measureColumnContent -- same real-gap-not-default
            // reasoning, row-side.
            horizontalArrangement = Arrangement.spacedBy(gap.px),
            insets = insets
        )
        measureScope.content(outerSlot)
        return measureContext.measuredContentSnapshot()
    }

    private fun createMeasureContext(sourceContext: UiContext): UiContext {
        // Share the real state store rather than a fresh one -- a WrapContent/scroll trial pass
        // re-executes the same content, and any persisted (rememberStateValue) branch inside
        // that content (e.g. "which page is selected") must see the real current value, not
        // reset to its default. See RepoBugTest for the regression this fixes.
        val measureContext = UiContext(measuring = true, stateStore = sourceContext.stateStoreInternal())
        measureContext.beginFrame(
            UiFrameInput(
                viewportWidth = 100_000f,
                viewportHeight = 100_000f,
                input = UiInputState(),
                deltaSeconds = 0f
            )
        )
        measureContext.pushTextStyle(sourceContext.currentTextStyle)
        measureContext.pushFont(sourceContext.currentFont)
        measureContext.pushTheme(sourceContext.currentTheme)
        return measureContext
    }
}
