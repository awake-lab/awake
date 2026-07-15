// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.engine.application.GameWindowBackend
import io.github.ronjunevaldoz.awake.engine.application.game
import io.github.ronjunevaldoz.awake.engine.application.gameModule
import io.github.ronjunevaldoz.awake.engine.application.module
import io.github.ronjunevaldoz.awake.engine.application.requireService
import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.render.texture.RenderTarget
import io.github.ronjunevaldoz.awake.render.texture.TextureAsset
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.ui
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class SceneGameDslTest {

    @Test
    fun sceneGameProvidesTypedSystemsAndCachedAssets() = runTest {
        val recordingRenderer = RecordingRenderer()
        lateinit var tickSystem: SceneSystemHandle<RecordingSystem>
        val game = game {
            ecs {
                name("runtime-proof")
                entity("camera") {
                    camera {
                        primary(true)
                    }
                }
                entity("cube") {
                    transform {
                        position(0f, 0f, 0f)
                    }
                    meshRenderer(mesh = "cube", material = "default")
                }
                assets {
                    mesh("cube") { recordingRenderer.createMesh(EmptyGeometry) }
                    material("default") { recordingRenderer.createMaterial() }
                }
                tickSystem = system("tick") {
                    RecordingSystem()
                }
            }
        }

        game.ready(recordingRenderer)
        val runtime = game.requireService<SceneGameRuntime>()
        val mesh = runtime.requireMesh("cube")
        val material = runtime.requireMaterial("default")
        val system = runtime.system(tickSystem)

        runtime.update(tickSystem, 0.25f)
        runtime.update(tickSystem, 0.5f)

        assertSame(mesh, runtime.requireMesh("cube"))
        assertSame(material, runtime.requireMaterial("default"))
        assertEquals(1, recordingRenderer.meshCreateCount)
        assertEquals(1, recordingRenderer.materialCreateCount)
        assertEquals(2, system.calls)
        assertEquals(0.75f, system.accumulatedDelta)
        assertNotNull(runtime.findEntity("cube"))

        game.dispose()

        assertEquals(1, recordingRenderer.meshDestroyCount)
        assertEquals(1, recordingRenderer.materialDestroyCount)
    }

    @Test
    fun sceneBlockCanStillReplaceDirectlyAuthoredDocument() = runTest {
        val game = game {
            ecs {
                name("before-replace")
                entity("ignored") {
                    camera { primary(true) }
                }
                scene("after-replace") {
                    entity("camera") {
                        camera {
                            primary(true)
                        }
                    }
                }
            }
        }

        game.ready(RecordingRenderer())
        val runtime = game.requireService<SceneGameRuntime>()

        assertEquals("after-replace", runtime.sceneName)
        assertEquals(null, runtime.findEntity("ignored"))
        assertNotNull(runtime.findEntity("camera"))
    }

    @Test
    fun gameSceneFacadeBuildsNamedCameraAndMeshEntities() = runTest {
        val recordingRenderer = RecordingRenderer()
        val game = game {
            scene("facade-proof") {
                cameraEntity(
                    name = "camera",
                    transform = { position(0f, 1f, 5f) },
                    camera = { primary(true) }
                )
                meshEntity(
                    name = "cube",
                    mesh = "cube",
                    material = "default",
                    transform = { scale(2f, 2f, 2f) }
                )
                assets {
                    mesh("cube") { recordingRenderer.createMesh(EmptyGeometry) }
                    material("default") { recordingRenderer.createMaterial() }
                }
                orbitCameraSystem(
                    target = "cube",
                    camera = "camera",
                    initialDistance = 8f
                ) {
                    pitch = 0.2f
                }
                freeFlyCameraSystem(camera = "camera")
            }
        }

        game.ready(recordingRenderer)
        val runtime = game.requireService<SceneGameRuntime>()

        assertEquals("facade-proof", runtime.sceneName)
        assertNotNull(runtime.findEntity("camera"))
        assertNotNull(runtime.findEntity("cube"))
        assertNotNull(runtime.findTransform("cube"))
        assertNotNull(runtime.findCamera("camera"))
        assertTrue(runtime.system("orbit") is io.github.ronjunevaldoz.awake.scene.systems.OrbitCameraSystem)
        assertTrue(runtime.system("freeFly") is io.github.ronjunevaldoz.awake.scene.systems.FreeFlyCameraSystem)
    }

    @Test
    fun ecsGameSpecComposesWindowSceneUiAndInstallers() = runTest {
        val renderer = RecordingRenderer()
        val spec = ecsGameSpec {
            window {
                title = "Facade"
                size(1280, 720)
                backend.vulkan()
            }
            scene("facade-scene") {
                cameraEntity("camera", camera = { primary(true) })
                meshEntity(
                    name = "cube",
                    mesh = "cube",
                    material = "default"
                )
                assets {
                    mesh("cube") { renderer.createMesh(EmptyGeometry) }
                    material("default") { renderer.createMaterial() }
                }
            }
            ui {
                overlay { _, _ ->
                    column(x = 16f, y = 16f, width = 180f) {
                        text(requireService<SceneGameRuntime>().sceneName)
                    }
                }
            }
            install(
                io.github.ronjunevaldoz.awake.engine.application.gameInstaller {
                    service(String::class, "facade-ready")
                }
            )
        }

        val game = spec.createGame()
        game.ready(renderer)
        game.render(0.016f, 320f, 240f)

        assertEquals("Facade", game.windowConfig.title)
        assertEquals(1280, game.windowConfig.width)
        assertEquals(720, game.windowConfig.height)
        assertEquals(GameWindowBackend.VULKAN, game.windowConfig.backend)
        assertEquals("facade-ready", game.requireService(String::class))
        assertEquals("facade-scene", game.requireService<SceneGameRuntime>().sceneName)
        assertTrue(renderer.lastUiPrimitives.any { primitive -> primitive is UiDrawPrimitive.Glyph })
    }

    @Test
    fun ecsGameSpecCanComposeRoutedSceneFlow() = runTest {
        val renderer = RecordingRenderer()
        val spec = ecsGameSpec {
            window {
                title = "Flow"
                size(1280, 720)
                backend.vulkan()
            }
            flow {
                start("overview")
                scene("overview", label = "Overview") {
                    cameraEntity("camera", camera = { primary(true) })
                    meshEntity("cube", mesh = "cube", material = "default")
                    assets {
                        mesh("cube") { renderer.createMesh(EmptyGeometry) }
                        material("default") { renderer.createMaterial() }
                    }
                }
                scene("editor", label = "Editor") {
                    cameraEntity("camera", camera = { primary(true) })
                }
            }
        }

        val game = spec.createGame()
        game.ready(renderer)

        val router = game.requireService<SceneRouterRuntime>()
        assertEquals("overview", router.activeSceneId)
        router.switchTo("editor")
        game.render(0.016f, 320f, 240f)
        assertEquals("editor", router.activeSceneId)
    }

    @Test
    fun gameModuleCanOwnSceneAndUiComposition() = runTest {
        val renderer = RecordingRenderer()
        val module = gameModule {
            scene("module-scene") {
                cameraEntity("camera", camera = { primary(true) })
                meshEntity(
                    name = "cube",
                    mesh = "cube",
                    material = "default"
                )
                assets {
                    mesh("cube") { renderer.createMesh(EmptyGeometry) }
                    material("default") { renderer.createMaterial() }
                }
            }
            ui {
                overlay { _, _ ->
                    column(x = 16f, y = 16f, width = 180f) {
                        text(requireService<SceneGameRuntime>().sceneName)
                    }
                }
            }
        }

        val game = game {
            module(module)
        }

        game.ready(renderer)
        game.render(0.016f, 320f, 240f)

        assertEquals("module-scene", game.requireService<SceneGameRuntime>().sceneName)
        assertTrue(renderer.lastUiPrimitives.any { primitive -> primitive is UiDrawPrimitive.Glyph })
    }
}

