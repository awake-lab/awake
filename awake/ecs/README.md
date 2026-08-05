# Awake ECS

A small, dependency-free sparse-set Entity Component System for Kotlin Multiplatform
(Android, iOS, JVM/desktop). Built in-house for the [Awake](../README.md) engine instead of
adopting an existing library (Fleks, Artemis-odb, Ashley) — see
[docs/ecs-benchmark-scorecard.md](../docs/ecs-benchmark-scorecard.md) for the real,
same-JVM benchmark comparison that justifies this, and
[.claude/agents/ecs-dev.md](../.claude/agents/ecs-dev.md) for the architecture rationale.

Not thread-safe by design — this ECS is meant to be driven from a single game-update
thread, matching this project's Vulkan threading model.

## Installation

```kotlin
implementation("io.github.ronjunevaldoz:awake-ecs:1.0.0-SNAPSHOT")

repositories {
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
}
```

## Core concepts

- **`Entity`** — a value class wrapping a packed `id` + `generation`. The generation exists
  so a recycled id can't alias a stale handle held elsewhere.
- **`World`** — owns entity allocation/recycling and one component store per component
  type. Everything below is a method on `World`.
- **Components** — plain data classes/objects. No base interface or registration step
  required; any `Any` type can be a component.
- **`System`** — a `fun interface` with a single `update(world: World, delta: Float)`.
  Systems are just plain classes you call yourself each frame — there's no built-in
  scheduler.

## Quick start

```kotlin
import io.github.ronjunevaldoz.awake.ecs.World

data class Position(var x: Float, var y: Float)
data class Velocity(var dx: Float, var dy: Float)

val world = World()

val player = world.create()
world.add(player, Position(0f, 0f))
world.add(player, Velocity(1f, 0f))

// Iterate every entity that has both components:
world.queryEach<Position, Velocity> { entity, position, velocity ->
    position.x += velocity.dx
    position.y += velocity.dy
}

world.remove<Velocity>(player)     // stops moving
world.destroy(player)               // recycles the id; old `player` handle is now stale
world.isAlive(player)               // false
```

## Entities

```kotlin
val entity = world.create()
world.isAlive(entity)   // true
world.destroy(entity)   // true; id becomes eligible for reuse with a bumped generation
world.isAlive(entity)   // false -- this exact handle can never come back alive
```

A destroyed id can be handed out again by a later `create()`, but the new `Entity` will
have a different `generation`, so any old handle you were still holding safely reads as
not-alive rather than aliasing the new entity.

## Components

```kotlin
world.add(entity, Position(1f, 2f))       // add or replace
world.get<Position>(entity)               // Position? -- null if absent or entity is dead
world.has<Position>(entity)               // Boolean
world.remove<Position>(entity)            // Position? -- the removed value, or null
```

Every one of these also has an explicit-`KClass` overload
(`world.add(entity, Position::class, component)`, etc.) alongside the reified generic
sugar shown above. **Prefer the reified sugar for one-off calls; in a loop over many
entities, hoist the `KClass` once and use the explicit overload instead** — profiling
found Kotlin's reified generics re-derive the type token on every call site without
`kotlin-reflect` on the classpath, which only matters at that call frequency. See
`.claude/agents/ecs-dev.md`'s "Hot-path performance" section.

```kotlin
// Hot loop: hoist the KClass once instead of `world.add<Position>(entity, component)` per entity
val positionType = Position::class
for (entity in manyEntities) {
    world.add(entity, positionType, Position(0f, 0f))
}
```

There's an even faster path for the hottest loops: cache the `ComponentTypeId` (via
`world.typeId(type)`) instead of the `KClass` itself. It skips both the reflection-derived
type token *and* the `KClass`-keyed map lookup that the `KClass` overload still does:

```kotlin
val positionTypeId = world.typeId(Position::class)
for (entity in manyEntities) {
    world.add(entity, positionTypeId, Position(0f, 0f))   // fastest add() overload
}
```

## Component pooling

Register a factory once per component type, then obtain (and automatically recycle)
pooled instances instead of allocating fresh ones on every `add`/`remove`:

```kotlin
world.registerPool(Position::class) { Position(0f, 0f) }

val entity = world.spawn<Position> { it.x = 1f; it.y = 2f }   // create() + pooled add<T>() + init block
world.destroy(entity)                                          // Position instance returns to the pool
```

If a component implements `Poolable`, `reset()` runs automatically when it's returned to
the pool (on `remove`/`destroy`), so the next `obtain()` doesn't hand back stale state:

```kotlin
data class Position(var x: Float = 0f, var y: Float = 0f) : Poolable {
    override fun reset() { x = 0f; y = 0f }
}
```

Without a registered factory, `world.add<T>(entity)` (the no-component-argument overload)
falls back to reflection for zero-arg-constructor components on JVM/Android. **iOS has no
reflection-based instantiation** — register an explicit factory via `registerPool` for any
type you construct this way if the code needs to run there.

