// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("MagicNumber")

package io.github.ronjunevaldoz.awake.studio.examples

import io.github.ronjunevaldoz.awake.core.graphics.createBitmap
import io.github.ronjunevaldoz.awake.core.graphics.toRgba8Bytes
import io.github.ronjunevaldoz.awake.core.math.Aabb
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.utils.readResourceBytes
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
import io.github.ronjunevaldoz.awake.scene.runtime.Scene
import io.github.ronjunevaldoz.awake.scene.runtime.SceneAppLifecycleRuntime
import kotlin.math.sin

private const val FLICKER_FRAME_COUNT = 4
private const val SPARKLE_FRAME_COUNT = 4
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
private val particleQuadGeometry =
    MeshGeometry(quadVertices, quadIndices, format = VertexFormat.PositionUv)

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
    val stretchWithVelocity: Boolean = false,
    val stretchFactor: Float = 0.05f,
)

private val PARTICLE_VARIANTS = listOf(
    // Cast & aura, a real "charging circle": particles spawn on a ring around origin and
    // converge inward (ParticleMotion.spawnRadius + convergeToOrigin), cooling from violet to
    // pink as they close in -- the classic channeling-spell VFX, not just a rising stream.
    ParticleVariant(
        nodeName = "particles-aura",
        materialName = "particle",
        origin = Vec3(-4.5f, 1f, 0f),
        baseVelocity = Vec3(
            0f,
            1.5f,
            0f
        ), // magnitude only -- direction is overridden by convergeToOrigin
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
    // Level-up: a vertical light pillar -- ring spawn, straight-up velocity (no convergence,
    // unlike aura/spawn-in below), gold cooling into near-white as it rises, like light
    // intensifying toward the top. A real level-up would use ParticleLifecycle.burstCount for
    // one-shot-then-gone; kept continuous here so it stays visible in this side-by-side gallery.
    // Its own textured sparkle-strip material (not the shared soft-dot "particle" one) --
    // 4-point stars, twinkling via per-particle desynced frames (frameCount + Particle
    // .frameOffset), so every sparkle in the pillar twinkles on its own beat instead of
    // flickering in lockstep.
    ParticleVariant(
        nodeName = "particles-levelup",
        materialName = "particle-levelup",
        origin = Vec3(10.5f, 0f, 0f),
        baseVelocity = Vec3(0f, 3f, 0f),
        velocityJitter = 0f,
        coneHalfAngleDegrees = null,
        groundY = null,
        spawnRate = 40f,
        lifetime = 1.2f,
        startAlpha = 0.9f,
        scale = 0.3f,
        startColor = Vec3(1f, 0.85f, 0.3f),
        endColor = Vec3(1f, 1f, 0.9f),
        spawnRadius = 0.8f,
        frameCount = SPARKLE_FRAME_COUNT,
    ),
    // Spawn/teleport-in: the inverse of aura's charging circle -- a wider ring, faster
    // convergence, and a cool blue->cyan-white gradient (energy building toward a flash) instead
    // of aura's slow violet->pink channel. Same convergeToOrigin mechanism, different tuning and
    // color reads as a distinct effect entirely.
    ParticleVariant(
        nodeName = "particles-spawn",
        materialName = "particle",
        origin = Vec3(-10.5f, 1f, 0f),
        baseVelocity = Vec3(0f, 3f, 0f),
        velocityJitter = 0f,
        coneHalfAngleDegrees = null,
        groundY = null,
        spawnRate = 35f,
        lifetime = 0.6f,
        startAlpha = 0.85f,
        scale = 0.18f,
        startColor = Vec3(0.2f, 0.4f, 1f),
        endColor = Vec3(0.6f, 0.9f, 1f),
        spawnRadius = 1.5f,
        convergeToOrigin = true,
    ),
    // Streak: rain/spark-shower read -- fast straight-down fall, ParticleVisual
    // .stretchWithVelocity elongates each quad along its own on-screen fall direction instead of
    // the plain camera-facing square every other variant draws, so each particle reads as a
    // short streak rather than a dot.
    ParticleVariant(
        nodeName = "particles-streak",
        materialName = "particle",
        origin = Vec3(17.5f, 4f, 0f),
        baseVelocity = Vec3(0f, -9f, 0f),
        velocityJitter = 0.5f,
        coneHalfAngleDegrees = null,
        groundY = 0f,
        spawnRate = 30f,
        lifetime = 1f,
        startAlpha = 0.8f,
        scale = 0.08f,
        startColor = Vec3(0.6f, 0.8f, 1f),
        endColor = Vec3(0.6f, 0.8f, 1f),
        stretchWithVelocity = true,
        stretchFactor = 0.08f,
    ),
)

/** Simulated "player speed" for the context-driven emitter below -- oscillates over time since
 * this demo has no real player to read from. [advance] updates it every frame; a real game
 * would read actual gameplay state here instead. */
private var simulatedSpeedElapsed = 0f
private var simulatedSpeed = 0f

/** [ParticleEmitter] isn't an authorable [io.github.ronjunevaldoz.awake.scene.runtime
 * .SceneComponent] yet -- the particles example's scene document authors 9 empty, named
 * placeholder nodes instead, same "author a named node, attach the state a scene document
 * can't express in `onActivated`" shape [InstancedCubesExampleDriver] already uses. Covers
 * every [ParticleEmitter] capability:
 * - `particles-aura`: a charging circle -- ring spawn + inward convergence
 *   ([ParticleMotion.spawnRadius]/`convergeToOrigin`).
 * - `particles-levelup`/`particles-spawn`: the same ring-spawn shape reused two more ways --
 *   straight-up (no convergence) for a light-pillar level-up, faster/wider/converging for a
 *   teleport-in flash -- same mechanism, different tuning/color reads as distinct effects.
 * - `particles-impact`/`particles-environment`/`particles-turbulence`: the other 3
 *   [ParticleVariant]s above (cone burst, sprite-strip flicker, flat ground-stop, turbulence).
 * - `particles-context`: [ParticleDynamics.dynamicSpawnRate] reads [simulatedSpeed] every frame
 *   -- dust that kicks up harder as the (simulated) player moves faster.
 * - `particles-terrain`: [ParticleGround.groundHeightProvider] settles on undulating terrain
 *   instead of one flat plane.
 * - `particles-projectile`: [ParticleLifecycle.onParticleDeath] spawns a small burst
 *   ([spawnParticleBurst]) wherever each of its own trail particles expires -- a one-shot death
 *   chain, not [ParticleEmitter.children] itself.
 * - `particles-torch`: [ParticleEmitter.children] -- a real sub-emitter tree, a flame with an
 *   ember-spark child riding at a fixed local offset above the flame's own origin.
 * - `particles-collider`: [ParticleGround.colliders] -- falls onto the authored
 *   `particles-collider-platform` box (a real [io.github.ronjunevaldoz.awake.core.math.Aabb]
 *   collision, not a flat groundY plane).
 * - `particles-streak`: [ParticleVisual.stretchWithVelocity] -- each quad elongates along its
 *   own on-screen fall direction instead of staying a plain camera-facing square.
 */
internal object ParticleEmitterExampleDriver {
    private var dotTexture: TextureAsset? = null
    private var flickerTexture: TextureAsset? = null
    private var sparkleTexture: TextureAsset? = null

    /** Decodes the 3 real particle textures once at load time (same "parse ahead, `createMaterial`
     * just wraps the result" shape [GltfViewerAssets.preload] already established) -- CC0
     * (public domain) art from Kenney's Particle Pack (kenney.nl/assets/particle-pack), see
     * `assets/textures/particles-LICENSE.txt`. [flickerTexture]/[sparkleTexture] are pre-composited
     * 4-frame horizontal strips (not decoded from 4 separate files here) -- the strip layout
     * `particle.wgsl`'s frame-atlas UV math expects is baked into the PNG itself. */
    suspend fun preload() {
        if (dotTexture != null) return
        dotTexture = loadTexture("assets/textures/particle-dot.png")
        flickerTexture = loadTexture("assets/textures/particle-flicker-strip.png")
        sparkleTexture = loadTexture("assets/textures/particle-sparkle-strip.png")
    }

    private suspend fun loadTexture(path: String): TextureAsset {
        val bitmap = createBitmap(readResourceBytes(path))
        return TextureAsset(bitmap.toRgba8Bytes(), bitmap.width, bitmap.height)
    }

    fun attach(instance: Scene, runtime: SceneAppLifecycleRuntime) {
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
                        stretchWithVelocity = variant.stretchWithVelocity,
                        stretchFactor = variant.stretchFactor,
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
                    motion = ParticleMotion(
                        baseVelocity = Vec3(0f, 0.3f, 0f),
                        velocityJitter = 0.5f
                    ),
                    visual = ParticleVisual(
                        startColor = Vec3(0.6f, 0.5f, 0.3f),
                        endColor = Vec3(0.6f, 0.5f, 0.3f)
                    ),
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
                    visual = ParticleVisual(
                        startColor = Vec3(0.9f, 0.85f, 0.6f),
                        endColor = Vec3(0.9f, 0.85f, 0.6f)
                    ),
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
                    motion = ParticleMotion(
                        baseVelocity = Vec3(-3f, 0f, 0f),
                        velocityJitter = 0.05f
                    ),
                    visual = ParticleVisual(
                        startColor = Vec3(1f, 0.2f, 0.1f),
                        endColor = Vec3(1f, 0.2f, 0.1f)
                    ),
                    lifecycle = ParticleLifecycle(
                        onParticleDeath = { world, position ->
                            spawnParticleBurst(
                                world,
                                mesh,
                                material,
                                position,
                                count = 6,
                                spawnRate = 1000f,
                                lifetime = 0.2f,
                                startAlpha = 0.8f,
                                baseVelocity = Vec3(0f, 0.5f, 0f),
                                velocityJitter = 1.5f,
                                scale = 0.1f,
                                startColor = Vec3(1f, 0.6f, 0.2f),
                                endColor = Vec3(1f, 0.1f, 0f),
                            )
                        },
                    ),
                ),
            )
        }
        instance.roots.find { it.name == "particles-torch" }?.let { node ->
            // Real sub-emitter tree (ParticleEmitter.children): a warm flame column, with a
            // bright ember-spark child riding at a fixed offset above the flame's own origin --
            // one emitter tree, not two separately-tracked entities.
            val emberChild = ParticleEmitter(
                mesh = mesh,
                material = material,
                origin = Vec3(0f, 1.1f, 0f), // local offset above the parent's own origin
                maxParticles = MAX_PARTICLES_PER_EMITTER,
                spawnRate = 15f,
                lifetime = 0.5f,
                startAlpha = 0.9f,
                scale = 0.08f,
                motion = ParticleMotion(baseVelocity = Vec3(0f, 1.5f, 0f), velocityJitter = 1.2f),
                visual = ParticleVisual(
                    startColor = Vec3(1f, 0.9f, 0.5f),
                    endColor = Vec3(1f, 0.4f, 0.1f)
                ),
            )
            runtime.world.add(
                node.entity,
                ParticleEmitter(
                    mesh = mesh,
                    material = material,
                    origin = Vec3(14.5f, 0f, -4f),
                    maxParticles = MAX_PARTICLES_PER_EMITTER,
                    spawnRate = 25f,
                    lifetime = 1f,
                    startAlpha = 0.8f,
                    scale = 0.2f,
                    motion = ParticleMotion(
                        baseVelocity = Vec3(0f, 1.2f, 0f),
                        velocityJitter = 0.15f
                    ),
                    visual = ParticleVisual(
                        startColor = Vec3(1f, 0.5f, 0.1f),
                        endColor = Vec3(0.6f, 0.1f, 0.1f)
                    ),
                    children = listOf(emberChild),
                ),
            )
        }

        instance.roots.find { it.name == "particles-collider" }?.let { node ->
            // Physical ground collision (ParticleGround.colliders): falls straight onto the
            // authored "particles-collider-platform" box instead of a flat groundY plane --
            // the Aabb below matches that node's own authored transform (position (14.5,1,0),
            // scale (2,0.3,2) against a -0.5..0.5 unit cube).
            val platformTop = Aabb(Vec3(13.5f, 0.85f, -1f), Vec3(15.5f, 1.15f, 1f))
            runtime.world.add(
                node.entity,
                ParticleEmitter(
                    mesh = mesh,
                    material = material,
                    origin = Vec3(14.5f, 5f, 0f),
                    maxParticles = MAX_PARTICLES_PER_EMITTER,
                    spawnRate = 12f,
                    lifetime = 3f,
                    startAlpha = 0.8f,
                    scale = 0.18f,
                    motion = ParticleMotion(
                        baseVelocity = Vec3(0f, -2f, 0f),
                        velocityJitter = 0.4f
                    ),
                    visual = ParticleVisual(
                        startColor = Vec3(0.3f, 0.7f, 1f),
                        endColor = Vec3(0.3f, 0.7f, 1f)
                    ),
                    ground = ParticleGround(colliders = listOf(platformTop)),
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

    fun createMesh(runtime: SceneAppLifecycleRuntime) =
        runtime.renderer.createMesh(particleQuadGeometry)

    /** A real CC0 glow texture (see [preload]'s own doc comment) -- tinted per-particle in the
     * shader, not here, so this stays one shared texture for every non-flickering variant. */
    fun createMaterial(runtime: SceneAppLifecycleRuntime) =
        runtime.renderer.createMaterial(
            texture = requireNotNull(dotTexture),
            uniformFloatCount = PARTICLE_UNIFORM_FLOAT_COUNT,
        )

    /** [FLICKER_FRAME_COUNT]-frame horizontal sprite strip (real CC0 spark art, see [preload])
     * -- proves `particle.wgsl`'s frame-atlas UV math cycles frames, not just a static image.
     * Its own material (not the shared "particle" one) since `ParticleVisual.frameCount` is
     * per-emitter but the TEXTURE it slices is shared by whatever material the emitter uses --
     * a `frameCount = 1` emitter sharing this multi-frame texture would sample the whole strip
     * as one squished frame. */
    fun createFlickerMaterial(runtime: SceneAppLifecycleRuntime) =
        runtime.renderer.createMaterial(
            texture = requireNotNull(flickerTexture),
            uniformFloatCount = PARTICLE_UNIFORM_FLOAT_COUNT,
        )

    /** [SPARKLE_FRAME_COUNT]-frame horizontal strip of stars (real CC0 art, see [preload]) --
     * proves a real per-emitter TEXTURE (not just tint) reads through `particle.wgsl`, and each
     * frame's own size/brightness variance is what [Particle.frameOffset]'s desync makes visible
     * as independent twinkling instead of a uniform strobe. */
    fun createLevelupMaterial(runtime: SceneAppLifecycleRuntime) =
        runtime.renderer.createMaterial(
            texture = requireNotNull(sparkleTexture),
            uniformFloatCount = PARTICLE_UNIFORM_FLOAT_COUNT,
        )
}

/** particle.wgsl's Uniforms: viewProjection(16) + cameraRight(4) + cameraUp(4) + frameInfo(4). */
private const val PARTICLE_UNIFORM_FLOAT_COUNT = 28
