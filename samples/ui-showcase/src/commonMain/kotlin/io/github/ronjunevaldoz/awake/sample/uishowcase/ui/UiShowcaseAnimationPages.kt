// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.ui.EaseIn
import io.github.ronjunevaldoz.awake.ui.EaseInOut
import io.github.ronjunevaldoz.awake.ui.EaseOut
import io.github.ronjunevaldoz.awake.ui.Easing
import io.github.ronjunevaldoz.awake.ui.LinearEasing
import io.github.ronjunevaldoz.awake.ui.RepeatMode
import io.github.ronjunevaldoz.awake.ui.animateFloatRepeatable
import io.github.ronjunevaldoz.awake.ui.canvas
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSectionHeader
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.layouts.spacer
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.shadcnShimmer
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.sp
import io.github.ronjunevaldoz.awake.ui.UiStroke
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

internal fun ColumnScope.drawUiShowcaseShimmerPreview() {
    shadcnSectionHeader(
        title = "Shimmer Effect",
        description = "A subtle sweeping highlight applied to text and components."
    )
    spacer(Modifier.height(16f.dp))

    shadcnText(
        label = "Generating response...",
        modifier = Modifier.shadcnShimmer()
    )

    spacer(Modifier.height(12f.dp))

    shadcnText(
        label = "LOADING SCENE ASSETS",
        style = Style { textSize(14f.sp) },
        modifier = Modifier.shadcnShimmer()
    )
}

/** The four curves this page demonstrates, in the same order as the reference design. */
internal val UiShowcaseEasingCurves: List<Pair<String, Easing>> = listOf(
    "Linear" to LinearEasing,
    "Ease Out" to EaseOut,
    "Ease In Out" to EaseInOut,
    "Ease In" to EaseIn
)

private const val UiShowcaseEasingDurationMs = 1200f

internal fun ColumnScope.drawUiShowcaseEasingPreview() {
    shadcnSectionHeader(
        title = "Easing",
        description = "Fixed-duration tweens shaped by an Easing curve -- the speed profile of the moving " +
            "thumb below mirrors the shape of its curve thumbnail."
    )
    spacer(Modifier.height(16f.dp))

    UiShowcaseEasingCurves.forEach { (name, easing) ->
        drawUiShowcaseEasingRow(name, easing)
        spacer(Modifier.height(12f.dp))
    }
}

private fun ColumnScope.drawUiShowcaseEasingRow(name: String, easing: Easing) {
    val id = "showcase-easing-${name.lowercase().replace(" ", "-")}"
    val fraction = animateFloatRepeatable(
        id = id,
        initialValue = 0f,
        targetValue = 1f,
        durationMs = UiShowcaseEasingDurationMs,
        easing = easing,
        repeatMode = RepeatMode.Reverse
    )

    row(
        horizontalArrangement = Arrangement.spacedBy(12f.dp),
        modifier = Modifier.height(48f.dp.toDimension())
    ) {
        drawUiShowcaseEasingThumbnail(id = "$id-thumb", easing, modifier = Modifier.width(48f.dp).height(48f.dp))
        shadcnText(label = name, modifier = Modifier.width(96f.dp))
        drawUiShowcaseEasingTrack(
            fraction = fraction,
            modifier = Modifier.width(220f.dp).height(48f.dp)
        )
    }
}

private fun RowScope.drawUiShowcaseEasingThumbnail(
    id: String,
    easing: Easing,
    modifier: UiModifier
) {
    shadcnSurface(
        id = id,
        style = Style { shape(10f.dp) },
        modifier = modifier
    ) { slot ->
        canvas(slot.toSlot()) {
            val tokens = context.currentTheme.tokens
            val margin = bounds.width * 0.18f
            val innerWidth = bounds.width - margin * 2f
            val innerHeight = bounds.height - margin * 2f
            val steps = 20
            var previousX = margin
            var previousY = margin + innerHeight
            for (step in 1..steps) {
                val t = step / steps.toFloat()
                val eased = easing.transform(t)
                val x = margin + t * innerWidth
                val y = margin + (1f - eased) * innerHeight
                drawLine(previousX, previousY, x, y, color = tokens.primary, stroke = UiStroke(width = 2f.dp))
                previousX = x
                previousY = y
            }
        }
    }
}

private fun RowScope.drawUiShowcaseEasingTrack(
    fraction: Float,
    modifier: UiModifier
) {
    canvas(modifier) {
        val tokens = context.currentTheme.tokens
        val trackY = bounds.height / 2f
        val thumbSize = 14f
        val trackInset = thumbSize / 2f
        drawLine(
            trackInset,
            trackY,
            bounds.width - trackInset,
            trackY,
            color = tokens.border,
            stroke = UiStroke(width = 2f.dp)
        )
        val thumbX = trackInset + fraction * (bounds.width - trackInset * 2f) - thumbSize / 2f
        drawRoundRect(
            x = thumbX,
            y = trackY - thumbSize / 2f,
            width = thumbSize,
            height = thumbSize,
            color = tokens.primary,
            radius = 3f.dp
        )
    }
}
