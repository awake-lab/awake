<h1 align="center">Awake</h1>
<p align="center">Kotlin Multiplatform first, Vulkan and WebGPU native — a game engine and graphics runtime for developers who want to feel every frame they write.</p>

<p align="center">
  <a href="https://github.com/awake-lab/awake/actions/workflows/build-and-publish.yml"><img src="https://github.com/awake-lab/awake/actions/workflows/build-and-publish.yml/badge.svg" alt="Build And Publish"></a>
  <a href="http://kotlinlang.org"><img src="https://img.shields.io/badge/kotlin-2.4.0-blue.svg?logo=kotlin" alt="Kotlin"></a>
  <a href="https://opensource.org/licenses/Apache-2.0"><img src="https://img.shields.io/badge/License-Apache_2.0-blue.svg" alt="License"></a>
  <a href="https://www.patreon.com/cw/awakelab"><img src="https://img.shields.io/badge/donate-Patreon-f96854.svg?logo=patreon" alt="Patreon"></a>
</p>

> Not published yet — build from source. Code-first by design, editor optional. See [`docs/ABOUT.md`](docs/ABOUT.md) for why.

## What it does

- **Renders** — Vulkan on Desktop/Android/iOS, WebGPU on the Web. Shadows, PBR materials, glTF loading with GPU skinning, full-stack `CullMode` control, and batching state deduplication.
- **Simulates** — a high-performance sparse-set ECS, zero-allocation transform math, and declarative `@AwakeSceneDsl` authoring.
- **Draws UI** — an immediate-mode, shadcn-styled UI stack with ~80 components across `ui-core`, `ui-headless`, and `ui-designsystem`.
- **Procedural Geometry** — built-in primitive geometry generators (`generate { cube() }`, `generate { plane() }`) with auto-computed bounds.
- **Handles physics** — a backend-neutral API with a Jolt Physics bridge.

## Get started

```bash
./gradlew :samples:studio:run
```

Runs **Awake Studio**: an interactive 3D editor with live scene switching, glTF previewing, skinned mesh animations, particle emitters, inspector panels, and 3D gizmos.

Web build (Chrome/Edge 113+ with WebGPU):
```bash
./gradlew :samples:studio:wasmJsBrowserDevelopmentRun
```

Also runnable: [`samples/ui-showcase`](samples/ui-showcase), a complete gallery showcasing every shadcn UI component.

## Quickstart

Awake provides a declarative, type-safe DSL for building applications and scenes:

```kotlin
fun main() = runVulkanDesktopGame(
    game {
        window {
            title = "Rotating Cube"
            size(1280, 720)
        }
        scene {
            defaultOrbitCamera()

            entity("cube") {
                with(SpinControl(speedX = 0.5f, speedY = 1.0f))
                mesh(generate { cube(size = 2f, colored = true) })
            }

            lighting.singleDirectionalLight(color = Color.WHITE)
        }
    },
)
```

For lower-level control or custom render loops:
```kotlin
class MyCustomApp : AppLifecycle {
    override suspend fun ready(renderer: Renderer) { /* Load GPU assets */ }
    override fun update(delta: Float, viewportWidth: Float, viewportHeight: Float) { /* Draw commands */ }
}
```

## Modules

All `0.1.0-SNAPSHOT`, alpha.

- [`core`](awake/core) — math, geometry, matrix TRS, AABB bounds, animation
- [`ecs`](awake/ecs) — high-performance sparse-set ECS runtime
- [`asset`](awake/asset) — glTF loading, mesh optimization, shader tooling
- [`scene`](awake/scene/README.md) — scene graph, authoring DSL, and `SceneManager` lifecycle
- [`engine`](awake/engine) — application bootstrap, mediator runtime, and render-pass orchestration
- [`ui`](awake/ui/README.md) — immediate-mode UI stack (`ui-core`, `ui-headless`, `ui-designsystem`, `ui-tailwind`)
- [`physics:api`](awake/physics/api) — backend-neutral physics contract
- [`backend:vulkan`](awake/backend/vulkan) — main Vulkan renderer (Desktop/Android/iOS)
- [`backend:webgpu`](awake/backend/webgpu) — WebGPU web renderer
- [`backend:jolt`](awake/backend/jolt) — Jolt Physics bridge

## Docs

- [`CHANGELOG.md`](CHANGELOG.md) — what changed
- [`docs/MVP_PLAN.md`](docs/MVP_PLAN.md) — active roadmap and status
- [`docs/tasks.md`](docs/tasks.md) — current work lanes and landed milestones
- [`docs/reference/developer-docs.md`](docs/reference/developer-docs.md) — build/test/docs workflow
- [`docs/reference/ui-ownership.md`](docs/reference/ui-ownership.md) — UI architecture & clean layering
- [`docs/reference/ui-validation.md`](docs/reference/ui-validation.md) — how UI and icon fidelity are verified
- [`docs/benchmarks/performance-visualization.ipynb`](docs/benchmarks/performance-visualization.ipynb) — interactive performance benchmarks

## License

[Apache License, Version 2.0](LICENSE.md). Solo project, no company behind it — funded by
[Patreon](https://www.patreon.com/cw/awakelab) and [GitHub Sponsors](https://github.com/sponsors/awake-lab).

## Contributing

Issues and PRs welcome — [github.com/awake-lab/awake](https://github.com/awake-lab/awake).
