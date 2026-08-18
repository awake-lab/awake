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
private const val MAX_PARTICLES = 200
private const val SPAWN_RATE = 40f
private const val LIFETIME_SECONDS = 2f
private const val START_ALPHA = 0.8f
private const val PARTICLE_SCALE = 0.3f
private const val RISE_SPEED = 1.5f
private const val VELOCITY_JITTER = 0.4f

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

/** [ParticleEmitter] isn't an authorable [io.github.ronjunevaldoz.awake.scene.runtime
 * .SceneComponent] yet -- the particles example's scene document authors an empty, named
 * placeholder node instead (`particles`, no components), same "author a named node, attach
 * the state a scene document can't express in `onActivated`" shape [InstancedCubesExampleDriver]
 * already uses. */
internal object ParticleEmitterExampleDriver {
    fun attach(instance: SceneInstance, runtime: SceneGameRuntime) {
        val node = instance.roots.find { it.name == "particles" } ?: return
        val mesh = runtime.requireMesh("particle-quad")
        val material = runtime.requireMaterial("particle")
        runtime.world.add(
            node.entity,
            ParticleEmitter(
                mesh = mesh,
                material = material,
                origin = Vec3(0f, 0f, 0f),
                maxParticles = MAX_PARTICLES,
                spawnRate = SPAWN_RATE,
                lifetime = LIFETIME_SECONDS,
                startAlpha = START_ALPHA,
                baseVelocity = Vec3(0f, RISE_SPEED, 0f),
                velocityJitter = VELOCITY_JITTER,
                scale = PARTICLE_SCALE,
            ),
        )
    }

    fun createMesh(runtime: SceneGameRuntime) = runtime.renderer.createMesh(particleQuadGeometry)

    /** A soft white dot, radial alpha falloff from center to edge -- no image asset needed for
     * a first-slice demo; a real particle texture (sprite sheet, glow) is a later swap, same
     * [TextureAsset] shape either way. */
    fun createMaterial(runtime: SceneGameRuntime) =
        runtime.renderer.createMaterial(texture = softDotTexture(), uniformFloatCount = PARTICLE_UNIFORM_FLOAT_COUNT)
}

/** particle.wgsl's Uniforms: viewProjection(16) + cameraRight(4) + cameraUp(4). */
private const val PARTICLE_UNIFORM_FLOAT_COUNT = 24

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
