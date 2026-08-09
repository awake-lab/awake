# API Layering

Awake separates public API into three layers: **core**, **helpers**, and **sugar**. This
keeps the engine small where it must be stable, pleasant where authors need speed, and
honest about which APIs are durable contracts versus convenience.

## The Rule

```text
Core makes impossible states harder.
Helpers remove repetition.
Sugar makes demos, docs, and authored content pleasant.
```

When adding an API, classify it before choosing a module or name. If the classification is
unclear, keep it out of published core until real usage proves where it belongs.

## Layer Definitions

| Layer | Purpose | Stability | Belongs In | Avoid |
|---|---|---|---|---|
| Core | Minimal primitives and contracts that other layers build on | Highest | Published engine/runtime modules | Renderer-specific behavior, scene assumptions, opinionated defaults, demo shortcuts |
| Helpers | Reusable convenience over core that stays explicit | Medium-high | Runtime modules or helper packages | Hidden lifecycle, surprising allocation, policy that callers cannot override |
| Sugar | Authoring-friendly syntax and sample ergonomics | Medium-low | DSL/authoring/sample-facing modules | Becoming the only way to use the engine, leaking into core, hiding expensive work |

## ECS And Scene Mapping

Current intended split:

| API | Layer | Module | Why |
|---|---|---|---|
| `Entity` | Core | `:awake:ecs` | Stable identity primitive |
| `World` | Core | `:awake:ecs` | Owns entity/component storage |
| `System` | Core | `:awake:ecs` | Minimal behavior contract: `update(world, delta)` |
| `World.family(...)` / `queryEach(...)` | Core/helper boundary | `:awake:ecs` | Querying is central to using the ECS; ergonomic overloads are acceptable when they stay explicit |
| Component pooling | Helper | `:awake:ecs` | Performance convenience over component storage |
| `Transform`, `Name` | Scene core | `:awake:scene:core` via `:awake:scene` facade | Scene-domain components, not generic ECS concepts |
| `Camera`, `Light`, `MeshRenderer` | Scene rendering | `:awake:scene:rendering` via `:awake:scene` facade | Render-facing scene components stay out of the tiny scene core |
| `SceneGameRuntime` | Scene core | `:awake:scene:runtime` via `:awake:scene` facade | Owns scene lifecycle and game-loop integration |
| `SceneSystemPhase` | Scene core | `:awake:scene:runtime` via `:awake:scene` facade | Scheduling belongs to the scene runtime, not the ECS core |
| `fixedSystem(...)` / `frameSystem(...)` | Helper/sugar | `:awake:scene:authoring` | Friendly explicit registration for scene runtime phases |
| `cameraEntity(...)` / `meshEntity(...)` | Sugar | `:awake:scene:authoring` | Authoring convenience for common scene shapes |
| `scene { ... }` / `assets { ... }` | Sugar | `:awake:scene:authoring` | Declarative authoring surface |

Design credit: Awake's system model is Ashley-like in spirit, but with a
Bevy/Unity/Flecs-style separation. Systems describe behavior; schedules decide when that
behavior runs.

## Reusable Systems Versus Authored Gameplay

Engine-owned systems must be reusable behaviors, not one game's authored rules.

Put a `System` in an engine module only when it is generic, configurable, and useful across
multiple games or samples without carrying authored scenario assumptions. A reusable scene
system may say "propagate transforms," "submit mesh renderers," "sync physics bodies," or
"drive cameras from reusable control components."

Keep a `System` in the game/sample `gameplay/systems/` folder when it encodes authored
gameplay: chase this specific target style, move this specific player model, trigger this
demo sequence, score this rule, or prove one roadmap slice. Authored gameplay can graduate
later, but only after repeated usage reveals the reusable contract.

Rule of thumb: if the system's name only makes sense inside one demo's story or MVP slice,
it is authored gameplay first.

## Naming Rules

