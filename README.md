![Build And Publish](https://github.com/ronjunevaldoz/awake/actions/workflows/build-and-publish.yml/badge.svg)
[![Kotlin](https://img.shields.io/badge/kotlin-2.4.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

<p align="center">
  <img src="readme_images/logo.png" width="400" alt="Awake Logo">
</p>

# Awake

A Kotlin Multiplatform game engine. Write your game once in `commonMain` and run it on
Android, iOS, Desktop, and the Web — Awake picks the right graphics backend for each
(Vulkan, or WebGPU on the web).

<p align="center">
  <img src="readme_images/banner.png" alt="Awake Multiplatform">
</p>

> **Not published yet.** Build from source. Nothing has shipped to Maven Central.

## Try it

The demo suite is [`samples/scene3d-playground`](samples/scene3d-playground) — a rotating
cube with four camera modes, a glTF model viewer, GPU skinning, and texture sampling.

```bash
./gradlew :samples:scene3d-playground:run
```

For the web version (needs Chrome/Edge 113+ for WebGPU), run
`:samples:scene3d-playground:wasmJsBrowserDevelopmentRun` and open the URL it prints.

Desktop and Web are the targets with runnable apps today. iOS and Android build the shared
framework and AAR, but this sample has no app wrapper for them yet.

There's also [`samples/ui-showcase`](samples/ui-showcase), a gallery of every UI component.

## How a game fits together

Three pieces, roughly:

1. **A `Game`** — your code. Implement `ready(renderer)` and `render(delta, width, height)`
   (plus optional `resize`/`pause`/`resume`/`dispose`).
2. **An application** — `VulkanGameApplication` or `WebGpuGameApplication`. Hand it your
   `Game` and your shader paths; it builds the device, swapchain, and pipelines for you.
3. **A scene, if you want one** — `sceneGame { }` gives you an ECS world with `Transform`,
   `MeshRenderer`, `Camera`, and `Light` components, plus the systems that drive them.

Meshes and materials come from `renderer.createMesh(...)` / `renderer.createMaterial(...)`.
A mesh's vertex format decides which pipeline draws it, so skinned and textured meshes go
through the same `MeshRenderer` path as everything else.

The demo pages under `samples/scene3d-playground/src/commonMain` are worked examples of all
of this, and they're shared verbatim by both backends.

## Modules

All modules are `0.1.0-SNAPSHOT` and target Android, iOS, Desktop, and Web unless noted.

| Module | Status | What it does |
| :--- | :--- | :--- |
| [`awake:base`](awake/base) | Alpha | Math, assets, input |
| [`awake:ecs`](awake/ecs/README.md) | Alpha | Sparse-set ECS runtime |
| [`awake:engine`](awake/engine) | Alpha | Bootstrap and app lifecycle |
| [`awake:engine:ui`](awake/engine/ui) | Alpha | Immediate-mode UI stack |
| [`awake:scene`](awake/scene/README.md) | Alpha | Scene graph and components |
| [`awake:physics:api`](awake/physics/api) | Alpha | Backend-neutral physics contract |
| [`awake:backend:vulkan`](awake/backend/vulkan) | Alpha | Main renderer (no Web) |
| [`awake:backend:webgpu`](awake/backend/webgpu) | Experimental | Web renderer (Web only) |
| [`awake:backend:jolt`](awake/backend/jolt) | Alpha | Jolt Physics implementation |
| [`awake:backend:opengl`](awake/backend/opengl) | Frozen | Legacy; bugfixes only |

## Shaders

Shaders are authored once as WGSL and compiled to each backend's format. After editing one:

```bash
./gradlew :samples:scene3d-playground:syncAwakeShaders
```

Vulkan loads the compiled `.spv` binaries at runtime, **not** the `.frag`/`.vert` sources
next to them — so editing a source without recompiling silently changes nothing. The
`verifyShaderBinaries` check catches that and fails the build.

## Docs

- [`CHANGELOG.md`](CHANGELOG.md) — what changed
- [`docs/MVP_PLAN.md`](docs/MVP_PLAN.md) — near-term status
- [`docs/MMORPG_ROADMAP.md`](docs/MMORPG_ROADMAP.md) — long-horizon plan
- [`docs/reference/developer-docs.md`](docs/reference/developer-docs.md) — docs workflow

`./gradlew developerDocs` builds the API reference and UI tutorial pages.

## License

[Apache License, Version 2.0](LICENSE).
