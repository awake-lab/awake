// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:OptIn(ExperimentalForeignApi::class)

package io.github.ronjunevaldoz.awake.physics.jolt

import cnames.structs.JPC_BodyInterface
import cnames.structs.JPC_JobSystem
import cnames.structs.JPC_PhysicsSystem
import cnames.structs.JPC_Shape
import cnames.structs.JPC_String
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.physics.BodyHandle
import io.github.ronjunevaldoz.awake.physics.BodyTransform
import io.github.ronjunevaldoz.awake.physics.BoxShape
import io.github.ronjunevaldoz.awake.physics.MotionType
import io.github.ronjunevaldoz.awake.physics.PhysicsShape
import io.github.ronjunevaldoz.awake.physics.PhysicsWorld
import io.github.ronjunevaldoz.awake.physics.RaycastHit
import io.github.ronjunevaldoz.awake.physics.SphereShape
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.cValue
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import platform.joltc.JPC_ACTIVATION_ACTIVATE
import platform.joltc.JPC_ACTIVATION_DONT_ACTIVATE
import platform.joltc.JPC_Activation
import platform.joltc.JPC_BodyCreationSettings
import platform.joltc.JPC_BodyCreationSettings_default
import platform.joltc.JPC_BodyID
import platform.joltc.JPC_BodyInterface_AddBody
import platform.joltc.JPC_BodyInterface_CreateBody
import platform.joltc.JPC_BodyInterface_DestroyBody
import platform.joltc.JPC_BodyInterface_GetPositionAndRotation
import platform.joltc.JPC_BodyInterface_RemoveBody
import platform.joltc.JPC_Body_GetID
import platform.joltc.JPC_BoxShapeSettings
import platform.joltc.JPC_BoxShapeSettings_Create
import platform.joltc.JPC_BoxShapeSettings_default
import platform.joltc.JPC_BroadPhaseLayer
import platform.joltc.JPC_BroadPhaseLayerInterfaceFns
import platform.joltc.JPC_BroadPhaseLayerInterface_delete
import platform.joltc.JPC_BroadPhaseLayerInterface_new
import platform.joltc.JPC_FactoryInit
import platform.joltc.JPC_JobSystemThreadPool_delete
import platform.joltc.JPC_JobSystemThreadPool_new2
import platform.joltc.JPC_MAX_PHYSICS_BARRIERS
import platform.joltc.JPC_MAX_PHYSICS_JOBS
import platform.joltc.JPC_MotionType
import platform.joltc.JPC_NarrowPhaseQuery_CastRay
import platform.joltc.JPC_NarrowPhaseQuery_CastRayArgs
import platform.joltc.JPC_ObjectLayer
import platform.joltc.JPC_ObjectLayerPairFilterFns
import platform.joltc.JPC_ObjectLayerPairFilter_delete
import platform.joltc.JPC_ObjectLayerPairFilter_new
import platform.joltc.JPC_ObjectVsBroadPhaseLayerFilterFns
import platform.joltc.JPC_ObjectVsBroadPhaseLayerFilter_delete
import platform.joltc.JPC_ObjectVsBroadPhaseLayerFilter_new
import platform.joltc.JPC_PhysicsSystem_GetBodyInterface
import platform.joltc.JPC_PhysicsSystem_GetNarrowPhaseQuery
import platform.joltc.JPC_PhysicsSystem_Init
import platform.joltc.JPC_PhysicsSystem_SetGravity
import platform.joltc.JPC_PhysicsSystem_Update
import platform.joltc.JPC_PhysicsSystem_delete
import platform.joltc.JPC_PhysicsSystem_new
import platform.joltc.JPC_Quat
import platform.joltc.JPC_RVec3
import platform.joltc.JPC_RegisterDefaultAllocator
import platform.joltc.JPC_RegisterTypes
import platform.joltc.JPC_SphereShapeSettings
import platform.joltc.JPC_SphereShapeSettings_Create
import platform.joltc.JPC_SphereShapeSettings_default
import platform.joltc.JPC_String_c_str
import platform.joltc.JPC_String_delete
import platform.joltc.JPC_TempAllocatorImpl_delete
import platform.joltc.JPC_TempAllocatorImpl_new
import platform.joltc.JPC_Vec3

// Jolt Physics integration slice 2 (see docs/MVP_PLAN.md's decision log): only 2 object
// layers, matching JoltC's own HelloWorld example (mirrors the desktop/Android jolt-jni
// backend's own scoping, see that class's own doc comment) -- fine-grained per-game layer
// authoring is out of scope for this slice.
private const val OBJECT_LAYER_MOVING: JPC_ObjectLayer = 0u
private const val OBJECT_LAYER_NON_MOVING: JPC_ObjectLayer = 1u
private const val MAX_BODIES = 5_000u
private const val NUM_BODY_MUTEXES = 0u
private const val MAX_BODY_PAIRS = 65_536u
private const val MAX_CONTACTS = 20_480u

