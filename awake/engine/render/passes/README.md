# Awake Render Passes

Status: **phase 0** -- the types exist, nothing calls them yet.

Backend-neutral render-pass logic, shared by [`awake:backend:vulkan`](../../../backend/vulkan/README.md)
and [`awake:backend:webgpu`](../../../backend/webgpu/README.md). Sibling to
[`awake:engine:render:contract`](../contract/README.md), which stays pure interface/vocabulary
types -- this module is where behavior on top of those types lives.

## Installation

```kotlin
implementation(project(":awake:engine:render:passes"))
```

## What belongs here

| Shape | Form |
|---|---|
| Owns state across calls (a pipeline reference, a mesh) | class implementing an interface, member functions |
| One calculation from explicit inputs to an output | plain top-level function, explicit params |

Never a receiver extension on a stateful interface (`fun Renderer.fogFloats()`): it hides the
function's real inputs behind an implicit receiver and makes it untestable standalone. Uniform
packers take their inputs as parameters -- `fogFloats(fogColor, fogDensity)`, not
`this.fogColor`.

Nothing here may reference a Vulkan or WebGPU type. A shared class that needs a backend import
to compile means the missing capability belongs on `CommandRecorder`, not in a backend branch.

## Today

- `command/CommandRecorder.kt` -- `CommandRecorder` plus the three opaque backend-defined
  handles it passes through (`MaterialBinding`, `PipelineHandle`, `BufferHandle`).

Planned, per
[docs/audits/2026-08-19-vulkan-webgpu-common-backend-plan.md](../../../../docs/audits/2026-08-19-vulkan-webgpu-common-backend-plan.md):
the `SharedXRenderFeature` classes, and the uniform packers (`SkyboxUniforms.kt` moving over from
`render:contract`, plus `MaterialUniforms.kt`/`LightUniforms.kt`).
