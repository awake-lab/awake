// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.geometry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** A closed, watertight octahedron -- 6 vertices, 8 triangles, no boundary edges -- simple
 * enough to hand-verify, "closed manifold" enough to exercise the collapse loop without
 * hitting the boundary-quadric path (that's a separate concern, exercised by
 * [openMeshBoundaryStaysManifold]'s flat grid instead). */
private val octahedronPositions = floatArrayOf(
    1f, 0f, 0f,
    -1f, 0f, 0f,
    0f, 1f, 0f,
    0f, -1f, 0f,
    0f, 0f, 1f,
    0f, 0f, -1f,
)
private val octahedronIndices = intArrayOf(
    0, 2, 4,
    2, 1, 4,
    1, 3, 4,
    3, 0, 4,
    2, 0, 5,
    1, 2, 5,
    3, 1, 5,
    0, 3, 5,
)

class MeshSimplifierTest {
    @Test
    fun aggressiveRatioReducesTriangleCountToTheMinimumFloor() {
        val result = MeshSimplifier.simplify(octahedronPositions, octahedronIndices, targetTriangleRatio = 0.1f)

        val triangleCount = result.indices.size / 3
        assertEquals(4, triangleCount, "target is max(MIN_TRIANGLES=4, round(8 * 0.1)) = 4")
        assertNoDegenerateTriangles(result.indices)
        assertValidIndices(result)
        assertValidRemap(result, originalVertexCount = octahedronPositions.size / 3)
    }

    @Test
    fun ratioOfOneLeavesTheMeshUnchanged() {
        val result = MeshSimplifier.simplify(octahedronPositions, octahedronIndices, targetTriangleRatio = 1f)

        assertEquals(8, result.indices.size / 3, "target already met before the collapse loop starts")
        assertEquals(6, result.positions.size / 3)
    }

    @Test
    fun midRatioReducesTriangleCountWithoutExceedingTheOriginal() {
        val result = MeshSimplifier.simplify(octahedronPositions, octahedronIndices, targetTriangleRatio = 0.5f)

        val triangleCount = result.indices.size / 3
        assertTrue(triangleCount in 4..8, "expected between the floor (4) and the original (8), got $triangleCount")
        assertNoDegenerateTriangles(result.indices)
        assertValidIndices(result)
    }

    @Test
    fun openMeshBoundaryStaysManifold() {
        // A 3x3 flat grid (9 vertices, 8 triangles) -- has boundary edges on all four sides,
        // exercising the boundary-quadric path without a hand-verifiable exact triangle count.
        val positions = FloatArray(9 * 3)
        var v = 0
        for (y in 0..2) {
            for (x in 0..2) {
                positions[v * 3] = x.toFloat()
                positions[v * 3 + 1] = 0f
                positions[v * 3 + 2] = y.toFloat()
                v += 1
            }
        }
        fun index(x: Int, y: Int) = y * 3 + x
        val indices = mutableListOf<Int>()
        for (y in 0..1) {
            for (x in 0..1) {
                indices += listOf(index(x, y), index(x + 1, y), index(x, y + 1))
                indices += listOf(index(x + 1, y), index(x + 1, y + 1), index(x, y + 1))
            }
        }

        val result = MeshSimplifier.simplify(positions, indices.toIntArray(), targetTriangleRatio = 0.5f)

        assertNoDegenerateTriangles(result.indices)
        assertValidIndices(result)
        assertValidRemap(result, originalVertexCount = 9)
    }

    private fun assertNoDegenerateTriangles(indices: IntArray) {
        for (t in indices.indices step 3) {
            val i0 = indices[t]
            val i1 = indices[t + 1]
            val i2 = indices[t + 2]
            assertTrue(i0 != i1 && i1 != i2 && i0 != i2, "degenerate triangle at index $t: ($i0, $i1, $i2)")
        }
    }

    private fun assertValidIndices(result: MeshSimplifier.Result) {
        val vertexCount = result.positions.size / 3
        for (index in result.indices) {
            assertTrue(index in 0 until vertexCount, "index $index out of range for $vertexCount vertices")
        }
    }

    private fun assertValidRemap(result: MeshSimplifier.Result, originalVertexCount: Int) {
        assertEquals(originalVertexCount, result.vertexRemap.size)
        val newVertexCount = result.positions.size / 3
        for (mapped in result.vertexRemap) {
            assertTrue(mapped in 0 until newVertexCount, "remap target $mapped out of range for $newVertexCount vertices")
        }
    }
}
