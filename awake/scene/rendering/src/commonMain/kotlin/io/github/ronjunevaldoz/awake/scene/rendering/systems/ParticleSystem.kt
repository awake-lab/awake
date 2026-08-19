// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.rendering.systems

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.core.components.Transform
import io.github.ronjunevaldoz.awake.scene.rendering.components.Particle
import io.github.ronjunevaldoz.awake.scene.rendering.components.ParticleEmitter
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private const val DEGREES_TO_RADIANS = kotlin.math.PI.toFloat() / 180f
private const val FULL_TURN_RADIANS = 2f * kotlin.math.PI.toFloat()

/**
 * Spawns/advances every [ParticleEmitter]'s particle pool -- kept separate from
 * [RenderSystem] (which already does draw-call assembly, culling, and LOD selection) so this
 * stays a single-responsibility simulation step, same "own component, own system" shape
 * [InstancedMeshRenderer]/[LodGroup] already established. `RenderSystem` reads the pool's
 * current live particles fresh each frame; this system owns writing to it.
 *
 * Also owns [ParticleEmitter.lifecycle]'s `burstCount` cleanup: a one-shot emitter's entity is
 * destroyed once every particle it will ever spawn has died, so [io.github.ronjunevaldoz.awake
 * .scene.rendering.components.spawnParticleBurst] callers never need their own bookkeeping.
 */
class ParticleSystem : System {
    private val spentEntities = ArrayList<Entity>()

    override fun update(world: World, delta: Float) {
        spentEntities.clear()
        world.queryEach(ParticleEmitter::class) { entity, emitter ->
            emitter.elapsedTime += delta
            followOrigin(world, emitter)
            spawn(emitter, delta)
            advance(world, emitter, delta)
            if (isSpent(emitter)) spentEntities += entity
        }
        spentEntities.forEach { world.destroy(it) }
    }

    /** Re-anchors [ParticleEmitter.origin] to [ParticleEmitter.dynamics]' `followEntity`'s
     * current [Transform] position, if set -- the lightweight "sub-emitter" trailing-effect
     * path (see [io.github.ronjunevaldoz.awake.scene.rendering.components.ParticleDynamics]'s
     * own doc comment). A no-op when the followed entity has no [Transform] (already destroyed,
     * or was never one) -- the emitter just keeps spawning from wherever [origin] last was,
     * rather than crashing. */
    private fun followOrigin(world: World, emitter: ParticleEmitter) {
        val target = emitter.dynamics.followEntity ?: return
        val transform = world.get<Transform>(target) ?: return
        emitter.origin.set(transform.position)
    }

    private fun isSpent(emitter: ParticleEmitter): Boolean {
        val burstCount = emitter.lifecycle.burstCount ?: return false
        return emitter.spawnedTotal >= burstCount && emitter.particles.none { it.alive }
    }

    private fun spawn(emitter: ParticleEmitter, delta: Float) {
        val burstCount = emitter.lifecycle.burstCount
        if (burstCount != null && emitter.spawnedTotal >= burstCount) return
        // Context-driven emission: a caller-supplied rate read fresh every frame (player speed,
        // distance to a target, ...) overrides the static spawnRate when set -- see
        // ParticleDynamics.dynamicSpawnRate's own doc comment.
        val spawnRate = emitter.dynamics.dynamicSpawnRate?.invoke() ?: emitter.spawnRate
        emitter.spawnAccumulator += spawnRate * delta
        // ponytail: clamps the worst case to "refill the whole pool in one frame" rather than
        // true unbounded growth -- a pool that stays completely full for a long stretch would
        // otherwise accumulate an ever-growing backlog that dumps as one mega-burst the moment
        // a slot frees up. Good enough for a first slice; a real rate-limited drain is the
        // upgrade if a demo ever needs a perfectly steady stream under sustained pool pressure.
        emitter.spawnAccumulator = emitter.spawnAccumulator.coerceAtMost(emitter.maxParticles.toFloat())
        while (emitter.spawnAccumulator >= 1f) {
            if (burstCount != null && emitter.spawnedTotal >= burstCount) break
            val slot = emitter.particles.firstOrNull { !it.alive } ?: break
            emitter.spawnAccumulator -= 1f
            emitter.spawnedTotal += 1
            slot.alive = true
            val spawnPosition = spawnPosition(emitter)
            slot.position.set(spawnPosition.x, spawnPosition.y, spawnPosition.z)
            slot.velocity.set(spawnVelocity(emitter, spawnPosition))
            slot.age = 0f
            slot.lifetime = emitter.lifetime
            slot.startAlpha = emitter.startAlpha
            slot.scale = emitter.scale
            slot.settled = false
        }
    }

