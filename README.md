![Build And Publish](https://github.com/ronjunevaldoz/awake/actions/workflows/build-and-publish.yml/badge.svg)
[![Kotlin](https://img.shields.io/badge/kotlin-2.4.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

# Awake

Awake is a Kotlin Multiplatform game engine (Vulkan, WebGPU, OpenGL) with a shared ECS
runtime, targeting Android, iOS, Desktop (macOS/Windows/Linux), and the Web (Wasm/WebGPU)
from one `commonMain` codebase. `awake-demo` is the playable MVP built on top of it — see
[docs/MMORPG_ROADMAP.md](docs/MMORPG_ROADMAP.md) for the long-horizon plan and
[docs/MVP_PLAN.md](docs/MVP_PLAN.md) for near-term status.

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
- [`awake-demo`](awake-demo) — the playable demo app (shared code + per-platform entry points)
- [`sample-hello-cube`](sample-hello-cube) — minimal starter project (see "Building a New Game" below)

### Running the Demo

Clone the repo, then pick a platform:

- **Desktop** (real GLFW + Vulkan window, MoltenVK on macOS):
  ```
  ./gradlew :awake-demo:desktopApp:runVulkanDesktop
  ```
  Or the Compose-embedded variant (FPS/HUD overlay, Vulkan on/off switch):
  ```
  ./gradlew :awake-demo:desktopApp:run
  ```
- **Web** (WebGPU, requires a browser with WebGPU support — Chrome/Edge 113+):
  ```
  ./gradlew :awake-demo:shared:wasmJsBrowserDevelopmentRun
  ```
  Then open the URL it prints (usually `http://localhost:8080`).
- **Android**: open the project in Android Studio, run the `androidApp` configuration on a
  device/emulator with Vulkan support.
- **iOS**: open `awake-demo/iosApp` in Xcode (Apple Silicon Simulator or a real device) and
  run — the shared code ships as an XCFramework via SPM, no CocoaPods needed.

Controls: WASD/arrow keys (desktop/web) or touch-drag (Android/iOS) move the player; the
camera follows in third person; an NPC chases the player using a real Recast/Detour navmesh
(desktop/Android only for now — see `docs/MMORPG_ROADMAP.md`'s NavMesh decision).

### Building a New Game

`awake-demo` is a full MVP, not a starting point — for a new project, start from
[`sample-hello-cube`](sample-hello-cube) instead: a single static Vulkan cube, ~100 lines
total, no player/camera/AI/NavMesh. It targets all 4 platforms, same as `awake-demo`:

- **Desktop**: `./gradlew :sample-hello-cube:run`
- **Web** (WebGPU): `./gradlew :sample-hello-cube:wasmJsBrowserDevelopmentRun`, then open
  the printed URL.
- **Android**: open the project in Android Studio, run the `sample-hello-cube:androidApp`
  configuration — a plain `Activity` calling
  `setContentView(VulkanView(this, SampleApplication()))`, no Compose needed.
- **iOS**: open `sample-hello-cube/iosApp` in Xcode and run. Unlike `awake-demo` (which
  hosts its canvas through Compose), this sample wires `VulkanMetalView` directly from a
  plain `UIViewController` (`sample-hello-cube/src/iosMain/kotlin/main.ios.kt`'s
  `makeSampleViewController()`) — the pattern to copy if your own game skips Compose too.

The reusable engine bootstrap lives in `VulkanGameApplication`
(`awake-backend-vulkan`) and `WebGpuGameApplication` (`awake-backend-webgpu`) — a new game
subclasses one of these, supplying mesh geometry, an optional texture, and a scene JSON path
through the constructor. `GraphicsDevice`/`SwapchainManager`/`RenderPipeline`/`Mesh`/
`Material`/`Renderer` construction, scene loading, and the fixed-timestep loop are all
handled generically. Game-specific behavior (player movement, camera control, AI) is added
by overriding `onSceneReady()` (resolve your entities once the scene loads) and
`onFixedUpdate()`/`onRender()` (per-frame logic) — see `awake-demo`'s own
`VulkanApplication.kt`/`WebGpuApplication.kt` for a worked example with a moving player,
third-person camera, and NavMesh-driven AI layered on top of the same base class.

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
