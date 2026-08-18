// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("MagicNumber")

package io.github.ronjunevaldoz.awake.studio.examples

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.render.texture.TextureAsset
import io.github.ronjunevaldoz.awake.scene.rendering.components.ParticleEmitter
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.scene.runtime.SceneInstance

private const val PARTICLE_TEXTURE_SIZE = 16

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

/** One entity per variant (a `ParticleEmitter` is one-per-entity, like any other ECS
 * component) -- exercises the same generic [ParticleEmitter]/[ParticleSystem] against 4
 * visually distinct MMORPG-style configs, to see where the "first slice" (linear fade, no
 * sub-emitters, no directed cone spread beyond per-axis jitter) actually shows its limits:
 * - [Aura]: a coherent slow upward stream (low jitter), for cast/channel VFX.
 * - [Impact]: zero net velocity + large jitter reads as a radial burst, short lifetime.
 * - [Environment]: slow downward drift from height, for falling leaves/dust.
 * - [Projectile]: fast, near-zero-jitter directed motion, for a fireball/arrow trail.
 * All 4 share one `"particle"` mesh/material (see [createMesh]/[createMaterial]) -- only
 * [ParticleEmitter.tintColor] and the spawn/velocity numbers differ; the shared material's
 * uniform buffer gets its own frame/draw slot per emitter (same multi-draw-per-material
 * pattern `RenderSystem`'s other shared materials already use), so sharing is safe. */
private data class ParticleVariant(
    val nodeName: String,
    val origin: Vec3,
    val baseVelocity: Vec3,
    val velocityJitter: Float,
    val spawnRate: Float,
    val lifetime: Float,
    val startAlpha: Float,
    val scale: Float,
    val tintColor: Vec3,
)

private val PARTICLE_VARIANTS = listOf(
    // Cast & aura: a slow, coherent rising stream -- reads as a channeling glow.
    ParticleVariant(
        nodeName = "particles-aura",
        origin = Vec3(-4.5f, 0f, 0f),
        baseVelocity = Vec3(0f, 1.2f, 0f),
        velocityJitter = 0.15f,
        spawnRate = 25f,
        lifetime = 2.5f,
        startAlpha = 0.7f,
        scale = 0.25f,
        tintColor = Vec3(0.7f, 0.3f, 1f),
    ),
    // Impact/hit: zero net velocity + large jitter reads as an outward radial burst; short,
    // fast-fading lifetime matches a real hit-spark's snap.
    ParticleVariant(
        nodeName = "particles-impact",
        origin = Vec3(-1.5f, 1f, 0f),
        baseVelocity = Vec3(0f, 0f, 0f),
        velocityJitter = 2.5f,
        spawnRate = 60f,
        lifetime = 0.4f,
        startAlpha = 1f,
        scale = 0.2f,
        tintColor = Vec3(1f, 0.5f, 0.1f),
    ),
    // Environment: slow downward drift from height -- falling leaves/dust motes.
    ParticleVariant(
        nodeName = "particles-environment",
        origin = Vec3(1.5f, 3f, 0f),
        baseVelocity = Vec3(0f, -0.4f, 0f),
        velocityJitter = 0.3f,
        spawnRate = 10f,
        lifetime = 4f,
        startAlpha = 0.6f,
        scale = 0.2f,
        tintColor = Vec3(0.4f, 0.8f, 0.3f),
    ),
    // Projectile: fast, nearly-jitter-free directed motion -- a fireball/arrow trail.
    ParticleVariant(
        nodeName = "particles-projectile",
        origin = Vec3(4.5f, 1f, 0f),
        baseVelocity = Vec3(-3f, 0f, 0f),
        velocityJitter = 0.05f,
        spawnRate = 80f,
        lifetime = 0.6f,
        startAlpha = 0.9f,
        scale = 0.18f,
        tintColor = Vec3(1f, 0.2f, 0.1f),
    ),
)

private const val MAX_PARTICLES_PER_EMITTER = 200

/** [ParticleEmitter] isn't an authorable [io.github.ronjunevaldoz.awake.scene.runtime
 * .SceneComponent] yet -- the particles example's scene document authors 4 empty, named
 * placeholder nodes (one per [ParticleVariant]) instead, same "author a named node, attach
 * the state a scene document can't express in `onActivated`" shape [InstancedCubesExampleDriver]
 * already uses. */
internal object ParticleEmitterExampleDriver {
    fun attach(instance: SceneInstance, runtime: SceneGameRuntime) {
        val mesh = runtime.requireMesh("particle-quad")
        val material = runtime.requireMaterial("particle")
        PARTICLE_VARIANTS.forEach { variant ->
            val node = instance.roots.find { it.name == variant.nodeName } ?: return@forEach
            runtime.world.add(
                node.entity,
                ParticleEmitter(
                    mesh = mesh,
                    material = material,
                    origin = variant.origin,
                    maxParticles = MAX_PARTICLES_PER_EMITTER,
                    spawnRate = variant.spawnRate,
                    lifetime = variant.lifetime,
                    startAlpha = variant.startAlpha,
                    baseVelocity = variant.baseVelocity,
                    velocityJitter = variant.velocityJitter,
                    scale = variant.scale,
                    tintColor = variant.tintColor,
                ),
            )
        }
    }

    fun createMesh(runtime: SceneGameRuntime) = runtime.renderer.createMesh(particleQuadGeometry)

    /** A soft white dot, radial alpha falloff from center to edge -- no image asset needed for
     * a first-slice demo; a real particle texture (sprite sheet, glow) is a later swap, same
     * [TextureAsset] shape either way. Tinted per-emitter in the shader, not here -- this stays
     * one shared white texture for every variant. */
    fun createMaterial(runtime: SceneGameRuntime) =
        runtime.renderer.createMaterial(texture = softDotTexture(), uniformFloatCount = PARTICLE_UNIFORM_FLOAT_COUNT)
}

/** particle.wgsl's Uniforms: viewProjection(16) + cameraRight(4) + cameraUp(4) + tintColor(4). */
private const val PARTICLE_UNIFORM_FLOAT_COUNT = 28

private fun softDotTexture(): TextureAsset {
    val size = PARTICLE_TEXTURE_SIZE
    val center = (size - 1) / 2f
    val radius = size / 2f
    val data = ByteArray(size * size * 4)
    for (y in 0 until size) {
        for (x in 0 until size) {
            val dx = x - center
            val dy = y - center
            val distance = kotlin.math.sqrt(dx * dx + dy * dy)
            val alpha = ((1f - (distance / radius)).coerceIn(0f, 1f) * 255f).toInt()
            val index = (y * size + x) * 4
            data[index] = 255.toByte()
            data[index + 1] = 255.toByte()
            data[index + 2] = 255.toByte()
            data[index + 3] = alpha.toByte()
        }
    }
    return TextureAsset(data, size, size)
}
