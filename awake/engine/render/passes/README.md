# Awake Render Passes (`:awake:engine:render:passes`)

Status: **Active** — shared render pass logic and draw command recording across Vulkan and WebGPU.

This module provides **backend-neutral render pass orchestration** shared by [`awake:backend:vulkan`](../../../backend/vulkan/README.md) and [`awake:backend:webgpu`](../../../backend/webgpu/README.md).

Sibling to [`awake:engine:render:contract`](../contract/README.md), which defines pure vocabulary and public interfaces. **`:awake:engine:render:passes` is where execution behavior and pass sequencing live.**

---

## Architecture & Layer Boundaries

```
┌────────────────────────────────────────────────────────────────────────┐
│ 1. :awake:engine:render:contract (Vocabulary & Public Interfaces)      │
│    • Renderer, Material, Mesh, VertexFormat, CullMode, RenderTarget    │
│    • Zero execution logic, zero per-frame algorithms                   │
└────────────────────────────────────┬───────────────────────────────────┘
                                     │
┌────────────────────────────────────┴───────────────────────────────────┐
│ 2. :awake:engine:render:passes (Pass Orchestration & Command Recording)│
│    • SharedOpaqueRenderFeature  ──> 3D mesh batching & draw recording  │
│    • SharedSkyboxRenderFeature  ──> background cube pass               │
│    • SharedUiRenderFeature      ──> 2D UI batching & scissor clipping  │
│    • CommandRecorder, PreparedDraw                                     │
│    • Pure uniform packing math (PBR, fog, lights)                      │
└────────────────────────────────────┬───────────────────────────────────┘
                                     │
┌────────────────────────────────────┴───────────────────────────────────┐
│ 3. :awake:backend:vulkan / :awake:backend:webgpu (Native GPU Drivers)  │
│    • VkDevice, GPUDevice, Swapchains, Physical GPU Memory Allocation   │
│    • VulkanCommandRecorder / WebGpuCommandRecorder implementations     │
└────────────────────────────────────────────────────────────────────────┘
```

---

## What is a "Render Pass"?

In GPU architecture, a **Render Pass** is an execution phase targeting a specific framebuffer / render target with dedicated pipeline state rules (clear color, depth testing, blending):

1. **Shadow Pass**: Draws depth from light space to an offscreen depth texture.
2. **Opaque 3D Pass**: Draws 3D scene meshes (PBR, skinned, instanced) with depth testing enabled.
3. **Skybox Pass**: Draws the environment cube with `LEQUAL` depth testing.
4. **Debug Line Pass**: Draws world-space gizmos, collision hulls, and raycasts.
5. **UI Overlay Pass**: Draws immediate-mode 2D elements with alpha blending and scissor clipping.

---

## Architectural Philosophy: Why Not a 1:1 `expect`/`actual` HAL?

When targeting multiple graphics backends (Vulkan on Desktop/Android/iOS vs. WebGPU on Browser/WasmJs), developers often ask why we do not simply declare:
```kotlin
expect class GpuDevice
expect class GpuCommandBuffer
expect class GpuPipeline
```

### The Pitfalls of 1:1 `expect`/`actual` Graphics HALs:
1. **Divergent API Paradigms**:
   - **Vulkan** relies on descriptor set pools, dynamic viewports/scissors, subpasses, pipeline layouts, and explicit memory heaps.
   - **WebGPU** relies on auto-derived bind group layouts, immutable pipeline state objects, and strict command-encoder render passes.
   - Forcing both into a 1:1 class hierarchy results in lowest-common-denominator compromises and loses the native idiomatic strengths of each driver.
2. **Boxing & JNI/Wasm Overhead**:
   - Creating thousands of wrapper objects per frame across the JVM and JS/Wasm interop boundaries degrades frame rates.
3. **Evolutionary Debt**:
   - Because both backends were originally developed independently to achieve fast time-to-first-pixel, CPU-side algorithms (uniform packing, vertex serialization, primitive batching, scissor stack math) were duplicated.

### The Awake Approach: Pass-Level Orchestration & Thin Recorders
Instead of wrapping GPU hardware objects, **Awake commonizes the execution logic**:
- **CPU Data Preparation & Math (`render:passes`)**: Pure Kotlin math, layout calculations, and batch sorting (`UiVertexLayout`, `MaterialUniformLayouts`, `UiBatchCoalescer`).
- **Pass Orchestration (`RenderFeature`)**: High-level execution sequencing (`SharedOpaqueRenderFeature`, `SharedSkyboxRenderFeature`, `SharedUiRenderFeature`).
- **Thin Recorders (`CommandRecorder`)**: Direct, zero-allocation translations into native Vulkan (`VkCommandBuffer`) or WebGPU (`GPURenderPassEncoder`) calls without intermediate wrapper overhead.

---

## Module Scope & Guidelines

| Shape | Form & Rule |
|---|---|
| **Pass Feature** (Owns state across calls) | Class implementing `RenderFeature`, member functions (`SharedOpaqueRenderFeature`, `SharedSkyboxRenderFeature`). |
| **Uniform Packing Math** (Pure calculation) | Plain top-level function taking explicit parameters (`fogUniformFloats(color, density)`), **never** an implicit receiver on `Renderer`. |
| **Backend Isolation** | **Never import a Vulkan or WebGPU type.** A capability this module needs belongs on `CommandRecorder` as a new method. |

---

## Components

- **`command/CommandRecorder.kt`**: Opaque backend-defined command recording interface (`bindPipeline`, `bindMaterial`, `bindVertexBuffer`, `bindIndexBuffer`, `draw`, `drawIndexed`).
- **`command/PreparedDraw.kt`**: Resolved draw command containing vertex/index buffer handles and material bindings.
- **`passes/SharedOpaqueRenderFeature.kt`**: Unifies 3D draw recording for default, skinned, instanced, and debug line meshes with automatic vertex/index buffer binding deduplication.

---

## Installation

```kotlin
implementation(project(":awake:engine:render:passes"))
```
