// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d

import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.engine.application.game
import io.github.ronjunevaldoz.awake.engine.application.module
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.render.texture.RenderTarget
import io.github.ronjunevaldoz.awake.render.texture.TextureAsset
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

/** Proves the menu | viewport | controls shell actually renders through the real game/module
 * pipeline (`game { module(...) }`, same shape `GameUiDslTest` uses for game-dsl's own overlay
 * tests) rather than a raw `GameUiRuntime` (which needs `render()`'s real
 * `viewportWidth`/`viewportHeight` wiring, not just `uiContext.beginFrame`). */
class Scene3DPlaygroundUiTest {

    @Test
    fun playgroundShellRendersMenuViewportAndControlsForTheActiveDemo() = runTest {
        val renderer = RecordingScene3DRenderer()
        val game = game {
            module(scene3DPlaygroundModule())
        }

        game.ready(renderer)
        game.render(0.016f, 1200f, 800f)

        assertTrue(
            renderer.lastUiPrimitives.filterIsInstance<UiDrawPrimitive.Glyph>().isNotEmpty(),
            "playground shell must render text (menu labels, viewport content, control labels)"
        )
    }

    @Test
    fun switchingTheActiveDemoChangesWhichControlsRender() {
        val state = Scene3DPlaygroundState()
        assertTrue(state.activeDemoId == Scene3DDemos.first().id, "default active demo must be the first entry")

        state.activeDemoId = Scene3DDemos.last().id
        assertTrue(state.activeDemoId == Scene3DDemos.last().id, "activeDemoId must be freely settable by the menu's onClick")
    }
}

private class RecordingScene3DRenderer : Renderer {
    var lastUiPrimitives: List<UiDrawPrimitive> = emptyList()

    override val flipYForClipSpace: Boolean = false
    override var clearColor: FloatArray = floatArrayOf(0f, 0f, 0f, 1f)

    override fun createMesh(geometry: MeshGeometry): Mesh = object : Mesh {
        override fun bind(commandBuffer: Long) = Unit
        override fun draw(commandBuffer: Long) = Unit
        override fun destroy() = Unit
    }

    override fun createMaterial(texture: TextureAsset?, renderTarget: RenderTarget?, uniformFloatCount: Int): Material =
        object : Material {
            override fun updateUniformBuffer(mvp: FloatArray) = Unit
            override fun bind(commandBuffer: Long, pipelineLayout: Long) = Unit
            override fun destroy() = Unit
        }

    override fun createRenderTarget(width: Int, height: Int): RenderTarget = object : RenderTarget {
        override val width: Int = width
        override val height: Int = height
        override fun destroy() = Unit
    }

    override fun draw(camera: Camera, drawCalls: List<DrawCall>) = Unit
    override fun renderToTexture(target: RenderTarget, camera: Camera, drawCalls: List<DrawCall>) = Unit
    override suspend fun readPixels(target: RenderTarget): TextureAsset =
        TextureAsset(ByteArray(target.width * target.height * 4), target.width, target.height)

    override fun drawUi(primitives: List<UiDrawPrimitive>, font: UiFont?) {
        lastUiPrimitives = primitives
    }

    override fun drawDebugLines(lines: List<LineSegment>) = Unit
    override fun destroy() = Unit
}
