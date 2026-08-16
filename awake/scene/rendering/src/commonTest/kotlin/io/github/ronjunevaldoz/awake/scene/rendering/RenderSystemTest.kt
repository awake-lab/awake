// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.rendering

import io.github.ronjunevaldoz.awake.core.math.Aabb
import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.ClipSpace
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.core.components.Transform
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.render.renderer.DEFAULT_SCENE_LIGHT
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.render.renderer.SceneLight
import io.github.ronjunevaldoz.awake.render.texture.PbrTextureSet
import io.github.ronjunevaldoz.awake.render.texture.RenderTarget
import io.github.ronjunevaldoz.awake.render.texture.TextureAsset
import io.github.ronjunevaldoz.awake.scene.rendering.components.InstancedMeshRenderer
import io.github.ronjunevaldoz.awake.scene.rendering.components.Light
import io.github.ronjunevaldoz.awake.scene.rendering.components.LodGroup
import io.github.ronjunevaldoz.awake.scene.rendering.components.LodLevel
import io.github.ronjunevaldoz.awake.scene.rendering.components.MeshBounds
import io.github.ronjunevaldoz.awake.scene.rendering.components.MeshRenderer
import io.github.ronjunevaldoz.awake.scene.rendering.systems.RenderSystem
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

/** [io.github.ronjunevaldoz.awake.scene.rendering.systems.RenderSystem] doesn't shade anything itself -- it just resolves the scene's [io.github.ronjunevaldoz.awake.scene.rendering.components.Light] entity
 * (or [io.github.ronjunevaldoz.awake.render.renderer.DEFAULT_SCENE_LIGHT] when there isn't one) into the backend-neutral [io.github.ronjunevaldoz.awake.render.renderer.SceneLight]
 * [io.github.ronjunevaldoz.awake.render.renderer.Renderer.draw] expects, same "world state in, render-api call out" shape the mesh/camera
 * side already has. A recording fake [io.github.ronjunevaldoz.awake.render.renderer.Renderer] (matching [Scene3DPlaygroundUiTest]'s own
 * `RecordingScene3DRenderer` shape) captures what actually reached [io.github.ronjunevaldoz.awake.render.renderer.Renderer.draw] without
 * needing a real GPU backend. */
class RenderSystemTest {
    private class RecordingRenderer : Renderer {
        var lastLight: SceneLight? = null
        var lastDrawCalls: List<DrawCall> = emptyList()
        override val clipSpace: ClipSpace = ClipSpace.WebGpu
        override var clearColor: FloatArray = floatArrayOf(0f, 0f, 0f, 1f)
        override var wireframe: Boolean = false
        override var shadowsEnabled: Boolean = true
        override fun createMesh(geometry: MeshGeometry): Mesh = error("not needed for this test")
        override fun createMaterial(
            texture: TextureAsset?,
            renderTarget: RenderTarget?,
            uniformFloatCount: Int,
        pbrTextures: PbrTextureSet?,
        ): Material =
            error("not needed for this test")

        override fun createRenderTarget(width: Int, height: Int): RenderTarget =
            error("not needed for this test")

        override fun draw(camera: Camera, drawCalls: List<DrawCall>, light: SceneLight) {
            lastLight = light
            lastDrawCalls = drawCalls
        }

        override fun renderToTexture(
            target: RenderTarget,
            camera: Camera,
            drawCalls: List<DrawCall>,
        ) = Unit

        override suspend fun readPixels(target: RenderTarget): TextureAsset =
            error("not needed for this test")

        override fun drawUi(primitives: List<UiDrawPrimitive>, font: UiFont?) = Unit
        override fun drawDebugLines(lines: List<LineSegment>) = Unit
        override fun destroy() = Unit
    }

    private fun worldWithPrimaryCamera(): World {
        val world = World()
        val cameraEntity = world.create()
        world.add(
            cameraEntity,
            io.github.ronjunevaldoz.awake.scene.rendering.components.Camera(
                Camera(
                    eye = Vec3(
                        0f,
                        0f,
                        5f,
                    ),
                    center = Vec3(0f, 0f, 0f),
                    fovYRadians = 1f,
                    near = 0.1f,
                    far = 100f,
                ),
            ),
        )
        return world
    }

    @Test
    fun usesDefaultSceneLightWhenNoLightEntityExists() {
        val world = worldWithPrimaryCamera()
        val renderer = RecordingRenderer()

        RenderSystem(renderer).update(world, 1f / 60f)

        assertEquals(DEFAULT_SCENE_LIGHT, renderer.lastLight)
    }

    @Test
    fun convertsTheSceneLightEntityIntoDirectionAndIntensityMultipliedColor() {
        val world = worldWithPrimaryCamera()
        val lightEntity = world.create()
        world.add(
            lightEntity,
            Light(color = Vec3(1f, 0.5f, 0.25f), intensity = 2f, direction = Vec3(1f, 0f, 0f)),
        )
        val renderer = RecordingRenderer()

        RenderSystem(renderer).update(world, 1f / 60f)

        val light = renderer.lastLight
        assertEquals(Vec3(1f, 0f, 0f), light?.direction)
        assertEquals(Vec3(2f, 1f, 0.5f), light?.color)
    }

