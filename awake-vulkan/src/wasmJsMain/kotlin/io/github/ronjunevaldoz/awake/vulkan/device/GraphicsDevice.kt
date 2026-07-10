package io.github.ronjunevaldoz.awake.vulkan.device

import io.github.ronjunevaldoz.awake.vulkan.webgpu.WebGpuHandles
import io.ygdrasil.webgpu.WGPUContext

/**
 * Phase 2.5 milestone 2 slice 1 (see docs/MVP_PLAN.md): real wgpu4k implementation.
 *
 * Unlike the other three platforms, [create]'s [window] parameter here is a pre-resolved
 * [WGPUContext], not a raw canvas -- `canvasContextRenderer()`/`Surface.configure()` are
 * `suspend`, and `kotlinx.coroutines.runBlocking` does not exist on `wasmJs` at all (a
 * single-threaded JS event loop can't block), confirmed the hard way (the import itself
 * doesn't resolve). `create()` on the `expect` contract is not `suspend`, so the async
 * device/surface acquisition has to happen *before* this is called -- the caller resolves
 * a `WGPUContext` via `canvasContextRenderer()` + `surface.configure()` in its own
 * coroutine, then passes it here. This is the same "[window] means something different per
 * platform" pattern already in use (GLFW `Long` on desktop, `android.view.Surface` on
 * Android) -- `Any` was deliberately loose for exactly this kind of flexibility.
 *
 * [wgpuContext] is `internal`, not part of the `expect` contract -- only other wasmJsMain
 * actuals (RenderPipeline/Mesh/Renderer, all in this same source set) reach into it
 * directly, via [WebGpuHandles] for the individual object handles their own Long-typed
 * fields need.
 */
actual class GraphicsDevice actual constructor() {
    actual var instance: Long = 0
    actual var debugUtilsMessenger: Long = 0
    actual var surface: Long = 0
    actual var physicalDevice: Long = 0
    actual var device: Long = 0
    actual var graphicsQueue: Long = 0
    actual var presentQueue: Long = 0

    internal lateinit var wgpuContext: WGPUContext
        private set

    actual fun create(window: Any) {
        wgpuContext = window as? WGPUContext
            ?: error(
                "GraphicsDevice.create expects a pre-resolved WGPUContext on wasmJs " +
                    "(canvasContextRenderer() + surface.configure() must run in the " +
                    "caller's own coroutine first -- see this class's doc comment), got $window"
            )
        device = WebGpuHandles.register(wgpuContext.device)
    }

    actual fun destroy() {
        WebGpuHandles.release(device)
        wgpuContext.close()
    }
}
