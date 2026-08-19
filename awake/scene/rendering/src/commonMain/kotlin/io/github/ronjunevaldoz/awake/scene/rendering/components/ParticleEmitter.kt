// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.rendering.components

import io.github.ronjunevaldoz.awake.core.math.Aabb
import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.math.Vec4
import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.ecs.Poolable
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh

/**
 * One live particle slot inside a [ParticleEmitter]'s fixed-capacity pool -- [ParticleSystem]
 * mutates these in place every frame (position/age advance, dead slots respawn) rather than
 * replacing a `List` wholesale, since a particle system's whole point is high per-frame churn
 * (many slots spawning/dying every frame) that a full-list rebuild would allocate for
 * needlessly. [Poolable] because a "dead" slot is reused for the next spawn, not discarded --
 * [reset] must clear EVERY field (including [alive]) or a recycled slot carries stale state
 * into its next life, the exact bug class `CameraComponent.needsReset` already shipped once
 * (see `skills/awake-ecs-authoring/SKILL.md`).
 */
internal class Particle : Poolable {
    var alive: Boolean = false
    val position: Vec3 = Vec3(0f, 0f, 0f)
    val velocity: Vec3 = Vec3(0f, 0f, 0f)
    var age: Float = 0f
    var lifetime: Float = 0f
    var startAlpha: Float = 0f
    var scale: Float = 1f
    /** `true` once this particle has hit its ground height and stopped moving -- still fades
     * out over its remaining lifetime, it just no longer integrates [velocity]. */
    var settled: Boolean = false
    /** A random `0 until frameCount` offset chosen at spawn -- added to this particle's own
     * age-derived frame index so particles sharing one emitter show DIFFERENT sprite-strip
     * frames at the same instant (desynced), instead of every particle in the emitter switching
     * frames in lockstep. See [io.github.ronjunevaldoz.awake.scene.rendering.systems
     * .RenderSystem]'s own particle-instancing block for where this is read. */
    var frameOffset: Int = 0

    override fun reset() {
        alive = false
        position.set(0f, 0f, 0f)
        velocity.set(0f, 0f, 0f)
        age = 0f
        lifetime = 0f
        startAlpha = 0f
        scale = 1f
        settled = false
        frameOffset = 0
    }
}

/**
 * How particles move once spawned -- everything about direction/spread/turbulence lives here,
 * grouped separately from [ParticleVisual]/[ParticleGround]/[ParticleLifecycle]/
 * [ParticleDynamics] because these are independent, freely-COMBINABLE dimensions (unlike
 * `RenderPipeline`'s `PipelineVariant`, these aren't a closed set of named shapes -- a real
 * effect wants turbulence AND a cone burst AND ground-stop together, so a sealed preset would
 * either explode combinatorially or block real combos; small grouped value classes fix the
 * param-count bloat without that trade-off).
 *
 * Every particle spawns [spawnRadius] units from [ParticleEmitter.origin], on a random point
 * around a flat horizontal ring (`0f`, the default, spawns exactly at `origin`, unchanged from
 * before this field existed).
 *
 * Velocity resolution, in priority order:
 * 1. [convergeToOrigin] `true`: direction points from the spawn point back toward `origin`, at
 *    [baseVelocity]'s own speed -- combined with [spawnRadius] > 0, this is a "charging circle"
 *    (particles appear on a ring and converge inward), the classic cast/channel-spell VFX.
 * 2. [coneHalfAngleDegrees] set (and [baseVelocity] non-zero): direction randomized within that
 *    half-angle around [baseVelocity]'s own direction, at [baseVelocity]'s own speed -- a
 *    cone/fan burst. [velocityJitter] is ignored in this mode.
 * 3. Otherwise: [baseVelocity] plus independent per-axis [velocityJitter] -- a soft cloud.
 *
 * [turbulence] adds a smooth flow-field offset to velocity every frame, scaled by this strength
 * and sampled at [turbulenceFrequency] -- see [io.github.ronjunevaldoz.awake.scene.rendering
 * .systems.turbulenceOffset]. `0f` (default) is a no-op. ponytail: a cheap sine-based flow
 * field, not true Perlin/simplex/curl noise.
 */
