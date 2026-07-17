// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.physics.jolt

import com.github.stephengold.joltjni.BodyCreationSettings
import com.github.stephengold.joltjni.BodyInterface
import com.github.stephengold.joltjni.BroadPhaseLayerInterfaceTable
import com.github.stephengold.joltjni.JobSystem
import com.github.stephengold.joltjni.JobSystemThreadPool
import com.github.stephengold.joltjni.Jolt
import com.github.stephengold.joltjni.ObjectLayerPairFilterTable
import com.github.stephengold.joltjni.ObjectVsBroadPhaseLayerFilterTable
import com.github.stephengold.joltjni.PhysicsSystem
import com.github.stephengold.joltjni.Quat
import com.github.stephengold.joltjni.RRayCast
import com.github.stephengold.joltjni.RVec3
import com.github.stephengold.joltjni.RayCastResult
import com.github.stephengold.joltjni.TempAllocator
import com.github.stephengold.joltjni.TempAllocatorMalloc
import com.github.stephengold.joltjni.enumerate.EActivation
import com.github.stephengold.joltjni.enumerate.EMotionType
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.physics.BodyHandle
import io.github.ronjunevaldoz.awake.physics.BodyTransform
import io.github.ronjunevaldoz.awake.physics.BoxShape
import io.github.ronjunevaldoz.awake.physics.MotionType
import io.github.ronjunevaldoz.awake.physics.PhysicsShape
import io.github.ronjunevaldoz.awake.physics.PhysicsWorld
import io.github.ronjunevaldoz.awake.physics.RaycastHit
import io.github.ronjunevaldoz.awake.physics.SphereShape

// Jolt Physics integration slice 1 (see docs/MVP_PLAN.md's decision log): only 2 object
// layers, matching jolt-jni's own HelloJoltJni tutorial -- fine-grained per-game layer
// authoring (multiple moving layers with custom collision rules) is out of scope for this
// slice, the same "coarse first, refine later" scoping this slice's API itself follows.
private const val NUM_OBJECT_LAYERS = 2
private const val OBJECT_LAYER_MOVING = 0
private const val OBJECT_LAYER_NON_MOVING = 1
private const val NUM_BROADPHASE_LAYERS = 1
private const val MAX_BODIES = 5_000
private const val MAX_BODY_PAIRS = 65_536
private const val MAX_CONTACTS = 20_480

/**
 * jolt-jni-backed [PhysicsWorld] -- Android only (see `desktopMain`'s near-identical
 * duplicate, and this module's `build.gradle.kts` comment for why it isn't a shared
 * intermediate source set). Duplicated verbatim rather than factored into a shared
 * `commonMain` class because jolt-jni's actual JVM classes ARE the desktop/Android
 * implementation -- there's no additional platform-neutral layer to extract without
 * reinventing jolt-jni's own API.
 */
class JoltPhysicsWorld(gravity: Vec3 = Vec3(0f, -9.81f, 0f)) : PhysicsWorld {
    // Companion object init always runs before any instance member -- the only ordering
    // guarantee strong enough to load the native lib before tempAllocator's native constructor
    // call (confirmed via UnsatisfiedLinkError when this lived in an instance init instead).
    private companion object {
        init {
            JoltNative.ensureLoaded()
        }
    }

    private val physicsSystem: PhysicsSystem
    private val bodyInterface: BodyInterface
    private val tempAllocator: TempAllocator = TempAllocatorMalloc()
    private val jobSystem: JobSystem
    // Every body this PhysicsWorld created, so syncTransforms() knows what to read back and
    // destroy() knows what to tear down -- jolt-jni's PhysicsSystem itself doesn't expose an
    // "iterate my bodies" call cheap enough to lean on instead (getBodies() copies into a
    // BodyIdVector every call).
    private val trackedBodyIds = mutableListOf<Int>()

    init {
        val objectLayerPairFilter = ObjectLayerPairFilterTable(NUM_OBJECT_LAYERS).apply {
            enableCollision(OBJECT_LAYER_MOVING, OBJECT_LAYER_MOVING)
            enableCollision(OBJECT_LAYER_MOVING, OBJECT_LAYER_NON_MOVING)
            disableCollision(OBJECT_LAYER_NON_MOVING, OBJECT_LAYER_NON_MOVING)
        }
        val broadPhaseLayerInterface = BroadPhaseLayerInterfaceTable(NUM_OBJECT_LAYERS, NUM_BROADPHASE_LAYERS).apply {
            mapObjectToBroadPhaseLayer(OBJECT_LAYER_MOVING, 0)
            mapObjectToBroadPhaseLayer(OBJECT_LAYER_NON_MOVING, 0)
        }
        val objectVsBroadPhaseLayerFilter = ObjectVsBroadPhaseLayerFilterTable(
            broadPhaseLayerInterface, NUM_BROADPHASE_LAYERS, objectLayerPairFilter, NUM_OBJECT_LAYERS
        )

        physicsSystem = PhysicsSystem().apply {
            init(
                MAX_BODIES, 0, MAX_BODY_PAIRS, MAX_CONTACTS,
                broadPhaseLayerInterface, objectVsBroadPhaseLayerFilter, objectLayerPairFilter
            )
            setGravity(gravity.x, gravity.y, gravity.z)
        }
        bodyInterface = physicsSystem.bodyInterface

        val numWorkerThreads = Runtime.getRuntime().availableProcessors()
        jobSystem = JobSystemThreadPool(Jolt.cMaxPhysicsJobs, Jolt.cMaxPhysicsBarriers, numWorkerThreads)
    }

