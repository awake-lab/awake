// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.testing

import kotlin.math.round

/** One label that got slower than its checked-in baseline by more than the allowed tolerance. */
data class TimingRegression(
    val label: String,
    val baselineMs: Double,
    val actualMs: Double,
) {
    /** How much slower, as a multiple -- 1.0 is exactly the baseline. */
    val ratio: Double get() = if (baselineMs == 0.0) Double.POSITIVE_INFINITY else actualMs / baselineMs
}

/**
 * Result of comparing a run's per-label mean frame/pass times against a golden baseline -- the
 * timing counterpart of [PixelDiffResult]. [matches] is what a test asserts on; [summary] is the
 * failure message.
 */
data class TimingDiffResult(
    val matches: Boolean,
    val regressions: List<TimingRegression>,
    val missingLabels: List<String>,
) {
    val summary: String
        get() = buildString {
            if (matches) {
                append("all spans within tolerance")
                return@buildString
            }
            if (missingLabels.isNotEmpty()) {
                append("baseline labels never measured: ${missingLabels.joinToString()}")
                append(" (the pass was removed or renamed, or its span never ran)")
                if (regressions.isNotEmpty()) append("; ")
            }
            regressions.forEach { regression ->
                append(
                    "'${regression.label}' ${formatMs(regression.actualMs)}ms vs baseline " +
                        "${formatMs(regression.baselineMs)}ms (${formatMs(regression.ratio)}x); ",
                )
            }
        }
}

/**
 * Compares a run's per-label mean times (see [FrameSpans.meansMs]) against a checked-in
 * [baseline], reporting only *regressions* -- getting faster is never a failure, though it is
 * worth re-recording the baseline for.
 *
 * A label fails when it is both more than [toleranceRatio] slower *and* more than [noiseFloorMs]
 * slower in absolute terms. Both conditions, not either: a relative check alone turns a
 * 0.01ms -> 0.02ms scheduling blip into a "100% regression", and an absolute check alone can't be
 * shared between a 0.2ms UI pass and a 12ms shadow pass. The floor is what makes this usable as a
 * CI gate instead of a flake generator.
 *
 * A baseline label missing from [actual] fails too -- that means a pass stopped being measured,
 * which reads as "no regression" to any comparison that only walks what it was given.
 *
 * Labels in [actual] but not in [baseline] are ignored: a newly added pass has nothing to compare
 * against yet, and failing on it would block the very commit that adds it.
 *
 * Pure/common-code, no file I/O and no test-framework dependency, same as [comparePixels] -- each
 * consumer loads its own baseline resource however its platform prefers.
 */
fun compareTimings(
    actual: Map<String, Double>,
    baseline: Map<String, Double>,
    toleranceRatio: Double = 0.25,
    noiseFloorMs: Double = 0.05,
): TimingDiffResult {
    require(toleranceRatio >= 0.0) { "toleranceRatio must be >= 0, was $toleranceRatio" }
    val missing = baseline.keys.filter { it !in actual }
    val regressions = baseline.mapNotNull { (label, baselineMs) ->
        val actualMs = actual[label] ?: return@mapNotNull null
        val overBudget = actualMs - baselineMs
        val exceedsRatio = actualMs > baselineMs * (1.0 + toleranceRatio)
        if (exceedsRatio && overBudget > noiseFloorMs) {
            TimingRegression(label, baselineMs = baselineMs, actualMs = actualMs)
        } else {
            null
        }
    }
    return TimingDiffResult(
        matches = regressions.isEmpty() && missing.isEmpty(),
        regressions = regressions,
        missingLabels = missing,
    )
}

/**
 * Renders [meansMs] as the checked-in baseline text -- `label=ms` lines, sorted by label so a
 * re-record produces a reviewable diff rather than a reordered file.
 *
 * [note] becomes a leading `#` comment. Pass one: unlike a pixel baseline, a timing baseline is
 * only meaningful next to the machine it was recorded on, and a reviewer looking at a number
 * change has no other way to tell "slower" from "recorded on different hardware".
 *
 * Stored as text, not bytes, for the one thing pixel baselines can't do: a human can read the
 * diff. Otherwise this follows the same convention as `baselines/cube-headless.rgba` -- a
 * committed resource, re-recorded under `-DAWAKE_RECORD_SNAPSHOTS=true`.
 */
fun formatTimingBaseline(meansMs: Map<String, Double>, note: String? = null): String = buildString {
    if (note != null) appendLine("# $note")
    meansMs.entries.sortedBy { it.key }.forEach { (label, ms) ->
        appendLine("$label=${formatMs(ms)}")
    }
}

/** Parses [formatTimingBaseline]'s output. Blank lines and `#` comments are skipped. */
fun parseTimingBaseline(text: String): Map<String, Double> =
    text.lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") }
        .associate { line ->
            val separator = line.indexOf('=')
            require(separator > 0) { "malformed timing baseline line: '$line' (expected label=ms)" }
            val label = line.substring(0, separator).trim()
            val ms = line.substring(separator + 1).trim().toDoubleOrNull()
            requireNotNull(ms) { "malformed timing baseline line: '$line' (ms is not a number)" }
            label to ms
        }

/** 3 decimals, no `String.format` (JVM-only) -- microsecond resolution is finer than any of these
 * harnesses can actually resolve, and trailing noise digits make baseline diffs unreadable. */
private fun formatMs(ms: Double): String = (round(ms * MS_ROUNDING) / MS_ROUNDING).toString()

private const val MS_ROUNDING = 1000.0