    /** [ParticleMotion.spawnRadius] > 0 spawns on a random point around a flat horizontal ring
     * of that radius centered on [ParticleEmitter.origin], instead of exactly at `origin` (the
     * `0f` default) -- the "charging circle" cast-VFX spawn shape, meant to pair with
     * [ParticleMotion.convergeToOrigin]. */
    private fun spawnPosition(emitter: ParticleEmitter): Vec3 {
        val radius = emitter.motion.spawnRadius
        if (radius <= 0f) return emitter.origin
        val angle = Random.nextFloat() * FULL_TURN_RADIANS
        return Vec3(
            emitter.origin.x + cos(angle) * radius,
            emitter.origin.y,
            emitter.origin.z + sin(angle) * radius,
        )
    }

    /** Velocity resolution, in priority order -- see [io.github.ronjunevaldoz.awake.scene
     * .rendering.components.ParticleMotion]'s own doc comment for the full 3-case breakdown
     * (converge-to-origin, cone burst, per-axis jitter). */
    private fun spawnVelocity(emitter: ParticleEmitter, spawnPosition: Vec3): Vec3 {
        val motion = emitter.motion
        if (motion.convergeToOrigin) {
            val toOrigin = emitter.origin - spawnPosition
            val direction = if (toOrigin.length3() > 0f) toOrigin.normalized() else Vec3(0f, 1f, 0f)
            return direction * motion.baseVelocity.length3()
        }
        val coneHalfAngleDegrees = motion.coneHalfAngleDegrees
        val speed = motion.baseVelocity.length3()
        if (coneHalfAngleDegrees != null && speed > 0f) {
            return coneDirection(motion.baseVelocity.normalized(), coneHalfAngleDegrees) * speed
        }
        return Vec3(
            motion.baseVelocity.x + jitter(motion.velocityJitter),
            motion.baseVelocity.y + jitter(motion.velocityJitter),
            motion.baseVelocity.z + jitter(motion.velocityJitter),
        )
    }

    /** A random unit vector within [halfAngleDegrees] of [axis] -- builds an orthonormal
     * (right, up) basis perpendicular to [axis] (Gram-Schmidt against an arbitrary non-parallel
     * fallback axis), then picks a uniform-random polar angle in `[0, halfAngle]` and azimuth
     * in `[0, 2*PI)` around it. Uniform in ANGLE, not solid angle -- biases slightly toward the
     * cone's edge rather than its center; fine for a visual burst, not a physically exact
     * sampler. */
    private fun coneDirection(axis: Vec3, halfAngleDegrees: Float): Vec3 {
        val fallback = if (kotlin.math.abs(axis.y) > 0.99f) Vec3(1f, 0f, 0f) else Vec3(0f, 1f, 0f)
        val right = axis.cross(fallback).normalized()
        val up = right.cross(axis)
        val theta = Random.nextFloat() * halfAngleDegrees * DEGREES_TO_RADIANS
        val phi = Random.nextFloat() * FULL_TURN_RADIANS
        val cosTheta = cos(theta)
        val sinTheta = sin(theta)
        return (axis * cosTheta) + (right * (sinTheta * cos(phi))) + (up * (sinTheta * sin(phi)))
    }

