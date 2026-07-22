// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.application

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.ui.UiDensity
import io.ygdrasil.webgpu.CompositeAlphaMode
import io.ygdrasil.webgpu.GPUTextureFormat
import io.ygdrasil.webgpu.GPUTextureUsage
import io.ygdrasil.webgpu.SurfaceConfiguration
import io.ygdrasil.webgpu.WGPUContext
import io.ygdrasil.webgpu.canvasContextRenderer
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import web.dom.ElementId
import web.dom.document
import web.html.HTMLCanvasElement

/**
 * Reusable WebGPU canvas host for authored games.
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
    syncUiDensityFromWindow()
    val resolvedApplication = applicationFactory()
    val input = resolvedApplication.input
    
    bindWindowPointerInput(input)
    bindWindowKeyboardInput(input)
    
    val initialSize = currentCanvasSize()
    syncCanvasSize(canvas, initialSize.first, initialSize.second)
    var wgpuContext: WGPUContext? = null
    var application: WebGpuGameApplication? = resolvedApplication
    
    window.addEventListener("resize") {
        val (width, height) = currentCanvasSize()
        syncCanvasSize(canvas, width, height)
        wgpuContext?.let(::configureSurface)
        application?.resize(x = 0, y = 0, width = width, height = height)
    }

    MainScope().launch {
        val canvasContext = canvasContextRenderer(
            htmlCanvas = canvas,
            width = initialSize.first,
            height = initialSize.second
        )
        val resolvedContext = canvasContext.wgpuContext
        wgpuContext = resolvedContext
        configureSurface(resolvedContext)

        resolvedApplication.resize(x = 0, y = 0, width = initialSize.first, height = initialSize.second)
        resolvedApplication.create(resolvedContext)

        var lastFrameTime = window.performance.now()
        fun frame(time: Double) {
            syncUiDensityFromWindow()
            val deltaSeconds = ((time - lastFrameTime) / 1000.0).toFloat()
            lastFrameTime = time
            resolvedApplication.update(deltaSeconds)
            window.requestAnimationFrame(::frame)
        }
        window.requestAnimationFrame(::frame)
    }
}

val DefaultDomGameplayKeys: Map<String, io.github.ronjunevaldoz.awake.core.input.Key> = linkedMapOf(
    "w" to io.github.ronjunevaldoz.awake.core.input.Key.W,
    "a" to io.github.ronjunevaldoz.awake.core.input.Key.A,
    "s" to io.github.ronjunevaldoz.awake.core.input.Key.S,
    "d" to io.github.ronjunevaldoz.awake.core.input.Key.D,
    "arrowup" to io.github.ronjunevaldoz.awake.core.input.Key.ArrowUp,
    "arrowdown" to io.github.ronjunevaldoz.awake.core.input.Key.ArrowDown,
    "arrowleft" to io.github.ronjunevaldoz.awake.core.input.Key.ArrowLeft,
    "arrowright" to io.github.ronjunevaldoz.awake.core.input.Key.ArrowRight,
    " " to io.github.ronjunevaldoz.awake.core.input.Key.Space,
    "spacebar" to io.github.ronjunevaldoz.awake.core.input.Key.Space,
    "escape" to io.github.ronjunevaldoz.awake.core.input.Key.Escape
)

fun bindWindowPointerInput(input: Input) {
    fun scaledPointer(event: MouseEvent): Pair<Float, Float> {
        val density = currentWindowDensity()
        return Pair(
            (event.offsetX * density).toFloat(),
            (event.offsetY * density).toFloat()
        )
    }

    window.addEventListener("mousemove") { event ->
        val (x, y) = scaledPointer(event as MouseEvent)
        input.setPointer(input.pointerDown, x, y)
    }
    window.addEventListener("mousedown") { event ->
        val (x, y) = scaledPointer(event as MouseEvent)
        input.setPointer(true, x, y)
    }
    window.addEventListener("mouseup") { event ->
        val (x, y) = scaledPointer(event as MouseEvent)
        input.setPointer(false, x, y)
    }
}

fun bindWindowKeyboardInput(
    input: Input,
    keys: Map<String, io.github.ronjunevaldoz.awake.core.input.Key> = DefaultDomGameplayKeys
) {
    fun resolveKey(event: KeyboardEvent): io.github.ronjunevaldoz.awake.core.input.Key? {
        return keys[event.key.lowercase()]
    }

    window.addEventListener("keydown") { event ->
        val key = resolveKey(event as KeyboardEvent) ?: return@addEventListener
        input.setKeyDown(key, true)
    }
    window.addEventListener("keyup") { event ->
        val key = resolveKey(event as KeyboardEvent) ?: return@addEventListener
        input.setKeyDown(key, false)
    }
    window.addEventListener("blur") {
        input.clearKeys()
    }
}

private fun currentCanvasSize(): Pair<Int, Int> {
    val density = currentWindowDensity()
    val width = (window.innerWidth * density).toInt().coerceAtLeast(1)
    val height = (window.innerHeight * density).toInt().coerceAtLeast(1)
    return width to height
}

private fun currentWindowDensity(): Double {
    val scale = window.devicePixelRatio
    return if (scale.isFinite() && scale > 0.0) scale else 1.0
}


private fun syncUiDensityFromWindow() {
    UiDensity.scale = currentWindowDensity().toFloat()
}

private fun syncCanvasSize(canvas: HTMLCanvasElement, width: Int, height: Int) {
    canvas.width = width
    canvas.height = height
}

private fun configureSurface(wgpuContext: WGPUContext) {
    wgpuContext.surface.configure(
        SurfaceConfiguration(
            device = wgpuContext.device,
            format = GPUTextureFormat.RGBA8Unorm,
            usage = GPUTextureUsage.RenderAttachment or GPUTextureUsage.CopySrc,
            alphaMode = CompositeAlphaMode.Opaque
        )
    )
}
