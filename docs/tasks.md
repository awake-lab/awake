# Tasks

## Current Objective

Stabilize Awake's public API boundaries before the next core/scene split. The current focus
is separating core APIs, reusable helpers, and authoring sugar so future module moves do not
just spread unclear ownership across more folders.

## Active Phase

- 2026-08-20: Architecture-governance standardization is drafted in
  [2026-08-20-architecture-governance-standardization-plan](tasks/2026-08-20-architecture-governance-standardization-plan.md).
  It first restores truthful module/task verification, then aligns active docs, agent routing,
  and facade-versus-leaf dependency rules before considering any further core extraction.
- 2026-08-20: Scene entity authoring refactored to `EntityScope` (replacing `EntityModifier`), eliminating deferred queues and auto-wiring hierarchical parent entities. Procedural geometry generation (`generate { cube() }`, `generate { plane() }`) and `SceneConvenienceSugar` (`defaultOrbitCamera`, `lighting.singleDirectionalLight`) landed in `:awake:scene:authoring`.
- 2026-08-20: Full-stack `CullMode` (`None`, `Back`, `Front`) landed across render contract, Vulkan backend, WebGPU backend, and scene serialization.
- 2026-08-20: Unified scene lifecycle management under `SceneManager` (`switchTo`, `close`), with `ExampleLoader` in Studio migrated to it.
- 2026-08-20: Vulkan and UI performance optimizations landed: zero-allocation transform TRS, redundant Vulkan pipeline/buffer binding elision, draw call clustering by mesh, shape painter early exit, and UI measurement array recycling.
- 2026-08-12: UI showcase visual-parity work is tracked in
  [docs/tasks/2026-08-12-ui-showcase-parity-tracker.md](tasks/2026-08-12-ui-showcase-parity-tracker.md).
  It is deliberately separate from the now-public-boundary-complete Headless migration: no
  component is marked visually complete without source, semantic, crop, and final-build proof.
- 2026-08-05: API layering plan added. Use
  [docs/reference/api-layering.md](reference/api-layering.md) as the stable rule and
  [docs/tasks/2026-08-05-api-layering-plan.md](tasks/2026-08-05-api-layering-plan.md) as
  the active ECS/scene cleanup plan.
- 2026-08-05: Phase 2 ECS/scene API classification audit recorded in the API layering
  plan. Demo navmesh bootstrap moved out of `awake:scene`; authored gameplay systems moved
  to the scene3d sample. The scene module split proposal is now tracked in
  [docs/tasks/archive/2026-08-05-scene-module-split-proposal.md](tasks/archive/2026-08-05-scene-module-split-proposal.md).
- 2026-08-05: First scene split slice landed behind the `awake-scene` facade:
  `:awake:scene:scene-core` owns `Transform`/`Name`, and `:awake:scene:rendering` owns
  render-facing scene components plus `RenderSystem`.
- 2026-08-05: Physics scene leaf split landed behind the `awake-scene` facade:
  `:awake:scene:physics` owns `PhysicsBody`/`PhysicsSystem`.
- 2026-08-05: Controls scene leaf split landed behind the `awake-scene` facade:
  `:awake:scene:controls` owns `OrbitControl`/`FreeFlyControl`/`FollowControl`/
  `MovementControl` plus their camera systems. `PlayerControlSystem` stays in
  `:awake:scene` (depends on `ui-core`).
