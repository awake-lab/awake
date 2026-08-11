// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UiScrollStateTest {

    @Test
    fun updateClampsOffsetToScrollableRange() {
        val state = UiScrollState(initialOffsetY = 120f)

        state.update(viewportHeight = 80f, contentHeight = 140f)

        assertEquals(60f, state.maxOffsetY)
        assertEquals(60f, state.offsetY)
        assertTrue(state.canScroll)
    }

    @Test
    fun scrollByStaysWithinBounds() {
        val state = UiScrollState()
        state.update(viewportHeight = 100f, contentHeight = 260f)

        state.scrollBy(48f)
        state.scrollBy(200f)
        assertEquals(160f, state.offsetY)

        state.scrollBy(-500f)
        assertEquals(0f, state.offsetY)
    }

    @Test
    fun resetClearsState() {
        val state = UiScrollState(initialOffsetY = 24f)
        state.update(viewportHeight = 100f, contentHeight = 180f)

        state.reset()

        assertEquals(0f, state.offsetY)
        assertEquals(0f, state.viewportHeight)
        assertEquals(0f, state.contentHeight)
        assertFalse(state.canScroll)
    }

    @Test
    fun verticalScrollThumbTracksOffsetProgress() {
        val state = UiScrollState()
        state.update(viewportHeight = 80f, contentHeight = 200f)
        state.scrollTo(60f)

        val thumb = verticalScrollThumb(
            track = UiBounds(90f, 10f, 6f, 80f),
            state = state,
        )

        assertNotNull(thumb)
        assertEquals(UiBounds(90f, 10f, 6f, 80f), thumb.track)
        assertEquals(32f, thumb.thumb.height)
        assertEquals(34f, thumb.thumb.y)
    }
}
