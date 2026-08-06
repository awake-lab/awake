![Build And Publish](https://github.com/ronjunevaldoz/awake/actions/workflows/build-and-publish.yml/badge.svg)
[![Kotlin](https://img.shields.io/badge/kotlin-2.4.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

# Awake

Awake is a Kotlin Multiplatform game engine (Vulkan, WebGPU, OpenGL) with a shared ECS
runtime, targeting Android, iOS, Desktop (macOS/Windows/Linux), and the Web (Wasm/WebGPU)
from one `commonMain` codebase. [`samples/scene3d-playground`](samples/scene3d-playground) is
the project's live demo suite — see [docs/MMORPG_ROADMAP.md](docs/MMORPG_ROADMAP.md) for the
long-horizon plan and [docs/MVP_PLAN.md](docs/MVP_PLAN.md) for near-term status.

Not yet published — `group`/`version` are set for a future Maven Central release, but no
artifact has shipped yet. Build from source.

### Latest Changes (Unreleased)

- **ECS & Scene Split**: `awake-ecs` is now a pure sparse-set ECS runtime; scene-specific components/systems moved to `awake-scene`.
- **GPU Skinning & Textures**: Unified `MeshRenderer` draw path now supports GPU skinning (joint palettes) and texture sampling via per-mesh vertex formats.
- **Improved Camera Controls**: Extracted `OrbitControl`, `FreeFlyControl`, and `LookAtControl` into reusable scene systems.
- **Publishing Migration**: Migrated to Central Portal via `vanniktech` plugin; supports `awake-ecs` and `awake-scene` as publishable artifacts.

### Library Status

| Module | Version | Status | Stage | Platforms | Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| [`awake:base`](awake/base) | `0.1.0-SNAPSHOT` | ![Active](https://img.shields.io/badge/status-active-brightgreen) | **Alpha** | Android, iOS, Desktop, Web | Math, asset/resource utilities, input abstraction |
| [`awake:ecs`](awake/ecs/README.md) | `0.1.0-SNAPSHOT` | ![Active](https://img.shields.io/badge/status-active-brightgreen) | **Alpha** | Android, iOS, Desktop, Web | High-performance sparse-set ECS runtime |
| [`awake:engine`](awake/engine) | `0.1.0-SNAPSHOT` | ![Active](https://img.shields.io/badge/status-active-brightgreen) | **Alpha** | Android, iOS, Desktop, Web | Core engine bootstrap and application lifecycle |
| [`awake:scene`](awake/scene/README.md) | `0.1.0-SNAPSHOT` | ![Active](https://img.shields.io/badge/status-active-brightgreen) | **Alpha** | Android, iOS, Desktop, Web | Scene-graph facade (Transform, MeshRenderer, Camera) |
| [`awake:physics:api`](awake/physics/api) | `0.1.0-SNAPSHOT` | ![Active](https://img.shields.io/badge/status-active-brightgreen) | **Alpha** | Android, iOS, Desktop, Web | Backend-neutral physics world and body interfaces |
| [`awake:backend:vulkan`](awake/backend/vulkan) | `0.1.0-SNAPSHOT` | ![Active](https://img.shields.io/badge/status-active-brightgreen) | **Alpha** | Android, iOS, Desktop | Primary Vulkan renderer (Android, Desktop, iOS) |
| [`awake:backend:jolt`](awake/backend/jolt) | `0.1.0-SNAPSHOT` | ![Active](https://img.shields.io/badge/status-active-brightgreen) | **Alpha** | Android, iOS, Desktop, Web | Jolt Physics implementation of `awake:physics:api` |
| [`awake:backend:webgpu`](awake/backend/webgpu) | `0.1.0-SNAPSHOT` | ![Experimental](https://img.shields.io/badge/status-experimental-orange) | **Dev** | Web | WebGPU renderer for Web/Wasm (In-progress spike) |
| [`awake:backend:opengl`](awake/backend/opengl) | `0.1.0-SNAPSHOT` | ![Frozen](https://img.shields.io/badge/status-frozen-blue) | **Legacy** | Android, iOS, Desktop | Legacy OpenGL backend (Maintenance only) |
| [`awake:engine:ui`](awake/engine/ui) | `0.1.0-SNAPSHOT` | ![Active](https://img.shields.io/badge/status-active-brightgreen) | **Alpha** | Android, iOS, Desktop, Web | Immediate-mode UI stack (core, unstyled, designsystem) |

### Features Supported

- Vulkan — Android, Desktop (macOS/Windows/Linux), iOS (via MoltenVK)
- WebGPU — Web (Wasm), behind the same renderer abstraction as Vulkan
- OpenGL — Android, iOS, Desktop (frozen: bugfixes only, Vulkan is the active backend)
- Shared ECS (`awake:ecs`) + scene runtime (`awake:scene`: `Transform`, `MeshRenderer`,
  `Camera`, `Light`, orbit/free-fly/follow/look-at camera controls, physics bodies)
- GPU skinning, texture sampling, and directional lighting through one unified
  `MeshRenderer`/`RenderSystem` draw path (per-mesh vertex format resolves its own pipeline)
- Jolt Physics integration (`awake:backend:jolt`) behind a backend-neutral
  `awake:physics:api` contract

### Sample Projects

- [`samples:scene3d-playground`](samples/scene3d-playground) — The live demo suite (see "Running the Demos" below).
- [`samples:ui-showcase`](samples/ui-showcase) — Component gallery for the `awake:engine:ui` stack.
- [`samples:server`](samples/server) — Ktor-based debug-control server for remote engine inspection.

### Docs

- Developer docs workflow: [docs/reference/developer-docs.md](docs/reference/developer-docs.md)
- Tutorial coverage tracker: [docs/reference/tutorial-coverage.md](docs/reference/tutorial-coverage.md)
- Build the current API references plus UI tutorial artifacts:
  ```
  ./gradlew developerDocs
  ```
- UI tutorial output lands at
  `awake/engine/ui/ui-unstyled/build/reports/ui-tutorials/index.html`
  and the broader visual snapshot gallery lands at
  `awake/engine/ui/ui-unstyled/build/reports/ui-snapshots/index.html`

### Running the Demos

[`samples:scene3d-playground`](samples/scene3d-playground): a suite of ECS-driven demo
pages (rotating cube with orbit/free-fly/follow/look-at camera modes, a real glTF viewer,
GPU skinning, texture sampling — see `src/commonMain/.../demos`). Desktop and Web are the
platforms with runnable app entry points today; iOS/Android targets build the shared
framework/AAR but don't yet have a standalone app wrapper in this sample.

- **Desktop**: `./gradlew :samples:scene3d-playground:run` (real GLFW + Vulkan window,
  MoltenVK on macOS)
- **Web** (WebGPU, requires a browser with WebGPU support — Chrome/Edge 113+):
  ```
  ./gradlew :samples:scene3d-playground:wasmJsBrowserDevelopmentRun
  ```
  Then open the URL it prints.

### Building a New Game

The reusable engine bootstrap is `VulkanGameApplication` (`awake:backend:vulkan`) /
`WebGpuGameApplication` (`awake:backend:webgpu`), which extend `GenericGameApplication`
(`awake:engine:game`) — a pure render bootstrap with no ECS/UI/asset opinions of its own.
Construct one with your shader/vertex-layout paths plus a `Game` implementation (`awake:
engine:game`'s `Game` interface: `suspend fun ready(renderer)`, `fun render(delta,
viewportWidth, viewportHeight)`, plus optional `resize`/`pause`/`resume`/`dispose`) —
`GraphicsDevice`/`SwapchainManager`/`RenderPipeline`/`Renderer` construction is handled
generically, and every `Application` lifecycle callback forwards straight to your `Game`.
Your `Game` calls `renderer.createMesh(geometry)`/`createMaterial(texture)` for whatever
assets it wants, and — if it wants an ECS/scene graph — drives its own
`SceneGameRuntime` (`awake:scene:runtime`, via the `sceneGame { }` DSL in `awake:scene-dsl`)
to register systems and entities against `TransformSystem`/`RenderSystem`. See
`samples:scene3d-playground`'s demo pages (commonMain, injected identically into both
backends at each platform's entry point) for worked examples covering camera controls,
lighting, skinning, and texturing.

### Tools

Vulkan requires `.spv` shaders, so a GlslValidator Gradle plugin converts `.glsl`/`.vert`/
`.frag` sources to `.spv` on demand (manual step, not wired into the automatic build):

1. Install `glslangValidator`:
   ```
   brew install glslang
   ```
2. Verify installation:
   ```
   glslangValidator --version
   ```
3. Run the Gradle task after editing any shader:
   ```
   ./gradlew glslValidator
   ```

### License

Released under the [Apache License, Version 2.0](LICENSE).
