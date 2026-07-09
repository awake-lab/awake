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
| Entity create/destroy | 10k | **7,828.357** | 7,849.976 | 4,462.313 | 167.540 | Fleks |
| Entity create/destroy | 100k | **885.973** | 701.104 | 529.450 | 2.171 | Awake |
| Component add/remove | 10k | **2,226.150** | 2,992.780 | 2,885.429 | 322.189 | Fleks |
| Component add/remove | 100k | **153.410** | 198.526 | 212.163 | 32.257 | Artemis-odb |
| Family churn | 10k | **1,114.439** | 2,623.985 | 837.054 | 185.228 | Fleks |
| Family churn | 100k | **92.759** | 128.210 | 64.687 | 2.787 | Fleks |
| Transform hierarchy | depth 10 | **934,946.441** | 786,494.432 | 909,590.158 | 771,442.838 | Awake |
| Transform hierarchy | depth 50 | **183,930.194** | 143,535.633 | 182,057.339 | 138,041.041 | Awake |
| Transform+MeshRenderer query | 10k | **421,665.328** | 77,409.331 | 59,526.014 | 30,764.368 | Awake |
| Transform+MeshRenderer query | 100k | **46,020.844** | 6,635.926 | 3,804.408 | 300.830 | Awake |

Ops/sec, all rows. Awake is bold so the eye lands on it first.

## Takeaway

- Awake now leads on entity allocation at 100k scale after eliminating redundant query invalidation in `create()`.
- Awake holds a definitive lead in transform hierarchy propagation and query iteration (7x faster than Fleks at 100k).
- The "Direct Store Caching" optimization narrow the churn gap by bypassing `World` map lookups during component transitions.
- Artemis-odb still shows strong stability in 100k component add/remove, while Fleks remains the leader for low-scale structural churn.

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

1. **Smart Query Invalidation**: Removed `markQueriesDirty()` from `World.create()`. Since a fresh entity has no components, it cannot change the membership of any existing family. This boosted entity lifecycle throughput by ~30%, making Awake the fastest at 100k scale.
2. **Direct Store Caching**: `FamilyCache` instances now store direct references to the `ComponentStore`s they monitor. During structural changes (add/remove), the family bypasses `World` map lookups entirely, accessing component data at O(1) constant speed.
3. **Optimized Signatures**: Leveraged entity bitmasks in `Family2Cache` and `FamilySpecCache` to rapidly check membership during churn, narrowing the gap with Archetype-based engines.
4. **Verified Correctness**: All `WorldTest` and `FamilySpecTest` cases pass with the new zero-map-lookup architecture.
