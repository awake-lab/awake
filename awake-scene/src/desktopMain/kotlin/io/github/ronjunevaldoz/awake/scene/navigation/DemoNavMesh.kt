/*
 * Awake
 * Awake.awake-scene.desktopMain
 *
 * Copyright (c) ronjunevaldoz 2023.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ronjunevaldoz.awake.scene.navigation

import io.github.ronjunevaldoz.awake.core.math.Vec3
import org.recast4j.detour.DefaultQueryFilter
import org.recast4j.detour.NavMeshBuilder
import org.recast4j.detour.NavMeshDataCreateParams
import org.recast4j.detour.NavMeshQuery
import org.recast4j.recast.AreaModification
import org.recast4j.recast.RecastBuilder
import org.recast4j.recast.RecastBuilderConfig
import org.recast4j.recast.RecastConfig
import org.recast4j.recast.RecastConstants.PartitionType
import org.recast4j.recast.geom.SimpleInputGeomProvider
import org.recast4j.detour.NavMesh as DetourNavMesh

/**
 * recast4j-backed [NavMesh] (see docs/MMORPG_ROADMAP.md's NavMesh decision) -- desktop +
 * Android only, since recast4j is a plain-JVM library with zero native code. Builds a real
 * navmesh once from [DemoNavMeshGeometry]'s hardcoded ground+obstacle geometry via the
 * standard Recast-then-Detour pipeline (voxelize -> region-carve -> polygon mesh -> Detour
 * navmesh data), then answers [NavMesh.findPath] queries against it. Duplicated verbatim in
 * `androidMain` rather than a shared intermediate source set -- recast4j behaves identically
 * on both, and this repo's default hierarchy template doesn't already group desktop+Android
 * under one JVM-shared source set (unlike its auto `webMain` grouping for js+wasmJs).
 */
private class Recast4jNavMesh(private val query: NavMeshQuery) : NavMesh {
    override fun findPath(start: Vec3, end: Vec3): List<Vec3> {
        val filter = DefaultQueryFilter()
        val extents = floatArrayOf(2f, 4f, 2f)
        val startNearest = query.findNearestPoly(floatArrayOf(start.x, start.y, start.z), extents, filter)
        val endNearest = query.findNearestPoly(floatArrayOf(end.x, end.y, end.z), extents, filter)
        if (!startNearest.succeeded() || !endNearest.succeeded()) {
            return emptyList()
        }
        val startRef = startNearest.result.nearestRef
        val endRef = endNearest.result.nearestRef
        if (startRef == 0L || endRef == 0L) {
            return emptyList()
        }
        val startPos = startNearest.result.nearestPos
        val endPos = endNearest.result.nearestPos
        val pathResult = query.findPath(startRef, endRef, startPos, endPos, filter)
        if (!pathResult.succeeded() || pathResult.result.isEmpty()) {
            return emptyList()
        }
        val straight = query.findStraightPath(startPos, endPos, pathResult.result, Int.MAX_VALUE, 0)
        if (!straight.succeeded()) {
            return emptyList()
        }
        return straight.result.map { Vec3(it.pos[0], it.pos[1], it.pos[2]) }
    }
}

actual fun createDemoNavMesh(): NavMesh? {
    val (vertices, faces) = DemoNavMeshGeometry.buildGroundWithObstacle()
    val geom = SimpleInputGeomProvider(vertices, faces)

    val cellSize = 0.3f
    val cellHeight = 0.2f
    val agentHeight = 1.8f
    val agentRadius = 0.4f
    // Must be smaller than the demo cube's 0.5m height above the ground (see
    // DemoNavMeshGeometry) -- otherwise Recast treats stepping onto the cube as a
    // traversable ledge instead of an obstacle, and the navmesh routes straight over it.
    val agentMaxClimb = 0.2f
    val agentMaxSlope = 45f
    val regionMinSize = 8
    val regionMergeSize = 20
    val edgeMaxLen = 12f
    val edgeMaxError = 1.3f
    val vertsPerPoly = 6
    val detailSampleDist = 6f
    val detailSampleMaxError = 1f
    // Value/mask are arbitrary but must be non-zero -- recast4j treats area id 0 as "not
    // walkable" (PolyMesh.areas), and this ground is the only area this demo needs.
    val walkableAreaMod = AreaModification(1, 0x07)

    val config = RecastConfig(
        PartitionType.WATERSHED,
        cellSize, cellHeight, agentHeight, agentRadius, agentMaxClimb, agentMaxSlope,
        regionMinSize, regionMergeSize, edgeMaxLen, edgeMaxError, vertsPerPoly,
        detailSampleDist, detailSampleMaxError, walkableAreaMod
    )
    val builderConfig = RecastBuilderConfig(config, geom.meshBoundsMin, geom.meshBoundsMax)
    val result = RecastBuilder().build(geom, builderConfig)
    val polyMesh = result.mesh ?: return null
    for (i in 0 until polyMesh.npolys) {
        polyMesh.flags[i] = 1
    }
    val detailMesh = result.meshDetail

    val params = NavMeshDataCreateParams().apply {
        verts = polyMesh.verts
        vertCount = polyMesh.nverts
        polys = polyMesh.polys
        polyAreas = polyMesh.areas
        polyFlags = polyMesh.flags
        polyCount = polyMesh.npolys
        nvp = polyMesh.nvp
        detailMeshes = detailMesh?.meshes
        detailVerts = detailMesh?.verts
        detailVertsCount = detailMesh?.nverts ?: 0
        detailTris = detailMesh?.tris
        detailTriCount = detailMesh?.ntris ?: 0
        walkableHeight = agentHeight
        walkableRadius = agentRadius
        walkableClimb = agentMaxClimb
        bmin = polyMesh.bmin
        bmax = polyMesh.bmax
        cs = cellSize
        ch = cellHeight
        buildBvTree = true
    }
    val meshData = NavMeshBuilder.createNavMeshData(params) ?: return null
    val detourNavMesh = DetourNavMesh(meshData, vertsPerPoly, 0)
    return Recast4jNavMesh(NavMeshQuery(detourNavMesh))
}
