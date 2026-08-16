// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val TOLERANCE = 1e-3f

class RayIntersectionTest {

    @Test
    fun aDirectionIsNormalizedSoDistancesAreInWorldUnits() {
        val ray = Ray(Vec3(0f, 0f, 0f), Vec3(0f, 0f, -5f))
        assertEquals(1f, ray.direction.length3(), TOLERANCE)
        val hit = assertNotNull(ray.intersectSphere(Vec3(0f, 0f, -10f), radius = 1f))
        assertEquals(9f, hit, TOLERANCE, "an unnormalized direction must not scale the distance")
    }

    @Test
    fun aRayPointingAwayFromASphereMisses() {
        val ray = Ray(Vec3(0f, 0f, 0f), Vec3(0f, 0f, 1f))
        assertNull(ray.intersectSphere(Vec3(0f, 0f, -10f), radius = 1f))
    }

    /** Reporting the exit point instead would make a click inside an object select what is
     * behind it. */
    @Test
    fun aRayStartingInsideASphereHitsImmediately() {
        val ray = Ray(Vec3(0f, 0f, 0f), Vec3(1f, 0f, 0f))
        assertEquals(0f, assertNotNull(ray.intersectSphere(Vec3(0f, 0f, 0f), radius = 2f)))
    }

    @Test
    fun aRayHitsTheNearFaceOfABox() {
        val box = Aabb(Vec3(-1f, -1f, -1f), Vec3(1f, 1f, 1f))
        val ray = Ray(Vec3(0f, 0f, 5f), Vec3(0f, 0f, -1f))
        assertEquals(4f, assertNotNull(ray.intersectAabb(box)), TOLERANCE)
    }

    /** The slab method divides by each direction component; an axis-parallel ray has a zero
     * there, and the infinities that produces have to compare correctly rather than crash. */
    @Test
    fun anAxisParallelRayIsHandledWithoutSpecialCasing() {
        val box = Aabb(Vec3(-1f, -1f, -1f), Vec3(1f, 1f, 1f))
        assertNotNull(Ray(Vec3(-5f, 0f, 0f), Vec3(1f, 0f, 0f)).intersectAabb(box), "should hit")
        assertNull(Ray(Vec3(-5f, 9f, 0f), Vec3(1f, 0f, 0f)).intersectAabb(box), "should miss above")
    }

    @Test
    fun aRayHitsTheGroundPlaneAtTheExpectedDistance() {
        val ground = Plane.through(Vec3(0f, 0f, 0f), Vec3(0f, 1f, 0f))
        val ray = Ray(Vec3(0f, 3f, 0f), Vec3(0f, -1f, 0f))
        assertEquals(3f, assertNotNull(ray.intersectPlane(ground)), TOLERANCE)
    }

    @Test
    fun aRayParallelToAPlaneMissesIt() {
        val ground = Plane.through(Vec3(0f, 0f, 0f), Vec3(0f, 1f, 0f))
        assertNull(Ray(Vec3(0f, 3f, 0f), Vec3(1f, 0f, 0f)).intersectPlane(ground))
    }

    @Test
    fun aPlaneReportsWhichSideAPointIsOn() {
        val ground = Plane.through(Vec3(0f, 0f, 0f), Vec3(0f, 1f, 0f))
        assertTrue(ground.signedDistanceTo(Vec3(0f, 2f, 0f)) > 0f)
        assertTrue(ground.signedDistanceTo(Vec3(0f, -2f, 0f)) < 0f)
        val projected = ground.project(Vec3(1f, 5f, -2f))
        assertEquals(0f, projected.y, TOLERANCE, "projection must land on the plane")
    }
}
