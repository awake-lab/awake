// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.testing.ui

import io.github.ronjunevaldoz.awake.ui.UiSemanticNode
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds

enum class UiSemanticIssueKind {
    InvalidSemanticBounds,
    DuplicateSemanticId,
    SemanticOverlap,
    TextTruncated,
    ContentOutsideBounds,
    /** Text [UiSemanticNode.contentBounds] center deviates from the node's own [UiSemanticNode.bounds]
     *  center by more than the configured tolerance. Catches widgets that emit text with
     *  `centered = true` but feed it the wrong slot (e.g. a wrap-content inner claim instead
     *  of the parent panel's full bounds). */
    ContentNotCentered,
    /** A Panel node's [UiSemanticNode.contentBounds] (or Text node's [UiSemanticNode.contentBounds])
     *  is too close to the edge of its [UiSemanticNode.bounds] — less than the minimum inset
     *  on at least one side. Catches components rendered without the required content padding. */
    InsufficientPadding,
    /** Two adjacent sibling nodes are too close (gap < minimum) or overlap each other (gap < 0).
     *  Catches row/column spacing bugs where `spacedBy()` was omitted or set to zero. */
    InsufficientSpacing,
    /** A node's background, foreground, or border token does not match the expected token ID. */
    MismatchedToken,
    /** A node's width or height deviates from the exact value required by the design system. */
    WrongDimension,
    /** A node's content padding deviates from the exact value required by the design system. */
    MismatchedPadding,
    /** A node's spacing from its sibling deviates from the exact value required by the design system. */
    MismatchedSpacing
}

data class UiSemanticIssue(
    val kind: UiSemanticIssueKind,
    val nodeId: String? = null,
    val message: String
)

data class UiSemanticReport(val issues: List<UiSemanticIssue>) {
    val isClean: Boolean get() = issues.isEmpty()

    fun summary(): String = if (issues.isEmpty()) {
        "No UI semantic issues."
    } else {
        issues.joinToString(separator = "\n") { issue ->
            val prefix = issue.nodeId?.let { "node[$it] " } ?: ""
            "$prefix${issue.kind}: ${issue.message}"
        }
    }

    fun requireClean() {
        check(isClean) { summary() }
    }
}

fun inspectSemanticNodes(nodes: List<UiSemanticNode>): UiSemanticReport {
    val issues = ArrayList<UiSemanticIssue>()
    val seenIds = HashSet<String>()
    nodes.forEach { node ->
        if (!node.bounds.hasFiniteSize()) {
            issues += UiSemanticIssue(
                kind = UiSemanticIssueKind.InvalidSemanticBounds,
                nodeId = node.id,
                message = "semantic bounds are invalid: ${node.bounds}"
            )
        }
        val contentBounds = node.contentBounds
        if (contentBounds != null && !contentBounds.hasFiniteSize()) {
            issues += UiSemanticIssue(
                kind = UiSemanticIssueKind.InvalidSemanticBounds,
                nodeId = node.id,
                message = "semantic content bounds are invalid: $contentBounds"
            )
        }
        val clippedBounds = node.clippedBounds
        if (clippedBounds != null && !clippedBounds.hasFiniteSize()) {
            issues += UiSemanticIssue(
                kind = UiSemanticIssueKind.InvalidSemanticBounds,
                nodeId = node.id,
                message = "semantic clipped bounds are invalid: $clippedBounds"
            )
        }
        node.id?.let { id ->
            if (!seenIds.add(id)) {
                issues += UiSemanticIssue(
                    kind = UiSemanticIssueKind.DuplicateSemanticId,
                    nodeId = id,
                    message = "duplicate semantic id detected"
                )
            }
        }
    }
    return UiSemanticReport(issues)
}

fun inspectTextTruncation(
    nodes: List<UiSemanticNode>,
    allowIds: Set<String> = emptySet()
): UiSemanticReport {
    val issues = nodes
        .filter { it.role == UiSemanticRole.Text && it.truncated && it.id !in allowIds }
        .map { node ->
            UiSemanticIssue(
                kind = UiSemanticIssueKind.TextTruncated,
                nodeId = node.id,
                message = "text '${node.label.orEmpty()}' was truncated in bounds ${node.bounds}"
            )
        }
    return UiSemanticReport(issues)
}

