# Awake Render Contract

Status: **stable**.

The one real cross-backend `interface Renderer` -- the entry point `RenderSystem` calls.
Backends implement it via `expect class Renderer(...) : Renderer`, one `actual` per
platform target:

- [`awake:backend:vulkan`](../../../backend/vulkan/README.md) -- Android, desktop, iOS
- [`awake:backend:webgpu`](../../../backend/webgpu/README.md) -- wasmJs

## Installation

```kotlin
implementation(project(":awake:engine:render:contract"))
```

## Extensibility

The contract itself is capability-only -- it declares what a backend *can* draw
(`draw`, `drawDebugLines`, `drawUi`), never what gets drawn. Content stays optional at
the backend implementation layer. Full convention:
[docs/reference/render-extensibility.md](../../../../docs/reference/render-extensibility.md).
