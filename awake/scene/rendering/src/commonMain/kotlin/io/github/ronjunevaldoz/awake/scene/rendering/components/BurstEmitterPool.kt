// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.rendering.components

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh

/** [ParticleEmitter.mesh]/[ParticleEmitter.material]/[ParticleEmitter.maxParticles] together
 * decide the shape of the backing `Array<Particle>` and GPU instance buffers -- two bursts only
 * share a pooled emitter when all three match. `Mesh`/`Material` have no custom `equals()`, so
 * this compares by reference, exactly right: the same mesh/material INSTANCE reused across
 * bursts is a shape match, two different instances never accidentally collide. */
private data class BurstPoolKey(val mesh: Mesh, val material: Material, val maxParticles: Int)

/**
 * Free-list of spent burst [ParticleEmitter]s, keyed by [BurstPoolKey] -- [spawnParticleBurst]
 * borrows from here instead of always allocating a fresh emitter (and its backing
 * `Array<Particle>`/instance buffers). Entirely outside the ECS: `World.create()`/`destroy()`
 * are untouched by this, it only avoids reallocating the `ParticleEmitter` object itself on a
 * rapid-fire same-shaped burst (machine-gun impacts, footstep dust).
 *
 * Hand-attached, continuously-running emitters (Studio demos, a `followEntity` aura) never go
 * through this -- only [spawnParticleBurst]'s one-shot path does, since that's the only shape
 * that churns emitters at a rate worth pooling.
 */
internal object BurstEmitterPool {
    private val free = mutableMapOf<BurstPoolKey, MutableList<ParticleEmitter>>()

    fun obtain(
        mesh: Mesh,
        material: Material,
        maxParticles: Int,
        origin: Vec3,
        spawnRate: Float,
        lifetime: Float,
        startAlpha: Float,
        scale: Float,
        motion: ParticleMotion,
        visual: ParticleVisual,
        ground: ParticleGround,
        lifecycle: ParticleLifecycle,
        dynamics: ParticleDynamics,
    ): ParticleEmitter {
        val key = BurstPoolKey(mesh, material, maxParticles)
        val pooled = free[key]?.removeLastOrNull()
        val emitter = if (pooled != null) {
            pooled.reconfigure(origin, spawnRate, lifetime, startAlpha, scale, motion, visual, ground, lifecycle, dynamics)
            pooled
        } else {
            ParticleEmitter(
                mesh = mesh,
                material = material,
                origin = Vec3(origin.x, origin.y, origin.z),
                maxParticles = maxParticles,
                spawnRate = spawnRate,
                lifetime = lifetime,
                startAlpha = startAlpha,
                scale = scale,
                motion = motion,
                visual = visual,
                ground = ground,
                lifecycle = lifecycle,
                dynamics = dynamics,
            )
        }
        emitter.pooledForBurst = true
        return emitter
    }

    /** Returns [emitter] to its shape's free list for the next matching burst -- called by
     * [io.github.ronjunevaldoz.awake.scene.rendering.systems.ParticleSystem] right before it
     * destroys a spent emitter's entity, gated on [ParticleEmitter.pooledForBurst] so only
     * emitters that actually came from [obtain] are ever pooled. */
    fun release(emitter: ParticleEmitter) {
        val key = BurstPoolKey(emitter.mesh, emitter.material, emitter.maxParticles)
        free.getOrPut(key) { mutableListOf() } += emitter
    }
}
