// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.rendering

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.scene.rendering.components.ParticleEmitter
import io.github.ronjunevaldoz.awake.scene.rendering.systems.ParticleSystem
import io.github.ronjunevaldoz.awake.scene.rendering.systems.currentAlpha
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Pure CPU simulation logic -- no GPU/[io.github.ronjunevaldoz.awake.render.renderer.Renderer]
 * involved, unlike [RenderSystemTest]'s own fixture (which this test doesn't need at all: it
 * asserts on [ParticleEmitter]'s own pool state directly, not on what reached a renderer). */
class ParticleSystemTest {

    private fun emitter(
        maxParticles: Int = 4,
        spawnRate: Float = 2f,
        lifetime: Float = 1f,
        startAlpha: Float = 1f,
    ) = ParticleEmitter(
        mesh = fakeMesh(),
        material = fakeMaterial(),
        origin = Vec3(0f, 0f, 0f),
        maxParticles = maxParticles,
        spawnRate = spawnRate,
        lifetime = lifetime,
        startAlpha = startAlpha,
        baseVelocity = Vec3(0f, 1f, 0f),
        velocityJitter = 0f,
        scale = 1f,
    )

    @Test
    fun spawnRateConvergesToSpawnRateTimesDelta() {
        val world = World()
        world.add(world.create(), emitter(maxParticles = 100, spawnRate = 10f))
        val system = ParticleSystem()

        // 10 particles/second * 1 second, in 60 steps of 1/60s -- exact since 10/60 accumulates
        // to whole particles evenly (no fractional remainder lost or double-counted).
        repeat(60) { system.update(world, 1f / 60f) }

        val emitter = world.family<ParticleEmitter>().components().first()
        val alive = emitter.particles.count { it.alive }
        assertEquals(10, alive, "expected spawnRate*elapsedSeconds particles alive")
    }

    @Test
    fun spawningStopsOnceEveryPoolSlotIsLive() {
        val world = World()
        // lifetime long relative to the 1s step below -- a particle spawned this frame must
        // still be alive when the count is asserted, not die same-frame from age==lifetime.
        world.add(world.create(), emitter(maxParticles = 3, spawnRate = 1000f, lifetime = 10f))
        val system = ParticleSystem()

        system.update(world, 1f) // would spawn 1000 particles/second if the pool allowed it

        val emitter = world.family<ParticleEmitter>().components().first()
        assertEquals(3, emitter.particles.count { it.alive }, "pool must never exceed maxParticles")
    }

    @Test
    fun aParticleDiesExactlyAtItsLifetimeAndItsSlotIsReusable() {
        val world = World()
        world.add(world.create(), emitter(maxParticles = 1, spawnRate = 1000f, lifetime = 0.5f))
        val system = ParticleSystem()

        system.update(world, 0.1f) // spawns into the pool's one slot
        val emitter = world.family<ParticleEmitter>().components().first()
        assertTrue(emitter.particles[0].alive, "the one slot should be alive after the first frame")

        system.update(world, 0.5f) // pushes age past lifetime -- the slot must die and reset
        assertEquals(false, emitter.particles[0].alive, "particle must die once age >= lifetime")
        assertEquals(0f, emitter.particles[0].age, "a dead slot must be fully reset, not left dirty")

        // A dead slot must be reusable by the NEXT spawn -- the exact pool-recycling behavior
        // Poolable.reset()'s own completeness rule exists to protect.
        system.update(world, 0.1f)
        assertTrue(emitter.particles[0].alive, "a reset slot must accept a new spawn")
    }

    @Test
    fun aliveParticlesMoveByVelocityTimesDelta() {
        val world = World()
        world.add(world.create(), emitter(maxParticles = 1, spawnRate = 1000f, lifetime = 10f))
        val system = ParticleSystem()

        system.update(world, 0.01f) // spawn
        val particle = world.family<ParticleEmitter>().components().first().particles[0]
        val yBeforeMove = particle.position.y

        system.update(world, 0.5f) // baseVelocity = (0, 1, 0) -- half a second of upward motion
        assertEquals(yBeforeMove + 0.5f, particle.position.y, 1e-4f)
    }

    @Test
    fun currentAlphaFadesLinearlyFromStartAlphaToZero() {
        val world = World()
        world.add(world.create(), emitter(maxParticles = 1, spawnRate = 1000f, lifetime = 1f, startAlpha = 0.8f))
        val system = ParticleSystem()

        system.update(world, 0.001f) // spawn, age ~= 0
        val particle = world.family<ParticleEmitter>().components().first().particles[0]
        assertEquals(0.8f, particle.currentAlpha(), 0.01f, "just spawned, alpha should be ~startAlpha")

        system.update(world, 0.5f) // age ~= 0.5 of a 1s lifetime -- halfway faded
        assertEquals(0.4f, particle.currentAlpha(), 0.02f, "halfway through lifetime, alpha should be ~half")
    }

    private fun fakeMesh(): Mesh = object : Mesh {
        override val format = VertexFormat.PositionUv
        override fun bind(commandBuffer: Long) = Unit
        override fun draw(commandBuffer: Long) = Unit
        override fun destroy() = Unit
    }

    private fun fakeMaterial(): Material = object : Material {
        override fun updateUniformBuffer(mvp: FloatArray) = Unit
        override fun bind(commandBuffer: Long, pipelineLayout: Long) = Unit
        override fun destroy() = Unit
    }
}
