# Architecture

Awake is a Kotlin Multiplatform game engine library: a Vulkan-first cross-platform renderer
(Android/iOS/desktop, with WebGPU covering wasmJs) evolving toward a full KMP engine with ECS,
a Compose-style scene API, an immediate-mode UI stack, and a desktop editor.

## Read This With

- [docs/MVP_PLAN.md](/Users/ronvaldoz/StudioProjects/awaken/docs/MVP_PLAN.md) for the active roadmap
- [docs/tasks.md](/Users/ronvaldoz/StudioProjects/awaken/docs/tasks.md) for current work lanes
- [docs/reference/ui-ownership.md](/Users/ronvaldoz/StudioProjects/awaken/docs/reference/ui-ownership.md) for reusable UI boundaries
- [docs/reference/api-layering.md](/Users/ronvaldoz/StudioProjects/awaken/docs/reference/api-layering.md) for core/helper/sugar API classification
- [docs/reference/game-structure.md](/Users/ronvaldoz/StudioProjects/awaken/docs/reference/game-structure.md) for game state categories and folder ownership
- [docs/reference/ai-collaboration.md](/Users/ronvaldoz/StudioProjects/awaken/docs/reference/ai-collaboration.md) for agent-entrypoint and `skills/*` responsibilities

## Module Shape

Arrows point from a module to the modules that depend on it. Only `main`-source Gradle
dependencies are drawn; test-only edges (mostly into `awake:ui:testing`) and the standalone
tooling/benchmark modules are listed in the table instead.

```mermaid
flowchart TD
    core["awake:core"]
    geometry["awake:core:geometry"]
    coreAnim["awake:core:animation"]
    ecs["awake:ecs"]

    subgraph assets["awake:asset"]
        gltf["gltf"]
        shaders["shaders"]
    end

    subgraph engine["awake:engine"]
        renderContract["render:contract"]
        renderPasses["render:passes"]
        platform["platform"]
        platformAuthoring["platform-authoring"]
        engineApp["app"]
    end

    subgraph sceneModules["awake:scene"]
        sceneCore["scene-core"]
        sceneRendering["rendering"]
        scenePhysics["physics"]
        sceneControls["controls"]
        sceneRuntime["runtime"]
        sceneAuthoring["authoring"]
        scene["awake:scene (facade)"]
    end

    subgraph uiModules["awake:ui"]
        uiGraphics["graphics<br/>contract values"]
        uiText["text"]
        uiCore["ui-core<br/>runtime mechanics"]
        uiAnimation["animation"]
        uiHeroicons["heroicons"]
        uiHeadless["headless<br/>generic behavior + neutral visuals"]
        uiTailwind["tailwind"]
        uiDesignsystem["designsystem<br/>branded recipes + variants"]
    end

    subgraph backends["awake:backend"]
        vkBindings["vulkan:bindings"]
        vkAndroidNative["vulkan:bindings:android-native"]
        vulkan["vulkan"]
        webgpu["webgpu"]
        jolt["jolt"]
    end

    physicsApi["awake:physics:api"]
    samples["samples:*"]

    core --> coreAnim
    core --> gltf
    geometry --> gltf
    coreAnim --> gltf
    core --> shaders
    renderContract --> shaders
    core --> physicsApi
    core --> jolt
    physicsApi --> jolt

    core --> renderContract
    uiCore --> renderContract
    renderContract --> renderPasses
    core --> platform
    renderContract --> platform
    uiCore --> platform
    platform --> platformAuthoring
    uiHeadless --> platformAuthoring
    platform --> engineApp
    renderContract --> engineApp
    shaders --> engineApp
    vulkan --> engineApp
    webgpu --> engineApp

    core --> sceneCore
    ecs --> sceneCore
    sceneCore --> sceneRendering
    renderContract --> sceneRendering
    sceneCore --> sceneControls
    sceneRendering --> sceneControls
    sceneCore --> scenePhysics
    physicsApi --> scenePhysics
    core --> sceneRuntime
    ecs --> sceneRuntime
    sceneCore --> sceneRuntime
    sceneRendering --> sceneRuntime
    renderContract --> sceneRuntime
    platform --> sceneRuntime
    uiCore --> sceneRuntime
    uiHeadless --> sceneRuntime
    sceneCore --> sceneAuthoring
    sceneRendering --> sceneAuthoring
    sceneControls --> sceneAuthoring
    sceneRuntime --> sceneAuthoring
    platformAuthoring --> sceneAuthoring
    uiCore --> sceneAuthoring
    core --> scene
    ecs --> scene
    renderContract --> scene
    physicsApi --> scene
    sceneCore --> scene
    sceneRendering --> scene
    scenePhysics --> scene
    sceneControls --> scene
    sceneRuntime --> scene
    sceneAuthoring --> scene

    core --> uiGraphics
    core --> uiText
    uiGraphics --> uiText
    core --> uiCore
    uiGraphics --> uiCore
    uiText --> uiCore
    core --> uiAnimation
    uiCore --> uiAnimation
    uiGraphics --> uiHeroicons
    uiCore --> uiHeroicons
    core --> uiHeadless
    uiGraphics --> uiHeadless
    uiText --> uiHeadless
    uiCore --> uiHeadless
    uiAnimation --> uiHeadless
    uiHeroicons --> uiHeadless
    core --> uiTailwind
    uiGraphics --> uiTailwind
    uiHeadless --> uiTailwind
    uiCore --> uiDesignsystem
    uiHeadless --> uiDesignsystem
    uiHeroicons --> uiDesignsystem
    uiTailwind --> uiDesignsystem

    core --> vkBindings
    vkAndroidNative --> vkBindings
    core --> vulkan
    uiCore --> vulkan
    renderContract --> vulkan
    renderPasses --> vulkan
    platform --> vulkan
    shaders --> vulkan
    vkBindings --> vulkan
    core --> webgpu
    uiCore --> webgpu
    renderContract --> webgpu
    renderPasses --> webgpu
    platform --> webgpu
    shaders --> webgpu
    scene --> webgpu

    core --> samples
    coreAnim --> samples
    ecs --> samples
    gltf --> samples
    shaders --> samples
    renderContract --> samples
    platformAuthoring --> samples
    scene --> samples
    sceneAuthoring --> samples
    uiCore --> samples
    uiHeroicons --> samples
    uiDesignsystem --> samples
    vulkan --> samples
    webgpu --> samples
```