fun inspectSemanticContentFit(
    nodes: List<UiSemanticNode>,
    tolerancePx: Float = 0f
): UiSemanticReport {
    val issues = ArrayList<UiSemanticIssue>()
    nodes.forEach { node ->
        val content = node.contentBounds ?: return@forEach
        if (!content.isWithin(node.bounds, tolerancePx)) {
            issues += UiSemanticIssue(
                kind = UiSemanticIssueKind.ContentOutsideBounds,
                nodeId = node.id,
                message = "content bounds $content exceed node bounds ${node.bounds} with tolerance=$tolerancePx"
            )
        }
        val clipped = node.clippedBounds ?: return@forEach
        if (!clipped.isWithin(node.bounds, tolerancePx)) {
            issues += UiSemanticIssue(
                kind = UiSemanticIssueKind.ContentOutsideBounds,
                nodeId = node.id,
                message = "clipped bounds $clipped exceed node bounds ${node.bounds} with tolerance=$tolerancePx"
            )
        }
    }
    return UiSemanticReport(issues)
}

fun inspectSemanticOverlaps(
    label: String,
    nodes: List<UiSemanticNode>,
    tolerancePx: Float = 0f
): UiSemanticReport {
    val issues = ArrayList<UiSemanticIssue>()
    nodes.forEachIndexed { index, current ->
        nodes.drop(index + 1).forEach { other ->
            if (current.bounds.overlaps(other.bounds, tolerancePx)) {
                issues += UiSemanticIssue(
                    kind = UiSemanticIssueKind.SemanticOverlap,
                    nodeId = current.id ?: other.id,
                    message = "$label overlap between ${describeNode(current)} and ${describeNode(other)}"
                )
            }
        }
    }
    return UiSemanticReport(issues)
}

/**
 * Checks that every [UiSemanticRole.Text] node whose [UiSemanticNode.contentBounds] is non-null
 * has its content center aligned with the node bounds center, within [tolerancePx].
 *
 * This is the authoritative check for text-centering correctness. It runs entirely on semantic
 * data — no GPU or pixel rasterizer required — and catches bugs like passing a wrap-content slot
 * to [renderTextBlock] instead of the enclosing panel's full bounds.
 *
 * @param nodes        Semantic nodes from [io.github.ronjunevaldoz.awake.ui.context.UiFrameOutput.semantics].
 * @param tolerancePx  Maximum allowed deviation in each axis (default 1 px — half a sub-pixel).
 * @param horizontal   When true, check horizontal centering (default true).
 * @param vertical     When true, check vertical centering (default true).
 * @param allowIds     IDs of nodes deliberately off-centre (e.g. left-aligned labels).
 */
fun inspectTextCentering(
    nodes: List<UiSemanticNode>,
    tolerancePx: Float = 1f,
    horizontal: Boolean = true,
    vertical: Boolean = true,
    allowIds: Set<String> = emptySet()
): UiSemanticReport {
    val issues = ArrayList<UiSemanticIssue>()
    nodes.forEach { node ->
        if (node.role != UiSemanticRole.Text) return@forEach
        val content = node.contentBounds ?: return@forEach
        if (node.id in allowIds) return@forEach

        val nodeCX = node.bounds.x + node.bounds.width / 2f
        val nodeCY = node.bounds.y + node.bounds.height / 2f
        val contentCX = content.x + content.width / 2f
        val contentCY = content.y + content.height / 2f

        if (horizontal && kotlin.math.abs(contentCX - nodeCX) > tolerancePx) {
            issues += UiSemanticIssue(
                kind = UiSemanticIssueKind.ContentNotCentered,
                nodeId = node.id,
                message = "text '${node.label.orEmpty()}' is not horizontally centered: " +
                    "contentCenterX=${contentCX.roundTo1()} ≠ nodeCenterX=${nodeCX.roundTo1()} (tolerance=$tolerancePx, deviation=${kotlin.math.abs(contentCX - nodeCX).roundTo1()})"
            )
        }
        if (vertical && kotlin.math.abs(contentCY - nodeCY) > tolerancePx) {
            issues += UiSemanticIssue(
                kind = UiSemanticIssueKind.ContentNotCentered,
                nodeId = node.id,
                message = "text '${node.label.orEmpty()}' is not vertically centered: " +
                    "contentCenterY=${contentCY.roundTo1()} ≠ nodeCenterY=${nodeCY.roundTo1()} (tolerance=$tolerancePx, deviation=${kotlin.math.abs(contentCY - nodeCY).roundTo1()})"
            )
        }
    }
    return UiSemanticReport(issues)
}

fun requireSemanticNode(
    nodes: List<UiSemanticNode>,
    id: String,
    role: UiSemanticRole? = null
): UiSemanticNode = requireNotNull(
    nodes.firstOrNull { it.id == id && (role == null || it.role == role) }
) {
    "expected semantic node id=$id role=${role ?: "any"}"
}

