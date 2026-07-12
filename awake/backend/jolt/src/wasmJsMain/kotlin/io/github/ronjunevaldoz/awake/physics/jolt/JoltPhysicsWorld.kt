// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.ronjunevaldoz.awake.physics.jolt

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.physics.BodyHandle
import io.github.ronjunevaldoz.awake.physics.BodyTransform
import io.github.ronjunevaldoz.awake.physics.BoxShape
import io.github.ronjunevaldoz.awake.physics.MotionType
import io.github.ronjunevaldoz.awake.physics.PhysicsShape
import io.github.ronjunevaldoz.awake.physics.PhysicsWorld
import io.github.ronjunevaldoz.awake.physics.RaycastHit
import io.github.ronjunevaldoz.awake.physics.SphereShape
import kotlinx.coroutines.await
import kotlin.js.Promise

/**
 * `@JsModule("jolt-physics")`'s default export: an Emscripten module-factory function
 * (`declare function Jolt<T>(target?: T): Promise<T & typeof Jolt>`, see the npm package's
 * `dist/types.d.ts`) -- calling it with no argument resolves a `Promise` of the `Jolt`
 * namespace object itself (every `Jolt.XXX` class/constant lives as a property on that one
 * resolved object, not as separate ES module exports Kotlin/Wasm's `@JsModule` could bind
 * per-declaration). Kotlin/Wasm has no `dynamic` type, so nothing about that resolved
 * object's shape can be described with a compile-time-bound `external class`/`external
 * interface` the way `kotlinx-browser`'s DOM bindings are -- every actual Jolt call below
 * goes through an `@JsFun` inline-JS snippet instead, taking the resolved namespace object
 * (opaque `JsAny`) as a plain parameter and indexing into it by property name at runtime,
 * exactly like any other reflective/dynamic JS access. This is the Kotlin/Wasm-idiomatic
 * substitute for what would be `dynamic` on Kotlin/JS.
 */
@JsModule("jolt-physics")
external fun initJoltModule(): Promise<JsAny>

/**
 * Loads the `jolt-physics` WASM module at most once per page -- mirrors the desktop/iOS
 * backends' own "register/init native library exactly once per process" companion-object
 * pattern (`JoltNative.ensureLoaded()` / the iOS `JPC_FactoryInit()` companion `init`), just
 * async since Emscripten's own module bootstrap is a `Promise` here instead of a synchronous
 * `dlopen`.
 */
private object JoltModule {
    private var cached: JsAny? = null

    suspend fun get(): JsAny {
        cached?.let { return it }
        val loaded = initJoltModule().await<JsAny>()
        cached = loaded
        return loaded
    }
}

// --- @JsFun glue: each function below is one self-contained JoltPhysics.js call sequence,
// mirroring the exact boilerplate jrouwe/JoltPhysics.js's own Examples/js/example.js uses
// (setupCollisionFiltering/initPhysics/createBox/createSphere/addToScene/removeFromScene/
// updatePhysics) -- see that file for the upstream original this was copied from. Every
// `jolt.XXX`-shaped value threaded through is the same namespace object [JoltModule.get]
// resolves; there is deliberately no separate typed wrapper class for it (see that object's
// doc comment for why one isn't possible here).

/**
 * [jolt.JoltInterface]'s own object-layer/broadphase-layer collision-filtering setup, plus
 * gravity -- mirrors `Examples/js/example.js`'s `setupCollisionFiltering`/`initPhysics`
 * combined into one call. Two object layers (matching every other platform's own "moving vs.
 * non-moving" scoping, see e.g. the desktop backend's own `NUM_OBJECT_LAYERS` doc comment)
 * mapped 1:1 onto two broadphase layers -- copied verbatim from the upstream example rather
 * than collapsed to one broadphase layer (as the desktop/iOS backends do) specifically
 * because this is the exact shape JoltPhysics.js's own maintained example uses; deviating
 * from it for a slice this thin isn't worth losing that as a reference point.
 *
 * Returns an opaque JS object `{ jolt, joltInterface, physicsSystem, bodyInterface }` --
 * every other glue function below takes that object as its first parameter and reads
 * `.jolt`/`.bodyInterface`/`.physicsSystem` back out of it, since none of those individual
 * values can be given their own Kotlin-typed handle (same "no dynamic type" constraint
 * [JoltModule] documents).
 */
