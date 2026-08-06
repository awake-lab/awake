// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.context.UiFrameInput
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnInputOTP
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.layouts.column
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShadcnInputOTPTest {

    // ──────────────────────────────────────────────────────────
    // Helper: build a fresh UiContext with a default BitmapFont
    // ──────────────────────────────────────────────────────────
    private fun uiContext() = UiContext().also { it.pushFont(BitmapFont()) }

    // ──────────────────────────────────────────────────────────
    // Slot count
    // ──────────────────────────────────────────────────────────

    @Test
    fun shadcnInputOTPRendersCorrectSlotCount() {
        val ui = uiContext()

        ui.beginFrame(UiFrameInput(viewportWidth = 300f, viewportHeight = 160f, input = UiInputState()))
        var value = "12"
        ui.column { value = shadcnInputOTP(id = "otp-1", value = value, length = 6) }
        val output = ui.finishFrame()

        assertEquals("12", value, "value must be unchanged when no input event")

        val slotPanels = output.semantics.filter { (it.id ?: "").startsWith("otp-1.slot.") }
        assertEquals(6, slotPanels.size, "Expected 6 OTP slot panels in semantic tree")
    }

    // ──────────────────────────────────────────────────────────
    // Digit centering — the core regression guard
    // ──────────────────────────────────────────────────────────

    /**
     * Verifies that the rendered glyph for digit "4" (placed in slot 0) is
     * both horizontally and vertically centred within the slot panel bounds.
     *
     * Before the fix this test would have caught the bug where text(modifier=...)
     * claimed a wrap-content slot instead of the parent panel's full 36×40 dp bounds.
     */
    @Test
    fun shadcnInputOTPDigitIsCenteredInSlot() {
        val ui = uiContext()

        // Use a wide viewport so slots are not constrained, and a pre-filled digit
        ui.beginFrame(UiFrameInput(viewportWidth = 400f, viewportHeight = 80f, input = UiInputState()))
        ui.column { shadcnInputOTP(id = "otp", value = "4", length = 6) }
        val output = ui.finishFrame()

        // 1. Locate the slot-0 Panel node
        val slot0 = output.semantics.firstOrNull { it.id == "otp.slot.0" }
        assertTrue(slot0 != null, "semantic node 'otp.slot.0' must exist")

        // 2. Locate the Text node whose label is "4"
        val digitNode = output.semantics.firstOrNull {
            it.role == UiSemanticRole.Text && it.label == "4"
        }
        assertTrue(digitNode != null, "semantic Text node with label '4' must exist")

        val contentBounds = digitNode!!.contentBounds
        assertTrue(contentBounds != null, "digit Text node must have contentBounds set by renderTextBlock")

        // 3. Assert centering within 1 px tolerance
        val tolerancePx = 1f

        val slotCX = slot0!!.bounds.x + slot0.bounds.width / 2f
        val slotCY = slot0.bounds.y + slot0.bounds.height / 2f
        val contentCX = contentBounds!!.x + contentBounds.width / 2f
        val contentCY = contentBounds.y + contentBounds.height / 2f

        assertTrue(
            abs(contentCX - slotCX) <= tolerancePx,
            "digit '4' is NOT horizontally centered in slot 0: " +
                "contentCenterX=$contentCX, slotCenterX=$slotCX, " +
                "deviation=${abs(contentCX - slotCX)}, tolerance=$tolerancePx"
        )
        assertTrue(
            abs(contentCY - slotCY) <= tolerancePx,
            "digit '4' is NOT vertically centered in slot 0: " +
                "contentCenterY=$contentCY, slotCenterY=$slotCY, " +
                "deviation=${abs(contentCY - slotCY)}, tolerance=$tolerancePx"
        )
    }

    // ──────────────────────────────────────────────────────────
    // All filled slots have centered digits (regression sweep)
    // ──────────────────────────────────────────────────────────

    @Test
    fun shadcnInputOTPAllFilledDigitsAreCenteredInTheirSlots() {
        val ui = uiContext()
        val digits = "482019"

        ui.beginFrame(UiFrameInput(viewportWidth = 400f, viewportHeight = 80f, input = UiInputState()))
        ui.column { shadcnInputOTP(id = "otp", value = digits, length = 6) }
        val output = ui.finishFrame()

        val tolerancePx = 1f
        val violations = mutableListOf<String>()

        digits.forEachIndexed { i, char ->
            val slotNode = output.semantics.firstOrNull { it.id == "otp.slot.$i" } ?: run {
                violations += "slot $i panel node missing"
                return@forEachIndexed
            }
            val digitNode = output.semantics
                .filter { it.role == UiSemanticRole.Text && it.label == char.toString() }
                .getOrNull(0) ?: run {
                violations += "slot $i: no Text node with label '$char'"
                return@forEachIndexed
            }
            val content = digitNode.contentBounds ?: run {
                violations += "slot $i: Text node for '$char' missing contentBounds"
                return@forEachIndexed
            }

            val slotCX = slotNode.bounds.x + slotNode.bounds.width / 2f
            val slotCY = slotNode.bounds.y + slotNode.bounds.height / 2f
            val contentCX = content.x + content.width / 2f
            val contentCY = content.y + content.height / 2f

            if (abs(contentCX - slotCX) > tolerancePx) {
                violations += "slot $i ('$char') H-off-center: contentCX=$contentCX slotCX=$slotCX"
            }
            if (abs(contentCY - slotCY) > tolerancePx) {
                violations += "slot $i ('$char') V-off-center: contentCY=$contentCY slotCY=$slotCY"
            }
        }

        assertTrue(violations.isEmpty(), "Centering violations:\n${violations.joinToString("\n")}")
    }
}
