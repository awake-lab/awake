# API Layering Plan

Date: 2026-08-05

## Objective

Separate Awake's ECS and scene-facing API into clear **core**, **helper**, and **sugar**
layers before adding more convenience APIs or splitting scene modules.

This plan follows the durable rule in
[docs/reference/api-layering.md](../reference/api-layering.md).

## Why This Exists

Recent scene work exposed a boundary smell: scheduling policy lived on
`awake-ecs` `System` even though scheduling is owned by the scene/game runtime. That made the
core API look convenient, but it mixed generic ECS behavior with scene lifecycle policy.

The cleanup direction is:

```text
System = behavior
SceneSystemPhase = schedule bucket
fixedSystem/frameSystem = scene DSL registration helpers
```

This keeps `awake-ecs` reusable outside scene rendering while still giving demos a pleasant
authoring surface.

## Current Classification

| API                                    | Classification         | Action                                                                                                   |
|----------------------------------------|------------------------|----------------------------------------------------------------------------------------------------------|
| `Entity`, `World`, `System`            | Core                   | Keep in `:awake:ecs`                                                                                     |
| `World.family(...)`, `queryEach(...)`  | Core/helper boundary   | Keep in `:awake:ecs`; watch allocation and naming                                                        |
| `SystemFrequency`                      | Removed core leakage   | Replace with scene-owned scheduling                                                                      |
| `SceneSystemPhase`                     | Scene core             | Now in `:awake:scene:runtime` via the `:awake:scene` facade                                              |
| `fixedSystem(...)`, `frameSystem(...)` | Scene DSL helper/sugar | Keep in `:awake:scene-dsl`                                                                               |
| `TransformSystem`, `RenderSystem`      | Scene runtime systems  | Split landed: `TransformSystem` in `:awake:scene:scene-core`, `RenderSystem` in `:awake:scene:rendering` |
| `cameraEntity(...)`, `meshEntity(...)` | Sugar                  | Keep in `:awake:scene-dsl`                                                                               |
| Ashley-style `EntitySystem`            | Not accepted yet       | Do not add until repeated boilerplate proves a helper is needed                                          |

## Phase 2 Audit Snapshot

### `:awake:ecs`

Overall status: mostly healthy. The scheduler cleanup moved timing policy back out of the
generic ECS layer. Remaining work is about separating stable core from advanced/performance
helpers.

| Surface                                                                        | Layer                    | Recommendation                                                                                                                                                  |
|--------------------------------------------------------------------------------|--------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `Entity`, `World`, `System`                                                    | Core                     | Keep public and boring. These are the durable ECS contract.                                                                                                     |
| `World.create/destroy/isAlive/clear`                                           | Core                     | Keep public.                                                                                                                                                    |
| `World.add/get/remove/has` with `KClass`                                       | Core                     | Keep public. This is the explicit non-sugar path.                                                                                                               |
| Reified `World.add/get/remove/has/spawn/query/family`                          | Helper                   | Keep public; document as ergonomic sugar over explicit `KClass` overloads.                                                                                      |
| `World.family(...)`, `Family1`, `Family2`, `Family`                            | Core/helper boundary     | Keep public; these are central to efficient system authoring. Watch naming if more arities appear.                                                              |
| `FamilySpec`, `FamilySpecBuilder`                                              | Helper                   | Keep public for arbitrary-arity/`one`/`exclude` queries.                                                                                                        |
| `Poolable`, `registerPool`, pooled `add<T>()`                                  | Helper                   | Keep public. This is a deliberate performance convenience.                                                                                                      |
| `World.replace/ensure/toggle`                                                  | Helper                   | Keep public for now, but document as convenience helpers rather than core.                                                                                      |
| `ComponentStore`, `World.store(...)`                                           | Advanced/leaky helper    | Do not expand. Consider moving to an `unsafe`/advanced package or making this internal before stable publish if no external consumer needs direct store access. |
| `ComponentTypeId`, `World.typeId(...)`, `add/get/remove(..., ComponentTypeId)` | Advanced hot-path helper | Keep only if benchmark-critical and documented as advanced. Otherwise consider internalizing to avoid exposing storage internals.                               |
| `QueryCache` / `QueryCollector`                                                | Internal/stale-adjacent  | Not public, but still conceptually overlaps with maintained families. Revisit during performance cleanup; do not expose.                                        |