data class ParticleMotion(
    val baseVelocity: Vec3 = Vec3(0f, 1f, 0f),
    val velocityJitter: Float = 0f,
    val coneHalfAngleDegrees: Float? = null,
    val spawnRadius: Float = 0f,
    val convergeToOrigin: Boolean = false,
    val turbulence: Float = 0f,
    val turbulenceFrequency: Float = 1f,
)

/** How a particle looks over its life. [startColor]/[endColor] linearly interpolate per
 * particle over its own age (constant color when equal, the default). [frameCount] treats the
 * material's texture as a horizontal sprite strip of that many equal-width frames, cycling at
 * [frameRate] frames/second -- `1` (default) is a no-op. Each particle picks a random frame
 * OFFSET at spawn ([Particle.frameOffset]) and advances from there based on its own age, so
 * particles sharing one emitter are visibly desynced, not switching frames in lockstep -- a
 * real per-instance GPU attribute (`particle.wgsl`'s `inFrame`), not an emitter-wide uniform. */
data class ParticleVisual(
    val startColor: Vec3 = Vec3(1f, 1f, 1f),
    val endColor: Vec3 = startColor,
    val frameCount: Int = 1,
    val frameRate: Float = 8f,
)

/** How (and whether) a particle interacts with the ground. Resolution priority: [groundY]/
 * [groundHeightProvider]/[colliders] all clamp a falling particle's Y the same way --
 * [groundHeightProvider] wins when set (a per-position height function `(x, z) -> groundHeight`,
 * for hand-authored non-flat terrain), otherwise [colliders] wins when a particle's XZ falls
 * inside one of these world-space [Aabb] boxes' footprint (real collision against authored scene
 * geometry -- reuses the same [Aabb] type `MeshBounds`/`Occluder` already use, not a
 * physics-engine dependency), otherwise the flat [groundY] plane. All `null`/empty (default)
 * means a particle never clamps and simply fades out mid-air/mid-fall.
 *
 * What happens AT the clamp is [restitution]: `0f` (default) is the original behavior -- velocity
 * zeroed, [Particle.settled] set, the particle stops for good. `> 0f` reflects [Particle.velocity]
 * .y instead (scaled by [restitution] -- `1f` is a lossless bounce, values above that gain
 * energy and are the caller's own choice), and [friction] scales the horizontal (x/z) velocity
 * on every bounce (`1f`, the default, keeps it unchanged; `< 1f` bleeds it off, a bounce that
 * skids to a stop instead of bouncing forever sideways). A particle only fully settles once a
 * bounce's resulting vertical speed drops under [BOUNCE_STOP_VELOCITY] -- without that floor, a
 * `restitution` just under 1 would bounce in ever-smaller hops for the particle's entire
 * lifetime instead of visibly coming to rest. */
data class ParticleGround(
    val groundY: Float? = null,
    val groundHeightProvider: ((x: Float, z: Float) -> Float)? = null,
    val colliders: List<Aabb> = emptyList(),
    val restitution: Float = 0f,
    val friction: Float = 1f,
)

/** Below this vertical speed (units/second), a bouncing particle settles instead of bouncing
 * again -- see [ParticleGround.restitution]'s own doc comment for why a floor is needed at all. */
const val BOUNCE_STOP_VELOCITY = 0.3f

/** Spawn/death lifecycle hooks. [burstCount] caps the LIFETIME total spawn count instead of the
 * per-frame rate -- `null` (default) spawns forever; a non-null value stops spawning once that
 * many particles have ever been spawned, and [io.github.ronjunevaldoz.awake.scene.rendering
 * .systems.ParticleSystem] destroys this emitter's own entity once every spawned particle has
 * also died, so a one-shot burst cleans itself up with no caller bookkeeping -- see
 * [spawnParticleBurst]. [onParticleDeath], when set, is called with (world, death position)
 * every time a particle dies of old age (not one that's still settled/fading) -- the chained-
 * effect hook for a real sub-emitter tree without composing emitters directly: e.g. a
 * projectile's dying particles each spawn their own impact burst via [spawnParticleBurst].
 * `null` (default) does nothing extra on death. */
data class ParticleLifecycle(
    val burstCount: Int? = null,
    val onParticleDeath: ((world: World, position: Vec3) -> Unit)? = null,
)

