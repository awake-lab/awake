// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.rendering.components

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.Poolable
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

    override fun reset() {
        alive = false
        position.set(0f, 0f, 0f)
        velocity.set(0f, 0f, 0f)
        age = 0f
        lifetime = 0f
        startAlpha = 0f
        scale = 1f
    }
}

/**
 * A fixed-capacity pool of camera-facing billboard particles, spawned/advanced by
 * [io.github.ronjunevaldoz.awake.scene.rendering.systems.ParticleSystem] and drawn via GPU
 * instancing by [io.github.ronjunevaldoz.awake.scene.rendering.systems.RenderSystem] --
 * mirrors [InstancedMeshRenderer]'s own "N copies of one mesh/material in one draw call"
 * shape, with the same "no `Transform`/entity-hierarchy requirement" independence, plus
 * per-instance alpha ([io.github.ronjunevaldoz.awake.render.renderer.DrawCall
 * .instanceAlphas]) for independent fade -- something static instancing has no use for.
 *
 * [origin] is the emitter's own fixed spawn point (world space, not entity-relative -- same
 * "no `Transform` composition" independence [InstancedMeshRenderer] already has). Every
 * particle spawns at [origin] with a velocity randomized within [velocityJitter] of
 * [baseVelocity], lives for [lifetime] seconds, and fades linearly from [startAlpha] to 0 over
 * its life -- no rotation is ever written into a particle's own model matrix (billboarding is
 * camera-facing math done entirely in `particle.wgsl`'s vertex stage), so `ParticleSystem`
 * only ever needs to build a translation+uniform-scale matrix per particle, never a rotation.
 *
 * [maxParticles] bounds the pool -- [spawnRate] particles/second spawn into dead slots until
 * every slot is live, at which point spawning silently stops until a slot frees up (no
 * queueing, no overflow allocation).
 *
 * [tintColor] multiplies the whole emitter's sampled texture color (`particle.wgsl`'s
 * fragment stage) -- one color per EMITTER, not per particle (every particle in a burst is
 * the same effect, e.g. all orange for fire), which is why it rides in [RenderSystem
 * .kt][io.github.ronjunevaldoz.awake.scene.rendering.systems.RenderSystem]'s per-DrawCall
 * `extraUniformFloats` alongside the camera basis, not in a per-instance buffer.
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
    val tintColor: Vec3 = Vec3(1f, 1f, 1f),
) {
    internal val particles: Array<Particle> = Array(maxParticles) { Particle() }

    /** Fractional particles/second carried across frames -- without this, a [spawnRate] that
     * isn't an exact multiple of the frame rate (almost always) either spawns nothing some
     * frames or drops sub-particle remainders every frame, both of which read as visibly
     * uneven/bursty rather than a steady stream. */
    internal var spawnAccumulator: Float = 0f
}
