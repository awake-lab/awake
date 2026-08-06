// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.testing.ui

/** Design system theme mode representation in Figma validation matrix. */
enum class FigmaMode {
    Light,
    Dark
}

/** Display density scale representation in Figma validation matrix. */
enum class FigmaDensity(val densityScale: Float) {
    Default(1.0f),
    HighDensity(2.0f)
}

/** Layout scale multiplier representation in Figma validation matrix. */
enum class FigmaScale(val scale: Float) {
    Scale1_0x(1.0f),
    Scale1_5x(1.5f),
    Scale2_0x(2.0f)
}

/** A single configuration combination within the Figma Mode Matrix. */
data class FigmaMatrixConfig(
    val mode: FigmaMode,
    val density: FigmaDensity,
    val scale: FigmaScale
) {
    val id: String get() = "${mode.name.lowercase()}-${density.name.lowercase()}-${scale.scale}x"
}

/**
 * Automation runner for running UI Previews against the Cartesian product of:
 * - Modes: Light, Dark
 * - Densities: Default (1.0x), High-Density (2.0x)
 * - Scales: 1.0x, 1.5x, 2.0x
 *
 * Collects all identified semantic/fidelity issues into a single [UiSemanticReport].
 */
object FigmaModeMatrix {
    val defaultMatrix: List<FigmaMatrixConfig> = buildList {
        for (mode in FigmaMode.entries) {
            for (density in FigmaDensity.entries) {
                for (scale in FigmaScale.entries) {
                    add(FigmaMatrixConfig(mode, density, scale))
                }
            }
        }
    }

    /**
     * Runs the given [previewRunner] over each variation in [matrix] and collects all results
     * into a single [UiSemanticReport].
     */
    /**
     * Runs the given [previewRunner] over each variation in [matrix] and collects all results
     * into a single [UiSemanticReport].
     */
    fun runMatrix(
        matrix: List<FigmaMatrixConfig> = defaultMatrix,
        previewRunner: (config: FigmaMatrixConfig) -> UiSemanticReport
    ): UiSemanticReport {
        val aggregatedIssues = ArrayList<UiSemanticIssue>()
        for (config in matrix) {
            val report = previewRunner(config)
            report.issues.forEach { issue ->
                aggregatedIssues.add(
                    issue.copy(
                        message = "[${config.id}] ${issue.message}"
                    )
                )
            }
        }
        return UiSemanticReport(aggregatedIssues)
    }

    /**
     * Runs [previewRunner] returning [AwakeUiPreviewValidationReport] over each variation in [matrix]
     * and collects all validation issues into a single [UiSemanticReport].
     */
    fun runValidationMatrix(
        matrix: List<FigmaMatrixConfig> = defaultMatrix,
        previewRunner: (config: FigmaMatrixConfig) -> AwakeUiPreviewValidationReport
    ): UiSemanticReport {
        val aggregatedIssues = ArrayList<UiSemanticIssue>()
        for (config in matrix) {
            val report = previewRunner(config)
            report.issues.forEach { issueStr ->
                aggregatedIssues.add(
                    UiSemanticIssue(
                        kind = UiSemanticIssueKind.MismatchedToken,
                        nodeId = config.id,
                        message = "[${config.id}] $issueStr"
                    )
                )
            }
        }
        return UiSemanticReport(aggregatedIssues)
    }
}
