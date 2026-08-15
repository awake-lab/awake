// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.createUiScope
import io.github.ronjunevaldoz.awake.ui.headless.otpInput
import io.github.ronjunevaldoz.awake.ui.headless.text
import io.github.ronjunevaldoz.awake.ui.headless.width
import kotlin.test.Test
import kotlin.test.assertEquals

class OtpInputWidgetTest {
    @Test
    fun eachSlotReceivesItsOwnCharacterFromValue() {
        val ui = UiContext()
        ui.beginFrame(200f, 100f, testSnapshot())
        val seen = mutableListOf<Pair<Int, String>>()
        ui.createUiScope(UiBounds(0f, 0f, 200f, 100f)).otpInput(
            id = "otp.test",
            value = "12",
            length = 4,
        ) { index, char -> seen.add(index to char) }
        ui.finishFrame()

        assertEquals(listOf(0 to "1", 1 to "2", 2 to "", 3 to ""), seen)
    }

    @Test
    fun separatorFiresOnlyAtGroupBoundariesNotAtIndexZero() {
        val ui = UiContext()
        ui.beginFrame(200f, 100f, testSnapshot())
        var separatorCount = 0
        ui.createUiScope(UiBounds(0f, 0f, 200f, 100f)).otpInput(
            id = "otp.grouped",
            value = "123456",
            length = 6,
            groupSize = 3,
            separator = { separatorCount++; text("-") },
        ) { _, _ -> }
        ui.finishFrame()

        // 6 slots, groupSize 3 -> one boundary, before index 3. Index 0 must never separate.
        assertEquals(1, separatorCount)
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
