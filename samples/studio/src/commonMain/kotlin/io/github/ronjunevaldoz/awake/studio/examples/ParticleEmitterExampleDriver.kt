// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("MagicNumber")

package io.github.ronjunevaldoz.awake.studio.examples

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.render.texture.TextureAsset
import io.github.ronjunevaldoz.awake.scene.rendering.components.ParticleDynamics
import io.github.ronjunevaldoz.awake.scene.rendering.components.ParticleEmitter
import io.github.ronjunevaldoz.awake.scene.rendering.components.ParticleGround
import io.github.ronjunevaldoz.awake.scene.rendering.components.ParticleLifecycle
import io.github.ronjunevaldoz.awake.scene.rendering.components.ParticleMotion
import io.github.ronjunevaldoz.awake.scene.rendering.components.ParticleVisual
import io.github.ronjunevaldoz.awake.scene.rendering.components.spawnParticleBurst
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.scene.runtime.SceneInstance
import kotlin.math.sin

private const val PARTICLE_TEXTURE_SIZE = 16
private const val FLICKER_FRAME_COUNT = 4
private const val MAX_PARTICLES_PER_EMITTER = 200

/** Unit billboard quad (-0.5..0.5 on x/y, z=0) -- `particle.wgsl`'s vertex shader offsets these
 * local x/y by the camera-right/camera-up basis, so this quad's own orientation never matters. */
private val quadVertices = floatArrayOf(
    -0.5f, -0.5f, 0f, 0f, 1f,
    0.5f, -0.5f, 0f, 1f, 1f,
    0.5f, 0.5f, 0f, 1f, 0f,
    -0.5f, 0.5f, 0f, 0f, 0f,
)
private val quadIndices = intArrayOf(0, 1, 2, 2, 3, 0)
private val particleQuadGeometry = MeshGeometry(quadVertices, quadIndices, format = VertexFormat.PositionUv)

/** The 4 simplest variants -- distinct config, no closures needed -- built from this one data
 * shape in a loop. The 3 capabilities that need a closure (context-driven rate, non-flat
 * terrain, sub-emitter chaining) are constructed by hand in [ParticleEmitterExampleDriver.attach]
 * instead, since a plain data class can't hold `mesh`/`material`/`world`-capturing lambdas
 * cleanly. */
private data class ParticleVariant(
    val nodeName: String,
    val materialName: String,
    val origin: Vec3,
    val baseVelocity: Vec3,
    val velocityJitter: Float,
    val coneHalfAngleDegrees: Float?,
    val groundY: Float?,
    val spawnRate: Float,
    val lifetime: Float,
    val startAlpha: Float,
    val scale: Float,
    val startColor: Vec3,
    val endColor: Vec3,
    val frameCount: Int = 1,
    val turbulence: Float = 0f,
    val turbulenceFrequency: Float = 1f,
    val spawnRadius: Float = 0f,
    val convergeToOrigin: Boolean = false,
)

