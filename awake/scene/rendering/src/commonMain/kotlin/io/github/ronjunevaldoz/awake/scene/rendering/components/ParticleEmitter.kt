// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.rendering.components

import io.github.ronjunevaldoz.awake.core.math.Vec3
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
    /** `true` once this particle has hit [ParticleEmitter.groundY] and stopped moving -- still
     * fades out over its remaining lifetime, it just no longer integrates [velocity]. */
    var settled: Boolean = false

    override fun reset() {
        alive = false
        position.set(0f, 0f, 0f)
        velocity.set(0f, 0f, 0f)
        age = 0f
        lifetime = 0f
        startAlpha = 0f
        scale = 1f
        settled = false
    }
}

/**
 * A fixed-capacity pool of camera-facing billboard particles, spawned/advanced by
 * [io.github.ronjunevaldoz.awake.scene.rendering.systems.ParticleSystem] and drawn via GPU
 * instancing by [io.github.ronjunevaldoz.awake.scene.rendering.systems.RenderSystem] --
 * mirrors [InstancedMeshRenderer]'s own "N copies of one mesh/material in one draw call"
 * shape, with the same "no `Transform`/entity-hierarchy requirement" independence, plus
 * per-instance color+alpha ([io.github.ronjunevaldoz.awake.render.renderer.DrawCall
 * .instanceColors]) for independent per-particle fade/tint -- something static instancing has
 * no use for.
 *
 * [origin] is the emitter's own spawn point (world space, not entity-relative -- same "no
 * `Transform` composition" independence [InstancedMeshRenderer] already has), MUTATED in place
 * (`origin.set(...)`) rather than reassigned to move an emitter -- e.g. tracking [followEntity]'s
 * position every frame, or gameplay code calling `emitter.origin.set(...)` directly for a spell
 * that follows its caster. Every particle spawns at [origin] with a velocity either randomized
 * within [velocityJitter] of [baseVelocity] (the default, independent per-axis jitter -- reads
 * as a soft cloud) or, when [coneHalfAngleDegrees] is set, randomized in DIRECTION within that
 * half-angle around [baseVelocity]'s own direction at [baseVelocity]'s own speed (reads as a
 * cone/fan burst -- [velocityJitter] is ignored in this mode, the cone already controls spread).
 * A particle lives for [lifetime] seconds, fades linearly from [startAlpha] to 0 over its life,
 * and tints from [startColor] to [endColor] over the same span (constant color when
 * [endColor] equals [startColor], the default) -- no rotation is ever written into a particle's
 * own model matrix (billboarding is camera-facing math done entirely in `particle.wgsl`'s
 * vertex stage), so `ParticleSystem` only ever needs to build a translation+uniform-scale
 * matrix per particle, never a rotation.
 *
 * [maxParticles] bounds the pool -- [spawnRate] particles/second spawn into dead slots until
 * every slot is live, at which point spawning silently stops until a slot frees up (no
 * queueing, no overflow allocation). [burstCount] caps the LIFETIME total instead of the
 * per-frame rate -- `null` (default) spawns forever; a non-null value stops spawning once that
 * many particles have ever been spawned, and [io.github.ronjunevaldoz.awake.scene.rendering
 * .systems.ParticleSystem] destroys this emitter's own entity once every spawned particle has
 * also died, so a one-shot burst cleans itself up with no caller bookkeeping -- see
 * [spawnParticleBurst].
 *
 * [followEntity] re-anchors [origin] to that entity's [io.github.ronjunevaldoz.awake.scene.core
 * .components.Transform] position every frame (read, not composed -- this emitter still has no
 * `Transform` of its own) -- a lightweight "sub-emitter" for a trailing effect (smoke behind a
 * moving fireball) without a real parent/child emitter tree. `null` (default) leaves [origin]
 * exactly where the caller last set it.
 *
 * [groundY] stops a particle's downward fall the instant `position.y` reaches it (velocity
 * zeroed, position clamped, [Particle.settled] set) instead of letting it pass through -- for
 * "leaves settle on the ground" rather than fading out mid-air. `null` (default) never clamps.
 *
 * [frameCount] treats [material]'s texture as a horizontal sprite strip of that many equal-width
 * frames, cycling at [frameRate] frames/second -- for ember flicker or a rotating rune glyph
 * without a static dot. `1` (the default) is a no-op: the texture is sampled unchanged. ponytail:
 * the whole EMITTER shows one shared frame at a time (every particle in this emitter's DrawCall
 * is the same frame simultaneously), not a per-particle-desynced animation -- a real per-particle
 * phase offset would need its own per-instance attribute, not just this one emitter-wide uniform;
 * upgrade path if a demo ever needs particles visibly out of sync with each other.
 *
 * [turbulence] adds a smooth flow-field offset to each particle's velocity every frame, scaled
 * by this strength and sampled at [turbulenceFrequency] (higher = faster spatial variation) --
 * see [io.github.ronjunevaldoz.awake.scene.rendering.systems.turbulenceOffset] for the actual
 * field. `0f` (default) is a no-op. ponytail: a cheap sine-based flow field, not true Perlin/
 * simplex/curl noise -- smooth and non-repeating enough for a demo, not a physically accurate
 * turbulence model; swap `turbulenceOffset`'s implementation for a real noise library if a demo
 * ever needs one.
 *
 * [dynamicSpawnRate], when set, is called once per [io.github.ronjunevaldoz.awake.scene.rendering
 * .systems.ParticleSystem.update] and OVERRIDES [spawnRate] for that frame -- the context-driven
 * emission hook: gameplay code can read live state (player speed, distance to a target, terrain
 * under the emitter) and return a rate that reacts to it, e.g. `{ 20f + playerSpeed() * 5f }` for
 * dust that kicks up harder the faster a character runs. `null` (default) always uses the static
 * [spawnRate].
 *
 * [groundHeightProvider], when set, OVERRIDES [groundY] with a per-position height function
 * `(x, z) -> groundHeight` -- for settling on real (non-flat) terrain instead of one flat plane.
 * `null` (default) falls back to [groundY] (or never clamps, if that's also `null`).
 *
 * [onParticleDeath], when set, is called with (world, this particle's death position) every time
 * a particle dies of old age (not one that's still [Particle.settled] and fading) -- the
 * chained-effect hook for a real sub-emitter tree without composing emitters directly: e.g. a
 * projectile emitter's `onParticleDeath` calls [spawnParticleBurst] to spawn an impact burst
 * wherever each of its own particles expires. `null` (default) does nothing extra on death.
 */
