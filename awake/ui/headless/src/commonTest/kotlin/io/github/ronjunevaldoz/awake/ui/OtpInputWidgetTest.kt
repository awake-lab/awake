// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.headless.createUiScope
import io.github.ronjunevaldoz.awake.ui.headless.otpInput
import io.github.ronjunevaldoz.awake.ui.headless.text
import kotlin.test.Test
import kotlin.test.assertEquals

class OtpInputWidgetTest {
    // row() runs its content lambda twice per frame (trial-measure + real layout, see Row.kt) --
    // asserting through recordSemantic-backed nodes (deduped by id) rather than a raw closure
    // call counter sidesteps that, matching how ActionRowWidgetsTest checks semantics too.
    @Test
    fun eachSlotReceivesItsOwnCharacterFromValue() {
        val ui = UiContext()
        ui.beginFrame(200f, 100f, testSnapshot())
        ui.createUiScope(UiBounds(0f, 0f, 200f, 100f)).otpInput(
            id = "otp.test",
            value = "12",
            length = 4,
        ) { index, char -> text(char, semanticId = "otp.test.slot.$index") }
        val frame = ui.finishFrame()

        assertEquals("1", frame.semantics.first { it.id == "otp.test.slot.0" }.label)
        assertEquals("2", frame.semantics.first { it.id == "otp.test.slot.1" }.label)
        assertEquals("", frame.semantics.first { it.id == "otp.test.slot.2" }.label)
        assertEquals("", frame.semantics.first { it.id == "otp.test.slot.3" }.label)
    }

    @Test
    fun separatorFiresOnlyAtGroupBoundariesNotAtIndexZero() {
        val ui = UiContext()
        ui.beginFrame(200f, 100f, testSnapshot())
        var separatorSlotIndex = 0
        ui.createUiScope(UiBounds(0f, 0f, 200f, 100f)).otpInput(
            id = "otp.grouped",
            value = "123456",
            length = 6,
            groupSize = 3,
            separator = {
                text("-", semanticId = "otp.grouped.separator.${separatorSlotIndex++}")
            },
        ) { _, _ -> }
        val frame = ui.finishFrame()

        // 6 slots, groupSize 3 -> exactly one boundary, before index 3. Index 0 must never
        // separate -- deduped by id, so the trial-measure pass can't inflate this count.
        assertEquals(1, frame.semantics.count { it.id?.startsWith("otp.grouped.separator.") == true })
    }

    @Test
    fun noSeparatorLambdaMeansNoSeparatorRendered() {
        val ui = UiContext()
        ui.beginFrame(200f, 100f, testSnapshot())
        ui.createUiScope(UiBounds(0f, 0f, 200f, 100f)).otpInput(
            id = "otp.nogroup",
            value = "123456",
            length = 6,
            groupSize = 3,
        ) { _, _ -> }
        // No separator lambda supplied -- must not throw, must not render anything extra.
        val frame = ui.finishFrame()
        assertEquals(true, frame.semantics.none { it.id == "otp.nogroup.separator" })
    }

    @Test
    fun untypedValuePassesThroughUnchanged() {
        val ui = UiContext()
        ui.beginFrame(200f, 100f, testSnapshot())
        val resolved = ui.createUiScope(UiBounds(0f, 0f, 200f, 100f)).otpInput(
            id = "otp.passthrough",
            value = "42",
            length = 4,
        ) { _, _ -> }
        ui.finishFrame()

        assertEquals("42", resolved, "with no simulated keystroke, the hidden field echoes the caller's value back unchanged")
    }
}
