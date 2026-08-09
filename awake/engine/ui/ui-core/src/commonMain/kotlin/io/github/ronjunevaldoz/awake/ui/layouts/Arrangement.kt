// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.layouts

import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.UiSpacing
import io.github.ronjunevaldoz.awake.ui.layout.LayoutWeight
import io.github.ronjunevaldoz.awake.ui.toPx
import kotlin.math.min

sealed interface Arrangement {
    data class SpacedBy(val space: Dp) : Arrangement
    data object Start : Arrangement
    data object Center : Arrangement
    data object End : Arrangement
    data object SpaceBetween : Arrangement
    data object SpaceEvenly : Arrangement
    data object SpaceAround : Arrangement

    companion object {
        fun spacedBy(space: Dp): Arrangement = SpacedBy(space)
    }
}

internal data class ArrangementPlan(
    val leadingSpacePx: Float,
    val betweenSpacePx: Float,
)

fun Arrangement.baseSpacingPx(): Float = when (this) {
    is Arrangement.SpacedBy -> space.toPx()
    else -> 0f
}

internal fun Arrangement.requiresMeasuredDistribution(): Boolean = when (this) {
    Arrangement.Start -> false
    is Arrangement.SpacedBy -> false
    else -> true
}

internal fun Arrangement.plan(containerSize: Float, childCount: Int, occupiedSize: Float): ArrangementPlan {
    if (childCount <= 0) return ArrangementPlan(leadingSpacePx = 0f, betweenSpacePx = 0f)
    val freeSpace = (containerSize - occupiedSize).coerceAtLeast(0f)
    return when (this) {
        Arrangement.Start -> ArrangementPlan(0f, 0f)
        is Arrangement.SpacedBy -> ArrangementPlan(0f, space.toPx())
        Arrangement.Center -> ArrangementPlan(freeSpace / 2f, 0f)
        Arrangement.End -> ArrangementPlan(freeSpace, 0f)
        Arrangement.SpaceBetween -> ArrangementPlan(
            leadingSpacePx = 0f,
            betweenSpacePx = if (childCount > 1) freeSpace / (childCount - 1) else 0f,
        )
        Arrangement.SpaceEvenly -> {
            val spacing = freeSpace / (childCount + 1)
            ArrangementPlan(leadingSpacePx = spacing, betweenSpacePx = spacing)
        }
        Arrangement.SpaceAround -> {
            val spacing = freeSpace / childCount
            ArrangementPlan(leadingSpacePx = spacing / 2f, betweenSpacePx = spacing)
        }
    }
}

/**
 * Distributes a row/column's main-axis space between non-weighted children (untouched, except
 * for the FillMax case below) and [LayoutWeight]-tagged children (share [remaining] proportionally
 * to their weight) -- shared by [io.github.ronjunevaldoz.awake.ui.UiScope.row]/[io.github.ronjunevaldoz.awake.ui.UiScope.column].
 * [measuredSizes]/[weights]/[fillsMainAxis] are parallel lists (one entry per child, `weights[i]
 * == null` for a non-weighted child), matching
 * [io.github.ronjunevaldoz.awake.ui.context.UiMeasuredContent.slots]/[io.github.ronjunevaldoz.awake.ui.context.UiMeasuredContent.weights]/
 * [io.github.ronjunevaldoz.awake.ui.context.UiMeasuredContent.fillsMainAxis].
 *
 * Resolution order matters: weighted children claim their share of [containerSize] first, as if
 * every non-weighted `FillMax` sibling weren't there at all -- only a non-weighted child with a
 * real fixed/intrinsic-content size counts as "occupied" up front. `FillMax` non-weighted
 * siblings then divide whatever's left over *after* weighted children are resolved (which may be
 * zero -- a `fillMaxWidth()` sibling competing with `weight()` siblings for the same axis is a
 * genuine contradiction in intent, and Compose resolves it the same way: weighted children are
 * measured against the real constraint first, so a `fillMaxWidth()` sibling naturally shrinks to
 * make room instead of starving them). Fixes a real, already-hit bug class (`shadcnField*`,
 * checkout-form grid, `shadcnToggleGroup` all needed `weight()`-only workarounds this session
 * because the old order let a `FillMax` sibling's own trial-measured (pre-weight-resolution) size
 * claim the entire row/column as "occupied" before weighted siblings got anything).
 *
 * ponytail: overflowing total weight just clamps `remaining` (and therefore every weighted
 * child) to 0 rather than shrinking non-weighted siblings -- matches the task's stated edge
 * case, not full Compose min-constraint negotiation. Multiple non-weighted FillMax siblings all
 * separately claim the *same* leftover amount (not shared/split between them) -- matches the old
 * behavior for that case and mirrors Compose's own overlapping-fillMaxWidth-siblings outcome.
 */
internal fun resolveWeightedMainAxis(
    measuredSizes: List<Float>,
    weights: List<LayoutWeight?>,
    containerSize: Float,
    gap: Float,
    fillsMainAxis: List<Boolean> = emptyList(),
): List<Float> {
    if (weights.none { it != null }) return measuredSizes
    val gapsTotal = gap * (measuredSizes.size - 1).coerceAtLeast(0)
    var occupied = 0f
    var totalWeight = 0f
    measuredSizes.forEachIndexed { index, size ->
        val weight = weights.getOrNull(index)
        when {
            weight != null -> totalWeight += weight.weight
            // A non-weighted FillMax sibling's trial size is an artifact of the trial measuring
            // against the full container before any weighted sibling claimed its share -- it is
            // not a real "occupied" claim, so it must not shrink what weighted siblings get.
            fillsMainAxis.getOrNull(index) == true -> Unit
            else -> occupied += size
        }
    }
    val remaining = (containerSize - occupied - gapsTotal).coerceAtLeast(0f)
    val weightUnit = if (totalWeight > 0f) remaining / totalWeight else 0f
    var weightedTotal = 0f
    val withWeightResolved = measuredSizes.mapIndexed { index, size ->
        val weight = weights.getOrNull(index) ?: return@mapIndexed size
        val allotted = (weightUnit * weight.weight).coerceAtLeast(0f)
        val resolved = if (weight.fill) allotted else min(size, allotted)
        weightedTotal += resolved
        resolved
    }
    val leftoverForFill = (containerSize - occupied - weightedTotal - gapsTotal).coerceAtLeast(0f)
    return withWeightResolved.mapIndexed { index, size ->
        if (weights.getOrNull(index) != null) return@mapIndexed size
        if (fillsMainAxis.getOrNull(index) == true) leftoverForFill else size
    }
}

fun defaultArrangement(): Arrangement = Arrangement.spacedBy(UiSpacing.sm)
