<p align="center">
  <img src=".github/logo.png" alt="Awake" width="200">
</p>

<p align="center">
  <a href="https://github.com/awake-lab/awake/actions/workflows/build-and-publish.yml"><img src="https://github.com/awake-lab/awake/actions/workflows/build-and-publish.yml/badge.svg" alt="Build And Publish"></a>
  <a href="http://kotlinlang.org"><img src="https://img.shields.io/badge/kotlin-2.4.0-blue.svg?logo=kotlin" alt="Kotlin"></a>
  <a href="https://opensource.org/licenses/Apache-2.0"><img src="https://img.shields.io/badge/License-Apache_2.0-blue.svg" alt="License"></a>
  <a href="https://www.patreon.com/cw/awakelab"><img src="https://img.shields.io/badge/donate-Patreon-f96854.svg?logo=patreon" alt="Patreon"></a>
</p>

<h1 align="center">Awake</h1>
<p align="center">A Kotlin Multiplatform game engine — one codebase, every platform.</p>

> **Not published yet.** Build from source. Nothing has shipped to Maven Central.

## What it does

- **Renders** — Vulkan on Desktop/Android/iOS, WebGPU on the Web. Shadows, PBR
  materials, glTF loading with GPU skinning.
- **Simulates** — a sparse-set ECS, a `scene { }` DSL, scenes authored as JSON instead of
  hand-rolled demo code.
- **Draws UI** — an immediate-mode, shadcn-styled UI stack, ~80 components across three
  owned layers (layout, unstyled widgets, styled design system).
- **Handles physics** — a backend-neutral API with a Jolt Physics implementation.

## Get started

```bash
./gradlew :samples:studio:run
```

A small editor shell -- rotating cube, glTF viewer, skinned-mesh, and instanced demos, each
with camera/projection/debug-visualization controls. For the Web build (Chrome/Edge 113+):
`:samples:studio:wasmJsBrowserDevelopmentRun`, then open the printed URL.

Also runnable: [`samples/ui-showcase`](samples/ui-showcase) (one page per UI component;
components shadcn ships that Awake has not built yet are listed as explicit placeholders). Desktop and Web
have app wrappers today; iOS and Android build the shared framework/AAR only, no app
wrapper yet.

## How a game fits together

1. **A `Game`** — your code (`ready`, `render`, optional `resize`/`pause`/`resume`).
2. **An application** — `VulkanGameApplication` or `WebGpuGameApplication`, which builds
   the device/swapchain/pipelines for you.
3. **A scene, optionally** — `sceneGame { }` gives you an ECS world with `Transform`,
   `MeshRenderer`, `Camera`, and `Light`, plus the systems that drive them.

```kotlin
class MyGame : Game {
    override suspend fun ready(renderer: Renderer) { /* load meshes/materials */ }
    override fun render(delta: Float, viewportWidth: Float, viewportHeight: Float) { /* draw */ }
}

val app = VulkanGameApplication(
    shaderSet = gameShaderSet("triangle"),
    vertexFormat = VertexFormat.PositionNormalColor,
    game = MyGame(),
)
```

## Modules

All modules are `0.1.0-SNAPSHOT`. ✅ supported · ⚠️ compiles, not functional yet · ❌ not targeted.

| Module | Type | Status | 📱 Mobile | 🖥️ Desktop | 🌐 Web | What it does |
|:---|:---|:---|:---:|:---:|:---:|:---|
| [`base`](awake/base) | Core | 🧪 Alpha | ✅ | ✅ | ✅ | Math, assets, input |
| [`ecs`](awake/ecs) | Core | 🧪 Alpha | ✅ | ✅ | ✅ | Sparse-set ECS runtime |
| [`engine`](awake/engine) | Runtime | 🧪 Alpha | ✅ | ✅ | ✅ | Bootstrap and app lifecycle |
| [`engine:ui`](awake/engine/ui) | UI | 🧪 Alpha | ✅ | ✅ | ✅ | Immediate-mode UI stack |
| [`scene`](awake/scene/README.md) | Runtime | 🧪 Alpha | ✅ | ✅ | ✅ | Scene graph and components |
| [`physics:api`](awake/physics/api) | API | 🧪 Alpha | ✅ | ✅ | ✅ | Backend-neutral physics contract |
| [`backend:vulkan`](awake/backend/vulkan) | Backend | 🧪 Alpha | ✅ | ✅ | ❌ | Main renderer |
| [`backend:webgpu`](awake/backend/webgpu) | Backend | ⚠️ Experimental | ❌ | ❌ | ✅ | Web renderer |
| [`backend:jolt`](awake/backend/jolt) | Backend | 🧪 Alpha | ✅ | ✅ | ⚠️ | Jolt Physics implementation (Web stub only) |
| [`backend:opengl`](awake/backend/opengl) | Backend | 🧊 Frozen | ✅ | ✅ | ❌ | The old engine's renderer. Compiles, not wired in. |

## Docs

- [`CHANGELOG.md`](CHANGELOG.md) — what changed
- [`docs/MVP_PLAN.md`](docs/MVP_PLAN.md) — near-term status
- [`docs/reference/developer-docs.md`](docs/reference/developer-docs.md) — build/test/docs workflow
- [`docs/reference/ui-validation.md`](docs/reference/ui-validation.md) — how UI/icon fidelity is
  verified
- [`tools/README.md`](tools/README.md) — asset generators and the parity toolchain

`./gradlew developerDocs` builds the API reference and UI tutorial pages.

## License

[Apache License, Version 2.0](LICENSE.md) — free for everyone, indie or enterprise, no
royalties, no revenue threshold. Awake has no company behind it and no other revenue
source: development is funded entirely by [Patreon](https://www.patreon.com/cw/awakelab)
and [GitHub Sponsors](https://github.com/sponsors/awake-lab). If Awake is useful to you,
that's what keeps it maintained.

## Contributing

Issues and PRs welcome — [github.com/awake-lab/awake](https://github.com/awake-lab/awake).