// JoltC's callback fn-pointer tables require plain top-level `staticCFunction`s (no captured
// state), so the single-broadphase-layer/two-object-layer scheme below is hardcoded rather
// than instance state -- mirrors the desktop/Android backend's own layer mapping.
@OptIn(ExperimentalForeignApi::class)
private fun getNumBroadPhaseLayers(self: COpaquePointer?): UInt = 1u

@OptIn(ExperimentalForeignApi::class)
private fun getBroadPhaseLayer(
    self: COpaquePointer?,
    layer: JPC_ObjectLayer,
): JPC_BroadPhaseLayer = 0u

@OptIn(ExperimentalForeignApi::class)
private fun objectVsBroadPhaseShouldCollide(
    self: COpaquePointer?,
    objectLayer: JPC_ObjectLayer,
    broadPhaseLayer: JPC_BroadPhaseLayer,
): Boolean = true

@OptIn(ExperimentalForeignApi::class)
private fun objectLayerPairShouldCollide(
    self: COpaquePointer?,
    layer1: JPC_ObjectLayer,
    layer2: JPC_ObjectLayer,
): Boolean = !(layer1 == OBJECT_LAYER_NON_MOVING && layer2 == OBJECT_LAYER_NON_MOVING)

// Function-parameter Vec3/Quat cinterop as by-value CValue ([vec3Value]/[quatValue]), but
// nested struct fields (e.g. JPC_BodyCreationSettings.Position) expose only a read-only view
// with no CValue setter, so those need field-by-field writes ([JPC_Vec3.write]/[JPC_Quat.write]).
@OptIn(ExperimentalForeignApi::class)
private fun vec3Value(v: Vec3): CValue<JPC_Vec3> = cValue {
    x = v.x
    y = v.y
    z = v.z
}

@OptIn(ExperimentalForeignApi::class)
private fun quatValue(w: Float, x: Float, y: Float, z: Float): CValue<JPC_Quat> = cValue {
    this.w = w
    this.x = x
    this.y = y
    this.z = z
}

@OptIn(ExperimentalForeignApi::class)
private fun JPC_Vec3.write(v: Vec3) {
    x = v.x
    y = v.y
    z = v.z
}

@OptIn(ExperimentalForeignApi::class)
private fun JPC_Quat.write(w: Float, x: Float, y: Float, z: Float) {
    this.w = w
    this.x = x
    this.y = y
    this.z = z
}

/** [JPC_String] is JoltC's owned error-message type (see JoltC/Functions.h's `JPC_String_c_str`/
 * `JPC_String_delete`) -- callers of a `*_Create` function that fails must free the returned
 * error string themselves once they're done reading it. */
@OptIn(ExperimentalForeignApi::class)
private fun joltErrorMessage(error: CPointer<JPC_String>?): String {
    if (error == null) return "(no error message)"
    val message = JPC_String_c_str(error)?.toKString() ?: "(null error message)"
    JPC_String_delete(error)
    return message
}

/**
 * JoltC-backed (cinterop, `SecondHalfGames/JoltC` -- see this module's `build.gradle.kts` for
 * how the vendored `ios-native/JoltC` submodule is built into a static lib and linked)
 * [PhysicsWorld] -- iOS only. Mirrors the desktop/Android `jolt-jni` backend's own semantics
 * (gravity default, body-creation/tracked-body-list/syncTransforms/raycast/destroy shape) as
 * closely as JoltC's C API allows; see that class's doc comment for the parts of this design
 * this class intentionally duplicates rather than shares (no common intermediate layer exists
 * since jolt-jni and JoltC share zero code).
 */
@OptIn(ExperimentalForeignApi::class)
class JoltPhysicsWorld(gravity: Vec3 = Vec3(0f, -9.81f, 0f)) : PhysicsWorld {
    private companion object {
        // Matches HelloWorld's own initialization order: allocator, factory, types, exactly
        // once per process. A `private companion object { init { } }` runs before any
        // instance member here, same ordering guarantee the desktop backend's own
        // `JoltNative.ensureLoaded()` companion-object trick relies on.
        init {
            JPC_RegisterDefaultAllocator()
            JPC_FactoryInit()
            JPC_RegisterTypes()
        }
    }

    private val tempAllocator = JPC_TempAllocatorImpl_new((10 * 1024 * 1024).toUInt())
        ?: error("JPC_TempAllocatorImpl_new failed")
    private val jobSystem = JPC_JobSystemThreadPool_new2(JPC_MAX_PHYSICS_JOBS.toUInt(), JPC_MAX_PHYSICS_BARRIERS.toUInt())
        ?: error("JPC_JobSystemThreadPool_new2 failed")