// ─────────────────────────────────────────────────────────────────────────────
// Padding inspector
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Checks that each node's [UiSemanticNode.contentBounds] is inset from its
 * [UiSemanticNode.bounds] by at least [minPaddingPx] on every side.
 *
 * Applies to:
 * - [UiSemanticRole.Panel] nodes whose `contentBounds` is set (e.g. a `surface` whose
 *   internal column measured its children and reported their union as contentBounds).
 * - [UiSemanticRole.Text] nodes — verifies the text block respects its parent's content area.
 *
 * A tolerance of 0 means content must be strictly inside the bounds by at least `minPaddingPx`.
 *
 * @param minPaddingPx Minimum required inset on each side in pixels.
 * @param allowIds     IDs of nodes deliberately flush to their bounds (e.g. full-bleed images).
 */
fun inspectPadding(
    nodes: List<UiSemanticNode>,
    minPaddingPx: Float,
    allowIds: Set<String> = emptySet()
): UiSemanticReport {
    if (minPaddingPx <= 0f) return UiSemanticReport(emptyList())
    val issues = ArrayList<UiSemanticIssue>()
    nodes.forEach { node ->
        val content = node.contentBounds ?: return@forEach
        if (node.id in allowIds) return@forEach
        if (node.role != UiSemanticRole.Panel && node.role != UiSemanticRole.Text) return@forEach

        val insetLeft   = content.x - node.bounds.x
        val insetTop    = content.y - node.bounds.y
        val insetRight  = (node.bounds.x + node.bounds.width) - (content.x + content.width)
        val insetBottom = (node.bounds.y + node.bounds.height) - (content.y + content.height)

        val violations = buildList {
            if (insetLeft   < minPaddingPx) add("left=${insetLeft.roundTo1()}")
            if (insetTop    < minPaddingPx) add("top=${insetTop.roundTo1()}")
            if (insetRight  < minPaddingPx) add("right=${insetRight.roundTo1()}")
            if (insetBottom < minPaddingPx) add("bottom=${insetBottom.roundTo1()}")
        }
        if (violations.isNotEmpty()) {
            issues += UiSemanticIssue(
                kind = UiSemanticIssueKind.InsufficientPadding,
                nodeId = node.id,
                message = "node ${describeNode(node)} has insufficient content padding " +
                    "(min=${minPaddingPx}px): ${violations.joinToString(", ")}"
            )
        }
    }
    return UiSemanticReport(issues)
}

private fun Float.roundTo1(): String = (kotlin.math.round(this * 10f) / 10f).toString()

/**
 * Checks that each node's [UiSemanticNode.contentBounds] is inset from its
 * [UiSemanticNode.bounds] by exactly [expectedPaddingPx] on every side.
 *
 * @param expectedPaddingPx The exact required inset on each side in pixels.
 * @param tolerancePx       Maximum allowed deviation from the exact padding (default 0.5px).
 */
fun inspectExactPadding(
    nodes: List<UiSemanticNode>,
    expectedPaddingPx: Float,
    tolerancePx: Float = 0.5f,
    allowIds: Set<String> = emptySet()
): UiSemanticReport {
    val issues = ArrayList<UiSemanticIssue>()
    nodes.forEach { node ->
        val content = node.contentBounds ?: return@forEach
        if (node.id in allowIds) return@forEach
        if (node.role != UiSemanticRole.Panel && node.role != UiSemanticRole.Text) return@forEach

        val insetLeft   = content.x - node.bounds.x
        val insetTop    = content.y - node.bounds.y
        val insetRight  = (node.bounds.x + node.bounds.width) - (content.x + content.width)
        val insetBottom = (node.bounds.y + node.bounds.height) - (content.y + content.height)

        val violations = buildList {
            if (kotlin.math.abs(insetLeft - expectedPaddingPx) > tolerancePx) add("left=${insetLeft.roundTo1()}")
            if (kotlin.math.abs(insetTop - expectedPaddingPx) > tolerancePx) add("top=${insetTop.roundTo1()}")
            if (kotlin.math.abs(insetRight - expectedPaddingPx) > tolerancePx) add("right=${insetRight.roundTo1()}")
            if (kotlin.math.abs(insetBottom - expectedPaddingPx) > tolerancePx) add("bottom=${insetBottom.roundTo1()}")
        }
        if (violations.isNotEmpty()) {
            issues += UiSemanticIssue(
                kind = UiSemanticIssueKind.MismatchedPadding,
                nodeId = node.id,
                message = "node ${describeNode(node)} has incorrect content padding " +
                    "(expected=${expectedPaddingPx}px): ${violations.joinToString(", ")}"
            )
        }
    }
    return UiSemanticReport(issues)
}

