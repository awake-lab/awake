// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.gameauthoring

import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.ClipSpace
import io.github.ronjunevaldoz.awake.engine.game.GameInstaller
import io.github.ronjunevaldoz.awake.engine.game.GameSpecBuilder
import io.github.ronjunevaldoz.awake.engine.game.GameWindowBackend
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.render.renderer.SceneLight
import io.github.ronjunevaldoz.awake.render.texture.RenderTarget
import io.github.ronjunevaldoz.awake.render.texture.TextureAsset
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GameDslTest {

    @Test
    fun gameDslRoutesLifecycleCallbacks() = runTest {
        var readyCalls = 0
        var renderDelta = 0f
        var renderWidth = 0f
        var renderHeight = 0f
        var resized = ""
        var paused = false
        var resumed = false
        var disposed = false

        val game = game {
            ready { readyCalls++ }
            render { delta, viewportWidth, viewportHeight ->
                renderDelta = delta
                renderWidth = viewportWidth
                renderHeight = viewportHeight
            }
            resize { width, height ->
                resized = "$width x $height"
            }
            pause { paused = true }
            resume { resumed = true }
            dispose { disposed = true }
        }

        game.ready(FakeRenderer)
        game.render(0.016f, 1280f, 720f)
        game.resize(640f, 360f)
        game.pause()
        game.resume()
        game.dispose()

        assertEquals(1, readyCalls)
        assertEquals(0.016f, renderDelta)
        assertEquals(1280f, renderWidth)
        assertEquals(720f, renderHeight)
        assertEquals("640.0 x 360.0", resized)
        assertTrue(paused)
        assertTrue(resumed)
        assertTrue(disposed)
    }

    @Test
    fun gameDslCapturesWindowConfiguration() {
        val game = game {
            window {
                title = "Hello Cube"
                size(1600, 900)
                backend.vulkan()
            }
        }

        assertEquals("Hello Cube", game.windowConfig.title)
        assertEquals(1600, game.windowConfig.width)
        assertEquals(900, game.windowConfig.height)
        assertEquals(GameWindowBackend.VULKAN, game.windowConfig.backend)
    }

    @Test
    fun gameSpecCanBeBuiltAndCreatedSeparately() = runTest {
        var readyCalls = 0
        val spec = gameSpec {
            window {
                title = "Spec First"
                size(960, 540)
                backend.webGpu()
            }
            ready { readyCalls += 1 }
            service(String::class, "runtime-proof")
        }

        val game = spec.createGame()
        game.ready(FakeRenderer)

        assertEquals("Spec First", spec.windowConfig.title)
        assertEquals(960, game.windowConfig.width)
        assertEquals(540, game.windowConfig.height)
        assertEquals(GameWindowBackend.WEBGPU, game.windowConfig.backend)
        assertEquals("runtime-proof", game.requireService(String::class))
        assertEquals(1, readyCalls)
    }

    @Test
    fun gameDslCanInstallFeatureServices() {
        val game = game {
            install(
                object : GameInstaller {
                    override fun install(into: GameSpecBuilder) {
                        into.service(String::class, "debug")
                    }
                },
            )
        }

        assertEquals("debug", game.requireService(String::class))
    }

    @Test
    fun gameDslCanInstallReusableGameModule() = runTest {
        val events = mutableListOf<String>()
        val feature = gameModule {
            service(String::class, "module-service")
            ready { events += "module-ready" }
            render { _, _, _ -> events += "module-render" }
            dispose { events += "module-dispose" }
        }

        val game = game {
            ready { events += "root-ready" }
            module(feature)
        }

        game.ready(FakeRenderer)
        game.render(0.016f, 320f, 240f)
        game.dispose()

        assertEquals("module-service", game.requireService(String::class))
        assertEquals(
            listOf("root-ready", "module-ready", "module-render", "module-dispose"),
            events,
        )
    }

    @Test
    fun gameModuleCanCreateGameShellDirectly() = runTest {
        val events = mutableListOf<String>()
        val feature = gameModule {
            service(String::class, "feature-service")
            ready { events += "module-ready" }
        }

        val game = feature.createGame {
            title = "Module Shell"
            size(1024, 576)
            backend.webGpu()
        }

        game.ready(FakeRenderer)

        assertEquals("Module Shell", game.windowConfig.title)
        assertEquals(1024, game.windowConfig.width)
        assertEquals(576, game.windowConfig.height)
        assertEquals(GameWindowBackend.WEBGPU, game.windowConfig.backend)
        assertEquals("feature-service", game.requireService(String::class))
        assertEquals(listOf("module-ready"), events)
    }

    @Test
    fun gameDefinitionOwnsStateWindowAndModuleFactory() = runTest {
        val definition = gameDefinition(createState = { mutableListOf("state") }) {
            window {
                title = "Definition Shell"
                size(1280, 720)
                backend.vulkan()
            }
            module { state ->
                gameModule {
                    service(List::class, state)
                    ready { state += "ready" }
                }
            }
        }

        val state = definition.createState()
        val spec = definition.createGameSpec(state)
        val game = spec.createGame()
        game.ready(FakeRenderer)

        assertEquals("Definition Shell", spec.windowConfig.title)
        assertEquals(1280, game.windowConfig.width)
        assertEquals(720, game.windowConfig.height)
        assertEquals(GameWindowBackend.VULKAN, game.windowConfig.backend)
        assertEquals(listOf("state", "ready"), state)
        assertEquals(state, game.requireService(List::class))
    }

    @Test
    fun gameDslComposesInstallerCallbacksInOrder() = runTest {
        val events = mutableListOf<String>()
        val game = game {
            ready { events += "root-ready" }
            render { _, _, _ -> events += "root-render" }
            dispose { events += "root-dispose" }
            install(
                object : GameInstaller {
                    override fun install(into: GameSpecBuilder) {
                        into.ready { events += "feature-ready" }
                        into.render { _, _, _ -> events += "feature-render" }
                        into.dispose { events += "feature-dispose" }
                    }
                },
            )
        }

        game.ready(FakeRenderer)
        game.render(0.016f, 320f, 200f)
        game.dispose()

        assertEquals(
            listOf(
                "root-ready",
                "feature-ready",
                "root-render",
                "feature-render",
                "feature-dispose",
                "root-dispose",
            ),
            events,
        )
    }
}

