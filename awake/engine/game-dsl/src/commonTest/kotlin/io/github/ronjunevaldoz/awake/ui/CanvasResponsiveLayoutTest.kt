// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.math.ClipSpace
import io.github.ronjunevaldoz.awake.engine.application.GameServiceLookup
import io.github.ronjunevaldoz.awake.engine.application.GameUiRuntime
import io.github.ronjunevaldoz.awake.engine.application.frame
import io.github.ronjunevaldoz.awake.engine.application.gameUi
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.align
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.padding
import io.github.ronjunevaldoz.awake.ui.layout.toDimension
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.headless.input.text.text
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import io.github.ronjunevaldoz.awake.ui.context.UiFrameInput
import io.github.ronjunevaldoz.awake.ui.layout.*

private object TestDummyRenderer : io.github.ronjunevaldoz.awake.render.renderer.Renderer {
    override val clipSpace: ClipSpace = ClipSpace.WebGpu
    override var clearColor: FloatArray = floatArrayOf(0f, 0f, 0f, 1f)
    override var shadowsEnabled: Boolean = true
    override fun createMesh(geometry: io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry): io.github.ronjunevaldoz.awake.render.mesh.Mesh = object : io.github.ronjunevaldoz.awake.render.mesh.Mesh {
        override val format: io.github.ronjunevaldoz.awake.render.mesh.VertexFormat = geometry.format
        override fun bind(commandBuffer: Long) = Unit
        override fun draw(commandBuffer: Long) = Unit
        override fun destroy() = Unit
    }
    override fun createMaterial(texture: io.github.ronjunevaldoz.awake.render.texture.TextureAsset?, renderTarget: io.github.ronjunevaldoz.awake.render.texture.RenderTarget?, uniformFloatCount: Int): io.github.ronjunevaldoz.awake.render.material.Material = object : io.github.ronjunevaldoz.awake.render.material.Material {
        override fun updateUniformBuffer(mvp: FloatArray) = Unit
        override fun bind(commandBuffer: Long, pipelineLayout: Long) = Unit
        override fun destroy() = Unit
    }
    override fun createRenderTarget(width: Int, height: Int): io.github.ronjunevaldoz.awake.render.texture.RenderTarget = object : io.github.ronjunevaldoz.awake.render.texture.RenderTarget {
        override val width: Int = width
        override val height: Int = height
        override fun destroy() = Unit
    }
    override fun draw(camera: io.github.ronjunevaldoz.awake.core.math.Camera, drawCalls: List<io.github.ronjunevaldoz.awake.render.renderer.DrawCall>, light: io.github.ronjunevaldoz.awake.render.renderer.SceneLight) = Unit
    override fun renderToTexture(target: io.github.ronjunevaldoz.awake.render.texture.RenderTarget, camera: io.github.ronjunevaldoz.awake.core.math.Camera, drawCalls: List<io.github.ronjunevaldoz.awake.render.renderer.DrawCall>) = Unit
    override suspend fun readPixels(target: io.github.ronjunevaldoz.awake.render.texture.RenderTarget): io.github.ronjunevaldoz.awake.render.texture.TextureAsset = io.github.ronjunevaldoz.awake.render.texture.TextureAsset(ByteArray(0), 0, 0)
    override fun drawUi(primitives: List<UiDrawPrimitive>, font: io.github.ronjunevaldoz.awake.ui.font.UiFont?) = Unit
    override fun drawDebugLines(lines: List<io.github.ronjunevaldoz.awake.render.renderer.LineSegment>) = Unit
    override fun destroy() = Unit
}

private val testInput = io.github.ronjunevaldoz.awake.core.input.Input()

private fun unusedGameServices() = object : GameServiceLookup {
    override fun <T : Any> service(type: kotlin.reflect.KClass<T>): T? = null
    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> requireService(type: kotlin.reflect.KClass<T>): T = when (type) {
        io.github.ronjunevaldoz.awake.render.renderer.Renderer::class -> TestDummyRenderer as T
        io.github.ronjunevaldoz.awake.core.input.Input::class -> testInput as T
        else -> error("unused: $type")
    }
}

class CanvasResponsiveLayoutTest {

    @Test
    fun canvasExposesResponsiveWidthClassesAndAlignment() = runTest {
        UiDensity.scale = 1f
        var widthClass: UiWidthSizeClass? = null
        var panelSlot: UiBounds? = null

        val runtime = GameUiRuntime(
            services = unusedGameServices(),
            spec = gameUi {
                overlay {
                    frame { constraints ->
                        widthClass = constraints.widthSizeClass
                        panelSlot = surface(
                            id = "overlay-panel",
                            modifier = (Modifier
                                .align(UiAlignment.BottomEnd)
                                .padding(start = 0f.dp, top = 0f.dp, end = 16f.dp, bottom = 12f.dp)).width(120f.toDimension()).height(
                                    Dimension.WrapContent)) {
                            text("Status")
                        }
                    }
                }
            }
        )
        runtime.ready(TestDummyRenderer)
        runtime.render(0.016f, 360f, 240f)

        assertEquals(UiWidthSizeClass.Compact, widthClass)
        assertEquals(UiBounds(224f, 196f, 120f, 32f), panelSlot)
    }

    @Test
    fun canvasSupportsStackedResponsiveColumns() = runTest {
        UiDensity.scale = 1f
        var widthClass: UiWidthSizeClass? = null
        var columnSlot: UiBounds? = null

        val runtime = GameUiRuntime(
            services = unusedGameServices(),
            spec = gameUi {
                overlay {
                    frame { constraints ->
                        widthClass = constraints.widthSizeClass
                        columnSlot = column(
                            id = "stacked-column",
                            modifier = (Modifier
                                .align(UiAlignment.TopStart)
                                .padding(20f.dp)).width(320f.toDimension()).height(Dimension.WrapContent)) {
                            surface(id = "one", modifier = Modifier.width(Dimension.FillMax).height(Dimension.WrapContent)) {
                                text("One")
                            }
                            surface(id = "two", modifier = Modifier.width(Dimension.FillMax).height(Dimension.WrapContent)) {
                                text("Two")
                            }
                        }
                    }
                }
            }
        )
        runtime.ready(TestDummyRenderer)
        runtime.render(0.016f, 900f, 600f)

        assertEquals(UiWidthSizeClass.Expanded, widthClass)
        assertEquals(UiBounds(20f, 20f, 320f, 112f), columnSlot)
    }

    @Test
    fun canvasUsesDensityIndependentWidthClasses() = runTest {
        val originalScale = UiDensity.scale
        UiDensity.scale = 2f
        try {
            var widthClass: UiWidthSizeClass? = null
            var maxWidth: Float? = null
            var maxWidthPx: Float? = null

            val runtime = GameUiRuntime(
                services = unusedGameServices(),
                spec = gameUi {
                    overlay {
                        frame { constraints ->
                            widthClass = constraints.widthSizeClass
                            maxWidth = constraints.maxWidth
                            maxWidthPx = constraints.maxWidthPx
                        }
                    }
                }
            )
            runtime.ready(TestDummyRenderer)
            runtime.render(0.016f, 900f, 600f)

            assertEquals(UiWidthSizeClass.Compact, widthClass)
            assertEquals(450f, maxWidth)
            assertEquals(900f, maxWidthPx)
        } finally {
            UiDensity.scale = originalScale
        }
    }
}
