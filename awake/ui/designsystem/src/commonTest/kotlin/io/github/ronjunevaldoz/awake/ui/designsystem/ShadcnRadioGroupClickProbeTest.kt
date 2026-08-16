// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnRadioGroup
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.rememberScrollState
import io.github.ronjunevaldoz.awake.ui.headless.rememberStateValue
import io.github.ronjunevaldoz.awake.ui.headless.uiScope
import io.github.ronjunevaldoz.awake.ui.headless.verticalScroll
import io.github.ronjunevaldoz.awake.ui.headless.width
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Live report: "Radio button is not clickable in a group". Probes the list-based
 * [shadcnRadioGroup] -- the exact overload the showcase page uses -- with real pointer frames
 * (settle, press, release) targeting bounds read from the frame's own semantics, the same
 * harness shape as [ShadcnButtonScrollClickInteractionTest].
 *
 * Two environments: bare, and inside a `verticalScroll` container like the showcase's content
 * pane. The sidebar-footer bug taught that component-level green with page-level breakage means
 * the environment is the variable, so both are pinned here.
 */
class ShadcnRadioGroupClickProbeTest {

    private var lastSelection: Int = -1
    private val trace = mutableListOf<String>()

    private fun buildFrame(ui: UiContext, x: Float, y: Float, down: Boolean, scrolled: Boolean) {
        ui.beginFrame(400f, 300f, testSnapshot(x = x, y = y, down = down))
        ui.pushFont(BitmapFont())
        val root = ui.headlessRoot()
        root.shadcnTheme {
            if (scrolled) {
                val scroll = root.rememberScrollState("radio-probe-scroll")
                column(
                    modifier = Modifier.verticalScroll(scroll).width(360f.dp).height(280f.dp),
                ) {
                    lastSelection = uiScope().shadcnRadioGroup(
                        id = "radio-probe",
                        options = listOf("System", "Light", "Dark"),
                        selectedIndex = lastSelection.coerceAtLeast(0),
                        onIndexChange = { lastSelection = it },
                    )
                }
            } else {
                val fed = lastSelection.coerceAtLeast(0)
                val returned = shadcnRadioGroup(
                    id = "radio-probe",
                    options = listOf("System", "Light", "Dark"),
                    selectedIndex = fed,
                    onIndexChange = {
                        trace += "onIndexChange($it) [down=$down]"
                        lastSelection = it
                    },
                )
                trace += "frame(down=$down): fed=$fed returned=$returned lastSelection=$lastSelection"
                lastSelection = returned
            }
        }
        val radio2 = ui.semanticNodes().firstOrNull { it.id == "radio-probe.2" }?.bounds
        trace += "  post: radio2=$radio2 pointer=($x,$y)"
        ui.endFrame()
    }

    private fun clickOption(ui: UiContext, index: Int, scrolled: Boolean) {
        val target = ui.semanticNodes()
            .first { it.role == UiSemanticRole.Radio && it.id == "radio-probe.$index" }
            .bounds
        val px = target.x + target.width / 2f
        val py = target.y + target.height / 2f
        buildFrame(ui, px, py, down = true, scrolled = scrolled)
        buildFrame(ui, px, py, down = false, scrolled = scrolled)
    }

    private fun runScenario(scrolled: Boolean) {
        lastSelection = 0
        val ui = UiContext()
        buildFrame(ui, x = -100f, y = -100f, down = false, scrolled = scrolled) // settle mount

        clickOption(ui, index = 2, scrolled = scrolled)
        assertEquals(2, lastSelection, "clicking option 2 (scrolled=$scrolled)\n" + trace.joinToString("\n"))

        clickOption(ui, index = 1, scrolled = scrolled)
        assertEquals(1, lastSelection, "clicking option 1 after 2 (scrolled=$scrolled)")
    }

    @Test
    fun bareRadioGroupSelectsTheClickedOption() = runScenario(scrolled = false)

    /** The showcase page's exact state pattern: selection lives in rememberStateValue, and the
     * group sits in a scrolled column like the real content pane. */
    @Test
    fun radioGroupBackedByRememberedStateSelectsTheClickedOption() {
        val ui = UiContext()
        fun frame(x: Float, y: Float, down: Boolean): Int {
            ui.beginFrame(400f, 300f, testSnapshot(x = x, y = y, down = down))
            ui.pushFont(BitmapFont())
            val root = ui.headlessRoot()
            var result = -1
            root.shadcnTheme {
                val scroll = root.rememberScrollState("radio-state-scroll")
                column(
                    modifier = Modifier.verticalScroll(scroll).width(360f.dp).height(280f.dp),
                ) {
                    var selected by rememberStateValue("radio-state-probe", "selected") { 0 }
                    selected = uiScope().shadcnRadioGroup(
                        id = "radio-state-probe",
                        options = listOf("System", "Light", "Dark"),
                        selectedIndex = selected,
                        onIndexChange = { selected = it },
                    )
                    result = selected
                }
            }
            ui.endFrame()
            return result
        }
        frame(-100f, -100f, down = false)
        val target = ui.semanticNodes().first { it.id == "radio-state-probe.2" }.bounds
        val px = target.x + target.width / 2f
        val py = target.y + target.height / 2f
        frame(px, py, down = true)
        val after = frame(px, py, down = false)
        assertEquals(2, after, "remembered-state radio selection after clicking option 2")
    }

    @Test
    fun radioGroupInsideAScrollContainerSelectsTheClickedOption() = runScenario(scrolled = true)
}