- 2026-08-05: Runtime scene split landed behind the `awake-scene` facade:
  `:awake:scene:runtime` owns `SceneGameRuntime`/`SceneGameSpec`/`SceneRouterSpec`, the
  scene document model, and `SceneAssetLibrary` (moved as one unit -- `SceneGameSpec`
  couples them directly). The deprecated `SceneRuntime` bootstrap stays in `:awake:scene`
  (depends on `TransformSystem`, which hasn't split out yet).
- 2026-08-05: Scene module split Phase 5 (DSL dependency tightening) landed:
  `TransformSystem` moved to `:awake:scene:scene-core`, `PlayerControlSystem` moved into
  `:awake:scene:authoring` directly (needs `ui-core`). `:awake:scene:authoring` now depends on the
  specific `:awake:scene:*` leaf modules it uses instead of the whole `:awake:scene`
  facade -- this completes the module list the split proposal sketched, leaving only
  `NavMesh` and the deprecated `SceneRuntime` bootstrap in `:awake:scene` itself.
- 2026-08-06: `:awake:scene:scene-core` gained `SpinControl`/`SpinSystem` (generic entity
  rotation) and `:awake:scene:controls` gained `LookAtControl`/`LookAtCameraSystem` plus
  `PrimaryOrbitCamera` (a plain lifecycle helper, not an ECS `System`, for a UI-driven
  debug camera entity) -- extracted from duplicated demo boilerplate, following the same
  component+System convention the existing camera controls already use.
- 2026-07-10: `VulkanApplication` now loads `scene.json` through `SceneRuntimeHost`; next
  we peel shared code into smaller modules, starting with math/runtime/utils.
- 2026-08-06: The above core split is done -- confirmed complete via
  [docs/reference/decision-log.md](reference/decision-log.md) D11-D13 and the current
  `settings.gradle.kts` (no `awake:core` module exists anymore): `awake-core` split into
  dependency-free `:awake:core` (math/input/glTF/utils), `:awake:backend:opengl`, and
  `:awake:engine:render:contract`; the Vulkan/WebGPU backends physically split into
  `:awake:backend:vulkan`/`:awake:backend:webgpu`. This entry and the matching Open
  Questions/Fix Lanes bullets below were stale, written before D11-D13 landed.
- 2026-08-06: `RotatingCubeDemo` gained a "Camera mode" toggle (Orbit/Follow/Look at),
  wiring the previously-unused `FollowControl`/`LookAtControl` + `FollowCameraSystem`/
  `LookAtCameraSystem` (built 2026-08-06, never attached to any entity until now) onto
  `PrimaryOrbitCamera`'s camera entity -- `PrimaryOrbitCamera.entity` is now a public
  read-only property so a demo can attach extra components to that same entity instead of
  spawning a second camera.

## Open Questions

- Which ECS/scene APIs are true core, which are reusable helpers, and which are only
  authoring sugar?
- Should `SceneGameRuntime` remain renderer/UI aware, or should those concerns move behind
  smaller runtime interfaces during the scene split?
- Do we split `physics` now, or leave it until the scene/runtime shape is settled?
- Should Awake v1 use one universal `Style`, or separate style types immediately for
  text/button/panel families?
- Which properties stay in `UiModifier`, and which must move into the new `Style` layer?
- Should the first UI DSL slice target inspector panels only, or should it also cover
  HUD/menu composition in the same pass?
- Do we want `animate { }` support in the first style pass, or only after the static style
  property model settles?

## Fix Lanes

- Dev: Core split (complete -- see
  [docs/reference/decision-log.md](reference/decision-log.md) D11-D13)
- Dev: ECS/scene API layering and classification
- Dev: Scene module split (complete -- see
  [docs/tasks/archive/2026-08-05-scene-module-split-proposal.md](tasks/archive/2026-08-05-scene-module-split-proposal.md))
- Dev: UI DSL and style audit
- Beta: None yet
- Stable: Refresh runtime docs after the module split lands

## Task Log

- [2026-07-09-decouple-world](tasks/2026-07-09-decouple-world.md)
- [2026-08-05-api-layering-plan](tasks/2026-08-05-api-layering-plan.md)
- [2026-08-12-ui-showcase-parity-tracker](tasks/2026-08-12-ui-showcase-parity-tracker.md)

## Archive Index

- [2026-07-10-scene-runtime](tasks/archive/2026-07-10-scene-runtime.md) -- core split executed
- [2026-07-14-ui-dsl-audit](tasks/archive/2026-07-14-ui-dsl-audit.md) -- superseded by the 2026-08-11 headless boundary migration doc
- [2026-07-14-ui-module-split](tasks/archive/2026-07-14-ui-module-split.md) -- superseded by the 2026-08-11 headless boundary migration doc
- [2026-07-17-ui-api-simplification](tasks/archive/2026-07-17-ui-api-simplification.md) -- all implementation items done
- [2026-07-22-core-graphics-split](tasks/archive/2026-07-22-core-graphics-split.md) -- standing policy decided (no shared graphics module yet)
- [2026-07-24-uislot-narrowing](tasks/archive/2026-07-24-uislot-narrowing.md) -- superseded plan, UiSlot/UiBounds merged
- [2026-08-02-trial-measure-cross-frame-cache](tasks/archive/2026-08-02-trial-measure-cross-frame-cache.md) -- shipped (`cacheKey` param on row/column)
- [2026-08-02-trial-measure-double-execution](tasks/archive/2026-08-02-trial-measure-double-execution.md) -- shipped (`requiresMeasuredDistribution()`, commit `55dd0681`)
- [2026-08-05-scene-module-split-proposal](tasks/archive/2026-08-05-scene-module-split-proposal.md) -- complete, all five leaf modules exist
- [2026-08-10-glyph-scale-regression](tasks/archive/2026-08-10-glyph-scale-regression.md) -- resolved bug investigation
- [2026-08-11-ui-designsystem-headless-boundary-migration](tasks/archive/2026-08-11-ui-designsystem-headless-boundary-migration.md) -- superseded by the 2026-08-15 visual-policy decision
- [2026-08-14-ui-implementation-plan](tasks/archive/2026-08-14-ui-implementation-plan.md) -- superseded by 2026-08-14-ui-roadmap.md
