// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.rendering.systems

import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.rendering.components.Particle
import io.github.ronjunevaldoz.awake.scene.rendering.components.ParticleEmitter
import kotlin.random.Random

/**
 * Spawns/advances every [ParticleEmitter]'s particle pool -- kept separate from
 * [RenderSystem] (which already does draw-call assembly, culling, and LOD selection) so this
 * stays a single-responsibility simulation step, same "own component, own system" shape
 * [InstancedMeshRenderer]/[LodGroup] already established. `RenderSystem` reads the pool's
 * current live particles fresh each frame; this system owns writing to it.
 */
class ParticleSystem : System {
    override fun update(world: World, delta: Float) {
        world.queryEach(ParticleEmitter::class) { _, emitter ->
            spawn(emitter, delta)
            advance(emitter, delta)
        }
    }

    private fun spawn(emitter: ParticleEmitter, delta: Float) {
        emitter.spawnAccumulator += emitter.spawnRate * delta
        // ponytail: clamps the worst case to "refill the whole pool in one frame" rather than
        // true unbounded growth -- a pool that stays completely full for a long stretch would
        // otherwise accumulate an ever-growing backlog that dumps as one mega-burst the moment
        // a slot frees up. Good enough for a first slice; a real rate-limited drain is the
        // upgrade if a demo ever needs a perfectly steady stream under sustained pool pressure.
        emitter.spawnAccumulator = emitter.spawnAccumulator.coerceAtMost(emitter.maxParticles.toFloat())
        while (emitter.spawnAccumulator >= 1f) {
            val slot = emitter.particles.firstOrNull { !it.alive } ?: break
            emitter.spawnAccumulator -= 1f
            slot.alive = true
            slot.position.set(emitter.origin.x, emitter.origin.y, emitter.origin.z)
            slot.velocity.set(
                emitter.baseVelocity.x + jitter(emitter.velocityJitter),
                emitter.baseVelocity.y + jitter(emitter.velocityJitter),
                emitter.baseVelocity.z + jitter(emitter.velocityJitter),
            )
            slot.age = 0f
            slot.lifetime = emitter.lifetime
            slot.startAlpha = emitter.startAlpha
            slot.scale = emitter.scale
        }
    }

    private fun advance(emitter: ParticleEmitter, delta: Float) {
        emitter.particles.forEach { particle ->
            if (!particle.alive) return@forEach
            particle.age += delta
            if (particle.age >= particle.lifetime) {
                particle.reset()
                return@forEach
            }
            particle.position.x += particle.velocity.x * delta
            particle.position.y += particle.velocity.y * delta
            particle.position.z += particle.velocity.z * delta
        }
    }

    private fun jitter(magnitude: Float): Float = (Random.nextFloat() * 2f - 1f) * magnitude
}

/** This particle's current linear fade -- [ParticleEmitter.startAlpha] at spawn, 0 at death.
 * Read by [RenderSystem] to build [io.github.ronjunevaldoz.awake.render.renderer.DrawCall
 * .instanceAlphas]; not stored on [Particle] itself since it's fully derived from [Particle
 * .age]/`lifetime`/`startAlpha`, not independent state. */
internal fun Particle.currentAlpha(): Float = startAlpha * (1f - (age / lifetime).coerceIn(0f, 1f))
