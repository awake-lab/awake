// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.render.renderer.RenderViewport
import io.github.ronjunevaldoz.awake.scene.core.components.Name
import io.github.ronjunevaldoz.awake.scene.rendering.components.Camera
import io.github.ronjunevaldoz.awake.testing.render.NoopRenderer
import io.github.ronjunevaldoz.awake.testing.ui.uiTestSession
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnThemeValues
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import kotlin.test.Test
import kotlin.test.assertEquals
import io.github.ronjunevaldoz.awake.core.math.Camera as CoreCamera

/** Renderer double with real [sceneViewport] storage -- the contract's own accessors ignore
 * writes (see `Renderer.sceneViewport`), so a plain [NoopRenderer] cannot observe the borrow. */
private class ViewportRecordingRenderer : NoopRenderer() {
    override var sceneViewport: RenderViewport? = null
}

private fun studioCamera() = CoreCamera(
    eye = Vec3(0f, 5f, 10f),
    center = Vec3(0f, 0f, 0f),
    fovYRadians = 0.8f,
    near = 0.1f,
    far = 100f,
)

class InspectorCameraPreviewTest {

    @Test
    fun onlyASelectedCameraEntityGetsAPreviewQuad() {
        val world = World()
        val cameraEntity = world.create()
        world.add(cameraEntity, Name("camera"))
        world.add(cameraEntity, Camera(studioCamera()))
        val cubeEntity = world.create()
        world.add(cubeEntity, Name("Cube"))

        val preview = StudioCameraPreview()
        preview.render(ViewportRecordingRenderer(), studioCamera(), emptyList())

        uiTestSession(width = 320f, height = 600f, font = BitmapFont()) {
            fun textureQuads(selectedEntityId: Int) = frame {
                shadcnTheme(theme = shadcnThemeValues(dark = true)) {
                    drawInspectorPanel(world, selectedEntityId, preview)
                }
            }.primitives.count { it is UiDrawPrimitive.Texture }

            assertEquals(1, textureQuads(cameraEntity.id), "a camera entity must show its preview")
            assertEquals(0, textureQuads(cubeEntity.id), "an entity with no Camera must show none")
        }
    }

    /** The shell points `sceneViewport` at the viewport panel in window coordinates, and Vulkan
     * applies that rect to `renderToTexture` too -- leaving it cleared would silently confine the
     * next frame's main scene pass to the whole surface. */
    @Test
    fun thePreviewPassRestoresTheSceneViewportItBorrowed() {
        val renderer = ViewportRecordingRenderer()
        val panel = RenderViewport(x = 200f, y = 80f, width = 900f, height = 600f)
        renderer.sceneViewport = panel

        StudioCameraPreview().render(renderer, studioCamera(), emptyList())

        assertEquals(panel, renderer.sceneViewport)
    }
}
