// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.hellocube.app

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
 * Bare-canvas wasmJs entry point -- same pattern as `awake-demo/shared/src/wasmJsMain/kotlin/
 * main.kt`, minus input wiring (this sample's cube is static). Resolves the WebGPU context
 * (a `suspend` operation) once, then drives a plain [WebGpuGameApplication] injected with
 * [helloCubeGame] via a `requestAnimationFrame` loop.
 */
fun main() {
    val canvas = document.getElementById(ElementId("awake-canvas")) as HTMLCanvasElement
    // Feeds the custom UI overlay's hit-testing (UiContext.button/toggle poll
    // Input.pointerX/Y/Down) -- see docs/MVP_PLAN.md's custom-UI decision log entry. Desktop
    // already polls GLFW mouse state, Android/iOS already feed Input.setPointer from touch;
    // this is the only platform with no existing pointer glue.
    //
    // Scaled by devicePixelRatio: `offsetX`/`offsetY` are CSS pixels, but the UI overlay's
    // screenSize uniform (UiRenderPipeline) is set from the WebGPU canvas's backing-buffer
    // size, i.e. physical pixels (CSS size * devicePixelRatio on any HiDPI/Retina display) --
    // scaling here keeps Input.pointerX/Y in the same physical-pixel space the shader's NDC
    // conversion already assumes, so widget hit-testing lines up with what's actually drawn.
    window.addEventListener("mousemove") { event ->
        val mouseEvent = event as MouseEvent
        Input.setPointer(
            Input.pointerDown,
            (mouseEvent.offsetX * window.devicePixelRatio).toFloat(),
            (mouseEvent.offsetY * window.devicePixelRatio).toFloat()
        )
    }
    window.addEventListener("mousedown") { event ->
        val mouseEvent = event as MouseEvent
        Input.setPointer(
            true,
            (mouseEvent.offsetX * window.devicePixelRatio).toFloat(),
            (mouseEvent.offsetY * window.devicePixelRatio).toFloat()
        )
    }
    window.addEventListener("mouseup") { event ->
        val mouseEvent = event as MouseEvent
        Input.setPointer(
            false,
            (mouseEvent.offsetX * window.devicePixelRatio).toFloat(),
            (mouseEvent.offsetY * window.devicePixelRatio).toFloat()
        )
    }
    // A <canvas>'s backing-buffer size (.width/.height) never follows its CSS/client size
    // automatically -- unlike most elements, it has to be set explicitly, or WebGPU configures
    // the swapchain at whatever the attributes' default value is (300x150, the HTML spec's
    // own canvas default, if index.html never sets width="…" height="…"). Passed as
    // `canvasContextRenderer`'s own `width`/`height` params below, NOT set directly on
    // `canvas.width`/`height` first -- `canvasContextRenderer` unconditionally re-reads
    // `canvas.clientWidth`/`clientHeight` itself when those params are omitted, silently
    // overwriting whatever the caller set beforehand (confirmed by reading wgpu4k-toolkit's
    // own `Surface.kt` source). `.coerceAtLeast(1)` guards against a genuinely zero-sized
    // window (WebGPU rejects a 0x0 surface configuration outright) -- window size should
    // never actually be 0 in a real browser tab, this is a defensive floor, not the expected
    // path. There's no swapchain-recreation path here (see vulkan-tutorial.com's
    // swapchain-recreation chapter for the canonical pattern this project's Vulkan/WebGPU
    // backends don't yet implement), so this is also the ONLY size the canvas will ever
    // render at for this session.
    fun currentCanvasSize(): Pair<Int, Int> {
        val width = (window.innerWidth * window.devicePixelRatio).toInt().coerceAtLeast(1)
        val height = (window.innerHeight * window.devicePixelRatio).toInt().coerceAtLeast(1)
        return width to height
    }
    val (canvasWidth, canvasHeight) = currentCanvasSize()
    // Without this, canvas.width/height (the actual GPU backing-buffer size) stay fixed at
    // whatever they were on first paint forever -- CSS (index.html's `width:100vw;
    // height:100vh`) DOES react to a window resize, but the backing buffer doesn't unless
    // something explicitly updates it. That mismatch is exactly what makes the cube look
    // "not square": the browser stretches the (correctly-proportioned, fixed-aspect) rendered
    // image to fill the CSS box, whose aspect ratio has since changed. `Renderer.draw()`
    // already re-reads `renderingContext.width`/`height` (== `canvas.width`/`height`) live
    // every frame for its projection aspect, and WebGPU auto-resizes the presentation
    // texture to match the canvas's current pixel size on the next `getCurrentTexture()` --
    // updating `canvas.width`/`height` here is the only piece actually missing.
    window.addEventListener("resize") {
        val (width, height) = currentCanvasSize()
        canvas.width = width
        canvas.height = height
    }
    MainScope().launch {
        val canvasContext = canvasContextRenderer(htmlCanvas = canvas, width = canvasWidth, height = canvasHeight)
        val wgpuContext = canvasContext.wgpuContext
        // canvasContextRenderer() never calls Surface.configure() itself -- see
        // awake-demo's main.kt for the full "getCurrentTexture() throws" story.
        wgpuContext.surface.configure(
            SurfaceConfiguration(
                device = wgpuContext.device,
                format = wgpuContext.renderingContext.textureFormat,
                usage = GPUTextureUsage.RenderAttachment or GPUTextureUsage.CopySrc,
                alphaMode = CompositeAlphaMode.Opaque
            )
        )

        val application = createHelloCubeWebGpuApplication()
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
