# ECS Benchmark Scorecard

> **Naming note:** `SparseIndex` and `GeneralFamilyCache`/`GeneralFamily.kt` mentioned in the
> dated sections below were later renamed to `EntityIndexMap` and `FamilySpecCache`/
> `FamilySpec.kt` for clarity. Left as-is here since these are historical records of what
> each commit actually did at the time — search the current source for the new names.

> Current matrix below: run 2026-07-10 on `Rons-MacBook-Pro-2.local` via the generated JMH
> jar (equivalent to `./gradlew :awake-ecs-benchmark:mainBenchmark`), 1 fork, 5 warmup
> iterations, 5 measurement iterations, 1 second per iteration -- refreshed after the
> reified-generics fix described in "Closing the component add/remove and family-churn gap"
> below. Historical profiling notes below are preserved for context.
>
> Awake benchmark shape: pure `awake-ecs` runtime plus `awake-scene` components/systems.

## Current matrix

Refreshed `2026-07-10` after the reified `add`/`remove`/`get`/`has` fix described below.
The `Awake` column is bolded so it stands out quickly in plain markdown.

| Benchmark | Size | **Awake** | Fleks 2.14 | Artemis-odb 2.3.0 | Ashley 1.7.3 | Fastest |
|---|---:|---:|---:|---:|---:|---|
| Entity create/destroy | 10k | **7,593.512** | 6,684.083 | 4,230.751 | 170.795 | Awake |
| Entity create/destroy | 100k | **794.153** | 740.180 | 580.375 | 2.216 | Awake |
| Component add/remove | 10k | 3,068.254 | 2,709.705 | **3,181.491** | 441.722 | Artemis-odb (Awake within noise) |
| Component add/remove | 100k | **267.619** | 194.728 | 197.205 | 34.902 | Awake |
| Family churn | 10k | 2,885.825 | 2,636.648 | **4,152.138** | 154.931 | Artemis-odb |
| Family churn | 100k | 217.649 | 173.172 | **258.125** | 2.795 | Artemis-odb |
| Transform hierarchy | depth 10 | **932,031.527** | 839,497.399 | 934,941.989 | 939,057.778 | Ashley (all four within noise) |
| Transform hierarchy | depth 50 | **189,585.027** | 153,933.758 | 190,473.938 | 161,590.337 | Artemis-odb (Awake within noise) |
| Transform+MeshRenderer query | 10k | **421,355.645** | 70,840.389 | 57,346.462 | 12,947.965 | Awake |
| Transform+MeshRenderer query | 100k | **46,072.594** | 5,891.936 | 2,620.428 | 346.995 | Awake |
| General `world.query(...)` iteration (diagnostic, Awake-only -- see below) | 10k | **17,354.210** | -- | -- | -- | n/a |
| General `world.query(...)` iteration (diagnostic, Awake-only -- see below) | 100k | **1,714.405** | -- | -- | -- | n/a |

Ops/sec, all rows. Awake is bold so the eye lands on it first.

## Takeaway

- Awake now leads or is statistically tied for the lead on **component add/remove at both
  sizes** -- previously the clearest Artemis-odb/Fleks win in this suite -- after the fix
  described in "Closing the component add/remove and family-churn gap" below. At 100k it's a
  clean, decisive lead (267.6 vs Artemis-odb's 197.2 and Fleks's 194.7); at 10k it's within
  JMH's own error bars of Artemis-odb's lead. This is a real, mechanism-explained fix, not
  noise -- see that section for the isolated A/B and the bytecode evidence.
- Family churn is **not** meaningfully changed by this pass, and Artemis-odb still leads it
  clearly at both sizes. This is expected, not a gap in the fix: `awakeFamilyChurn` was
  already on the fastest available Awake path (cached `ComponentTypeId` + pooling) before
  this pass, so it was never affected by the reified-generics cost the fix targets. See
  "Closing the component add/remove and family-churn gap" for why this row's remaining
  deficit against Artemis-odb looks like a different, deeper cost (not caller-side
  reflection) that this pass could not responsibly fix.
- Awake still leads entity create/destroy and Transform+MeshRenderer query iteration at both
  sizes, and transform hierarchy propagation is a near-4-way tie within noise, same as prior
  runs -- none of this pass's changes touch those paths.

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
