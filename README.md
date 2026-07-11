![Build And Publish](https://github.com/ronjunevaldoz/awake/actions/workflows/build-and-publish.yml/badge.svg)
[![Kotlin](https://img.shields.io/badge/kotlin-2.4.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

# Awake

Awake is a Kotlin Multiplatform game engine (Vulkan, WebGPU, OpenGL) with a shared ECS
runtime, targeting Android, iOS, Desktop (macOS/Windows/Linux), and the Web (Wasm/WebGPU)
from one `commonMain` codebase. [`sample-hello-cube`](sample-hello-cube) is the project's
demo/starter app — see [docs/MMORPG_ROADMAP.md](docs/MMORPG_ROADMAP.md) for the long-horizon
plan and [docs/MVP_PLAN.md](docs/MVP_PLAN.md) for near-term status.

### Features Supported

- Vulkan — Android, Desktop (macOS/Windows/Linux), iOS (via MoltenVK)
- WebGPU — Web (Wasm), behind the same renderer abstraction as Vulkan
- OpenGL — Android, iOS, Desktop (frozen: bugfixes only, Vulkan is the active backend)
- Shared ECS (`awake-ecs`) + scene graph (`awake-scene`: `Transform`, `MeshRenderer`,
  `Camera`, `Light`, NavMesh-driven AI)

### Modules

- [`awake-engine`](awake-engine) — engine core: `EngineConfig`, `Application`, game loop
- [`awake-base`](awake-base) — math, asset/resource utilities, input abstraction
- [`awake-ecs`](awake-ecs/README.md) — sparse-set ECS runtime (entities, components, systems, queries)
- [`awake-scene`](awake-scene/README.md) — scene-graph components/systems built on `awake-ecs`
- [`awake-engine-render-api`](awake-engine-render-api) — backend-neutral renderer interfaces
- [`awake-backend-vulkan`](awake-backend-vulkan) — Vulkan bindings + renderer (Android/Desktop/iOS)
- [`awake-backend-webgpu`](awake-backend-webgpu) — WebGPU renderer (Web/Wasm)
- [`awake-opengl`](awake-opengl) — legacy OpenGL backend (frozen)
- [`awake-engine-game`](awake-engine-game) — `GenericGameApplication`, the backend-neutral
  game bootstrap `VulkanGameApplication`/`WebGpuGameApplication` both extend
- [`sample-hello-cube`](sample-hello-cube) — the demo/starter project (see "Running the Demo"
  below)

### Running the Demo

[`sample-hello-cube`](sample-hello-cube): a single cube with an orbit/free-fly camera and a
frustum-wireframe debug toggle, ~150 lines total. Targets all 4 platforms:

- **Desktop**: `./gradlew :sample-hello-cube:run` (real GLFW + Vulkan window, MoltenVK on
  macOS)
- **Web** (WebGPU, requires a browser with WebGPU support — Chrome/Edge 113+):
  ```
  ./gradlew :sample-hello-cube:wasmJsBrowserDevelopmentRun
  ```
  Then open the URL it prints (usually `http://localhost:8081`).
- **Android**: open the project in Android Studio, run the `sample-hello-cube:androidApp`
  configuration — a plain `Activity` calling
  `setContentView(VulkanView(this, SampleApplication()))`, no Compose needed.
- **iOS**: open `sample-hello-cube/iosApp` in Xcode (Apple Silicon Simulator or a real
  device) and run — the shared code ships as an XCFramework via SPM, no CocoaPods needed.
  `VulkanMetalView` is wired directly from a plain `UIViewController`
  (`sample-hello-cube/src/iosMain/kotlin/main.ios.kt`'s `makeSampleViewController()`), no
  Compose in the loop.

Controls: drag to orbit/free-fly the camera, `W`/`S` to zoom (Orbit mode) or move
forward/back (Free-fly), the top-left panel switches camera mode and toggles the
frustum-wireframe overlay.

### Building a New Game

The reusable engine bootstrap lives in `VulkanGameApplication`
(`awake-backend-vulkan`) and `WebGpuGameApplication` (`awake-backend-webgpu`) — both extend
`GenericGameApplication` (`awake-engine-game`), which owns everything backend-neutral (scene
loading, the fixed-timestep loop, UI/debug-line staging). A new game subclasses
`VulkanGameApplication`/`WebGpuGameApplication`, supplying mesh geometry, an optional
texture, and a scene JSON path through the constructor —
`GraphicsDevice`/`SwapchainManager`/`RenderPipeline`/`Mesh`/`Material`/`Renderer`
construction is handled generically. Game-specific behavior (player movement, camera
control, AI) is added by overriding `onSceneReady()` (resolve your entities once the scene
loads) and `onFixedUpdate()`/`onRender()` (per-frame logic) — see `sample-hello-cube`'s own
`SampleApplication.kt`/`WebGpuSampleApplication.kt` for a worked example with an orbit/
free-fly camera and a frustum-wireframe debug overlay layered on top of the same base class.

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
