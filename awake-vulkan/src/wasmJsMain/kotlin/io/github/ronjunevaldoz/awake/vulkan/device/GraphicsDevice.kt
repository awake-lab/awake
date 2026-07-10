package io.github.ronjunevaldoz.awake.vulkan.device

// Phase 2.5 (Web/WebGPU, decision D7) milestone 1: compile-only stub, mirroring the exact
// convention Phase 6 used for iOS's first Vulkan.kt pass -- see docs/MVP_PLAN.md.
actual class GraphicsDevice actual constructor() {
    actual var instance: Long = 0
    actual var debugUtilsMessenger: Long = 0
    actual var surface: Long = 0
    actual var physicalDevice: Long = 0
    actual var device: Long = 0
    actual var graphicsQueue: Long = 0
    actual var presentQueue: Long = 0

    actual fun create(window: Any) {
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")
    }

    actual fun destroy() {
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")
    }
}
