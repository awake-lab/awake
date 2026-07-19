// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.application

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.input.Key
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val GLFW_KEY_W = 87
private const val GLFW_KEY_SPACE = 32

class GlfwInputBridgeTest {

    @AfterTest
    fun resetInput() {
        Input.clearKeys()
        Input.setPointer(down = false, x = 0f, y = 0f)
        Input.scrollDeltaY = 0f
    }

    @Test
    fun heldGameplayKeyReachesInput() {
        val reader = FakeGlfwWindowInput().apply { keysDown += GLFW_KEY_W }
        pollGlfwInput(reader)
        assertTrue(Input.isKeyDown(Key.W), "a held GLFW key must translate to Input.isKeyDown")

        reader.keysDown -= GLFW_KEY_W
        pollGlfwInput(reader)
        assertFalse(Input.isKeyDown(Key.W), "releasing the GLFW key must clear Input.isKeyDown next poll")
    }

    @Test
    fun pointerScalesByFramebufferRatio() {
        val reader = FakeGlfwWindowInput().apply {
            mouseDown = true
            cursorXValue = 100.0
            cursorYValue = 50.0
            scaleX = 2f
            scaleY = 2f
        }
        pollGlfwInput(reader)
        assertTrue(Input.pointerDown)
        assertEquals(200f, Input.pointerX, "HiDPI framebuffer scale must be applied to cursor x")
        assertEquals(100f, Input.pointerY, "HiDPI framebuffer scale must be applied to cursor y")
    }

    @Test
    fun scrollDeltaPassesThroughUnconsumed() {
        val reader = FakeGlfwWindowInput().apply { pendingScrollDeltaY = -3.5 }
        pollGlfwInput(reader)
        assertEquals(-3.5f, Input.scrollDeltaY, "a scroll tick must reach Input.scrollDeltaY for the UI/camera layer to consume")
    }

    @Test
    fun unmappedKeyIsIgnored() {
        val reader = FakeGlfwWindowInput().apply { keysDown += GLFW_KEY_SPACE }
        pollGlfwInput(reader, keys = mapOf(GLFW_KEY_W to Key.W))
        assertFalse(Input.isKeyDown(Key.Space), "a key not present in the keys map must not set any Input key")
    }
}
