# Awake Render Contract (`:awake:engine:render:contract`)

Status: **Stable** — pure cross-backend vocabulary and public rendering contracts.

This module defines the **interfaces and data types** that games, ECS systems, and render features consume to express what gets drawn.

Sibling to [`awake:engine:render:passes`](../passes/README.md), which implements backend-neutral render pass execution on top of these contracts.

---

## What Lives Here

This is a **pure-types, zero-execution** module:

- **`Renderer`**: The primary public interface (`draw`, `drawDebugLines`, `drawUi`, `createMesh`, `createMaterial`, `createTexture`).
- **`Mesh` & `MeshGeometry`**: Geometry representations, vertex formats, procedural generators, and intrinsic bounding boxes.
- **`Material` & `RenderMaterial`**: Material interfaces, uniform counts, and descriptor binding contracts.
- **`VertexFormat`**: Vertex layouts (`PositionColorUv`, `PositionNormalColor`, `PositionNormalColorSkin`, etc.).
- **`CullMode`**: Face culling contracts (`None`, `Back`, `Front`).
- **`RenderTarget` & `Texture`**: Target dimensions and GPU texture handles.
- **`DrawCall` & `SceneLight`**: Renderable descriptors passed from scene systems to the renderer.

---

## Guidelines

- **No backend types**: Never import Vulkan or WebGPU driver types in this module.
- **No execution algorithms**: Frame loops, pass recordings, and command buffers belong in `:awake:engine:render:passes` or backend implementations.
- **Extensibility**: The contract declares capabilities, not hardcoded scenes. Full convention: [docs/reference/render-extensibility.md](../../../../docs/reference/render-extensibility.md).

---

## Installation

```kotlin
implementation(project(":awake:engine:render:contract"))
```