// ─────────────────────────────────────────────────────────────────────────────
// Spacing inspector
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Checks that the gap between every pair of adjacent siblings (ordered by axis) is at
 * least [minGapPx]. Works entirely from [UiSemanticNode.bounds] — no parent reference needed.
 *
 * Pass a named subset of nodes (e.g. siblings of a particular row) via [nodes]. The inspector
 * automatically determines the dominant axis (H or V) from how the nodes are arranged.
 *
 * @param label      Human-readable name of the group for error messages.
 * @param nodes      Sibling nodes to check, in any order (they are sorted by the inspector).
 * @param minGapPx   Minimum required gap between siblings in pixels. Use 0 to just assert
 *                   no overlap without a minimum gap requirement.
 * @param axis       Explicit axis override. If null, the axis is auto-detected from the spread.
 */
fun inspectSpacing(
    label: String,
    nodes: List<UiSemanticNode>,
    minGapPx: Float,
    axis: SpacingAxis? = null
): UiSemanticReport {
    if (nodes.size < 2) return UiSemanticReport(emptyList())
    val issues = ArrayList<UiSemanticIssue>()

    // Auto-detect axis: compare total horizontal spread vs vertical spread
    val resolvedAxis = axis ?: run {
        val hSpread = nodes.maxOf { it.bounds.x + it.bounds.width } - nodes.minOf { it.bounds.x }
        val vSpread = nodes.maxOf { it.bounds.y + it.bounds.height } - nodes.minOf { it.bounds.y }
        if (hSpread >= vSpread) SpacingAxis.Horizontal else SpacingAxis.Vertical
    }

    val sorted = when (resolvedAxis) {
        SpacingAxis.Horizontal -> nodes.sortedBy { it.bounds.x }
        SpacingAxis.Vertical   -> nodes.sortedBy { it.bounds.y }
    }

    sorted.zipWithNext().forEach { (a, b) ->
        val gap = when (resolvedAxis) {
            SpacingAxis.Horizontal -> b.bounds.x - (a.bounds.x + a.bounds.width)
            SpacingAxis.Vertical   -> b.bounds.y - (a.bounds.y + a.bounds.height)
        }
        if (gap < minGapPx) {
            issues += UiSemanticIssue(
                kind = UiSemanticIssueKind.InsufficientSpacing,
                nodeId = a.id ?: b.id,
                message = "$label ($resolvedAxis): gap between ${describeNode(a)} and " +
                    "${describeNode(b)} is ${gap.roundTo1()}px < min=${minGapPx}px"
            )
        }
    }
    return UiSemanticReport(issues)
}

/**
 * Checks that the gap between every pair of adjacent siblings (ordered by axis) is
 * exactly [expectedGapPx].
 *
 * @param expectedGapPx The exact required gap between siblings in pixels.
 * @param tolerancePx   Maximum allowed deviation from the exact spacing (default 0.5px).
 */
fun inspectExactSpacing(
    label: String,
    nodes: List<UiSemanticNode>,
    expectedGapPx: Float,
    axis: SpacingAxis? = null,
    tolerancePx: Float = 0.5f
): UiSemanticReport {
    if (nodes.size < 2) return UiSemanticReport(emptyList())
    val issues = ArrayList<UiSemanticIssue>()

    val resolvedAxis = axis ?: run {
        val hSpread = nodes.maxOf { it.bounds.x + it.bounds.width } - nodes.minOf { it.bounds.x }
        val vSpread = nodes.maxOf { it.bounds.y + it.bounds.height } - nodes.minOf { it.bounds.y }
        if (hSpread >= vSpread) SpacingAxis.Horizontal else SpacingAxis.Vertical
    }

    val sorted = when (resolvedAxis) {
        SpacingAxis.Horizontal -> nodes.sortedBy { it.bounds.x }
        SpacingAxis.Vertical   -> nodes.sortedBy { it.bounds.y }
    }

    sorted.zipWithNext().forEach { (a, b) ->
        val gap = when (resolvedAxis) {
            SpacingAxis.Horizontal -> b.bounds.x - (a.bounds.x + a.bounds.width)
            SpacingAxis.Vertical   -> b.bounds.y - (a.bounds.y + a.bounds.height)
        }
        if (kotlin.math.abs(gap - expectedGapPx) > tolerancePx) {
            issues += UiSemanticIssue(
                kind = UiSemanticIssueKind.MismatchedSpacing,
                nodeId = a.id ?: b.id,
                message = "$label ($resolvedAxis): gap between ${describeNode(a)} and " +
                    "${describeNode(b)} is ${gap.roundTo1()}px ≠ expected=${expectedGapPx}px"
            )
        }
    }
    return UiSemanticReport(issues)
}

