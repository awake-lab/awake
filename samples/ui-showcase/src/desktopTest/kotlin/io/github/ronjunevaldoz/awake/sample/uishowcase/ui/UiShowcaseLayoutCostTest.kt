// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.ui.UiDensity
import io.github.ronjunevaldoz.awake.ui.UiScrollConfig
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.context.UiMeasureTrialStats
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebar
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSurface
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layout.toDimension
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxSize
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.padding
import io.github.ronjunevaldoz.awake.ui.modifier.verticalScroll
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.rememberScrollState
import io.github.ronjunevaldoz.awake.ui.rememberStateValue
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.toUiInputState
import kotlin.test.Test
import kotlin.time.TimeSource

/**
 * Real-numbers investigation for the "ui-showcase feels laggy on web" report (see the WebGPU
 * fixes this session and docs/reference/MIRROR_MAP.md's trial-measure entry). This measures the
 * shared, backend-agnostic ui-core layout cost on the JVM, where wall-clock timing is trustworthy
 * -- the same layout code runs unmodified under the Vulkan/desktop and WebGPU/web backends, so a
 * CPU-side layout cost here is the same CPU-side layout cost there.
 *
 * Not a regression gate -- this prints numbers and asserts nothing beyond "it ran". The printed
 * report lands in this test's stdout capture (build/test-results/desktopTest/TEST-*.xml, or run
 * with `--info` for live console output).
 */
class UiShowcaseLayoutCostTest {

    @Test
    fun measureRealShellFrameCost() {
        val heavy = measureOneFrame(pageId = "field-demo") // Checkout Form: deepest real page.
        val light = measureOneFrame(pageId = "introduction") // Overview: simplest real page.

        val report = buildString {
            appendLine("ui-showcase real-shell frame cost (sidebar + selected page content):")
            appendLine(heavy.describe("field-demo (Checkout Form, heaviest page)"))
            appendLine(light.describe("introduction (Overview, simplest page)"))
        }
        println(report)

        check(heavy.totalNanos > 0) { "heavy frame measured zero time -- instrumentation broken" }
    }

    /**
     * Real before/after numbers for the shell/sidebar `id`/`cacheKey` migration landed in
     * UiShowcaseUi.kt's non-compact `row(id = "ui-showcase-shell-row", cacheKey = "static", ...)`
     * (docs/tasks/2026-08-02-trial-measure-cross-frame-cache.md). [drawRealShowcaseShell]'s outer
     * row now goes through `BoxScope.row()` (via `ui.createBox(...)`, matching the real
     * `frame { ... }`/`BoxScope` receiver production actually uses) instead of the earlier
     * `UiContext.row()` root helper, which bypassed the hasWeightedChild trial entirely and so
     * could never have exercised this cache either way.
     *
     * Honest scope note: this isolates the *outer shell row's own* hasWeightedChild trial only --
     * the content-viewport `column(id = "ui-showcase-*-content-viewport", ...)` calls set
     * `verticalScroll`, which routes through `smartColumn`'s scrollable-container branch
     * (`scrollPanel`), not `resolveMeasuredColumn` -- the only strategy this cache's `cacheKey`
     * param is wired into -- so those two columns get zero benefit from `cacheKey` regardless of
     * whether it's supplied (that's exactly why they were left unmigrated in production, per the
     * migration's "only where you can construct a real argument for safety/benefit" discipline).
     */
    @Test
    fun measureShellRowCacheImpact() {
        val before = measureShellRowTrials(cached = false)
        val after = measureShellRowTrials(cached = true)

        val report = buildString {
            appendLine("ui-showcase shell-row hasWeightedChild-trial cache impact (steady-state, per frame):")
            appendLine("  before (no id/cacheKey): trials=$before")
            appendLine("  after  (id/cacheKey=\"static\"): trials=$after")
            val delta = before - after
            appendLine(
                "  delta: $delta fewer trial passes/frame" +
                    if (before > 0) " (${"%.1f".format(delta * 100.0 / before)}% reduction)" else ""
            )
        }
        println(report)

        // Report the real numbers above rather than assert a specific magnitude -- this is a
        // single row (two direct, unweighted children: shadcnSidebar + a scroll column), not the
        // whole shell tree, so the absolute savings here are small; the only thing worth gating on
        // is that caching never *increases* trial cost and that instrumentation actually ran.
        check(before > 0) { "uncached frame measured zero trials -- instrumentation broken" }
        check(after <= before) {
            "cached frame ($after trials) must never exceed the uncached baseline ($before trials)"
        }
    }

