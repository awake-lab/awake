// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

import io.github.ronjunevaldoz.awake.ui.UiBoxConstraints
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiMeasureTrialStats
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.createUiScope
import io.github.ronjunevaldoz.awake.ui.headless.internal.text.textLayoutCacheStats
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
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

/**
 * Headless facade entry point for scene overlays.
 *
 * [frame] remains available for advanced Core layout authors, while ordinary component trees
 * should start here so the callback receives only the public Headless [UiScope].
 */
fun SceneGameRuntime.headlessFrame(
    viewportWidth: Float,
    viewportHeight: Float,
    block: UiScope.() -> Unit,
) {
    uiContext.createUiScope(UiBounds(0f, 0f, viewportWidth, viewportHeight)).block()
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
