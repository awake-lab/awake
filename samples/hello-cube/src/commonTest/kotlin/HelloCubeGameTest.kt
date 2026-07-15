// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.engine.application.GameWindowBackend
import io.github.ronjunevaldoz.awake.engine.application.game
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.render.texture.RenderTarget
import io.github.ronjunevaldoz.awake.render.texture.TextureAsset
import io.github.ronjunevaldoz.awake.scene.runtime.ecs
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
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
    fun helloCubeEcsInstallerBuildsRealRuntimeAndDebugServices() = runTest {
        val state = HelloCubeRuntimeState()
        val game = game {
            window {
                title = "Test"
                size(640, 480)
            }
            ecs {
                name("test-scene")
                entity("camera") {
                    camera {
                        eye(0f, 0f, 5f)
                        center(0f, 0f, 0f)
                        up(0f, 1f, 0f)
                        primary(true)
                    }
                }
                entity("cube") {
                    meshRenderer(mesh = "cube", material = "default")
                }
                helloCubeAssets()
                helloCubeCameraControls(state)
            }
            install(
                helloCubeDebug(state) {
                    websocketControls()
                    offscreenProof(false)
                }
            )
        }

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
