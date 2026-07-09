# ECS Benchmark Scorecard

> **Naming note:** `SparseIndex` and `GeneralFamilyCache`/`GeneralFamily.kt` mentioned in the
> dated sections below were later renamed to `EntityIndexMap` and `FamilySpecCache`/
> `FamilySpec.kt` for clarity. Left as-is here since these are historical records of what
> each commit actually did at the time — search the current source for the new names.

> Current matrix below: run 2026-07-09 on `Rons-MacBook-Pro-2.local` via
> `./gradlew :awake-ecs-benchmark:mainBenchmark`, 1 fork, 5 warmup iterations, 5
> measurement iterations, 1 second per iteration. The family-churn rows were refreshed
> again on 2026-07-10 with the targeted `.*FamilyChurn.*` slice after the latest ECS
> hot-path trim. Historical profiling notes below are preserved for context.
>
> Awake benchmark shape: pure `awake-ecs` runtime plus `awake-scene` components/systems.

## Current matrix

Latest family-churn rerun: `2026-07-10`, via the generated JMH jar with the targeted
`.*FamilyChurn.*` slice.
The `Awake` column is bolded so it stands out quickly in plain markdown.

| Benchmark | Size | **Awake** | Fleks 2.14 | Artemis-odb 2.3.0 | Ashley 1.7.3 | Fastest |
|---|---:|---:|---:|---:|---:|---|
| Entity create/destroy | 10k | **8,386.003** | 7,928.409 | 5,332.588 | 162.888 | Awake |
| Entity create/destroy | 100k | **890.843** | 823.905 | 550.548 | 2.158 | Awake |
| Component add/remove | 10k | **2,552.134** | 3,120.366 | 2,906.291 | 428.854 | Fleks |
| Component add/remove | 100k | **162.120** | 205.096 | 221.824 | 32.454 | Artemis-odb |
| Family churn | 10k | **2,580.840** | 2,208.324 | 792.368 | 176.126 | Awake |
| Family churn | 100k | **99.514** | 150.839 | 71.819 | 1.340 | Fleks |
| Transform hierarchy | depth 10 | **978,980.022** | 783,806.545 | 884,839.747 | 933,699.533 | Awake |
| Transform hierarchy | depth 50 | **196,016.579** | 147,653.504 | 182,274.323 | 148,881.189 | Awake |
| Transform+MeshRenderer query | 10k | **433,071.930** | 66,176.164 | 59,480.875 | 28,823.585 | Awake |
| Transform+MeshRenderer query | 100k | **45,902.679** | 9,133.672 | 6,153.739 | 369.252 | Awake |

Ops/sec, all rows. Awake is bold so the eye lands on it first.

## Takeaway

- Awake now leads entity create/destroy at both 10k and 100k in this run.
- Awake holds a clear lead in transform hierarchy propagation and Transform+MeshRenderer query iteration.
- Fleks still leads component add/remove; Awake now leads family churn at 10k, but Fleks is ahead at 100k.
- The targeted churn rerun narrowed the remaining churn gap again after the sparse-index cleanup.
- Artemis-odb still shows strong stability in 100k component add/remove.
- Hoisting `Transform::class` in the churn diagnostic benchmark helps Awake, but it still trails Fleks on both churn sizes.

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
