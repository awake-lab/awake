// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.testing.ui

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiPath
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.bounds
import io.github.ronjunevaldoz.awake.ui.toPx
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

enum class UiPrimitiveMetricKind {
    Quad,
    GradientQuad,
    RoundedQuad,
    Glyph,
    Texture,
    FilledPath,
    StrokedPath,
    ClipPush,
    ClipPathPush,
    ClipPop
}

data class UiFrameMetrics(
    val frame: UiSlot,
    val primitiveCounts: Map<UiPrimitiveMetricKind, Int>,
    val contentBounds: UiSlot?
) {
    fun normalizedContentBounds(): UiSlot? = contentBounds?.let { bounds ->
        if (frame.width <= 0f || frame.height <= 0f) {
            null
        } else {
            UiSlot(
                x = (bounds.x - frame.x) / frame.width,
                y = (bounds.y - frame.y) / frame.height,
                width = bounds.width / frame.width,
                height = bounds.height / frame.height
            )
        }
    }
}

data class UiMetricsReport(val issues: List<String>) {
    val isClean: Boolean get() = issues.isEmpty()

    fun summary(): String = if (issues.isEmpty()) {
        "No UI metrics issues."
    } else {
        issues.joinToString(separator = "\n")
    }

    fun requireClean() {
        check(isClean) { summary() }
    }
}

fun measureUiFrame(
    primitives: List<UiDrawPrimitive>,
    frame: UiSlot
): UiFrameMetrics {
    val counts = linkedMapOf<UiPrimitiveMetricKind, Int>()
    var contentBounds: UiSlot? = null

    primitives.forEach { primitive ->
        counts[primitive.metricKind()] = (counts[primitive.metricKind()] ?: 0) + 1
        primitive.metricBounds()?.let { bounds ->
            if (bounds.width <= 0f || bounds.height <= 0f) {
                return@let
            }
            contentBounds = contentBounds?.union(bounds) ?: bounds
        }
    }

    return UiFrameMetrics(
        frame = frame,
        primitiveCounts = counts,
        contentBounds = contentBounds
    )
}

fun inspectThemeParity(
    reference: UiFrameMetrics,
    candidate: UiFrameMetrics,
    boundsTolerancePx: Float = 1f
): UiMetricsReport {
    val issues = ArrayList<String>()
    val allKinds = (reference.primitiveCounts.keys + candidate.primitiveCounts.keys).distinct()
    allKinds.forEach { kind ->
        val referenceCount = reference.primitiveCounts[kind] ?: 0
        val candidateCount = candidate.primitiveCounts[kind] ?: 0
        if (referenceCount != candidateCount) {
            issues += "primitive-count mismatch for $kind: expected $referenceCount, actual $candidateCount"
        }
    }

    compareBounds(
        label = "theme content bounds",
        reference = reference.contentBounds,
        candidate = candidate.contentBounds,
        tolerance = boundsTolerancePx,
        issues = issues
    )

    return UiMetricsReport(issues)
}

fun inspectDensityParity(
    reference: UiFrameMetrics,
    candidate: UiFrameMetrics,
    normalizedTolerance: Float = 0.04f
): UiMetricsReport {
    val issues = ArrayList<String>()
    val referenceBounds = reference.normalizedContentBounds()
    val candidateBounds = candidate.normalizedContentBounds()
    compareBounds(
        label = "density-normalized content bounds",
        reference = referenceBounds,
        candidate = candidateBounds,
        tolerance = normalizedTolerance,
        issues = issues
    )
    return UiMetricsReport(issues)
}

fun inspectBoundsFit(
    label: String,
    metrics: UiFrameMetrics,
    allowedBounds: UiSlot,
    tolerancePx: Float = 0f
): UiMetricsReport {
    val bounds = metrics.contentBounds ?: return UiMetricsReport(emptyList())
    val issues = ArrayList<String>()
    if (bounds.x < allowedBounds.x - tolerancePx ||
        bounds.y < allowedBounds.y - tolerancePx ||
        bounds.x + bounds.width > allowedBounds.x + allowedBounds.width + tolerancePx ||
        bounds.y + bounds.height > allowedBounds.y + allowedBounds.height + tolerancePx
    ) {
        issues += "$label exceeds allowed bounds: content=$bounds allowed=$allowedBounds tolerance=$tolerancePx"
    }
    return UiMetricsReport(issues)
}

