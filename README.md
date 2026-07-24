![Build And Publish](https://github.com/ronjunevaldoz/awake/actions/workflows/build-and-publish.yml/badge.svg)
[![Kotlin](https://img.shields.io/badge/kotlin-2.4.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

# Awake

Awake is a Kotlin Multiplatform game engine (Vulkan, WebGPU, OpenGL) with a shared ECS
runtime, targeting Android, iOS, Desktop (macOS/Windows/Linux), and the Web (Wasm/WebGPU)
from one `commonMain` codebase. [`samples/hello-cube`](samples/hello-cube) is the project's
demo/starter app — see [docs/MMORPG_ROADMAP.md](docs/MMORPG_ROADMAP.md) for the long-horizon
plan and [docs/MVP_PLAN.md](docs/MVP_PLAN.md) for near-term status.

### Features Supported

- Vulkan — Android, Desktop (macOS/Windows/Linux), iOS (via MoltenVK)
- WebGPU — Web (Wasm), behind the same renderer abstraction as Vulkan
- OpenGL — Android, iOS, Desktop (frozen: bugfixes only, Vulkan is the active backend)
- Shared ECS (`awake:ecs`) + scene graph (`awake:scene`: `Transform`, `MeshRenderer`,
  `Camera`, `Light`, NavMesh-driven AI)

### Modules

- [`awake:engine`](awake/engine) — engine core: `EngineConfig`, `Application`, game loop
- [`awake:base`](awake/base) — math, asset/resource utilities, input abstraction
- [`awake:ecs`](awake/ecs/README.md) — sparse-set ECS runtime (entities, components, systems, queries)
- [`awake:scene`](awake/scene/README.md) — scene-graph components/systems built on `awake:ecs`
- [`awake:engine:render-api`](awake/engine/render-api) — backend-neutral renderer interfaces
- [`awake:backend:vulkan`](awake/backend/vulkan) — Vulkan bindings + renderer (Android/Desktop/iOS)
- [`awake:backend:webgpu`](awake/backend/webgpu) — WebGPU renderer (Web/Wasm)
- [`awake:backend:opengl`](awake/backend/opengl) — legacy OpenGL backend (frozen)
- [`awake:engine:game`](awake/engine/game) — `GenericGameApplication`, the backend-neutral
  game bootstrap `VulkanGameApplication`/`WebGpuGameApplication` both extend
- [`samples:hello-cube`](samples/hello-cube) — the demo/starter project (see "Running the Demo"
  below)

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

### Running the Demo

[`samples:hello-cube`](samples/hello-cube): a single cube with an orbit/free-fly camera and a
frustum-wireframe debug toggle, ~150 lines total. Targets all 4 platforms:

- **Desktop**: `./gradlew :samples:hello-cube:run` (real GLFW + Vulkan window, MoltenVK on
  macOS)
- **Web** (WebGPU, requires a browser with WebGPU support — Chrome/Edge 113+):
  ```
  ./gradlew :samples:hello-cube:wasmJsBrowserDevelopmentRun
  ```
  Then open the URL it prints (usually `http://localhost:8081`).
- **Android**: open the project in Android Studio, run the `samples:hello-cube:androidApp`
  configuration — a plain `Activity` calling
  `setContentView(VulkanView(this, SampleApplication()))`, no Compose needed.
- **iOS**: open `samples/hello-cube/iosApp` in Xcode (Apple Silicon Simulator or a real
  device) and run — the shared code ships as an XCFramework via SPM, no CocoaPods needed.
  `VulkanMetalView` is wired directly from a plain `UIViewController`
  (`samples/hello-cube/src/iosMain/kotlin/main.ios.kt`'s `makeSampleViewController()`), no
  Compose in the loop.

Controls: drag to orbit/free-fly the camera, `W`/`S` to zoom (Orbit mode) or move
forward/back (Free-fly), the top-left panel switches camera mode and toggles the
frustum-wireframe overlay.

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
assets it wants, and — if it wants an ECS/scene graph — constructs its own `SceneRuntime`
(`awake:scene`) to load a scene JSON and drive `TransformSystem`/`RenderSystem`. See
`samples:hello-cube`'s own `SampleGame.kt` (commonMain, injected identically into both
backends at each platform's entry point) for a worked example with an orbit/free-fly
camera, a frustum-wireframe debug overlay, and its own `SceneRuntime`.

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

This library is released under the Apache, Version 2.0 License..