/**
 * Checks that specific nodes have exact width and height as required by the design system.
 */
fun inspectDimensions(
    nodes: List<UiSemanticNode>,
    exactHeight: Float? = null,
    exactWidth: Float? = null,
    tolerancePx: Float = 0.5f
): UiSemanticReport {
    val issues = ArrayList<UiSemanticIssue>()
    nodes.forEach { node ->
        if (exactHeight != null && kotlin.math.abs(node.bounds.height - exactHeight) > tolerancePx) {
            issues += UiSemanticIssue(
                kind = UiSemanticIssueKind.WrongDimension,
                nodeId = node.id,
                message = "node ${describeNode(node)} has incorrect height: " +
                    "${node.bounds.height.roundTo1()}px ≠ expected=${exactHeight}px"
            )
        }
        if (exactWidth != null && kotlin.math.abs(node.bounds.width - exactWidth) > tolerancePx) {
            issues += UiSemanticIssue(
                kind = UiSemanticIssueKind.WrongDimension,
                nodeId = node.id,
                message = "node ${describeNode(node)} has incorrect width: " +
                    "${node.bounds.width.roundTo1()}px ≠ expected=${exactWidth}px"
            )
        }
    }
    return UiSemanticReport(issues)
}

/**
 * Assert that specific semantic nodes use the correct design tokens.
 */
fun inspectTokens(
    nodes: List<UiSemanticNode>,
    expectedBackgroundToken: String? = null,
    expectedForegroundToken: String? = null,
    expectedBorderToken: String? = null,
    expectedTextStyleToken: String? = null
): UiSemanticReport {
    val issues = ArrayList<UiSemanticIssue>()
    nodes.forEach { node ->
        if (expectedBackgroundToken != null && node.backgroundToken != expectedBackgroundToken) {
            issues += UiSemanticIssue(
                kind = UiSemanticIssueKind.MismatchedToken,
                nodeId = node.id,
                message = "node ${describeNode(node)} background token mismatch: " +
                    "actual='${node.backgroundToken}' ≠ expected='$expectedBackgroundToken'"
            )
        }
        if (expectedForegroundToken != null && node.foregroundToken != expectedForegroundToken) {
            issues += UiSemanticIssue(
                kind = UiSemanticIssueKind.MismatchedToken,
                nodeId = node.id,
                message = "node ${describeNode(node)} foreground token mismatch: " +
                    "actual='${node.foregroundToken}' ≠ expected='$expectedForegroundToken'"
            )
        }
        if (expectedBorderToken != null && node.borderToken != expectedBorderToken) {
            issues += UiSemanticIssue(
                kind = UiSemanticIssueKind.MismatchedToken,
                nodeId = node.id,
                message = "node ${describeNode(node)} border token mismatch: " +
                    "actual='${node.borderToken}' ≠ expected='$expectedBorderToken'"
            )
        }
        if (expectedTextStyleToken != null && node.textStyleToken != expectedTextStyleToken) {
            issues += UiSemanticIssue(
                kind = UiSemanticIssueKind.MismatchedToken,
                nodeId = node.id,
                message = "node ${describeNode(node)} text style token mismatch: " +
                    "actual='${node.textStyleToken}' ≠ expected='$expectedTextStyleToken'"
            )
        }
    }
    return UiSemanticReport(issues)
}

/** Axis override for [inspectSpacing]. Null = auto-detect from node spread. */
enum class SpacingAxis { Horizontal, Vertical }

private fun UiBounds.hasFiniteSize(): Boolean =
    x.isFinite() && y.isFinite() && width.isFinite() && height.isFinite() && width >= 0f && height >= 0f

private fun UiBounds.isWithin(other: UiBounds, tolerancePx: Float): Boolean =
    x >= other.x - tolerancePx &&
        y >= other.y - tolerancePx &&
        x + width <= other.x + other.width + tolerancePx &&
        y + height <= other.y + other.height + tolerancePx

private fun UiBounds.overlaps(other: UiBounds, tolerancePx: Float): Boolean =
    x < other.x + other.width - tolerancePx &&
        x + width > other.x + tolerancePx &&
        y < other.y + other.height - tolerancePx &&
        y + height > other.y + tolerancePx

private fun describeNode(node: UiSemanticNode): String =
    "${node.role}${node.id?.let { "[$it]" } ?: ""}@${node.bounds}"
