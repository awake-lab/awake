// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.webgpu.application.WebGpuGameApplication
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
 * [SampleGame] via a `requestAnimationFrame` loop.
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

        val application = WebGpuGameApplication(
            vertexShaderResourcePath = "assets/shader/webgpu/triangle.wgsl",
            fragmentShaderResourcePath = "assets/shader/webgpu/triangle.wgsl",
            vertexStride = sampleVertexStride,
            game = SampleGame()
        )
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
