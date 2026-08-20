// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.authoring

import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.ClipSpace
import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.engine.app.dsl.requireService
import io.github.ronjunevaldoz.awake.engine.platformauthoring.dsl.app
import io.github.ronjunevaldoz.awake.engine.platformauthoring.dsl.appModule
import io.github.ronjunevaldoz.awake.engine.platformauthoring.dsl.module
import io.github.ronjunevaldoz.awake.engine.platformauthoring.ui.ui
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.render.renderer.SceneLight
import io.github.ronjunevaldoz.awake.render.texture.PbrTextureSet
import io.github.ronjunevaldoz.awake.render.texture.RenderTarget
import io.github.ronjunevaldoz.awake.render.texture.TextureAsset
import io.github.ronjunevaldoz.awake.scene.authoring.blueprints.cameraEntity
import io.github.ronjunevaldoz.awake.scene.authoring.blueprints.meshEntity
import io.github.ronjunevaldoz.awake.scene.authoring.dsl.Modifier
import io.github.ronjunevaldoz.awake.scene.authoring.dsl.camera
import io.github.ronjunevaldoz.awake.scene.authoring.dsl.transform
import io.github.ronjunevaldoz.awake.scene.authoring.infrastructure.cameraSystem
import io.github.ronjunevaldoz.awake.scene.controls.systems.CameraSystem
import io.github.ronjunevaldoz.awake.scene.runtime.SceneAppLifecycleRuntime
import io.github.ronjunevaldoz.awake.scene.runtime.frame
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.headless.internal.text.text
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.offset
import io.github.ronjunevaldoz.awake.ui.modifier.size
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class SceneAppLifecycleDslTest {

    @Test
    fun sceneGameProvidesTypedSystemsAndCachedAssets() = runTest {
        val recordingRenderer = RecordingRenderer()
        lateinit var tickSystem: io.github.ronjunevaldoz.awake.scene.runtime.SceneSystemHandle<RecordingSystem>
        val game = app {
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
        val runtime = game.requireService<SceneAppLifecycleRuntime>()
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
        val game = app {
            ecs {
                name("before-replace")
                entity("ignored")
                scene("after-replace") {
                    entity("camera", Modifier().camera())
                }
            }
        }

        game.ready(RecordingRenderer())
        val runtime = game.requireService<SceneAppLifecycleRuntime>()

        assertEquals("after-replace", runtime.sceneName)
        assertEquals(null, runtime.findEntity("ignored"))
        assertNotNull(runtime.findEntity("camera"))
    }

    @Test
    fun gameSceneFacadeBuildsNamedCameraAndMeshEntities() = runTest {
        val recordingRenderer = RecordingRenderer()
        val game = app {
            scene("facade-proof") {
                cameraEntity("camera", Modifier().transform(y = 1f, z = 5f))
                meshEntity(
                    "cube",
                    recordingRenderer.createMesh(EmptyGeometry),
                    recordingRenderer.createMaterial(),
                    Modifier().transform(sx = 2f, sy = 2f, sz = 2f),
                )
                assets {
                    mesh("cube") { recordingRenderer.createMesh(EmptyGeometry) }
                    material("default") { recordingRenderer.createMaterial() }
                }
                cameraSystem()
            }
        }

        game.ready(recordingRenderer)
        val runtime = game.requireService<SceneAppLifecycleRuntime>()

        assertEquals("facade-proof", runtime.sceneName)
        assertNotNull(runtime.findEntity("camera"))
        assertNotNull(runtime.findEntity("cube"))
        assertNotNull(runtime.findTransform("cube"))
        assertNotNull(runtime.findCamera("camera"))
        assertTrue(runtime.system("camera") is CameraSystem)
    }

    @Test
    fun gameModuleCanOwnSceneAndUiComposition() = runTest {
        val renderer = RecordingRenderer()
        val module = appModule {
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
                    val scene =
                        requireService<SceneAppLifecycleRuntime>()
                    rootColumn(modifier = Modifier.offset(16f.dp, 16f.dp).size(180f.dp, 120f.dp)) {
                        text(scene.sceneName)
                    }
                }
            }
        }

        val game = app {
            module(module)
        }

        game.ready(renderer)
        game.update(0.016f, 320f, 240f)

        assertEquals("module-scene", game.requireService<SceneAppLifecycleRuntime>().sceneName)
        assertTrue(renderer.lastUiPrimitives.any { primitive -> primitive is UiDrawPrimitive.Glyph })
    }

    @Test
    fun sceneRuntimeStagesUiBeforeInfrastructureUpdate() = runTest {
        val renderer = RecordingRenderer()
        val game = app {
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
        game.update(0.016f, 320f, 240f)

        assertEquals(listOf("drawUi", "draw"), renderer.frameCalls)
    }

    @Test
    fun explicitSystemPhasesSeparateFixedStepsFromRenderedFrames() = runTest {
        lateinit var fixedHandle: io.github.ronjunevaldoz.awake.scene.runtime.SceneSystemHandle<RecordingSystem>
        lateinit var frameHandle: io.github.ronjunevaldoz.awake.scene.runtime.SceneSystemHandle<RecordingSystem>
        val game = app {
            scene("phase-proof") {
                cameraEntity("camera")
                fixedHandle = fixedSystem("fixed") { RecordingSystem() }
                frameHandle = frameSystem("frame") { RecordingSystem() }
            }
        }

        game.ready(RecordingRenderer())
        val runtime = game.requireService<SceneAppLifecycleRuntime>()
        val fixedSystem = runtime.system(fixedHandle)
        val frameSystem = runtime.system(frameHandle)
        fixedSystem.reset()
        frameSystem.reset()

        game.update(0.001f, 320f, 240f)

        assertEquals(0, fixedSystem.calls)
        assertEquals(1, frameSystem.calls)

        game.update(1f / 60f, 320f, 240f)

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

    override val clipSpace: ClipSpace = ClipSpace.WebGpu
    override var clearColor: FloatArray = floatArrayOf(0f, 0f, 0f, 1f)
    override var wireframe: Boolean = false
    override var shadowsEnabled: Boolean = true

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

    override fun createMaterial(
        texture: TextureAsset?,
        renderTarget: RenderTarget?,
        uniformFloatCount: Int,
        pbrTextures: PbrTextureSet?,
    ): Material {
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

    override fun renderToTexture(target: RenderTarget, camera: Camera, drawCalls: List<DrawCall>) =
        Unit

    override suspend fun readPixels(target: RenderTarget): TextureAsset =
        TextureAsset(ByteArray(target.width * target.height * 4), target.width, target.height)

    override fun drawUi(primitives: List<UiDrawPrimitive>, font: UiFont?) {
        frameCalls += "drawUi"
        lastUiPrimitives = primitives
    }

    override fun drawDebugLines(lines: List<LineSegment>) = Unit

    override fun destroy() = Unit
}
