// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.application

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.context.UiMeasureTrialStats
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.offset
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.textLayoutCacheStats
import kotlin.math.roundToInt

/**
 * Minimal live perf HUD, toggled by [GameUiRuntime.debugOverlayEnabled] alongside the F3
 * wireframe overlay (see [GameUiRuntime.render]). Shows frame time/fps, the row/column
 * trial-measure count for the frame just composed ([UiMeasureTrialStats]), and the text-layout
 * cache's cumulative hit rate ([textLayoutCacheStats]) -- the two known "redundant per-frame
 * work" categories this session's WebGPU-lag investigation surfaced (see
 * docs/tasks/2026-08-02-trial-measure-cross-frame-cache.md and
 * docs/tasks/2026-08-03-text-layout-measure-cache.md).
 *
 * Deliberately minimal: no history graph, no per-widget breakdown, no persistence -- just the
 * current numbers, redrawn every frame like any other overlay content. A v2 (frame-time history
 * graph, GC/memory stats, per-widget cost) is real follow-up work, not part of this pass.
 */
internal fun GameUiRuntime.drawPerfStatsOverlay() {
    val (cacheHits, cacheMisses) = textLayoutCacheStats()
    val cacheTotal = cacheHits + cacheMisses
    val cacheHitRatePercent = if (cacheTotal > 0) (cacheHits * 100f / cacheTotal).roundToInt() else 0

    val lines = listOf(
        "${averageFrameTimeMs.roundToTenth()}ms  ${fps.roundToInt()} fps",
        "trial passes: ${UiMeasureTrialStats.trialCount}",
        "text cache: $cacheHitRatePercent% hit ($cacheHits/$cacheTotal)"
    )

    canvas {
        column(
            id = "awake-perf-stats-overlay",
            modifier = Modifier.offset(8f.dp, 8f.dp)
        ) {
            lines.forEach { line ->
                text(
                    label = line,
                    color = Color(0.3f, 1f, 0.4f, 1f),
                    textStyle = null
                )
            }
        }
    }
}

private fun Float.roundToTenth(): Float = (this * 10f).roundToInt() / 10f
