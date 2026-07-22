// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.engine.application.ui
import io.github.ronjunevaldoz.awake.engine.application.game
import io.github.ronjunevaldoz.awake.engine.application.requireService
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.render.texture.RenderTarget
import io.github.ronjunevaldoz.awake.render.texture.TextureAsset
import io.github.ronjunevaldoz.awake.scene.runtime.entities.cameraEntity
import io.github.ronjunevaldoz.awake.scene.runtime.entities.meshEntity
import io.github.ronjunevaldoz.awake.engine.application.GameUiRuntime
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.offset
import io.github.ronjunevaldoz.awake.ui.size
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SceneRouterDslTest {

    @Test
    fun routedScenesSwitchThroughCommonRuntime() = runTest {
        val renderer = RouterRecordingRenderer()
        val game = game {
            scenes {
                initial("overview")
                route("overview", label = "Overview") {
                    cameraEntity("camera", camera = { primary(true) })
                    meshEntity("cube", "cube", "default")
                    assets {
                        mesh("cube") { renderer.createMesh(RouterGeometry) }
                        material("default") { renderer.createMaterial() }
                    }
                }
                route("editor", label = "Editor") {
                    cameraEntity("camera", camera = { primary(true) })
                    meshEntity("cube", "cube", "default", transform = { scale(2f, 2f, 2f) })
                    assets {
                        mesh("cube") { renderer.createMesh(RouterGeometry) }
                        material("default") { renderer.createMaterial() }
                    }
                }
            }
        }

        game.ready(renderer)
        game.render(0.016f, 640f, 480f)

        val router = game.requireService<SceneRouterRuntime>()
        assertEquals("overview", router.activeSceneId)
        assertEquals("overview", router.sceneRuntime.sceneName)

        router.switchTo("editor")
        game.render(0.016f, 640f, 480f)

        assertEquals("editor", router.activeSceneId)
        assertEquals("editor", router.sceneRuntime.sceneName)
    }

    @Test
    fun routedScenesComposeWithGameUiInstaller() = runTest {
        val renderer = RouterRecordingRenderer()
        val game = game {
            scenes {
                route("overview") {
                    cameraEntity("camera", camera = { primary(true) })
                }
                route("editor") {
                    cameraEntity("camera", camera = { primary(true) })
                }
            }
            ui {
                overlay {
                    val router = requireService<SceneRouterRuntime>()
                    rootColumn(modifier = UiModifier().offset(16f.dp, 16f.dp).size(180f.dp, 120f.dp)) {
                        text(router.activeSceneLabel)
                    }
                }
            }
        }

        game.ready(renderer)
        game.render(0.016f, 320f, 240f)

        assertEquals("overview", game.requireService<SceneRouterRuntime>().activeSceneLabel)
        assertEquals(GameUiRuntime::class, game.requireService<GameUiRuntime>()::class)
    }
}

private val RouterGeometry = MeshGeometry(floatArrayOf(), intArrayOf())

private class RouterRecordingRenderer : Renderer {
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

    override fun drawUi(primitives: List<UiDrawPrimitive>, font: UiFont?) {
        lastUiPrimitives = primitives
    }

    override fun drawDebugLines(lines: List<LineSegment>) = Unit

    override fun destroy() = Unit
}
