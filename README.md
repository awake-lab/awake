<h1 align="center">Awake</h1>
<p align="center">Kotlin Multiplatform first, Vulkan and WebGPU native — a game engine for developers who want to feel every frame they write.</p>

<p align="center">
  <a href="https://github.com/awake-lab/awake/actions/workflows/build-and-publish.yml"><img src="https://github.com/awake-lab/awake/actions/workflows/build-and-publish.yml/badge.svg" alt="Build And Publish"></a>
  <a href="http://kotlinlang.org"><img src="https://img.shields.io/badge/kotlin-2.4.0-blue.svg?logo=kotlin" alt="Kotlin"></a>
  <a href="https://opensource.org/licenses/Apache-2.0"><img src="https://img.shields.io/badge/License-Apache_2.0-blue.svg" alt="License"></a>
  <a href="https://www.patreon.com/cw/awakelab"><img src="https://img.shields.io/badge/donate-Patreon-f96854.svg?logo=patreon" alt="Patreon"></a>
</p>

> Not published yet — build from source. Code-first by design, editor optional. See [`docs/ABOUT.md`](docs/ABOUT.md) for why.

## What it does

- **Renders** — Vulkan on Desktop/Android/iOS, WebGPU on the Web. Shadows, PBR materials, glTF loading with GPU skinning.
- **Simulates** — a sparse-set ECS and a `scene { }` DSL; scenes are authored as JSON, not hand-rolled demo code.
- **Draws UI** — an immediate-mode, shadcn-styled UI stack, ~80 components.
- **Handles physics** — a backend-neutral API with a Jolt Physics implementation.

## Get started

```bash
./gradlew :samples:studio:run
```

Rotating cube, glTF viewer, skinned-mesh, and instanced demos, each with camera/projection/debug controls.
Web build (Chrome/Edge 113+): `:samples:studio:wasmJsBrowserDevelopmentRun`, then open the printed URL.

Also runnable: [`samples/ui-showcase`](samples/ui-showcase), one page per UI component.

## How a game fits together

- **`AppLifecycle`** — your code: `ready` to load, `update` to draw.
- **`VulkanEngine` / `WebGpuEngine`** — builds the device/swapchain/pipelines for you.
- **`sceneGame { }`**, optional — an ECS world with `Transform`, `MeshRenderer`, `Camera`, `Light`, and the systems that drive them.

```kotlin
private val triangleShaders = shaderSet("triangle")

class MyGame : AppLifecycle {
    override suspend fun ready(renderer: Renderer) { /* load meshes/materials */ }
    override fun update(delta: Float, viewportWidth: Float, viewportHeight: Float) { /* draw */ }
}

val engine = VulkanEngine(
    shaderSet = triangleShaders,
    vertexFormat = VertexFormat.PositionNormalColor,
    appLifecycle = MyGame(),
)
```

## Modules

All `0.1.0-SNAPSHOT`, alpha.

- [`core`](awake/core) — math, geometry, animation
- [`ecs`](awake/ecs) — sparse-set ECS runtime
- [`asset`](awake/asset) — glTF loading, mesh optimization, shader tooling
- [`scene`](awake/scene/README.md) — scene graph and `scene { }` DSL
- [`engine`](awake/engine) — app lifecycle and render-pass orchestration
- [`ui`](awake/ui/README.md) — immediate-mode UI stack (layout, headless, shadcn design system)
- [`physics:api`](awake/physics/api) — backend-neutral physics contract
- [`backend:vulkan`](awake/backend/vulkan) — main renderer (Desktop/Android/iOS)
- [`backend:webgpu`](awake/backend/webgpu) — web renderer, experimental
- [`backend:jolt`](awake/backend/jolt) — Jolt Physics implementation

## Docs

- [`CHANGELOG.md`](CHANGELOG.md) — what changed
- [`docs/MVP_PLAN.md`](docs/MVP_PLAN.md) — near-term status
- [`docs/reference/developer-docs.md`](docs/reference/developer-docs.md) — build/test/docs workflow
- [`docs/reference/ui-validation.md`](docs/reference/ui-validation.md) — how UI/icon fidelity is verified
- [`tools/README.md`](tools/README.md) — asset generators and the parity toolchain

`./gradlew developerDocs` builds the API reference and UI tutorial pages.

## License

[Apache License, Version 2.0](LICENSE.md). Solo project, no company behind it — funded by
[Patreon](https://www.patreon.com/cw/awakelab) and [GitHub Sponsors](https://github.com/sponsors/awake-lab).

## Contributing

Issues and PRs welcome — [github.com/awake-lab/awake](https://github.com/awake-lab/awake).