/** Live, per-frame-reactive knobs. [followEntity], when set, re-anchors
 * [ParticleEmitter.origin] to that entity's [io.github.ronjunevaldoz.awake.scene.core
 * .components.Transform] position every frame -- a lightweight "sub-emitter" for a trailing
 * effect (smoke behind a moving fireball) without a real parent/child emitter tree. `var`
 * because gameplay code re-targets it (e.g. handing an emitter off between two casters).
 * [dynamicSpawnRate], when set, is called once per [io.github.ronjunevaldoz.awake.scene
 * .rendering.systems.ParticleSystem.update] and OVERRIDES [ParticleEmitter.spawnRate] for that
 * frame -- the context-driven emission hook: gameplay code can read live state (player speed,
 * distance to a target) and return a rate that reacts to it. `null` (default) always uses the
 * static `spawnRate`. */
data class ParticleDynamics(
    var followEntity: Entity? = null,
    val dynamicSpawnRate: (() -> Float)? = null,
)

/**
 * A fixed-capacity pool of camera-facing billboard particles, spawned/advanced by
 * [io.github.ronjunevaldoz.awake.scene.rendering.systems.ParticleSystem] and drawn via GPU
 * instancing by [io.github.ronjunevaldoz.awake.scene.rendering.systems.RenderSystem] --
 * mirrors [InstancedMeshRenderer]'s own "N copies of one mesh/material in one draw call" shape,
 * with the same "no `Transform`/entity-hierarchy requirement" independence, plus per-instance
 * color+alpha ([io.github.ronjunevaldoz.awake.render.renderer.DrawCall.instanceColors]) for
 * independent per-particle fade/tint -- something static instancing has no use for.
 *
 * [origin] is the emitter's own spawn point (world space, not entity-relative), MUTATED in
 * place (`origin.set(...)`) rather than reassigned to move an emitter -- e.g. tracking
 * [ParticleDynamics.followEntity]'s position every frame, or gameplay code calling
 * `emitter.origin.set(...)` directly for a spell that follows its caster.
 *
 * [maxParticles] bounds the pool -- [spawnRate] particles/second spawn into dead slots until
 * every slot is live, at which point spawning silently stops until a slot frees up (no
 * queueing, no overflow allocation). A particle lives for [lifetime] seconds, fades linearly
 * from [startAlpha] to 0 over its life -- no rotation is ever written into a particle's own
 * model matrix (billboarding is camera-facing math done entirely in `particle.wgsl`'s vertex
 * stage), so `ParticleSystem` only ever needs to build a translation+uniform-scale matrix per
 * particle, never a rotation.
 *
 * [motion]/[visual]/[ground]/[lifecycle]/[dynamics] group every optional capability by concern
 * -- see each type's own doc comment. All default to their own no-op shape, so a caller that
 * only needs the 8 required params above gets exactly the plain-jitter-cloud emitter this class
 * always built.
 *
 * [children] is a real parent/child emitter tree -- each child's own [origin] is treated as a
 * fixed LOCAL offset from this emitter's `origin` (never mutated), recomposed every frame by
 * [io.github.ronjunevaldoz.awake.scene.rendering.systems.ParticleSystem] the same way
 * [ParticleDynamics.followEntity] recomposes THIS emitter's own origin from a followed
 * `Transform`. Unlike [ParticleDynamics.followEntity] (one emitter trailing one moving entity)
 * or [ParticleLifecycle.onParticleDeath] (a one-shot burst chained on death), a child here is a
 * CONTINUOUSLY co-simulated emitter riding along for this emitter's whole life -- a torch flame
 * with a child ember-spark emitter at its tip, both driven by one authored composition instead
 * of two separately-wired entities. Children have no `Entity`/`World` presence of their own --
 * they live and die with this emitter's own entity, simplest possible teardown. Empty (default)
 * is a leaf emitter, unchanged from before this field existed.
 */
