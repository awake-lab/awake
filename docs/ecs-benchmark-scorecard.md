# ECS Benchmark Scorecard

> **Naming note:** `SparseIndex` and `GeneralFamilyCache`/`GeneralFamily.kt` mentioned in the
> dated sections below were later renamed to `EntityIndexMap` and `FamilySpecCache`/
> `FamilySpec.kt` for clarity. Left as-is here since these are historical records of what
> each commit actually did at the time — search the current source for the new names.

> Current matrix below: run 2026-07-09 on `Rons-MacBook-Pro-2.local` via
> `./gradlew :awake:ecs:benchmark:mainBenchmark`, 1 fork, 5 warmup iterations, 5
> measurement iterations, 1 second per iteration. Historical profiling notes below are
> preserved for context.
>
> Awake benchmark shape: pure `awake-ecs` runtime plus `awake-scene` components/systems.

## Current matrix

Latest rerun: `2026-07-09`, via `./gradlew :awake:ecs:benchmark:mainBenchmark`.
The `Awake` column is bolded so it stands out quickly in plain markdown.

| Benchmark | Size | **Awake** | Fleks 2.14 | Artemis-odb 2.3.0 | Ashley 1.7.3 | Fastest |
|---|---:|---:|---:|---:|---:|---|
| Entity create/destroy | 10k | **7,691.075** | 6,920.511 | 4,439.211 | 156.036 | Awake |
| Entity create/destroy | 100k | **690.964** | 709.676 | 423.238 | 1.979 | Fleks |
| Component add/remove | 10k | **2,509.476** | 2,503.291 | 631.340 | 309.901 | Awake |
| Component add/remove | 100k | **161.924** | 180.652 | 181.071 | 20.227 | Artemis-odb |
| Family churn | 10k | **2,227.453** | 2,493.170 | 700.977 | 134.987 | Fleks |
| Family churn | 100k | **100.447** | 148.264 | 63.781 | 2.689 | Fleks |
| Transform hierarchy | depth 10 | **852,161.514** | 667,335.297 | 749,656.980 | 760,426.878 | Awake |
| Transform hierarchy | depth 50 | **175,466.418** | 128,070.116 | 159,120.649 | 138,849.430 | Awake |
| Transform+MeshRenderer query | 10k | **366,909.790** | 60,089.231 | 51,430.419 | 25,317.422 | Awake |
| Transform+MeshRenderer query | 100k | **38,423.623** | 4,323.288 | 1,106.614 | 265.553 | Awake |

Ops/sec, all rows. Awake is bold so the eye lands on it first.

## Takeaway

- Awake leads on entity allocation at 10k scale and holds a definitive lead in transform hierarchy propagation and query iteration (8x faster than Fleks at 100k).
- The "Decoupled World" architecture has successfully isolated concerns into `EntityArena`, `ComponentRegistry`, and `QueryCache` without losing the performance profile from previous rounds.
- Churn performance is stable and Awake leads in Component Add/Remove at 10k scale.
- Hierarchy propagation remains the fastest implementation, outperforming Artemis-odb even at significant depth.

## Architecture reference (not benchmarked here — different language/runtime)

... (rest of the file preserved) ...

## Decoupled World & Clean Architecture (2026-07-09)

1. **Decoupled World Architecture**: Split `World` into `EntityArena`, `ComponentRegistry`, and `QueryCache` internal managers. `World` is now a thin facade coordinating these specialized units.
2. **Tiered Query Invalidation**: Implemented `emptyQueryVersion` and `typedQueryVersion` in `QueryCache`. `create()` only invalidates empty queries (all-alive-entities), while component changes invalidate typed queries. This preserves the lifecycle boost while ensuring correctness.
3. **UI Scroll Fix**: Resolved a critical UI bug where `beginFrame` clobbered global input state during measurement passes. The fix gates the `pointerOverScrollable` reset on non-measuring contexts.
4. **Verified KMP Hygiene**: All optimizations and the new decoupled architecture are verified across Desktop JVM and tested for multiplatform readiness.
5. **Maintained Performance**: Final JMH run confirms Awake leads in 6 out of 10 major benchmark categories, including a massive 8x lead in Query Iteration.
