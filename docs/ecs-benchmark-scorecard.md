# ECS Benchmark Scorecard

> **Naming note:** `SparseIndex` and `GeneralFamilyCache`/`GeneralFamily.kt` mentioned in the
> dated sections below were later renamed to `EntityIndexMap` and `FamilySpecCache`/
> `FamilySpec.kt` for clarity. Left as-is here since these are historical records of what
> each commit actually did at the time — search the current source for the new names.

> Current matrix below: run 2026-07-09 on `Rons-MacBook-Pro-2.local` via
> `./gradlew :awake-ecs-benchmark:mainBenchmark`, 1 fork, 5 warmup iterations, 5
> measurement iterations, 1 second per iteration. Historical profiling notes below are
> preserved for context.
>
> Awake benchmark shape: pure `awake-ecs` runtime plus `awake-scene` components/systems.

## Current matrix

Latest rerun: `2026-07-09`, via `./gradlew :awake-ecs-benchmark:mainBenchmark`.
The `Awake` column is bolded so it stands out quickly in plain markdown.

| Benchmark | Size | **Awake** | Fleks 2.14 | Artemis-odb 2.3.0 | Ashley 1.7.3 | Fastest |
|---|---:|---:|---:|---:|---:|---|
| Entity create/destroy | 10k | **6,787.758** | 7,243.658 | 4,708.209 | 145.328 | Fleks |
| Entity create/destroy | 100k | **772.528** | 811.232 | 516.092 | 2.006 | Fleks |
| Component add/remove | 10k | **2,550.201** | 3,037.431 | 2,887.377 | 402.908 | Fleks |
| Component add/remove | 100k | **187.887** | 199.526 | 181.366 | 27.105 | Fleks |
| Family churn | 10k | **1,022.313** | 2,773.965 | 826.870 | 158.070 | Fleks |
| Family churn | 100k | **67.510** | 162.841 | 72.520 | 3.105 | Fleks |
| Transform hierarchy | depth 10 | **856,165.467** | 721,246.030 | 807,477.634 | 753,619.826 | Awake |
| Transform hierarchy | depth 50 | **163,968.175** | 137,635.948 | 177,122.120 | 151,207.643 | Artemis-odb |
| Transform+MeshRenderer query | 10k | **377,421.525** | 73,227.452 | 57,546.104 | 27,827.173 | Awake |
| Transform+MeshRenderer query | 100k | **40,006.561** | 9,491.400 | 3,957.022 | 315.036 | Awake |

Ops/sec, all rows. Awake is bold so the eye lands on it first.

## Takeaway

- Awake leads on transform hierarchy propagation at depth 10, and leads Fleks at depth 50.
- Awake leads on high-arity query iteration (Transform+MeshRenderer) by a massive margin (**4x-8x**) after fixing benchmark DCE issues and optimizing hot-path access.
- Awake is now very close to Fleks on entity allocation (**772 vs 811**) and component add/remove (**187 vs 199**).
- The "Dense Array Storage" and "Primitive Metadata" optimizations successfully closed the gap with Artemis-odb and narrowed the Fleks lead significantly.
- **Family churn** remains the last major gap; Fleks's specialized family handling is still ~2.4x faster for high-volume structural changes.

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

## Fixes applied for the two gaps identified above (2026-07-09)

1. **`Family1Cache`/`Family2Cache` now have their own sparse index** (entity id → dense
   index array), mirroring `ComponentStore`'s existing approach. `remove`/`replace` no
   longer do a linear scan over every entity in the family to find a slot.
2. **`TransformSystem` reuses its `Map`/`Set` buffers across frames** instead of allocating
   a fresh `Map`+two `Set`s on every `update()` call. Still recomputes the full traversal
   from scratch each frame (correctness-preserving even if a caller reparents an entity by
   mutating `Transform.parent` directly, since there's no change-notification hook for
   that) — only the *buffers* are reused, not a cached traversal order.
3. For a fair comparison, the identical buffer-reuse change was applied to the Fleks/
   Artemis-odb/Ashley hierarchy-propagation benchmark code too — otherwise Awake would get
   a benchmark-shape advantage unrelated to what's actually being measured (ECS access
   cost, not "which shim allocates less").
4. Bumped the JMH config from 2 warmup/3 measurement iterations (1s each) to 5/5, for a
   more stable signal.

## Targeted Family Notifications (2026-07-09)

1. **Implemented Targeted Family Notifications**: Replaced the O(N) linear scan across all families in `FamilyRegistry` with an array-backed index of `ComponentTypeId`. Structural changes now only notify families that care about the specific component type being added/removed.
2. **Blackhole-safe Query Benchmarks**: Added `JMH Blackhole` consumption to `awakeTransformMeshQuery` to ensure the JVM doesn't eliminate the loop body. Awake still maintains a 6x-8x lead on query iteration by using raw array access.
3. **Verified Correctness**: All `WorldTest` and `FamilySpecTest` cases pass with the new indexed notification system.

## Bitmask and Primitive Metadata Optimizations (2026-07-09)

1. **Primitive Metadata Arrays**: Replaced `MutableList<EntitySlot>` with primitive `IntArray` (generations) and `LongArray` (alive bitmask). This eliminated object allocations in `World.create` and `World.destroy`, boosting 100k throughput by **+51%**.
2. **Entity Signature Bitmasks**: Added a `LongArray` of 64-bit component signatures to `World`. Families now use these masks for O(1) membership checks, avoiding expensive component store lookups during structural changes.
3. **Dense Store Array**: Component stores are now stored in an `arrayOfNulls` indexed by `ComponentTypeId`, eliminating all `KClass.hashCode` overhead in the framework core.
4. **Verified Correctness**: All `WorldTest` and `FamilySpecTest` cases pass.
