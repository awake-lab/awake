// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.engine.application.game
import io.github.ronjunevaldoz.awake.engine.application.gameModule
import io.github.ronjunevaldoz.awake.engine.application.module
import io.github.ronjunevaldoz.awake.engine.application.ui
import io.github.ronjunevaldoz.awake.engine.application.requireService
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
import io.github.ronjunevaldoz.awake.scene.runtime.entities.cameraEntity
import io.github.ronjunevaldoz.awake.scene.runtime.entities.meshEntity
import io.github.ronjunevaldoz.awake.scene.runtime.systems.cameraSystem
import io.github.ronjunevaldoz.awake.scene.runtime.dsl.Modifier
import io.github.ronjunevaldoz.awake.scene.runtime.dsl.camera
import io.github.ronjunevaldoz.awake.scene.runtime.dsl.transform
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.offset
import io.github.ronjunevaldoz.awake.ui.modifier.size
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
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
                scene {
                    entity("camera", Modifier().camera())
                    entity("cube", Modifier().transform())
                }
                assets {
                    mesh("cube") { recordingRenderer.createMesh(EmptyGeometry) }
                    material("default") { recordingRenderer.createMaterial() }
                }
                tickSystem = fixedSystem("tick") {
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
                entity("ignored")
                scene("after-replace") {
                    entity("camera", Modifier().camera())
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
                cameraEntity("camera", Modifier().transform(y = 1f, z = 5f))
                meshEntity(
                    "cube",
                    recordingRenderer.createMesh(EmptyGeometry),
                    recordingRenderer.createMaterial(),
                    Modifier().transform(sx = 2f, sy = 2f, sz = 2f)
                )
                assets {
                    mesh("cube") { recordingRenderer.createMesh(EmptyGeometry) }
                    material("default") { recordingRenderer.createMaterial() }
                }
                cameraSystem()
            }
        }

        game.ready(recordingRenderer)
        val runtime = game.requireService<SceneGameRuntime>()

        assertEquals("facade-proof", runtime.sceneName)
        assertNotNull(runtime.findEntity("camera"))
        assertNotNull(runtime.findEntity("cube"))
        assertNotNull(runtime.findTransform("cube"))
        assertNotNull(runtime.findCamera("camera"))
        assertTrue(runtime.system("camera") is io.github.ronjunevaldoz.awake.scene.systems.CameraSystem)
    }

    @Test
    fun gameModuleCanOwnSceneAndUiComposition() = runTest {
        val renderer = RecordingRenderer()
        val module = gameModule {
            scene("module-scene") {
                cameraEntity("camera")
                meshEntity("cube", renderer.createMesh(EmptyGeometry), renderer.createMaterial())
                assets {
                    mesh("cube") { renderer.createMesh(EmptyGeometry) }
                    material("default") { renderer.createMaterial() }
                }
            }
            ui {
                overlay {
                    val scene = requireService<SceneGameRuntime>()
                    rootColumn(modifier = Modifier.offset(16f.dp, 16f.dp).size(180f.dp, 120f.dp)) {
                        text(scene.sceneName)
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

    @Test
    fun sceneRuntimeStagesUiBeforeInfrastructureRender() = runTest {
        val renderer = RecordingRenderer()
        val game = game {
            scene("ordered-scene") {
                cameraEntity("camera")
                overlay { width, height ->
                    frame(width, height) {
                        text("ordered")
                    }
                }
            }
        }

        game.ready(renderer)
        renderer.frameCalls.clear()
        game.render(0.016f, 320f, 240f)

        assertEquals(listOf("drawUi", "draw"), renderer.frameCalls)
    }

    @Test
    fun explicitSystemPhasesSeparateFixedStepsFromRenderedFrames() = runTest {
        lateinit var fixedHandle: SceneSystemHandle<RecordingSystem>
        lateinit var frameHandle: SceneSystemHandle<RecordingSystem>
        val game = game {
            scene("phase-proof") {
                cameraEntity("camera")
                fixedHandle = fixedSystem("fixed") { RecordingSystem() }
                frameHandle = frameSystem("frame") { RecordingSystem() }
            }
        }

        game.ready(RecordingRenderer())
        val runtime = game.requireService<SceneGameRuntime>()
        val fixedSystem = runtime.system(fixedHandle)
        val frameSystem = runtime.system(frameHandle)
        fixedSystem.reset()
        frameSystem.reset()

        game.render(0.001f, 320f, 240f)

        assertEquals(0, fixedSystem.calls)
        assertEquals(1, frameSystem.calls)

        game.render(1f / 60f, 320f, 240f)

        assertEquals(1, fixedSystem.calls)
        assertEquals(2, frameSystem.calls)
    }
}

private class RecordingSystem : System {
    var calls = 0
    var accumulatedDelta = 0f

    override fun update(world: World, delta: Float) {
        calls += 1
        accumulatedDelta += delta
    }

    fun reset() {
        calls = 0
        accumulatedDelta = 0f
    }
}

private val EmptyGeometry = MeshGeometry(vertices = floatArrayOf(), indices = intArrayOf())

private class RecordingRenderer : Renderer {
    var meshCreateCount = 0
    var materialCreateCount = 0
    var meshDestroyCount = 0
    var materialDestroyCount = 0
    var lastUiPrimitives: List<UiDrawPrimitive> = emptyList()
    val frameCalls = mutableListOf<String>()

    override val flipYForClipSpace: Boolean = false
    override var clearColor: FloatArray = floatArrayOf(0f, 0f, 0f, 1f)

    override fun createMesh(geometry: MeshGeometry): Mesh {
        meshCreateCount += 1
        return object : Mesh {
            override val format: VertexFormat = geometry.format
            override fun bind(commandBuffer: Long) = Unit
            override fun draw(commandBuffer: Long) = Unit

            override fun destroy() {
                meshDestroyCount += 1
            }
        }
    }

    override fun createMaterial(texture: TextureAsset?, renderTarget: RenderTarget?, uniformFloatCount: Int): Material {
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

    override fun draw(camera: Camera, drawCalls: List<DrawCall>, light: SceneLight) {
        frameCalls += "draw"
    }

    override fun renderToTexture(target: RenderTarget, camera: Camera, drawCalls: List<DrawCall>) = Unit

    override suspend fun readPixels(target: RenderTarget): TextureAsset =
        TextureAsset(ByteArray(target.width * target.height * 4), target.width, target.height)

    override fun drawUi(primitives: List<UiDrawPrimitive>, font: UiFont?) {
        frameCalls += "drawUi"
        lastUiPrimitives = primitives
    }

    override fun drawDebugLines(lines: List<LineSegment>) = Unit

    override fun destroy() = Unit
}
