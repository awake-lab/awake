// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.application

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.input.TextEditAction
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

private const val GLFW_KEY_A = 65
private const val GLFW_KEY_LEFT_SHIFT = 340
private const val GLFW_KEY_BACKSPACE = 259

// Must match GlfwTextInputBridge.kt's private REPEAT_INITIAL_DELAY_SECONDS/REPEAT_INTERVAL_SECONDS --
// there's no seam to read those from a test, so this asserts the observable cadence, not the constants.
private const val REPEAT_INITIAL_DELAY_SECONDS = 0.5
private const val REPEAT_INTERVAL_SECONDS = 0.05

/** [Input] is a per-session instance now (no longer a global object) -- each test
 * constructs its own and reads it back via [Input.updateSnapshot], which drains the typed
 * text/edit action buffers the same way the old static `consumeTypedText`/`consumeEditActions`
 * did. */
class GlfwTextInputBridgeTest {

    @BeforeTest
    fun reset() {
        resetGlfwTextInputRepeatStateForTest()
    }

    @AfterTest
    fun cleanup() {
        resetGlfwTextInputRepeatStateForTest()
    }

    @Test
    fun keyPressFiresExactlyOnceOnTheRisingEdge() {
        val input = Input()
        val reader = FakeGlfwWindowInput().apply { keysDown += GLFW_KEY_A }
        pollGlfwTextInput(reader, input, deltaSeconds = 0.0)
        assertEquals("a", input.updateSnapshot().typedText, "the first frame a key is down must insert exactly one character")

        pollGlfwTextInput(reader, input, deltaSeconds = 0.05)
        assertEquals("", input.updateSnapshot().typedText, "holding the key without crossing the repeat threshold must not insert again")
    }

    @Test
    fun holdingPastTheInitialDelayRepeats() {
        val input = Input()
        val reader = FakeGlfwWindowInput().apply { keysDown += GLFW_KEY_A }
        pollGlfwTextInput(reader, input, deltaSeconds = 0.0)
        input.updateSnapshot()

        pollGlfwTextInput(reader, input, deltaSeconds = REPEAT_INITIAL_DELAY_SECONDS)
        assertEquals("a", input.updateSnapshot().typedText, "holding past the initial repeat delay must fire another insert")
    }

    @Test
    fun releasingResetsTheRepeatCadence() {
        val input = Input()
        val reader = FakeGlfwWindowInput().apply { keysDown += GLFW_KEY_A }
        pollGlfwTextInput(reader, input, deltaSeconds = 0.0)
        input.updateSnapshot()

        reader.keysDown -= GLFW_KEY_A
        pollGlfwTextInput(reader, input, deltaSeconds = 1.0)
        assertEquals("", input.updateSnapshot().typedText, "releasing the key must stop insertion even after a long delta")

        reader.keysDown += GLFW_KEY_A
        pollGlfwTextInput(reader, input, deltaSeconds = 0.0)
        assertEquals("a", input.updateSnapshot().typedText, "pressing again after a release must fire a fresh rising edge, not resume mid-repeat")
    }

    @Test
    fun shiftUppercasesLetters() {
        val input = Input()
        val reader = FakeGlfwWindowInput().apply {
            keysDown += GLFW_KEY_A
            keysDown += GLFW_KEY_LEFT_SHIFT
        }
        pollGlfwTextInput(reader, input, deltaSeconds = 0.0)
        assertEquals("A", input.updateSnapshot().typedText, "shift held must uppercase the inserted letter")
    }

    @Test
    fun editKeyPushesEditAction() {
        val input = Input()
        val reader = FakeGlfwWindowInput().apply { keysDown += GLFW_KEY_BACKSPACE }
        pollGlfwTextInput(reader, input, deltaSeconds = 0.0)
        assertEquals(listOf(TextEditAction.Backspace), input.updateSnapshot().editActions, "a held edit key must push its TextEditAction on the rising edge")
    }
}
