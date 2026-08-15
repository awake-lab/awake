// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.studio.state.StudioStore
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.context.UiMeasureTrialStats
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnThemeValues
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.createUiScope
import kotlin.test.Test
import kotlin.time.measureTime

/** Throwaway probe for the reported UI performance drop: real shell composition, steady-state
 * frame cost + trial-measure count + text-layout cache behavior. Numbers print to stdout. */
class StudioFramePerfProbeTest {

    @Test
    fun measureSteadyStateFrameCost() {
        val ui = UiContext()
        val store = StudioStore()
        val world = World()
        val renderer = PerfNoopRenderer()
        ui.pushFont(BitmapFont())
        ui.pushTheme(shadcnThemeValues(dark = true))

        fun frame() {
            ui.beginFrame(1440f, 900f, UiInputState(pointerX = 400f, pointerY = 300f))
            ui.createUiScope(slot = UiBounds(0f, 0f, 1440f, 900f))
                .drawStudioShellBody(store, world, renderer)
            ui.finishFrame()
        }

        repeat(60) { frame() } // warmup

        UiMeasureTrialStats.enabled = true
        UiMeasureTrialStats.reset()
        val frames = 300
        val elapsed = measureTime { repeat(frames) { frame() } }
        val trialCount = UiMeasureTrialStats.trialCount
        UiMeasureTrialStats.enabled = false

        val msPerFrame = elapsed.inWholeMicroseconds / 1000.0 / frames
        println(
            "PERF studio-shell msPerFrame=$msPerFrame trialsPerFrame=${trialCount / frames}",
        )
    }
}

private class PerfNoopRenderer : io.github.ronjunevaldoz.awake.render.renderer.Renderer {
    override val clipSpace = io.github.ronjunevaldoz.awake.core.math.ClipSpace.WebGpu
    override var clearColor: FloatArray = floatArrayOf(0f, 0f, 0f, 1f)
    override var wireframe: Boolean = false
    override var shadowsEnabled: Boolean = true
    override fun createMesh(geometry: io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry) =
        object : io.github.ronjunevaldoz.awake.render.mesh.Mesh {
            override val format = geometry.format
            override fun bind(commandBuffer: Long) = Unit
            override fun draw(commandBuffer: Long) = Unit
            override fun destroy() = Unit
        }
    override fun createMaterial(
        texture: io.github.ronjunevaldoz.awake.render.texture.TextureAsset?,
        renderTarget: io.github.ronjunevaldoz.awake.render.texture.RenderTarget?,
        uniformFloatCount: Int,
    ) = object : io.github.ronjunevaldoz.awake.render.material.Material {
        override fun updateUniformBuffer(mvp: FloatArray) = Unit
        override fun bind(commandBuffer: Long, pipelineLayout: Long) = Unit
        override fun destroy() = Unit
    }
    override fun createRenderTarget(width: Int, height: Int) =
        object : io.github.ronjunevaldoz.awake.render.texture.RenderTarget {
            override val width: Int = width
            override val height: Int = height
            override fun destroy() = Unit
        }
    override fun draw(
        camera: io.github.ronjunevaldoz.awake.core.math.Camera,
        drawCalls: List<io.github.ronjunevaldoz.awake.render.renderer.DrawCall>,
        light: io.github.ronjunevaldoz.awake.render.renderer.SceneLight,
    ) = Unit
    override fun renderToTexture(
        target: io.github.ronjunevaldoz.awake.render.texture.RenderTarget,
        camera: io.github.ronjunevaldoz.awake.core.math.Camera,
        drawCalls: List<io.github.ronjunevaldoz.awake.render.renderer.DrawCall>,
    ) = Unit
    override suspend fun readPixels(target: io.github.ronjunevaldoz.awake.render.texture.RenderTarget) =
        io.github.ronjunevaldoz.awake.render.texture.TextureAsset(ByteArray(0), 0, 0)
    override fun drawUi(primitives: List<io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive>, font: io.github.ronjunevaldoz.awake.ui.font.UiFont?) = Unit
    override fun drawDebugLines(lines: List<io.github.ronjunevaldoz.awake.render.renderer.LineSegment>) = Unit
    override fun destroy() = Unit
}
