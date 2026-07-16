// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

class UiScrollState(initialOffsetY: Float = 0f) {
    var offsetY: Float = initialOffsetY.coerceAtLeast(0f)
        private set

    var viewportHeight: Float = 0f
        private set

    var contentHeight: Float = 0f
        private set

    val maxOffsetY: Float
        get() = (contentHeight - viewportHeight).coerceAtLeast(0f)

    val canScroll: Boolean
        get() = maxOffsetY > 0f

    fun update(viewportHeight: Float, contentHeight: Float) {
        this.viewportHeight = viewportHeight.coerceAtLeast(0f)
        this.contentHeight = contentHeight.coerceAtLeast(0f)
        offsetY = offsetY.coerceIn(0f, maxOffsetY)
    }

    fun scrollBy(deltaY: Float) {
        scrollTo(offsetY + deltaY)
    }

    fun scrollTo(offsetY: Float) {
        this.offsetY = offsetY.coerceIn(0f, maxOffsetY)
    }

    fun reset() {
        viewportHeight = 0f
        contentHeight = 0f
        offsetY = 0f
    }
}

data class UiScrollThumb(
    val track: UiSlot,
    val thumb: UiSlot
)

fun verticalScrollThumb(
    track: UiSlot,
    state: UiScrollState,
    minThumbHeight: Float = 12f
): UiScrollThumb? {
    if (!state.canScroll || track.height <= 0f || track.width <= 0f) {
        return null
    }
    val visibleFraction = (state.viewportHeight / state.contentHeight).coerceIn(0f, 1f)
    val thumbHeight = (track.height * visibleFraction).coerceIn(minThumbHeight.coerceAtLeast(0f), track.height)
    val availableTravel = (track.height - thumbHeight).coerceAtLeast(0f)
    val progress = if (state.maxOffsetY <= 0f) 0f else (state.offsetY / state.maxOffsetY).coerceIn(0f, 1f)
    val thumbY = track.y + availableTravel * progress
    return UiScrollThumb(
        track = track,
        thumb = UiSlot(
            x = track.x,
            y = thumbY,
            width = track.width,
            height = thumbHeight
        )
    )
}
