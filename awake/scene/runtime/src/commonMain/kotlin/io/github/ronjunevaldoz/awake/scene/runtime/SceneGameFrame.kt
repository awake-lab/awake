// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

import io.github.ronjunevaldoz.awake.ui.UiBoxConstraints
import io.github.ronjunevaldoz.awake.ui.context.UiMeasureTrialStats
import io.github.ronjunevaldoz.awake.ui.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.textLayoutCacheStats
import kotlin.math.roundToInt

/** Root-level full-viewport box for a [SceneGameRuntime] overlay -- same shape as
 * [io.github.ronjunevaldoz.awake.engine.application.GameUiRuntime.frame], the port that closes
 * the `GameUiRuntime`/`SceneGameRuntime` composability gap. Takes [viewportWidth]/
 * [viewportHeight] as explicit params (available from [SceneOverlayBlock]'s own signature)
 * instead of reading stored fields -- `SceneGameRuntime` doesn't keep viewport size in a field
 * the way `GameUiRuntime` does. */
fun SceneGameRuntime.frame(
    viewportWidth: Float,
    viewportHeight: Float,
    contentAlignment: UiAlignment = UiAlignment.TopStart,
    block: BoxScope.(constraints: UiBoxConstraints) -> Unit,
) {
    val rootSlot = UiBounds(0f, 0f, viewportWidth, viewportHeight)
    uiContext.createBox(
        slot = rootSlot,
        contentAlignment = contentAlignment,
    ).block(
        UiBoxConstraints(
            maxWidthPx = viewportWidth,
            maxHeightPx = viewportHeight,
        ),
    )
}

/** Same shape as [io.github.ronjunevaldoz.awake.engine.application.GameFrameStats] -- see that
 * type's doc comment. */
data class SceneFrameStats(
    val frameTimeMs: Float,
    val fps: Float,
    val trialPasses: Int,
    val textCacheHits: Int,
    val textCacheMisses: Int,
) {
    val textCacheTotal: Int get() = textCacheHits + textCacheMisses
    val textCacheHitRatePercent: Int get() = if (textCacheTotal > 0) (textCacheHits * 100 / textCacheTotal) else 0
}

fun SceneGameRuntime.frameStats(): SceneFrameStats {
    val (cacheHits, cacheMisses) = textLayoutCacheStats()
    return SceneFrameStats(
        frameTimeMs = averageFrameTimeMs.roundToTenth(),
        fps = fps,
        trialPasses = UiMeasureTrialStats.trialCount,
        textCacheHits = cacheHits,
        textCacheMisses = cacheMisses,
    )
}

private fun Float.roundToTenth(): Float = (this * 10f).roundToInt() / 10f