    private val broadPhaseLayerInterfaceFns = cValue<JPC_BroadPhaseLayerInterfaceFns> {
        GetNumBroadPhaseLayers = staticCFunction(::getNumBroadPhaseLayers)
        GetBroadPhaseLayer = staticCFunction(::getBroadPhaseLayer)
    }
    private val broadPhaseLayerInterface = JPC_BroadPhaseLayerInterface_new(null, broadPhaseLayerInterfaceFns)
        ?: error("JPC_BroadPhaseLayerInterface_new failed")

    private val objectVsBroadPhaseLayerFilterFns = cValue<JPC_ObjectVsBroadPhaseLayerFilterFns> {
        ShouldCollide = staticCFunction(::objectVsBroadPhaseShouldCollide)
    }
    private val objectVsBroadPhaseLayerFilter =
        JPC_ObjectVsBroadPhaseLayerFilter_new(null, objectVsBroadPhaseLayerFilterFns)
            ?: error("JPC_ObjectVsBroadPhaseLayerFilter_new failed")

    private val objectLayerPairFilterFns = cValue<JPC_ObjectLayerPairFilterFns> {
        ShouldCollide = staticCFunction(::objectLayerPairShouldCollide)
    }
    private val objectLayerPairFilter = JPC_ObjectLayerPairFilter_new(null, objectLayerPairFilterFns)
        ?: error("JPC_ObjectLayerPairFilter_new failed")

    private val physicsSystem: CPointer<JPC_PhysicsSystem> =
        JPC_PhysicsSystem_new() ?: error("JPC_PhysicsSystem_new failed")

    private val bodyInterface: CPointer<JPC_BodyInterface>

    // Every body this PhysicsWorld created, so syncTransforms() knows what to read back and
    // destroy() knows what to tear down -- same reasoning as the desktop backend's own
    // `trackedBodyIds` (JoltC exposes no cheap "iterate my bodies" call either).
    private val trackedBodyIds = mutableListOf<JPC_BodyID>()

    init {
        JPC_PhysicsSystem_Init(
            physicsSystem,
            MAX_BODIES,
            NUM_BODY_MUTEXES,
            MAX_BODY_PAIRS,
            MAX_CONTACTS,
            broadPhaseLayerInterface,
            objectVsBroadPhaseLayerFilter,
            objectLayerPairFilter,
        )
        JPC_PhysicsSystem_SetGravity(physicsSystem, vec3Value(gravity))
        bodyInterface = JPC_PhysicsSystem_GetBodyInterface(physicsSystem) ?: error("JPC_PhysicsSystem_GetBodyInterface failed")
    }

    override fun createBody(
        shape: PhysicsShape,
        position: Vec3,
        rotation: Vec3,
        motionType: MotionType,
    ): BodyHandle = memScoped {
        val joltShape = when (shape) {
            is BoxShape -> {
                val settings = alloc<JPC_BoxShapeSettings>()
                JPC_BoxShapeSettings_default(settings.ptr)
                settings.HalfExtent.write(shape.halfExtents)
                val outShape = alloc<CPointerVar<JPC_Shape>>()
                val outError = alloc<CPointerVar<JPC_String>>()
                check(JPC_BoxShapeSettings_Create(settings.ptr, outShape.ptr, outError.ptr)) {
                    "JPC_BoxShapeSettings_Create failed: ${joltErrorMessage(outError.value)}"
                }
                outShape.value ?: error("JPC_BoxShapeSettings_Create returned null shape")
            }
            is SphereShape -> {
                val settings = alloc<JPC_SphereShapeSettings>()
                JPC_SphereShapeSettings_default(settings.ptr)
                settings.Radius = shape.radius
                val outShape = alloc<CPointerVar<JPC_Shape>>()
                val outError = alloc<CPointerVar<JPC_String>>()
                check(JPC_SphereShapeSettings_Create(settings.ptr, outShape.ptr, outError.ptr)) {
                    "JPC_SphereShapeSettings_Create failed: ${joltErrorMessage(outError.value)}"
                }
                outShape.value ?: error("JPC_SphereShapeSettings_Create returned null shape")
            }
        }

        val joltMotionType: JPC_MotionType = when (motionType) {
            MotionType.STATIC -> JPC_MotionType.JPC_MOTION_TYPE_STATIC
            MotionType.KINEMATIC -> JPC_MotionType.JPC_MOTION_TYPE_KINEMATIC
            MotionType.DYNAMIC -> JPC_MotionType.JPC_MOTION_TYPE_DYNAMIC
        }
        val objectLayer = if (motionType == MotionType.STATIC) OBJECT_LAYER_NON_MOVING else OBJECT_LAYER_MOVING
        val (qw, qx, qy, qz) = eulerVec3ToQuatWxyz(rotation)

        val bodyCreationSettings = alloc<JPC_BodyCreationSettings>()
        JPC_BodyCreationSettings_default(bodyCreationSettings.ptr)
        bodyCreationSettings.apply {
            Position.write(position)
            Rotation.write(qw, qx, qy, qz)
            MotionType = joltMotionType
            ObjectLayer = objectLayer
            Shape = joltShape
        }

        val body = JPC_BodyInterface_CreateBody(bodyInterface, bodyCreationSettings.ptr)
            ?: error("JPC_BodyInterface_CreateBody failed")
        val bodyId = JPC_Body_GetID(body)
        val activation: JPC_Activation = if (motionType == MotionType.STATIC) {
            JPC_ACTIVATION_DONT_ACTIVATE
        } else {
            JPC_ACTIVATION_ACTIVATE
        }
        JPC_BodyInterface_AddBody(bodyInterface, bodyId, activation)

        trackedBodyIds.add(bodyId)
        BodyHandle(bodyId.toLong())
    }

