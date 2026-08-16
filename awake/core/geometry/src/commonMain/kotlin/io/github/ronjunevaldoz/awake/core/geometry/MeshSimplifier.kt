// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.geometry

import kotlin.math.roundToInt

/**
 * Garland-Heckbert quadric-error-metric edge-collapse mesh simplification -- position-only
 * (no UV/normal-seam awareness yet; a caller downsampling other per-vertex attributes uses
 * [Result.vertexRemap] to pick each surviving vertex's own attribute values, not a
 * barycentric re-projection -- see this file's own scope note below).
 *
 * Takes and returns the same `positions: FloatArray` + `indices: IntArray` shape
 * `MeshGeometry`/`GltfMesh` already use -- no new geometry representation, no dependency on
 * any asset format. Works on any mesh from any source.
 *
 * ponytail: the active-edge scan in [simplify] is O(edges) per collapse (a real priority
 * queue would be O(log edges)) -- fine for the mesh sizes this is exercised against today;
 * upgrade to a binary heap if this becomes a bottleneck on much larger meshes. Attribute
 * carry-over is nearest-vertex only (via [Result.vertexRemap]), not re-interpolated;
 * barycentric re-projection is the upgrade path once visual accuracy at aggressive ratios
 * actually matters.
 */
object MeshSimplifier {
    private const val MIN_TRIANGLES = 4
    private const val DEGENERATE_NORMAL_LENGTH = 1e-12
    private const val SINGULAR_DETERMINANT = 1e-12
    private const val BOUNDARY_QUADRIC_WEIGHT = 1000.0

    /**
     * [positions] reduced to their surviving vertices only, [indices] rewritten against them.
     * [vertexRemap] has one entry per ORIGINAL vertex (`vertexRemap.size ==
     * originalPositions.size / 3`): `vertexRemap[originalIndex]` is the index into this
     * result's own [positions] that original vertex collapsed into (or kept, if it survived
     * unmoved). A caller downsampling a parallel per-vertex array (normals, UVs, colors)
     * builds one representative original index per surviving vertex from this map, then reads
     * that original array by it.
     */
    data class Result(
        val positions: FloatArray,
        val indices: IntArray,
        val vertexRemap: IntArray,
    )

