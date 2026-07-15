// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.application

import io.github.ronjunevaldoz.awake.core.input.Input
import io.ygdrasil.webgpu.CompositeAlphaMode
import io.ygdrasil.webgpu.GPUTextureUsage
import io.ygdrasil.webgpu.SurfaceConfiguration
import io.ygdrasil.webgpu.canvasContextRenderer
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.w3c.dom.events.MouseEvent
import web.dom.ElementId
import web.dom.document
import web.html.HTMLCanvasElement

/**
 * Reusable WebGPU canvas host for authored games.
 *
 * It resolves the canvas + WebGPU context, keeps the backing buffer in sync with browser
 * size, feeds pointer input into Awake's UI runtime, and drives the requestAnimationFrame
 * loop. Consumers only provide the authored [WebGpuGameApplication] instance.
 */
fun launchWebGpuGame(
    canvasId: String = "awake-canvas",
    applicationFactory: () -> WebGpuGameApplication
) {
    val canvas = document.getElementById(ElementId(canvasId)) as? HTMLCanvasElement
        ?: error("No canvas found with id '$canvasId'.")
    launchWebGpuGame(canvas, applicationFactory)
}

fun launchWebGpuGame(
    canvas: HTMLCanvasElement,
    applicationFactory: () -> WebGpuGameApplication
) {
    bindWindowPointerInput()
    val initialSize = currentCanvasSize()
    syncCanvasSize(canvas, initialSize.first, initialSize.second)
    window.addEventListener("resize") {
        val (width, height) = currentCanvasSize()
        syncCanvasSize(canvas, width, height)
    }

    MainScope().launch {
        val canvasContext = canvasContextRenderer(
            htmlCanvas = canvas,
            width = initialSize.first,
            height = initialSize.second
        )
        val wgpuContext = canvasContext.wgpuContext
        wgpuContext.surface.configure(
            SurfaceConfiguration(
                device = wgpuContext.device,
                format = wgpuContext.renderingContext.textureFormat,
                usage = GPUTextureUsage.RenderAttachment or GPUTextureUsage.CopySrc,
                alphaMode = CompositeAlphaMode.Opaque
            )
        )

        val application = applicationFactory()
        application.create(wgpuContext)

        var lastFrameTime = window.performance.now()
        fun frame(time: Double) {
            val deltaSeconds = ((time - lastFrameTime) / 1000.0).toFloat()
            lastFrameTime = time
            application.update(deltaSeconds)
            window.requestAnimationFrame(::frame)
        }
        window.requestAnimationFrame(::frame)
    }
}

private fun bindWindowPointerInput() {
    fun scaledPointer(event: MouseEvent): Pair<Float, Float> = Pair(
        (event.offsetX * window.devicePixelRatio).toFloat(),
        (event.offsetY * window.devicePixelRatio).toFloat()
    )

    window.addEventListener("mousemove") { event ->
        val (x, y) = scaledPointer(event as MouseEvent)
        Input.setPointer(Input.pointerDown, x, y)
    }
    window.addEventListener("mousedown") { event ->
        val (x, y) = scaledPointer(event as MouseEvent)
        Input.setPointer(true, x, y)
    }
    window.addEventListener("mouseup") { event ->
        val (x, y) = scaledPointer(event as MouseEvent)
        Input.setPointer(false, x, y)
    }
}

private fun currentCanvasSize(): Pair<Int, Int> {
    val width = (window.innerWidth * window.devicePixelRatio).toInt().coerceAtLeast(1)
    val height = (window.innerHeight * window.devicePixelRatio).toInt().coerceAtLeast(1)
    return width to height
}

private fun syncCanvasSize(canvas: HTMLCanvasElement, width: Int, height: Int) {
    canvas.width = width
    canvas.height = height
}
