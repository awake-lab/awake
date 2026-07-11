/*
 * Awake
 * Awake.awake-scene.desktopTest
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
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Real, falsifiable proof the recast4j pipeline (voxelize -> region-carve -> polygon mesh ->
 * Detour navmesh) is doing something, not just A* on an open plane -- a navmesh built from
 * an obstacle-free area would only ever need a straight line. See
 * docs/MMORPG_ROADMAP.md's NavMesh decision.
 */
class DemoNavMeshTest {
    @Test
    fun pathAroundObstacleDeviatesFromStraightLine() {
        val navMesh = createDemoNavMesh() ?: error("Expected a recast4j-backed NavMesh on desktop.")

        // Straight line between these two points runs directly through the origin cube's
        // footprint (|x|,|z| <= 0.5) -- a real navmesh must route around it.
        val path = navMesh.findPath(Vec3(-3f, 0f, 0f), Vec3(3f, 0f, 0f))

        assertTrue(
            path.isNotEmpty(),
            "Expected a real path between two points on opposite sides of the obstacle."
        )
        val maxAbsZ = path.maxOf { abs(it.z) }
        assertTrue(
            maxAbsZ > 0.6f,
            "Expected the path to bend around the cube obstacle (footprint |z| <= 0.5), " +
                "but max |z| across waypoints was only $maxAbsZ -- looks like a straight " +
                "line through it, meaning the obstacle wasn't actually carved out."
        )
    }

    @Test
    fun pathWithNoObstacleInTheWayIsStillFound() {
        val navMesh = createDemoNavMesh() ?: error("Expected a recast4j-backed NavMesh on desktop.")
        val path = navMesh.findPath(Vec3(-8f, 0f, -8f), Vec3(-8f, 0f, 8f))
        assertTrue(path.isNotEmpty(), "Expected a path along open ground, away from the obstacle.")
    }
}
