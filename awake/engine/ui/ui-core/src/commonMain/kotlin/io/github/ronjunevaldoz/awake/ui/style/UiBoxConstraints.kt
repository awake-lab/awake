// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

enum class UiWidthSizeClass {
    Compact,
    Medium,
    Expanded
}

/** Responsive breakpoint data for a viewport-rooted layout -- lives in `ui-core` (not
 * `ui-dsl`) since it only depends on [UiDensity] and is a pure sizing concept any layer of
 * the UI stack (including `ui-unstyled`) could consume, not something specific to authored
 * DSL composition. */
data class UiBoxConstraints(
    val maxWidthPx: Float,
    val maxHeightPx: Float,
    val densityScale: Float = UiDensity.scale
) {
    val maxWidth: Float = maxWidthPx / densityScale
    val maxHeight: Float = maxHeightPx / densityScale
    val maxWidthDp: Float get() = maxWidth
    val maxHeightDp: Float get() = maxHeight

    val widthSizeClass: UiWidthSizeClass = when {
        maxWidth < 600f -> UiWidthSizeClass.Compact
        maxWidth < 840f -> UiWidthSizeClass.Medium
        else -> UiWidthSizeClass.Expanded
    }

    val isCompact: Boolean get() = widthSizeClass == UiWidthSizeClass.Compact
    val isMedium: Boolean get() = widthSizeClass == UiWidthSizeClass.Medium
    val isExpanded: Boolean get() = widthSizeClass == UiWidthSizeClass.Expanded
}
