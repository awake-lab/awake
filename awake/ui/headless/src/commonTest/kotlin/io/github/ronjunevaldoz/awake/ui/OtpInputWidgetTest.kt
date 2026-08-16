// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.testing.ui.renderUiComponent
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
        val frame = renderUiComponent(width = 200f, height = 100f) {
            otpInput(
                id = "otp.test",
                value = "12",
                length = 4,
            ) { index, char -> text(char, semanticId = "otp.test.slot.$index") }
        }

        assertEquals("1", frame.semantics.first { it.id == "otp.test.slot.0" }.label)
        assertEquals("2", frame.semantics.first { it.id == "otp.test.slot.1" }.label)
        assertEquals("", frame.semantics.first { it.id == "otp.test.slot.2" }.label)
        assertEquals("", frame.semantics.first { it.id == "otp.test.slot.3" }.label)
    }

    @Test
    fun separatorFiresOnlyAtGroupBoundariesNotAtIndexZero() {
        val frame = renderUiComponent(width = 200f, height = 100f) {
            otpInput(
                id = "otp.grouped",
                value = "123456",
                length = 6,
                groupSize = 3,
                separator = { beforeIndex ->
                    text("-", semanticId = "otp.grouped.separator.$beforeIndex")
                },
            ) { _, _ -> }
        }

        // 6 slots, groupSize 3 -> exactly one boundary, before index 3. Index 0 must never
        // separate -- deduped by id, so the trial-measure pass can't inflate this count.
        assertEquals(1, frame.semantics.count { it.id?.startsWith("otp.grouped.separator.") == true })
    }

    @Test
    fun noSeparatorLambdaMeansNoSeparatorRendered() {
        val frame = renderUiComponent(width = 200f, height = 100f) {
            otpInput(
                id = "otp.nogroup",
                value = "123456",
                length = 6,
                groupSize = 3,
            ) { _, _ -> }
        }
        // No separator lambda supplied -- must not throw, must not render anything extra.
        assertEquals(true, frame.semantics.none { it.id == "otp.nogroup.separator" })
    }

    @Test
    fun untypedValuePassesThroughUnchanged() {
        var resolved: String? = null
        renderUiComponent(width = 200f, height = 100f) {
            resolved = otpInput(
                id = "otp.passthrough",
                value = "42",
                length = 4,
            ) { _, _ -> }
        }

        assertEquals("42", resolved, "with no simulated keystroke, the hidden field echoes the caller's value back unchanged")
    }
}
