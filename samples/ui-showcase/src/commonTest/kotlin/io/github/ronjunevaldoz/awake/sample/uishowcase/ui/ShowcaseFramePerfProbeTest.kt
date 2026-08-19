// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.context.UiMeasureTrialStats
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnThemeValues
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxHeight
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.modifier.padding
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.modifier.weight
import kotlin.test.Test
import kotlin.time.measureTime
import io.github.ronjunevaldoz.awake.ui.context.UiFrameInput
import io.github.ronjunevaldoz.awake.ui.context.LocalFont

/** Perf probe for the wasm "1 fps" report: same shell composition, desktop JVM numbers.
 * Prints steady-state ms/frame and trial-measure passes per frame. */
class ShowcaseFramePerfProbeTest {

    @Test
    fun measureShellFrameCost() {
        val ui = UiContext()
        val state = UiShowcaseRuntimeState()
        ui.pushLocal(LocalFont, BitmapFont())

        fun frame() {
            ui.beginFrame(UiFrameInput(viewportWidth = 1280f, viewportHeight = 900f, input = UiInputState(pointerX = 400f, pointerY = 300f)))
            ui.showcaseRoot(theme = shadcnThemeValues(dark = false), bounds = UiBounds(0f, 0f, 1280f, 900f)) {
                row(
                    id = "showcase-probe-row",
                    cacheKey = "probe",
                    modifier = Modifier.padding(24f.dp).fillMaxWidth().fillMaxHeight(),
                ) {
                    column(modifier = Modifier.fillMaxHeight()) {
                        drawUiShowcaseSidebar(compact = false)
                    }
                    column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        drawUiShowcasePageContent(state, showInlineMenu = false)
                    }
                }
            }
            ui.finishFrame().primitives
        }

        repeat(30) { frame() }

        UiMeasureTrialStats.enabled = true
        UiMeasureTrialStats.reset()
        val frames = 100
        val elapsed = measureTime { repeat(frames) { frame() } }
        val trials = UiMeasureTrialStats.trialCount
        UiMeasureTrialStats.enabled = false

        val ms = elapsed.inWholeMicroseconds / 1000.0 / frames
        val trialsPerFrame = trials / frames
        println("PERF showcase-shell msPerFrame=$ms trialsPerFrame=$trialsPerFrame")
        kotlin.test.assertTrue(
            trialsPerFrame < 8500,
            "trialsPerFrame must stay below 8,500 -- got $trialsPerFrame",
        )
    }
}
