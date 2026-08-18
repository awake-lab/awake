# Awake WebGPU Backend

Status: **stable**.

WebGPU implementation of [`awake:engine:render:contract`](../../engine/render/contract/README.md),
targeting `wasmJs`. `awake-webgpu`'s `expect class Renderer` implements the contract
interface for the browser target.

## Installation

```kotlin
implementation(project(":awake:backend:webgpu"))
```

## Extensibility

Same convention as [`awake:backend:vulkan`](../vulkan/README.md): `Renderer`'s
constructor takes nullable injected pipelines for optional content
(`skyboxRenderPipeline`, `wireframeRenderPipeline`) and non-null capabilities for
always-available draw primitives. Full convention:
[docs/reference/render-extensibility.md](../../../docs/reference/render-extensibility.md).
