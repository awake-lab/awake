// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.rendering

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.scene.core.components.Transform
import io.github.ronjunevaldoz.awake.scene.rendering.components.ParticleDynamics
import io.github.ronjunevaldoz.awake.scene.rendering.components.ParticleEmitter
import io.github.ronjunevaldoz.awake.scene.rendering.components.ParticleGround
import io.github.ronjunevaldoz.awake.scene.rendering.components.ParticleLifecycle
import io.github.ronjunevaldoz.awake.scene.rendering.components.ParticleMotion
import io.github.ronjunevaldoz.awake.scene.rendering.components.ParticleVisual
import io.github.ronjunevaldoz.awake.scene.rendering.components.spawnParticleBurst
import io.github.ronjunevaldoz.awake.scene.rendering.systems.ParticleSystem
import io.github.ronjunevaldoz.awake.scene.rendering.systems.currentAlpha
import io.github.ronjunevaldoz.awake.scene.rendering.systems.currentColor
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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
        scale = 1f,
        motion = ParticleMotion(baseVelocity = Vec3(0f, 1f, 0f), velocityJitter = 0f),
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

    @Test
    fun burstEmitterDestroysItsOwnEntityOnceEveryParticleHasDied() {
        val world = World()
        val entity = world.create()
        world.add(
            entity,
            ParticleEmitter(
                mesh = fakeMesh(), material = fakeMaterial(), origin = Vec3(0f, 0f, 0f),
                maxParticles = 5, spawnRate = 1000f, lifetime = 0.2f, startAlpha = 1f, scale = 1f,
                motion = ParticleMotion(baseVelocity = Vec3(0f, 1f, 0f)),
                lifecycle = ParticleLifecycle(burstCount = 5),
            ),
        )
        val system = ParticleSystem()

        system.update(world, 0.01f) // spawns all 5 (burstCount reached), still alive
        assertEquals(1, world.family<ParticleEmitter>().size, "burst entity still alive mid-life")

        system.update(world, 1f) // well past lifetime -- every particle dies this step
        assertEquals(0, world.family<ParticleEmitter>().size, "burst entity destroyed once spent")
    }

    @Test
    fun nonBurstEmitterIsNeverDestroyed() {
        val world = World()
        world.add(world.create(), emitter(maxParticles = 1, spawnRate = 1000f, lifetime = 0.1f))
        val system = ParticleSystem()

        repeat(10) { system.update(world, 0.5f) } // many spawn/die cycles
        assertEquals(1, world.family<ParticleEmitter>().size, "burstCount == null must never self-destroy")
    }

    @Test
    fun spawnParticleBurstHelperCreatesAOneShotEmitterAtThePassedPosition() {
        val world = World()
        val entity = spawnParticleBurst(
            world, fakeMesh(), fakeMaterial(), position = Vec3(3f, 2f, 1f),
            count = 4, spawnRate = 1000f, lifetime = 5f, startAlpha = 1f,
            baseVelocity = Vec3(0f, 0f, 0f),
        )
        val emitter = requireNotNull(world.get<ParticleEmitter>(entity))
        assertEquals(4, emitter.lifecycle.burstCount)
        assertEquals(Vec3(3f, 2f, 1f), emitter.origin)
    }

    @Test
    fun coneSpreadKeepsEveryVelocityWithinTheHalfAngleOfBaseVelocity() {
        val world = World()
        val baseVelocity = Vec3(0f, 1f, 0f)
        world.add(
            world.create(),
            ParticleEmitter(
                mesh = fakeMesh(), material = fakeMaterial(), origin = Vec3(0f, 0f, 0f),
                maxParticles = 50, spawnRate = 1000f, lifetime = 10f, startAlpha = 1f, scale = 1f,
                motion = ParticleMotion(baseVelocity = baseVelocity, coneHalfAngleDegrees = 30f),
            ),
        )
        val system = ParticleSystem()
        system.update(world, 1f) // spawns into every slot

        val emitter = world.family<ParticleEmitter>().components().first()
        val baseDirection = baseVelocity.normalized()
        val cosHalfAngle = kotlin.math.cos(30f * (kotlin.math.PI.toFloat() / 180f))
        emitter.particles.filter { it.alive }.forEach { particle ->
            val cosAngle = particle.velocity.normalized().let {
                it.x * baseDirection.x + it.y * baseDirection.y + it.z * baseDirection.z
            }
            // cos is monotonically decreasing over [0, 180deg] -- angle <= halfAngle iff
            // cos(angle) >= cos(halfAngle).
            assertTrue(cosAngle >= cosHalfAngle - 1e-4f, "velocity direction exceeded the cone's half-angle")
        }
    }

    @Test
    fun chargingCircleSpawnsOnARingAndConvergesInward() {
        val world = World()
        val origin = Vec3(0f, 0f, 0f)
        world.add(
            world.create(),
            ParticleEmitter(
                mesh = fakeMesh(), material = fakeMaterial(), origin = origin,
                maxParticles = 50, spawnRate = 1000f, lifetime = 10f, startAlpha = 1f, scale = 1f,
                motion = ParticleMotion(
                    baseVelocity = Vec3(0f, 2f, 0f), // magnitude only -- direction is overridden
                    spawnRadius = 3f,
                    convergeToOrigin = true,
                ),
            ),
        )
        val system = ParticleSystem()
        // A small delta -- spawn() and advance() share it within one update() call, so a large
        // delta (e.g. 1f) would both spawn every particle AND move them 2 units/sec * 1s toward
        // origin before this test ever inspects a spawn position. 0.01s of drift at speed 2 is
        // ~0.02 units, well inside the tolerances below.
        system.update(world, 0.01f)

        val emitter = world.family<ParticleEmitter>().components().first()
        emitter.particles.filter { it.alive }.forEach { particle ->
            // Spawned on the ring: distance from origin must be ~spawnRadius (y unchanged).
            val spawnDistance = kotlin.math.sqrt(particle.position.x * particle.position.x + particle.position.z * particle.position.z)
            assertEquals(3f, spawnDistance, 0.05f, "must spawn ~spawnRadius from origin")
            // Converging: velocity must point back toward origin, at baseVelocity's own speed.
            val towardOrigin = (origin - particle.position).normalized()
            val velocityDirection = particle.velocity.normalized()
            val dot = towardOrigin.x * velocityDirection.x + towardOrigin.y * velocityDirection.y + towardOrigin.z * velocityDirection.z
            assertTrue(dot > 0.99f, "velocity must point toward origin, not baseVelocity's own direction")
            assertEquals(2f, particle.velocity.length3(), 0.01f, "speed must match baseVelocity's magnitude")
        }
    }

    @Test
    fun aFallingParticleSettlesAtGroundYInsteadOfPassingThrough() {
        val world = World()
        world.add(
            world.create(),
            ParticleEmitter(
                mesh = fakeMesh(), material = fakeMaterial(), origin = Vec3(0f, 5f, 0f),
                maxParticles = 1, spawnRate = 1000f, lifetime = 10f, startAlpha = 1f, scale = 1f,
                motion = ParticleMotion(baseVelocity = Vec3(0f, -10f, 0f)),
                ground = ParticleGround(groundY = 0f),
            ),
        )
        val system = ParticleSystem()

        system.update(world, 0.01f) // spawn near y=5
        system.update(world, 1f) // -10 units/sec for 1s would put it well below ground

        val particle = world.family<ParticleEmitter>().components().first().particles[0]
        assertEquals(0f, particle.position.y, 1e-4f, "particle must clamp at groundY, not pass through")
        assertEquals(0f, particle.velocity.y, "a settled particle's velocity must be zeroed")
        assertTrue(particle.alive, "a settled particle keeps aging/fading, it doesn't die on landing")
    }

    @Test
    fun followEntityReanchorsOriginToTheFollowedTransformEachFrame() {
        val world = World()
        val target = world.create()
        world.add(target, Transform(position = Vec3(1f, 2f, 3f)))
        val emitterEntity = world.create()
        world.add(
            emitterEntity,
            ParticleEmitter(
                mesh = fakeMesh(), material = fakeMaterial(), origin = Vec3(0f, 0f, 0f),
                maxParticles = 1, spawnRate = 0f, lifetime = 1f, startAlpha = 1f, scale = 1f,
                motion = ParticleMotion(baseVelocity = Vec3(0f, 0f, 0f)),
                dynamics = ParticleDynamics(followEntity = target),
            ),
        )
        val system = ParticleSystem()

        system.update(world, 0.016f)
        val emitter = requireNotNull(world.get<ParticleEmitter>(emitterEntity))
        assertEquals(Vec3(1f, 2f, 3f), emitter.origin)

        world.get<Transform>(target)!!.position.set(5f, 6f, 7f)
        system.update(world, 0.016f)
        assertEquals(Vec3(5f, 6f, 7f), emitter.origin, "origin must re-anchor every frame, not just once")
    }

    @Test
    fun currentColorLerpsFromStartColorToEndColorOverLife() {
        val world = World()
        world.add(
            world.create(),
            ParticleEmitter(
                mesh = fakeMesh(), material = fakeMaterial(), origin = Vec3(0f, 0f, 0f),
                maxParticles = 1, spawnRate = 1000f, lifetime = 1f, startAlpha = 1f, scale = 1f,
                motion = ParticleMotion(baseVelocity = Vec3(0f, 0f, 0f)),
                visual = ParticleVisual(startColor = Vec3(1f, 0f, 0f), endColor = Vec3(0f, 0f, 1f)),
            ),
        )
        val system = ParticleSystem()

        system.update(world, 0.001f) // spawn, age ~= 0
        val emitter = world.family<ParticleEmitter>().components().first()
        val particle = emitter.particles[0]
        assertTrue(abs(particle.currentColor(emitter).x - 1f) < 0.01f, "just spawned, color should be ~startColor")
        assertFalse(particle.currentColor(emitter).z > 0.01f, "just spawned, no endColor blend yet")

        system.update(world, 0.5f) // age ~= 0.5 of a 1s lifetime -- halfway blended
        val halfway = particle.currentColor(emitter)
        assertEquals(0.5f, halfway.x, 0.02f, "halfway through lifetime, red should be ~half")
        assertEquals(0.5f, halfway.z, 0.02f, "halfway through lifetime, blue should be ~half")
    }

    @Test
    fun dynamicSpawnRateOverridesTheStaticRateEveryFrame() {
        val world = World()
        var liveRate = 0f
        world.add(
            world.create(),
            ParticleEmitter(
                mesh = fakeMesh(), material = fakeMaterial(), origin = Vec3(0f, 0f, 0f),
                maxParticles = 100, spawnRate = 999f, lifetime = 10f, startAlpha = 1f, scale = 1f,
                motion = ParticleMotion(baseVelocity = Vec3(0f, 0f, 0f)),
                dynamics = ParticleDynamics(dynamicSpawnRate = { liveRate }),
            ),
        )
        val system = ParticleSystem()

        system.update(world, 1f) // liveRate == 0 -- static spawnRate must be ignored
        val emitter = world.family<ParticleEmitter>().components().first()
        assertEquals(0, emitter.particles.count { it.alive }, "dynamicSpawnRate must override spawnRate, not add to it")

        liveRate = 10f
        system.update(world, 1f)
        assertEquals(10, emitter.particles.count { it.alive }, "dynamicSpawnRate must be re-read every frame")
    }

    @Test
    fun groundHeightProviderTakesPriorityOverTheFlatGroundY() {
        val world = World()
        world.add(
            world.create(),
            ParticleEmitter(
                mesh = fakeMesh(), material = fakeMaterial(), origin = Vec3(0f, 5f, 0f),
                maxParticles = 1, spawnRate = 1000f, lifetime = 10f, startAlpha = 1f, scale = 1f,
                motion = ParticleMotion(baseVelocity = Vec3(0f, -10f, 0f)),
                // groundY would settle at 0 if groundHeightProvider below didn't win.
                ground = ParticleGround(groundY = 0f, groundHeightProvider = { _, _ -> 2f }),
            ),
        )
        val system = ParticleSystem()

        system.update(world, 0.01f)
        system.update(world, 1f)

        val particle = world.family<ParticleEmitter>().components().first().particles[0]
        assertEquals(2f, particle.position.y, 1e-4f, "groundHeightProvider must win over the flat groundY")
    }

    @Test
    fun turbulencePerturbsVelocityAwayFromTheUnperturbedPath() {
        val world = World()
        world.add(
            world.create(),
            ParticleEmitter(
                mesh = fakeMesh(), material = fakeMaterial(), origin = Vec3(1f, 1f, 1f),
                maxParticles = 1, spawnRate = 1000f, lifetime = 10f, startAlpha = 1f, scale = 1f,
                motion = ParticleMotion(baseVelocity = Vec3(0f, 1f, 0f), turbulence = 5f, turbulenceFrequency = 1f),
            ),
        )
        val system = ParticleSystem()

        system.update(world, 0.01f) // spawn, velocity == baseVelocity exactly
        val particle = world.family<ParticleEmitter>().components().first().particles[0]
        val vxBefore = particle.velocity.x
        val vzBefore = particle.velocity.z

        system.update(world, 0.1f) // turbulence accumulates into velocity every step
        assertTrue(
            abs(particle.velocity.x - vxBefore) > 1e-4f || abs(particle.velocity.z - vzBefore) > 1e-4f,
            "turbulence must perturb velocity off the pure-baseVelocity path",
        )
    }

    @Test
    fun onParticleDeathFiresExactlyOnceWithTheDeathPosition() {
        val world = World()
        var callCount = 0
        var deathPosition: Vec3? = null
        world.add(
            world.create(),
            ParticleEmitter(
                mesh = fakeMesh(), material = fakeMaterial(), origin = Vec3(2f, 0f, 0f),
                maxParticles = 1, spawnRate = 1000f, lifetime = 0.2f, startAlpha = 1f, scale = 1f,
                motion = ParticleMotion(baseVelocity = Vec3(0f, 0f, 0f)),
                lifecycle = ParticleLifecycle(
                    onParticleDeath = { _, position -> callCount += 1; deathPosition = Vec3(position.x, position.y, position.z) },
                ),
            ),
        )
        val system = ParticleSystem()

        system.update(world, 0.01f) // spawn at (2, 0, 0)
        system.update(world, 1f) // well past lifetime -- dies this step

        assertEquals(1, callCount, "onParticleDeath must fire exactly once per death, not per frame")
        assertEquals(Vec3(2f, 0f, 0f), deathPosition, "callback must see the particle's actual death position")
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
