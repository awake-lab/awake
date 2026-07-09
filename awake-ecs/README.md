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

If a system keeps its own per-frame scratch state (buffers, visited-sets), reuse instance
fields across `update()` calls instead of allocating fresh collections every frame — see
`awake-scene`'s `TransformSystem` for a worked example (entity-id-indexed arrays instead of
a `Map`/`Set` keyed by `Entity`, to avoid boxing the value class on every frame).

## What this ECS deliberately doesn't do

- No archetype/table storage — sparse-set per component type instead (see
  `.claude/agents/ecs-dev.md` for why, and when that tradeoff would need revisiting).
- No built-in scheduler, job system, or parallelism — single-threaded by design.
- No serialization — component types are plain data classes; use whatever serialization
  approach fits your game (`kotlinx.serialization` works fine against them).
- No reflection-based component registration — any `Any` works as a component the moment
  you `add` one.

## Benchmarking

`awake-ecs-benchmark` (a separate, JVM-only module) benchmarks this ECS against Fleks,
Artemis-odb, and Ashley on the same JVM/hardware. See
[docs/ecs-benchmark-scorecard.md](../docs/ecs-benchmark-scorecard.md) for the numbers,
methodology, and an honest account of where this ECS currently wins and where it doesn't.
