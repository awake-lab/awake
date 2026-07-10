package io.github.ronjunevaldoz.awake.core.input

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InputTest {
    @AfterTest
    fun resetSharedState() {
        Input.clearKeys()
        Input.setPointer(down = false, x = 0f, y = 0f)
    }

    @Test
    fun keyIsNotDownByDefault() {
        assertFalse(Input.isKeyDown(Key.Space))
    }

    @Test
    fun settingAKeyDownIsObservable() {
        Input.setKeyDown(Key.W, down = true)
        assertTrue(Input.isKeyDown(Key.W))
    }

    @Test
    fun releasingAKeyClearsIt() {
        Input.setKeyDown(Key.W, down = true)
        Input.setKeyDown(Key.W, down = false)
        assertFalse(Input.isKeyDown(Key.W))
    }

    @Test
    fun clearKeysReleasesEveryHeldKey() {
        Input.setKeyDown(Key.W, down = true)
        Input.setKeyDown(Key.Space, down = true)
        Input.clearKeys()
        assertFalse(Input.isKeyDown(Key.W))
        assertFalse(Input.isKeyDown(Key.Space))
    }

    @Test
    fun pointerStateReflectsTheLastSetCall() {
        Input.setPointer(down = true, x = 12f, y = 34f)
        assertTrue(Input.pointerDown)
        assertEquals(12f, Input.pointerX)
        assertEquals(34f, Input.pointerY)

        Input.setPointer(down = false, x = 56f, y = 78f)
        assertFalse(Input.pointerDown)
        assertEquals(56f, Input.pointerX)
        assertEquals(78f, Input.pointerY)
    }
}