fun inspectNonOverlappingBounds(
    label: String,
    bounds: List<UiSlot>,
    tolerancePx: Float = 0f
): UiMetricsReport {
    val issues = ArrayList<String>()
    bounds.forEachIndexed { index, current ->
        bounds.drop(index + 1).forEachIndexed { offset, other ->
            val otherIndex = index + offset + 1
            val overlaps = current.x < other.x + other.width - tolerancePx &&
                current.x + current.width > other.x + tolerancePx &&
                current.y < other.y + other.height - tolerancePx &&
                current.y + current.height > other.y + tolerancePx
            if (overlaps) {
                issues += "$label overlap between [$index]=$current and [$otherIndex]=$other with tolerance=$tolerancePx"
            }
        }
    }
    return UiMetricsReport(issues)
}

private fun compareBounds(
    label: String,
    reference: UiSlot?,
    candidate: UiSlot?,
    tolerance: Float,
    issues: MutableList<String>
) {
    when {
        reference == null && candidate == null -> Unit
        reference == null || candidate == null -> issues += "$label missing on one side: expected=$reference actual=$candidate"
        abs(reference.x - candidate.x) > tolerance ||
            abs(reference.y - candidate.y) > tolerance ||
            abs(reference.width - candidate.width) > tolerance ||
            abs(reference.height - candidate.height) > tolerance ->
            issues += "$label drifted beyond tolerance=$tolerance: expected=$reference actual=$candidate"
    }
}

private fun UiDrawPrimitive.metricKind(): UiPrimitiveMetricKind = when (this) {
    is UiDrawPrimitive.Quad -> UiPrimitiveMetricKind.Quad
    is UiDrawPrimitive.GradientQuad -> UiPrimitiveMetricKind.GradientQuad
    is UiDrawPrimitive.RoundedQuad -> UiPrimitiveMetricKind.RoundedQuad
    is UiDrawPrimitive.Glyph -> UiPrimitiveMetricKind.Glyph
    is UiDrawPrimitive.Texture -> UiPrimitiveMetricKind.Texture
    is UiDrawPrimitive.FilledPath -> UiPrimitiveMetricKind.FilledPath
    is UiDrawPrimitive.StrokedPath -> UiPrimitiveMetricKind.StrokedPath
    is UiDrawPrimitive.ClipPush -> UiPrimitiveMetricKind.ClipPush
    is UiDrawPrimitive.ClipPathPush -> UiPrimitiveMetricKind.ClipPathPush
    is UiDrawPrimitive.ClipPop -> UiPrimitiveMetricKind.ClipPop
}

private fun UiDrawPrimitive.metricBounds(): UiSlot? = when (this) {
    is UiDrawPrimitive.Quad -> UiSlot(x, y, w, h)
    is UiDrawPrimitive.GradientQuad -> UiSlot(x, y, w, h)
    is UiDrawPrimitive.RoundedQuad -> UiSlot(x, y, w, h)
    is UiDrawPrimitive.Glyph -> UiSlot(x, y, w, h)
    is UiDrawPrimitive.Texture -> UiSlot(x, y, w, h)
    is UiDrawPrimitive.FilledPath -> path.bounds()
    is UiDrawPrimitive.StrokedPath -> strokedBounds(path, stroke.width.toPx())
    is UiDrawPrimitive.ClipPush -> null
    is UiDrawPrimitive.ClipPathPush -> null
    is UiDrawPrimitive.ClipPop -> null
}

private fun strokedBounds(path: UiPath, strokeWidthPx: Float): UiSlot {
    val bounds = path.bounds()
    val inset = strokeWidthPx / 2f
    return UiSlot(
        x = bounds.x - inset,
        y = bounds.y - inset,
        width = bounds.width + inset * 2f,
        height = bounds.height + inset * 2f
    )
}

private fun UiSlot.union(other: UiSlot): UiSlot {
    val minX = min(x, other.x)
    val minY = min(y, other.y)
    val maxX = max(x + width, other.x + other.width)
    val maxY = max(y + height, other.y + other.height)
    return UiSlot(
        x = minX,
        y = minY,
        width = maxX - minX,
        height = maxY - minY
    )
}