private val PARTICLE_VARIANTS = listOf(
    // Cast & aura, a real "charging circle": particles spawn on a ring around origin and
    // converge inward (ParticleMotion.spawnRadius + convergeToOrigin), cooling from violet to
    // pink as they close in -- the classic channeling-spell VFX, not just a rising stream.
    ParticleVariant(
        nodeName = "particles-aura",
        materialName = "particle",
        origin = Vec3(-4.5f, 1f, 0f),
        baseVelocity = Vec3(0f, 1.5f, 0f), // magnitude only -- direction is overridden by convergeToOrigin
        velocityJitter = 0f,
        coneHalfAngleDegrees = null,
        groundY = null,
        spawnRate = 30f,
        lifetime = 1f,
        startAlpha = 0.8f,
        scale = 0.2f,
        startColor = Vec3(0.7f, 0.3f, 1f),
        endColor = Vec3(1f, 0.5f, 0.9f),
        spawnRadius = 1.2f,
        convergeToOrigin = true,
    ),
    // Impact/hit: a real 70-degree cone burst (not just jitter), orange fading to red, and a
    // flickering 4-frame sprite strip for an ember look -- its own material/texture (the strip
    // atlas), not the shared "particle" one, since sharing would distort every OTHER variant's
    // single-frame sampling (see this driver's own doc comment on why).
    ParticleVariant(
        nodeName = "particles-impact",
        materialName = "particle-flicker",
        origin = Vec3(-1.5f, 1f, 0f),
        baseVelocity = Vec3(0f, 2f, 0f),
        velocityJitter = 0f,
        coneHalfAngleDegrees = 70f,
        groundY = null,
        spawnRate = 60f,
        lifetime = 0.4f,
        startAlpha = 1f,
        scale = 0.2f,
        startColor = Vec3(1f, 0.6f, 0.1f),
        endColor = Vec3(1f, 0.1f, 0f),
        frameCount = FLICKER_FRAME_COUNT,
    ),
    // Environment: slow downward drift from height -- falling leaves/dust that settle on a
    // FLAT ground (groundY) instead of fading out mid-air. See particles-terrain below for the
    // non-flat (groundHeightProvider) version of the same idea.
    ParticleVariant(
        nodeName = "particles-environment",
        materialName = "particle",
        origin = Vec3(1.5f, 3f, 0f),
        baseVelocity = Vec3(0f, -0.4f, 0f),
        velocityJitter = 0.3f,
        coneHalfAngleDegrees = null,
        groundY = 0f,
        spawnRate = 10f,
        lifetime = 4f,
        startAlpha = 0.6f,
        scale = 0.2f,
        startColor = Vec3(0.4f, 0.8f, 0.3f),
        endColor = Vec3(0.4f, 0.8f, 0.3f),
    ),
    // Turbulence: a smoke/mist column -- weak upward drift, strong flow-field perturbation
    // (ParticleMotion.turbulence), so motion swirls instead of reading as pure random jitter.
    ParticleVariant(
        nodeName = "particles-turbulence",
        materialName = "particle",
        origin = Vec3(-7.5f, 0f, 0f),
        baseVelocity = Vec3(0f, 0.6f, 0f),
        velocityJitter = 0.05f,
        coneHalfAngleDegrees = null,
        groundY = null,
        spawnRate = 20f,
        lifetime = 4f,
        startAlpha = 0.5f,
        scale = 0.3f,
        startColor = Vec3(0.6f, 0.6f, 0.65f),
        endColor = Vec3(0.3f, 0.3f, 0.35f),
        turbulence = 3f,
        turbulenceFrequency = 0.5f,
    ),
)

/** Simulated "player speed" for the context-driven emitter below -- oscillates over time since
 * this demo has no real player to read from. [advance] updates it every frame; a real game
 * would read actual gameplay state here instead. */
private var simulatedSpeedElapsed = 0f
private var simulatedSpeed = 0f

/** [ParticleEmitter] isn't an authorable [io.github.ronjunevaldoz.awake.scene.runtime
 * .SceneComponent] yet -- the particles example's scene document authors 7 empty, named
 * placeholder nodes instead, same "author a named node, attach the state a scene document
 * can't express in `onActivated`" shape [InstancedCubesExampleDriver] already uses. Covers
 * every [ParticleEmitter] capability:
 * - `particles-aura`: a charging circle -- ring spawn + inward convergence
 *   ([ParticleMotion.spawnRadius]/`convergeToOrigin`).
 * - `particles-impact`/`particles-environment`/`particles-turbulence`: the other 3
 *   [ParticleVariant]s above (cone burst, sprite-strip flicker, flat ground-stop, turbulence).
 * - `particles-context`: [ParticleDynamics.dynamicSpawnRate] reads [simulatedSpeed] every frame
 *   -- dust that kicks up harder as the (simulated) player moves faster.
 * - `particles-terrain`: [ParticleGround.groundHeightProvider] settles on undulating terrain
 *   instead of one flat plane.
 * - `particles-projectile`: [ParticleLifecycle.onParticleDeath] spawns a small burst
 *   ([spawnParticleBurst]) wherever each of its own trail particles expires -- real sub-emitter
 *   chaining, not just a trailing stream.
 */