    override fun createBody(
        shape: PhysicsShape,
        position: Vec3,
        rotation: Vec3,
        motionType: MotionType
    ): BodyHandle {
        val joltShape = when (shape) {
            is BoxShape -> com.github.stephengold.joltjni.BoxShape(
                shape.halfExtents.x, shape.halfExtents.y, shape.halfExtents.z
            )
            is SphereShape -> com.github.stephengold.joltjni.SphereShape(shape.radius)
        }
        val joltMotionType = when (motionType) {
            MotionType.STATIC -> EMotionType.Static
            MotionType.KINEMATIC -> EMotionType.Kinematic
            MotionType.DYNAMIC -> EMotionType.Dynamic
        }
        val objectLayer = if (motionType == MotionType.STATIC) OBJECT_LAYER_NON_MOVING else OBJECT_LAYER_MOVING

        val bcs = BodyCreationSettings().apply {
            setShape(joltShape)
            setPosition(RVec3(position.x.toDouble(), position.y.toDouble(), position.z.toDouble()))
            setRotation(Quat.sEulerAngles(rotation.x, rotation.y, rotation.z))
            setMotionType(joltMotionType)
            setObjectLayer(objectLayer)
        }
        val body = bodyInterface.createBody(bcs)
        val activation = if (motionType == MotionType.STATIC) EActivation.DontActivate else EActivation.Activate
        bodyInterface.addBody(body, activation)

        trackedBodyIds.add(body.id)
        return BodyHandle(body.id.toLong())
    }

    override fun destroyBody(handle: BodyHandle) {
        val id = handle.id.toInt()
        bodyInterface.removeBody(id)
        bodyInterface.destroyBody(id)
        trackedBodyIds.remove(id)
    }

    override fun step(deltaTime: Float) {
        val collisionSteps = 1
        physicsSystem.update(deltaTime, collisionSteps, tempAllocator, jobSystem)
    }

    override fun syncTransforms(): List<BodyTransform> {
        // One JNI call per tracked body, but still only one Kotlin-side call per frame --
        // see PhysicsWorld.syncTransforms's own doc comment for why that's the intended
        // coarse boundary, not a violation of it. jolt-jni exposes no bulk position/rotation
        // query across an arbitrary body-id list.
        val position = RVec3()
        val rotation = Quat()
        return trackedBodyIds.map { id ->
            bodyInterface.getPositionAndRotation(id, position, rotation)
            BodyTransform(
                handle = BodyHandle(id.toLong()),
                position = Vec3(position.x(), position.y(), position.z()),
                rotation = quatToEulerVec3(rotation.w, rotation.x, rotation.y, rotation.z)
            )
        }
    }

    override fun raycast(origin: Vec3, direction: Vec3, maxDistance: Float): RaycastHit? {
        val normalizedDirection = direction.normalize()
        val castVector = com.github.stephengold.joltjni.Vec3(
            normalizedDirection.x * maxDistance,
            normalizedDirection.y * maxDistance,
            normalizedDirection.z * maxDistance
        )
        val rRayCast = RRayCast(RVec3(origin.x.toDouble(), origin.y.toDouble(), origin.z.toDouble()), castVector)
        val result = RayCastResult()
        val hit = physicsSystem.narrowPhaseQuery.castRay(rRayCast, result)
        if (!hit) return null

        val distance = maxDistance * result.fraction
        val point = Vec3(
            origin.x + normalizedDirection.x * distance,
            origin.y + normalizedDirection.y * distance,
            origin.z + normalizedDirection.z * distance
        )
        return RaycastHit(BodyHandle(result.bodyId.toLong()), point, distance)
    }

    override fun destroy() {
        trackedBodyIds.forEach { id ->
            bodyInterface.removeBody(id)
            bodyInterface.destroyBody(id)
        }
        trackedBodyIds.clear()
        jobSystem.close()
        tempAllocator.close()
        physicsSystem.close()
    }
}
