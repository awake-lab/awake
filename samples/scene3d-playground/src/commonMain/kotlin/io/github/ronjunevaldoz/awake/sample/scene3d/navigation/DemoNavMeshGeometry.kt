// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("MagicNumber")

package io.github.ronjunevaldoz.awake.sample.scene3d.navigation

/**
 * Hardcoded walkable geometry for the MVP demo world (see docs/MMORPG_ROADMAP.md's NavMesh
 * decision): a flat 20x20 ground quad plus the existing decorative cube's footprint as a
 * solid obstacle, so navmesh generation actually exercises Recast's region-carving logic
 * instead of building a trivial obstacle-free plane -- confirming the generated path bends
 * around the cube is this slice's real verification signal. Deliberately invisible/synthetic
 * (not tied to any render mesh format) -- purely input to the navmesh builder, see this
 * slice's ground-geometry decision.
 */
internal object DemoNavMeshGeometry {
    private const val GROUND_HALF_EXTENT = 10f
    private const val CUBE_HALF_EXTENT = 0.5f

    /**
     * Flattened (x,y,z)-per-vertex positions and flattened vertex-index triangle triples,
     * in the layout `recast4j`'s `SimpleInputGeomProvider(vertices, faces)` expects.
     */
    fun buildGroundWithObstacle(): Pair<FloatArray, IntArray> {
        val g = GROUND_HALF_EXTENT
        val groundVerts = floatArrayOf(
            -g, 0f, -g, // 0
            g, 0f, -g, // 1
            g, 0f, g, // 2
            -g, 0f, g, // 3
        )
        // Wound so the cross product points +Y (up) -- Recast's walkable-slope filtering
        // discards downward-facing triangles, so the ground itself must wind this way to
        // be walkable at all.
        val groundTris = intArrayOf(
            0,
            2,
            1,
            0,
            3,
            2,
        )

        // Same corner layout/winding as the demo's rendered cube geometry (see
        // WebGpuApplication.kt's cubeVertices/cubeIndices) -- a separate hardcoded copy is
        // intentional here (position-only, no color/uv), not a shared dependency on render
        // vertex data.
        val c = CUBE_HALF_EXTENT
        val cubeVerts = floatArrayOf(
            -c, -c, -c, // 0
            c, -c, -c, // 1
            c, c, -c, // 2
            -c, c, -c, // 3
            -c, -c, c, // 4
            c, -c, c, // 5
            c, c, c, // 6
            -c, c, c, // 7
        )
        val cubeTris = intArrayOf(
            0, 1, 2, 2, 3, 0, // back
            4, 5, 6, 6, 7, 4, // front
            0, 3, 7, 7, 4, 0, // left
            1, 5, 6, 6, 2, 1, // right
            0, 4, 5, 5, 1, 0, // bottom
            3, 2, 6, 6, 7, 3, // top
        )

        val vertexOffset = groundVerts.size / 3
        val combinedVerts = groundVerts + cubeVerts
        val combinedTris = groundTris + IntArray(cubeTris.size) { cubeTris[it] + vertexOffset }
        return combinedVerts to combinedTris
    }
}