class ParticleEmitter(
    val mesh: Mesh,
    val material: Material,
    val origin: Vec3,
    val maxParticles: Int,
    var spawnRate: Float,
    var lifetime: Float,
    var startAlpha: Float,
    var scale: Float,
    var motion: ParticleMotion = ParticleMotion(),
    var visual: ParticleVisual = ParticleVisual(),
    var ground: ParticleGround = ParticleGround(),
    var lifecycle: ParticleLifecycle = ParticleLifecycle(),
    var dynamics: ParticleDynamics = ParticleDynamics(),
    val children: List<ParticleEmitter> = emptyList(),
) {
    internal val particles: Array<Particle> = Array(maxParticles) { Particle() }

    /** [origin]'s value at construction time -- for a [children] entry, this is the fixed LOCAL
     * offset from the parent's own `origin` (see [children]'s doc comment); `origin` itself gets
     * overwritten every frame with the recomposed world position, so the original authored
     * offset has to be captured once, here, before that first overwrite. Unused for a top-level
     * (non-child) emitter. */
    internal val localOffset: Vec3 = Vec3(origin.x, origin.y, origin.z)

    /** Fractional particles/second carried across frames -- without this, a [spawnRate] that
     * isn't an exact multiple of the frame rate (almost always) either spawns nothing some
     * frames or drops sub-particle remainders every frame, both of which read as visibly
     * uneven/bursty rather than a steady stream. */
    internal var spawnAccumulator: Float = 0f

    /** Total particles spawned across this emitter's whole life -- compared against
     * [ParticleLifecycle.burstCount], not reset per frame (unlike [spawnAccumulator]). Unused
     * when `burstCount` is `null`. */
    internal var spawnedTotal: Int = 0

    /** Seconds this emitter has been alive -- unused directly by [ParticleVisual.frameCount]'s
     * sprite-strip cycling anymore (that's per-particle now, driven by each [Particle]'s own
     * `age`/[Particle.frameOffset]), kept for anything else that wants an emitter-wide clock. */
    internal var elapsedTime: Float = 0f

    /** `true` once this emitter has come from [BurstEmitterPool.obtain] -- the marker
     * [io.github.ronjunevaldoz.awake.scene.rendering.systems.ParticleSystem] checks before
     * returning a spent emitter to that pool, so a hand-attached (non-burst) emitter that
     * happens to also set [ParticleLifecycle.burstCount] is never mistakenly pooled. Stays
     * `true` for this object's whole life once set -- pooling is a one-way opt-in per instance,
     * not something [reconfigure] needs to touch. */
    internal var pooledForBurst: Boolean = false

    /** Reused every frame by [io.github.ronjunevaldoz.awake.scene.rendering.systems
     * .RenderSystem] to build this emitter's `DrawCall.instanceModels`/`.instanceColors`/
     * `.instanceFrames` -- `clear()`+refill instead of a fresh `list.filter{}.map{}` per frame,
     * the same "resize only when the count actually changes" no-per-frame-allocation rule every
     * other instanced buffer in this codebase already follows (see
     * `skills/awake-core-math/SKILL.md`). Still allocates one `Mat4`/`Vec4` per LIVE particle
     * each frame (not per emitter) -- fully eliminating that needs pooling those per particle
     * slot and mutating them in place, a larger follow-up, not done here. */
    internal val instanceModelsBuffer: MutableList<Mat4> = ArrayList(maxParticles)
    internal val instanceColorsBuffer: MutableList<Vec4> = ArrayList(maxParticles)
    internal val instanceFramesBuffer: MutableList<Float> = ArrayList(maxParticles)

    /** Reused every frame by [io.github.ronjunevaldoz.awake.scene.rendering.systems
     * .RenderSystem] to hold this frame's frustum-visible [Particle]s (a subset of [particles]),
     * sorted back-to-front before [instanceModelsBuffer]/etc are built from it -- see that
     * system's own `addParticleDrawCalls` doc comment. Holds `Particle` references, not floats,
     * unlike the buffers above -- it's an intermediate filter/sort step, not GPU-bound data. */
    internal val visibleParticlesBuffer: MutableList<Particle> = ArrayList(maxParticles)

    /** Reused every frame for `DrawCall.extraUniformFloats` (camera basis + frame info) --
     * written in place instead of `cameraBasis + floatArrayOf(...)`, which allocates a new
     * array via concatenation every frame. */
    internal val uniformFloatsBuffer: FloatArray = FloatArray(EXTRA_UNIFORM_FLOAT_COUNT)

    /** Rewrites every field a fresh burst config supplies, and resets simulation state (spawn
     * accounting, elapsed time, every pool slot) back to a clean start -- what
     * [io.github.ronjunevaldoz.awake.scene.rendering.components.BurstEmitterPool] calls on an
     * emitter it's handing out for reuse, pooled or freshly constructed either way, so there's
     * exactly one place this class considers "reset to a new burst," not one implicit
     * (`Poolable.reset`) and one explicit path fighting over the same fields. [mesh]/[material]/
     * [maxParticles] are NOT touched here -- they're the pool's own key, an emitter is only ever
     * handed back for a burst that already matches that shape. */
    internal fun reconfigure(
        newOrigin: Vec3,
        newSpawnRate: Float,
        newLifetime: Float,
        newStartAlpha: Float,
        newScale: Float,
        newMotion: ParticleMotion,
        newVisual: ParticleVisual,
        newGround: ParticleGround,
        newLifecycle: ParticleLifecycle,
        newDynamics: ParticleDynamics,
    ) {
        origin.set(newOrigin.x, newOrigin.y, newOrigin.z)
        spawnRate = newSpawnRate
        lifetime = newLifetime
        startAlpha = newStartAlpha
        scale = newScale
        motion = newMotion
        visual = newVisual
        ground = newGround
        lifecycle = newLifecycle
        dynamics = newDynamics
        spawnAccumulator = 0f
        spawnedTotal = 0
        elapsedTime = 0f
        particles.forEach { it.reset() }
    }
}