internal object ParticleEmitterExampleDriver {
    fun attach(instance: SceneInstance, runtime: SceneGameRuntime) {
        simulatedSpeedElapsed = 0f
        simulatedSpeed = 0f
        val mesh = runtime.requireMesh("particle-quad")
        val material = runtime.requireMaterial("particle")

        PARTICLE_VARIANTS.forEach { variant ->
            val node = instance.roots.find { it.name == variant.nodeName } ?: return@forEach
            val variantMaterial = runtime.requireMaterial(variant.materialName)
            runtime.world.add(
                node.entity,
                ParticleEmitter(
                    mesh = mesh,
                    material = variantMaterial,
                    origin = variant.origin,
                    maxParticles = MAX_PARTICLES_PER_EMITTER,
                    spawnRate = variant.spawnRate,
                    lifetime = variant.lifetime,
                    startAlpha = variant.startAlpha,
                    scale = variant.scale,
                    motion = ParticleMotion(
                        baseVelocity = variant.baseVelocity,
                        velocityJitter = variant.velocityJitter,
                        coneHalfAngleDegrees = variant.coneHalfAngleDegrees,
                        spawnRadius = variant.spawnRadius,
                        convergeToOrigin = variant.convergeToOrigin,
                        turbulence = variant.turbulence,
                        turbulenceFrequency = variant.turbulenceFrequency,
                    ),
                    visual = ParticleVisual(
                        startColor = variant.startColor,
                        endColor = variant.endColor,
                        frameCount = variant.frameCount,
                    ),
                    ground = ParticleGround(groundY = variant.groundY),
                ),
            )
        }

        instance.roots.find { it.name == "particles-context" }?.let { node ->
            runtime.world.add(
                node.entity,
                ParticleEmitter(
                    mesh = mesh,
                    material = material,
                    origin = Vec3(7.5f, 0f, 0f),
                    maxParticles = MAX_PARTICLES_PER_EMITTER,
                    spawnRate = 5f, // only used if dynamicSpawnRate is somehow unset; it isn't
                    lifetime = 1f,
                    startAlpha = 0.6f,
                    scale = 0.15f,
                    motion = ParticleMotion(baseVelocity = Vec3(0f, 0.3f, 0f), velocityJitter = 0.5f),
                    visual = ParticleVisual(startColor = Vec3(0.6f, 0.5f, 0.3f), endColor = Vec3(0.6f, 0.5f, 0.3f)),
                    dynamics = ParticleDynamics(dynamicSpawnRate = { 5f + simulatedSpeed * 8f }),
                ),
            )
        }

        instance.roots.find { it.name == "particles-terrain" }?.let { node ->
            runtime.world.add(
                node.entity,
                ParticleEmitter(
                    mesh = mesh,
                    material = material,
                    origin = Vec3(0f, 5f, -4f),
                    maxParticles = MAX_PARTICLES_PER_EMITTER,
                    spawnRate = 8f,
                    lifetime = 6f,
                    startAlpha = 0.7f,
                    scale = 0.22f,
                    motion = ParticleMotion(
                        // Drifts sideways while falling, to visibly cross the undulating ground.
                        baseVelocity = Vec3(0.3f, -1.5f, 0f),
                        velocityJitter = 0.2f,
                    ),
                    visual = ParticleVisual(startColor = Vec3(0.9f, 0.85f, 0.6f), endColor = Vec3(0.9f, 0.85f, 0.6f)),
                    ground = ParticleGround(groundHeightProvider = { x, _ -> sin(x * 0.5f) * 1f - 3f }),
                ),
            )
        }

        instance.roots.find { it.name == "particles-projectile" }?.let { node ->
            runtime.world.add(
                node.entity,
                ParticleEmitter(
                    mesh = mesh,
                    material = material,
                    origin = Vec3(4.5f, 1f, 0f),
                    maxParticles = MAX_PARTICLES_PER_EMITTER,
                    spawnRate = 80f,
                    lifetime = 0.6f,
                    startAlpha = 0.9f,
                    scale = 0.18f,
                    motion = ParticleMotion(baseVelocity = Vec3(-3f, 0f, 0f), velocityJitter = 0.05f),
                    visual = ParticleVisual(startColor = Vec3(1f, 0.2f, 0.1f), endColor = Vec3(1f, 0.2f, 0.1f)),
                    lifecycle = ParticleLifecycle(
                        onParticleDeath = { world, position ->
                            spawnParticleBurst(
                                world, mesh, material, position,
                                count = 6, spawnRate = 1000f, lifetime = 0.2f, startAlpha = 0.8f,
                                baseVelocity = Vec3(0f, 0.5f, 0f), velocityJitter = 1.5f, scale = 0.1f,
                                startColor = Vec3(1f, 0.6f, 0.2f), endColor = Vec3(1f, 0.1f, 0f),
                            )
                        },
                    ),
                ),
            )
        }
    }

