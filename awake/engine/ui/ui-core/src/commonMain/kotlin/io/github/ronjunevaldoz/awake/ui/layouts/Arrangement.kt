package io.github.ronjunevaldoz.awake.ui.layouts

import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.toPx

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

fun defaultArrangement(): Arrangement = Arrangement.spacedBy(UiSpacing.sm)

internal fun deprecatedGapArrangement(gapPx: Float): Arrangement = Arrangement.spacedBy(gapPx.dp)