    /** Steady-state (JIT-warmed, cache-warmed) trial count for just [drawRealShowcaseShell]'s
     * outer shell row, isolated from the rest of the real shell's cost. */
    private fun measureShellRowTrials(cached: Boolean, warmupFrames: Int = 5, measuredFrames: Int = 3): Int {
        val state = UiShowcaseRuntimeState()
        val theme = state.showcaseTheme()
        val ui = UiContext()
        val input = Input()
        input.setPointer(down = false, x = -100f, y = -100f)

        var trials = 0
        repeat(warmupFrames + measuredFrames) { frameIndex ->
            val measured = frameIndex >= warmupFrames
            ui.beginFrame(1440f, 900f, input.updateSnapshot().toUiInputState())
            ui.pushTheme(theme)
            ui.rememberStateValue<String>("ui-showcase-page", "entry") { "field-demo" }.value = "field-demo"

            if (measured) {
                UiMeasureTrialStats.reset()
                UiMeasureTrialStats.enabled = true
            }
            drawRealShowcaseShell(ui, state, cacheShellRow = cached)
            ui.endFrame()
            if (measured) {
                trials += UiMeasureTrialStats.trialCount
                UiMeasureTrialStats.enabled = false
                UiMeasureTrialStats.reset()
            }
        }
        return trials / measuredFrames
    }

    private data class FrameCost(
        val totalNanos: Long,
        val trialNanos: Long,
        val trialCount: Int,
        val primitiveCount: Int,
        val ctorNanos: Long = 0L
    ) {
        fun describe(label: String): String {
            val totalMs = totalNanos / 1_000_000.0
            val trialMs = trialNanos / 1_000_000.0
            val trialPct = if (totalNanos > 0) (trialNanos * 100.0 / totalNanos) else 0.0
            return "  $label: total=%.3fms trial-measure=%.3fms (%.1f%%, %d passes) primitives=%d ctor=%.3fms"
                .format(totalMs, trialMs, trialPct, trialCount, primitiveCount, ctorNanos / 1_000_000.0)
        }
    }

    /**
     * Runs the real shell across several frames on the *same* [UiContext] (matching Awake's
     * immediate-mode "rebuild the whole tree every frame" model -- see the task's hypothesis)
     * and reports the steady-state (JIT-warmed) cost, not the first-frame cost, which is
     * dominated by classloading/JIT and would overstate the ongoing per-frame cost this
     * investigation actually cares about. [warmupFrames] frames run untimed first.
     */
    private fun measureOneFrame(
        pageId: String,
        warmupFrames: Int = 5,
        measuredFrames: Int = 5
    ): FrameCost {
        val previewScale = 1
        val previousScale = UiDensity.scale
        val previousFontScale = UiDensity.fontScale
        UiDensity.scale = previewScale.toFloat()
        UiDensity.fontScale = 1f
        try {
            val state = UiShowcaseRuntimeState()
            val theme = state.showcaseTheme()
            val ui = UiContext()
            val input = Input()
            input.setPointer(down = false, x = -100f, y = -100f)

            var totalNanos = 0L
            var trialNanos = 0L
            var trialCount = 0
            var primitiveCount = 0
            var ctorNanos = 0L

            repeat(warmupFrames + measuredFrames) { frameIndex ->
                val measured = frameIndex >= warmupFrames
                ui.beginFrame(
                    1440f,
                    900f,
                    input.updateSnapshot().toUiInputState()
                )
                ui.pushTheme(theme)
                // Seed the same "ui-showcase-page" state key drawUiShowcaseSidebar/
                // drawUiShowcasePageContent read, so this frame renders pageId's real content
                // instead of always the default "introduction" -- same stateStore-sharing
                // approach UiContextMeasureState.createMeasureContext relies on for trial passes.
                ui.rememberStateValue<String>("ui-showcase-page", "entry") { pageId }.value = pageId

                if (measured) {
                    UiMeasureTrialStats.reset()
                    UiMeasureTrialStats.enabled = true
                }
                val start = TimeSource.Monotonic.markNow()
                drawRealShowcaseShell(ui, state)
                val primitives = ui.endFrame()
                if (measured) {
                    totalNanos += start.elapsedNow().inWholeNanoseconds
                    trialNanos += UiMeasureTrialStats.trialNanos
                    trialCount += UiMeasureTrialStats.trialCount
                    primitiveCount = primitives.size
                    ctorNanos += UiMeasureTrialStats.contextCtorNanos
                    UiMeasureTrialStats.enabled = false
                    UiMeasureTrialStats.reset()
                }
            }

            return FrameCost(
                totalNanos = totalNanos / measuredFrames,
                trialNanos = trialNanos / measuredFrames,
                trialCount = trialCount / measuredFrames,
                primitiveCount = primitiveCount,
                ctorNanos = ctorNanos / measuredFrames
            )
        } finally {
            UiDensity.scale = previousScale
            UiDensity.fontScale = previousFontScale
        }
    }