/** [ParticleEmitter.uniformFloatsBuffer]'s fixed size: cameraRight(4) + cameraUp(4) +
 * frameInfo(4), matching `particle.wgsl`'s own `Uniforms` layout past `viewProjection`.
 * frameInfo.x still carries `frameCount` (the atlas is fixed-per-material); frameInfo.y
 * (formerly the emitter-wide `currentFrame`) is unused now that frame cycling is per-particle
 * via [Particle.frameOffset] -- kept as a reserved pad slot rather than reshuffling the struct
 * for one dead float. */
internal const val EXTRA_UNIFORM_FLOAT_COUNT = 12

/**
 * Creates a fresh entity carrying a one-shot [ParticleEmitter] (`burstCount = count`) at
 * [position] and returns it -- the convenience path for gameplay code that wants "play this
 * effect right here, right now" (a spell impact, a hit spark) without hand-rolling entity
 * creation, worrying about cleanup, or constructing [ParticleMotion]/[ParticleGround] wrappers
 * for the common case: [io.github.ronjunevaldoz.awake.scene.rendering.systems.ParticleSystem]
 * destroys the returned entity once every spawned particle has died. Stays a flat parameter
 * list on purpose -- advanced capabilities (turbulence, sprite frames, sub-emitter chaining,
 * a charging-circle spawn shape) go through [ParticleEmitter]'s own grouped constructor
 * directly. Example:
 * ```
 * fun onFireballImpact(world: World, mesh: Mesh, material: Material, hitPosition: Vec3) {
 *     spawnParticleBurst(
 *         world, mesh, material, hitPosition,
 *         count = 40, spawnRate = 400f, lifetime = 0.5f, startAlpha = 1f,
 *         baseVelocity = Vec3(0f, 1f, 0f), coneHalfAngleDegrees = 60f,
 *         startColor = Vec3(1f, 0.6f, 0.1f), endColor = Vec3(1f, 0.1f, 0f),
 *     )
 * }
 * ```
 */
fun spawnParticleBurst(
    world: World,
    mesh: Mesh,
    material: Material,
    position: Vec3,
    count: Int,
    spawnRate: Float,
    lifetime: Float,
    startAlpha: Float,
    baseVelocity: Vec3,
    velocityJitter: Float = 0f,
    scale: Float = 0.25f,
    startColor: Vec3 = Vec3(1f, 1f, 1f),
    endColor: Vec3 = startColor,
    coneHalfAngleDegrees: Float? = null,
    followEntity: Entity? = null,
    groundY: Float? = null,
): Entity {
    val entity = world.create()
    val emitter = BurstEmitterPool.obtain(
        mesh = mesh,
        material = material,
        maxParticles = count,
        origin = position,
        spawnRate = spawnRate,
        lifetime = lifetime,
        startAlpha = startAlpha,
        scale = scale,
        motion = ParticleMotion(
            baseVelocity = baseVelocity,
            velocityJitter = velocityJitter,
            coneHalfAngleDegrees = coneHalfAngleDegrees,
        ),
        visual = ParticleVisual(startColor = startColor, endColor = endColor),
        ground = ParticleGround(groundY = groundY),
        lifecycle = ParticleLifecycle(burstCount = count),
        dynamics = ParticleDynamics(followEntity = followEntity),
    )
    world.add(entity, emitter)
    return entity
}
