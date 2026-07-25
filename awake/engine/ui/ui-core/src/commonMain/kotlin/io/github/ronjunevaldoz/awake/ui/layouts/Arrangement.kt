package io.github.ronjunevaldoz.awake.ui.layouts

import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.dp
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
            betweenSpacePx = if (childCount > 1) freeSpace / (childCount - 1) else 0f
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
 * Distributes a row/column's main-axis space between fixed-size children (untouched) and
 * [LayoutWeight]-tagged children (share [remaining] proportionally to their weight) -- shared
 * by [io.github.ronjunevaldoz.awake.ui.UiScope.row]/[io.github.ronjunevaldoz.awake.ui.UiScope.column]. [measuredSizes]/[weights] are
 * parallel lists (one entry per child, `weights[i] == null` for a non-weighted child), matching
 * [io.github.ronjunevaldoz.awake.ui.context.UiMeasuredContent.slots]/[io.github.ronjunevaldoz.awake.ui.context.UiMeasuredContent.weights].
 *
 * ponytail: overflowing total weight just clamps `remaining` (and therefore every weighted
 * child) to 0 rather than shrinking non-weighted siblings -- matches the task's stated edge
 * case, not full Compose min-constraint negotiation.
 */
internal fun resolveWeightedMainAxis(
    measuredSizes: List<Float>,
    weights: List<LayoutWeight?>,
    containerSize: Float,
    gap: Float
): List<Float> {
    if (weights.none { it != null }) return measuredSizes
    val gapsTotal = gap * (measuredSizes.size - 1).coerceAtLeast(0)
    var nonWeightedOccupied = 0f
    var totalWeight = 0f
    measuredSizes.forEachIndexed { index, size ->
        val weight = weights.getOrNull(index)
        if (weight == null) nonWeightedOccupied += size else totalWeight += weight.weight
    }
    val remaining = (containerSize - nonWeightedOccupied - gapsTotal).coerceAtLeast(0f)
    val weightUnit = if (totalWeight > 0f) remaining / totalWeight else 0f
    return measuredSizes.mapIndexed { index, size ->
        val weight = weights.getOrNull(index) ?: return@mapIndexed size
        val allotted = (weightUnit * weight.weight).coerceAtLeast(0f)
        if (weight.fill) allotted else min(size, allotted)
    }
}

fun defaultArrangement(): Arrangement = Arrangement.spacedBy(UiSpacing.sm)

internal fun deprecatedGapArrangement(gapPx: Float): Arrangement = Arrangement.spacedBy(gapPx.dp)