    /** Advances [simulatedSpeed] -- the fake "player speed" `particles-context` reacts to. A
     * real game would read actual gameplay state instead of oscillating a sine wave. */
    fun advance(delta: Float) {
        simulatedSpeedElapsed += delta
        simulatedSpeed = 3f + 3f * sin(simulatedSpeedElapsed)
    }

    fun createMesh(runtime: SceneGameRuntime) = runtime.renderer.createMesh(particleQuadGeometry)

    /** A soft white dot, radial alpha falloff from center to edge -- no image asset needed for
     * a first-slice demo; a real particle texture (sprite sheet, glow) is a later swap, same
     * [TextureAsset] shape either way. Tinted per-particle in the shader, not here -- this stays
     * one shared white texture for every non-flickering variant. */
    fun createMaterial(runtime: SceneGameRuntime) =
        runtime.renderer.createMaterial(texture = softDotTexture(), uniformFloatCount = PARTICLE_UNIFORM_FLOAT_COUNT)

    /** [FLICKER_FRAME_COUNT]-frame horizontal sprite strip, each frame a soft dot at a
     * different size/brightness -- proves `particle.wgsl`'s frame-atlas UV math cycles frames,
     * not just a static image. Its own material (not the shared "particle" one) since
     * `ParticleVisual.frameCount` is per-emitter but the TEXTURE it slices is shared by
     * whatever material the emitter uses -- a `frameCount = 1` emitter sharing this multi-frame
     * texture would sample the whole strip as one squished frame. */
    fun createFlickerMaterial(runtime: SceneGameRuntime) =
        runtime.renderer.createMaterial(texture = flickerStripTexture(), uniformFloatCount = PARTICLE_UNIFORM_FLOAT_COUNT)
}

/** particle.wgsl's Uniforms: viewProjection(16) + cameraRight(4) + cameraUp(4) + frameInfo(4). */
private const val PARTICLE_UNIFORM_FLOAT_COUNT = 28

private fun softDotTexture(): TextureAsset = dotTexture(PARTICLE_TEXTURE_SIZE, brightness = 1f, sizeFactor = 1f)

private fun flickerStripTexture(): TextureAsset {
    val frameSize = PARTICLE_TEXTURE_SIZE
    val stripWidth = frameSize * FLICKER_FRAME_COUNT
    val data = ByteArray(stripWidth * frameSize * 4)
    for (frame in 0 until FLICKER_FRAME_COUNT) {
        // Alternates brightness/size per frame for a simple ember flicker -- not a real
        // hand-authored sprite sheet, just enough variation to prove frame-cycling works.
        val brightness = 0.6f + 0.4f * (frame % 2)
        val sizeFactor = 0.7f + 0.3f * ((frame + 1) % 2)
        val frameTexture = dotTexture(frameSize, brightness, sizeFactor)
        for (y in 0 until frameSize) {
            for (x in 0 until frameSize) {
                val srcIndex = (y * frameSize + x) * 4
                val dstIndex = (y * stripWidth + frame * frameSize + x) * 4
                frameTexture.data.copyInto(data, dstIndex, srcIndex, srcIndex + 4)
            }
        }
    }
    return TextureAsset(data, stripWidth, frameSize)
}

private fun dotTexture(size: Int, brightness: Float, sizeFactor: Float): TextureAsset {
    val center = (size - 1) / 2f
    val radius = (size / 2f) * sizeFactor
    val data = ByteArray(size * size * 4)
    for (y in 0 until size) {
        for (x in 0 until size) {
            val dx = x - center
            val dy = y - center
            val distance = kotlin.math.sqrt(dx * dx + dy * dy)
            val alpha = ((1f - (distance / radius)).coerceIn(0f, 1f) * 255f * brightness).toInt().coerceIn(0, 255)
            val index = (y * size + x) * 4
            data[index] = 255.toByte()
            data[index + 1] = 255.toByte()
            data[index + 2] = 255.toByte()
            data[index + 3] = alpha.toByte()
        }
    }
    return TextureAsset(data, size, size)
}
