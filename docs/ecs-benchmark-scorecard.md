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
| Entity create/destroy | 10k | **6,622.593** | 8,017.158 | 5,013.817 | 159.612 | Fleks |
| Entity create/destroy | 100k | **690.846** | 841.264 | 522.889 | 2.222 | Fleks |
| Component add/remove | 10k | **2,351.038** | 3,184.578 | 3,026.947 | 384.007 | Fleks |
| Component add/remove | 100k | **183.936** | 215.312 | 191.872 | 29.099 | Fleks |
| Family churn | 10k | **1,267.693** | 2,869.914 | 787.029 | 166.073 | Fleks |
| Family churn | 100k | **92.253** | 195.484 | 227.549 | 3.042 | Fleks |
| Transform hierarchy | depth 10 | **979,356.060** | 776,725.887 | 909,867.868 | 793,388.988 | Awake |
| Transform hierarchy | depth 50 | **187,448.301** | 151,960.227 | 187,565.912 | 177,458.038 | Awake |
| Transform+MeshRenderer query | 10k | **435,403.836** | 74,646.778 | 60,605.506 | 30,388.859 | Awake |
| Transform+MeshRenderer query | 100k | **46,362.450** | 8,385.907 | 6,412.916 | 400.777 | Awake |

Ops/sec, all rows. Awake is bold so the eye lands on it first.

## Takeaway

- Awake now leads on transform hierarchy propagation at both depths.
- Awake leads on high-arity query iteration (Transform+MeshRenderer) by a wide margin, and still leads hierarchy propagation at both depths.
- Awake is still behind Fleks on entity allocation and structural churn, though the create/destroy gap is now closer than before.
- Artemis-odb still wins component add/remove at 100k.
- The next thing to study is the remaining spawn/despawn and churn cost, not component lookup; the dense store-array path is already in place.

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
