// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.layouts.AbsoluteScope

/** Slot API for custom scrollbar rendering. */
typealias UiScrollbarSlot = AbsoluteScope.(thumb: UiScrollThumb) -> Unit

/** Policy for when scrollbars should be visible. */
enum class UiScrollbarVisibility {
    /** Show only when content exceeds the viewport. */
    Auto,
    /** Always reserve space and show the thumb (even if disabled). */
    Always,
    /** Never show or reserve space for the scrollbar, even if content overflows. */
    Never
}

/** Styling and behavior configuration for a scrollable container. */
data class UiScrollConfig(
    val width: Dp = 6f.dp,
    val gap: Dp = 8f.dp,
    val verticalVisibility: UiScrollbarVisibility = UiScrollbarVisibility.Auto,
    val horizontalVisibility: UiScrollbarVisibility = UiScrollbarVisibility.Auto,
    val verticalScrollbar: UiScrollbarSlot? = null,
    val horizontalScrollbar: UiScrollbarSlot? = null,
    val scrollSpeed: Float = 32f
) {
    companion object {
        val Default = UiScrollConfig()
        val Hidden = UiScrollConfig(
            verticalVisibility = UiScrollbarVisibility.Never,
            horizontalVisibility = UiScrollbarVisibility.Never
        )
    }
}

open class UiScrollState(
    initialOffsetX: Float = 0f,
    initialOffsetY: Float = 0f
) {
    open var offsetX: Float = initialOffsetX.coerceAtLeast(0f)
        protected set

    open var offsetY: Float = initialOffsetY.coerceAtLeast(0f)
        protected set

    var viewportWidth: Float = 0f
        private set

    var viewportHeight: Float = 0f
        private set

    var contentWidth: Float = 0f
        private set

    var contentHeight: Float = 0f
        private set

    val maxOffsetX: Float
        get() = (contentWidth - viewportWidth).coerceAtLeast(0f)

    val maxOffsetY: Float
        get() = (contentHeight - viewportHeight).coerceAtLeast(0f)

    val canScrollX: Boolean
        get() = maxOffsetX > 0f

    val canScrollY: Boolean
        get() = maxOffsetY > 0f

    /** Historical alias for [canScrollY] used by [verticalScrollThumb] and [scrollPanel]. */
    val canScroll: Boolean get() = canScrollY

    open fun update(
        viewportWidth: Float = this.viewportWidth,
        viewportHeight: Float = this.viewportHeight,
        contentWidth: Float = this.contentWidth,
        contentHeight: Float = this.contentHeight
    ) {
        this.viewportWidth = viewportWidth.coerceAtLeast(0f)
        this.viewportHeight = viewportHeight.coerceAtLeast(0f)
        this.contentWidth = contentWidth.coerceAtLeast(0f)
        this.contentHeight = contentHeight.coerceAtLeast(0f)
        
        offsetX = offsetX.coerceIn(0f, maxOffsetX)
        offsetY = offsetY.coerceIn(0f, maxOffsetY)
    }

    fun scrollBy(deltaY: Float = 0f, deltaX: Float = 0f) {
        scrollTo(offsetY = offsetY + deltaY, offsetX = offsetX + deltaX)
    }

    open fun scrollTo(offsetY: Float = this.offsetY, offsetX: Float = this.offsetX) {
        this.offsetX = offsetX.coerceIn(0f, maxOffsetX)
        this.offsetY = offsetY.coerceIn(0f, maxOffsetY)
    }

    open fun reset() {
        viewportWidth = 0f
        viewportHeight = 0f
        contentWidth = 0f
        contentHeight = 0f
        offsetX = 0f
        offsetY = 0f
    }
}

/**
 * Persisted version of [UiScrollState] backed by Awake's [WidgetState].
 */
internal class PersistedUiScrollState(
    private val widgetState: WidgetState,
    initialOffsetX: Float = 0f,
    initialOffsetY: Float = 0f
) : UiScrollState(
    widgetState.get("offsetX", initialOffsetX),
    widgetState.get("offsetY", initialOffsetY)
) {
    override var offsetX: Float
        get() = super.offsetX
        set(value) {
            super.offsetX = value
            widgetState.set("offsetX", value)
        }

    override var offsetY: Float
        get() = super.offsetY
        set(value) {
            super.offsetY = value
            widgetState.set("offsetY", value)
        }

    override fun scrollTo(offsetY: Float, offsetX: Float) {
        super.scrollTo(offsetY, offsetX)
        widgetState.set("offsetX", this.offsetX)
        widgetState.set("offsetY", this.offsetY)
    }

    override fun reset() {
        super.reset()
        widgetState.remove("offsetX")
        widgetState.remove("offsetY")
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
    if (!state.canScrollY || track.height <= 0f || track.width <= 0f) {
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

fun horizontalScrollThumb(
    track: UiSlot,
    state: UiScrollState,
    minThumbWidth: Float = 12f
): UiScrollThumb? {
    if (!state.canScrollX || track.width <= 0f || track.height <= 0f) {
        return null
    }
    val visibleFraction = (state.viewportWidth / state.contentWidth).coerceIn(0f, 1f)
    val thumbWidth = (track.width * visibleFraction).coerceIn(minThumbWidth.coerceAtLeast(0f), track.width)
    val availableTravel = (track.width - thumbWidth).coerceAtLeast(0f)
    val progress = if (state.maxOffsetX <= 0f) 0f else (state.offsetX / state.maxOffsetX).coerceIn(0f, 1f)
    val thumbX = track.x + availableTravel * progress
    return UiScrollThumb(
        track = track,
        thumb = UiSlot(
            x = thumbX,
            y = track.y,
            width = thumbWidth,
            height = track.height
        )
    )
}