    override fun destroyBody(handle: BodyHandle) {
        val id: JPC_BodyID = handle.id.toUInt()
        JPC_BodyInterface_RemoveBody(bodyInterface, id)
        JPC_BodyInterface_DestroyBody(bodyInterface, id)
        trackedBodyIds.remove(id)
    }

    override fun step(deltaTime: Float) {
        val collisionSteps = 1
        JPC_PhysicsSystem_Update(physicsSystem, deltaTime, collisionSteps, tempAllocator, jobSystem.reinterpret<JPC_JobSystem>())
    }

    override fun syncTransforms(): List<BodyTransform> = memScoped {
        val position = alloc<JPC_RVec3>()
        val rotation = alloc<JPC_Quat>()
        trackedBodyIds.map { id ->
            JPC_BodyInterface_GetPositionAndRotation(bodyInterface, id, position.ptr, rotation.ptr)
            BodyTransform(
                handle = BodyHandle(id.toLong()),
                position = Vec3(position.x, position.y, position.z),
                rotation = quatToEulerVec3(rotation.w, rotation.x, rotation.y, rotation.z),
            )
        }
    }

    override fun raycast(origin: Vec3, direction: Vec3, maxDistance: Float): RaycastHit? = memScoped {
        val normalizedDirection = direction.normalized()
        val castVector = Vec3(
            normalizedDirection.x * maxDistance,
            normalizedDirection.y * maxDistance,
            normalizedDirection.z * maxDistance,
        )
        val args = alloc<JPC_NarrowPhaseQuery_CastRayArgs>()
        args.Ray.Origin.write(origin)
        args.Ray.Direction.write(castVector)
        args.BroadPhaseLayerFilter = null
        args.ObjectLayerFilter = null
        args.BodyFilter = null
        args.ShapeFilter = null

        val narrowPhaseQuery = JPC_PhysicsSystem_GetNarrowPhaseQuery(physicsSystem)
            ?: error("JPC_PhysicsSystem_GetNarrowPhaseQuery failed")
        val hit = JPC_NarrowPhaseQuery_CastRay(narrowPhaseQuery, args.ptr)
        if (!hit) return@memScoped null

        val distance = maxDistance * args.Result.Fraction
        val point = Vec3(
            origin.x + normalizedDirection.x * distance,
            origin.y + normalizedDirection.y * distance,
            origin.z + normalizedDirection.z * distance,
        )
        RaycastHit(BodyHandle(args.Result.BodyID.toLong()), point, distance)
    }

    override fun destroy() {
        trackedBodyIds.forEach { id ->
            JPC_BodyInterface_RemoveBody(bodyInterface, id)
            JPC_BodyInterface_DestroyBody(bodyInterface, id)
        }
        trackedBodyIds.clear()
        JPC_PhysicsSystem_delete(physicsSystem)
        JPC_ObjectLayerPairFilter_delete(objectLayerPairFilter)
        JPC_ObjectVsBroadPhaseLayerFilter_delete(objectVsBroadPhaseLayerFilter)
        JPC_BroadPhaseLayerInterface_delete(broadPhaseLayerInterface)
        JPC_JobSystemThreadPool_delete(jobSystem)
        JPC_TempAllocatorImpl_delete(tempAllocator)
        // JPC_UnregisterTypes()/JPC_FactoryDelete() are deliberately NOT called here -- they
        // are process-wide (matching JPC_RegisterTypes()/JPC_FactoryInit() in the companion
        // `init` above being called at most once too), and a second JoltPhysicsWorld could
        // still be constructed later in the same process.
    }
}
