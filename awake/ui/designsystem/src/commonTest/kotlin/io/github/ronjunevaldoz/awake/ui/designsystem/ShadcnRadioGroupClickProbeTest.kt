// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.testing.ui.UiComponentFrame
import io.github.ronjunevaldoz.awake.testing.ui.UiTestSession
import io.github.ronjunevaldoz.awake.testing.ui.uiTestSession
import io.github.ronjunevaldoz.awake.ui.api.dp
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

    private fun buildFrame(
        session: UiTestSession,
        x: Float,
        y: Float,
        down: Boolean,
        scrolled: Boolean,
    ): UiComponentFrame {
        val frame = session.frame(x = x, y = y, down = down) {
            if (scrolled) {
                val scroll = rememberScrollState("radio-probe-scroll")
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
        val radio2 = frame.boundsOrNull("radio-probe.2")
        trace += "  post: radio2=$radio2 pointer=($x,$y)"
        return frame
    }

    private fun clickOption(
        session: UiTestSession,
        previous: UiComponentFrame,
        index: Int,
        scrolled: Boolean,
    ): UiComponentFrame {
        val target = previous.semantics
            .first { it.role == UiSemanticRole.Radio && it.id == "radio-probe.$index" }
            .bounds
        val px = target.x + target.width / 2f
        val py = target.y + target.height / 2f
        buildFrame(session, px, py, down = true, scrolled = scrolled)
        return buildFrame(session, px, py, down = false, scrolled = scrolled)
    }

    private fun runScenario(scrolled: Boolean) {
        lastSelection = 0
        uiTestSession(
            width = 400f,
            height = 300f,
            font = BitmapFont(),
            rootProvider = { content -> shadcnTheme { content() } },
        ) {
            var frame = buildFrame(this, x = -100f, y = -100f, down = false, scrolled = scrolled)
            frame = clickOption(this, frame, index = 2, scrolled = scrolled)
            assertEquals(2, lastSelection, "clicking option 2 (scrolled=$scrolled)\n" + trace.joinToString("\n"))

            clickOption(this, frame, index = 1, scrolled = scrolled)
            assertEquals(1, lastSelection, "clicking option 1 after 2 (scrolled=$scrolled)")
        }
    }

    @Test
    fun bareRadioGroupSelectsTheClickedOption() = runScenario(scrolled = false)

    /** The showcase page's exact state pattern: selection lives in rememberStateValue, and the
     * group sits in a scrolled column like the real content pane. */
    @Test
    fun radioGroupBackedByRememberedStateSelectsTheClickedOption() {
        uiTestSession(
            width = 400f,
            height = 300f,
            font = BitmapFont(),
            rootProvider = { content -> shadcnTheme { content() } },
        ) {
            fun frame(x: Float, y: Float, down: Boolean): Pair<Int, UiComponentFrame> {
            var result = -1
                val output = frame(x = x, y = y, down = down) {
                    val scroll = rememberScrollState("radio-state-scroll")
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
                return result to output
            }
            val initial = frame(-100f, -100f, down = false).second
            val target = initial.node("radio-state-probe.2").bounds
            val px = target.x + target.width / 2f
            val py = target.y + target.height / 2f
            frame(px, py, down = true)
            val after = frame(px, py, down = false).first
            assertEquals(2, after, "remembered-state radio selection after clicking option 2")
        }
    }

    @Test
    fun radioGroupInsideAScrollContainerSelectsTheClickedOption() = runScenario(scrolled = true)
}