    /**
     * Simplifies [positions]/[indices] down to roughly `originalTriangleCount *
     * [targetTriangleRatio]` triangles (never below [MIN_TRIANGLES], never more than the
     * input has). `targetTriangleRatio` outside `0f..1f` is clamped, not an error -- `0f`
     * simplifies as aggressively as this algorithm can (down to [MIN_TRIANGLES]), `1f` (or
     * above) returns the input essentially unchanged (the collapse loop's target is already
     * met before it starts).
     */
    fun simplify(positions: FloatArray, indices: IntArray, targetTriangleRatio: Float): Result {
        val vertexCount = positions.size / 3
        val ratio = targetTriangleRatio.coerceIn(0f, 1f)
        val originalTriangleCount = indices.size / 3

        val working = DoubleArray(positions.size) { positions[it].toDouble() }
        val quadrics = Array(vertexCount) { DoubleArray(10) }
        val vertexTriangles = Array(vertexCount) { mutableSetOf<Int>() }
        val triangleAlive = BooleanArray(originalTriangleCount)
        val triangleVertices = IntArray(originalTriangleCount * 3) { indices[it] }
        val edgeTriangleCount = HashMap<Pair<Int, Int>, Int>()

        var validTriangleCount = 0
        for (t in 0 until originalTriangleCount) {
            val i0 = triangleVertices[t * 3]
            val i1 = triangleVertices[t * 3 + 1]
            val i2 = triangleVertices[t * 3 + 2]
            val plane = trianglePlane(working, i0, i1, i2) ?: continue
            triangleAlive[t] = true
            validTriangleCount += 1
            vertexTriangles[i0] += t
            vertexTriangles[i1] += t
            vertexTriangles[i2] += t
            addPlaneQuadric(quadrics[i0], plane)
            addPlaneQuadric(quadrics[i1], plane)
            addPlaneQuadric(quadrics[i2], plane)
            for (edge in triangleEdges(i0, i1, i2)) {
                edgeTriangleCount[edge] = (edgeTriangleCount[edge] ?: 0) + 1
            }
        }

        // Boundary edges (used by exactly one triangle) get a large penalty quadric on both
        // endpoints so an open mesh's border doesn't visibly shrink inward as interior
        // triangles collapse around it.
        for ((edge, count) in edgeTriangleCount) {
            if (count != 1) continue
            val boundary = boundaryQuadric(working, edge.first, edge.second) ?: continue
            val boundaryQuadricMatrix = planeOuterProduct(boundary)
            addWeightedQuadric(quadrics[edge.first], boundaryQuadricMatrix, BOUNDARY_QUADRIC_WEIGHT)
            addWeightedQuadric(quadrics[edge.second], boundaryQuadricMatrix, BOUNDARY_QUADRIC_WEIGHT)
        }

        val vertexAlive = BooleanArray(vertexCount) { true }
        val vertexOwner = IntArray(vertexCount) { it }
        val edgeCandidates = HashMap<Pair<Int, Int>, EdgeCandidate>()
        fun refreshEdge(a: Int, b: Int) {
            val key = edgeKey(a, b)
            val combined = DoubleArray(10)
            addWeightedQuadric(combined, quadrics[a], 1.0)
            addWeightedQuadric(combined, quadrics[b], 1.0)
            val target = solveOptimalPosition(combined) ?: midpoint(working, a, b)
            edgeCandidates[key] = EdgeCandidate(a, b, combined, target, quadricError(combined, target))
        }
        for ((edge, _) in edgeTriangleCount) refreshEdge(edge.first, edge.second)

        var currentTriangleCount = validTriangleCount
        val targetTriangleCount = (validTriangleCount * ratio).roundToInt().coerceAtLeast(MIN_TRIANGLES)

        while (currentTriangleCount > targetTriangleCount) {
            val next = lowestErrorCandidate(edgeCandidates, vertexAlive) ?: break
            edgeCandidates.remove(edgeKey(next.a, next.b))
            if (!vertexAlive[next.a] || !vertexAlive[next.b]) continue
            if (createsFlippedTriangle(working, vertexTriangles, triangleVertices, triangleAlive, next.a, next.b, next.target)) continue

            working[next.a * 3] = next.target[0]
            working[next.a * 3 + 1] = next.target[1]
            working[next.a * 3 + 2] = next.target[2]
            quadrics[next.a] = next.quadric

            val survivorTriangles = vertexTriangles[next.a]
            for (t in vertexTriangles[next.b]) {
                if (!triangleAlive[t]) continue
                for (slot in 0 until 3) {
                    if (triangleVertices[t * 3 + slot] == next.b) triangleVertices[t * 3 + slot] = next.a
                }
                val i0 = triangleVertices[t * 3]
                val i1 = triangleVertices[t * 3 + 1]
                val i2 = triangleVertices[t * 3 + 2]
                if (i0 == i1 || i1 == i2 || i0 == i2) {
                    triangleAlive[t] = false
                    currentTriangleCount -= 1
                } else {
                    survivorTriangles += t
                }
            }
            vertexTriangles[next.b].clear()
            vertexAlive[next.b] = false
            vertexOwner[next.b] = next.a

            val neighbors = mutableSetOf<Int>()
            for (t in survivorTriangles) {
                if (!triangleAlive[t]) continue
                for (slot in 0 until 3) {
                    val v = triangleVertices[t * 3 + slot]
                    if (v != next.a) neighbors += v
                }
            }
            for (neighbor in neighbors) {
                edgeCandidates.remove(edgeKey(neighbor, next.b))
                if (vertexAlive[neighbor]) refreshEdge(next.a, neighbor)
            }
        }

        return buildResult(working, vertexCount, vertexAlive, vertexOwner, triangleAlive, triangleVertices)
    }