private class RecordingSystem : System {
    var calls = 0
    var accumulatedDelta = 0f

    override fun update(world: World, delta: Float) {
        calls += 1
        accumulatedDelta += delta
    }
}

private val EmptyGeometry = MeshGeometry(vertices = floatArrayOf(), indices = intArrayOf())

private class RecordingRenderer : Renderer {
    var meshCreateCount = 0
    var materialCreateCount = 0
    var meshDestroyCount = 0
    var materialDestroyCount = 0
    var lastUiPrimitives: List<UiDrawPrimitive> = emptyList()

    override val flipYForClipSpace: Boolean = false

    override fun createMesh(geometry: MeshGeometry): Mesh {
        meshCreateCount += 1
        return object : Mesh {
            override fun bind(commandBuffer: Long) = Unit
            override fun draw(commandBuffer: Long) = Unit

            override fun destroy() {
                meshDestroyCount += 1
            }
        }
    }

    override fun createMaterial(texture: TextureAsset?, renderTarget: RenderTarget?): Material {
        materialCreateCount += 1
        return object : Material {
            override fun updateUniformBuffer(mvp: FloatArray) = Unit
            override fun bind(commandBuffer: Long, pipelineLayout: Long) = Unit

            override fun destroy() {
                materialDestroyCount += 1
            }
        }
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
