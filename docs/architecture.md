# Architecture

Awake is a Kotlin Multiplatform game engine library: a cross-platform graphics wrapper
(OpenGL working; Vulkan in progress) evolving toward a full KMP engine with ECS, a
Compose-style scene API, and a desktop editor.

## Read This With

- [docs/MVP_PLAN.md](/Users/ronvaldoz/StudioProjects/awaken/docs/MVP_PLAN.md) for the active roadmap
- [docs/tasks.md](/Users/ronvaldoz/StudioProjects/awaken/docs/tasks.md) for current work lanes
- [docs/reference/ui-ownership.md](/Users/ronvaldoz/StudioProjects/awaken/docs/reference/ui-ownership.md) for reusable UI boundaries
- [docs/reference/api-layering.md](/Users/ronvaldoz/StudioProjects/awaken/docs/reference/api-layering.md) for core/helper/sugar API classification
- [docs/reference/game-structure.md](/Users/ronvaldoz/StudioProjects/awaken/docs/reference/game-structure.md) for game state categories and folder ownership
- [docs/reference/ai-collaboration.md](/Users/ronvaldoz/StudioProjects/awaken/docs/reference/ai-collaboration.md) for agent-entrypoint and `skills/*` responsibilities

## Module Shape

```mermaid
flowchart TD
    base["awake:base"]
    core["awake:core"]
    ecs["awake:ecs"]
    scene["awake:scene"]
    sceneCore["awake:scene:core"]
    sceneControls["awake:scene:controls"]
    scenePhysics["awake:scene:physics"]
    sceneRendering["awake:scene:rendering"]
    sceneRuntime["awake:scene:runtime"]
    sceneDsl["awake:scene-dsl"]
    render["awake:engine:render-api"]
    game["awake:engine:game"]
    gameDsl["awake:engine:game-dsl"]
    physicsApi["awake:physics:api"]
    jolt["awake:backend:jolt"]
    uiCore["awake:engine:ui:ui-core"]
    uiWidgets["awake:engine:ui:ui-unstyled"]
    ui["awake:engine:ui"]
    uiDs["awake:engine:ui:ui-designsystem"]
    vulkan["awake:backend:vulkan"]
    webgpu["awake:backend:webgpu"]
    samples["samples:*"]

    core --> game
    base --> ecs
    base --> sceneCore
    base --> scene
    base --> render
    base --> physicsApi
    ecs --> sceneCore
    ecs --> scene
    sceneCore --> sceneRendering
    sceneCore --> scenePhysics
    sceneCore --> sceneControls
    sceneRendering --> sceneControls
    sceneCore --> sceneRuntime
    sceneRendering --> sceneRuntime
    render --> sceneRuntime
    game --> sceneRuntime
    uiCore --> sceneRuntime
    render --> sceneRendering
    physicsApi --> scenePhysics
    sceneCore --> scene
    scenePhysics --> scene
    sceneRendering --> scene
    sceneControls --> scene
    sceneRuntime --> scene
    sceneCore --> sceneDsl
    sceneRendering --> sceneDsl
    sceneControls --> sceneDsl
    sceneRuntime --> sceneDsl
    uiCore --> sceneDsl
    render --> game
    scene --> game
    game --> gameDsl
    sceneDsl --> gameDsl
    physicsApi --> jolt
    uiCore --> uiWidgets
    uiWidgets --> ui
    ui --> uiDs
    game --> samples
    render --> vulkan
    render --> webgpu
    vulkan --> samples
    webgpu --> samples
```

## Module Graph

| Module | Purpose | Published |
|---|---|---|
| `:awake-base` | Dependency-free portable core: math, `Input`, `FixedTimestepLoop`, glTF parsing, bitmap/resource I/O | `awake-base` |
| `:awake-core` | Backend-agnostic app-lifecycle glue: `Application`, `GameLoop`, `VulkanView`, `EngineConfig` | `awake-core` |
| `:awake-opengl` | Legacy OpenGL rendering backend | `awake-opengl` |
| `:awake-ecs` | Sparse-set ECS runtime: entities, stores, queries, systems | `awake-ecs` |
| `:awake-scene` | Published compatibility facade for scene runtime/components while scene internals split by capability | `awake-scene` |
| `:awake-scene:core` | Scene core components/systems such as `Transform`/`Name`/`TransformSystem`, plus generic entity-rotation `SpinControl`/`SpinSystem`; first internal split behind `awake-scene` | not published |
| `:awake-scene:physics` | Physics-facing scene components and systems: `PhysicsBody`, `PhysicsSystem` | not published |
| `:awake-scene:rendering` | Render-facing scene components and systems: `Camera`, `Light`, `MeshRenderer`, `RenderSystem` | not published |
| `:awake-scene:controls` | Reusable camera/movement control components and systems: `CameraComponent` (with its `CameraMode` enum) plus the `ActiveCamera` tag, `MovementControl`, and the `CameraSystem`, `CameraInputSystem`, `MatrixRelativeMovementSystem`, `PlayerInputSystem` systems | not published |
| `:awake-scene:runtime` | `SceneGameRuntime`/`SceneGameSpec`/`SceneRouterSpec`, the scene document model (`SceneDocument`, `SceneLoader`, `SceneValidator`, `SceneInstantiationAdapter`), and `SceneAssetLibrary` -- moved as one unit since `SceneGameSpec` couples the runtime and document model directly | not published |
| `:awake-scene-dsl` | Authored scene DSL (`sceneGame { ... }`, entities/assets/systems) on top of the scene leaf modules (`core`/`rendering`/`controls`/`runtime`), not the `awake:scene` facade | not published |
| `:awake-backend:vulkan` | Vulkan KMP bindings and JNI bridge | `awake-vulkan` |
| `:awake-engine:game` | Backend-neutral game bootstrap and runtime glue | not published |
| `:awake-engine:game-dsl` | Authored `game { ... }`/`gameSpec { ... }` entrypoint for assembling a game from `awake:engine:game` contracts | not published |
| `:awake-engine:render-api` | Renderer-facing abstractions and draw orchestration | not published |
| `:awake-physics:api` | Backend-agnostic physics contracts: `PhysicsWorld`, `BodyHandle`, `BodyTransform`, `PhysicsShape`, `MotionType`, `RaycastHit` | not published |
| `:awake-backend:jolt` | Jolt Physics binding (JNI on desktop/Android via `jolt-jni`, JoltC cinterop on iOS) implementing `awake:physics:api` | not published |
| `:awake-engine:ui:ui-core` | Foundational UI drawing and layout primitives | not published |
| `:awake-engine:ui:ui-unstyled` | Reusable widget-level primitives built on `ui-core` | not published |
| `:awake-engine:ui` | Style-agnostic UI composition templates and DSL surfaces | not published |
| `:awake-engine:ui:ui-designsystem` | Branded or strongly opinionated UI recipes | not published |
| `:samples:*` | Sample applications and demos | sample-only |

## Stable Rules

### Engine Boundaries

- Engine modules do not follow the app-style `:model/:api/:domain/:data/:presenter/:ui`
  clean-architecture split.
- Keep core engine layers API-agnostic: rendering internals stay out of generic runtime
  facades, and sample-only logic stays out of reusable engine modules.
- The common Vulkan API in `awake-backend:vulkan/src/commonMain` is the single source of truth.

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
- `Application.update(delta)` is synchronous end-to-end on that owning thread.
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