    private data class EdgeCandidate(
        val a: Int,
        val b: Int,
        val quadric: DoubleArray,
        val target: DoubleArray,
        val error: Double,
    )

    private fun lowestErrorCandidate(
        candidates: Map<Pair<Int, Int>, EdgeCandidate>,
        vertexAlive: BooleanArray,
    ): EdgeCandidate? {
        var best: EdgeCandidate? = null
        for (candidate in candidates.values) {
            if (!vertexAlive[candidate.a] || !vertexAlive[candidate.b]) continue
            if (best == null || candidate.error < best.error) best = candidate
        }
        return best
    }

    private fun edgeKey(a: Int, b: Int): Pair<Int, Int> = if (a < b) a to b else b to a

    private fun triangleEdges(i0: Int, i1: Int, i2: Int): List<Pair<Int, Int>> =
        listOf(edgeKey(i0, i1), edgeKey(i1, i2), edgeKey(i2, i0))

    /** `(nx, ny, nz, d)` for the plane through `i0,i1,i2` -- `null` for a degenerate
     * (zero-area) triangle, which contributes no quadric and isn't counted as valid. */
    private fun trianglePlane(p: DoubleArray, i0: Int, i1: Int, i2: Int): DoubleArray? {
        val ax = p[i1 * 3] - p[i0 * 3]
        val ay = p[i1 * 3 + 1] - p[i0 * 3 + 1]
        val az = p[i1 * 3 + 2] - p[i0 * 3 + 2]
        val bx = p[i2 * 3] - p[i0 * 3]
        val by = p[i2 * 3 + 1] - p[i0 * 3 + 1]
        val bz = p[i2 * 3 + 2] - p[i0 * 3 + 2]
        var nx = ay * bz - az * by
        var ny = az * bx - ax * bz
        var nz = ax * by - ay * bx
        val length = kotlin.math.sqrt(nx * nx + ny * ny + nz * nz)
        if (length < DEGENERATE_NORMAL_LENGTH) return null
        nx /= length
        ny /= length
        nz /= length
        val d = -(nx * p[i0 * 3] + ny * p[i0 * 3 + 1] + nz * p[i0 * 3 + 2])
        return doubleArrayOf(nx, ny, nz, d)
    }

    /** A plane perpendicular to both the boundary edge `a-b` and to that edge's own direction
     * (a "fence" plane containing the edge, oriented along it) -- constrains a collapse
     * target from sliding the border inward. `null` for a zero-length edge. */
    private fun boundaryQuadric(p: DoubleArray, a: Int, b: Int): DoubleArray? {
        val ex = p[b * 3] - p[a * 3]
        val ey = p[b * 3 + 1] - p[a * 3 + 1]
        val ez = p[b * 3 + 2] - p[a * 3 + 2]
        val length = kotlin.math.sqrt(ex * ex + ey * ey + ez * ez)
        if (length < DEGENERATE_NORMAL_LENGTH) return null
        // Any vector perpendicular to the edge direction works as the fence's own normal --
        // pick one via a cross product with a not-parallel helper axis.
        val helper = if (kotlin.math.abs(ex) < kotlin.math.abs(ey)) doubleArrayOf(1.0, 0.0, 0.0) else doubleArrayOf(0.0, 1.0, 0.0)
        var nx = ey * helper[2] - ez * helper[1]
        var ny = ez * helper[0] - ex * helper[2]
        var nz = ex * helper[1] - ey * helper[0]
        val nLength = kotlin.math.sqrt(nx * nx + ny * ny + nz * nz)
        if (nLength < DEGENERATE_NORMAL_LENGTH) return null
        nx /= nLength
        ny /= nLength
        nz /= nLength
        val d = -(nx * p[a * 3] + ny * p[a * 3 + 1] + nz * p[a * 3 + 2])
        return doubleArrayOf(nx, ny, nz, d)
    }

