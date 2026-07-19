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

class GlfwTextInputBridgeTest {

    @BeforeTest
    fun reset() {
        resetGlfwTextInputRepeatStateForTest()
        Input.consumeTypedText()
        Input.consumeEditActions()
    }

    @AfterTest
    fun cleanup() {
        resetGlfwTextInputRepeatStateForTest()
    }

    @Test
    fun keyPressFiresExactlyOnceOnTheRisingEdge() {
        val reader = FakeGlfwWindowInput().apply { keysDown += GLFW_KEY_A }
        pollGlfwTextInput(reader, deltaSeconds = 0.0)
        assertEquals("a", Input.consumeTypedText(), "the first frame a key is down must insert exactly one character")

        pollGlfwTextInput(reader, deltaSeconds = 0.05)
        assertEquals("", Input.consumeTypedText(), "holding the key without crossing the repeat threshold must not insert again")
    }

    @Test
    fun holdingPastTheInitialDelayRepeats() {
        val reader = FakeGlfwWindowInput().apply { keysDown += GLFW_KEY_A }
        pollGlfwTextInput(reader, deltaSeconds = 0.0)
        Input.consumeTypedText()

        pollGlfwTextInput(reader, deltaSeconds = REPEAT_INITIAL_DELAY_SECONDS)
        assertEquals("a", Input.consumeTypedText(), "holding past the initial repeat delay must fire another insert")
    }

    @Test
    fun releasingResetsTheRepeatCadence() {
        val reader = FakeGlfwWindowInput().apply { keysDown += GLFW_KEY_A }
        pollGlfwTextInput(reader, deltaSeconds = 0.0)
        Input.consumeTypedText()

        reader.keysDown -= GLFW_KEY_A
        pollGlfwTextInput(reader, deltaSeconds = 1.0)
        assertEquals("", Input.consumeTypedText(), "releasing the key must stop insertion even after a long delta")

        reader.keysDown += GLFW_KEY_A
        pollGlfwTextInput(reader, deltaSeconds = 0.0)
        assertEquals("a", Input.consumeTypedText(), "pressing again after a release must fire a fresh rising edge, not resume mid-repeat")
    }

    @Test
    fun shiftUppercasesLetters() {
        val reader = FakeGlfwWindowInput().apply {
            keysDown += GLFW_KEY_A
            keysDown += GLFW_KEY_LEFT_SHIFT
        }
        pollGlfwTextInput(reader, deltaSeconds = 0.0)
        assertEquals("A", Input.consumeTypedText(), "shift held must uppercase the inserted letter")
    }

    @Test
    fun editKeyPushesEditAction() {
        val reader = FakeGlfwWindowInput().apply { keysDown += GLFW_KEY_BACKSPACE }
        pollGlfwTextInput(reader, deltaSeconds = 0.0)
        assertEquals(listOf(TextEditAction.Backspace), Input.consumeEditActions(), "a held edit key must push its TextEditAction on the rising edge")
    }
}
