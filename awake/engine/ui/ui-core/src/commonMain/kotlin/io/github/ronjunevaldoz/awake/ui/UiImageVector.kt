// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.UiStroke

data class UiVectorPath(
    val path: UiPath,
    val fill: Color? = null,
    val stroke: UiStroke? = null
)

data class UiImageVector(
    val defaultWidth: Dp,
    val defaultHeight: Dp,
    val viewportWidth: Float,
    val viewportHeight: Float,
    val paths: List<UiVectorPath>
)

class UiImageVectorBuilder internal constructor(
    private val defaultWidth: Dp,
    private val defaultHeight: Dp,
    private val viewportWidth: Float,
    private val viewportHeight: Float
) {
    private val paths = ArrayList<UiVectorPath>()

    fun path(fill: Color? = null, block: UiPathBuilder.() -> Unit) {
        paths += UiVectorPath(
            path = uiPath(block = block),
            fill = fill
        )
    }

    internal fun build(): UiImageVector = UiImageVector(
        defaultWidth = defaultWidth,
        defaultHeight = defaultHeight,
        viewportWidth = viewportWidth,
        viewportHeight = viewportHeight,
        paths = paths.toList()
    )
}

fun uiImageVector(
    defaultWidth: Dp,
    defaultHeight: Dp,
    viewportWidth: Float,
    viewportHeight: Float,
    block: UiImageVectorBuilder.() -> Unit
): UiImageVector {
    val builder = UiImageVectorBuilder(defaultWidth, defaultHeight, viewportWidth, viewportHeight)
    builder.block()
    return builder.build()
}

fun UiImageVector.fitTo(slot: UiBounds): List<UiVectorPath> {
    if (viewportWidth <= 0f || viewportHeight <= 0f || slot.width <= 0f || slot.height <= 0f) return emptyList()
    val scale = minOf(slot.width / viewportWidth, slot.height / viewportHeight)
    val scaledWidth = viewportWidth * scale
    val scaledHeight = viewportHeight * scale
    val translateX = slot.x + (slot.width - scaledWidth) / 2f
    val translateY = slot.y + (slot.height - scaledHeight) / 2f
    return paths.map { vectorPath ->
        vectorPath.copy(
            path = vectorPath.path.transform(
                scaleX = scale,
                scaleY = scale,
                translateX = translateX,
                translateY = translateY
            )
        )
    }
}
