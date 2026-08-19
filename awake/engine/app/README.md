# Awake Engine App

Platform application entrypoints and host windowing shells for [Awake](../../README.md). Boots and
hosts `GameApplication` instances across Desktop (GLFW/JVM), Android (Vulkan Activity), iOS (UIKit),
and WasmJs (Browser Canvas).

## Installation

```kotlin
implementation(project(":awake:engine:app"))
```

## Key Primitives

- `AwakeApplication` — platform-specific application runner launching the target runtime loop and
  managing window surface creation.

## Target Implementations

- **Desktop (`vulkanMain` / JVM)**: Window creation via GLFW and Vulkan surface presentation.
- **Android (`androidMain`)**: Native `VulkanView` embedded in Android Activity lifecycle.
- **WasmJs (`wasmJsMain`)**: WebGPU canvas resizing, requestAnimationFrame frame pump, and DOM input
  event dispatching.

## Usage Example

```kotlin
import io.github.ronjunevaldoz.awake.engine.app.AwakeApplication

fun main() {
    AwakeApplication.run(myGameSpec)
}
```

## Related Modules

- [`:awake:engine:platform`](../game/README.md) — game application lifecycle and frame loop
  contract.
- [`:awake:backend:vulkan`](../../backend/vulkan/README.md) — Desktop/Android Vulkan renderer.
- [`:awake:backend:webgpu`](../../backend/webgpu/README.md) — Web browser WebGPU renderer.
