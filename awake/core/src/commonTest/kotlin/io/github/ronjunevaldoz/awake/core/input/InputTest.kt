// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.input

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** [Input] is a per-session instance now (no longer a global object) -- each test
 * constructs its own, so there's no shared state to reset between tests. */
class InputTest {
    @Test
    fun keyIsNotDownByDefault() {
        val input = Input()
        assertFalse(input.isKeyDown(Key.Space))
    }

    @Test
    fun settingAKeyDownIsObservable() {
        val input = Input()
        input.setKeyDown(Key.W, down = true)
        assertTrue(input.isKeyDown(Key.W))
    }

    @Test
    fun releasingAKeyClearsIt() {
        val input = Input()
        input.setKeyDown(Key.W, down = true)
        input.setKeyDown(Key.W, down = false)
        assertFalse(input.isKeyDown(Key.W))
    }

    @Test
    fun clearKeysReleasesEveryHeldKey() {
        val input = Input()
        input.setKeyDown(Key.W, down = true)
        input.setKeyDown(Key.Space, down = true)
        input.clearKeys()
        assertFalse(input.isKeyDown(Key.W))
        assertFalse(input.isKeyDown(Key.Space))
    }

    @Test
    fun keyEdgesFireOnceAcrossPressHoldRelease() {
        val input = Input()

        input.setKeyDown(Key.F1, down = true)
        val pressFrame = input.updateSnapshot()
        assertEquals(setOf(Key.F1), pressFrame.keysPressed, "the first frame down is a rising edge")
        assertEquals(emptySet(), pressFrame.keysReleased)
        assertTrue(pressFrame.wasPressed(Key.F1))
        assertTrue(pressFrame.isDown(Key.F1))

        val holdFrame = input.updateSnapshot()
        assertEquals(emptySet(), holdFrame.keysPressed, "holding must not re-fire the rising edge")
        assertEquals(emptySet(), holdFrame.keysReleased)
        assertFalse(holdFrame.wasPressed(Key.F1))
        assertTrue(holdFrame.isDown(Key.F1), "a held key is still down")

        input.setKeyDown(Key.F1, down = false)
        val releaseFrame = input.updateSnapshot()
        assertEquals(emptySet(), releaseFrame.keysPressed)
        assertEquals(setOf(Key.F1), releaseFrame.keysReleased, "letting go is a falling edge")
        assertFalse(releaseFrame.isDown(Key.F1))
    }

    @Test
    fun pressingAgainAfterAReleaseIsAFreshEdge() {
        val input = Input()
        input.setKeyDown(Key.F1, down = true)
        input.updateSnapshot()
        input.setKeyDown(Key.F1, down = false)
        input.updateSnapshot()
        input.setKeyDown(Key.F1, down = true)
        assertTrue(input.updateSnapshot().wasPressed(Key.F1))
    }

    @Test
    fun pointerStateReflectsTheLastSetCall() {
        val input = Input()
        input.setPointer(down = true, x = 12f, y = 34f)
        assertTrue(input.pointerDown)
        assertEquals(12f, input.pointerX)
        assertEquals(34f, input.pointerY)

        input.setPointer(down = false, x = 56f, y = 78f)
        assertFalse(input.pointerDown)
        assertEquals(56f, input.pointerX)
        assertEquals(78f, input.pointerY)
    }
}