    private fun addPlaneQuadric(quadric: DoubleArray, plane: DoubleArray) = addWeightedQuadric(quadric, planeOuterProduct(plane), 1.0)

    private fun planeOuterProduct(plane: DoubleArray): DoubleArray {
        val (nx, ny, nz, d) = plane
        return doubleArrayOf(
            nx * nx, nx * ny, nx * nz, nx * d,
            ny * ny, ny * nz, ny * d,
            nz * nz, nz * d,
            d * d,
        )
    }

    private fun addWeightedQuadric(target: DoubleArray, source: DoubleArray, weight: Double) {
        for (i in target.indices) target[i] += source[i] * weight
    }

    /** `v^T Q v` for homogeneous `v = (x, y, z, 1)`, `Q` packed as
     * `[q0 q1 q2 q3; q1 q4 q5 q6; q2 q5 q7 q8; q3 q6 q8 q9]`. */
    private fun quadricError(q: DoubleArray, v: DoubleArray): Double {
        val (x, y, z) = v
        return q[0] * x * x + 2 * q[1] * x * y + 2 * q[2] * x * z + 2 * q[3] * x +
            q[4] * y * y + 2 * q[5] * y * z + 2 * q[6] * y +
            q[7] * z * z + 2 * q[8] * z +
            q[9]
    }

    /** Minimizes `v^T Q v` by solving `Av = b` for the upper-left 3x3 block -- `null` when
     * that block is singular (or nearly so), so the caller falls back to the edge midpoint. */
    private fun solveOptimalPosition(q: DoubleArray): DoubleArray? {
        val a00 = q[0]; val a01 = q[1]; val a02 = q[2]
        val a10 = q[1]; val a11 = q[4]; val a12 = q[5]
        val a20 = q[2]; val a21 = q[5]; val a22 = q[7]
        val bx = -q[3]; val by = -q[6]; val bz = -q[8]

        val det = a00 * (a11 * a22 - a12 * a21) - a01 * (a10 * a22 - a12 * a20) + a02 * (a10 * a21 - a11 * a20)
        if (kotlin.math.abs(det) < SINGULAR_DETERMINANT) return null

        val x = (bx * (a11 * a22 - a12 * a21) - a01 * (by * a22 - a12 * bz) + a02 * (by * a21 - a11 * bz)) / det
        val y = (a00 * (by * a22 - a12 * bz) - bx * (a10 * a22 - a12 * a20) + a02 * (a10 * bz - by * a20)) / det
        val z = (a00 * (a11 * bz - by * a21) - a01 * (a10 * bz - by * a20) + bx * (a10 * a21 - a11 * a20)) / det
        return doubleArrayOf(x, y, z)
    }

    private fun midpoint(p: DoubleArray, a: Int, b: Int): DoubleArray = doubleArrayOf(
        (p[a * 3] + p[b * 3]) / 2.0,
        (p[a * 3 + 1] + p[b * 3 + 1]) / 2.0,
        (p[a * 3 + 2] + p[b * 3 + 2]) / 2.0,
    )

    /** `true` if moving [b] into [a] at [target] would flip any of [b]'s surviving (non-shared,
     * still-[triangleAlive]) triangles inside out -- compares each such triangle's normal
     * before and after substituting [b]'s position with [target]; a sign flip means this
     * collapse folds the mesh over itself, so the caller should reject this edge and try the
     * next-lowest-error one instead. Triangles shared by both [a] and [b] are skipped -- those
     * collapse away entirely, not a flip candidate. A triangle that's already degenerate
     * before or after (zero-area either way) is skipped too, not counted as a flip -- the
     * caller's own post-collapse degenerate filtering handles that case. */
    private fun createsFlippedTriangle(
        p: DoubleArray,
        vertexTriangles: Array<MutableSet<Int>>,
        triangleVertices: IntArray,
        triangleAlive: BooleanArray,
        a: Int,
        b: Int,
        target: DoubleArray,
    ): Boolean {
        for (t in vertexTriangles[b]) {
            if (!triangleAlive[t]) continue
            val i0 = triangleVertices[t * 3]
            val i1 = triangleVertices[t * 3 + 1]
            val i2 = triangleVertices[t * 3 + 2]
            if (i0 == a || i1 == a || i2 == a) continue

            val oldNormal = trianglePlane(p, i0, i1, i2) ?: continue
            val newNormal = triangleNormalSubstituting(p, i0, i1, i2, b, target) ?: continue
            val dot = oldNormal[0] * newNormal[0] + oldNormal[1] * newNormal[1] + oldNormal[2] * newNormal[2]
            if (dot < 0.0) return true
        }
        return false
    }

