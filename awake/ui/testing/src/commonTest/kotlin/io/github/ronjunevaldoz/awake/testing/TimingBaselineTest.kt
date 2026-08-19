// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.testing

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TimingBaselineTest {

    @Test
    fun withinToleranceMatches() {
        val result = compareTimings(
            actual = mapOf("opaque-pass" to 10.5),
            baseline = mapOf("opaque-pass" to 10.0),
            toleranceRatio = 0.25,
        )
        assertTrue(result.matches, result.summary)
    }

    @Test
    fun realRegressionFails() {
        val result = compareTimings(
            actual = mapOf("opaque-pass" to 20.0),
            baseline = mapOf("opaque-pass" to 10.0),
            toleranceRatio = 0.25,
        )
        assertFalse(result.matches)
        assertEquals(listOf("opaque-pass"), result.regressions.map { it.label })
        assertEquals(2.0, result.regressions.single().ratio)
        assertTrue("opaque-pass" in result.summary, result.summary)
    }

    @Test
    fun tinySpanDoublingIsNoiseNotRegression() {
        // 0.01ms -> 0.02ms is 100% "slower" and completely meaningless. The absolute floor is
        // what stops that from being a CI failure.
        val result = compareTimings(
            actual = mapOf("ui-pass" to 0.02),
            baseline = mapOf("ui-pass" to 0.01),
            toleranceRatio = 0.25,
            noiseFloorMs = 0.05,
        )
        assertTrue(result.matches, result.summary)
    }

    @Test
    fun gettingFasterIsNotAFailure() {
        val result = compareTimings(
            actual = mapOf("shadow-pass" to 1.0),
            baseline = mapOf("shadow-pass" to 12.0),
        )
        assertTrue(result.matches, result.summary)
    }

    @Test
    fun baselineLabelThatStoppedBeingMeasuredFails() {
        val result = compareTimings(
            actual = mapOf("opaque-pass" to 10.0),
            baseline = mapOf("opaque-pass" to 10.0, "shadow-pass" to 12.0),
        )
        assertFalse(result.matches, "a pass that silently stopped being timed must not read as a pass")
        assertEquals(listOf("shadow-pass"), result.missingLabels)
    }

    @Test
    fun newLabelWithNoBaselineIsIgnored() {
        val result = compareTimings(
            actual = mapOf("opaque-pass" to 10.0, "brand-new-pass" to 99.0),
            baseline = mapOf("opaque-pass" to 10.0),
        )
        assertTrue(result.matches, "a newly added pass has nothing to compare against yet")
    }

    @Test
    fun baselineTextRoundTrips() {
        val means = mapOf("ui-pass" to 0.4567, "opaque-pass" to 10.1234)
        val text = formatTimingBaseline(means, note = "recorded on MoltenVK/macOS")

        assertTrue(text.startsWith("# recorded on MoltenVK/macOS\n"), text)
        val firstEntry = text.lines().first { it.isNotBlank() && !it.startsWith("#") }
        assertTrue(
            firstEntry.startsWith("opaque-pass="),
            "labels must be sorted so a re-record diffs cleanly: $text",
        )

        val parsed = parseTimingBaseline(text)
        assertEquals(setOf("opaque-pass", "ui-pass"), parsed.keys)
        assertEquals(10.123, parsed.getValue("opaque-pass"))
        assertEquals(0.457, parsed.getValue("ui-pass"))
    }

    @Test
    fun malformedBaselineLineThrows() {
        assertFailsWith<IllegalArgumentException> { parseTimingBaseline("opaque-pass 10.0") }
        assertFailsWith<IllegalArgumentException> { parseTimingBaseline("opaque-pass=fast") }
    }

    @Test
    fun spansFeedComparisonDirectly() {
        val spans = FrameSpans()
        spans.span("pass") { }
        val result = compareTimings(spans.meansMs(), parseTimingBaseline(formatTimingBaseline(spans.meansMs())))
        assertTrue(result.matches, result.summary)
    }
}
