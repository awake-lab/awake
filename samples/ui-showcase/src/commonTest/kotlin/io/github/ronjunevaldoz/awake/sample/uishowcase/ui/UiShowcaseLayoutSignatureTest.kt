// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.testing.ui.describeLayout
import io.github.ronjunevaldoz.awake.testing.ui.layoutSignature
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Whole-page layout fingerprints, without a recorded matrix to maintain.
 *
 * This used to compare 54 per-page hex constants committed in source. That is a change detector,
 * not a correctness oracle -- it says something moved, never whether what moved is right -- and its
 * failure mode is that an intentional change invalidates all 54 at once, so the "review" is a bulk
 * paste nobody reads. It was regenerated wholesale four times in a single session.
 *
 * Correctness now has a real oracle: ShadcnGeometryParityTest compares Awake's bounds against
 * shadcn's own getBoundingClientRect numbers. What is left for fingerprints is the job they are
 * actually good at, and neither needs a baseline:
 *
 *  - the same page rendered twice must fingerprint the same (nondeterminism -- iteration order,
 *    hash order, uninitialised state -- shows up here and nowhere else)
 *  - two different pages must not share a fingerprint, which is how the old catalog hid five
 *    fixtures all rendering the Introduction page
 *
 * Cross-target drift is the one thing the recorded matrix caught that this does not. It caught it
 * only transitively -- every target compared against the same constant -- and buying that back
 * costs a matrix that gets pasted over unread. Worth re-adding as a real cross-target comparison
 * if a target ever diverges; not worth 54 constants on the chance that it might.
 */
class UiShowcaseLayoutSignatureTest {

    @Test
    fun everyPageLayoutIsDeterministic() {
        val drift = UiShowcasePreviewEntries.mapNotNull { entry ->
            val first = layoutSignature(entry.render(entry.metadata).semantics)
            val secondFrame = entry.render(entry.metadata)
            val second = layoutSignature(secondFrame.semantics)
            if (first == second) {
                null
            } else {
                "${entry.metadata.id}: 0x${first.toString(16)} then 0x${second.toString(16)}\n" +
                    describeLayout(secondFrame.semantics)
            }
        }
        assertEquals(
            emptyList(),
            drift,
            "A page fingerprinted differently on a second render in the same process, so its " +
                "layout depends on something other than its inputs:\n${drift.joinToString("\n\n")}",
        )
    }

    /** A page whose fingerprint equals another page's is almost always a dispatch bug, not a
     * coincidence -- that is exactly how the old catalog hid five fixtures rendering the
     * Introduction page. */
    @Test
    fun everyPageProducesADistinctLayout() {
        val bySignature = UiShowcasePreviewEntries
            .groupBy { layoutSignature(it.render(it.metadata).semantics) }
            .filterValues { it.size > 1 }
            .mapValues { (_, entries) -> entries.map { it.page.id } }
        assertEquals(emptyMap(), bySignature, "Pages share a layout fingerprint: $bySignature")
    }
}
