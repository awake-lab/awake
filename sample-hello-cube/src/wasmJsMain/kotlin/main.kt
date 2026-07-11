// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
import io.ygdrasil.webgpu.CompositeAlphaMode
import io.ygdrasil.webgpu.GPUTextureUsage
import io.ygdrasil.webgpu.SurfaceConfiguration
import io.ygdrasil.webgpu.canvasContextRenderer
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import web.dom.ElementId
import web.dom.document
import web.html.HTMLCanvasElement

/**
 * Bare-canvas wasmJs entry point -- same pattern as `awake-demo/shared/src/wasmJsMain/kotlin/
 * main.kt`, minus input wiring (this sample's cube is static). Resolves the WebGPU context
 * (a `suspend` operation) once, then drives [WebGpuSampleApplication] with a plain
 * `requestAnimationFrame` loop.
 */
fun main() {
    val canvas = document.getElementById(ElementId("awake-canvas")) as HTMLCanvasElement
    MainScope().launch {
        val canvasContext = canvasContextRenderer(htmlCanvas = canvas)
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

        val application = WebGpuSampleApplication()
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