    /**
     * Mirrors UiShowcaseUi.kt's `drawUiShowcaseOverlay` non-compact ("desktop") branch exactly --
     * the real persistent sidebar + full page content tree the "laggy on web" report is about --
     * including the real `BoxScope` receiver `frame { ... }` production actually provides (via
     * `ui.createBox(...)` here instead of a full `GameUiRuntime`/`frame()` round-trip, which just
     * adds a viewport-constraints callback around the same content) -- this matters because
     * `row()`/`column()` resolve to different overloads (`BoxScope.row()` vs. the root
     * `UiContext.row()`) depending on receiver, and only the `BoxScope` one runs the
     * hasWeightedChild trial this cache targets. [cacheShellRow] mirrors the real
     * `id = "ui-showcase-shell-row", cacheKey = "static"` migration landed in production, so this
     * function can measure the real before/after trial-count delta (see
     * [measureShellRowCacheImpact]) without needing to physically revert production code.
     */
    private fun drawRealShowcaseShell(ui: UiContext, state: UiShowcaseRuntimeState, cacheShellRow: Boolean = true) {
        val sidebarScroll = ui.rememberScrollState("ui-showcase-scroll-side")
        val contentScroll = ui.rememberScrollState("ui-showcase-scroll-content")
        val outerPadding = 24f.dp
        val sidebarWidth = 264f.dp.toDimension()
        val railGap = 20f.dp

        ui.createBox(x = 0f, y = 0f, width = 1440f, height = 900f).row(
            id = if (cacheShellRow) "ui-showcase-shell-row" else null,
            cacheKey = if (cacheShellRow) "static" else null,
            horizontalArrangement = Arrangement.spacedBy(railGap),
            modifier = (Modifier.fillMaxSize().padding(outerPadding)).width(Dimension.FillMax).height(Dimension.FillMax)
        ) {
            shadcnSidebar(
                id = "ui-showcase-sidebar",
                style = Style { shape(16f.dp) },
                modifier = (Modifier.verticalScroll(sidebarScroll, UiScrollConfig.Hidden)).width(sidebarWidth).height(Dimension.FillMax)
            ) {
                drawUiShowcaseSidebar(compact = false)
            }

            column(
                id = "ui-showcase-content-viewport",
                modifier = (Modifier.verticalScroll(contentScroll)).width(Dimension.FillMax).height(Dimension.FillMax)
            ) {
                shadcnSurface(
                    id = "ui-showcase-content",
                    style = Style { shape(16f.dp) },
                    modifier = Modifier.height(Dimension.WrapContent)
                ) {
                    drawUiShowcasePageContent(state, showInlineMenu = false)
                }
            }
        }
    }
}
