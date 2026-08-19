---
name: awake-render-webgpu
description: >
  Rules and invariants for Awake's WebGPU rendering backend (`awake:backend:webgpu` and wgpu4k/Dawn integration).
  Read before touching WebGPU pipelines, WGSL/SPIR-V shader bindings, WASM browser canvas resizing, or buffer upload paths.
  Trigger keywords - WebGPU, wgpu4k, Dawn, WGSL, GPUTexture, GPUBuffer, GPURenderPipeline, WASM canvas, canvas resize.
---

# WebGPU Backend Engineering in Awake

Awake's WebGPU backend (`:awake:backend:webgpu`) targets cross-platform GPU execution on Web (WasmJs) and native desktop via `wgpu4k`/Dawn.

Read [docs/architecture.md](../../docs/architecture.md), [docs/MVP_PLAN.md](../../docs/MVP_PLAN.md), and [skills/awake-render-pipeline/SKILL.md](../awake-render-pipeline/SKILL.md) first.

## 1. Multiplatform Contract (WASM + Native)

- WebGPU backend uses `wgpu4k` bindings for unified WASM browser and native desktop execution.
- Web targets run on top of HTML5 Canvas (`wasmJs`); desktop targets use native window handles.
- **Rule**: Keep all WebGPU adapter, device, and pipeline initialization code backend-agnostic. Platform-specific surface acquisition stays isolated in platform adapters.

## 2. Canvas Sizing & High-DPI Handling

- In browser environments, the HTML5 canvas CSS size (`clientWidth`/`clientHeight`) differs from its backing buffer size (`width`/`height`).
- Always scale backing buffer dimensions by `window.devicePixelRatio` to maintain crisp rendering on Retina / high-DPI screens.
- Re-configure `GPUCanvasContext` on canvas resize events before beginning a new render pass.

## 3. Shader & Uniform Layout Agreement

- WebGPU and Vulkan backends share common uniform layout definitions in `awake:asset:shaders` (`LitShadowUniformLayout`, `TexturedUniformLayout`).
- **Rule**: Any uniform layout change must be verified against both backends to ensure binding indices and byte alignment match perfectly.

## 4. Buffer Upload & Lifetime Management

- Use staging buffers or `queue.writeBuffer` with explicit byte offsets and sizes.
- Ensure all created `GPUTexture` and `GPUBuffer` objects have matching `.destroy()` calls when their owning scene or material is disposed.
