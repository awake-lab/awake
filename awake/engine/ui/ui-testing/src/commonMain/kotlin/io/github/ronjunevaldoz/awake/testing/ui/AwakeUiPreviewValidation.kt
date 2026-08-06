// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.testing.ui

import io.github.ronjunevaldoz.awake.ui.UiSemanticNode
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds

/**
 * Sentinel: pass as [AwakeUiPreviewValidationConfig.checkCenteredTextIds] to assert centering
 * for ALL text nodes in the frame rather than a named subset.
 */
val ALL_TEXT_NODES: Set<String> = object : AbstractSet<String>() {
    override val size: Int get() = Int.MAX_VALUE
    override fun contains(element: String): Boolean = true
    override fun iterator(): Iterator<String> = emptySet<String>().iterator()
}

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
    val overlapRules: List<AwakeUiPreviewOverlapRule> = emptyList(),
    /**
     * When non-empty, [inspectTextCentering] is run and only the node IDs in this set
     * are exempted from the centering assertion. Pass [ALL_TEXT_NODES] to check every
     * Text node in the frame.
     *
     * Empty (the default) = centering check is OFF, preserving backward-compat for all
     * existing previews that do not need it.
     */
    val checkCenteredTextIds: Set<String> = emptySet(),
    /** Tolerance in px for the centering check (default 1 px). */
    val centeringTolerancePx: Float = 1f,
    /**
     * When > 0, [inspectPadding] is run: every Panel/Text node's contentBounds must be
     * inset from its own bounds by at least this many pixels on all four sides.
     * 0 (the default) = padding check is OFF.
     */
    val minContentPaddingPx: Float = 0f,
    /** IDs of nodes exempt from the padding check (e.g. full-bleed hero panels). */
    val paddingAllowIds: Set<String> = emptySet(),
    /**
     * Spacing rules: each entry names a group of sibling nodes and the minimum gap
     * required between them. Empty (the default) = spacing check is OFF.
     *
     * Example:
     * ```kotlin
     * spacingRules = listOf(
     *     AwakeUiPreviewSpacingRule("OTP slots", setOf("otp.slot.0",…"otp.slot.5"), minGapPx = 8f)
     * )
     * ```
     */
    val spacingRules: List<AwakeUiPreviewSpacingRule> = emptyList(),

    /** Assert exact pixel dimensions from Figma */
    val dimensionRules: List<AwakeUiPreviewDimensionRule> = emptyList(),

    /** Assert that specific semantic nodes use the correct design tokens */
    val tokenRules: List<AwakeUiPreviewTokenRule> = emptyList(),

    /** Assert exact content padding from Figma */
    val exactPaddingRules: List<AwakeUiPreviewExactPaddingRule> = emptyList(),

    /** Assert exact sibling spacing from Figma */
    val exactSpacingRules: List<AwakeUiPreviewExactSpacingRule> = emptyList()
)

/** Assert that specific semantic nodes use the correct design tokens */
data class AwakeUiPreviewTokenRule(
    val nodeId: String,
    val expectedBackgroundToken: String? = null,
    val expectedForegroundToken: String? = null,
    val expectedBorderToken: String? = null,
    val expectedTextStyleToken: String? = null
)

/** Assert exact pixel dimensions from Figma */
data class AwakeUiPreviewDimensionRule(
    val nodeId: String,
    val exactHeight: Float? = null,
    val exactWidth: Float? = null,
    val tolerancePx: Float = 0.5f
)

/** Assert exact content padding from Figma */
data class AwakeUiPreviewExactPaddingRule(
    val nodeId: String,
    val exactPaddingPx: Float,
    val tolerancePx: Float = 0.5f
)

/** Assert exact sibling spacing from Figma */
data class AwakeUiPreviewExactSpacingRule(
    val label: String,
    val nodeIds: Set<String>,
    val exactGapPx: Float,
    val axis: SpacingAxis? = null,
    val tolerancePx: Float = 0.5f
)