    /** Same normal computation [trianglePlane] does, but vertex [substituted]'s coordinates
     * come from [replacement] instead of [p] -- used to preview a triangle's normal after a
     * collapse without mutating [p] first. */
    private fun triangleNormalSubstituting(
        p: DoubleArray,
        i0: Int,
        i1: Int,
        i2: Int,
        substituted: Int,
        replacement: DoubleArray,
    ): DoubleArray? {
        fun x(i: Int) = if (i == substituted) replacement[0] else p[i * 3]
        fun y(i: Int) = if (i == substituted) replacement[1] else p[i * 3 + 1]
        fun z(i: Int) = if (i == substituted) replacement[2] else p[i * 3 + 2]
        val ax = x(i1) - x(i0); val ay = y(i1) - y(i0); val az = z(i1) - z(i0)
        val bx = x(i2) - x(i0); val by = y(i2) - y(i0); val bz = z(i2) - z(i0)
        var nx = ay * bz - az * by
        var ny = az * bx - ax * bz
        var nz = ax * by - ay * bx
        val length = kotlin.math.sqrt(nx * nx + ny * ny + nz * nz)
        if (length < DEGENERATE_NORMAL_LENGTH) return null
        nx /= length
        ny /= length
        nz /= length
        return doubleArrayOf(nx, ny, nz)
    }

    private fun buildResult(
        working: DoubleArray,
        vertexCount: Int,
        vertexAlive: BooleanArray,
        vertexOwner: IntArray,
        triangleAlive: BooleanArray,
        triangleVertices: IntArray,
    ): Result {
        fun root(index: Int): Int {
            var current = index
            while (vertexOwner[current] != current) current = vertexOwner[current]
            return current
        }

        val newIndexOf = IntArray(vertexCount) { -1 }
        var nextIndex = 0
        for (i in 0 until vertexCount) {
            if (!vertexAlive[i]) continue
            newIndexOf[i] = nextIndex
            nextIndex += 1
        }

        val newPositions = FloatArray(nextIndex * 3)
        for (i in 0 until vertexCount) {
            if (!vertexAlive[i]) continue
            val newIndex = newIndexOf[i]
            newPositions[newIndex * 3] = working[i * 3].toFloat()
            newPositions[newIndex * 3 + 1] = working[i * 3 + 1].toFloat()
            newPositions[newIndex * 3 + 2] = working[i * 3 + 2].toFloat()
        }

        val newIndices = ArrayList<Int>(triangleAlive.size * 3)
        for (t in triangleAlive.indices) {
            if (!triangleAlive[t]) continue
            val i0 = newIndexOf[root(triangleVertices[t * 3])]
            val i1 = newIndexOf[root(triangleVertices[t * 3 + 1])]
            val i2 = newIndexOf[root(triangleVertices[t * 3 + 2])]
            if (i0 == i1 || i1 == i2 || i0 == i2) continue
            newIndices += i0
            newIndices += i1
            newIndices += i2
        }

        val vertexRemap = IntArray(vertexCount) { newIndexOf[root(it)] }
        return Result(newPositions, newIndices.toIntArray(), vertexRemap)
    }
}
