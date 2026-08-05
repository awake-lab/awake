# Scene Module Split Proposal

Date: 2026-08-05

## Objective

Split `awake:scene` by reusable capability without breaking the existing `awake-scene`
published artifact or moving authored gameplay back into engine code.

This follows the stable API rule in
[docs/reference/api-layering.md](../reference/api-layering.md): core should stay small,
helpers should stay explicit, and authored gameplay belongs with the game/sample that owns
the rules.

## Recommendation

Use a staged capability split:

```text
:awake:scene                  published compatibility facade for now
:awake:scene:core             scene contracts, transform/name, document model, validation
:awake:scene:runtime          SceneGameRuntime scheduling and game-loop integration
:awake:scene:rendering        Camera/Light/MeshRenderer, RenderSystem, render asset binding
:awake:scene:physics          PhysicsBody and PhysicsSystem
:awake:scene:controls         reusable camera/control components and systems
:awake:scene-dsl              existing authoring DSL; keep path stable for now
```

Do not rename `:awake:scene-dsl` to `:awake:scene:authoring` yet. The current module name is
already clear, tests already protect its implicit infrastructure behavior, and renaming it
would add churn before the scene core split is proven.

## Current Implementation Status

The first physical split has started:

- `:awake:scene:core` exists and owns `Transform`/`Name`.
- `:awake:scene:rendering` exists and owns `Camera`/`Light`/`MeshRenderer` plus
  `RenderSystem`.
- `:awake:scene:physics` exists and owns `PhysicsBody`/`PhysicsSystem`.
- `:awake:scene:controls` exists and owns `OrbitControl`/`FreeFlyControl`/`FollowControl`/
  `MovementControl` plus `OrbitCameraSystem`/`FreeFlyCameraSystem`/`FollowCameraSystem`.
  `PlayerControlSystem` stays in `:awake:scene` (it depends on `ui-core`'s
  `UiInputOwnership`, per the UI-free preference below).
- `:awake:scene` still acts as the published facade and re-exports all four modules.

The document model and runtime are intentionally still in `:awake:scene` until their seams
are clearer.

## Compatibility Strategy

`awake:scene` should remain the published facade during the split.

- Consumers keep depending on `awake-scene`.
- New internal modules are introduced behind the facade.
- The facade re-exports new scene modules with `api(project(...))`.
- Source packages should stay stable during the first move where possible, even when files
  move between Gradle modules.
- Public package renames are deferred until a major-version migration plan exists.

This gives maintainers build-time boundaries without forcing downstream users to update every
import immediately.

## Proposed Module Responsibilities

| Module | Owns | Allowed dependencies | Must not own |
|---|---|---|---|
| `:awake:scene:core` | `Transform`, `Name` now; later `SceneDocument`, `SceneNode`, `SceneTransform`, validation, adapter contracts | `awake:base`, `awake:ecs`, serialization only once document model moves | `Renderer`, `PhysicsWorld`, UI input, authored gameplay |
| `:awake:scene:runtime` | `SceneGameRuntime`, `SceneGameSpec`, `SceneSystemPhase`, `SceneSystemHandle`, frame/update lifecycle, scene frame helpers | `scene:core`, `awake:engine:game`, `awake:engine:ui:ui-core`, likely `render-api` during transition | Render submission internals, physics backend details, sample demo drivers |
| `:awake:scene:rendering` | `Camera`, `Light`, `MeshRenderer`, `RenderSystem`, renderable request resolution, scene asset binding | `scene:core`, `scene:runtime` if asset factories still need runtime receiver, `awake:engine:render-api` | Vulkan/WebGPU concrete backends, authored sample meshes |
| `:awake:scene:physics` | `PhysicsBody`, `PhysicsSystem` | `scene:core`, `awake:physics:api` | Jolt backend classes, gameplay movement rules |
| `:awake:scene:controls` | `OrbitControl`, `FreeFlyControl`, `FollowControl`, reusable camera systems | `scene:core`, possibly `scene:rendering` for `Camera` | One-game movement/combat/chase rules, UI chrome |
| `:awake:scene-dsl` | `sceneGame {}`, `fixedSystem`, `frameSystem`, entity/asset/document builders, infrastructure system installation | facade or the exact modules it needs | Engine core primitives, hidden duplicate render paths |

## Transition Seams To Handle Carefully

### `SceneGameRuntime`

`SceneGameRuntime` is not pure scene core today. It owns:

- fixed/frame system scheduling;
- a `World`;
- renderer-facing asset loading and readback helpers;
- UI frame lifecycle for overlays.

So it should move only after the first rendering seams are clear. If moving it forces
`scene:core` to depend on `render-api` and UI, keep it in `:awake:scene` temporarily and split
leaf systems first.

### `SceneLoader.instantiate`

The document model is core, but the default Awake instantiation path currently creates
render-facing components:

