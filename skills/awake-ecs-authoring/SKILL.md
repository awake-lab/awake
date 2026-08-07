---
name: awake-ecs-authoring
description: Rules for authoring Awake ECS components, systems and scenes - component construction, query/family costs, structural-change churn, entity lifecycle, and the scene DSL. Read before adding a component, writing a System, or building entities with the scene { } DSL. Trigger keywords - ECS, World, entity, component, System.update, queryEach, family, Poolable, scene DSL, EntityModifier, Modifier(), cameraEntity, MeshRenderer, Transform, spawn, destroy.
---

# Authoring ECS components, systems and scenes in Awake

Pairs with `awake-core-math`, which owns the vector/matrix and per-frame allocation rules.

## Components

A component is plain data. Behaviour lives in a `System`.

```kotlin
class MovementControl : Poolable {
    var moveX: Float = 0f
    var moveZ: Float = 0f

    // reset() must clear EVERY field. A pooled instance is reused; anything you forget
    // leaks into the next entity that gets this instance.
    override fun reset() {
        moveX = 0f
        moveZ = 0f
    }
}
```

Two rules that have already bitten this codebase:

- **`reset()` must clear every field, including flags.** `CameraComponent.needsReset` was
  omitted, so a recycled component came back still flagged and re-ran its mode reset on the
  next entity's first frame.
- **Do not rely on reflective construction.** `world.add<T>(entity)` resolves a no-arg
  constructor at runtime. Kotlin does not emit one for a component holding a value class
  (`Transform.parent: Entity?` is the live example - `Entity` is a value class), and iOS and
  wasmJs do not support it at all. Always pass an explicit factory or instance:

```kotlin
world.add(entity, Name(name))              // explicit instance
world.ensure(entity, ::MovementControl)    // explicit factory
Modifier().configure(::Transform) { ... }  // DSL takes a factory for the same reason
```

## Systems

```kotlin
class SpinSystem : System {
    override fun update(world: World, delta: Float) {
        world.queryEach(Transform::class, SpinControl::class) { _, transform, spin ->
            transform.rotation.y = spin.radians
        }
    }
}
```

**Query cost.** The 1- and 2-component typed `queryEach` overloads run off maintained family
caches and allocate nothing. The `vararg` overload calls `types.toSet()` on every invocation -
avoid it in a per-frame path.

**Structural changes are not free.** `world.add` / `world.remove` invalidate the query cache
and churn the family caches. Never add-and-remove the same component every frame:

```kotlin
// Before: two full query-cache invalidations per frame, plus a fresh allocation, purely to
// zero two floats that the next system overwrites anyway.
world.remove<MovementControl>(target)
world.ensure(target, ::MovementControl)

// After: keep the component resident, mutate its fields.
world.get<MovementControl>(target)?.apply { moveX = 0f; moveZ = 0f }
```

**Never mutate the world while iterating it.** `queryEach` walks a family cache whose backing
array is swap-mutated by removal. Collect first, then act:

```kotlin
val previouslyActive = world.query(ActiveCamera::class)
previouslyActive.forEach { world.remove<ActiveCamera>(it) }
```

**Registration order is behaviour.** Systems run in registration order within a phase. Input
must precede intent consumers; the camera pose must be settled before anything reads its basis.
If a system reads another's output, say so in a comment at the registration site.

## Scenes and entities

```kotlin
world.scene {
    val cube = entity("SpinningCube", Modifier().transform(y = 0.5f).with(SpinControl()))
    entity("MainCamera", Modifier().camera(CameraMode.ThirdPerson, target = cube))
}
```

- `Modifier()` is a **function** returning a fresh `EntityModifier` (it mirrors Compose's
  `Modifier`; the UI one is a `val`, so both can be imported into one file).
- `EntityModifier.with(component)` **mutates and returns the same builder**. It is not a pure
  combinator - do not branch off a shared base expecting independent copies.
- `configure(::Type) { }` takes a factory (see the reflective-construction rule above).

**A camera needs both halves.** `Camera` is the lens `RenderSystem` renders from;
`CameraComponent` is the control state `CameraSystem` drives. An entity carrying only one is
silently inert - it will never be rendered from, or never move. `Modifier().camera(...)`
attaches both; prefer it over hand-assembling either.

Attaching a raw `core.math.Camera` does **not** make an entity a camera - it registers under
its own type and matches no scene query. Wrap it: `Modifier().camera(lens = myCoreCamera)`.

**Own what you destroy.** Keep the `Entity` handles you spawned and destroy exactly those.
A global `queryEach(SpinControl::class) { destroy(it) }` in a teardown will take out every
other system's entities too.

**Free GPU resources.** Meshes and materials created in `onActivate`/`ensureSpawned` are not
owned by the ECS. Destroy them in `onDeactivate`, or every activate/deactivate cycle leaks one
mesh and one material.

## DSL builders

`@DslMarker` is load-bearing. `SceneGameDsl` is annotated `@AwakeSceneDsl` so the enclosing
`GameSpecDsl` receiver is hidden inside its block. Without it, an inner `scene(name) { }` call
silently resolved to the *outer* `GameSpecDsl.scene` extension and installed a second scene
module - two `World`s, two render callbacks, and `requireService<SceneGameRuntime>()` returning
the wrong one. Annotate every nested builder receiver.

When you add or change a builder overload, check that a call inside a nested block still binds
to the member you expect. An overload that merely *fails to match* does not error - it falls
through to whatever outer extension does match.

## Checklist

- [ ] `reset()` clears every field of the component, flags included.
- [ ] No reflective `world.add<T>(entity)` - factory or instance passed explicitly.
- [ ] No per-frame add/remove of the same component.
- [ ] No structural mutation during `queryEach` iteration.
- [ ] Camera entities carry both `Camera` and `CameraComponent`.
- [ ] Teardown destroys only entities this feature spawned, plus its GPU resources.
- [ ] New nested DSL receivers are `@DslMarker`-annotated.