/** Declares a minimum-gap rule for a named set of sibling semantic nodes. */
data class AwakeUiPreviewSpacingRule(
    val label: String,
    val nodeIds: Set<String>,
    val minGapPx: Float,
    val axis: SpacingAxis? = null
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
    val frameBounds = UiBounds(0f, 0f, metadata.width.toFloat(), metadata.height.toFloat())

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

    val semanticReports = buildList {
        add(inspectSemanticNodes(semantics))
        add(inspectSemanticContentFit(semantics, tolerancePx = config.contentFitTolerancePx))
        add(inspectTextTruncation(semantics, allowIds = config.allowTruncatedTextIds))
        // Centering check: only runs when the caller has explicitly opted in via
        // checkCenteredTextIds. An empty set skips the check entirely (safe default).
        if (config.checkCenteredTextIds.isNotEmpty()) {
            val allowExempt = if (config.checkCenteredTextIds === ALL_TEXT_NODES) emptySet()
                             else semantics.mapNotNull { it.id }.toSet() - config.checkCenteredTextIds
            add(inspectTextCentering(
                nodes = semantics,
                tolerancePx = config.centeringTolerancePx,
                allowIds = allowExempt
            ))
        }
    }
    semanticReports
        .filterNot(UiSemanticReport::isClean)
        .forEach { report -> issues += "[${metadata.id}] ${report.summary()}" }

    config.requiredNodeIds.forEach { nodeId ->
        if (semantics.none { it.id == nodeId }) {
            issues += "[${metadata.id}] missing required semantic node '$nodeId'"
        }
    }

    // Padding check
    if (config.minContentPaddingPx > 0f) {
        val paddingReport = inspectPadding(
            nodes = semantics,
            minPaddingPx = config.minContentPaddingPx,
            allowIds = config.paddingAllowIds
        )
        if (!paddingReport.isClean) {
            issues += "[${metadata.id}] ${paddingReport.summary()}"
        }
    }

    // Spacing checks
    config.spacingRules.forEach { rule ->
        val nodes = rule.nodeIds.mapNotNull { nodeId ->
            semantics.firstOrNull { it.id == nodeId } ?: run {
                issues += "[${metadata.id}] spacing rule '${rule.label}' referenced missing node '$nodeId'"
                null
            }
        }
        if (nodes.size >= 2) {
            val spacingReport = inspectSpacing(
                label = rule.label,
                nodes = nodes,
                minGapPx = rule.minGapPx,
                axis = rule.axis
            )
            if (!spacingReport.isClean) {
                issues += "[${metadata.id}] ${spacingReport.summary()}"
            }
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

    // Exact Dimension checks
    config.dimensionRules.forEach { rule ->
        val node = semantics.firstOrNull { it.id == rule.nodeId } ?: run {
            issues += "[${metadata.id}] dimension rule referenced missing node '${rule.nodeId}'"
            null
        }
        if (node != null) {
            val dimensionReport = inspectDimensions(
                nodes = listOf(node),
                exactHeight = rule.exactHeight,
                exactWidth = rule.exactWidth,
                tolerancePx = rule.tolerancePx
            )
            if (!dimensionReport.isClean) {
                issues += "[${metadata.id}] ${dimensionReport.summary()}"
            }
        }
    }

    // Token checks
    config.tokenRules.forEach { rule ->
        val node = semantics.firstOrNull { it.id == rule.nodeId } ?: run {
            issues += "[${metadata.id}] token rule referenced missing node '${rule.nodeId}'"
            null
        }
        if (node != null) {
            val tokenReport = inspectTokens(
                nodes = listOf(node),
                expectedBackgroundToken = rule.expectedBackgroundToken,
                expectedForegroundToken = rule.expectedForegroundToken,
                expectedBorderToken = rule.expectedBorderToken,
                expectedTextStyleToken = rule.expectedTextStyleToken
            )
            if (!tokenReport.isClean) {
                issues += "[${metadata.id}] ${tokenReport.summary()}"
            }
        }
    }

    // Exact Padding checks
    config.exactPaddingRules.forEach { rule ->
        val node = semantics.firstOrNull { it.id == rule.nodeId } ?: run {
            issues += "[${metadata.id}] exact padding rule referenced missing node '${rule.nodeId}'"
            null
        }
        if (node != null) {
            val paddingReport = inspectExactPadding(
                nodes = listOf(node),
                expectedPaddingPx = rule.exactPaddingPx,
                tolerancePx = rule.tolerancePx
            )
            if (!paddingReport.isClean) {
                issues += "[${metadata.id}] ${paddingReport.summary()}"
            }
        }
    }

    // Exact Spacing checks
    config.exactSpacingRules.forEach { rule ->
        val nodes = rule.nodeIds.mapNotNull { nodeId ->
            semantics.firstOrNull { it.id == nodeId } ?: run {
                issues += "[${metadata.id}] exact spacing rule '${rule.label}' referenced missing node '$nodeId'"
                null
            }
        }
        if (nodes.size >= 2) {
            val spacingReport = inspectExactSpacing(
                label = rule.label,
                nodes = nodes,
                expectedGapPx = rule.exactGapPx,
                axis = rule.axis,
                tolerancePx = rule.tolerancePx
            )
            if (!spacingReport.isClean) {
                issues += "[${metadata.id}] ${spacingReport.summary()}"
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
