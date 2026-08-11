// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCollapsible
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebar
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.toUiInputState
import java.io.File
import kotlin.test.Test

/**
 * Real-render re-investigation of the "collapse animation is still snapping" live report,
 * despite [io.github.ronjunevaldoz.awake.ui.AnimatedHeightCollapseProbeTest] (ui-core) and
 * `ShadcnCollapsibleCollapseAnimationProbeTest` (samples/ui-showcase) both already proving the
 * LOGICAL height sequence is strictly monotonic with no jump. Neither of those probes ever asked
 * a renderer to actually draw anything -- both only read `UiBounds` off the real `UiContext`.
 * This drives the same real `shadcnCollapsible`-in-`shadcnSidebar` shape through the real
 * headless Vulkan renderer (via [UiAnimationFrameCapture]) and dumps the actual rendered PNG
 * sequence for visual inspection -- the one layer of proof those probes structurally cannot
 * provide (frame pacing / dirty-rect lag between computed clip bounds and what gets presented).
 *
 * Not a pass/fail assertion test (there's no automated "does this look snapped" check yet, see
 * docs/reference/ui-validation.md's Canonical Test Surfaces entry for this tool) -- its job is
 * to produce the PNG sequence a human (or this agent) inspects frame-by-frame. Output lands in
 * `build/ui-animation-capture/shadcn-collapsible-collapse/`.
 */
class ShadcnCollapsibleRealRenderCollapseFrameCaptureTest {

    @Test
    fun capturesRealRenderedCollapseSequence() {
        val font = UiFonts.default(cellSize = 12)
        val ui = UiContext()
        val input = Input()
        input.setPointer(down = false, x = -100f, y = -100f)
        var expanded = true

        fun frame(): List<UiDrawPrimitive> {
            ui.beginFrame(480f, 800f, input.updateSnapshot().toUiInputState())
            ui.pushFont(font)
            ui.pushTheme(ShadcnTheme)
            ui.createBox(x = 0f, y = 0f, width = 480f, height = 800f).shadcnSidebar(
                id = "capture-sidebar",
                modifier = Modifier.width(280f.dp).height(Dimension.FillMax),
            ) {
                shadcnCollapsible(
                    id = "capture-category-getting-started",
                    title = "Getting Started",
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                ) {
                    repeat(4) { index ->
                        shadcnButton(
                            id = "capture-item-$index",
                            label = "Item $index",
                            modifier = Modifier.fillMaxWidth().height(32f.dp),
                        ) {}
                    }
                }
                shadcnButton(
                    id = "capture-inputs-header",
                    label = "Inputs",
                    modifier = Modifier.fillMaxWidth().height(32f.dp),
                ) {}
            }
            return ui.endFrame()
        }

        val outputDir = File("build/ui-animation-capture/shadcn-collapsible-collapse")
        UiAnimationFrameCapture.create(480, 800, outputDir).use { capture ->
            // Let the expand animation fully settle before collapsing, same two-phase shape
            // AnimatedHeightCollapseProbeTest/ShadcnCollapsibleCollapseAnimationProbeTest use.
            repeat(20) { frame() }

            expanded = false
            repeat(FRAME_COUNT) { index ->
                val primitives = frame()
                capture.captureFrame(index, primitives, font)
            }
        }

        println(
            "Real rendered collapse sequence written to " +
                "${File(outputDir, "frame-000.png").absolutePath.substringBeforeLast(File.separator)} " +
                "($FRAME_COUNT frames) -- inspect for a visible jump/snap between consecutive frames.",
        )
    }

    private companion object {
        const val FRAME_COUNT = 20
    }
}
