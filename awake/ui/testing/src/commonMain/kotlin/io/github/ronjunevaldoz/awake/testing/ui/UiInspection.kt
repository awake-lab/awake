// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.testing.ui

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiPath
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.api.layout.intersect
import io.github.ronjunevaldoz.awake.ui.bounds
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.toPx

enum class UiInspectionIssueKind {
    InvalidPrimitiveBounds,
    PrimitiveOutsideFrame,
    GlyphMissingFont,
    ClipStackUnderflow,
    ClipStackUnbalanced,
}

data class UiInspectionIssue(
    val kind: UiInspectionIssueKind,
    val primitiveIndex: Int? = null,
    val message: String,
)

data class UiInspectionReport(
    val frame: UiBounds,
    val issues: List<UiInspectionIssue>,
) {
    val isClean: Boolean get() = issues.isEmpty()

    fun summary(): String = if (issues.isEmpty()) {
        "No UI inspection issues."
    } else {
        issues.joinToString(separator = "\n") { issue ->
            val indexPrefix = issue.primitiveIndex?.let { "primitive[$it] " } ?: ""
            "$indexPrefix${issue.kind}: ${issue.message}"
        }
    }

    fun requireClean() {
        check(isClean) { summary() }
    }
}

fun inspectUiFrame(
    primitives: List<UiDrawPrimitive>,
    frame: UiBounds,
    font: UiFont? = null,
): UiInspectionReport {
    val frameSlot = frame
    val issues = ArrayList<UiInspectionIssue>()
    val clipStack = ArrayDeque<io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds>()

    fun currentClip(): io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds = clipStack.lastOrNull() ?: frameSlot

    fun addIssue(kind: UiInspectionIssueKind, primitiveIndex: Int? = null, message: String) {
        issues += UiInspectionIssue(kind = kind, primitiveIndex = primitiveIndex, message = message)
    }

    fun Float.isFiniteCoordinate(): Boolean = isFinite() && !isNaN()

    fun slotHasFiniteBounds(slot: io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds): Boolean =
        slot.x.isFiniteCoordinate() &&
            slot.y.isFiniteCoordinate() &&
            slot.width.isFiniteCoordinate() &&
            slot.height.isFiniteCoordinate()

    fun slotHasValidSize(slot: io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds): Boolean = slot.width >= 0f && slot.height >= 0f

    fun visibleOutsideFrame(slot: io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds): Boolean {
        val visible = slot.intersect(currentClip())
        if (visible.width <= 0f || visible.height <= 0f) {
            return false
        }
        return visible.x < frameSlot.x ||
            visible.y < frameSlot.y ||
            visible.x + visible.width > frameSlot.x + frameSlot.width ||
            visible.y + visible.height > frameSlot.y + frameSlot.height
    }

    fun primitiveBounds(primitive: UiDrawPrimitive): io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds? = when (primitive) {
        is UiDrawPrimitive.Quad -> io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds(primitive.x, primitive.y, primitive.w, primitive.h)
        is UiDrawPrimitive.GradientQuad -> io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds(primitive.x, primitive.y, primitive.w, primitive.h)
        is UiDrawPrimitive.RoundedQuad -> io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds(primitive.x, primitive.y, primitive.w, primitive.h)
        is UiDrawPrimitive.Glyph -> io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds(primitive.x, primitive.y, primitive.w, primitive.h)
        is UiDrawPrimitive.Texture -> io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds(primitive.x, primitive.y, primitive.w, primitive.h)
        is UiDrawPrimitive.ShadowQuad -> io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds(
            primitive.x + primitive.offsetX - primitive.blurRadius - primitive.spread,
            primitive.y + primitive.offsetY - primitive.blurRadius - primitive.spread,
            primitive.w + (primitive.blurRadius + primitive.spread) * 2f,
            primitive.h + (primitive.blurRadius + primitive.spread) * 2f,
        )
        is UiDrawPrimitive.FilledPath -> primitive.path.bounds()
        is UiDrawPrimitive.StrokedPath -> strokedPathBounds(primitive.path, primitive.stroke.width.toPx())
        is UiDrawPrimitive.ClipPush -> primitive.rect
        is UiDrawPrimitive.ClipPathPush -> primitive.boundsRect
        is UiDrawPrimitive.ClipPop -> primitive.restoreRect
    }

    primitives.forEachIndexed { index, primitive ->
        when (primitive) {
            is UiDrawPrimitive.ClipPush -> {
                if (!slotHasFiniteBounds(primitive.rect) || !slotHasValidSize(primitive.rect)) {
                    addIssue(
                        UiInspectionIssueKind.InvalidPrimitiveBounds,
                        index,
                        "clip rect has invalid bounds: ${primitive.rect}",
                    )
                } else {
                    clipStack.addLast(currentClip().intersect(primitive.rect))
                }
            }
            is UiDrawPrimitive.ClipPathPush -> {
                if (!slotHasFiniteBounds(primitive.boundsRect) || !slotHasValidSize(primitive.boundsRect)) {
                    addIssue(
                        UiInspectionIssueKind.InvalidPrimitiveBounds,
                        index,
                        "clip path bounds have invalid bounds: ${primitive.boundsRect}",
                    )
                } else {
                    clipStack.addLast(currentClip().intersect(primitive.boundsRect))
                }
            }
            is UiDrawPrimitive.ClipPop -> {
                if (clipStack.isEmpty()) {
                    addIssue(
                        UiInspectionIssueKind.ClipStackUnderflow,
                        index,
                        "clip pop has no matching push",
                    )
                } else {
                    clipStack.removeLast()
                }
            }
            else -> {
                val bounds = primitiveBounds(primitive)
                if (bounds == null || !slotHasFiniteBounds(bounds) || !slotHasValidSize(bounds)) {
                    addIssue(
                        UiInspectionIssueKind.InvalidPrimitiveBounds,
                        index,
                        "drawable primitive has invalid bounds: $bounds",
                    )
                } else if (visibleOutsideFrame(bounds)) {
                    addIssue(
                        UiInspectionIssueKind.PrimitiveOutsideFrame,
                        index,
                        "visible bounds $bounds exceed frame $frame",
                    )
                }
                if (primitive is UiDrawPrimitive.Glyph && font == null) {
                    addIssue(
                        UiInspectionIssueKind.GlyphMissingFont,
                        index,
                        "glyph primitives were emitted without a font atlas",
                    )
                }
            }
        }
    }

    if (clipStack.isNotEmpty()) {
        addIssue(
            UiInspectionIssueKind.ClipStackUnbalanced,
            null,
            "frame ended with ${clipStack.size} unmatched clip push operations",
        )
    }

    return UiInspectionReport(frame = frame, issues = issues)
}

private fun strokedPathBounds(path: UiPath, strokeWidthPx: Float): io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds {
    val bounds = path.bounds()
    val inset = strokeWidthPx / 2f
    return io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds(
        x = bounds.x - inset,
        y = bounds.y - inset,
        width = bounds.width + inset * 2f,
        height = bounds.height + inset * 2f,
    )
}
