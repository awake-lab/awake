// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.testing.ui

import io.github.ronjunevaldoz.awake.ui.UiSemanticNode
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.UiSlot

data class AwakeUiPreviewOverlapRule(
    val label: String,
    val nodeIds: Set<String>,
    val tolerancePx: Float = 1f
)

data class AwakeUiPreviewValidationConfig(
    val requireSemantics: Boolean = true,
    val allowTruncatedTextIds: Set<String> = emptySet(),
    val requiredNodeIds: Set<String> = emptySet(),
    val contentFitTolerancePx: Float = 1f,
    val overlapRules: List<AwakeUiPreviewOverlapRule> = emptyList()
)

data class AwakeUiPreviewValidationReport(
    val issues: List<String>
) {
    val isClean: Boolean get() = issues.isEmpty()

    fun summary(): String = if (issues.isEmpty()) {
        "No UI preview issues."
    } else {
        issues.joinToString(separator = "\n")
    }

    fun requireClean() {
        check(isClean) { summary() }
    }
}

fun validateAwakeUiPreview(
    scene: AwakeUiPreviewScene,
    config: AwakeUiPreviewValidationConfig = AwakeUiPreviewValidationConfig()
): AwakeUiPreviewValidationReport = validateAwakeUiPreview(
    metadata = scene.metadata,
    frame = AwakeUiPreviewFrame(
        primitives = scene.primitives,
        background = scene.background,
        font = scene.font,
        semantics = scene.semantics
    ),
    config = config
)

fun validateAwakeUiPreview(
    metadata: AwakeUiPreviewMetadata,
    frame: AwakeUiPreviewFrame,
    config: AwakeUiPreviewValidationConfig = AwakeUiPreviewValidationConfig()
): AwakeUiPreviewValidationReport {
    val issues = ArrayList<String>()
    val frameBounds = UiSlot(0f, 0f, metadata.width.toFloat(), metadata.height.toFloat())

    val visualReport = inspectUiFrame(
        primitives = frame.primitives,
        frame = frameBounds,
        font = frame.font
    )
    if (!visualReport.isClean) {
        issues += "[${metadata.id}] ${visualReport.summary()}"
    }

    val semantics = frame.semantics
    if (config.requireSemantics && semantics.isEmpty()) {
        issues += "[${metadata.id}] preview emitted no semantic nodes"
    }

    val semanticReports = listOf(
        inspectSemanticNodes(semantics),
        inspectSemanticContentFit(semantics, tolerancePx = config.contentFitTolerancePx),
        inspectTextTruncation(semantics, allowIds = config.allowTruncatedTextIds)
    )
    semanticReports
        .filterNot(UiSemanticReport::isClean)
        .forEach { report -> issues += "[${metadata.id}] ${report.summary()}" }

    config.requiredNodeIds.forEach { nodeId ->
        if (semantics.none { it.id == nodeId }) {
            issues += "[${metadata.id}] missing required semantic node '$nodeId'"
        }
    }

    config.overlapRules.forEach { rule ->
        val nodes = rule.nodeIds.mapNotNull { nodeId ->
            semantics.firstOrNull { it.id == nodeId } ?: run {
                issues += "[${metadata.id}] overlap rule '${rule.label}' referenced missing node '$nodeId'"
                null
            }
        }
        if (nodes.size >= 2) {
            val overlapReport = inspectSemanticOverlaps(
                label = rule.label,
                nodes = nodes,
                tolerancePx = rule.tolerancePx
            )
            if (!overlapReport.isClean) {
                issues += "[${metadata.id}] ${overlapReport.summary()}"
            }
        }
    }

    return AwakeUiPreviewValidationReport(issues)
}

fun requirePreviewNode(
    semantics: List<UiSemanticNode>,
    id: String,
    role: UiSemanticRole? = null
): UiSemanticNode = requireSemanticNode(semantics, id, role)
