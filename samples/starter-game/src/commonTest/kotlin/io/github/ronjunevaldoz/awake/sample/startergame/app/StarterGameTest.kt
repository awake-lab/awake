// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.startergame.app

import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.engine.application.GameWindowBackend
import io.github.ronjunevaldoz.awake.engine.application.gameSpec
import io.github.ronjunevaldoz.awake.engine.application.module
import io.github.ronjunevaldoz.awake.engine.application.requireService
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.render.texture.RenderTarget
import io.github.ronjunevaldoz.awake.render.texture.TextureAsset
import io.github.ronjunevaldoz.awake.sample.startergame.debug.starterGameDebugConfig
import io.github.ronjunevaldoz.awake.sample.startergame.debug.starterGameDebugController
import io.github.ronjunevaldoz.awake.sample.startergame.debug.starterDebugModule
import io.github.ronjunevaldoz.awake.sample.startergame.scene.STARTER_SCENE_EDITOR
import io.github.ronjunevaldoz.awake.sample.startergame.scene.STARTER_SCENE_OVERVIEW
import io.github.ronjunevaldoz.awake.sample.startergame.scene.starterSceneModule
import io.github.ronjunevaldoz.awake.sample.startergame.state.StarterGameUiState
import io.github.ronjunevaldoz.awake.sample.startergame.state.StarterGameRuntimeState
import io.github.ronjunevaldoz.awake.sample.startergame.ui.starterUiModule
import io.github.ronjunevaldoz.awake.scene.runtime.SceneRouterRuntime
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StarterGameTest {

    @Test
    fun starterGameBuildsReusableSpec() {
        val spec = starterGameSpec()
        val game = spec.createGame()

        assertEquals("Awake Starter Game", spec.windowConfig.title)
        assertEquals(1600, game.windowConfig.width)
        assertEquals(900, game.windowConfig.height)
        assertTrue(
            game.windowConfig.backend == GameWindowBackend.VULKAN ||
                game.windowConfig.backend == GameWindowBackend.WEBGPU
        )
    }

    @Test
    fun starterGameSceneSwitchesThroughRouterAndKeepsUi() = runTest {
        val renderer = RecordingRenderer()
        val game = starterGame()

        game.ready(renderer)
        game.render(0.016f, 1280f, 720f)

        val router = game.requireService<SceneRouterRuntime>()
        assertEquals(STARTER_SCENE_OVERVIEW, router.activeSceneId)

        router.switchTo(STARTER_SCENE_EDITOR)
        game.render(0.016f, 1280f, 720f)

        assertEquals(STARTER_SCENE_EDITOR, router.activeSceneId)
        assertTrue(renderer.lastUiPrimitives.any { primitive -> primitive is UiDrawPrimitive.Glyph })
    }

    @Test
    fun starterGameModuleStaysComposable() = runTest {
        val renderer = RecordingRenderer()
        val state = StarterGameRuntimeState()
        val game = gameSpec {
            window {
                title = "Starter Facade"
                size(800, 600)
            }
            module(
                starterGameModule(
                    state = state,
                    websocketControlsEnabled = false
                )
            )
        }.createGame()

        game.ready(renderer)
        game.render(0.016f, 800f, 600f)

        assertEquals("overview", game.requireService<SceneRouterRuntime>().activeSceneId)
        assertTrue(!game.starterGameDebugConfig.websocketControlsEnabled)
    }

    @Test
    fun starterGameFeaturesCanComposeIndividually() = runTest {
        val renderer = RecordingRenderer()
        val state = StarterGameRuntimeState()
        val game = gameSpec {
            window {
                title = "Starter Features"
                size(1024, 640)
            }
            module(starterSceneModule())
            module(starterUiModule(state))
            module(
                starterDebugModule(
                    state = state,
                    websocketControlsEnabled = false
                )
            )
        }.createGame()

        game.ready(renderer)
        game.render(0.016f, 1024f, 640f)

        assertEquals(STARTER_SCENE_OVERVIEW, game.requireService<SceneRouterRuntime>().activeSceneId)
        assertTrue(renderer.lastUiPrimitives.any { primitive -> primitive is UiDrawPrimitive.Glyph })
        assertTrue(!game.starterGameDebugConfig.websocketControlsEnabled)
    }

    @Test
    fun starterGameDebugControllerSnapshotsActiveScene() = runTest {
        val renderer = RecordingRenderer()
        val game = starterGame()

        game.ready(renderer)
        game.render(0.016f, 1280f, 720f)
        val debugController = game.starterGameDebugController

        debugController.switchScene(STARTER_SCENE_EDITOR)
        game.render(0.016f, 1280f, 720f)
        val snapshot = debugController.snapshot()

        assertEquals(STARTER_SCENE_EDITOR, snapshot.activeSceneId)
        assertTrue(snapshot.sceneLabels.contains("Overview"))
        assertTrue(snapshot.sceneLabels.contains("Editor"))
    }

    @Test
    fun starterGameStateContainerPublishesUiStateFlow() {
        val state = StarterGameRuntimeState()

        state.tipsVisible = false
        state.showcaseDangerMode = true
        state.showcasePrimaryClicks = 2

        assertEquals(
            StarterGameUiState(
                tipsVisible = false,
                showcaseBadgeVariantIndex = 0,
                showcaseLiveBadge = true,
                showcaseDangerMode = true,
                showcaseSurfaceRadius = 12f,
                showcasePrimaryClicks = 2
            ),
            state.uiState.value
        )
    }
}

private class RecordingRenderer : Renderer {
    var lastUiPrimitives: List<UiDrawPrimitive> = emptyList()

    override val flipYForClipSpace: Boolean = false

    override fun createMesh(geometry: MeshGeometry): Mesh = object : Mesh {
        override fun bind(commandBuffer: Long) = Unit
        override fun draw(commandBuffer: Long) = Unit
        override fun destroy() = Unit
    }

    override fun createMaterial(texture: TextureAsset?, renderTarget: RenderTarget?): Material = object : Material {
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

    override fun drawUi(primitives: List<UiDrawPrimitive>, font: BitmapFont?) {
        lastUiPrimitives = primitives
    }

    override fun drawDebugLines(lines: List<LineSegment>) = Unit

    override fun destroy() = Unit
}
