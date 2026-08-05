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

| API | Classification | Action |
|---|---|---|
| `Entity`, `World`, `System` | Core | Keep in `:awake:ecs` |
| `World.family(...)`, `queryEach(...)` | Core/helper boundary | Keep in `:awake:ecs`; watch allocation and naming |
| `SystemFrequency` | Removed core leakage | Replace with scene-owned scheduling |
| `SceneSystemPhase` | Scene core | Keep in `:awake:scene` |
| `fixedSystem(...)`, `frameSystem(...)` | Scene DSL helper/sugar | Keep in `:awake:scene-dsl` |
| `TransformSystem`, `RenderSystem` | Scene runtime systems | Keep in `:awake:scene` for now; later evaluate rendering split |
| `cameraEntity(...)`, `meshEntity(...)` | Sugar | Keep in `:awake:scene-dsl` |
| Ashley-style `EntitySystem` | Not accepted yet | Do not add until repeated boilerplate proves a helper is needed |

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

- A short table in this task note or a dedicated reference page if the audit becomes stable.
- A list of APIs that should move, become internal, or stay public.

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
:awake:scene:core
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
- Do not remove or rename published public symbols without changelog/release-note treatment.
- Do not split modules until the API classification is clear.

## Next Concrete Step

Finish Phase 1 by committing the scheduler cleanup and this documentation together or as two
small commits:

1. `refactor: keep ecs systems scheduler-free`
2. `docs: define awake api layering`

Then start Phase 2 with an API classification audit.

