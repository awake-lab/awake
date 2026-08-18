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
private const val FLICKER_FRAME_COUNT = 4

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
 * visually distinct MMORPG-style configs:
 * - [Aura]: a coherent slow upward stream, violet->pink gradient, for cast/channel VFX.
 * - [Impact]: a real directional cone burst ([ParticleEmitter.coneHalfAngleDegrees]) with an
 *   orange->red gradient and a flickering 4-frame sprite strip ([ParticleEmitter.frameCount]).
 * - [Environment]: slow downward drift from height that settles at [ParticleEmitter.groundY]
 *   instead of fading mid-air, for falling leaves/dust.
 * - [Projectile]: fast, near-zero-jitter directed motion, for a fireball/arrow trail.
 */
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
)

private val PARTICLE_VARIANTS = listOf(
    // Cast & aura: a slow, coherent rising stream that cools from violet to pink as it ages.
    ParticleVariant(
        nodeName = "particles-aura",
        materialName = "particle",
        origin = Vec3(-4.5f, 0f, 0f),
        baseVelocity = Vec3(0f, 1.2f, 0f),
        velocityJitter = 0.15f,
        coneHalfAngleDegrees = null,
        groundY = null,
        spawnRate = 25f,
        lifetime = 2.5f,
        startAlpha = 0.7f,
        scale = 0.25f,
        startColor = Vec3(0.7f, 0.3f, 1f),
        endColor = Vec3(1f, 0.5f, 0.9f),
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
    // Environment: slow downward drift from height -- falling leaves/dust that settle on the
    // ground (groundY) instead of fading out mid-air.
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
    // Projectile: fast, nearly-jitter-free directed motion -- a fireball/arrow trail.
    ParticleVariant(
        nodeName = "particles-projectile",
        materialName = "particle",
        origin = Vec3(4.5f, 1f, 0f),
        baseVelocity = Vec3(-3f, 0f, 0f),
        velocityJitter = 0.05f,
        coneHalfAngleDegrees = null,
        groundY = null,
        spawnRate = 80f,
        lifetime = 0.6f,
        startAlpha = 0.9f,
        scale = 0.18f,
        startColor = Vec3(1f, 0.2f, 0.1f),
        endColor = Vec3(1f, 0.2f, 0.1f),
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
        PARTICLE_VARIANTS.forEach { variant ->
            val node = instance.roots.find { it.name == variant.nodeName } ?: return@forEach
            val mesh = runtime.requireMesh("particle-quad")
            val material = runtime.requireMaterial(variant.materialName)
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
                    startColor = variant.startColor,
                    endColor = variant.endColor,
                    coneHalfAngleDegrees = variant.coneHalfAngleDegrees,
                    groundY = variant.groundY,
                    frameCount = variant.frameCount,
                ),
            )
        }
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
     * `ParticleEmitter.frameCount` is per-emitter but the TEXTURE it slices is shared by
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