## Module Graph

| Module | Purpose | Published |
|---|---|---|
| `:awake:core` | Dependency-free portable core: math (`Vector`/`Matrix`/`Quat`/`Camera`/`Frustum`/`Aabb`), `Input`, `GameLoop`/`FixedTimestepLoop`/`EngineConfig`, `WindowApplication`, bitmap/resource I/O, `Color` | yes |
| `:awake:core:geometry` | Dependency-free mesh geometry utilities: `MeshSimplifier`, `NormalizedInt` vertex packing | yes |
| `:awake:core:animation` | Skeletal animation data and sampling: `AnimationClip`, `AnimationPose`, `AnimationCrossfade`, `Skeleton`, `Skin` | yes |
| `:awake:asset:gltf` | glTF/GLB parsing and asset import, split out of `awake:core` | yes |
| `:awake:asset:mesh-optimizer` | Standalone JVM CLI that pre-simplifies glTF meshes offline via `core:geometry` | not published |
| `:awake:asset:shaders` | Shared engine shader sources plus `ShaderSet`/`ShaderStages` and the engine uniform layouts (`TexturedUniformLayout`, `LitShadowUniformLayout`) | yes |
| `:awake:ecs` | Sparse-set ECS runtime: entities, stores, queries, systems | yes |
| `:awake:ecs:benchmark` | Standalone JVM kotlinx-benchmark harness for ECS/scene/Vulkan hot paths | not published |
| `:awake:scene` | Published compatibility facade re-exporting the scene leaf modules while scene internals stay split by capability | yes |
| `:awake:scene:scene-core` | Scene core components/systems such as `Transform`/`Name`/`TransformSystem`, plus generic entity-rotation `SpinControl`/`SpinSystem`; first internal split behind `awake:scene` | not published |
| `:awake:scene:physics` | Physics-facing scene components and systems: `PhysicsBody`, `PhysicsSystem` | not published |
| `:awake:scene:rendering` | Render-facing scene components and systems: `Camera`, `Light`, `MeshRenderer`, `RenderSystem` | not published |
| `:awake:scene:controls` | Reusable camera/movement control components and systems: `CameraComponent` (with its `CameraMode` enum) plus the `ActiveCamera` tag, `MovementControl`, and the `CameraSystem`, `CameraInputSystem`, `MatrixRelativeMovementSystem`, `PlayerInputSystem` systems | not published |
| `:awake:scene:runtime` | `SceneGameRuntime`/`SceneGameSpec`/`SceneRouterSpec`, the scene document model (`SceneDocument`, `SceneLoader`, `SceneValidator`, `SceneInstantiationAdapter`), and `SceneAssetLibrary` -- moved as one unit since `SceneGameSpec` couples the runtime and document model directly | not published |
| `:awake:scene:authoring` | Authored scene DSL (`sceneGame { ... }`, entities/assets/systems) on top of the scene leaf modules (`scene-core`/`rendering`/`controls`/`runtime`) and `engine:platform-authoring`, not the `awake:scene` facade | not published |
| `:awake:engine:render:contract` | Renderer-facing abstractions: `Renderer`, `DrawCall`, `Mesh`/`MeshGeometry`/`VertexFormat`, `Material`, `TextureAsset`/`RenderTarget`/`MipChain`, `SceneLight`, `UniformLayout`, `RenderViewport` | not published |
| `:awake:engine:render:passes` | Backend-shared pass and recording pieces built on the contract: `CommandRecorder`, `PreparedDraw`, `SharedOpaqueRenderFeature` | not published |
| `:awake:engine:platform` | Backend-neutral app/lifecycle glue: the `GraphicsEngine` base class both backends extend, `AppSpec`/`AppModule`/`AppLifecycle`/`AwakeAppLifecycle`, `WindowConfig`, `FrameStats`, and the Android `VulkanView` | not published |
| `:awake:engine:platform-authoring` | Authored `game { ... }` entrypoint (`GameDsl`, `GameModuleDsl`, `GameUiDsl`) assembling an `AppSpec` from `awake:engine:platform` contracts, plus the UI runtime and perf-overlay wiring | not published |
| `:awake:engine:app` | `expect class AwakeApplication`, the per-target alias picking `VulkanEngine` (android/ios/desktop) or `WebGpuEngine` (wasmJs) so `commonMain` never imports a backend module; no in-repo consumer yet -- samples construct the backend engine directly | not published |
| `:awake:backend:vulkan` | Vulkan renderer: `VulkanEngine`, `Renderer`/`RendererDraw3D`/`RendererDrawUi`, pipelines and render features | not published |
| `:awake:backend:vulkan:bindings` | Vulkan KMP API surface plus the JNI (android/desktop) and cinterop (iOS) bridge | not published |
| `:awake:backend:vulkan:bindings:android-native` | Android CMake/NDK module holding the generated JNI Accessor/Mutator C++; no Kotlin plugin | not published |
| `:awake:backend:vulkan:generator` | Standalone JVM CLI that generates the Vulkan Kotlin bindings and their JNI Accessor/Mutator C++ | not published |
| `:awake:backend:webgpu` | WebGPU renderer on wgpu4k (`WebGpuEngine`); wasmJs is its only target | not published |
| `:awake:physics:api` | Backend-agnostic physics contracts: `PhysicsWorld`, `BodyHandle`, `BodyTransform`, `PhysicsShape`, `MotionType`, `RaycastHit` | not published |
| `:awake:backend:jolt` | Jolt Physics binding (JNI on desktop/Android via `jolt-jni`, JoltC cinterop on iOS) implementing `awake:physics:api` | not published |
| `:awake:ui:graphics` | Runtime-free contract values: `UiBounds`, dimensions/units, and color/shape contracts (the "ui-api" role; no separate `ui-api` module exists) | not published |
| `:awake:ui:text` | Font and text contracts: `UiFont`/`BitmapFont`/`MsdfFont`/`PackedUiFont`, `GlyphAtlasSource`, `TextStyle`, `FontWeight` | not published |
| `:awake:ui:ui-core` | UI runtime mechanics: layout/drawing/input/state, modifiers, scopes, and neutral fallback resolution; no component recipes or variants | not published |
| `:awake:ui:animation` | UI tween/transition primitives: `UiAnimation`, `UiTransition`, `UiAnimatedVisibility`, `UiPopup` | not published |
| `:awake:ui:heroicons` | Generated Heroicons `UiImageVector` icon set | not published |
| `:awake:ui:headless` | Reusable widget behavior and neutral visual-state contracts built on `ui-core`; no named variants or design language | not published |
| `:awake:ui:tailwind` | Tailwind token scales (`Tw`, `OklchColor`, `TwLayout`, `TwInsets`, `TwModifiers`) consumed by the design system | not published |
| `:awake:ui:designsystem` | Branded themes, named variants, and `shadcn*` recipes that map to Headless neutral visual states | not published |
| `:awake:ui:testing` | Shared test harness: `AwakeUiSnapshot`/`UiRasterizer`/`AwakeUiPreview`, `PixelBaseline`/`TimingBaseline`, `NoopRenderer`; consumed from test source sets only | not published |
| `:awake:ui:tailwind-generator` | Standalone JVM CLI generating the `ui:tailwind` token sources | not published |
| `:awake:ui:font-atlas-generator` | Standalone JVM CLI generating packed font atlases for `ui:text` | not published |
| `:awake:ui:benchmark` | Standalone JVM kotlinx-benchmark harness for UI layout/draw hot paths | not published |
| `:samples:ui-showcase` | Component gallery sample exercising the design system across Vulkan and WebGPU targets | sample-only |
| `:samples:studio` | Editor-shell sample: scene hierarchy, inspector, viewport docking | sample-only |
| `:samples:server` | Standalone JVM Ktor WebSocket debug-control channel for driving a desktop sample deterministically; no engine dependencies | sample-only |

