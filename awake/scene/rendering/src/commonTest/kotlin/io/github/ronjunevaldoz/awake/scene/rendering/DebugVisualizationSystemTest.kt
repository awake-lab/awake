// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.rendering

import io.github.ronjunevaldoz.awake.core.math.Aabb
import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.ClipSpace
import io.github.ronjunevaldoz.awake.core.math.Frustum
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.render.renderer.SceneLight
import io.github.ronjunevaldoz.awake.render.texture.PbrTextureSet
import io.github.ronjunevaldoz.awake.render.texture.RenderTarget
import io.github.ronjunevaldoz.awake.render.texture.TextureAsset
import io.github.ronjunevaldoz.awake.scene.core.components.Transform
import io.github.ronjunevaldoz.awake.scene.rendering.components.MeshBounds
import io.github.ronjunevaldoz.awake.scene.rendering.components.WorldDebugSettings
import io.github.ronjunevaldoz.awake.scene.rendering.systems.CONSERVATIVE_ASPECT
import io.github.ronjunevaldoz.awake.scene.rendering.systems.DebugVisualizationSystem
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DebugVisualizationSystemTest {
    private class RecordingRenderer : Renderer {
        var lastDebugLines: List<LineSegment>? = null
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
        ): Material = error("not needed for this test")

        override fun createRenderTarget(width: Int, height: Int): RenderTarget = error("not needed for this test")
        override fun draw(camera: Camera, drawCalls: List<DrawCall>, light: SceneLight) = Unit
        override fun renderToTexture(target: RenderTarget, camera: Camera, drawCalls: List<DrawCall>) = Unit
        override suspend fun readPixels(target: RenderTarget): TextureAsset = error("not needed for this test")
        override fun drawUi(primitives: List<UiDrawPrimitive>, font: UiFont?) = Unit
        override fun drawDebugLines(lines: List<LineSegment>) {
            lastDebugLines = lines
        }

        override fun destroy() = Unit
    }

    private fun worldWithPrimaryCamera(): World {
        val world = World()
        val cameraEntity = world.create()
        world.add(
            cameraEntity,
            io.github.ronjunevaldoz.awake.scene.rendering.components.Camera(
                Camera(eye = Vec3(0f, 0f, 5f), center = Vec3(0f, 0f, 0f), fovYRadians = 1f, near = 0.1f, far = 100f),
            ),
        )
        return world
    }

    @Test
    fun drawsNothingWhenNoWorldDebugSettingsExists() {
        val world = worldWithPrimaryCamera()
        val renderer = RecordingRenderer()

        DebugVisualizationSystem(renderer).update(world, 1f / 60f)

        assertEquals(null, renderer.lastDebugLines)
    }

    @Test
    fun drawsNothingWhenEveryToggleIsOff() {
        val world = worldWithPrimaryCamera()
        world.add(world.create(), WorldDebugSettings())
        val renderer = RecordingRenderer()

        DebugVisualizationSystem(renderer).update(world, 1f / 60f)

        assertEquals(null, renderer.lastDebugLines)
    }

    @Test
    fun showFrustumDrawsOneLinePerFrustumEdge() {
        val world = worldWithPrimaryCamera()
        world.add(world.create(), WorldDebugSettings(showFrustum = true))
        val renderer = RecordingRenderer()

        DebugVisualizationSystem(renderer).update(world, 1f / 60f)

        assertEquals(Frustum.EDGES.size, renderer.lastDebugLines?.size)
    }

    @Test
    fun showBoundsDrawsOneBoxPerMeshBoundsEntity() {
        val world = worldWithPrimaryCamera()
        world.add(world.create(), WorldDebugSettings(showBounds = true))
        val entity = world.create()
        world.add(entity, Transform())
        world.add(entity, MeshBounds(Aabb(Vec3(-0.5f, -0.5f, -0.5f), Vec3(0.5f, 0.5f, 0.5f))))
        val renderer = RecordingRenderer()

        DebugVisualizationSystem(renderer).update(world, 1f / 60f)

        assertEquals(Aabb.EDGES.size, renderer.lastDebugLines?.size)
    }

    @Test
    fun bothTogglesCombineTheirLines() {
        val world = worldWithPrimaryCamera()
        world.add(world.create(), WorldDebugSettings(showFrustum = true, showBounds = true))
        val entity = world.create()
        world.add(entity, Transform())
        world.add(entity, MeshBounds(Aabb(Vec3(-0.5f, -0.5f, -0.5f), Vec3(0.5f, 0.5f, 0.5f))))
        val renderer = RecordingRenderer()

        DebugVisualizationSystem(renderer).update(world, 1f / 60f)

        assertEquals(Frustum.EDGES.size + Aabb.EDGES.size, renderer.lastDebugLines?.size)
        assertTrue(CONSERVATIVE_ASPECT > 1f, "sanity: the shared conservative aspect constant is importable here")
    }
}