@Suppress("unused")
@JsFun(
    """
    (jolt, gx, gy, gz) => {
        const NUM_OBJECT_LAYERS = 2;
        const LAYER_NON_MOVING = 0;
        const LAYER_MOVING = 1;
        const NUM_BROAD_PHASE_LAYERS = 2;

        const settings = new jolt.JoltSettings();
        const objectFilter = new jolt.ObjectLayerPairFilterTable(NUM_OBJECT_LAYERS);
        objectFilter.EnableCollision(LAYER_NON_MOVING, LAYER_MOVING);
        objectFilter.EnableCollision(LAYER_MOVING, LAYER_MOVING);

        const bpLayerNonMoving = new jolt.BroadPhaseLayer(0);
        const bpLayerMoving = new jolt.BroadPhaseLayer(1);
        const bpInterface = new jolt.BroadPhaseLayerInterfaceTable(NUM_OBJECT_LAYERS, NUM_BROAD_PHASE_LAYERS);
        bpInterface.MapObjectToBroadPhaseLayer(LAYER_NON_MOVING, bpLayerNonMoving);
        bpInterface.MapObjectToBroadPhaseLayer(LAYER_MOVING, bpLayerMoving);

        settings.mObjectLayerPairFilter = objectFilter;
        settings.mBroadPhaseLayerInterface = bpInterface;
        settings.mObjectVsBroadPhaseLayerFilter = new jolt.ObjectVsBroadPhaseLayerFilterTable(
            bpInterface, NUM_BROAD_PHASE_LAYERS, objectFilter, NUM_OBJECT_LAYERS
        );

        const joltInterface = new jolt.JoltInterface(settings);
        jolt.destroy(settings);
        const physicsSystem = joltInterface.GetPhysicsSystem();
        physicsSystem.SetGravity(new jolt.Vec3(gx, gy, gz));
        const bodyInterface = physicsSystem.GetBodyInterface();

        return { jolt, joltInterface, physicsSystem, bodyInterface };
    }
    """
)
private external fun joltCreateWorld(jolt: JsAny, gravityX: Double, gravityY: Double, gravityZ: Double): JsAny

/** Object layer 1 ("moving") for every non-static body, 0 ("non-moving") for static ones --
 * matches [joltCreateWorld]'s own `LAYER_NON_MOVING`/`LAYER_MOVING` constants. */
@Suppress("unused")
@JsFun(
    """
    (world, isBox, hx, hy, hz, radius, px, py, pz, qw, qx, qy, qz, motionTypeCode, isStatic) => {
        const jolt = world.jolt;
        const shape = isBox
            ? new jolt.BoxShape(new jolt.Vec3(hx, hy, hz), 0.05, null)
            : new jolt.SphereShape(radius, null);
        const motionType = motionTypeCode === 0
            ? jolt.EMotionType_Static
            : (motionTypeCode === 1 ? jolt.EMotionType_Kinematic : jolt.EMotionType_Dynamic);
        const layer = isStatic ? 0 : 1;
        const bodyCreationSettings = new jolt.BodyCreationSettings(
            shape, new jolt.RVec3(px, py, pz), new jolt.Quat(qx, qy, qz, qw), motionType, layer
        );
        const body = world.bodyInterface.CreateBody(bodyCreationSettings);
        jolt.destroy(bodyCreationSettings);
        const activation = isStatic ? jolt.EActivation_DontActivate : jolt.EActivation_Activate;
        world.bodyInterface.AddBody(body.GetID(), activation);
        return body.GetID().GetIndexAndSequenceNumber();
    }
    """
)
private external fun joltCreateBody(
    world: JsAny,
    isBox: Boolean,
    halfExtentX: Double,
    halfExtentY: Double,
    halfExtentZ: Double,
    radius: Double,
    positionX: Double,
    positionY: Double,
    positionZ: Double,
    rotationW: Double,
    rotationX: Double,
    rotationY: Double,
    rotationZ: Double,
    motionTypeCode: Int,
    isStatic: Boolean
): Int

@Suppress("unused")
@JsFun(
    """
    (world, idNum) => {
        const id = new world.jolt.BodyID(idNum);
        world.bodyInterface.RemoveBody(id);
        world.bodyInterface.DestroyBody(id);
    }
    """
)
private external fun joltDestroyBody(world: JsAny, idNum: Int)