Key decision: no `EntitySystem` base class yet. Awake's current equivalent remains
`System` plus `World.family/queryEach`, with schedule selection owned by the runtime.

### `:awake:scene`

Overall status: useful but mixed. This module currently contains scene runtime core,
rendering integration, physics integration, controls, navigation, and a legacy/simple
runtime. That is fine for iteration, but it should not be treated as final architecture.

| Surface                                                                                          | Layer                             | Recommendation                                                                                                                                                                                                                                                                                       |
|--------------------------------------------------------------------------------------------------|-----------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `Transform`, `Name`                                                                              | Scene core                        | Keep in scene core.                                                                                                                                                                                                                                                                                  |
| `SceneDocument`, `SceneNode`, `SceneTransform`, `SceneCamera`, `SceneLight`, `SceneMeshRenderer` | Scene core/serialization          | Moved to `:awake:scene:runtime` alongside `SceneGameRuntime` (not `:awake:scene:scene-core`) -- `SceneGameSpec` couples the document model and runtime directly. A future `:awake:scene:scene-core` extraction remains possible once that coupling is loosened.                                      |
| `SceneLoader`, `SceneValidator`, `SceneInstantiationAdapter`, `AwakeWorldSceneAdapter`           | Scene core/helper                 | Moved to `:awake:scene:runtime`, same reasoning as `SceneDocument` above.                                                                                                                                                                                                                            |
| `SceneGameRuntime`, `SceneGameSpec`, `SceneSystemPhase`, `SceneSystemHandle`                     | Scene runtime core                | Moved to `:awake:scene:runtime` behind the `:awake:scene` facade.                                                                                                                                                                                                                                    |
| `SceneAssetLibrary`, mesh/material factory typealiases                                           | Runtime helper/rendering boundary | Moved to `:awake:scene:runtime` -- `SceneGameSpec.assetLibraryFactory` couples it to the runtime directly.                                                                                                                                                                                           |
| `Camera`, `Light`, `MeshRenderer`, `RenderSystem`                                                | Rendering                         | Moved to `:awake:scene:rendering` behind the `:awake:scene` facade. Depends on render-api and should not define generic scene core.                                                                                                                                                                  |
| `TransformSystem`                                                                                | Scene core/render preparation     | Moved to `:awake:scene:scene-core` behind the `:awake:scene` facade -- the rendering split happened, so per this row's own original condition, transform propagation now lives in core alongside `Transform`.                                                                                        |
| `PhysicsBody`, `PhysicsSystem`                                                                   | Physics                           | Moved to `:awake:scene:physics` behind the `:awake:scene` facade; depends on `awake:physics:api`.                                                                                                                                                                                                    |
| `OrbitControl`, `FreeFlyControl`, `FollowControl`, `MovementControl`                             | Controls                          | Moved to `:awake:scene:controls` behind the `:awake:scene` facade.                                                                                                                                                                                                                                   |
| `OrbitCameraSystem`, `FreeFlyCameraSystem`, `FollowCameraSystem`                                 | Controls                          | Moved to `:awake:scene:controls` behind the `:awake:scene` facade.                                                                                                                                                                                                                                   |
| `PlayerControlSystem`                                                                            | Controls/UI boundary smell        | Moved into `:awake:scene-dsl` directly (not `:awake:scene:controls`, not `:awake:scene`) -- depends on hardware input plus UI input ownership (`ui-core`'s `UiInputOwnership`), and `:awake:scene-dsl` is the "DSL/sample tooling" home this row's own original recommendation named.                |
| `PlayerMovementSystem`, `ChaseAiSystem`                                                          | Authored gameplay                 | Moved to `samples:scene3d-playground` gameplay systems. Do not keep scenario-specific systems in scene core.                                                                                                                                                                                         |
| `NavMesh`                                                                                        | Navigation                        | Keep as a tiny contract for now; it may deserve a small navigation contract module later.                                                                                                                                                                                                            |
| `createScene3DDemoNavMesh()`                                                                     | Sample-local                      | Moved to `samples:scene3d-playground`; no longer reusable scene API.                                                                                                                                                                                                                                 |
| `DemoNavMeshGeometry`                                                                            | Sample-local                      | Moved to `samples:scene3d-playground`; no longer reusable scene code.                                                                                                                                                                                                                                |
| `SceneRuntime`                                                                                   | Legacy/simple runtime             | Stays in `:awake:scene` (not `:awake:scene:runtime`) -- it depends on `TransformSystem`, which hasn't split out of `:awake:scene` yet; moving it would create a circular module dependency for a class already marked `@Deprecated`. Mark for removal once `SceneGameRuntime` is the canonical path. |
| `SceneRouterSpec`, `SceneRouterRuntime`                                                          | Runtime helper                    | Moved to `:awake:scene:runtime` alongside `SceneGameRuntime`. Still experimental/helper; needs stronger lifecycle story because routed `ready()` cannot suspend after startup.                                                                                                                       |

Key decision: split scene by capability only after moving demo/gameplay-specific concepts out
or clearly marking them as experimental.

### `:awake:scene-dsl`

Overall status: this is correctly the sugar/authoring layer. The main risk is that it
currently installs mandatory transform/render systems implicitly, so docs and tests must keep
that behavior visible.

| Surface                                                                                                     | Layer                          | Recommendation                                                                                                              |
|-------------------------------------------------------------------------------------------------------------|--------------------------------|-----------------------------------------------------------------------------------------------------------------------------|
| `sceneGame { ... }`, `GameSpecDsl.scene/ecs`                                                                | Sugar/composition              | Keep as primary authoring entrypoint for scene-backed games.                                                                |
| `SceneGameDsl.scene/name/entity/assets/renderables`                                                         | Sugar/helper                   | Keep. Provides non-source-file scene authoring.                                                                             |
| `fixedSystem(...)`, `frameSystem(...)`, `system(name, phase, ...)`                                          | Helper/sugar                   | Keep. Explicit schedule registration is the right shape.                                                                    |
| `update { ... }`, `overlay { ... }`, `onReady`, `onDispose`                                                 | Runtime sugar                  | Keep, but docs must explain fixed-step vs frame timing.                                                                     |
| `SceneAssetsDsl`                                                                                            | Sugar over `SceneAssetLibrary` | Keep.                                                                                                                       |
| Document DSL builders under `scene.runtime.dsl`                                                             | Sugar                          | Keep as the canonical document DSL package.                                                                                 |
| Compatibility `scene(...)` in `scene.runtime`                                                               | Stale/deprecated               | Keep temporarily; remove after migration window.                                                                            |
| `cameraEntity(...)`, `meshEntity(...)`                                                                      | Sugar                          | Keep. They are common authored-scene shortcuts.                                                                             |
| `orbitCameraSystem(...)`, `freeFlyCameraSystem(...)`, `followCameraSystem(...)`, `playerControlSystem(...)` | Sugar over controls            | Keep temporarily, but if controls move to `scene:controls`, these helpers should move with them or become optional imports. |
| `installInfrastructureSystems()`                                                                            | Internal sugar/runtime glue    | Keep internal. Its behavior must remain tested because duplicate render registration caused blinking.                       |

### `samples:scene3d-playground`

Overall status: good as a sample, not as reusable API. It is already mostly `internal`,
which is healthy.

| Surface                                                 | Layer                       | Recommendation                                                                          |
|---------------------------------------------------------|-----------------------------|-----------------------------------------------------------------------------------------|
| `scene3DPlayground()` / `scene3DPlaygroundSpec()`       | Sample public entrypoint    | Keep public for launchers/tests.                                                        |
| `scene3DPlaygroundModule()`                             | Sample composition          | Keep internal.                                                                          |
| `Scene3DDemo`, `Scene3DDemos`, `Scene3DPlaygroundState` | Sample-local helper         | Keep internal. This is a sample catalog pattern, not engine API yet.                    |
| `Scene3DDemoDriverSystem`                               | Sample-local frame driver   | Keep private/internal. Do not promote until multiple samples need this exact lifecycle. |
| `OrbitCameraController`, `ManualTimeController`         | Sample-local helper         | Keep internal; consider extraction only if reused by multiple demos.                    |
| Rotating cube mesh/debug geometry                       | Sample-local asset/helper   | Keep internal.                                                                          |
| glTF/skinned demo preload and render code               | Sample-local demo logic     | Keep internal; use it to inform future asset/animation APIs, not as API itself.         |
| UI overlay/chrome                                       | Sample-local UI composition | Keep internal; reusable widgets belong in UI modules, not this sample.                  |

## Phase 2 Findings

1. `awake:ecs` is now conceptually clean enough to keep stable, but `ComponentStore` and
   `ComponentTypeId` are advanced/hot-path APIs that need an explicit "advanced" story before
   stable publishing.
2. `awake:scene` is the next debt pocket. It mixes core scene contracts with rendering,
   physics, controls, navigation, and gameplay-ish systems.
3. The immediate split should not be a blind module move. Demo navmesh bootstrap and
   geometry are now sample-owned. Authored gameplay systems (`ChaseAiSystem`,
   `PlayerMovementSystem`) moved with the sample so future module boundaries are not
   polluted.
4. `scene-dsl` is correctly sugar, but its implicit built-in systems need continuous tests
   because the duplicate-render bug came from misunderstanding that hidden registration.
5. The Ashley-style `EntitySystem` idea remains deferred. A future `FamilySystem*` helper is
   acceptable only if repeated real systems prove the boilerplate is worth an abstraction.

## Implementation Plan

### Phase 1 — Stabilize The Scheduling Cleanup

- Keep `awake-ecs` `System` scheduler-free.
- Keep scene scheduling in `SceneSystemPhase`.
- Use `fixedSystem` for deterministic simulation/physics systems.
- Use `frameSystem` for camera, transform, render, UI-adjacent, and demo-driver systems.
- Keep the scene3d duplicate-render/blink regression protected by tests.

Exit criteria:

- Detekt passes.
- Scene DSL tests prove frame systems run once per rendered frame even when fixed systems run
  zero steps.
- `SystemFrequency` has no source references outside changelog/history.

### Phase 2 — API Classification Audit

Audit ECS and scene public APIs and assign each symbol to one of:

- core
- helper
- sugar
- sample-local
- stale/deprecated

Targets:

- `awake/ecs/src/commonMain`
- `awake/scene/src/commonMain`
- `awake/scene-dsl/src/commonMain`
- `samples/scene3d-playground/src/commonMain`

Output:

- Done in the Phase 2 audit snapshot above.
- Promote the stable parts into [docs/reference/api-layering.md](../reference/api-layering.md)
  after the next source cleanup confirms the classifications.

### Phase 3 — Helper Extraction Before Module Splits

Only after classification, identify repeated helper patterns:

- family iteration boilerplate
- common scene entity builders
- camera/control setup
- demo activation/deactivation lifecycle

Possible helper candidates:

```kotlin
// Only if repetition proves it is worth it.
abstract class FamilySystem2<A : Any, B : Any> : System
```

Do not add helpers just to imitate Ashley, Bevy, Unity, or Flecs. Borrow the useful design
principle, not the whole surface.

### Phase 4 — Scene Module Split Proposal

Prepare a separate proposal before moving files:

```text
:awake:scene:scene-core
:awake:scene:rendering
:awake:scene:physics
:awake:scene:controls
:awake:scene-dsl or :awake:scene:authoring
```

The proposal must answer:

- Which public packages move?
- Which modules stay published?
- Which dependencies are allowed?
- Which samples prove the split?
- What is the migration path for users?

## Guardrails

- Do not move scene concepts into `awake-ecs`.
- Do not put renderer-specific behavior into generic runtime facades.
- Do not add sugar without a non-sugar path.
- Do not put authored gameplay systems in reusable engine modules. Engine systems must be
  generic, configurable, and reusable across more than one game/sample.
- Do not remove or rename published public symbols without changelog/release-note treatment.
- Do not split modules until the API classification is clear.

## Next Concrete Step

The first source cleanup from the Phase 2 audit has started:

1. Keep `awake:ecs` unchanged except possibly documenting/renaming advanced APIs.
2. Demo-specific navmesh bootstrap now lives in `samples:scene3d-playground`.
3. `SceneRuntime` is deprecated in favor of `SceneGameRuntime`/`sceneGame {}`.
4. Authored gameplay systems moved to `samples:scene3d-playground`.
5. Scene split proposal drafted in
   [docs/tasks/2026-08-05-scene-module-split-proposal.md](2026-08-05-scene-module-split-proposal.md).
