---
name: awake-ecs-scene-runtime
description: >
  How to consume Awake's real ECS (awake-ecs + awake-scene + awake-scene-dsl) from a
  game or sample -- World/Entity/component basics, the sceneGame{}/GameModuleDsl.scene{}
  DSL surface, when to reach for SceneGameRuntime instead of GameUiRuntime (and why they
  don't compose inside one demo), and the TransformSystem-overwrites-worldMatrix trap.
  This is a how-to reference for consuming the ECS, distinct from the awake-ecs-performance-engineer
  and awake-scene-runtime-engineer personas (which guide work ON the ECS/scene-dsl internals
  themselves).
license: Apache-2.0
metadata:
  author: awake
  last-updated: '2026-08-04'
  keywords:
    - Awake
    - ECS
    - World
    - Entity
    - SceneGameRuntime
    - GameUiRuntime
    - sceneGame
    - RenderSystem
    - TransformSystem
    - scene3d-playground
---

## When to Use This Skill

Use when a game or sample needs real 3D content -- entities with `Transform`/`MeshRenderer`/
`Camera` driven by the actual ECS `World` -- not just 2D UI chrome. Load this before wiring a
new demo/sample onto `SceneGameRuntime`, or before deciding whether a feature needs the ECS at
all.

**Trigger keywords:** ECS, World, Entity, spawn entity, SceneGameRuntime, sceneGame,
GameModuleDsl.scene, RenderSystem, TransformSystem, MeshRenderer, real 3D scene, awake-scene,
awake-scene-dsl, GameUiRuntime vs SceneGameRuntime.

---

## Two runtimes, two jobs -- pick one per demo, don't mix

Awake has two separate `Game`/`GameModule` implementations. **They don't compose inside one
demo** -- each owns its own `render()` call, so installing both side by side means two
independent per-frame passes, not one integrated one (see `SceneGameDslTest.gameModuleCanOwnSceneAndUiComposition`,
the only place they've ever coexisted, which installs `scene{}` and `ui{}` as two *sibling*
`GameModule`s, not one runtime doing both).

| | `GameUiRuntime` (`engine/game-dsl`) | `SceneGameRuntime` (`awake:scene` + `awake:scene-dsl`) |
|---|---|---|
| Owns | `UiContext` only, no `World` | `UiContext` **and** a real `World` |
| 3D content | Only via `provideDrawCalls` escape hatch (a lambda a demo sets to smuggle one `Camera`+`List<DrawCall>` into the runtime's one `renderer.draw()` call) | Real ECS entities -- `RenderSystem` walks `world.family<Transform, MeshRenderer>()` every frame and calls `renderer.draw()` itself |
| Install via | `gameModule { ui { ... } }` | `gameModule { scene("name") { ... } }` (or `GameSpecDsl.ecs { ... }`) |
| Use when | Pure 2D UI, dashboards, no real scene graph | Any demo/game with actual 3D entities, cameras, or a scene graph |

If a demo needs BOTH real ECS entities and rich UI chrome (sidebar, controls, HUD) in the same
frame, use `SceneGameRuntime` for both -- see "UI chrome on `SceneGameRuntime`" below. Don't
reach for `provideDrawCalls` for new work; it's the pre-ECS pattern this skill's own migration
(`samples/scene3d-playground`) replaced.

---

## `World`/`Entity`/component basics

```kotlin
// Bare entity + explicit component add
val entity: Entity = world.create()
world.add(entity, Transform(position = Vec3(0f, 1f, 0f)))
world.add(entity, MeshRenderer(mesh, material))

// Sugar: create + default-construct + configure in one call (component type must be
// default-constructible, e.g. Transform())
val camera: Entity = world.spawn<Camera> { it.isPrimary = true }
// (SceneCamera/scene.components.Camera's fields are all `val` -- spawn{} can't mutate them
// after construction; replace the whole component instead, see "Replacing a component" below)

// Read a component
val transform: Transform? = world.get(entity, Transform::class)

// Destroy -- removes every component too
world.destroy(entity)
```

**Replacing a component** (e.g. a component with `val` fields, or any per-frame value that
changes wholesale): `world.add(entity, newComponentInstance)` -- `add` replaces the existing
component of that type if one is already present, it's not add-only.

```kotlin
// scene.components.Camera is `data class Camera(val camera: CoreCamera, val isPrimary: Boolean)`
// -- both val, so a per-frame camera update replaces the whole component:
world.add(cameraEntity, Camera(computeCamera(), isPrimary = true))
```

**Querying two components together** (the exact pattern `RenderSystem` itself uses):

```kotlin
val family = world.family<Transform, MeshRenderer>()
val transforms = family.componentsA()
val meshRenderers = family.componentsB()
var i = 0
while (i < family.size) {
    val drawCall = DrawCall(meshRenderers[i].mesh, meshRenderers[i].material, transforms[i].worldMatrix)
    i += 1
}
```

Index-based, not a lambda callback -- `componentsA()`/`componentsB()` are parallel arrays, walk
them together by index rather than expecting a `family.forEach { entity, a, b -> }` shape.

---

## The `sceneGame{}` / `GameModuleDsl.scene(name){}` DSL

```kotlin
fun myGameModule(): GameModule = gameModule {
    scene("my-scene") {
        // Register a system -- runs every frame per its SystemFrequency
        // (Simulation = fixed-timestep, Infrastructure = render-rate).
        system("render") { RenderSystem(renderer) }

        // Suspend, runs once before the first frame -- the only place to do suspend
        // work (asset loads) before entities need it.
        onReady { /* preload suspend resources here */ }

        // Runs at the fixed simulation rate; `this: SceneGameRuntime`, has `world`/`renderer`.
        update { delta, inputSnapshot -> /* game logic, entity spawn/despawn */ }

        // Runs at render rate; `this: SceneGameRuntime`, receives real viewport size as params
        // (SceneGameRuntime doesn't store viewportWidth/viewportHeight in a field the way
        // GameUiRuntime does -- see "UI chrome" below for why that matters).
        overlay { viewportWidth, viewportHeight -> /* UI drawing */ }
    }
}
```

Other entry points: `entity(name) { transform { } ; meshRenderer(mesh = "x", material = "y") }`
for declaratively-authored scene documents, `assets { mesh("x") { renderer.createMesh(...) } }`
for a named mesh/material library resolved lazily by name, `cameraEntity(...)`/`meshEntity(...)`
sugar (`awake-scene-dsl`'s `EntityExtensions.kt`) for the common camera/mesh entity shapes. Real
signatures live in `awake/scene-dsl/.../runtime/SceneGameDsl.kt` -- read it before guessing a
DSL method exists; it's a small, closed surface, not an open-ended builder.

---

## Spawn-on-activate / destroy-on-deactivate (the per-demo-page pattern)

A multi-page sample (see `samples/scene3d-playground`) needs each page to own its own entities
without leaking a previous page's mesh when the user switches pages. The pattern:

```kotlin
data class MyDemo(
    val onActivate: SceneGameRuntime.() -> Unit = {},   // spawn this page's entities
    val onDeactivate: (World) -> Unit = {},             // destroy exactly those entities
    val onUpdate: SceneGameRuntime.(delta: Float) -> Unit = {}
)
```

The shell's `update { delta, _ -> }` block tracks which page was active last frame; on change,
calls the old page's `onDeactivate(world)` then the new page's `onActivate(this)`. Keep each
page's spawned `Entity` references in `private var` fields on the page's own object so
`onDeactivate` knows exactly what to destroy -- don't scan the whole `World` for "this page's
entities", there's no tag for that by default.

---

## UI chrome on `SceneGameRuntime`

`GameUiRuntime.frame { }` / `.frameStats()` (the root full-viewport box + fps/frame-time HUD
helpers) only exist on `GameUiRuntime`. `awake:scene` ships equivalents --
`awake/scene/.../runtime/SceneGameFrame.kt`'s `SceneGameRuntime.frame(viewportWidth,
viewportHeight) { }` and `SceneGameRuntime.frameStats(): SceneFrameStats` -- same shape, just
taking `viewportWidth`/`viewportHeight` as explicit params (from the `overlay{}` block's own
signature) instead of reading a stored field. Everything below the root `frame { }` call
(`row`, `column`, `shadcnSidebar`, `text`, `uiContext.pushTheme(...)`) is receiver-agnostic --
plain `UiContext`-produced scopes, not typed to either runtime -- so porting existing UI chrome
from a `GameUiRuntime.() -> Unit` overlay to a `SceneGameRuntime.() -> Unit` one only requires
changing the function's own receiver type and the `frame{}`/`frameStats()` call sites, not
anything inside the layout tree.

---

## Traps

**`TransformSystem` overwrites a directly-set `worldMatrix`.** `awake/scene/.../systems/
TransformSystem.kt` recomputes every entity's `Transform.worldMatrix` from
`position`/`rotation`/`scale` (+ parent chain) every time it runs. If you set `worldMatrix`
directly (e.g. from a parsed glTF node transform, or a hand-built `Mat4`) and also register
`TransformSystem`, your matrix gets silently clobbered back to whatever `position`/`rotation`/
`scale` compose to (identity, by default) on the very next frame it runs. Fix: don't register
`TransformSystem` for entities with directly-set transforms -- either skip registering it
entirely (fine for a scene with no position/rotation/scale-driven entities), or keep
directly-set entities out of whatever family it targets. This isn't hypothetical -- it's exactly
what `samples/scene3d-playground`'s glTF viewer demo has to avoid (see `GltfViewerDemo.kt`'s own
doc comment).

**A `RenderSystem` you don't register never draws.** `SceneGameRuntime` doesn't call
`renderer.draw()` on its own -- only a registered `RenderSystem` (`system("render") {
RenderSystem(renderer) }`) does, once per frame, for every entity with `Transform` +
`MeshRenderer` and whichever `Camera` has `isPrimary = true`. Forgetting to register it means
entities exist in the `World` but nothing ever reaches the GPU -- no crash, no error, just a
blank/stale viewport.

**Components with `val` fields can't be mutated in place.** `scene.components.Camera` and
`MeshRenderer` are `data class`es with `val` fields -- `world.get(entity, Camera::class)!!
.camera = newCamera` doesn't compile. Replace the whole component instead: `world.add(entity,
Camera(newCamera, isPrimary = true))` (see "Replacing a component" above). `Transform`'s fields
ARE `var` (it's `Poolable`, reused across spawns), so `world.get(entity,
Transform::class)?.worldMatrix = newMatrix` works directly.

---

## Related Skills

- `awake-ecs-performance-engineer` -- persona for working ON `awake-ecs`'s own storage/query
  internals (entity arena, component pools, family indexing), not for consuming it from a game
- `awake-scene-runtime-engineer` -- persona for working ON `awake-scene`/`awake-scene-dsl`
  themselves (new components, new systems, DSL surface changes)
- `awake-game-framework-engineer` -- persona for `GameUiRuntime`/`engine`/`game-dsl` itself

---

## Changelog

| Date | Change |
|---|---|
| 2026-08-04 | Initial release -- written alongside `samples/scene3d-playground`'s full migration from `GameUiRuntime` to `SceneGameRuntime` (the first sample to actually use the ECS runtime end to end). |
