// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.hellocube.app

import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.Vec3
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
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.sample.hellocube.debug.DebugVec3
import io.github.ronjunevaldoz.awake.sample.hellocube.debug.helloCubeDebugConfig
import io.github.ronjunevaldoz.awake.sample.hellocube.debug.helloCubeDebugController
import io.github.ronjunevaldoz.awake.sample.hellocube.state.HelloCubeCameraMode
import io.github.ronjunevaldoz.awake.sample.hellocube.state.HelloCubeRuntimeState
import io.github.ronjunevaldoz.awake.sample.hellocube.state.HelloCubeUiState
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HelloCubeGameTest {

    @Test
    fun helloCubeGameBuildsThroughExplicitEcsDsl() {
        val game = helloCubeGame()

        assertEquals("Hello Cube", game.windowConfig.title)
        assertEquals(1600, game.windowConfig.width)
        assertEquals(900, game.windowConfig.height)
        assertTrue(game.helloCubeDebugConfig.websocketControlsEnabled)
        assertTrue(game.helloCubeDebugConfig.offscreenProofEnabled)
        assertTrue(
            game.windowConfig.backend == GameWindowBackend.VULKAN ||
                game.windowConfig.backend == GameWindowBackend.WEBGPU
        )
    }

    @Test
    fun helloCubeGameSpecRemainsReusable() {
        val spec = helloCubeGameSpec()
        val game = spec.createGame()

        assertEquals("Hello Cube", spec.windowConfig.title)
        assertEquals(1600, game.windowConfig.width)
        assertEquals(900, game.windowConfig.height)
        assertTrue(game.helloCubeDebugConfig.websocketControlsEnabled)
        assertTrue(game.helloCubeDebugConfig.offscreenProofEnabled)
    }

    @Test
    fun helloCubeSpecsComposeThroughGameFacade() = runTest {
        val state = HelloCubeRuntimeState()
        val game = gameSpec {
            window {
                title = "Facade"
                size(800, 600)
            }
            module(
                helloCubeGameModule(
                    state = state,
                    websocketControlsEnabled = false,
                    offscreenProofEnabled = false
                )
            )
        }.createGame()

        game.ready(FakeRenderer)
        game.render(0.016f, 320f, 240f)

        assertEquals("hello-cube", game.requireService<SceneGameRuntime>().sceneName)
        assertTrue(!game.helloCubeDebugConfig.websocketControlsEnabled)
    }

    @Test
    fun helloCubeCompositionPiecesRemainReusable() = runTest {
        val state = HelloCubeRuntimeState()
        val game = gameSpec {
            window {
                title = "Reusable"
                size(640, 480)
            }
            module(
                helloCubeGameModule(
                    state = state,
                    websocketControlsEnabled = false,
                    offscreenProofEnabled = false
                )
            )
        }.createGame()

        game.ready(FakeRenderer)
        game.render(0.016f, 320f, 240f)

        assertEquals("hello-cube", game.requireService<SceneGameRuntime>().sceneName)
        assertTrue(!game.helloCubeDebugConfig.websocketControlsEnabled)
        assertTrue(!game.helloCubeDebugConfig.offscreenProofEnabled)
    }

    @Test
    fun helloCubeEcsInstallerBuildsRealRuntimeAndDebugServices() = runTest {
        val state = HelloCubeRuntimeState()
        val game = gameSpec {
            window {
                title = "Test"
                size(640, 480)
            }
            module(
                helloCubeGameModule(
                    state = state,
                    offscreenProofEnabled = false
                )
            )
        }.createGame()

        game.ready(FakeRenderer)
        val controller = game.helloCubeDebugController
        controller.switchDemo(1)
        controller.setCameraEye(Vec3(7f, 8f, 9f))
        controller.setCameraCenter(Vec3(1f, 2f, 3f))
        controller.setMinimap(true)
        val snapshot = controller.snapshot()
        game.dispose()

        assertTrue(game.helloCubeDebugConfig.websocketControlsEnabled)
        assertEquals("HELLO CUBE", snapshot.demoName)
        assertEquals(DebugVec3(7f, 8f, 9f), snapshot.cameraEye)
        assertEquals(DebugVec3(1f, 2f, 3f), snapshot.cameraCenter)
        assertEquals(true, snapshot.minimapEnabled)
        assertTrue(snapshot.debugLines.any { it.contains("FREE_FLY") })
    }

    @Test
    fun helloCubeStateContainerPublishesUiStateFlow() {
        val state = HelloCubeRuntimeState()

        state.mode = HelloCubeCameraMode.FREE_FLY
        state.minimapEnabled = true

        assertEquals(
            HelloCubeUiState(
                mode = HelloCubeCameraMode.FREE_FLY,
                minimapEnabled = true
            ),
            state.uiState.value
        )
    }
}

private object FakeRenderer : Renderer {
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

    override fun drawUi(primitives: List<UiDrawPrimitive>, font: BitmapFont?) = Unit

    override fun drawDebugLines(lines: List<LineSegment>) = Unit

    override fun destroy() = Unit
}
