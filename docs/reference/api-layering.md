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
| `Transform`, `Camera`, `MeshRenderer` | Scene core | `:awake:scene` | Scene-domain components, not generic ECS concepts |
| `SceneGameRuntime` | Scene core | `:awake:scene` | Owns scene lifecycle and game-loop integration |
| `SceneSystemPhase` | Scene core | `:awake:scene` | Scheduling belongs to the scene runtime, not the ECS core |
| `fixedSystem(...)` / `frameSystem(...)` | Helper/sugar | `:awake:scene-dsl` | Friendly explicit registration for scene runtime phases |
| `cameraEntity(...)` / `meshEntity(...)` | Sugar | `:awake:scene-dsl` | Authoring convenience for common scene shapes |
| `scene { ... }` / `assets { ... }` | Sugar | `:awake:scene-dsl` | Declarative authoring surface |

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

:awake:scene:core
  Scene runtime contracts, scene-domain components, scene document/runtime lifecycle.

:awake:scene:rendering
  Render-facing scene components and systems, such as MeshRenderer and RenderSystem.

:awake:scene:physics
  Physics-facing scene components and systems.

:awake:scene:controls
  Reusable camera/player control components and systems.

:awake:scene-dsl or :awake:scene:authoring
  Declarative authoring sugar and demo-friendly builders.
```

Current guidance: classify and document the API first, then split modules. Splitting before
classification usually moves confusion into more folders.

## Review Questions For New API

Before merging a public API addition, answer:

1. Is this core, helper, or sugar?
2. Which module owns it today?
3. Which lower layer does it depend on?
4. Which higher layer consumes it?
5. What is the non-DSL way to do the same thing?
6. What would make this API hard to remove later?
7. Does the README/changelog need an update?