    private fun advance(world: World, emitter: ParticleEmitter, delta: Float) {
        emitter.particles.forEach { particle ->
            if (!particle.alive) return@forEach
            particle.age += delta
            if (particle.age >= particle.lifetime) {
                // Sub-emitter chaining: fires BEFORE reset() clears position, so the callback
                // sees exactly where this particle died -- see ParticleLifecycle.onParticleDeath's
                // own doc comment.
                emitter.lifecycle.onParticleDeath?.invoke(world, particle.position)
                particle.reset()
                return@forEach
            }
            if (particle.settled) return@forEach
            val turbulence = emitter.motion.turbulence
            if (turbulence != 0f) {
                val flow = turbulenceOffset(particle.position, emitter.elapsedTime, emitter.motion.turbulenceFrequency)
                particle.velocity.x += flow.x * turbulence * delta
                particle.velocity.y += flow.y * turbulence * delta
                particle.velocity.z += flow.z * turbulence * delta
            }
            particle.position.x += particle.velocity.x * delta
            particle.position.y += particle.velocity.y * delta
            particle.position.z += particle.velocity.z * delta
            // groundHeightProvider (real terrain) takes priority over the flat groundY plane --
            // see ParticleGround's own doc comment.
            val groundHeight = emitter.ground.groundHeightProvider?.invoke(particle.position.x, particle.position.z)
                ?: emitter.ground.groundY
            if (groundHeight != null && particle.position.y <= groundHeight) {
                particle.position.y = groundHeight
                particle.velocity.set(0f, 0f, 0f)
                particle.settled = true
            }
        }
    }

    private fun jitter(magnitude: Float): Float = (Random.nextFloat() * 2f - 1f) * magnitude
}

/** This particle's current linear fade -- [ParticleEmitter.startAlpha] at spawn, 0 at death.
 * Read by [RenderSystem] to build [io.github.ronjunevaldoz.awake.render.renderer.DrawCall
 * .instanceColors]; not stored on [Particle] itself since it's fully derived from [Particle
 * .age]/`lifetime`/`startAlpha`, not independent state. */
internal fun Particle.currentAlpha(): Float = startAlpha * (1f - (age / lifetime).coerceIn(0f, 1f))

/** This particle's current tint -- linearly interpolated from [ParticleEmitter.visual]'s
 * `startColor` to `endColor` over its life (constant when the two are equal, the common case).
 * Per-particle, not per-emitter: every particle ages independently, so a burst's later-spawned
 * particles show an earlier point in the gradient than its first-spawned ones at any given
 * frame -- exactly what "fire fades orange to yellow within one burst" needs. */
internal fun Particle.currentColor(emitter: ParticleEmitter): Vec3 {
    val t = (age / lifetime).coerceIn(0f, 1f)
    val visual = emitter.visual
    return Vec3(
        visual.startColor.x + (visual.endColor.x - visual.startColor.x) * t,
        visual.startColor.y + (visual.endColor.y - visual.startColor.y) * t,
        visual.startColor.z + (visual.endColor.z - visual.startColor.z) * t,
    )
}

/** A smooth, deterministic flow-field velocity offset at [position]/[time] -- see
 * [io.github.ronjunevaldoz.awake.scene.rendering.components.ParticleMotion]'s own doc comment
 * for why this is a cheap sine-based approximation, not true Perlin/simplex/curl noise. Each
 * axis samples a different phase-shifted combination of the other two spatial axes plus time,
 * so the field varies smoothly in space and animates smoothly over time without an explicit
 * noise table/library. */
internal fun turbulenceOffset(position: Vec3, time: Float, frequency: Float): Vec3 {
    val x = position.x * frequency
    val y = position.y * frequency
    val z = position.z * frequency
    return Vec3(
        sin(y + time) * cos(z * 0.7f + time * 1.3f),
        sin(z + time * 0.8f) * cos(x * 0.7f + time),
        sin(x + time * 1.1f) * cos(y * 0.7f + time * 0.9f),
    )
}
