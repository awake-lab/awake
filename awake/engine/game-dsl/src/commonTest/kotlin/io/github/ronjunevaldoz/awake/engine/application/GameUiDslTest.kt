// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.application

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.render.texture.RenderTarget
import io.github.ronjunevaldoz.awake.render.texture.TextureAsset
import io.github.ronjunevaldoz.awake.ui.theme.UiDefaultTheme
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.align
import io.github.ronjunevaldoz.awake.ui.modifier.offset
import io.github.ronjunevaldoz.awake.ui.modifier.size
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.layout.toDimension
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

class GameUiDslTest {

    @Test
    fun uiInstallerRendersOverlayAndResolvesServices() = runTest {
        val renderer = RecordingUiRenderer()
        val game = game {
            install(
                object : GameInstaller {
                    override fun install(into: GameSpecBuilder) {
                        into.service(String::class, "Inspector")
                    }
                }
            )
            ui {
                overlay {
                    val label = requireService<String>()
                    rootColumn(modifier = Modifier.offset(20f.dp, 20f.dp).size(160f.dp, 120f.dp)) {
                        label
                        emit(UiDrawPrimitive.Quad(x = 20f, y = 20f, w = 12f, h = 12f, color = Color(0.2f, 0.7f, 0.4f, 1f)))
                    }
                }
            }
        }

        game.ready(renderer)
        game.render(0.016f, 320f, 240f)

        assertEquals(1, renderer.uiDrawCalls)
        assertTrue(renderer.lastUiPrimitives.any { primitive -> primitive is UiDrawPrimitive.Quad })
    }

    @Test
    fun gameModuleCanOwnUiComposition() = runTest {
        val renderer = RecordingUiRenderer()
        val feature = gameModule {
            install(
                object : GameInstaller {
                    override fun install(into: GameSpecBuilder) {
                        into.service(String::class, "Module Inspector")
                    }
                }
            )
            ui {
                overlay {
                    val label = requireService<String>()
                    rootColumn(modifier = Modifier.offset(20f.dp, 20f.dp).size(180f.dp, 120f.dp)) {
                        label
                        emit(UiDrawPrimitive.Quad(x = 20f, y = 20f, w = 12f, h = 12f, color = Color(0.3f, 0.6f, 0.9f, 1f)))
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
        assertTrue(renderer.lastUiPrimitives.any { primitive -> primitive is UiDrawPrimitive.Quad })
    }

    @Test
    fun gameUiCanDeclareADefaultThemeForTheWholeOverlayRuntime() = runTest {
        val renderer = RecordingUiRenderer()
        var runtime: GameUiRuntime? = null
        val game = game {
            ui {
                theme(TestUiTheme)
                overlay {
                    runtime = this
                    frame {
                        surface(
                            id = "theme-proof",
                            modifier = (Modifier.align(UiAlignment.TopStart).offset(20f.dp, 20f.dp)).width(180f.toDimension()).height(96f.toDimension())) {
                            emit(UiDrawPrimitive.Quad(x = 28f, y = 28f, w = 16f, h = 16f, color = Color(0.9f, 0.9f, 0.2f, 1f)))
                        }
                    }
                }
            }
        }

        game.ready(renderer)
        game.render(0.016f, 320f, 240f)

        assertNotNull(runtime)
        assertEquals(TestUiTheme, runtime.theme)
        assertEquals(1, renderer.uiDrawCalls)
        assertTrue(renderer.lastUiPrimitives.any { primitive -> primitive is UiDrawPrimitive.Quad })
    }

    @Test
    fun nestedSlotColumnInsideRuntimeReceiverUsesCurrentUiScopeOverlayPass() {
        val runtime = GameUiRuntime(
            services = object : GameServiceLookup {
                override fun <T : Any> service(type: kotlin.reflect.KClass<T>): T? = null
            },
            spec = GameUiSpec(
                theme = UiDefaultTheme,
                font = UiFonts.default(),
                overlays = emptyList(),
                onReadyBlock = {},
                onDisposeBlock = {}
            )
        )
        val panelColor = Color(0.15f, 0.32f, 0.62f, 1f)
        val markerColor = Color(0.92f, 0.28f, 0.24f, 1f)

        runtime.uiContext.beginFrame(240f, 160f, UiInputState())
        with(runtime) {
            uiContext.createBox(
                slot = UiBounds(0f, 0f, 240f, 160f),
                contentAlignment = UiAlignment.TopStart,
                overlayOnly = true
            ).surface(
                id = "overlay-panel",
                modifier = (Modifier.align(UiAlignment.TopStart).offset(16f.dp, 20f.dp)).width(160f.toDimension()).height(96f.toDimension()),
                style = Style {
                    background(panelColor)
                }
            ) { panelSlot ->
                column(
                    slot = UiBounds(
                        x = panelSlot.x + 12f,
                        y = panelSlot.y + 12f,
                        width = 32f,
                        height = 16f
                    )
                ) {
                    emit(UiDrawPrimitive.Quad(x = 0f, y = 0f, w = 6f, h = 6f, color = markerColor))
                }
            }
        }

        val primitives = runtime.uiContext.endFrame()
        val panelIndex = primitives.indexOfFirst { primitive ->
            primitive is UiDrawPrimitive.RoundedQuad && primitive.color == panelColor
        }
        val markerIndex = primitives.indexOfFirst { primitive ->
            primitive is UiDrawPrimitive.Quad && primitive.color == markerColor
        }

        assertTrue(panelIndex >= 0, "expected the overlay surface background to render")
        assertTrue(markerIndex >= 0, "expected the nested column marker to render")
        assertTrue(
            markerIndex > panelIndex,
            "nested column(slot = ...) inside a GameUiRuntime receiver must inherit the current UiScope overlay pass instead of falling back to the runtime root receiver"
        )
    }
    @Test
    fun debugOverlayEnabledDrawsThePerfStatsHudAsGlyphPrimitives() = runTest {
        val renderer = RecordingUiRenderer()
        var runtime: GameUiRuntime? = null
        val game = game {
            ui {
                overlay {
                    runtime = this
                }
            }
        }

        game.ready(renderer)
        game.render(0.016f, 320f, 240f)
        assertNotNull(runtime)
        assertTrue(
            renderer.lastUiPrimitives.none { it is UiDrawPrimitive.Glyph },
            "perf HUD must not draw anything while debugOverlayEnabled is off (the default)"
        )

        runtime.debugOverlayEnabled = true
        game.render(0.016f, 320f, 240f)

        assertTrue(
            renderer.lastUiPrimitives.any { it is UiDrawPrimitive.Glyph },
            "perf HUD must draw glyph primitives (its fps/frame-time/cache-stat text) once " +
                "debugOverlayEnabled is on"
        )
    }
}

private object TestUiTheme : UiTheme by UiDefaultTheme

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
