// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.demos

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame

/**
 * Fast, GPU-free unit test for [GltfViewerDemo.scalePositions] -- the actual mechanism behind the
 * Duck-flicker fix (see [GltfViewerDeterminismTest]'s own doc comment for the full investigation).
 * Bakes a mesh's scale into its own vertex buffer once at load time instead of recombining a tiny
 * scale factor with large raw coordinates in a GPU matrix multiply every frame; this test only
 * needs to prove the CPU-side math is correct (positions scaled, everything else untouched,
 * source left alone), not spin up a headless renderer to do it.
 */
class GltfViewerScalePositionsTest {

    @Test
    fun scalesOnlyPositionComponentsLeavingNormalAndColorUntouched() {
        // One vertex: position(1, 2, 3), normal(0, 1, 0), color(1, 0.5, 0.25) -- the 9-float
        // position+normal+color stride GltfMesh.toInterleavedPositionNormalColor() produces.
        val source = floatArrayOf(1f, 2f, 3f, 0f, 1f, 0f, 1f, 0.5f, 0.25f)

        val result = GltfViewerDemo.scalePositions(source, 2f)

        assertEquals(
            floatArrayOf(2f, 4f, 6f, 0f, 1f, 0f, 1f, 0.5f, 0.25f).toList(),
            result.toList(),
            "position scaled by 2x, normal and color left exactly as they were"
        )
    }

    @Test
    fun scalesEveryVertexInAMultiVertexBuffer() {
        // Two vertices back to back -- confirms the stride walk doesn't drift after the first one.
        val source = floatArrayOf(
            1f, 0f, 0f, 0f, 0f, 1f, 1f, 1f, 1f,
            0f, 1f, 0f, 1f, 0f, 0f, 0f, 0f, 0f
        )

        val result = GltfViewerDemo.scalePositions(source, 0.5f)

        assertEquals(
            floatArrayOf(
                0.5f, 0f, 0f, 0f, 0f, 1f, 1f, 1f, 1f,
                0f, 0.5f, 0f, 1f, 0f, 0f, 0f, 0f, 0f
            ).toList(),
            result.toList()
        )
    }

    @Test
    fun neverMutatesTheSourceArray() {
        // Both rawInterleaved and normalizedInterleaved need to stay independently valid for as
        // long as normalizeScale can still be toggled back -- see scalePositions's own doc
        // comment for why an in-place scale would silently break that.
        val source = floatArrayOf(10f, 10f, 10f, 0f, 1f, 0f, 1f, 1f, 1f)
        val sourceCopyForComparison = source.copyOf()

        val result = GltfViewerDemo.scalePositions(source, 3f)

        assertEquals(sourceCopyForComparison.toList(), source.toList(), "source array must be untouched")
        assertNotSame(source, result, "result must be a distinct array, not the mutated source")
    }
}