## A hard limit: 64 component types per `World`

Entity-component membership is tracked as a single `Long` bitmask per entity (one bit per
component type), which is how `has()`/family-matching stay cheap. That caps this ECS at
**64 distinct component types per `World`** — registering a 65th type throws a clear
`IllegalArgumentException` from `world.typeId(...)`/`add(...)` rather than silently
overflowing. This is a deliberate tradeoff, not an oversight; if your game genuinely needs
more than 64 component types in one `World`, that's worth raising as a design question
before working around it.

## Queries and families

For iterating "every entity with components X (and Y)", use `queryEach` for a one-shot pass
or `family` for a cache you keep and reuse (e.g. as a `System`'s field):

```kotlin
// One-shot iteration
world.queryEach<Position> { entity, position -> /* ... */ }
world.queryEach<Position, Velocity> { entity, position, velocity -> /* ... */ }

// A maintained, reusable handle -- membership stays incrementally up to date as
// components are added/removed, no rescan needed on each access
val movers = world.family<Position, Velocity>()   // Family2<Position, Velocity>
movers.forEach { entity, position, velocity -> /* ... */ }
movers.forEachComponents { position, velocity -> /* ... */ }   // skip the Entity if you don't need it
movers.size

// Direct array access, for callers that want bulk/indexed access instead of a callback
val positions: Array<Position> = movers.componentsA()
val velocity = movers.componentB(0)
```

`Family1`/`Family2` cover the common 1- and 2-component case and hand you typed components
directly (no extra lookup per entity). For 3+ component types, or `one`/`exclude`
semantics, use the general `family { }` builder instead — it only hands back matched
`Entity` handles (Kotlin can't express an arbitrary-arity typed tuple), so read components
back via `world.get<T>(entity)`:

```kotlin
val renderable = world.family {
    all(Position::class, MeshRenderer::class)
    exclude(Hidden::class)
}
renderable.forEach { entity ->
    val position = world.get<Position>(entity)!!
    // ...
}
```

`world.query(vararg types)` / `world.queryEach(vararg types) { entity -> }` are also
available when you only need the matching `Entity` list/callback and don't care about
typed component access at all.

## Systems

```kotlin
class MovementSystem : System {
    override fun update(world: World, delta: Float) {
        world.queryEach<Position, Velocity> { _, position, velocity ->
            position.x += velocity.dx * delta
            position.y += velocity.dy * delta
        }
    }
}

val systems = listOf(MovementSystem())

// Your own game loop drives this -- there's no scheduler built into the ECS itself
fun gameLoop(world: World, delta: Float) {
    systems.forEach { it.update(world, delta) }
}
```

Design credit: Awake's system model is **Ashley-like in spirit, but with a
Bevy/Unity/Flecs-style separation**. Like libGDX Ashley, systems are behavior objects
that run against an `Engine`/`World`. Like Bevy schedules, Unity Entities system
groups, and Flecs pipelines, the decision about **when** a system runs belongs to the
runtime/schedule layer, not the component data model. In Awake that means `awake-ecs`
keeps `System` small and scheduler-free, while higher layers such as `awake-scene`
can register systems into explicit phases such as fixed simulation steps or per-frame
render/update passes.

If a system keeps its own per-frame scratch state (buffers, visited-sets), reuse instance
fields across `update()` calls instead of allocating fresh collections every frame — see
`awake-scene`'s `TransformSystem` for a worked example (entity-id-indexed arrays instead of
a `Map`/`Set` keyed by `Entity`, to avoid boxing the value class on every frame).

## What this ECS deliberately doesn't do

- No archetype/table storage — sparse-set per component type instead (see
  `.claude/agents/ecs-dev.md` for why, and when that tradeoff would need revisiting).
- No built-in scheduler, job system, or parallelism — single-threaded by design.
- No serialization at this layer — component types are plain data classes; use whatever
  serialization approach fits your game (`awake-scene`'s scene runtime uses
  `kotlinx.serialization` on top of this, entirely outside `awake-ecs` itself).
- No mandatory component registration — any `Any` works as a component the moment you
  `add` one, no base interface or upfront registration required. Reflection is used, but
  only opt-in: pooled zero-arg component instantiation on JVM/Android falls back to it if
  you don't register a factory (see "Component pooling" above); iOS has no reflection
  fallback and requires an explicit factory for that path.

## Benchmarking

`awake-ecs-benchmark` (a separate, JVM-only module) benchmarks this ECS against Fleks,
Artemis-odb, and Ashley on the same JVM/hardware. See
[docs/ecs-benchmark-scorecard.md](../docs/ecs-benchmark-scorecard.md) for the numbers,
methodology, and an honest account of where this ECS currently wins and where it doesn't.
