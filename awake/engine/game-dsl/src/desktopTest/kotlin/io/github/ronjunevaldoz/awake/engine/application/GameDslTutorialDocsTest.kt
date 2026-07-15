// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.application

import io.github.ronjunevaldoz.awake.core.math.Camera
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

class GameDslTutorialDocsTest {

    @Test
    fun reusableModuleOwnsTheGameContent() = runTest {
        val feature = gameModule {
            service(String::class, "hello-cube")
        }

        val game = feature.createGame {
            title = "Hello Cube"
            size(1600, 900)
            backend.vulkan()
        }

        assertEquals("Hello Cube", game.windowConfig.title)
        assertEquals(1600, game.windowConfig.width)
        assertEquals(900, game.windowConfig.height)
        assertEquals("hello-cube", game.requireService(String::class))

        recordGameDslTutorial(
            name = "game-dsl-module-shell",
            title = "Game Shell Around A Reusable Module",
            summary = "A sample or game can keep its authored content in a reusable gameModule while the root shell only owns window and platform concerns.",
            snippet = """
                val feature = gameModule {
                    service(String::class, "hello-cube")
                }

                val game = feature.createGame {
                    title = "Hello Cube"
                    size(1600, 900)
                    backend.vulkan()
                }
            """
        )
    }

    @Test
    fun modulesCanComposeOtherModulesAndStillBuildSpecs() = runTest {
        val events = mutableListOf<String>()
        val debugModule = gameModule {
            service(String::class, "debug")
            ready { events += "debug-ready" }
        }
        val hudModule = gameModule {
            service(Int::class, 2)
            ready { events += "hud-ready" }
        }
        val feature = gameModule {
            module(debugModule)
            module(hudModule)
        }

        val spec = feature.createGameSpec {
            title = "Composable"
            size(960, 540)
            backend.webGpu()
        }
        val game = spec.createGame()
        game.ready(TutorialRenderer)

        assertEquals("Composable", spec.windowConfig.title)
        assertEquals("debug", game.requireService(String::class))
        assertEquals(2, game.requireService(Int::class))
        assertEquals(listOf("debug-ready", "hud-ready"), events)

        recordGameDslTutorial(
            name = "game-dsl-composed-modules",
            title = "Modules Compose Into Larger Features",
            summary = "Reusable modules can stack other modules before they are wrapped in a root GameSpec, which keeps authored content split without reintroducing sample-local bootstrap glue.",
            snippet = """
                val debugModule = gameModule {
                    service(String::class, "debug")
                }
                val hudModule = gameModule {
                    service(Int::class, 2)
                }
                val feature = gameModule {
                    module(debugModule)
                    module(hudModule)
                }

                val spec = feature.createGameSpec {
                    title = "Composable"
                    size(960, 540)
                    backend.webGpu()
                }
            """
        )
    }
}

private object TutorialRenderer : Renderer {
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
