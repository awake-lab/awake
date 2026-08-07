// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.application

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.input.Key
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val GLFW_KEY_W = 87
private const val GLFW_KEY_SPACE = 32

/** [Input] is a per-session instance now (no longer a global object) -- each test
 * constructs its own, so there's no shared state to reset between tests. */
class GlfwInputBridgeTest {

    @Test
    fun heldGameplayKeyReachesInput() {
        val input = Input()
        val reader = FakeGlfwWindowInput().apply { keysDown += GLFW_KEY_W }
        pollGlfwInput(reader, input)
        assertTrue(input.isKeyDown(Key.W), "a held GLFW key must translate to Input.isKeyDown")

        reader.keysDown -= GLFW_KEY_W
        pollGlfwInput(reader, input)
        assertFalse(input.isKeyDown(Key.W), "releasing the GLFW key must clear Input.isKeyDown next poll")
    }

    @Test
    fun pointerScalesByFramebufferRatio() {
        val input = Input()
        val reader = FakeGlfwWindowInput().apply {
            mouseDown = true
            cursorXValue = 100.0
            cursorYValue = 50.0
            scaleX = 2f
            scaleY = 2f
        }
        pollGlfwInput(reader, input)
        assertTrue(input.pointerDown)
        assertEquals(200f, input.pointerX, "HiDPI framebuffer scale must be applied to cursor x")
        assertEquals(100f, input.pointerY, "HiDPI framebuffer scale must be applied to cursor y")
    }

    @Test
    fun rightButtonDrivesTheSecondaryPointerNotThePrimaryOne() {
        val input = Input()
        val reader = FakeGlfwWindowInput().apply { secondaryMouseDown = true }
        pollGlfwInput(reader, input)
        assertTrue(
            input.updateSnapshot().secondaryPointerDown,
            "GLFW button 1 must reach Input.secondaryPointerDown -- shadcnContextMenu gates on it",
        )
        assertFalse(input.pointerDown, "a right-click must not also register as a left click")
    }

    @Test
    fun scrollDeltaPassesThroughUnconsumed() {
        val input = Input()
        val reader = FakeGlfwWindowInput().apply { pendingScrollDeltaY = -3.5 }
        pollGlfwInput(reader, input)
        assertEquals(-3.5f, input.scrollDeltaY, "a scroll tick must reach Input.scrollDeltaY for the UI/camera layer to consume")
    }

    @Test
    fun unmappedKeyIsIgnored() {
        val input = Input()
        val reader = FakeGlfwWindowInput().apply { keysDown += GLFW_KEY_SPACE }
        pollGlfwInput(reader, input, keys = mapOf(GLFW_KEY_W to Key.W))
        assertFalse(input.isKeyDown(Key.Space), "a key not present in the keys map must not set any Input key")
    }
}
