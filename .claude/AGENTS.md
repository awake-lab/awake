# AGENTS.md — Awake

This project uses [kmm-agent-skills](https://github.com/ronjunevaldoz/kmm-agent-skills).
Skills are installed in `.claude/skills/`.

## Project overview

Awake is a Kotlin Multiplatform **game engine library**: a cross-platform graphics wrapper
(OpenGL working; Vulkan in progress) evolving toward a full KMP game engine with ECS,
a Compose-style scene API, and a desktop editor. Native Vulkan access is via JNI (Android /
Desktop JVM) and MoltenVK cinterop (iOS, planned). C++ JNI bindings are generated with
[jni-binding-generator](https://github.com/ronjunevaldoz/jni-binding-generator).

Group ID: `io.github.ronjunevaldoz` · Artifacts: `awake-core`, `awake-vulkan`,
`awake-ecs`, `awake-scene` ·
Published to: Maven Central (Sonatype snapshots)

**The roadmap and task checklist live in [docs/MVP_PLAN.md](../docs/MVP_PLAN.md) — consult
it before starting work; phases and open decisions (D1–D4) are tracked there.**

## Skill routing

| Topic | Skill |
|---|---|
| Publishing to Maven Central | `kotlin-multiplatform-library-publishing` |
| iOS / SPM distribution, MoltenVK framework | `kotlin-multiplatform-xcframework-spm` |
| JNI bridge / C++ marshalling / memory safety | `kotlin-multiplatform-jni-pro` |
| Platform-specific implementations (`expect/actual`) | `kotlin-multiplatform-expect-actual` |
| Toolchain upgrade / KMP migration | `kotlin-multiplatform-migration` |
| Unit / integration tests | `kotlin-multiplatform-unit-testing` |
| Code quality (Detekt, Ktlint) | `kotlin-multiplatform-code-quality` |
| CI automation | `kotlin-multiplatform-ci-github-actions` |
| Desktop windowing / packaging (editor, GLFW host) | `kotlin-multiplatform-desktop-app` |
| Custom drawing / graphics layers (editor viewport) | `kotlin-multiplatform-graphics-modifiers` |
| Release / versioning / changelog | `kotlin-multiplatform-release` |
| Architecture audit | `kotlin-multiplatform-audit` |
| Capture lessons learned | `kotlin-multiplatform-lessons` |
| Harvest consumer lessons | `/kmm-harvest-lessons` |

## Module graph

| Module | Purpose | Published |
|---|---|---|
| `:awake-base` | Dependency-free portable core: math, `Input`, `FixedTimestepLoop`, glTF parsing, bitmap/resource I/O — no Compose, no Vulkan, no OpenGL (see Decision Log D11) | ✅ `awake-base` |
| `:awake-core` | Backend-agnostic app-lifecycle glue: `Application`, `GameLoop` + actuals, `VulkanView`, `EngineConfig` — no Compose, no rendering backend (see Decision Log D11) | ✅ `awake-core` |
| `:awake-opengl` | Legacy OpenGL rendering backend: `Context`/`Config`, GL object wrappers, shaders, fonts, GLFW desktop window (see Decision Log D11) | ✅ `awake-opengl` |
| `:awake-ecs` | Pure sparse-set ECS runtime: entities, component stores, world queries, systems | ✅ `awake-ecs` |
| `:awake-scene` | Awake scene components/systems on top of ECS (`Transform`, `Camera`, render integration) | ✅ `awake-scene` |
| `:awake-vulkan` | Vulkan KMP bindings — common API + JNI C++ (`src/main/cpp`) | ✅ `awake-vulkan` |
| `:awake-engine-game` | `GenericGameApplication` — the backend-neutral game bootstrap (scene loading, fixed-timestep loop, UI/debug-line staging) `VulkanGameApplication`/`WebGpuGameApplication` both extend, instead of duplicating it (see Decision Log D19) | ❌ |
| `:awake-vulkan-generator` | Legacy bespoke C++ codegen — **being retired** (MVP Phase 1a) in favor of jni-binding-generator | ❌ |
| `:awake-demo:shared` | Sample app shared code (Vulkan triangle demo) | ❌ sample |
| `:awake-demo:androidApp` | Android sample entry point | ❌ sample |
| `:awake-demo:desktopApp` | Desktop sample entry point | ❌ sample |

## Project-specific rules

- **Regression gate:** the Android Vulkan triangle demo (`awake-demo`) must keep rendering
  after every change to `awake-vulkan` — it is the only working Vulkan backend today.
- The common Vulkan API in `awake-vulkan/src/commonMain` is the single source of truth;
  platform backends (`androidMain` JNI, `desktopMain` JNI, `iosMain` cinterop) implement it.
- Do not hand-edit generated C++ Accessor/Mutator files; regenerate via the binding generator.
- Engine modules do NOT follow the app 6-layer clean architecture
  (`:model/:api/:domain/:data/:presenter/:ui`) — that pattern is for app features, not engine
  subsystems. Do not restructure engine modules to match it.
- `.spv` shaders are compiled from GLSL via the `glslValidator` Gradle task (glslang).
- **Resource bundling rule**: a resource (shader, font atlas, etc.) that is identical across
  every consumer of a backend/engine module belongs in that module's own
  `src/<sourceSet>/resources/` (e.g. `awake-backend-vulkan/src/commonMain/resources`,
  `awake-backend-webgpu/src/wasmJsMain/resources`) — never copy-pasted into each consumer
  app's own resources. KMP already bundles a library module's resources transitively into
  every consumer (confirmed by a real Android resource-merge collision hit in this repo,
  see `docs/MVP_PLAN.md`'s scene-loader note) — duplicating the file instead just risks the
  copies drifting out of sync or a new consumer forgetting to add its own, which fails
  silently at compile time and only crashes at runtime (`readResourceBytes` throws). This
  does NOT apply to genuinely per-app content (each game's own 3D shaders, scenes, texture
  assets) — those are correctly consumer-owned and constructor-injected.
  **Known exception**: `awake-demo` applies the Compose Multiplatform plugin (unlike
  `sample-hello-cube`), and its wasmJs resource-aggregation didn't reliably pick up
  `awake-backend-webgpu`'s bundled shaders even after a full dev-server restart (a stale
  `wasmJsDevelopmentExecutableCompileSync` cache was the proximate cause, confirmed by
  forcing that exact task) — as a durable fix, `awake-demo/shared`'s own
  `wasmJsMain/resources` also carries copies of the shared WebGPU UI/debug-line shaders
  alongside its per-app `triangle.wgsl`. If a future consumer app also applies the Compose
  plugin and hits the same "resource not found (HTTP 404)" error for a backend-module
  shader, this is the first thing to check.

## Threading model

- **One thread owns every Vulkan call, per running app instance.** Vulkan objects
  (`VkDevice`, `VkQueue`, command buffers, etc.) are not free-threaded — do not call any
  `awake-vulkan`/`awake-core` rendering API, or touch a `GraphicsDevice`/`Renderer`/
  `Mesh`/`Texture`/`Material` instance, from a thread other than the one that owns it.
- **`Application.update(delta)` is that thread's entry point, and it's synchronous
  end-to-end.** One call does simulation (`SceneRuntimeHost.fixedUpdate`, via
  `FixedTimestepLoop`), rendering (`SceneRuntimeHost.render`), and the full Vulkan submit +
  present + `vkDeviceWaitIdle` (inside `Renderer.draw`) — all on the calling thread, in
  that order, before returning. There is no separate render thread *inside* the engine;
  the platform entry point supplies the one thread that drives everything.
- **Which thread that is, per platform:** Android's `VulkanView` spins a dedicated
  `Thread("VulkanView-Render")` and calls `AndroidGameLoop.startLoop { application.update
  (...) }` from it — never the UI thread, since `vkWaitForFences`/`vkQueuePresentKHR`
  block and `SurfaceView` has no render thread of its own. Desktop's Vulkan entry point
  (`VulkanDesktopMain.kt`) calls `DesktopGameLoop.startLoop { ... }` from `main()` directly
  — GLFW requires window/surface calls (`glfwPollEvents`, swapchain creation) to happen on
  the thread that created the window, so this has to be the main thread, not a spawned one.
- **Coroutines are for loading/IO only** (asset byte reads, glTF/scene JSON parsing,
  texture decode) **— never for anything that touches a Vulkan handle.** Decode off-thread
  if useful, but construct/mutate GPU-backed objects (`Mesh(...)`, `Texture(...)`,
  `Material.createResources(...)`) only on the render thread, after the off-thread work
  hands back plain data (bytes, parsed structs) with no Vulkan calls in it.
- **Input callbacks are not a separate thread.** GLFW fires callbacks synchronously during
  `glfwPollEvents()`, which already runs on the render thread (see above) — don't assume a
  callback could race a Vulkan call. Android touch events arrive on the UI thread, not the
  render thread; a touch handler that needs to affect game state must hand the event to
  the render thread (e.g. via a lock-free queue or `@Volatile` state it polls), not call
  into `World`/`Application` directly from `onTouchEvent`.

## API surface rules

- Never remove or rename public symbols without a major version bump
- Public API changes to published modules (`awake-core`, `awake-vulkan`, `awake-ecs`,
  `awake-scene`) require a CHANGELOG.md entry
- Mark internal symbols `internal` — keep the published API surface minimal

## Commands installed

See `.claude/commands/kmm-*.md`. Key commands:
- `/kmm-run-audit` — architecture audit with per-finding remediation
- `/kmm-harvest-lessons` — collect patterns to upstream to skills
- `/kmm-verify` — full validation pipeline (build + test)
- `/kmm-review-changes` — review git diff against architecture rules
- `/kmm-check-updates` — check for skill updates
