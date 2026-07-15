// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.engine.application.game
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
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame

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

    override fun drawUi(primitives: List<UiDrawPrimitive>, font: BitmapFont?) = Unit

    override fun drawDebugLines(lines: List<LineSegment>) = Unit

    override fun destroy() = Unit
}