    @Test
    fun neverCallsDrawWhenThereIsNoPrimaryCamera() {
        val world = World()
        val renderer = RecordingRenderer()

        RenderSystem(renderer).update(world, 1f / 60f)

        assertNull(renderer.lastLight)
    }

    @Test
    fun instancedMeshRendererProducesOneDrawCallCarryingEveryTransform() {
        val world = worldWithPrimaryCamera()
        val mesh = fakeMesh()
        val material = fakeMaterial()
        val transforms = listOf(Mat4(), Mat4().apply { m03 = 5f })
        val entity = world.create()
        world.add(entity, InstancedMeshRenderer(mesh, material, transforms))
        val renderer = RecordingRenderer()

        RenderSystem(renderer).update(world, 1f / 60f)

        val drawCall = renderer.lastDrawCalls.single()
        assertSame(mesh, drawCall.mesh)
        assertSame(material, drawCall.material)
        assertEquals(transforms, drawCall.instanceModels)
    }

    @Test
    fun meshRendererWithoutBoundsAlwaysDraws() {
        val world = worldWithPrimaryCamera()
        val entity = world.create()
        world.add(entity, Transform())
        world.add(entity, MeshRenderer(fakeMesh(), fakeMaterial()))
        val renderer = RecordingRenderer()

        RenderSystem(renderer).update(world, 1f / 60f)

        assertEquals(1, renderer.lastDrawCalls.size)
    }

    @Test
    fun meshRendererInsideTheFrustumDraws() {
        val world = worldWithPrimaryCamera()
        val entity = world.create()
        // Camera eye=(0,0,5) looks at the origin -- a unit box sitting at the origin is
        // squarely in front of it.
        world.add(entity, Transform())
        world.add(entity, MeshRenderer(fakeMesh(), fakeMaterial()))
        world.add(entity, MeshBounds(Aabb(Vec3(-0.5f, -0.5f, -0.5f), Vec3(0.5f, 0.5f, 0.5f))))
        val renderer = RecordingRenderer()

        RenderSystem(renderer).update(world, 1f / 60f)

        assertEquals(1, renderer.lastDrawCalls.size)
    }

    @Test
    fun meshRendererBehindTheCameraIsCulled() {
        val world = worldWithPrimaryCamera()
        val entity = world.create()
        // Camera eye=(0,0,5) looks toward -z (at the origin) -- z=20 is behind the eye,
        // outside the frustum entirely.
        world.add(entity, Transform(worldMatrix = Mat4().translate(0f, 0f, 20f)))
        world.add(entity, MeshRenderer(fakeMesh(), fakeMaterial()))
        world.add(entity, MeshBounds(Aabb(Vec3(-0.5f, -0.5f, -0.5f), Vec3(0.5f, 0.5f, 0.5f))))
        val renderer = RecordingRenderer()

        RenderSystem(renderer).update(world, 1f / 60f)

        assertEquals(0, renderer.lastDrawCalls.size)
    }

    @Test
    fun lodGroupPicksTheNearestLevelWhoseMaxDistanceCoversTheEntity() {
        val world = worldWithPrimaryCamera()
        val nearMesh = fakeMesh()
        val farMesh = fakeMesh()
        val entity = world.create()
        // Camera eye=(0,0,5), entity at the origin -- distance 5.
        world.add(entity, Transform())
        world.add(
            entity,
            LodGroup(
                listOf(
                    LodLevel(nearMesh, fakeMaterial(), maxDistance = 10f),
                    LodLevel(farMesh, fakeMaterial(), maxDistance = 1000f),
                ),
            ),
        )
        val renderer = RecordingRenderer()

        RenderSystem(renderer).update(world, 1f / 60f)

        assertSame(nearMesh, renderer.lastDrawCalls.single().mesh)
    }

    @Test
    fun lodGroupFallsBackToTheCoarsestLevelBeyondEveryThreshold() {
        val world = worldWithPrimaryCamera()
        val nearMesh = fakeMesh()
        val farMesh = fakeMesh()
        val entity = world.create()
        // Camera eye=(0,0,5) -- z=-995 is distance 1000, past both thresholds below.
        world.add(entity, Transform(worldMatrix = Mat4().translate(0f, 0f, -995f)))
        world.add(
            entity,
            LodGroup(
                listOf(
                    LodLevel(nearMesh, fakeMaterial(), maxDistance = 10f),
                    LodLevel(farMesh, fakeMaterial(), maxDistance = 100f),
                ),
            ),
        )
        val renderer = RecordingRenderer()

        RenderSystem(renderer).update(world, 1f / 60f)

        // LOD selects detail, it never culls -- the coarsest level still draws.
        assertSame(farMesh, renderer.lastDrawCalls.single().mesh)
    }

    private fun fakeMesh(): Mesh = object : Mesh {
        override val format = VertexFormat.PositionNormalColor
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