## Stable Rules

### Engine Boundaries

- Engine modules do not follow the app-style `:model/:api/:domain/:data/:presenter/:ui`
  clean-architecture split.
- Keep core engine layers API-agnostic: rendering internals stay out of generic runtime
  facades, and sample-only logic stays out of reusable engine modules.
- The common Vulkan API in `awake:backend:vulkan:bindings/src/commonMain` is the single source of
  truth.

### Generated And Native Code

- Do not hand-edit generated JNI Accessor/Mutator C++ files; regenerate them through the
  binding generator.
- Native-resource lifetime must remain symmetric: every create/allocation path needs the
  matching destroy/free path in the correct order.

### Resource Ownership

- A resource identical for every consumer of a backend or engine module belongs in that
  module's own `src/<sourceSet>/resources/`.
- Per-game content such as scenes, game shaders, textures, and authored assets stays in the
  consumer or sample module.
- If a Compose plugin packaging quirk forces a consumer-local duplicate for a backend shader,
  document the exception in a task note and keep the backend copy authoritative.

### Threading Model

- One thread owns every Vulkan call for a running app instance.
- `WindowApplication.update(delta)` is synchronous end-to-end on that owning thread.
- Desktop Vulkan runs on the main thread that created the window.
- Android Vulkan runs on its dedicated render thread, not the UI thread.
- Coroutines may help with IO and parsing, but must not touch Vulkan handles directly.

### Testing Posture

- GPU resource creation is not a useful target for fake-heavy app-style test doubles.
- Push parsing, packing, math, and protocol logic into small pure functions so those parts are
  straightforward to unit test.
- For visual Vulkan desktop paths, prefer headless pixel-baseline coverage when the path is
  testable.

### API Surface

- Never remove or rename public symbols in published modules without a major version bump.
- Public API changes to published modules require a changelog update.
- Keep internals `internal` unless they are intentional public surface.

## Validation Guardrails

- The Android Vulkan sample remains the regression gate for backend work.
- For rendering changes on desktop Vulkan, prefer headless pixel-baseline tests over manual
  screenshot-only verification when the path is testable.