- `Camera`
- `Light`
- queued `MeshRenderer` requests

Keep the adapter contract in core, but move the default Awake-world adapter only when
rendering ownership is clear. A good target is:

```text
core:      SceneDocument, SceneLoader.decode/validate, SceneInstantiationAdapter
rendering: AwakeWorldSceneAdapter, renderable request attachment, Camera/Light/MeshRenderer
```

### `PlayerControlSystem`

`PlayerControlSystem` is reusable-ish, but it crosses hardware input and UI input ownership.
Do not blindly move it into `scene:controls`.

Decision needed before moving:

- If `scene:controls` may depend on `ui-core`, move it there as an input-to-control adapter.
- If controls should stay UI-free, keep `PlayerControlSystem` in DSL/sample tooling and move
  only control components plus camera systems.

My preference: keep `scene:controls` UI-free first, then add an optional input adapter only
after another sample needs it.

## Migration Phases

### Phase 0 — Preflight

- Keep current commits green.
- Confirm no authored gameplay remains in `awake:scene`.
- Add a package/source inventory test or documentation check if the split becomes easy to
  regress.

Exit gate:

```bash
./gradlew detekt :awake:scene:desktopTest :awake:scene-dsl:desktopTest :samples:scene3d-playground:desktopTest
```

### Phase 1 — Rendering Leaf Split

Move the clearest render-facing leaf code first:

- `Camera`
- `Light`
- `MeshRenderer`
- `RenderSystem`

Keep packages stable initially. Update `:awake:scene` to re-export
`:awake:scene:rendering`.

Why first: these files already depend on `render-api`, and removing that dependency from the
core-ish surface is the clearest boundary win.

### Phase 2 — Physics Leaf Split

Move:

- `PhysicsBody`
- `PhysicsSystem`

Update `:awake:scene` to re-export `:awake:scene:physics`.

Why second: this removes `awake:physics:api` from the scene facade internals and keeps Jolt
backend details out of scene.

### Phase 3 — Controls Split

Move reusable, UI-free controls:

- `OrbitControl`
- `FreeFlyControl`
- `FollowControl`
- `MovementControl` only if it is still reusable outside authored gameplay
- `OrbitCameraSystem`
- `FreeFlyCameraSystem`
- `FollowCameraSystem`

Defer `PlayerControlSystem` until the UI-input dependency decision is made.

### Phase 4 — Runtime/Core Separation

After leaf splits, decide whether `SceneGameRuntime` belongs in:

- `:awake:scene:runtime`, if it remains renderer/UI aware; or
- `:awake:scene:core`, only if render/UI concerns are pulled behind explicit interfaces.

Do not force this earlier. Runtime is the highest-risk seam.

### Phase 5 — DSL Dependency Tightening

Update `:awake:scene-dsl` to depend only on the modules it truly uses.

Keep these protections:

- tests proving `fixedSystem` and `frameSystem` scheduling;
- tests proving infrastructure systems are installed exactly once;
- docs warning not to hand-register `RenderSystem` inside `scene {}`.

## Migration Path For Users

Short term:

```kotlin
implementation("io.github.ronjunevaldoz.awake:awake-scene:<version>")
```

continues to work through the facade.

Medium term:

- users who want the broad scene stack keep `awake-scene`;
- advanced users may depend on narrower artifacts once they are published;
- release notes list optional narrower coordinates.

Major-version option:

- if public package names ever change, ship a migration table and deprecation window first;
- do not mix package rename, module split, and behavior changes in one commit.

## Validation Matrix

| Change | Minimum validation |
|---|---|
| Rendering split | `detekt`, `:awake:scene:desktopTest`, `:awake:scene-dsl:desktopTest`, `:samples:scene3d-playground:desktopTest` |
| Physics split | `detekt`, `:awake:scene:desktopTest`, `:awake:backend:jolt:desktopTest` if available |
| Controls split | `detekt`, camera/control system tests, scene DSL tests |
| Runtime split | all scene/scene-dsl/sample tests plus Android sample compile |
| Published facade changes | publication dry run or at least Gradle metadata inspection before release |

## Open Decisions

1. Should `SceneGameRuntime` remain renderer/UI aware, or should those be extracted behind
   smaller runtime interfaces?
2. Should `PlayerControlSystem` live in reusable controls or stay as optional sample/DSL
   input glue?
3. Should `NavMesh` remain in scene core or move to a tiny `scene:navigation` only after
   more navigation APIs appear?
4. Should the first physical split preserve packages exactly, or start package deprecations
   immediately?

## Current Recommendation

Start with the rendering leaf split, not the full `scene:core` extraction.

That gets the biggest dependency win with the least semantic risk. After rendering and
physics leaves are clean, runtime/core separation will be easier to reason about instead of
being a giant tangled move.