- Prefer boring names in core: `World`, `Entity`, `System`.
- Prefer explicit phase names in runtime helpers: `fixedSystem`, `frameSystem`.
- Prefer author-friendly names in DSL sugar: `cameraEntity`, `meshEntity`, `assets`.
- Avoid names that import another framework's inheritance model unless Awake actually
  adopts that model. For example, do not add `EntitySystem` just because Ashley has one;
  Awake's equivalent is currently `System` plus `family/queryEach` plus explicit scene
  phases.

## Promotion Path

New APIs should move through layers intentionally:

```text
sample-local experiment
  -> sugar/helper if repeated in multiple demos
  -> runtime helper if useful without the DSL
  -> core only if it becomes a minimal primitive
```

Promotion checklist:

- Is this needed without the scene runtime?
- Is this needed without a renderer?
- Is this needed without the DSL?
- Can the caller predict lifecycle and cost?
- Does the name describe Awake's design, not just a borrowed framework pattern?
- If this lands in a published module, is the changelog updated?

## Module Split Direction

Do not split modules just to make the tree look architectural. Split only when the layer is
clear enough that module boundaries reduce mistakes.

Likely future shape:

```text
:awake:ecs
  Pure ECS core and low-level ECS helpers.

:awake:scene
  Published compatibility facade while scene internals split by capability.

:awake:scene:core
  Scene-domain core components. Currently owns Transform, Name, and TransformSystem
  (moved here once the rendering split made "keep transform propagation in core" the
  actual state, not just the plan), plus the generic entity-rotation SpinControl/
  SpinSystem.

:awake:scene:rendering
  Render-facing scene components and systems, such as MeshRenderer and RenderSystem.

:awake:scene:physics
  Physics-facing scene components and systems. Currently owns PhysicsBody and PhysicsSystem.

:awake:scene:controls
  Reusable camera/player control components and systems. Currently owns CameraComponent
  (with its CameraMode enum: FirstPerson, ThirdPerson, Cinematic, TopDown), the
  ActiveCamera tag, MovementControl, and the CameraSystem, CameraInputSystem,
  MatrixRelativeMovementSystem and PlayerInputSystem systems. This module stays UI-free:
  input reaches it as hardware snapshots mapped to intent components, never via ui-core.

:awake:scene:runtime
  SceneGameRuntime scheduling and game-loop integration. Currently owns SceneGameRuntime,
  SceneGameSpec, SceneSystemPhase/SceneSystemHandle, SceneRouterSpec, the scene document
  model (SceneDocument/SceneLoader/SceneValidator/SceneInstantiationAdapter), and
  SceneAssetLibrary -- moved as one unit since SceneGameSpec couples the runtime and
  document model directly. The deprecated SceneRuntime bootstrap stays in :awake:scene
  (depends on TransformSystem's package, unaffected by which module physically holds it,
  but not worth moving before its planned removal).

:awake:scene:authoring or :awake:scene:authoring
  Declarative authoring sugar and demo-friendly builders. Depends on
  :awake:scene:core/:rendering/:controls/:runtime directly, not the :awake:scene facade
  (Phase 5). Owns the DSL registration helpers for those systems (playerInputSystem(),
  cameraInputSystem(), matrixRelativeMovementSystem(), cameraSystem()), not the systems
  themselves.
```

Current guidance: classify and document the API first, then split modules. Splitting before
classification usually moves confusion into more folders. The active split plan is tracked in
[docs/tasks/2026-08-05-scene-module-split-proposal.md](../tasks/2026-08-05-scene-module-split-proposal.md).

## Review Questions For New API

Before merging a public API addition, answer:

1. Is this core, helper, or sugar?
2. Which module owns it today?
3. Which lower layer does it depend on?
4. Which higher layer consumes it?
5. What is the non-DSL way to do the same thing?
6. What would make this API hard to remove later?
7. Does the README/changelog need an update?
