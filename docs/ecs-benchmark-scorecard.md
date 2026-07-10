# ECS Benchmark Scorecard

> **Naming note:** `SparseIndex` and `GeneralFamilyCache`/`GeneralFamily.kt` mentioned in the
> dated sections below were later renamed to `EntityIndexMap` and `FamilySpecCache`/
> `FamilySpec.kt` for clarity. Left as-is here since these are historical records of what
> each commit actually did at the time — search the current source for the new names.

> Current matrix below: run 2026-07-10 on `Rons-MacBook-Pro-2.local` via
> `./gradlew :awake-ecs-benchmark:mainBenchmark`, 1 fork, 5 warmup iterations, 5
> measurement iterations, 1 second per iteration -- refreshed after the allocation-focused
> pass described in "Query-path allocation cleanup" below. The family-churn rows were
> refreshed on 2026-07-10 with the targeted `.*FamilyChurn.*` slice after the latest ECS
> hot-path trim; this pass's full-suite rerun reconfirmed those numbers (differences are
> within JMH's own error bars -- see that section for the full before/after). Historical
> profiling notes below are preserved for context.
>
> Awake benchmark shape: pure `awake-ecs` runtime plus `awake-scene` components/systems.

## Current matrix

Latest family-churn rerun: `2026-07-10`, via the generated JMH jar with the targeted
`.*FamilyChurn.*` slice. Full-suite rerun same day after the query-path allocation cleanup.
The `Awake` column is bolded so it stands out quickly in plain markdown.

| Benchmark | Size | **Awake** | Fleks 2.14 | Artemis-odb 2.3.0 | Ashley 1.7.3 | Fastest |
|---|---:|---:|---:|---:|---:|---|
| Entity create/destroy | 10k | **8,980.199** | 7,954.765 | 5,289.436 | 163.722 | Awake |
| Entity create/destroy | 100k | **923.066** | 826.509 | 520.139 | 2.176 | Awake |
| Component add/remove | 10k | **2,478.772** | 3,144.501 | 3,212.090 | 384.218 | Artemis-odb |
| Component add/remove | 100k | **195.828** | 208.354 | 218.908 | 33.500 | Artemis-odb |
| Family churn | 10k | **2,719.368** | 3,149.871 | 857.560 | 193.231 | Fleks |
| Family churn | 100k | **147.990** | 191.294 | 262.156 | 3.156 | Artemis-odb |
| Transform hierarchy | depth 10 | **960,978.179** | 765,427.942 | 880,832.036 | 786,724.717 | Awake |
| Transform hierarchy | depth 50 | **188,449.196** | 146,676.767 | 183,962.596 | 157,574.179 | Awake |
| Transform+MeshRenderer query | 10k | **430,116.921** | 73,107.294 | 59,280.160 | 30,242.596 | Awake |
| Transform+MeshRenderer query | 100k | **45,968.488** | 7,658.620 | 5,392.895 | 341.731 | Awake |
| General `world.query(...)` iteration (diagnostic, Awake-only -- see below) | 10k | **16,856.988** | -- | -- | -- | n/a |
| General `world.query(...)` iteration (diagnostic, Awake-only -- see below) | 100k | **1,677.848** | -- | -- | -- | n/a |

Ops/sec, all rows. Awake is bold so the eye lands on it first.

## Takeaway

- Awake still leads entity create/destroy, transform hierarchy propagation, and
  Transform+MeshRenderer query iteration at both sizes, same as the prior run.
- Component add/remove and family churn move around between runs (Artemis-odb and Fleks
  trade the lead across sizes here) but the deltas from the prior recorded run are within
  JMH's own confidence intervals for most rows -- see "Query-path allocation cleanup" below
  for the one row this pass's changes could plausibly affect, and why it isn't this table.
  Read run-to-run swings here as noise, not as either ECS regressing or improving.
- Artemis-odb shows continued strength in component add/remove and, in this run, family
  churn at 100k -- consistent with the prior note that Awake's own churn/add-remove path is
  about as tight as it can get without the caller hoisting its own type lookups (see
  `awakeFamilyChurnCachedClass`/`awakeFamilyChurnTypeIdDirect` below).
- The new "General `world.query(...)` iteration" row is Awake-only (diagnostic, not a
  cross-ECS comparison) and isolates a real fix from this pass -- see below.

## Architecture reference (not benchmarked here — different language/runtime)

These are real, widely used ECS implementations that can't run in this JVM-based benchmark
module (different language, no JVM target, or a fundamentally different embedding model).
Included for architectural context only — **do not treat these as measured or directly
comparable** to the table above; they're on different hardware, different languages, and
often different problem framings (e.g. Unity DOTS assumes Burst-compiled jobs, not plain
JVM bytecode).

