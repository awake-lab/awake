// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.rendering

import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.ClipSpace
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.renderer.DEFAULT_SCENE_LIGHT
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.render.renderer.SceneLight
import io.github.ronjunevaldoz.awake.render.texture.PbrTextureSet
import io.github.ronjunevaldoz.awake.render.texture.RenderTarget
import io.github.ronjunevaldoz.awake.render.texture.TextureAsset
import io.github.ronjunevaldoz.awake.scene.rendering.components.Light
import io.github.ronjunevaldoz.awake.scene.rendering.systems.RenderSystem
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** [io.github.ronjunevaldoz.awake.scene.rendering.systems.RenderSystem] doesn't shade anything itself -- it just resolves the scene's [io.github.ronjunevaldoz.awake.scene.rendering.components.Light] entity
 * (or [io.github.ronjunevaldoz.awake.render.renderer.DEFAULT_SCENE_LIGHT] when there isn't one) into the backend-neutral [io.github.ronjunevaldoz.awake.render.renderer.SceneLight]
 * [io.github.ronjunevaldoz.awake.render.renderer.Renderer.draw] expects, same "world state in, render-api call out" shape the mesh/camera
 * side already has. A recording fake [io.github.ronjunevaldoz.awake.render.renderer.Renderer] (matching [Scene3DPlaygroundUiTest]'s own
 * `RecordingScene3DRenderer` shape) captures what actually reached [io.github.ronjunevaldoz.awake.render.renderer.Renderer.draw] without
 * needing a real GPU backend. */
class RenderSystemTest {
    private class RecordingRenderer : Renderer {
        var lastLight: SceneLight? = null
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
}