private object FakeRenderer : Renderer {
    override val clipSpace: ClipSpace = ClipSpace.WebGpu
    override var clearColor: FloatArray = floatArrayOf(0f, 0f, 0f, 1f)
    override var wireframe: Boolean = false
    override var shadowsEnabled: Boolean = true

    override fun createMesh(geometry: MeshGeometry): Mesh = object : Mesh {
        override val format: VertexFormat =
            geometry.format

        override fun bind(commandBuffer: Long) = Unit
        override fun draw(commandBuffer: Long) = Unit
        override fun destroy() = Unit
    }

    override fun createMaterial(
        texture: TextureAsset?,
        renderTarget: RenderTarget?,
        uniformFloatCount: Int,
    ): Material = object : Material {
        override fun updateUniformBuffer(mvp: FloatArray) = Unit
        override fun bind(commandBuffer: Long, pipelineLayout: Long) = Unit
        override fun destroy() = Unit
    }

    override fun createRenderTarget(width: Int, height: Int): RenderTarget = object : RenderTarget {
        override val width: Int = width
        override val height: Int = height
        override fun destroy() = Unit
    }

    override fun draw(camera: Camera, drawCalls: List<DrawCall>, light: SceneLight) = Unit

    override fun renderToTexture(target: RenderTarget, camera: Camera, drawCalls: List<DrawCall>) =
        Unit

    override suspend fun readPixels(target: RenderTarget): TextureAsset =
        TextureAsset(ByteArray(0), 0, 0)

    override fun drawUi(primitives: List<UiDrawPrimitive>, font: UiFont?) = Unit

    override fun drawDebugLines(lines: List<LineSegment>) = Unit

    override fun destroy() = Unit
}
