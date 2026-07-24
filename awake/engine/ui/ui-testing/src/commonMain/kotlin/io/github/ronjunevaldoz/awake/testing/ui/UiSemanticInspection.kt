// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.testing.ui

import io.github.ronjunevaldoz.awake.ui.UiSemanticNode
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot

enum class UiSemanticIssueKind {
    InvalidSemanticBounds,
    DuplicateSemanticId,
    SemanticOverlap,
    TextTruncated,
    ContentOutsideBounds
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

fun requireSemanticNode(
    nodes: List<UiSemanticNode>,
    id: String,
    role: UiSemanticRole? = null
): UiSemanticNode = requireNotNull(
    nodes.firstOrNull { it.id == id && (role == null || it.role == role) }
) {
    "expected semantic node id=$id role=${role ?: "any"}"
}

private fun UiSlot.hasFiniteSize(): Boolean =
    x.isFinite() && y.isFinite() && width.isFinite() && height.isFinite() && width >= 0f && height >= 0f

private fun UiSlot.isWithin(other: UiSlot, tolerancePx: Float): Boolean =
    x >= other.x - tolerancePx &&
        y >= other.y - tolerancePx &&
        x + width <= other.x + other.width + tolerancePx &&
        y + height <= other.y + other.height + tolerancePx

private fun UiSlot.overlaps(other: UiSlot, tolerancePx: Float): Boolean =
    x < other.x + other.width - tolerancePx &&
        x + width > other.x + tolerancePx &&
        y < other.y + other.height - tolerancePx &&
        y + height > other.y + tolerancePx

private fun describeNode(node: UiSemanticNode): String =
    "${node.role}${node.id?.let { "[$it]" } ?: ""}@${node.bounds}"