class ParticleEmitter(
    val mesh: Mesh,
    val material: Material,
    val origin: Vec3,
    val maxParticles: Int,
    val spawnRate: Float,
    val lifetime: Float,
    val startAlpha: Float,
    val baseVelocity: Vec3,
    val velocityJitter: Float,
    val scale: Float,
    val startColor: Vec3 = Vec3(1f, 1f, 1f),
    val endColor: Vec3 = startColor,
    val burstCount: Int? = null,
    val coneHalfAngleDegrees: Float? = null,
    var followEntity: Entity? = null,
    val groundY: Float? = null,
    val frameCount: Int = 1,
    val frameRate: Float = 8f,
    val turbulence: Float = 0f,
    val turbulenceFrequency: Float = 1f,
    val dynamicSpawnRate: (() -> Float)? = null,
    val groundHeightProvider: ((x: Float, z: Float) -> Float)? = null,
    val onParticleDeath: ((world: World, position: Vec3) -> Unit)? = null,
) {
    internal val particles: Array<Particle> = Array(maxParticles) { Particle() }

    /** Fractional particles/second carried across frames -- without this, a [spawnRate] that
     * isn't an exact multiple of the frame rate (almost always) either spawns nothing some
     * frames or drops sub-particle remainders every frame, both of which read as visibly
     * uneven/bursty rather than a steady stream. */
    internal var spawnAccumulator: Float = 0f

    /** Total particles spawned across this emitter's whole life -- compared against
     * [burstCount], not reset per frame (unlike [spawnAccumulator]). Unused when [burstCount]
     * is `null`. */
    internal var spawnedTotal: Int = 0

    /** Seconds this emitter has been alive -- drives [frameCount]'s sprite-strip cycling.
     * Unused (stays 0, `frameInfo`'s `currentFrame` stays 0) when [frameCount] is 1. */
    internal var elapsedTime: Float = 0f
}

/**
 * Creates a fresh entity carrying a one-shot [ParticleEmitter] (`burstCount = count`) at
 * [position] and returns it -- the convenience path for gameplay code that wants "play this
 * effect right here, right now" (a spell impact, a hit spark) without hand-rolling entity
 * creation or worrying about cleanup: [io.github.ronjunevaldoz.awake.scene.rendering.systems
 * .ParticleSystem] destroys the returned entity once every spawned particle has died. Example:
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
    world.add(
        entity,
        ParticleEmitter(
            mesh = mesh,
            material = material,
            origin = Vec3(position.x, position.y, position.z),
            maxParticles = count,
            spawnRate = spawnRate,
            lifetime = lifetime,
            startAlpha = startAlpha,
            baseVelocity = baseVelocity,
            velocityJitter = velocityJitter,
            scale = scale,
            startColor = startColor,
            endColor = endColor,
            burstCount = count,
            coneHalfAngleDegrees = coneHalfAngleDegrees,
            followEntity = followEntity,
            groundY = groundY,
        ),
    )
    return entity
}
