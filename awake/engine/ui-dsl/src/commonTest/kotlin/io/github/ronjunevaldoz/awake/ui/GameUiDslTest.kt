// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.engine.application.GameInstaller
import io.github.ronjunevaldoz.awake.engine.application.game
import io.github.ronjunevaldoz.awake.engine.application.gameModule
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
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GameUiDslTest {

    @Test
    fun uiInstallerRendersOverlayAndResolvesServices() = runTest {
        val renderer = RecordingUiRenderer()
        val game = game {
            install(
                object : GameInstaller {
                    override fun install(into: io.github.ronjunevaldoz.awake.engine.application.GameSpecBuilder) {
                        into.service(String::class, "Inspector")
                    }
                }
            )
            ui {
                overlay { _, _ ->
                    column(x = 20f, y = 20f, width = 160f) {
                        text(requireService<String>())
                    }
                }
            }
        }

        game.ready(renderer)
        game.render(0.016f, 320f, 240f)

        assertEquals(1, renderer.uiDrawCalls)
        assertTrue(renderer.lastUiPrimitives.any { primitive -> primitive is UiDrawPrimitive.Glyph })
    }

    @Test
    fun gameModuleCanOwnUiComposition() = runTest {
        val renderer = RecordingUiRenderer()
        val feature = gameModule {
            install(
                object : GameInstaller {
                    override fun install(into: io.github.ronjunevaldoz.awake.engine.application.GameSpecBuilder) {
                        into.service(String::class, "Module Inspector")
                    }
                }
            )
            ui {
                overlay { _, _ ->
                    column(x = 20f, y = 20f, width = 180f) {
                        text(requireService<String>())
                    }
                }
            }
        }

        val game = game {
            module(feature)
        }

        game.ready(renderer)
        game.render(0.016f, 320f, 240f)

        assertEquals(1, renderer.uiDrawCalls)
        assertTrue(renderer.lastUiPrimitives.any { primitive -> primitive is UiDrawPrimitive.Glyph })
    }

    @Test
    fun gameUiCanDeclareADefaultThemeForTheWholeOverlayRuntime() = runTest {
        val renderer = RecordingUiRenderer()
        var runtime: GameUiRuntime? = null
        val game = game {
            ui {
                theme(TestUiTheme)
                overlay { _, _ ->
                    runtime = this
                    shellPane(
                        slot = UiSlot(20f, 20f, 180f, 96f),
                        id = "theme-proof"
                    ) {
                        text("Themed")
                    }
                }
            }
        }

        game.ready(renderer)
        game.render(0.016f, 320f, 240f)

        assertNotNull(runtime)
        assertEquals(TestUiTheme, runtime.theme)
        assertEquals(1, renderer.uiDrawCalls)
        assertTrue(renderer.lastUiPrimitives.any { primitive -> primitive is UiDrawPrimitive.Glyph })
    }
}

private object TestUiTheme : UiTheme by CoreUiTheme

private class RecordingUiRenderer : Renderer {
    var uiDrawCalls = 0
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
        uiDrawCalls += 1
        lastUiPrimitives = primitives
    }

    override fun drawDebugLines(lines: List<LineSegment>) = Unit

    override fun destroy() = Unit
}
