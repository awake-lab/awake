/*
 * Awake
 * Awake.awake-demo.shared.wasmJsMain
 *
 * Copyright (c) ronjunevaldoz 2023.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import demo.WebGpuApplication
import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.input.Key
import io.ygdrasil.webgpu.CompositeAlphaMode
import io.ygdrasil.webgpu.GPUTextureUsage
import io.ygdrasil.webgpu.SurfaceConfiguration
import io.ygdrasil.webgpu.canvasContextRenderer
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.w3c.dom.events.KeyboardEvent
import web.dom.ElementId
import web.dom.document
import web.html.HTMLCanvasElement

/**
 * Web demo (see docs/MVP_PLAN.md's decision log): a bare-canvas entry point that does not
 * go through App()/DemoScene()/AwakeCanvas -- Compose Multiplatform's wasmJs target has no
 * first-class API to embed a native canvas inside the Compose layout tree. Resolves the
 * WebGPU context (an inherently `suspend` operation, per [io.github.ronjunevaldoz.awake
 * .webgpu.device.GraphicsDevice]'s own doc comment) once, then drives [WebGpuApplication]
 * with a plain `requestAnimationFrame` loop -- the same "platform loop calls update(delta)
 * every frame" contract [io.github.ronjunevaldoz.awake.core.application.FixedTimestepLoop]
 * expects on every other platform.
 */
fun main() {
    val canvas = document.getElementById(ElementId("awake-canvas")) as HTMLCanvasElement
    // MVP1a (see docs/MMORPG_ROADMAP.md): the only platform with zero existing input glue --
    // desktop already polls GLFW keys, Android/iOS already feed Input.setPointer from touch.
    window.addEventListener("keydown") { event ->
        mapKey(event as KeyboardEvent)?.let { Input.setKeyDown(it, true) }
    }
    window.addEventListener("keyup") { event ->
        mapKey(event as KeyboardEvent)?.let { Input.setKeyDown(it, false) }
    }
    MainScope().launch {
        val canvasContext = canvasContextRenderer(htmlCanvas = canvas)
        val wgpuContext = canvasContext.wgpuContext
        // canvasContextRenderer() never calls Surface.configure() itself -- confirmed the
        // hard way during the wgpu4k spike (getCurrentTexture() throws "context is not
        // configured" without this).
        wgpuContext.surface.configure(
            SurfaceConfiguration(
                device = wgpuContext.device,
                format = wgpuContext.renderingContext.textureFormat,
                usage = GPUTextureUsage.RenderAttachment or GPUTextureUsage.CopySrc,
                alphaMode = CompositeAlphaMode.Opaque
            )
        )

        val application = WebGpuApplication()
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

private fun mapKey(event: KeyboardEvent): Key? = when (event.code) {
    "KeyW" -> Key.W
    "KeyA" -> Key.A
    "KeyS" -> Key.S
    "KeyD" -> Key.D
    "ArrowUp" -> Key.ArrowUp
    "ArrowDown" -> Key.ArrowDown
    "ArrowLeft" -> Key.ArrowLeft
    "ArrowRight" -> Key.ArrowRight
    "Space" -> Key.Space
    "Escape" -> Key.Escape
    else -> null
}
