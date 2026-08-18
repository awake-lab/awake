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
 * Also owns [ParticleEmitter.burstCount] cleanup: a one-shot emitter's entity is destroyed once
 * every particle it will ever spawn has died, so [io.github.ronjunevaldoz.awake.scene.rendering
 * .components.spawnParticleBurst] callers never need their own bookkeeping.
 */
class ParticleSystem : System {
    private val spentEntities = ArrayList<Entity>()

    override fun update(world: World, delta: Float) {
        spentEntities.clear()
        world.queryEach(ParticleEmitter::class) { entity, emitter ->
            emitter.elapsedTime += delta
            followOrigin(world, emitter)
            spawn(emitter, delta)
            advance(emitter, delta)
            if (isSpent(emitter)) spentEntities += entity
        }
        spentEntities.forEach { world.destroy(it) }
    }

    /** Re-anchors [ParticleEmitter.origin] to [ParticleEmitter.followEntity]'s current
     * [Transform] position, if set -- the lightweight "sub-emitter" trailing-effect path (see
     * [ParticleEmitter.followEntity]'s own doc comment). A no-op when the followed entity has
     * no [Transform] (already destroyed, or was never one) -- the emitter just keeps spawning
     * from wherever [origin] last was, rather than crashing. */
    private fun followOrigin(world: World, emitter: ParticleEmitter) {
        val target = emitter.followEntity ?: return
        val transform = world.get<Transform>(target) ?: return
        emitter.origin.set(transform.position)
    }

    private fun isSpent(emitter: ParticleEmitter): Boolean {
        val burstCount = emitter.burstCount ?: return false
        return emitter.spawnedTotal >= burstCount && emitter.particles.none { it.alive }
    }

    private fun spawn(emitter: ParticleEmitter, delta: Float) {
        val burstCount = emitter.burstCount
        if (burstCount != null && emitter.spawnedTotal >= burstCount) return
        emitter.spawnAccumulator += emitter.spawnRate * delta
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
            slot.position.set(emitter.origin.x, emitter.origin.y, emitter.origin.z)
            slot.velocity.set(spawnVelocity(emitter))
            slot.age = 0f
            slot.lifetime = emitter.lifetime
            slot.startAlpha = emitter.startAlpha
            slot.scale = emitter.scale
            slot.settled = false
        }
    }

    /** [ParticleEmitter.coneHalfAngleDegrees] set (and [ParticleEmitter.baseVelocity] non-zero)
     * randomizes DIRECTION within that half-angle around `baseVelocity`'s own direction, at
     * `baseVelocity`'s own speed -- a cone/fan burst. Otherwise falls back to the default
     * per-axis-independent jitter (a soft cloud), same as before cone spread existed. */
    private fun spawnVelocity(emitter: ParticleEmitter): Vec3 {
        val coneHalfAngleDegrees = emitter.coneHalfAngleDegrees
        val speed = emitter.baseVelocity.length3()
        if (coneHalfAngleDegrees != null && speed > 0f) {
            return coneDirection(emitter.baseVelocity.normalized(), coneHalfAngleDegrees) * speed
        }
        return Vec3(
            emitter.baseVelocity.x + jitter(emitter.velocityJitter),
            emitter.baseVelocity.y + jitter(emitter.velocityJitter),
            emitter.baseVelocity.z + jitter(emitter.velocityJitter),
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

    private fun advance(emitter: ParticleEmitter, delta: Float) {
        val groundY = emitter.groundY
        emitter.particles.forEach { particle ->
            if (!particle.alive) return@forEach
            particle.age += delta
            if (particle.age >= particle.lifetime) {
                particle.reset()
                return@forEach
            }
            if (particle.settled) return@forEach
            particle.position.x += particle.velocity.x * delta
            particle.position.y += particle.velocity.y * delta
            particle.position.z += particle.velocity.z * delta
            if (groundY != null && particle.position.y <= groundY) {
                particle.position.y = groundY
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

/** This particle's current tint -- linearly interpolated from [ParticleEmitter.startColor] to
 * [ParticleEmitter.endColor] over its life (constant when the two are equal, the common case).
 * Per-particle, not per-emitter: every particle ages independently, so a burst's later-spawned
 * particles show an earlier point in the gradient than its first-spawned ones at any given
 * frame -- exactly what "fire fades orange to yellow within one burst" needs. */
internal fun Particle.currentColor(emitter: ParticleEmitter): Vec3 {
    val t = (age / lifetime).coerceIn(0f, 1f)
    return Vec3(
        emitter.startColor.x + (emitter.endColor.x - emitter.startColor.x) * t,
        emitter.startColor.y + (emitter.endColor.y - emitter.startColor.y) * t,
        emitter.startColor.z + (emitter.endColor.z - emitter.startColor.z) * t,
    )
}