@Suppress("unused")
@JsFun("(world, deltaTime, collisionSteps) => { world.joltInterface.Step(deltaTime, collisionSteps); }")
private external fun joltStep(world: JsAny, deltaTime: Double, collisionSteps: Int)

/** Returns a plain JS array `[px, py, pz, qw, qx, qy, qz]` -- see [jsArrayGet] for why a raw
 * JS array (rather than 7 separate calls, or a Kotlin-typed return) is the simplest thing
 * that can cross this boundary (no `dynamic`, see [JoltModule]'s doc comment). */
@Suppress("unused")
@JsFun(
    """
    (world, idNum) => {
        const id = new world.jolt.BodyID(idNum);
        const position = world.bodyInterface.GetPosition(id);
        const rotation = world.bodyInterface.GetRotation(id);
        return [
            position.GetX(), position.GetY(), position.GetZ(),
            rotation.GetW(), rotation.GetX(), rotation.GetY(), rotation.GetZ()
        ];
    }
    """
)
private external fun joltGetBodyTransform(world: JsAny, idNum: Int): JsAny

/** Returns `null` on no hit, else `[fraction, bodyIdIndexAndSequenceNumber]` -- same "plain
 * JS array, no dynamic type" reasoning as [joltGetBodyTransform]. The four filter arguments
 * `NarrowPhaseQuery.CastRay` requires are each constructed from their accept-everything base
 * class (`new jolt.BroadPhaseLayerFilter()` etc.) -- the exact JS-side equivalent of the
 * desktop/iOS backends passing `null` broadphase/object-layer/body/shape filters into their
 * own raycast call. */
@Suppress("unused")
@JsFun(
    """
    (world, ox, oy, oz, dx, dy, dz) => {
        const jolt = world.jolt;
        const ray = new jolt.RRayCast(new jolt.RVec3(ox, oy, oz), new jolt.Vec3(dx, dy, dz));
        const raySettings = new jolt.RayCastSettings();
        const collector = new jolt.CastRayClosestHitCollisionCollector();
        world.physicsSystem.GetNarrowPhaseQuery().CastRay(
            ray, raySettings, collector,
            new jolt.BroadPhaseLayerFilter(), new jolt.ObjectLayerFilter(),
            new jolt.BodyFilter(), new jolt.ShapeFilter()
        );
        if (!collector.HadHit()) {
            return null;
        }
        const hit = collector.mHit;
        return [hit.mFraction, hit.mBodyID.GetIndexAndSequenceNumber()];
    }
    """
)
private external fun joltRaycast(
    world: JsAny,
    originX: Double,
    originY: Double,
    originZ: Double,
    directionX: Double,
    directionY: Double,
    directionZ: Double
): JsAny?

@Suppress("unused")
@JsFun("(arr, index) => arr[index]")
private external fun jsArrayGet(arr: JsAny, index: Int): Double

@Suppress("unused")
@JsFun("(world) => { world.jolt.destroy(world.joltInterface); }")
private external fun joltDestroyWorld(world: JsAny)

/**
 * jolt-physics (JoltPhysics.js)-backed [PhysicsWorld] -- wasmJs only. Mirrors the desktop/
 * Android (`jolt-jni`) and iOS (`JoltC`) backends' own semantics (gravity default, body-
 * creation/tracked-body-list/syncTransforms/raycast/destroy shape) as closely as
 * JoltPhysics.js's JS-shaped API allows; see those classes' own doc comments for the parts of
 * this design this class intentionally duplicates rather than shares (no common intermediate
 * layer exists since none of the three native bindings share any code).
 *
 * Constructed via the `suspend` [create] factory, not directly -- unlike the other three
 * platforms' plain no-arg constructor, this backend needs the `jolt-physics` WASM module's
 * own async `Promise`-returning bootstrap to resolve first (see [JoltModule]'s doc comment),
 * which is why `awake:backend:jolt`'s wasmJs actual is the one platform where
 * `PhysicsWorldFactory.createPhysicsWorld()` had to become `suspend` across every platform's
 * `expect`/`actual` (see that file's own doc comment) -- `PhysicsDemo.ready()` was already
 * `suspend`, so this ripples no further than that one call site.
 */
