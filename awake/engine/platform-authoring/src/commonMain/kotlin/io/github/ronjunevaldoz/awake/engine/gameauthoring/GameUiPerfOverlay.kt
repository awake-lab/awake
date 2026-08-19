// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.gameauthoring

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.context.UiMeasureTrialStats
import io.github.ronjunevaldoz.awake.ui.headless.internal.text.text
import io.github.ronjunevaldoz.awake.ui.headless.internal.text.textLayoutCacheStats
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.offset
import kotlin.math.roundToInt

/**
 * Raw numbers behind [drawPerfStatsOverlay]'s built-in HUD, for a game that wants its own
 * custom display instead (e.g. a different layout/subset, or styled to match its own theme)
 * -- read this directly rather than being stuck with the built-in green-text HUD. Always
 * computable regardless of [GameUiRuntime.perfStatsEnabled]/[GameUiRuntime.debugOverlayEnabled];
 * [trialPasses] just reads `0` when [UiMeasureTrialStats]
 * tracking isn't enabled, since nothing incremented it that frame.
 */
data class GameFrameStats(
    val frameTimeMs: Float,
    val fps: Float,
    val trialPasses: Int,
    val textCacheHits: Int,
    val textCacheMisses: Int,
) {
    val textCacheTotal: Int get() = textCacheHits + textCacheMisses
    val textCacheHitRatePercent: Int get() = if (textCacheTotal > 0) (textCacheHits * 100 / textCacheTotal) else 0
}

fun GameUiRuntime.frameStats(): GameFrameStats {
    val (cacheHits, cacheMisses) = textLayoutCacheStats()
    return GameFrameStats(
        frameTimeMs = averageFrameTimeMs.roundToTenth(),
        fps = fps,
        trialPasses = UiMeasureTrialStats.trialCount,
        textCacheHits = cacheHits,
        textCacheMisses = cacheMisses,
    )
}

/**
 * Minimal live perf HUD, toggled by [perfStatsEnabled] (F3 also flips this
 * alongside the wireframe overlay by default, see [render]). Shows [frameStats]'
 * frame time/fps, row/column trial-measure count, and text-layout cache hit rate -- the two
 * known "redundant per-frame work" categories this session's WebGPU-lag investigation surfaced
 * (see docs/tasks/2026-08-02-trial-measure-cross-frame-cache.md and
 * docs/tasks/2026-08-03-text-layout-measure-cache.md).
 *
 * Deliberately minimal: no history graph, no per-widget breakdown, no persistence -- just the
 * current numbers, redrawn every frame like any other overlay content. A v2 (frame-time history
 * graph, GC/memory stats, per-widget cost) is real follow-up work, not part of this pass. A game
 * that wants a different look can skip this entirely and read [frameStats] itself instead.
 */
internal fun GameUiRuntime.drawPerfStatsOverlay() {
    val stats = frameStats()
    val lines = listOf(
        "${stats.frameTimeMs}ms  ${stats.fps.roundToInt()} fps",
        "trial passes: ${stats.trialPasses}",
        "text cache: ${stats.textCacheHitRatePercent}% hit (${stats.textCacheHits}/${stats.textCacheTotal})",
    )

    frame {
        column(
            id = "awake-perf-stats-overlay",
            modifier = Modifier.offset(8f.dp, 8f.dp),
        ) {
            lines.forEach { line ->
                text(
                    label = line,
                    color = Color(0.3f, 1f, 0.4f, 1f),
                    textStyle = null,
                )
            }
        }
    }
}

private fun Float.roundToTenth(): Float = (this * 10f).roundToInt() / 10f
