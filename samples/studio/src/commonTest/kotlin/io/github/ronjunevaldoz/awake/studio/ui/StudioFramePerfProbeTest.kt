// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.studio.state.StudioStore
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.context.UiMeasureTrialStats
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnThemeValues
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.testing.render.NoopRenderer
import io.github.ronjunevaldoz.awake.ui.headless.createUiScope
import kotlin.test.Test
import kotlin.time.measureTime

/** Throwaway probe for the reported UI performance drop: real shell composition, steady-state
 * frame cost + trial-measure count + text-layout cache behavior. Numbers print to stdout. */
class StudioFramePerfProbeTest {

    @Test
    fun measureSteadyStateFrameCost() {
        val ui = UiContext()
        val store = StudioStore()
        val world = World()
        val renderer = NoopRenderer()
        ui.pushFont(BitmapFont())
        ui.pushTheme(shadcnThemeValues(dark = true))

        fun frame() {
            ui.beginFrame(1440f, 900f, UiInputState(pointerX = 400f, pointerY = 300f))
            ui.createUiScope(slot = UiBounds(0f, 0f, 1440f, 900f))
                .drawStudioShellBody(store, world, renderer)
            ui.finishFrame()
        }

        repeat(60) { frame() } // warmup

        UiMeasureTrialStats.enabled = true
        UiMeasureTrialStats.reset()
        val frames = 300
        val elapsed = measureTime { repeat(frames) { frame() } }
        val trialCount = UiMeasureTrialStats.trialCount
        UiMeasureTrialStats.enabled = false

        val msPerFrame = elapsed.inWholeMicroseconds / 1000.0 / frames
        println(
            "PERF studio-shell msPerFrame=$msPerFrame trialsPerFrame=${trialCount / frames}",
        )
    }
}