class JoltPhysicsWorld private constructor(private val world: JsAny) : PhysicsWorld {
    // Every body this PhysicsWorld created, so syncTransforms() knows what to read back and
    // destroy() knows what to tear down -- same reasoning (and the same "no cheap bulk query"
    // constraint) as the desktop/iOS backends' own `trackedBodyIds`.
    private val trackedBodyIds = mutableListOf<Int>()

    override fun createBody(
        shape: PhysicsShape,
        position: Vec3,
        rotation: Vec3,
        motionType: MotionType
    ): BodyHandle {
        val (qw, qx, qy, qz) = eulerVec3ToQuatWxyz(rotation)
        val motionTypeCode = when (motionType) {
            MotionType.STATIC -> 0
            MotionType.KINEMATIC -> 1
            MotionType.DYNAMIC -> 2
        }
        val isStatic = motionType == MotionType.STATIC
        val idNum = when (shape) {
            is BoxShape -> joltCreateBody(
                world,
                true,
                shape.halfExtents.x.toDouble(),
                shape.halfExtents.y.toDouble(),
                shape.halfExtents.z.toDouble(),
                0.0,
                position.x.toDouble(),
                position.y.toDouble(),
                position.z.toDouble(),
                qw.toDouble(),
                qx.toDouble(),
                qy.toDouble(),
                qz.toDouble(),
                motionTypeCode,
                isStatic
            )
            is SphereShape -> joltCreateBody(
                world,
                false,
                0.0,
                0.0,
                0.0,
                shape.radius.toDouble(),
                position.x.toDouble(),
                position.y.toDouble(),
                position.z.toDouble(),
                qw.toDouble(),
                qx.toDouble(),
                qy.toDouble(),
                qz.toDouble(),
                motionTypeCode,
                isStatic
            )
        }
        trackedBodyIds.add(idNum)
        return BodyHandle(idNum.toLong())
    }

    override fun destroyBody(handle: BodyHandle) {
        val idNum = handle.id.toInt()
        joltDestroyBody(world, idNum)
        trackedBodyIds.remove(idNum)
    }

    override fun step(deltaTime: Float) {
        val collisionSteps = 1
        joltStep(world, deltaTime.toDouble(), collisionSteps)
    }

    override fun syncTransforms(): List<BodyTransform> = trackedBodyIds.map { idNum ->
        val transform = joltGetBodyTransform(world, idNum)
        BodyTransform(
            handle = BodyHandle(idNum.toLong()),
            position = Vec3(
                jsArrayGet(transform, 0).toFloat(),
                jsArrayGet(transform, 1).toFloat(),
                jsArrayGet(transform, 2).toFloat()
            ),
            rotation = quatToEulerVec3(
                jsArrayGet(transform, 3).toFloat(),
                jsArrayGet(transform, 4).toFloat(),
                jsArrayGet(transform, 5).toFloat(),
                jsArrayGet(transform, 6).toFloat()
            )
        )
    }

    override fun raycast(origin: Vec3, direction: Vec3, maxDistance: Float): RaycastHit? {
        val normalizedDirection = direction.normalize()
        val castVector = Vec3(
            normalizedDirection.x * maxDistance,
            normalizedDirection.y * maxDistance,
            normalizedDirection.z * maxDistance
        )
        val result = joltRaycast(
            world,
            origin.x.toDouble(),
            origin.y.toDouble(),
            origin.z.toDouble(),
            castVector.x.toDouble(),
            castVector.y.toDouble(),
            castVector.z.toDouble()
        ) ?: return null

        val fraction = jsArrayGet(result, 0).toFloat()
        val bodyIdNum = jsArrayGet(result, 1).toInt()
        val distance = maxDistance * fraction
        val point = Vec3(
            origin.x + normalizedDirection.x * distance,
            origin.y + normalizedDirection.y * distance,
            origin.z + normalizedDirection.z * distance
        )
        return RaycastHit(BodyHandle(bodyIdNum.toLong()), point, distance)
    }

    override fun destroy() {
        trackedBodyIds.forEach { idNum -> joltDestroyBody(world, idNum) }
        trackedBodyIds.clear()
        joltDestroyWorld(world)
    }

    companion object {
        suspend fun create(gravity: Vec3 = Vec3(0f, -9.81f, 0f)): JoltPhysicsWorld {
            val jolt = JoltModule.get()
            val world = joltCreateWorld(jolt, gravity.x.toDouble(), gravity.y.toDouble(), gravity.z.toDouble())
            return JoltPhysicsWorld(world)
        }
    }
}