| ECS | Language | Architecture | Notes |
|---|---|---|---|
| [bevy_ecs](https://github.com/bevyengine/bevy) | Rust | Archetype (table-based) | Powers the Bevy game engine; widely cited as one of the fastest archetype ECS implementations, largely due to Rust's lack of GC pauses and tight memory control — not a like-for-like comparison against a JVM/Kotlin runtime. |
| [EnTT](https://github.com/skypjack/entt) | C++ | Sparse-set (same family as Awake) | The reference implementation most sparse-set ECS designs (including this one) trace their lineage to; header-only, zero-overhead-abstraction C++ templates give it a structural advantage no JVM sparse-set implementation can fully match. |
| [flecs](https://github.com/SanderMertens/flecs) | C | Archetype + relationships | Adds first-class entity relationships/hierarchies as a query primitive rather than a plain component field — a different modeling approach to what `Transform.parent` does here. |
| Unity DOTS (Entities package) | C# (Burst-compiled) | Archetype (chunk-based) | Designed around Unity's Job System + Burst compiler for SIMD/multi-threaded iteration; the performance story depends entirely on that compilation pipeline, which has no JVM/Kotlin equivalent. |

## Query-path allocation cleanup (2026-07-10)

A read-through of `World`/`ComponentStore`/`QueryCollector`/`FamilyRegistry`/`Families` found
the maintained-family hot paths (`Family1Cache`/`Family2Cache`/`FamilySpecCache`, what
`RenderSystem`/`TransformSystem` and the `awakeFamilyChurn`/`awakeTransformMeshQuery`
benchmarks exercise) already about as tight as they can get -- O(1) sparse-set add/remove,
typed dense-array iteration with no `checkNotNull` insertion, no per-call `KClass` lookups
on the cached-typeId fast paths. The two real remaining allocation sources were both in the
*general* `world.query(vararg types)` path (`QueryCollector`), which none of the existing
JMH benchmarks exercise (they all go through the maintained Family caches instead):

1. **`ComponentStore.entities` allocated a fresh `object : AbstractList<Entity>()` wrapper
   on every property access** instead of once per store. `QueryCollector.collect` reads this
   once per query recomputation, so every cache-miss re-query paid this allocation. Fixed by
   caching the view in a `private val` created once per `ComponentStore` instance -- the
   view still reads the enclosing instance's live `count`/`denseEntities` fields, so nothing
   about staleness changed, only the wrapper-object churn.
2. **`QueryCollector.collect`'s multi-type branch allocated three separate collections per
   call**: `types.mapNotNull { ... }` for resolving stores, then
   `smallestStore.entities.filter { queryStores.all { ... } }` -- and that `.entities` access
   is a `List<Entity>`, so iterating it through the `Iterable`/`List` interface boxes every
   `Entity` (a value class) at the interface boundary, on top of `filter`/`all` each
   allocating their own iterators. Rewritten to resolve stores into a plain array up front
   (bailing out to `emptyList()` as soon as any type has no store yet, same semantics as the
   old size-mismatch check) and to walk the smallest store via its own typed `forEach`
   (dense-array iteration, no `List` boxing) instead of `.entities.filter`.

Also added a `World.spawn(typeId, block)` overload alongside the existing `add(entity,
typeId)`/`remove(entity, typeId)` fast paths, so callers spawning many entities of the same
pooled component type in a loop have a non-reified option to hoist `T::class`'s resolution
out of the loop, mirroring this project's own "hoist `T::class` out of per-entity loops"
rule -- previously only the reified `spawn<T> { }` sugar existed.

**Why this doesn't show up in the "Current matrix" table above:** none of the existing
benchmarks call `world.query(...)`/`queryEach(vararg types)` -- `RenderSystem` and
`TransformSystem` (and every JMH benchmark modeled on them) use the maintained
`Family1`/`Family2` caches, which don't go through `ComponentStore.entities` or
`QueryCollector` at all in steady state. To measure the actual fix, a new Awake-only
diagnostic benchmark (`awakeGeneralQueryIteration`) was added: it forces a `QueryCache` miss
on every invocation (toggling a marker component on a scratch entity outside the query's own
type set) so it's actually exercising `QueryCollector.collect`'s recompute path each call,
not a cached map lookup.

Isolated A/B result (same benchmark code both times, run via a separate `git worktree`
checked out at the pre-change commit so the working tree stayed untouched, 1 fork, 5 warmup
+ 5 measurement iterations, 1s each):

| Entities | Before | After | Delta |
|---:|---:|---:|---:|
| 10k | 12,478.388 ops/s | 16,856.988 ops/s | +35.1% |
| 100k | 1,353.519 ops/s | 1,677.848 ops/s | +24.0% |

Full-suite rerun after these changes (`:awake-ecs-benchmark:mainBenchmark`, full matrix,
same params) showed every other row within JMH's own confidence intervals of the prior
run -- expected, since `Family1Cache`/`Family2Cache`/`ComponentRegistry`'s add/remove paths
weren't touched. See "Current matrix" above for the refreshed numbers.

Verified `:awake-ecs:desktopTest` (`EcsOptimizationTest`, `FamilySpecTest`, `WorldTest`) --
26/26 passing, no behavior change to query results, ordering, or family membership.

## Absolute Lead & Direct Store Caching (2026-07-09)

1. **Primitive entity storage**: `World` now stores generations, alive flags, and component
   signatures in primitive arrays instead of per-entity slot objects. Entity ids are still
   recycled through `EntityIdStack`.
2. **Correct query invalidation**: `World.create()` dirties query caches so empty queries
   (`world.query()` with no component filter) stay correct when entities are created.
3. **Direct store caching**: `FamilyCache` instances keep direct references to the
   `ComponentStore`s they monitor, reducing `World` lookup overhead during family updates.
4. **Signature guard**: component signatures are backed by one `Long`, so `World` now fails
   clearly after 64 component types instead of silently colliding signature bits.
5. **Verified correctness**: `:awake-ecs:allTests` passes on desktop and iOS simulator;
   `:awake-scene:desktopTest` also passes.
6. **Churn add-path trim**: `Family1Cache` and `Family2Cache` now append directly on new
   component inserts, because `World.add(..., component)` already routes replacements to
   `replaceComponent(...)`. That removes a redundant membership probe from the churn hot path.
7. **Sparse index cleanup**: removed component ids are cleared out of `EntityIndexMap`,
   which lets `ComponentStore` and family caches drop stale membership checks on remove/
   re-add churn and keeps the sparse-set invariant easier to reason about.
